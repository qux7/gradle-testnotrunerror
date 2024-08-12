package io.github.qux7.testnotrunerror

trait PluginAccessConfig {
    /** Force GStringImpl: Groovy's stripIntent() ignores the length of the last blank line */
    static String GROOVY_STRING = ""

    def description = "functionalTest"

    def pluginManagementBlock = ""

    def pluginsBlock = """$GROOVY_STRING
                plugins {
                    id 'application'
                    id 'io.github.qux7.testnotrunerror'
                }
            """.stripIndent()

    def useGradleVersion = null
}
