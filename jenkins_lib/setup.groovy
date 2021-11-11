def call(modules, jenkins_lib = null, sh_tools = null) {
    env.JENKINS_LIB = jenkins_lib ?: env.JENKINS_LIB ?: "build_utils/jenkins_lib"
    env.SH_TOOLS = sh_tools ?: env.SH_TOOLS ?: "build_utils/sh"
    modules.each { mod ->
        if (!binding.hasVariable("${mod}")) {
            evaluate("def ${mod} = load(\"${env.JENKINS_LIB}/${mod}.groovy\")")
        }
    }
}

return this;
