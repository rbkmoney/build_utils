def runTestsSequentially(boolean testWithDependencies, String pltHomeDir)  {
    def withDialyzerCache = load("${env.JENKINS_LIB}/withDialyzerCache.groovy")
    runStage('lint') {
        sh 'make wc_lint'
    }
    runStage('check format') {
        sh 'make wc_check_format'
    }
    runStage('xref') {
        sh 'make wc_xref'
    }
    runStage('dialyze') {
        withDialyzerCache(pltHomeDir) {
            sh 'make wc_dialyze'
        }
    }
    runStage('test') {
        if (testWithDependencies) {
            sh "make wdeps_test"
        } else {
            sh "make wc_test"
        }
    }
}

def runTestsInParallel(boolean testWithDependencies, String pltHomeDir) {
    def withDialyzerCache = load("${env.JENKINS_LIB}/withDialyzerCache.groovy")
    stages = [
            failFast: true,
            lint: {
                runStage('lint') {
                    sh 'make wc_lint'
                }
            },
            check_format: {
                runStage('check format') {
                    sh 'make wc_check_format'
                }
            },
            xref: {
                runStage('xref') {
                    sh 'make wc_xref'
                }
            },
            dialyze: {
                runStage('dialyze') {
                    withDialyzerCache(pltHomeDir) {
                        sh 'make wc_dialyze'
                    }
                }
            },
            test: {
                runStage('test') {
                    if (testWithDependencies) {
                        sh "make wdeps_test"
                    } else {
                        sh "make wc_test"
                    }
                }
            }
    ]
    parallel stages
}

return this;
