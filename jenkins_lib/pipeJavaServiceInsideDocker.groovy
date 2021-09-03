// Actual pipeline for Java service
def call(String serviceName,
         String mvnArgs = "", // arguments for mvn install in build container. For exmple: ' -DjvmArgs="-Xmx256m" '
         String serviceBaseImageTag = "1d57d77a38eb7b351eca3c1a9a3e45ec441ed9aa", // https://github.com/rbkmoney/image-service-java
         String buildImageTag = "e26b9f98e2da13f570197ce62c8f6247bbf93108" // https://github.com/rbkmoney/image-build-java
) {
    // service name - usually equals artifactId
    env.SERVICE_NAME = serviceName
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

    // Quick fix related to https://github.com/rbkmoney/image-build-java/blob/a9b8771e24a69101c9327d9501a30afb4c5cd685/Dockerfile#L70
    def m2home = sh(returnStdout: true, script: "echo $HOME/.m2").trim()
    def insideParams = " --group-add 200 -v /var/run/docker.sock:/var/run/docker.sock -v ${m2home}:/home/postgres/.m2:rw"
    // Run mvn and generate docker file
    runStage('Execute build container') {
        withMaven() {
            buildContainer.inside(insideParams) {
                def mvn_command_arguments = ' --batch-mode --settings  $SETTINGS_XML ' +
                        '-Ddockerfile.base.service.tag=$BASE_IMAGE_TAG ' +
                        '-Ddockerfile.build.container.tag=$BUILD_IMAGE_TAG ' +
                " ${mvnArgs}"
                sh 'mvn verify install' + mvn_command_arguments + ' -Dgpg.skip=true'
            }
        }
    }
    // Run security tests and quality analysis
    runJavaSecurityTools(mvnArgs)

    def serviceImage
    def imgShortName = 'rbkmoney/' + env.SERVICE_NAME + ':' + '$COMMIT_ID';
    getCommitId()
    runStage('Build Service image') {
        withPrivateRegistry() {
            serviceImage = docker.build(imgShortName, '-f ./target/Dockerfile ./target')
        }
    }

    // Wait for security and quality analysis results
    getJavaSecurityResults(mvnArgs)

    try {
        if (env.BRANCH_NAME == 'master') {
            runStage('Push Service image to private registry') {
                withPrivateRegistry() {
                    serviceImage.push()
                    // Push under 'withRegistry' generates 2d record with 'long name' in local docker registry.
                    // Untag the long-name
                    sh "docker rmi -f " + env.REGISTRY + "/${imgShortName} || true"
                }
            }
            if (env.REPO_PUBLIC == 'true') {
                runStage('Push image to public docker registry') {
                    withPublicRegistry() {
                        serviceImage.push()
                        sh "docker rmi -f " + env.REGISTRY + "/${imgShortName} || true"
                    }
                }
            }
        }
    }
    finally {
        runStage('Remove local image') {
            // Remove the image to keep Jenkins runner clean.
            sh "docker rmi -f ${imgShortName} || true"
        }
    }
}

return this
