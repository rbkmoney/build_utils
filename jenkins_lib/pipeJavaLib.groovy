//Default pipeline for Java library
def call(String buildImageTag, String mvnArgs = "") {

    // mvnArgs - arguments for mvn install in build container. For exmple: ' -DjvmArgs="-Xmx256m" '
    if (env.REPO_PUBLIC == 'true'){
      mvnArgs += ' -P public -Dgpg.keyname="$GPG_KEYID" -Dgpg.passphrase="$GPG_PASSPHRASE" '
    }
    else {
      mvnArgs += ' -P private '
    }

    def buildContainer = docker.image("rbkmoney/build:${buildImageTag}")
    runStage('Pull build image') {
        withPrivateRegistry() {
          buildContainer.pull()
          buildContainer = docker.image(env.REGISTRY + "/rbkmoney/build:${buildImageTag}")
        }
    }

    runStage('Execute build container') {
        withMaven() {
            buildContainer.inside() {
                if (env.BRANCH_NAME == 'master') {
                    withGPG(){
                        sh 'mvn deploy --batch-mode --settings $SETTINGS_XML ' + "${mvnArgs}"
                    }
                } else {
                    sh 'mvn package --batch-mode --settings  $SETTINGS_XML ' + "${mvnArgs}"
                }
            }
        }
    }
    
    // Run security tests and quality analysis and wait for results
    runJavaSecurityTools(mvnArgs)
    // Wait for security and quality analysis results
    getJavaSecurityResults(mvnArgs)

}

return this
