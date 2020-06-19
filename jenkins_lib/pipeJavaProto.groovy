//Default pipeline for Java proto
def call(Closure body) {
    withMaven() {
        withGPG() {
           body.call()
        }
    }

    // Run security tests and quality analysis and wait for results
    runJavaSecurityTools()
    // Wait for security and quality analysis results
    getJavaSecurityResults()

}

return this
