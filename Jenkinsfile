#!groovy

/* Required Jenkins plugins:
- https://wiki.jenkins-ci.org/display/JENKINS/Timestamper
- https://wiki.jenkins-ci.org/display/JENKINS/Static+Code+Analysis+Plug-ins
- https://wiki.jenkins-ci.org/display/JENKINS/Checkstyle+Plugin
- https://wiki.jenkins-ci.org/display/JENKINS/FindBugs+Plugin
- https://wiki.jenkins-ci.org/display/JENKINS/PMD+Plugin
- https://wiki.jenkins-ci.org/display/JENKINS/DRY+Plugin
- https://wiki.jenkins-ci.org/display/JENKINS/Task+Scanner+Plugin
- https://wiki.jenkins-ci.org/display/JENKINS/Javadoc+Plugin
- https://wiki.jenkins-ci.org/display/JENKINS/JaCoCo+Plugin
*/

// Only keep the 10 most recent builds
properties([[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', numToKeepStr: '10']]])

// Pipeline steps
timestamps {
    node {
        stage('Checkout') {
            checkout scm
        }

        stage('Build') {
            mvn 'clean package -B -V -U -e -DskipTests'
        }

        stage('Static Code Analysis') {
            mvn 'checkstyle:check pmd:check pmd:cpd-check findbugs:check -B -e'
            step([$class: 'CheckStylePublisher', pattern: 'target/checkstyle-result.xml'])
            step([$class: 'FindBugsPublisher', pattern: 'target/findbugsXml.xml'])
            step([$class: 'PmdPublisher', pattern: 'target/pmd.xml'])
            step([$class: 'DryPublisher', pattern: 'target/cpd.xml'])
            step([$class: 'TasksPublisher', high: 'FIXME', low: '', normal: 'TODO', pattern: 'src/**/*.java'])
        }

        stage('Documentation') {
            mvn 'javadoc:javadoc -B -e'
            step([$class: 'JavadocArchiver', javadocDir: 'target/site/apidocs', keepAll: false])
        }

        stage('Unit Tests') {
            mvn 'test-compile jacoco:prepare-agent surefire:test -B -e'
            step([$class: 'JUnitResultArchiver', testResults: 'target/surefire-reports/TEST-*.xml'])
        }

        stage('Code Coverage') {
            step([$class: 'JacocoPublisher', execPattern: 'target/jacoco.exec', exclusionPattern: '**/Messages.class'])
        }

        stage('Archive Artifacts') {
            step([$class: 'ArtifactArchiver', artifacts: 'target/*.hpi,target/*.jpi', fingerprint: true])
        }
    }
}

// Run Maven from tool "mvn"
void mvn(def args) {
    // Get JDK tool installation
    def jdkHome = tool name: 'JDK7', type: 'hudson.model.JDK'

    // Get Maven tool installation
    def mvnHome = tool name: 'M3', type: 'hudson.tasks.Maven$MavenInstallation'

    // Set JAVA_HOME, MAVEN_HOME and special PATH variables
    List javaEnv = [
        "PATH+JDK=${jdkHome}/bin",
        "PATH+MVN=${mvnHome}/bin",
        "JAVA_HOME=${jdkHome}",
        "MAVEN_HOME=${mvnHome}",
        // Additional variables needed by tests on machines
        // that don't have global git user.name and user.email configured.
        'GIT_COMMITTER_EMAIL=me@hatescake.com',
        'GIT_COMMITTER_NAME=Hates',
        'GIT_AUTHOR_NAME=Cake',
        'GIT_AUTHOR_EMAIL=hates@cake.com',
        'LOGNAME=hatescake'
    ]

    // Call Maven within Java environment
    withEnv(javaEnv) {
        timeout(time: 60, unit: 'MINUTES') {
            if (isUnix()) {
                sh "${mvnHome}/bin/mvn ${args}"
            } else {
                bat "${mvnHome}\\bin\\mvn ${args}"
            }
        }
    }
}
