// Default pipeline for Java service
def call(String serviceName, Boolean useJava11 = false, String mvnArgs = "") {
    // service name - usually equals artifactId
    env.SERVICE_NAME = serviceName
    // use java11 or use std JAVA_HOME (java8)
    if (useJava11) {
        env.JAVA_HOME = sh(returnStdout: true, script: 'java-config --select-vm openjdk-bin-11 --jdk-home').trim()
    }

    // mvnArgs - arguments for mvn. For example: ' -DjvmArgs="-Xmx256m" '

    // Run mvn and generate docker file
    runStage('Running Maven build') {
        withPrivateRegistry() {
            withMaven() {
                def mvn_command_arguments = ' --batch-mode --settings  $SETTINGS_XML ' +
                        " -Dgit.branch=${env.BRANCH_NAME} " +
                        " ${mvnArgs}"
                sh 'mvn verify' + mvn_command_arguments + ' -Dgpg.skip=true'
            }
        }
    }

    // Run security tests and quality analysis and wait for results
    runJavaSecurityTools(mvnArgs)

    //run docker build, while Sonar runs analysis
    def serviceImage
    def defaultImageShortName = 'rbkmoney/' + env.SERVICE_NAME + ':' + '$COMMIT_ID'
    def imageShortName = env.BRANCH_NAME.startsWith('epic') ? defaultImageShortName + '-epic' : defaultImageShortName

    getCommitId()
    runStage('Build local service docker image') {
        withPrivateRegistry() {
            serviceImage = docker.build(imageShortName, '-f ./target/Dockerfile ./target')
        }
    }

    // Wait for security and quality analysis results
    getJavaSecurityResults(mvnArgs)

    try {
        if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME.startsWith('epic')) {
            runStage('Push service docker image to rbkmoney docker registry') {
                withPrivateRegistry() {
                    serviceImage.push()
                    // Push under 'withRegistry' generates 2d record with 'long name' in local docker registry.
                    // Untag the long-name
                    sh "docker rmi -f " + env.REGISTRY + "/${imageShortName} || true"
                }
            }
            if (env.REPO_PUBLIC == 'true') {
                runStage('Push image to public docker registry') {
                    withPublicRegistry() {
                        serviceImage.push()
                        if (env.BRANCH_NAME == 'master') {
                            serviceImage.push('latest')
                        }
                        sh "docker rmi -f " + env.REGISTRY + "/${imageShortName} || true"
                    }
                }
            }
        }
    }
    finally {
        runStage('Remove local docker image') {
            // Remove the image to keep Jenkins runner clean.
            sh "docker rmi -f ${imageShortName} || true"
        }
    }
}

return this
