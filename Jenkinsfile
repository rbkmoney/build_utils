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

    withCredentials([[$class: 'FileBinding', credentialsId: 'github-rbkmoney-ci-bot-file', variable: 'GITHUB_PRIVKEY'],
                     [$class: 'FileBinding', credentialsId: 'bakka-su-rbkmoney-all', variable: 'BAKKA_SU_PRIVKEY']]) {
      runStage('test utils_repo') {
        sh 'make wc_init-repos'
      }
    }

    runStage('test utils_image (build image)') {
      sh 'make build_image'
    }

    def testTag = 'jenkins_build_test'
    runStage('test utils_iamge (push image)') {
      sh "make push_image SERVICE_IMAGE_PUSH_TAG=${testTag}"
    }
  }
}

