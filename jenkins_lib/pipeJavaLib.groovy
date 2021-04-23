//Default pipeline for Java library
def call(String buildImageTag, String mvnArgs = "") {

    // mvnArgs - arguments for mvn install in build container. For exmple: ' -DjvmArgs="-Xmx256m" '

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
                if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME.startsWith('epic')) {
                    withGPG() {
                        sh 'mvn deploy --batch-mode --settings $SETTINGS_XML ' + "${mvnArgs}" + 
                            ' -Dgpg.keyname="$GPG_KEYID" -Dgpg.passphrase="$GPG_PASSPHRASE" '
                    }
                } else {
                    sh 'mvn verify --batch-mode --settings  $SETTINGS_XML ' + "${mvnArgs}" + ' -Dgpg.skip=true'
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
