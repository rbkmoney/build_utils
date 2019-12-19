def put(String relPath) {
  def curDir = pwd()
  def copyToDir = getCachePath(relPath).getParent().toString()
  def copyFrom  = java.nio.file.Paths.get("$curDir", relPath).toString()

  sh "mkdir -p $copyToDir"
  sh " cp -r $copyFrom $copyToDir"

  println("CACHE: PUT '$copyFrom' TO '${copyToDir}/${relPath}'")
}

def get(String relPathFrom, String relPathTo) {
  def curDir   = pwd()
  def copyFrom    = getCachePath(relPathFrom).toString()
  def copyToDir   = java.nio.file.Paths.get("$curDir", relPathTo).getParent().toString()
  def newFilePath = java.nio.file.Paths.get("$curDir", relPathTo).toString()

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
  def absPath = getCachePath(relPath).toString()

  sh "rm -rf $absPath"

  println("CACHE: DELETE '$absPath'")
}

// Internal
def getCachePath(String relPath) {
  def cacheRootDir = "$env.HOME/.cache/$env.JOB_NAME"
  java.nio.file.Paths.get(cacheRootDir, relPath)
}

return this

