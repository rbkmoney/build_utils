def call(Closure body, String registry = "dr2.rbkmoney.com", String registryCredentialsId = "jenkins_harbor") {
  env.REGISTRY = registry

  runStage('init pipeline') {
    docker.withRegistry('https://' + registry + '/v2/', registryCredentialsId) {
      body.call()
    }
  }
}

return this;
