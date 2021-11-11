def call(List modules, String jenkins_lib = "", String sh_tools = "") {
    env.JENKINS_LIB = jenkins_lib ?: env.JENKINS_LIB ?: "build_utils/jenkins_lib"
    env.SH_TOOLS = sh_tools ?: env.SH_TOOLS ?: "build_utils/sh"
    modules.each { mod ->
        if (!binding.hasVariable("${mod}")) {
            file = "${env.JENKINS_LIB}/${mod}.groovy"
            evaluate("${mod} = load(\"${file}\")")
        }
    }
}

return this;
