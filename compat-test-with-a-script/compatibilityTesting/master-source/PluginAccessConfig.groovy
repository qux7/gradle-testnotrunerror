package io.github.qux7.testnotrunerror

trait PluginAccessConfig {
    /** Force GStringImpl: Groovy's stripIntent() ignores the length of the last blank line */
    static String GROOVY_STRING = ""

    def description = "scriptFunctionalTest"

    def pluginManagementBlock = """$GROOVY_STRING
                pluginManagement {
                    repositories {
                        mavenLocal()
                        gradlePluginPortal()
                    }
                }
            """.stripIndent()

    @Lazy
    def pluginsBlock = {
        String pluginVersion = getProjectVersion(somewhereAbove('build-with-gradle-6.2.1/build.gradle'))
        """$GROOVY_STRING
                plugins {
                    id 'application'
                    id 'io.github.qux7.testnotrunerror' version '$pluginVersion'
                }
            """.stripIndent()
    }()

    def useGradleVersion = null

    // === implementation logic ===
    /**
     * Find path in one of the ancestor directories.
     * Start from the parent directory (of the current directory), see if path exists,
     * then go to the parent's parent, and so on.
     * For example, instead of {@code "../../../../foo/bar"} we can write {@code somewhereAbove("foo/bar")}.
     * @param path A relative path that is expected to point ot an existing file or directory
     *             from one of the ancestor directories
     * @return full path of the found file/directory. It is equivalent to path appended to the nearest ancestor
     *         directory such that {@code new File(ancestor, path).exists()}
     * @exception IllegalArgumentException if there is no such ancestor directory that when we append path to it,
     *            we get a file or directory that exists.
     */
    def somewhereAbove(String path) {
        File res, cur = new File('..');
        while (cur && !(res = new File(cur, path)).exists()) {
            cur = cur.toPath().toAbsolutePath().normalize().toFile().parentFile
        };
        if (!cur) {
            throw new IllegalArgumentException("'" + path + "' not found in ancestor directories")
        };
        //new File('.').relatvePath(res)
        res
    }

    /**
     * Read the specified file (some build.gradle) and extract the project version from it.
     * @param path a file that contains a "version 'X.Y.Z'" string
     * @return the version string, e.g. 1.2.3
     */
    def getProjectVersion(File path) {
        String vLine = path.readLines().find { it.matches('version\\s.*') }
        getVersion(vLine)
    }

    /**
     * Given a string of the format "version 'X.Y.Z'", extract the version string from it
     * (X.Y.Z in this case, without quotes).
     * @param vLine a string containing the word "version" followed by whitespace and
     *              a version number, probably in quotes
     * @return a version string, without quotes, or null
     */
    def getVersion(String vLine) {
        if (vLine) {
            def va = vLine.split('\\s')
            vLine = va.size() > 1 ? va[1] : null
        }
        if (vLine?.contains('//')) {
            vLine = vLine.split('//')[0]
        }
        vLine?.replaceAll('"', '')?.replaceAll("'", '')?.trim() ?: null
    }
}
