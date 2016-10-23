def finalHook = {
  runStage('store artifacts') {
    def storeArtifacts = load("${env.JENKINS_LIB}/storeArtifacts.groovy")
    storeArtifacts('examples/')
  }
}

build('build_utils', 'gentoo', finalHook) {
  checkoutRepo()

  def pipeDefault
  runStage('load pipeline') {
    env.JENKINS_LIB = "./jenkins_lib"
    pipeDefault = load("${env.JENKINS_LIB}/pipeDefault.groovy")
  }

  pipeDefault() {
    runStage('smoke test ') {
      sh 'make smoke_test'
    }
    runStage('test utils_container (wc)') {
      sh 'make wc_smoke_test'
    }
    runStage('test utils_container (wdeps)') {
      sh 'make wdeps_smoke_test'
    }

    def wsCache
    runStage('load workspace cache functions') {
      wsCache = load("${env.JENKINS_LIB}/wsCache.groovy")
    }

    def testDir = '.testcache'
    runStage('test cache file') {
      sh "mkdir -p $testDir"
      dir("$testDir") {
        sh 'echo test > test.txt'
        def sourceFile = 'test.txt'
        def targetFile = 'test2.txt'
        wsCache.put(sourceFile)
        wsCache.get(sourceFile, targetFile)
        if (!fileExists(targetFile)) {
          error("File $sourceFile had not been extracted from cache to file $targetFile")
        }
        wsCache.delete(sourceFile)
        try{
          wsCache.get(sourceFile, targetFile)
          error("File $sourceFile had not been deleted from cache.")
        } catch (Exception e){
          //we expected exception as file should be deleted from cache
        }
      }
    }

    runStage('test cache dir') {
      dir("$testDir") {
        sh 'mkdir ttt'
        sh 'echo test > ttt/test.txt'
        def sourceDir = 'ttt'
        def targetDir = 'ttt2'
        wsCache.put(sourceDir)
        wsCache.get(sourceDir, targetDir)
        if (!fileExists("$targetDir/test.txt")) {
          error("Dir $sourceDir had not been extracted from cache to dir $targetDir")
        }
        wsCache.delete(sourceDir)
        try{
          wsCache.get(sourceDir, targetDir)
          error("Dir $sourceDir had not been deleted from cache.")
        }catch (Exception e){
          //we expected exception as file should be deleted from cache
        }
      }
    }

    def withWsCache
    runStage('load withWsCache function') {
      withWsCache = load("${env.JENKINS_LIB}/withWsCache.groovy")
    }

    runStage('test withWsCache') {
      sh 'mkdir -p some_cache_dir'
      def cacheFile = 'some_cache_dir/somecache'
      def cacheVal1  = 'cache_value'
      withWsCache(cacheFile) {
        sh "echo $cacheVal1 > $cacheFile"
      }
      def cacheVal2
      withWsCache(cacheFile) {
        cacheVal2 = sh(script: "cat $cacheFile", returnStdout: true).trim()
      }

      //cleanup anyway
      wsCache.delete(cacheFile)

      if (cacheVal1 != cacheVal2) {
        error("withWsCache failed: cache values: $cacheVal1 != $cacheVal2")
      }
    }

    runStage('test utils_repo') {
      withGithubPrivkey {
        sh 'make wc_init-repos'
      }
    }
    runStage('test utils_image (build image)') {
      sh 'make build_image'
    }
    def testTag = 'jenkins_build_test'
    try {
      runStage('test utils_image (push image)') {
        sh "make push_image SERVICE_IMAGE_PUSH_TAG=${testTag}"
      }
    } finally {
      runStage('test utils_image (rm local image)') {
        sh "make rm_local_image SERVICE_IMAGE_PUSH_TAG=${testTag}"
      }
    }
  }
}

