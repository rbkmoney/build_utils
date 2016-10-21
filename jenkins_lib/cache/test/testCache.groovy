def call() {
    def cachePut = load("${env.JENKINS_LIB}/cache/cachePut.groovy")
    def cacheGet = load("${env.JENKINS_LIB}/cache/cacheGet.groovy")
    def cacheDelete = load("${env.JENKINS_LIB}/cache/cacheDelete.groovy")

    def testDir = '.testcache'
    try {
        sh "mkdir -p $testDir"
        //test cache file
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
        //test cache dir
        dir("$testDir") {
            sh 'mkdir ttt'
            sh 'echo test > ttt/test.txt'
            def sourceDir = 'ttt'
            def targetDir = 'ttt2'
            cachePut(sourceDir)
            cacheGet(sourceDir, targetDir)
            if (!fileExists("$targetDir/test.txt")) {
                error("Dir $sourceDir had not been extracted from cache to dir $targetDir")
            }
            cacheDelete(sourceDir)
            try{
                cacheGet(sourceDir, targetDir)
                error("Dir $sourceDir had not been deleted from cache.")
            }catch (Exception e){
                //we expected exception as file should be deleted from cache
            }
        }
    }finally {
        sh "rm -r $testDir*"
    }
}

return this

