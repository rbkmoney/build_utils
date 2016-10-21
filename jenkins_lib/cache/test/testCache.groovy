def call() {
    def cachePut = load("${env.JENKINS_LIB}/cache/cachePut.groovy")
    def cacheGet = load("${env.JENKINS_LIB}/cache/cacheGet.groovy")
    def cacheDelete = load("${env.JENKINS_LIB}/cache/cacheDelete.groovy")

    def testDir = '.testcache'
    try {
        sh "mkdir -p $testDir"
        dir("$testDir") {
            sh 'echo test > test.txt'
            def sourceFile = 'test.txt'
            def targetFile = 'test2.txt'
            cachePut(sourceFile)
            cacheGet(sourceFile, targetFile)
            if (!fileExists(targetFile)) {
                error("File $sourceFile had not been extracted from cache to file $targetFile")
            }
            cacheDelete(sourceFile)
            try{
                cacheGet(sourceFile, targetFile)
                error("File $sourceFile had not been deleted from cache.")
            }catch (Exception e){
                //we expected exception as file should be deleted from cache
            }
        }
    }finally {
        sh "rm -r $testDir*"
    }
}

return this

