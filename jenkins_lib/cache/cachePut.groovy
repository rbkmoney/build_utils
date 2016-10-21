def call(String relPath) {
    def WORKSPACE = pwd()
    def cacheRootDir = "$env.HOME/.cache"
    def workspacePathRelatedToHome = java.nio.file.Paths.get("$env.HOME").relativize(java.nio.file.Paths.get("$WORKSPACE")).toString();
    def copyFrom = java.nio.file.Paths.get("$WORKSPACE", relPath).toString()
    def copyToDir = java.nio.file.Paths.get(cacheRootDir, workspacePathRelatedToHome, relPath).getParent().toString()

    sh "mkdir -p $copyToDir"
    sh " cp -r $copyFrom $copyToDir"

    println("CACHE: PUT '$copyFrom' TO '${copyToDir}/${relPath}'")
}

return this

