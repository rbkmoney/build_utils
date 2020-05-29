//Default pipeline for Java library
def call(String buildImageTag, String mvnArgs = "") {

    // mvnArgs - arguments for mvn install outside build container. For exmple: ' -DjvmArgs="-Xmx256m" '
    if (env.REPO_PUBLIC == 'true'){
      mvnArgs += ' -P public -Dgpg.keyname="$GPG_KEYID" -Dgpg.passphrase="$GPG_PASSPHRASE" '
    }
    else {
      mvnArgs += ' -P private '
    }

    runStage('Build outside container') {
        withMaven() {
            if (env.BRANCH_NAME == 'master') {
                withGPG() {
                    sh 'mvn deploy --batch-mode --settings  $SETTINGS_XML ' + "${mvnArgs}"
                }
            } else {
                sh 'mvn package --batch-mode --settings  $SETTINGS_XML ' + "${mvnArgs}"
            }
        }
    }

    //skip SonarQube analysis in master branch
    if (env.BRANCH_NAME != 'master') {
        runStage('Running SonarQube analysis') {
            withMaven() {
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
