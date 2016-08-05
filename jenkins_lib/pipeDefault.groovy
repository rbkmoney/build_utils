def call(Closure body) {
  runStage 'init pipeline' {
    getCommitAuthor()
    docker.withRegistry('https://dr.rbkmoney.com/v2/', 'dockerhub-rbkmoneycibot') {
      body.call()
    }
  }
}

return this;

