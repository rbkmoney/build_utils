//Default pipeline for Java library
def call(String mvnArgs = "", // arguments for mvn install in build container. For exmple: ' -DjvmArgs="-Xmx256m" '
         String buildImageTag = "25c031edd46040a8745334570940a0f0b2154c5c" // https://github.com/rbkmoney/image-build-java
) {
    // build container image tag
    env.BUILD_IMAGE_TAG = buildImageTag

    // Using withRegistry() for auth on docker hub server.
    // Pull it to local images with short name and reopen it with full name, to exclude double naming problem
    def buildContainer = docker.image('rbkmoney/build:$BUILD_IMAGE_TAG')
    runStage('Pull build image') {
        withPrivateRegistry() {
            buildContainer.pull()
            buildContainer = docker.image(env.REGISTRY + '/rbkmoney/build:$BUILD_IMAGE_TAG')
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
