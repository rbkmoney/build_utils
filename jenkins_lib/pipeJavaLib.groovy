//Default pipeline for Java library
def call(String buildImageTag, String mvnArgs = "",
  String registry = "dr2.rbkmoney.com", String registryCredentialsId = "jenkins_harbor") {

    // mvnArgs - arguments for mvn install in build container. For exmple: ' -DjvmArgs="-Xmx256m" '
    if (env.REPO_PUBLIC == 'true'){
      mvnArgs += ' -P public '
    }
    else {
      mvnArgs += ' -P private '
    }

    env.REGISTRY = registry

    def buildContainer = docker.image("rbkmoney/build:${buildImageTag}")
    runStage('Pull build image') {
        docker.withRegistry('https://' + registry + '/v2/', registryCredentialsId) {
            buildContainer.pull()
        }
        buildContainer = docker.image(registry + "/rbkmoney/build:${buildImageTag}")
    }

    runStage('Execute build container') {
        withCredentials([[$class: 'FileBinding', credentialsId: 'maven-settings-nexus-github.xml', variable: 'SETTINGS_XML']]) {
            buildContainer.inside() {
                if (env.BRANCH_NAME == 'master') {
                    sh 'mvn deploy --batch-mode --settings $SETTINGS_XML ' + "${mvnArgs}"
                } else {
                    sh 'mvn package --batch-mode --settings $SETTINGS_XML ' + "${mvnArgs}"
                }
            }
        }
    }

    //skip SonarQube analysis in master branch
    if (env.BRANCH_NAME != 'master') {
        runStage('Running SonarQube analysis') {

            withCredentials([[$class: 'FileBinding', credentialsId: 'maven-settings-nexus-github.xml', variable: 'SETTINGS_XML']]) {
                // sonar1 - SonarQube server name in Jenkins properties
                withSonarQubeEnv('sonar1') {
                    sh 'mvn sonar:sonar' +
                            " --batch-mode --settings  $SETTINGS_XML " +
                            " -Dgit.branch=${env.BRANCH_NAME} " +
                            " ${mvnArgs}" +
                            " -Dsonar.host.url=${env.SONAR_ENDPOINT}"
                }
            }
        }

        runStage("Running SonarQube Quality Gate result") {
            def retryAttempt = 1
            retry(4) {
                try {
                    timeout(time: 30, unit: 'SECONDS') {
                        echo "Trying for the " + retryAttempt + " time"
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "Pipeline aborted due to quality gate failure: ${qg.status}"
                        }
                    }
                } catch (ex) {
                    // Work around https://issues.jenkins-ci.org/browse/JENKINS-51454
                    retryAttempt++
                    sleep(5)
                    error 'Quality gate timeout has been exceeded'
                }
            }
        }
    }


}

return this
