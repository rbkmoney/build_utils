// Default pipeline for Erlang libraries
def runPipe(boolean testWithDependencies = true, boolean runInParallel = false) {
    def erlangUtils = load("${env.JENKINS_LIB}/pipeErlangUtils.groovy")
    withPrivateRegistry() {
        runStage('compile') {
            withGithubPrivkey {
                sh 'make wc_compile'
            }
        }
        if (runInParallel) {
            erlangUtils.runTestsInParallel(testWithDependencies)
        } else {
            erlangUtils.runTestsSequentially(testWithDependencies)
        }
        runErlSecurityTools()
    }
}

return this;
