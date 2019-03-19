def call(Closure body, String registry = "dr.rbkmoney.com", String registryCredentialsId = "dockerhub-rbkmoneycibot") {
  env.REGISTRY = registry

  runStage('init pipeline') {
    docker.withRegistry('https://' + registry + '/v2/', registryCredentialsId) {
      body.call()
    }
  }
}

return this;
