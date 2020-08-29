import jenkins.model.*
import hudson.security.*
import hudson.model.*
import hudson.slaves.EnvironmentVariablesNodeProperty

/*
This section configures the Slack integration
*/

if(!binding.hasVariable('slack_settings')) {
  slack_settings = [:]
}
if(!(slack_settings instanceof Map)) {
  throw new Exception('slack_settings must be a Map.')
}

slackDomain = System.env.SLACK_DOMAIN
slackChanel = System.env.SLACK_CHANEL

def slack = Jenkins.instance.getExtensionList('jenkins.plugins.slack.SlackNotifier$DescriptorImpl')[0]

boolean save = false
String teamDomain = (slack_settings['teamDomain'])?:slackDomain
String tokenCredentialId = (slack_settings['tokenCredentialId'])?:'jenkins-slack'
String room = (slack_settings['room'])?:slackChanel

if(teamDomain != slack.teamDomain) {
  slack.teamDomain = teamDomain
  save = true
}
if(tokenCredentialId != slack.tokenCredentialId) {
  slack.tokenCredentialId = tokenCredentialId
  save = true
}
if(room != slack.room) {
  slack.room = room
  save = true
}
if(save) {
  println 'Slack configured.'
  slack.save()
}
else {
  println 'Nothing changed.  Slack already configured.'
}

/*
Imports needed to allow interaction with credentials
*/

import com.cloudbees.plugins.credentials.*;
import com.cloudbees.plugins.credentials.domains.*;
import org.jenkinsci.plugins.plaincredentials.*
import org.jenkinsci.plugins.plaincredentials.impl.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*
import hudson.util.Secret

/*
Add secret text for jenkins-slack integration
*/

slackToken = System.env.SLACK_TOKEN

Credentials secretText = (Credentials) new StringCredentialsImpl(
CredentialsScope.GLOBAL,
"jenkins-slack", // id
"Jenkins Slack", // description
Secret.fromString(slackToken) // secret
)

SystemCredentialsProvider.getInstance().getStore().addCredentials(Domain.global(), secretText)

/*
Create Jenkins user with SSH key
*/

gitKey = System.env.GIT_KEY

private_key_source = new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(gitKey)
Credentials user_with_key = new BasicSSHUserPrivateKey(
CredentialsScope.GLOBAL,
"jenkins-git", // id
"jenkins", // username
private_key_source, // private key source
null, // passphrase
"jenkins-git" // description
)

SystemCredentialsProvider.getInstance().getStore().addCredentials(Domain.global(), user_with_key)
