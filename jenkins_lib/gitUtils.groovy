#!groovy
// -*- mode: groovy -*-

/*
 * Copyright (c) 2016, Andrey Makeev <amaksoft@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice unmodified, this list of conditions, and the following
 *    disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and|or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * gitUtils.push - method for pushing build results to git repo via SSH. (SSH Agent Plugin required)
 * To keep things going while we wait for official Git Publish support for pipelines (https://issues.jenkins-ci.org/browse/JENKINS-28335)
 * gitUtils.push uses gitUtils.checkout methiod.
 *
 * Example call (Inline values):
 * gitUtils.push(repo: "git@github.com:rbkmoney/demo.git", branch: "master", commitMsg: "Jenkins build #${env.BUILD_NUMBER}",
 *         tagName: "build-${env.BUILD_NUMBER}", files: ".", username: "Jenkins CI", email: "jenkins-ci@example.com");
 *
 * Example call (Environment variables):
 * env.BRANCH_NAME = "mycoolbranch"// BRANCH_NAME is predefined in multibranch pipeline job
 * ...
 * gitUtils.push(commitMsg: "Jenkins build #${env.BUILD_NUMBER}", tagName: "build-${env.BUILD_NUMBER}", files: ".");
 *
 * @param args Map with following parameters:
 *   commitMsg : (String) commit message
 *   files : (String) list of files to push (space serparated) (Won't push files if not specified)
 *   tagName : (String) tag name (won't push tag if not specified)
 *   repo: (String) git/ssh url of a target repo (default: env.RBK_REPO_URL)
 *   branch : (String) git branch (default: env.BRANCH_NAME)
 *   orphan: (boolean) create a _new_ branch as orphan (from scratch) or from master HEAD (default: false).
 *           Note, that if remote branch exists, this parameter is irrelevant.
 *   username : (String) committer name (default: env.RBK_USERNAME)
 *   email : (String) committer email (default: env.RBK_EMAIL)
 *
 * gitUtils.checkout - method for checking out a branch from a repo via SSH.
 */

def push(Map args) {
    def defaultRepoName = "git@github.com:rbkmoney/${env.REPO_NAME}.git"

    def tagName = args.tagName
    def commitMsg = args.commitMsg
    def files = args.files
    def repo = args.repo != null ? args.repo : defaultRepoName
    def branch = args.branch != null ? args.branch : env.BRANCH_NAME
    def orphan = args.orphan != null ? args.orphan : false
    def username = args.username != null ? args.username : env.RBK_USERNAME
    def email = args.email != null ? args.email : env.RBK_EMAIL

    if (tagName == null && files == null) {
        echo "Neither tag nor files to push specified. Ignoring.";
        return;
    }
    if (branch == null) {
        error "Error. Invalid value: git branch = ${branch}";
    }
    if (username == null || email == null) {
        error "Error. Invalid value set: { username = ${username}, email = ${email} }"
    }

    def stashTar = 'gitUtils.tar.gz'
    def stashName = 'gitUtils.push'
    if (files != null) {
        sh "tar -czf ${stashTar} ${files}"
        stash name:stashName, allowEmpty:true, includes:stashTar
    }
    ws {
        def repoDir = 'gitUtils.push-tmp'
        try {
            checkout(repo, branch, orphan, repoDir)
            withGithubSshCredentials {
                dir(repoDir) {
                    sh """ git config push.default simple
                           git config user.name \"${username}\"
                           git config user.email \"${email}\"
                       """
                    if (files != null) {
                        unstash name:stashName
                        sh "tar -xzf ${stashTar}"
                        sh """ git add ${files} && git commit -m \"${commitMsg}\" || true """
                        sh """ git push origin ${branch}"""
                    }
                    if (tagName != null) {
                        sh """ git tag -fa \"${tagName}\" -m \"${commitMsg}\" """
                        sh """ git push origin \"${tagName}\" """
                    }
                }
            }
        } finally {
            sh "rm -rf ${repoDir} || true"
        }
    }
}

def checkout(String repo, String branch, boolean orphan, String targetDir) {
    def opts = orphan == true ? '--orphan' : '-b'
    withGithubSshCredentials {
        sh "git clone -q ${repo} ${targetDir}"
        dir(targetDir) {
            sh "git checkout ${branch} 2>/dev/null || (git checkout ${opts} ${branch} && git reset --hard)"
        }
    }
}

return this;

