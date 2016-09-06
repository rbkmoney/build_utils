//Default pipeline for Java library
def call(String buildImageTag, String mvnArgs = "") {

    // mvnArgs - arguments for mvn install in build container. For exmple: ' -DjvmArgs="-Xmx256m" '

    def buildContainer = docker.image("rbkmoney/build:${buildImageTag}")
    runStage('Pull build image') {
        docker.withRegistry('https://dr.rbkmoney.com/v2/', 'dockerhub-rbkmoneycibot') {
            buildContainer.pull()
        }
        buildContainer = docker.image("dr.rbkmoney.com/rbkmoney/build:${buildImageTag}")
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
