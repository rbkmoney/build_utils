// Default pipeline for Java service
def call(String serviceName, Boolean useJava11 = false, String mvnArgs = "") {
    // service name - usually equals artifactId
    env.SERVICE_NAME = serviceName
    // use java11 or use std JAVA_HOME (java8)
    env.JAVA_HOME = useJava11 ? "JAVA_HOME=/opt/openjdk-bin-11.0.1_p13 " : ""

    // mvnArgs - arguments for mvn. For example: ' -DjvmArgs="-Xmx256m" '

    // Run mvn and generate docker file
    runStage('Maven package') {
        withCredentials([[$class: 'FileBinding', credentialsId: 'java-maven-settings.xml', variable: 'SETTINGS_XML']]) {
            def mvn_command_arguments = ' --batch-mode --settings  $SETTINGS_XML -P ci ' +
                    " -Dgit.branch=${env.BRANCH_NAME} " +
                    " ${mvnArgs}"
            if (env.BRANCH_NAME == 'master') {
                sh env.JAVA_HOME + 'mvn deploy' + mvn_command_arguments
            } else {
                sh env.JAVA_HOME + 'mvn package' + mvn_command_arguments
            }
        }
    }

    // Send result build to sonar
//    runStage('Maven sonar') {
//        withCredentials([[$class: 'FileBinding', credentialsId: 'java-maven-settings.xml', variable: 'SETTINGS_XML']]) {
//            def mvn_command_arguments = ' --batch-mode --settings  $SETTINGS_XML -P ci ' +
//                    " -Dgit.branch=${env.BRANCH_NAME} " +
//                    " ${mvnArgs}"
//            try {
//                sh env.JAVA_HOME + 'mvn sonar:sonar' + mvn_command_arguments +
//                        " -Dsonar.host.url=${env.SONAR_ENDPOINT}"
//            }
//            catch (Exception e) {
//                echo "Can't send to sonar ${env.SONAR_ENDPOINT}."
//            }
//        }
//    }

    def serviceImage;
    def imgShortName = 'rbkmoney/' + env.SERVICE_NAME + ':' + '$COMMIT_ID';
    getCommitId()
    runStage('Build Service image') {
        serviceImage = docker.build(imgShortName, '-f ./target/Dockerfile ./target')
    }

    try {
        if (env.BRANCH_NAME == 'master') {
            runStage('Push Service image') {
                docker.withRegistry('https://dr.rbkmoney.com/v2/', 'dockerhub-rbkmoneycibot') {
                    serviceImage.push()
                }
                // Push under 'withRegistry' generates 2d record with 'long name' in local docker registry.
                // Untag the long-name
                sh "docker rmi dr.rbkmoney.com/${imgShortName}"
            }
        }
    }
    finally {
        runStage('Remove local image') {
            // Remove the image to keep Jenkins runner clean.
            sh "docker rmi ${imgShortName}"
        }
    }
}

return this