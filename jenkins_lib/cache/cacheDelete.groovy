def call(String relPath) {
    def WORKSPACE = pwd()
    def cacheRootDir = "$env.HOME/.cache"
    def workspacePathRelatedToHome = java.nio.file.Paths.get("$env.HOME").relativize(java.nio.file.Paths.get("$WORKSPACE")).toString();
    def absolutePath = java.nio.file.Paths.get(cacheRootDir, workspacePathRelatedToHome, relPath).toString()

    sh "rm -rf $absolutePath"

    println("CACHE: DELETE '$absolutePath'")
}

return this

