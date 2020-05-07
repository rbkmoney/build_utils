// Not actual (but maybe useful in the future) pipeline for Java service
def call(String serviceName, String baseImageTag, String buildImageTag, String dbHostName, String mvnArgs = "",
  String privateRegistry = "dr2.rbkmoney.com", String privateRegistryCredsId = "jenkins_harbor",
  String publicRegistry = "index.docker.io", String publicRegistryCredsId = "dockerhub-rbkmoneycibot") {
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
    // mvnArgs - arguments for mvn install in build container. For exmple: ' -DjvmArgs="-Xmx256m" '
    if (env.REPO_PUBLIC == 'true'){
      mvnArgs += ' -P public '
    }
    else {
      mvnArgs += ' -P private '
    }
    env.REGISTRY = privateRegistry

    def privateRegistryURL = 'https://' + privateRegistry + '/v2/'
    def publicRegistryURL = 'https://' + publicRegistry + '/v1/'

    // Using withRegistry() for auth on docker hub server.
    // Pull it to local images with short name and reopen it with full name, to exclude double naming problem
    def buildContainer = docker.image('rbkmoney/build:$BUILD_IMAGE_TAG')
    runStage('Pull build image') {
        docker.withRegistry(privateRegistryURL, privateRegistryCredsId) {
            buildContainer.pull()
        }
        buildContainer = docker.image(privateRegistry + '/rbkmoney/build:$BUILD_IMAGE_TAG')
    }

    def postgresImage
    try {
        // Start db if necessary.

        def insideParams = ''
        if (dbHostName != null) {
            runStage('Run PostgresDB container') {
                docker.withRegistry(privateRegistryURL, privateRegistryCredsId) {
                    postgresImage = docker.image(privateRegistry + '/rbkmoney/postgres:9.6')
                            .run(
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
            withCredentials([[$class: 'FileBinding', credentialsId: 'maven-settings-nexus-github.xml', variable: 'SETTINGS_XML']]) {
                buildContainer.inside(insideParams) {
                    def mvn_command_arguments = ' --batch-mode --settings  $SETTINGS_XML ' +
                            '-Ddockerfile.base.service.tag=$BASE_IMAGE_TAG ' +
                            '-Ddockerfile.build.container.tag=$BUILD_IMAGE_TAG ' +
                            '-Ddb.url.host.name=$DB_HOST_NAME ' +
                            " ${mvnArgs}"
                    if (env.BRANCH_NAME == 'master') {
                        sh 'mvn deploy' + mvn_command_arguments
                    } else {
                        sh 'mvn package' + mvn_command_arguments
                    }
                }
            }
        }
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
        docker.withRegistry(privateRegistryURL, privateRegistryCredsId) {
            serviceImage = docker.build(imgShortName, '-f ./target/Dockerfile ./target')
        }
    }

    try {
        if (env.BRANCH_NAME == 'master') {
            runStage('Push Service image to private registry') {
                docker.withRegistry(privateRegistryURL, privateRegistryCredsId) {
                    serviceImage.push()
                }
                // Push under 'withRegistry' generates 2d record with 'long name' in local docker registry.
                // Untag the long-name
                sh "docker rmi -f " + privateRegistry + "/${imgShortName}"
            }
            if (env.REPO_PUBLIC == 'true'){
                runStage('Push image to public docker registry') {
                    docker.withRegistry(publicRegistryURL, publicRegistryCredsId) {
                        serviceImage.push()
                    }
                    sh "docker rmi -f " + publicRegistry + "/${imgShortName}"
                }
            }
        }
    }
    finally {
        runStage('Remove local image') {
            // Remove the image to keep Jenkins runner clean.
            sh "docker rmi -f ${imgShortName}"
        }
    }
}

return this