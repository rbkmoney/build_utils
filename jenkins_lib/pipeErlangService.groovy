// Default pipeline for Erlang services
def runPipe(boolean testWithDependencies = true, boolean runInParallel = false, String pltHomeDir = 'default') {
    def erlangUtils = load("${env.JENKINS_LIB}/pipeErlangUtils.groovy")
    withPrivateRegistry() {
        if (env.BRANCH_NAME != 'master') {
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
        }
        runStage('make release') {
            withGithubPrivkey {
                sh "make wc_release"
            }
        }
        runStage('build image') {
            sh "make build_image"
        }

        try {
            if (masterlikeBranch()) {
                runStage('push image') {
                    sh "make push_image"
                }
            }
        } finally {
            runStage('rm local image') {
                sh 'make rm_local_image'
            }
        }
        runErlSecurityTools()
    }
}

return this;
