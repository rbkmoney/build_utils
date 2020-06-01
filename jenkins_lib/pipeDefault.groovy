def call(Closure body) {
  withPrivateRegistry() {
    body.call()
  }
}

return this;
