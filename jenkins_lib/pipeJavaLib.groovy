//Default pipeline for Java library
def call(String buildImageTag, String mvnArgs = "", 
  String registry = "dr.rbkmoney.com", String registryCredentialsId = "dockerhub-rbkmoneycibot")) {

    // mvnArgs - arguments for mvn install in build container. For exmple: ' -DjvmArgs="-Xmx256m" '
    env.REGISTRY = registry

    def buildContainer = docker.image("rbkmoney/build:${buildImageTag}")
    runStage('Pull build image') {
        docker.withRegistry('https://' + registry + '/v2/', registryCredentialsId) {
            buildContainer.pull()
        }
        buildContainer = docker.image(registry + "/rbkmoney/build:${buildImageTag}")
    }

    runStage('Execute build container') {
        withCredentials([[$class: 'FileBinding', credentialsId: 'java-maven-settings.xml', variable: 'SETTINGS_XML']]) {
            buildContainer.inside() {
                if (env.BRANCH_NAME == 'master') {
                    sh 'mvn deploy --batch-mode --settings  $SETTINGS_XML ' + "${mvnArgs}"
                } else {
                    sh 'mvn package --batch-mode --settings  $SETTINGS_XML ' + "${mvnArgs}"
                }
            }
        }
    }
}

return this
