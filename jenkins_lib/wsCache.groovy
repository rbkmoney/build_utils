def put(String relPath) {
  def WORKSPACE = pwd()
  def copyToDir = getCachePath(relPath, WORKSPACE).getParent().toString()
  def copyFrom  = java.nio.file.Paths.get("$WORKSPACE", relPath).toString()

  sh "mkdir -p $copyToDir"
  sh " cp -r $copyFrom $copyToDir"

  println("CACHE: PUT '$copyFrom' TO '${copyToDir}/${relPath}'")
}

def get(String relPathFrom, String relPathTo) {
  def WORKSPACE   = pwd()
  def copyFrom    = getCachePath(relPathFrom, WORKSPACE).toString()
  def copyToDir   = java.nio.file.Paths.get("$WORKSPACE", relPathTo).getParent().toString()
  def newFilePath = java.nio.file.Paths.get("$WORKSPACE", relPathTo).toString()

  // fail if there is no such cache
  sh "ls $copyFrom"

  if (!(new java.io.File(copyFrom).isDirectory())) {
    sh "mkdir -p $copyToDir"
    sh "cp -r $copyFrom $newFilePath"
  } else {
    sh "mkdir -p $newFilePath"
    sh "cp -r $copyFrom/* $newFilePath"
  }

  println("CACHE: GET '$copyFrom' TO '$newFilePath'")
}

def delete(String relPath) {
  def WORKSPACE = pwd()
  def absPath   = getCachePath(relPath, pwd()).toString()

  sh "rm -rf $absPath"

  println("CACHE: DELETE '$absPath'")
}

// Internal
def getCachePath(String relPath, String WORKSPACE) {
  def cacheRootDir = "$env.HOME/.cache"
  def workspacePathRelatedToHome = java.nio.file.Paths.get("$env.HOME").relativize(java.nio.file.Paths.get("$WORKSPACE")).toString();
  java.nio.file.Paths.get(cacheRootDir, workspacePathRelatedToHome, relPath)
}

return this

