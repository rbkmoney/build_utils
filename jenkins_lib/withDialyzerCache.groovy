def call(String pltHomeDir, Closure body) {
    def withWsCache = load("${env.JENKINS_LIB}/withWsCache.groovy")
    String OtpReleaseFN = "otp_version." + suffix()
    sh "make wc_cmd WC_CMD='${env.SH_TOOLS}/otp_version ${OtpReleaseFN}'"
    String OtpRelease = readFile OtpReleaseFN
    def Plt = "_build/${pltHomeDir}/rebar3_${OtpRelease}_plt"
    withWsCache(Plt, body)
}

static def suffix() {
    def pool = ['a'..'z','A'..'Z',0..9].flatten()
    Random rand = new Random(System.currentTimeMillis())

    def passChars = (0..6).collect { pool[rand.nextInt(pool.size())] }
    passChars.join("")
}

return this;
