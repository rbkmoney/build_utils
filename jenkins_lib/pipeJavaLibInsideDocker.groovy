//Default pipeline for Java library
def call(String mvnArgs = "", // arguments for mvn install in build container. For exmple: ' -DjvmArgs="-Xmx256m" '
         String buildImageTag = "a9b8771e24a69101c9327d9501a30afb4c5cd685" // https://github.com/rbkmoney/image-build-java
) {
    // build container image tag
    env.BUILD_IMAGE_TAG = buildImageTag

    // Using withRegistry() for auth on docker hub server.
    // Pull it to local images with short name and reopen it with full name, to exclude double naming problem
    def buildContainer = docker.image('rbkmoney/build-java:$BUILD_IMAGE_TAG')
    runStage('Pull build image') {
        withPrivateRegistry() {
            buildContainer.pull()
            buildContainer = docker.image(env.REGISTRY + '/rbkmoney/build-java:$BUILD_IMAGE_TAG')
        }
    }

    def insideParams = ' --group-add 200 -v /var/run/docker.sock:/var/run/docker.sock '
    runStage('Execute build container') {
        withMaven() {
            buildContainer.inside(insideParams) {
                if (env.BRANCH_NAME == 'master') {
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
