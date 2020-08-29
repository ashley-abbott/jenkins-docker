ARG VERSION=lts
FROM jenkins/jenkins:$VERSION

LABEL MAINTAINER="ashley@ashleyabbott.me"
LABEL DESCRIPTION="Jenkins instance which allows overwiting authentication method via env vars"
LABEL VERSION="0.1.0"

ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false
ENV JENKINS_NUM_EXECUTORS 0
ENV JENKINS_SAML_MAXIMUM_AUTHENTICATION_LIFETIME 86400
ENV JENKINS_UI_URL 'http://{your-domain}:{port}'
ENV JENKINS_USER administrator
ENV JENKINS_PASS bT9%*8PK2.mTT~^;
ENV SLACK_DOMAIN YourSlackDomain
ENV SLACK_CHANEL #general
ENV SLACK_TOKEN 12345678910a
ENV GIT_KEY '-----BEGIN RSA PRIVATE KEY-----\nreplace\nwith\none\nline\nprivate\nkey\n-----END RSA PRIVATE KEY-----'

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
USER root
RUN apt-get update && \
    apt-get install vim -y && \
    rm -rf /var/lib/apt/lists/*
USER jenkins
RUN install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

COPY 01-configure.groovy /usr/share/jenkins/ref/init.groovy.d/01-configure.groovy
COPY 02-slack.groovy /usr/share/jenkins/ref/init.groovy.d/02-slack.groovy

EXPOSE 8080