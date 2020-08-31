//Default pipeline for Java library
def call(String buildImageTag, String mvnArgs = "") {

    // mvnArgs - arguments for mvn install outside build container. For exmple: ' -DjvmArgs="-Xmx256m" '

    runStage('Build outside container') {
        withMaven() {
            if (env.BRANCH_NAME == 'master') {
                withGPG() {
                    sh 'mvn deploy --batch-mode --settings  $SETTINGS_XML ' + "${mvnArgs}" +
                        ' -Dgpg.keyname="$GPG_KEYID" -Dgpg.passphrase="$GPG_PASSPHRASE" '
                }
            } else {
                sh 'mvn verify --batch-mode --settings  $SETTINGS_XML ' + "${mvnArgs}" + ' -Dgpg.skip=true'
            }
        }
    }

    // Run security tests and quality analysis and wait for results
    runJavaSecurityTools(mvnArgs)
    // Wait for security and quality analysis results
    getJavaSecurityResults(mvnArgs)

}

return this
