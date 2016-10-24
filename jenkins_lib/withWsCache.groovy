def call(String cachePath, Closure body) {
  checkJenkinsLib()
  def wsCache = load("${env.JENKINS_LIB}/wsCache.groovy")
  try {
    wsCache.get(cachePath, cachePath)
    echo "Found $cachePath in cache."
  } catch (Exception e) {
    echo "No $cachePath found in cache."
  }

  body.call()

  wsCache.put(cachePath)
  echo "Updated $cachePath in cache."
}

def checkJenkinsLib() {
  if (env.JENKINS_LIB == null) {
    error('env.JENKINS_LIB should be set')
  }
}

return this
