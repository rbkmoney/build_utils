//Default pipeline for Java library
def call(String buildImageTag, String mvnArgs = "") {

    // Arguments for mvn install in build container. For exmple: ' -DjvmArgs="-Xmx256m" '
    env.MVN_ARGS = mvnArgs

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
                    sh 'mvn deploy --batch-mode --settings  $SETTINGS_XML $MVN_ARGS'
                } else {
                    sh 'mvn install --batch-mode --settings  $SETTINGS_XML $MVN_ARGS'
                }
            }
        }
    }
}

return this
