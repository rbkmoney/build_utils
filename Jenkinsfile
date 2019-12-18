#!groovy
// -*- mode: groovy -*-

def finalHook = {
  runStage('store artifacts') {
    def storeArtifacts = load("${env.JENKINS_LIB}/storeArtifacts.groovy")
    storeArtifacts('examples/')
  }
}

build('build_utils', 'docker-host', finalHook) {
  checkoutRepo()

  def pipeDefault
  runStage('load pipeline') {
    env.JENKINS_LIB = "./jenkins_lib"
    pipeDefault = load("${env.JENKINS_LIB}/pipeDefault.groovy")
  }

  pipeDefault() {
    def wsCache
    runStage('load workspace cache functions') {
      wsCache = load("${env.JENKINS_LIB}/wsCache.groovy")
    }

    def testDir = ".testcache"
    runStage('test cache file') {
      def sourceFile = "$testDir/test.txt"
      def targetFile = "$testDir/test2.txt"
      sh "mkdir -p $testDir"
      sh "echo test > $sourceFile"
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

      sh "rm -rf $cacheFile"

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

    def gitUtils
    runStage('load gitUtils') {
      gitUtils = load("${env.JENKINS_LIB}/gitUtils.groovy")
    }

    def dummyFilename = 'dummy'
    runStage('make dummy file') {
      sh "echo $BUILD_TAG > ${dummyFilename}"
    }
    runStage('test gitUtils.push non orphan') {
      gitUtils.push(commitMsg: "Jenkins build #${env.BUILD_TAG}",
                    files: dummyFilename, branch: "test-git-push");
    }
    def dummyDir = 'dummy-dir'
    runStage('test gitUtils.push orphan (and push file + directory)') {
      sh "mkdir -p ${dummyDir} && cp ${dummyFilename} ${dummyDir}/"
      gitUtils.push(commitMsg: "Jenkins build #${env.BUILD_TAG}",
                    files: dummyFilename + " ${dummyDir}", branch: "test-git-push-orphan", orphan: true);
    }

    def tmpDir = "jenkins-tmp"
    runStage('test gitUtils.checkout non orphan') {
      ws {
        try {
          gitUtils.checkout("git@github.com:rbkmoney/build_utils.git", "test-git-push", false, tmpDir)
          dir(tmpDir) {
            def dummyContent = sh(returnStdout: true, script: "cat ${dummyFilename}").trim()
            if (dummyContent != env.BUILD_TAG) {
              error "Content of checked out file does not match the build number"
            }
          }
        } finally {
          sh "rm -rf ${tmpDir} || true"
        }
      }
    }
    runStage('test gitUtils.checkout orphan') {
      ws {
        try {
          gitUtils.checkout("git@github.com:rbkmoney/build_utils.git", "test-git-push", true, tmpDir)
          sh "ls -al ${tmpDir}"
        } finally {
          sh "rm -rf ${tmpDir} || true"
        }
      }
    }

    runStage('test utils_repo') {
      withGithubPrivkey {
        sh 'make wc_init-repos'
      }
    }

    runStage('smoke test ') {
      sh 'make smoke_test'
    }
    runStage('test utils_container (wc)') {
      sh 'make wc_smoke_test'
    }
    runStage('test utils_container (wdeps)') {
      sh 'make wdeps_smoke_test'
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

