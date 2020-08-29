# Jenkins Docker image

The Jenkins Continuous Integration and Delivery server.

<img src="https://jenkins.io/sites/default/files/jenkins_logo.png"/>

## Getting Started

These instructions will cover usage information and for the docker container

### Prerequisities

In order to run this container you'll need docker installed.

* [Windows](https://docs.docker.com/windows/started)
* [OS X](https://docs.docker.com/mac/started/)
* [Linux](https://docs.docker.com/linux/started/)

### Usage

Run the image as a daemon

```shell
docker run -d <your image name>/jenkins:0.1.0
```

Shell into started container

```shell
docker exec -it <your image name>/jenkins:0.1.0 bash
```

### Environment Variables

* `JENKINS_UI_URL` - Optionally specify the HTTP address of the Jenkins installation, such as <http://yourhost.yourdomain/jenkins/> This value is used to let Jenkins know how to refer to itself, ie. to display images or to create links in emails.
* `JENKINS_NUM_EXECUTORS` - Number of executors Jenkins can use
* `SLACK_DOMAIN` - Slack domain **Must change to enable Slack notifications** i.e 'ashleyabbottme'
* `SLACK_CHANEL` - Slack chanel to post notification to **default** - '#general'
* `SLACK_TOKEN` - Integration token required for Slack
* `GIT_KEY` - Private key used to retrieve projects

#### Authentication

##### MATRIX (Username|Password)

* `JENKINS_USER` - Initial administrative username to create **default** - 'administrator'
* `JENKINS_PASS` - Password to set for `JENKINS_USER` **default** - '2EMZGvLpBJQ7gJMu'

##### SAML

* `JENKINS_AUTHORIZATION_STRATEGY` - Default of Matrix authorization (U/N and P/W) set to `SAML` to use SSO
* `JENKINS_SAML_IDP_METADATA` - Flattened (one line) IdP metadata xml file provided by SAML provider
* `JENKINS_SAML_DISPLAY_NAME_ATTRIBUTE_NAME` - Fill name of display name attribute in SAML response, **default** <http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name>
* `JENKINS_SAML_GROUPS_ATTRIBUTE_NAME` - Fill name of groups attribute in SAML response, **default** <http://schemas.xmlsoap.org/claims/Group>
* `JENKINS_SAML_MAXIMUM_AUTHENTICATION_LIFETIME` - Number of seconds since user was authenticated in IdP while his authentication is considering as active **default** 86400
* `JENKINS_SAML_USERNAME_ATTRIBUTE_NAME` - The SAML attribute to use as the username
* `JENKINS_SAML_EMAIL_ATTRIBUTE_NAME` - Fill name of email attribute in SAML response

To flatten the XML file use the following:

```shell
cat file.xml | awk '{$1=$1};1' | tr -d "\n\r"
```

## Authors

* **Ashley Abbott**
