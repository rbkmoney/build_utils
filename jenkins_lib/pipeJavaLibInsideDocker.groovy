//Default pipeline for Java library
def call(String mvnArgs = "", // arguments for mvn install in build container. For exmple: ' -DjvmArgs="-Xmx256m" '
         String serviceBaseImageTag = "1d57d77a38eb7b351eca3c1a9a3e45ec441ed9aa", // https://github.com/rbkmoney/image-service-java
         String buildImageTag = "e26b9f98e2da13f570197ce62c8f6247bbf93108" // https://github.com/rbkmoney/image-build-java
) {
    // service java image tag
    env.BASE_IMAGE_TAG = serviceBaseImageTag
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

    runStage('Execute build container') {
        withMaven() {
            buildContainer.inside() {
                if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME.startsWith('epic/')) {
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
