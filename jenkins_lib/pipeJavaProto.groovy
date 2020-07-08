//Default pipeline for Java proto
def call() {
  withMaven() {
    runStage('Generate Java lib') {
      if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME.startsWith('epic/')) {
        withGPG() {
          sh "make wc_java.deploy"
        }
      } else {
        sh "make wc_java.compile"
      }
    }
  }

  // Run security tests and quality analysis and wait for results
  runJavaSecurityTools()
  // Wait for security and quality analysis results
  getJavaSecurityResults()

}

return this
