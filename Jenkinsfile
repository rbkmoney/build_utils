node('gentoo') {
  stage 'git checkout'
  checkout scm

  stage 'load pipeline'
  def pipeline = load("jenkins_lib/pipeline.groovy")

  pipeline("build_utils", 'examples/') {
    runStage('test runStage') {
      sh 'echo OK'
    }
  }
}
