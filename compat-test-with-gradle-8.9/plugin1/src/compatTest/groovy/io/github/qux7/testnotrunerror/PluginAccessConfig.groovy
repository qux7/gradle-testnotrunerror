package io.github.qux7.testnotrunerror

trait PluginAccessConfig {
    /** Force GStringImpl: Groovy's stripIntent() ignores the length of the last blank line */
    static String GROOVY_STRING = ""

    def description = "compatTest"

    def pluginManagementBlock = ""

    def pluginsBlock = """$GROOVY_STRING
        buildscript {
            def somewhereAbove = { String path ->
                File res,  cur = file('..');
                while(cur && !(res = new File(cur, path)).exists()) { cur = cur.parentFile };
                if (!cur) { throw new IllegalArgumentException("'" + path + "' not found in ancestor directories") };
                res
            }
            dependencies{
                classpath fileTree(dir: somewhereAbove('build-with-gradle-6.2.1/build/libs'), include: ['*.jar'])
            }
        }
        plugins {
            id 'application'
        }
        apply plugin: 'io.github.qux7.testnotrunerror'
    """.stripIndent()

    @Lazy
    def useGradleVersion = { System.getProperty("compat.gradle.version") }()
}
