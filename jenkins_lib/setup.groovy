def call(modules, jenkins_lib: "build_utils/jenkins_lib", sh_tools: "build_utils/sh") {
    env.JENKINS_LIB = env.JENKINS_LIB ?: jenkins_lib
    env.SH_TOOLS = env.SH_TOOLS ?: sh_tools
    modules.each { mod ->
        if (!binding.hasVariable("${mod}")) {
            evaluate("def ${mod} = load(${env.JENKINS_LIB}/${mod}.groovy)")
        }
    }
}
