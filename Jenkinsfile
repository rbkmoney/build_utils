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
    env.JENKINS_LIB = "./jenkins_lib"
    pipeDefault = load("${env.JENKINS_LIB}/pipeDefault.groovy")
  }

  pipeDefault() {
    runStage('smoke test ') {
      sh 'make smoke_test'
    }
    runStage('test utils_container (wc)') {
      sh 'make wc_smoke_test'
    }
    runStage('test utils_container (wdeps)') {
      sh 'make wdeps_smoke_test'
    }
    runStage('test utils_repo') {
      withGithubPrivkey {
        sh 'make wc_init-repos'
      }
    }
    runStage('test utils_image (build image)') {
      sh 'make build_image'
    }

    stage('test cache(PUT, GET, DELETE)'){
        testCache = load("${env.JENKINS_LIB}/cache/testCache.groovy")
        testCache()
    }

    def testTag = 'jenkins_build_test'
    try {
      runStage('test utils_image (push image)') {
        sh "make push_image SERVICE_IMAGE_PUSH_TAG=${testTag}"
      }
    } finally {
      runStage('test utils_image (rm local image)') {
        sh "make rm_local_image SERVICE_IMAGE_PUSH_TAG=${testTag}"
      }
    }
  }
}

