def call(String relPathFrom, String relPathTo) {
    def WORKSPACE = pwd()
    def cacheRootDir = "$env.HOME/.cache"
    def workspacePathRelatedToHome = java.nio.file.Paths.get("$env.HOME").relativize(java.nio.file.Paths.get("$WORKSPACE")).toString();

    def copyFrom = java.nio.file.Paths.get(cacheRootDir, workspacePathRelatedToHome, relPathFrom).toString()
    def copyToDir = java.nio.file.Paths.get("$WORKSPACE", relPathTo).getParent().toString()
    def newFilePath = java.nio.file.Paths.get("$WORKSPACE", relPathTo).toString()


    if (!(new java.io.File(copyFrom).isDirectory())) {
        sh "mkdir -p $copyToDir"
        sh "cp -r $copyFrom $newFilePath"
    } else {
        sh "mkdir -p $newFilePath"
        sh "cp -r $copyFrom/* $newFilePath"
    }

    println("CACHE: GET '$copyFrom' TO '$newFilePath'")
}

return this

