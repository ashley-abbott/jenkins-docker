import hudson.security.csrf.DefaultCrumbIssuer
import jenkins.model.*
import hudson.security.*
import org.jenkinsci.plugins.*
import org.jenkinsci.plugins.saml.*

def isValidString = { value ->
    if (value != null && value instanceof String && value.trim() != "") {
        return true
    }
    return false
}

def env = System.getenv()
def jenkins = Jenkins.getInstance()

int num_executors = 2
if (isValidString(env.JENKINS_NUM_EXECUTORS)) {
  num_executors = env.JENKINS_NUM_EXECUTORS.toInteger()
}

jenkins.setNumExecutors(num_executors)

// Enable CSRF protection
jenkins.setCrumbIssuer(new DefaultCrumbIssuer(true))

// Disable old/unsafe agent protocols for security
jenkins.agentProtocols = ["JNLP4-connect", "Ping"] as Set

// disabled CLI access over TCP listener (separate port)
def p = jenkins.AgentProtocol.all()
p.each { x ->
    if (x.name?.contains("CLI")) {
        println "Removing protocol ${x.name}"
        p.remove(x)
    }
}

// get Jenkins location configuration
urlConfig = JenkinsLocationConfiguration.get()

// set Jenkins URL
urlConfig.setUrl(env.JENKINS_UI_URL)

// save current Jenkins state to disk
urlConfig.save()

// Configure Matrix-based Security
def configureMatrixAuthorizationStrategy = { jenkinsUser, jenkinsPassword ->
    if (!isValidString(jenkinsUser)) {
        throw new Throwable("'JENKINS_USER' is required to create the initial admin user")
    }
    if (!isValidString(jenkinsPassword)) {
        throw new Throwable("'JENKINS_PASS' is required to create the initial admin user")
    }

    if(!(jenkins.getSecurityRealm() instanceof HudsonPrivateSecurityRealm))
        jenkins.setSecurityRealm(new HudsonPrivateSecurityRealm(false))

    if(!(jenkins.getAuthorizationStrategy() instanceof GlobalMatrixAuthorizationStrategy))
        jenkins.setAuthorizationStrategy(new GlobalMatrixAuthorizationStrategy())

    def user = jenkins.getSecurityRealm().createAccount(jenkinsUser, jenkinsPassword)
    user.save()
    jenkins.getAuthorizationStrategy().add(Jenkins.ADMINISTER, jenkinsUser)
    jenkins.save()
}

// Configure SAML Security
def configureSAMLAuthorizationStrategy = { idp_metadata_configuration,
                                           displayNameAttributeName,
                                           groupsAttributeName,
                                           maximumAuthenticationLifetime,
                                           usernameAttributeName,
                                           emailAttributeName,
                                           logoutUrl,
                                           usernameCaseConversion,
                                           binding,
                                           samlCustomAttributes ->
    if (!isValidString(idp_metadata_configuration)) {
        throw new Throwable("'JENKINS_SAML_IDP_METADATA' is required")
    }

    // https://github.com/jenkinsci/saml-plugin/blob/master/src/main/java/org/jenkinsci/plugins/saml/SamlSecurityRealm.java
    /**
     * @param idpMetadataConfiguration      How to obtains the IdP Metadata configuration.
     * @param displayNameAttributeName      attribute that has the displayname
     * @param groupsAttributeName           attribute that has the groups
     * @param maximumAuthenticationLifetime maximum time that an identification it is valid
     * @param usernameAttributeName         attribute that has the username
     * @param emailAttributeName            attribute that has the email
     * @param logoutUrl                     optional URL to redirect on logout
     * @param advancedConfiguration         advanced configuration settings
     * @param encryptionData                encryption configuration settings
     * @param usernameCaseConversion        username case sensitive settings
     * @param binding                       SAML binding method.
     * @param samlCustomAttributes          Custom Attributes to read from the SAML Responsse.
     * @throws IOException if it is not possible to write the IdP metadata file.
     */
    def securityRealm = new SamlSecurityRealm(
            new IdpMetadataConfiguration(idp_metadata_configuration),
            displayNameAttributeName ?: 'http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name',
            groupsAttributeName ?: 'http://schemas.xmlsoap.org/claims/Group',
            maximumAuthenticationLifetime.toInteger(),
            usernameAttributeName ?: '',
            emailAttributeName ?: '',
            logoutUrl ?: '',
            null,
            null,
            usernameCaseConversion ?: '',
            binding ?: 'urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect',
            samlCustomAttributes ?: new ArrayList<>(),
    )

    jenkins.setSecurityRealm(securityRealm)

    def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
    strategy.setAllowAnonymousRead(false)
    jenkins.setAuthorizationStrategy(strategy)

    jenkins.save()
}

// Configure Authorization Strategy ('SAML' or 'Matrix')
def jenkinsAuthorizationStrategy = env.JENKINS_AUTHORIZATION_STRATEGY ?: 'Matrix'
switch (jenkinsAuthorizationStrategy) {
    case "None":
        // Do nothing. We just don't want to override the security settings in Jenkins that were set up manually
        break
    case "Matrix":
        configureMatrixAuthorizationStrategy(
                env.JENKINS_USER,
                env.JENKINS_PASS
        )
        break
    case "SAML":
        configureSAMLAuthorizationStrategy(
                env.JENKINS_SAML_IDP_METADATA,
                env.JENKINS_SAML_DISPLAY_NAME_ATTRIBUTE_NAME,
                env.JENKINS_SAML_GROUPS_ATTRIBUTE_NAME,
                env.JENKINS_SAML_MAXIMUM_AUTHENTICATION_LIFETIME,
                env.JENKINS_SAML_USERNAME_ATTRIBUTE_NAME,
                env.JENKINS_SAML_EMAIL_ATTRIBUTE_NAME,
                env.JENKINS_SAML_LOGOUT_URL,
                env.JENKINS_SAML_USERNAME_CASE_CONVERSION,
                env.JENKINS_SAML_BINDING,
                env.JENKINS_SAML_CUSTOM_ATTRIBUTES
        )
        break
    default:
        throw new Throwable("Invalid 'JENKINS_AUTHORIZATION_STRATEGY'")
}