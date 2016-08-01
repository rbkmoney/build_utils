node('gentoo') {
  stage 'git checkout'
  checkout scm

  stage 'load pipeline'
  def pipeline = load("jenkins_lib/pipeline.groovy")

  pipeline("build_utils", 'examples/') {
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
