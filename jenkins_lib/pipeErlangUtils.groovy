def runTestsSequentially(testWithDependencies)  {
    def withDialyzerCache = load("${env.JENKINS_LIB}/withDialyzerCache.groovy")
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
            sh "make wdeps_test"
        } else {
            sh "make wc_test"
        }
    }
}

def runTestsInParallel(testWithDependencies) {
    def withDialyzerCache = load("${env.JENKINS_LIB}/withDialyzerCache.groovy")
    stages = [
            failFast: true,
            lint: {
                runStage('lint') {
                    sh 'make wc_lint'
                }
            },
            xref: {
                runStage('xref') {
                    sh 'make wc_xref'
                }
            },
            dialyze: {
                runStage('dialyze') {
                    withDialyzerCache() {
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