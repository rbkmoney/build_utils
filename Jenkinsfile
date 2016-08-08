def finalHook = {
  runStage('store artifacts') {
    def storeArtifacts = load("${env.JENKINS_LIB}/storeArtifacts.groovy")
    storeArtifacts('examples/')
  }
}

build('build_utils', 'gentoo', finalHook) {
  checkoutRepo()

  def pipeDefault
  runStage('load pipeline') {
    // env.JENKINS_LIB is required for pipeline.groovy
    env.JENKINS_LIB = "./jenkins_lib"
    pipeDefault = load("${env.JENKINS_LIB}/pipeDefault.groovy")
  }

  pipeDefault() {
    runStage('smoke test ') {
      sh 'make smoke_test'
    }

    runStage('test wc') {
      sh 'make wc_smoke_test'
    }

    runStage('test wdeps') {
      sh 'make wdeps_smoke_test'
    }

    runStage('test build image') {
      sh 'make build_image'
    }

    def testTag = 'jenkins_build_test'
    runStage('test push image') {
      sh "make push_image SERVICE_IMAGE_PUSH_TAG=${testTag}"
    }
  }
}

