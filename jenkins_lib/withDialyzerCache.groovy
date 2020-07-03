def call(Closure body) {
    def withWsCache = load("${env.JENKINS_LIB}/withWsCache.groovy")
    env.SH_TOOLS = "build_utils/sh"
    String OtpReleaseFN = "otp_version.tmp"
    sh "make wc_cmd WC_CMD='${env.SH_TOOLS}/otp_version ${OtpReleaseFN}'"
    String OtpRelease = readFile OtpReleaseFN
    def Plt = ["_build/default/rebar3_",OtpRelease,"_plt"].join("")
    withWsCache(Plt, body)
}

return this;
