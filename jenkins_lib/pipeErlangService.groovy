// Default pipeline for Erlang services
def run(boolean testWithDependencies = false, boolean runInParallel = false) {
    def withDialyzerCache = load("${env.JENKINS_LIB}/withDialyzerCache.groovy")
    withPrivateRegistry() {
        if (masterlikeBranch()) {
            // RELEASE pipe
            runStage('make release') {
                withGithubPrivkey {
                    sh "make wc_release"
                }
            }
            runStage('build image') {
                sh "make build_image"
            }

            try {
                runStage('push image') {
                    sh "make push_image"
                }
            } finally {
                runStage('rm local image') {
                    sh 'make rm_local_image'
                }
            }
        } else {
            // TEST pipe
            runStage('compile') {
                withGithubPrivkey {
                    sh 'make wc_compile'
                }
            }
            if (runInParallel) {
                runTestsInParallel(testWithDependencies)
            } else {
                runTests(testWithDependencies)
            }
        }
        runErlSecurityTools()
    }
}

def runTests(testWithDependencies)  {
    runStage('lint') {
        sh 'make wc_lint'
    }
    runStage('xref') {
        sh 'make wc_xref'
    }
    runStage('dialyze') {
        withDialyzerCache() {
            sh 'make wc_dialyze'
        }
    }
    runStage('test') {
        if (testWithDependencies) {
            sh "make wc_test"
        } else {
            sh "make wdeps_test"
        }
    }
}

def runTestsInParallel(testWithDependencies) {
    stages = [
            runStage('lint') {
                sh 'make wc_lint'
            },
            runStage('xref') {
                sh 'make wc_xref'
            },
            runStage('dialyze') {
                withDialyzerCache() {
                    sh 'make wc_dialyze'
                }
            },
            runStage('test') {
                if (testWithDependencies) {
                    sh "make wc_test"
                } else {
                    sh "make wdeps_test"
                }
            }
    ]
    parallel stages
}

return this;
