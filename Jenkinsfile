timestamps {
    node {
        checkout scm

        def mvnHome = tool 'M3'
        env.JAVA_HOME = tool 'JDK7'

        if (isUnix()) {
            stage 'Build'
            sh "${mvnHome}/bin/mvn -B -e -V -U clean package -DskipTests"

            stage 'Static Code Analysis'
            sh "${mvnHome}/bin/mvn -B -e checkstyle:check pmd:check pmd:cpd-check findbugs:check"
            step([$class: 'CheckStylePublisher', pattern: 'target/checkstyle-result.xml'])
            step([$class: 'FindBugsPublisher', pattern: 'target/findbugsXml.xml'])
            step([$class: 'PmdPublisher', pattern: 'target/pmd.xml'])
            step([$class: 'DryPublisher', pattern: 'target/cpd.xml'])
            step([$class: 'TasksPublisher', high: 'FIXME', low: '', normal: 'TODO', pattern: 'src/**/*.java'])

            stage 'Documentation'
            sh "${mvnHome}/bin/mvn -B -e javadoc:javadoc"
            step([$class: 'JavadocArchiver', javadocDir: 'target/site/apidocs', keepAll: false])

            stage 'Unit Tests'
            sh "${mvnHome}/bin/mvn -B -e test-compile jacoco:prepare-agent surefire:test"
            step([$class: 'JUnitResultArchiver', testResults: 'target/surefire-reports/*.xml'])

            stage 'Code Coverage'
            step([$class: 'JacocoPublisher', execPattern: 'target/jacoco.exec', exclusionPattern: '**/Messages.class'])

            stage 'Archive Artifacts'
            step([$class: 'ArtifactArchiver', artifacts: 'target/ecutest.hpi', fingerprint: true])
        } else {
            stage 'Build'
            bat "${mvnHome}\\bin\\mvn -B -e -V -U clean package -DskipTests"

            stage 'Static Code Analysis'
            bat "${mvnHome}\\bin\\mvn -B -e checkstyle:check pmd:check pmd:cpd-check findbugs:check"
            step([$class: 'CheckStylePublisher', pattern: 'target/checkstyle-result.xml'])
            step([$class: 'FindBugsPublisher', pattern: 'target/findbugsXml.xml'])
            step([$class: 'PmdPublisher', pattern: 'target/pmd.xml'])
            step([$class: 'DryPublisher', pattern: 'target/cpd.xml'])
            step([$class: 'TasksPublisher', high: 'FIXME', low: '', normal: 'TODO', pattern: 'src/**/*.java'])

            stage 'Documentation'
            bat "${mvnHome}\\bin\\mvn -B -e javadoc:javadoc"
            step([$class: 'JavadocArchiver', javadocDir: 'target/site/apidocs', keepAll: false])

            stage 'Unit Tests'
            bat "${mvnHome}\\bin\\mvn -B -e test-compile jacoco:prepare-agent surefire:test"
            step([$class: 'JUnitResultArchiver', testResults: 'target/surefire-reports/*.xml'])

            stage 'Code Coverage'
            step([$class: 'JacocoPublisher', execPattern: 'target/jacoco.exec', exclusionPattern: '**/Messages.class'])

            stage 'Archive Artifacts'
            step([$class: 'ArtifactArchiver', artifacts: 'target/ecutest.hpi', fingerprint: true])
        }
    }
}
