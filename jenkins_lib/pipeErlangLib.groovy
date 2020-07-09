// Default pipeline for Erlang libraries
def runPipe(boolean testWithDependencies = true, boolean runInParallel = false, String pltHomeDir = 'default') {
    def erlangUtils = load("${env.JENKINS_LIB}/pipeErlangUtils.groovy")
    withPrivateRegistry() {
        runStage('compile') {
            withGithubPrivkey {
                sh 'make wc_compile'
            }
        }
        if (runInParallel) {
            erlangUtils.runTestsInParallel(testWithDependencies, pltHomeDir)
        } else {
            erlangUtils.runTestsSequentially(testWithDependencies, pltHomeDir)
        }
        runErlSecurityTools()
    }
}

return this;
