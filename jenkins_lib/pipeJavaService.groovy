// Default pipeline for Java service
def call(String serviceName, Boolean useJava11 = false, String mvnArgs = "",
         String registry = "dr2.rbkmoney.com", String registryCredentialsId = "jenkins_harbor") {
    // service name - usually equals artifactId
    env.SERVICE_NAME = serviceName
    // use java11 or use std JAVA_HOME (java8)
    env.JAVA_HOME = useJava11 ? "JAVA_HOME=/opt/openjdk-bin-11.0.1_p13 " : ""

    // mvnArgs - arguments for mvn. For example: ' -DjvmArgs="-Xmx256m" '
    env.REGISTRY = registry
    env.JENKINS_LIB = "./jenkins_lib"

    withWsCache = load("${env.JENKINS_LIB}/withWsCache.groovy")

    // Run mvn and generate docker file
    runStage('Running Maven build') {
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

    //skip SonarQube analysis in master branch
    if (env.BRANCH_NAME != 'master') {
        runStage('Running SonarQube analysis') {
            withWsCache("/home/jenkins/.sonar/cache") {
                withCredentials([[$class: 'FileBinding', credentialsId: 'java-maven-settings.xml', variable: 'SETTINGS_XML']]) {
                    // sonar1 - SonarQube server name in Jenkins properties
                    withSonarQubeEnv('sonar1') {
                        sh env.JAVA_HOME + 'mvn sonar:sonar' +
                                " --batch-mode --settings  $SETTINGS_XML -P ci " +
                                " -Dgit.branch=${env.BRANCH_NAME} " +
                                " ${mvnArgs}" +
                                " -Dsonar.host.url=${env.SONAR_ENDPOINT}"
                    }
                }
            }
        }
    }

    //run docker build, while Sonar runs analysis
    def serviceImage
    def imgShortName = 'rbkmoney/' + env.SERVICE_NAME + ':' + '$COMMIT_ID'
    getCommitId()
    runStage('Build local service docker image') {
        docker.withRegistry('https://' + registry + '/v2/', registryCredentialsId) {
            serviceImage = docker.build(imgShortName, '-f ./target/Dockerfile ./target')
        }
    }

    //skip SonarQube analysis in master branch
    if (env.BRANCH_NAME != 'master') {

        runStage("Running SonarQube Quality Gate result") {
            def retryAttempt = 1
            retry(4) {
                try {
                    timeout(time: 30, unit: 'SECONDS') {
                        echo "Trying for the " + retryAttempt + " time"
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "Pipeline aborted due to quality gate failure: ${qg.status}"
                        }
                    }
                } catch (ex) {
                    // Work around https://issues.jenkins-ci.org/browse/JENKINS-51454
                    retryAttempt++
                    sleep(5)
                    error 'Quality gate timeout has been exceeded'
                }
            }
        }
    }


    try {
        if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME.startsWith('epic')) {
            runStage('Push service docker image to rbkmoney docker registry') {
                docker.withRegistry('https://' + registry + '/v2/', registryCredentialsId) {
                    serviceImage.push()
                }
                // Push under 'withRegistry' generates 2d record with 'long name' in local docker registry.
                // Untag the long-name
                sh "docker rmi " + registry + "/${imgShortName}"
            }
        }
    }
    finally {
        runStage('Remove local docker image') {
            // Remove the image to keep Jenkins runner clean.
            sh "docker rmi ${imgShortName}"
        }
    }
}

return this
