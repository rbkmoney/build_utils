// Not actual (but maybe useful in the future) pipeline for Java service
def call(String serviceName, String baseImageTag, String buildImageTag, String dbHostName, String mvnArgs = "") {
    // service name - usually equals artifactId
    env.SERVICE_NAME = serviceName
    // service java image tag
    env.BASE_IMAGE_TAG = baseImageTag
    // build container image tag
    env.BUILD_IMAGE_TAG = buildImageTag
    // data base name
    env.DB_NAME = serviceName
    // host url for database. If null - DB will not start
    env.DB_HOST_NAME = dbHostName
    // mvnArgs - arguments for mvn install in build container. For example: ' -DjvmArgs="-Xmx256m" '

    // Using withRegistry() for auth on docker hub server.
    // Pull it to local images with short name and reopen it with full name, to exclude double naming problem
    def buildContainer = docker.image('rbkmoney/build-java:$BUILD_IMAGE_TAG')
    runStage('Pull build image') {
        withPrivateRegistry() {
            buildContainer.pull()
            buildContainer = docker.image(env.REGISTRY + '/rbkmoney/build-java:$BUILD_IMAGE_TAG')
        }
    }

    def postgresImage
    try {
        // Start db if necessary.

        def insideParams = ''
        if (env.DB_HOST_NAME != null) {
            runStage('Run PostgresDB container') {
                withPrivateRegistry() {
                    postgresImage = docker.image(env.REGISTRY + '/rbkmoney/postgres:11.4')
                            .run(
                                '-p 5432:5432 ' +
                                '-e POSTGRES_PASSWORD=postgres ' +
                                '-e POSTGRES_USER=postgres ' +
                                '-e POSTGRES_DB=$DB_NAME '
                    )
                    insideParams = ' --link ' + postgresImage.id + ':$DB_HOST_NAME '
                }
            }
        }
        // Run mvn and generate docker file
        runStage('Execute build container') {
            withMaven() {
                buildContainer.inside(insideParams) {
                    def mvn_command_arguments = ' --batch-mode --settings  $SETTINGS_XML ' +
                            '-Ddockerfile.base.service.tag=$BASE_IMAGE_TAG ' +
                            '-Ddockerfile.build.container.tag=$BUILD_IMAGE_TAG ' +
                            '-Ddb.url.host.name=$DB_HOST_NAME ' +
                            " ${mvnArgs}"
                    if (env.BRANCH_NAME == 'master') {
                        withGPG(){
                            sh 'mvn deploy' + mvn_command_arguments +
                                ' -Dgpg.keyname="$GPG_KEYID" -Dgpg.passphrase="$GPG_PASSPHRASE" '
                        }
                    } else {
                        sh 'mvn verify' + mvn_command_arguments + ' -Dgpg.skip=true'
                    }
                }
            }
        }
        // Run security tests and quality analysis
        runJavaSecurityTools(mvnArgs)
    }
    finally {
        if (postgresImage != null) {
            runStage('Stop PostgresDB container') {
                postgresImage.stop()
            }
        }
    }

    def serviceImage;
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
            if (env.REPO_PUBLIC == 'true'){
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
