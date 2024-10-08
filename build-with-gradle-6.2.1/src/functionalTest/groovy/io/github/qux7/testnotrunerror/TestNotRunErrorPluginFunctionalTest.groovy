package io.github.qux7.testnotrunerror

import org.gradle.api.GradleException
import org.gradle.util.GradleVersion
import org.junit.Assume
import spock.lang.Requires
import spock.lang.Specification
import org.gradle.testkit.runner.GradleRunner

/**
 * Test the functionality of the 'io.github.qux7.testnotrunerror' plugin.
 * This tests suite is used for both functional testing and compatibility testing, in the latter case
 * this file is compiled and run by different versions of Gradle/Groovy/Java.
 */
class TestNotRunErrorPluginFunctionalTest extends Specification implements PluginAccessConfig {
    static String classCheckErrorMessagePrefix = "Test classes are present but tests were not executed:"
    static String javaSourceCheckErrorMessagePrefix = "Java source files are present but tests were not executed:"
    static String testFilterDetected = "no error because --tests was specified on the command line"
    static String stopOnFailureDisabled = "no error because `testnotrunerror { stopOnFailure = false }` was specified"
    static String THIS_BUILD_FAILURE_IS_OK = "^^^^^ ^^^^^^ THIS BUILD WAS EXPECTED TO FAIL"
    static String extensionBeforeOverrides = "TestNotRunErrorPlugin extension before applying properties: "
    static String extensionAfterOverrides = "TestNotRunErrorPlugin extension after applying properties: "
    /** Force GStringImpl: Groovy's stripIntent() ignores the length of the last blank line */
    static String GROOVY_STRING = ""

    def "can build project, run and re-run tests"() {
        given:
        def projectDir = createProjectWithout([]).projectDir

        when:
        def result = createMyGradleRunner(projectDir)
                .withArguments("clean", "test", "integrationTest")
                .build()

        then:
        result.output.contains("running unitTestApp()")
        result.output.contains("running unitTestFoo()")
        result.output.contains("running unitTestBar()")
        result.output.contains("[test] Total tests run: 3")
        !result.output.contains("[test] $classCheckErrorMessagePrefix")
        !result.output.contains("[test] $javaSourceCheckErrorMessagePrefix")

        result.output.contains("running integrationTestApp()")
        result.output.contains("running integrationTestFoo()")
        result.output.contains("running integrationTestBar()")
        result.output.contains("[integrationTest] Total tests run: 3")
        !result.output.contains("[integrationTest] $classCheckErrorMessagePrefix")
        !result.output.contains("[integrationTest] $javaSourceCheckErrorMessagePrefix")

        and:
        !result.output.contains(extensionBeforeOverrides)
        !result.output.contains(extensionAfterOverrides)

        when:
        def result2 = createMyGradleRunner(projectDir)
                .withArguments("test")
                .build()

        then:
        result2.output.contains("Task :test UP-TO-DATE")
        !result2.output.contains("running unitTestApp()")
        !result2.output.contains("running unitTestFoo()")
        !result2.output.contains("running unitTestBar()")
        !result2.output.contains("[test] Total tests run: 3")
        !result2.output.contains("[test] $classCheckErrorMessagePrefix")
        !result2.output.contains("[test] $javaSourceCheckErrorMessagePrefix")

        !result2.output.contains("running integrationTestApp()")
        !result2.output.contains("running integrationTestFoo()")
        !result2.output.contains("running integrationTestBar()")
        !result2.output.contains("[integrationTest] Total tests run: 3")
        !result2.output.contains("[integrationTest] $classCheckErrorMessagePrefix")
        !result2.output.contains("[integrationTest] $javaSourceCheckErrorMessagePrefix")
    }

    def "can run project with info logging"() {
        given:
        def projectDir = createProjectWithout([]).projectDir
        when:
        def result = createMyGradleRunner(projectDir)
                .withArguments("test", "-i")
                .build()
        then:
        result.output.contains(extensionBeforeOverrides)
        result.output.contains(extensionAfterOverrides)
    }

    def "test project runs ok with --tests"() {
        given:
        def projectDir = createProjectWithout([]).projectDir

        when:
        def result = createMyGradleRunner(projectDir)
                .withArguments("clean", "test", "--tests", "BarTest")
                .build()

        then:
        !result.output.contains("running unitTestApp()")
        !result.output.contains("running unitTestFoo()")
        result.output.contains("running unitTestBar()")
        result.output.contains("[test] Total tests run: 1")
        result.output.contains("[test] $classCheckErrorMessagePrefix")
        result.output.contains("[test] $javaSourceCheckErrorMessagePrefix")
        result.output.contains("[test] $testFilterDetected")

        !result.output.contains("running integrationTestApp()")
        !result.output.contains("running integrationTestFoo()")
        !result.output.contains("running integrationTestBar()")
        !result.output.contains("[integrationTest] Total tests run: 3")
        !result.output.contains("[integrationTest] $classCheckErrorMessagePrefix")
        !result.output.contains("[integrationTest] $javaSourceCheckErrorMessagePrefix")
    }

    def "detects missing unit test"() {
        given:
        def projectDir = createProjectWithout(['//unitTestBar']).projectDir

        when:
        def result = createMyGradleRunner(projectDir)
                .withArguments("clean", "test", "-s")
                .buildAndFail()
        println(THIS_BUILD_FAILURE_IS_OK)

        then:
        result.output.contains("running unitTestApp()")
        result.output.contains("running unitTestFoo()")
        !result.output.contains("running unitTestBar()")
        result.output.contains("[test] Total tests run: 2")
        result.output.contains("[test] $classCheckErrorMessagePrefix [ftest.BarTest]")
        result.output.contains("[test] $javaSourceCheckErrorMessagePrefix [ftest.BarTest]")
        !result.output.contains("[test] $stopOnFailureDisabled")

        !result.output.contains("running integrationTestApp()")
        !result.output.contains("running integrationTestFoo()")
        !result.output.contains("running integrationTestBar()")
        !result.output.contains("[integrationTest] Total tests run:")
        !result.output.contains("[integrationTest] $classCheckErrorMessagePrefix")
        !result.output.contains("[integrationTest] $javaSourceCheckErrorMessagePrefix")
    }

    def "detects missing unit test but stopOnFailure = false in build.gradle prevents task failure"() {
        given:
        def prj = createProjectWithout(['//unitTestBar'])
        def projectDir = prj.projectDir
        prj % "build.gradle" << """$GROOVY_STRING
            testnotrunerror {
                stopOnFailure = false
            }
        """.stripIndent()

        when:
        def result = createMyGradleRunner(projectDir)
                .withArguments("clean", "test", "-s")
                .build()

        then:
        result.output.contains("running unitTestApp()")
        result.output.contains("running unitTestFoo()")
        !result.output.contains("running unitTestBar()")
        result.output.contains("[test] Total tests run: 2")
        result.output.contains("[test] $classCheckErrorMessagePrefix [ftest.BarTest]")
        result.output.contains("[test] $javaSourceCheckErrorMessagePrefix [ftest.BarTest]")
        result.output.contains("[test] $stopOnFailureDisabled")

        !result.output.contains("running integrationTestApp()")
        !result.output.contains("running integrationTestFoo()")
        !result.output.contains("running integrationTestBar()")
        !result.output.contains("[integrationTest] Total tests run:")
        !result.output.contains("[integrationTest] $classCheckErrorMessagePrefix")
        !result.output.contains("[integrationTest] $javaSourceCheckErrorMessagePrefix")
    }

    def "detects missing unit test but @test.not.run=ignore in the source prevents task failure"() {
        given:
        def prj = createProjectWithout(['//unitTestBar'])
        def projectDir = prj.projectDir
        prj % "src/test/java/ftest/BarTest.java" << """$GROOVY_STRING
            //@test.not.run=ignore
        """.stripIndent()

        when:
        def result = createMyGradleRunner(projectDir)
                .withArguments("clean", "test", "-s")
                .build()

        then:
        result.output.contains("running unitTestApp()")
        result.output.contains("running unitTestFoo()")
        !result.output.contains("running unitTestBar()")
        result.output.contains("[test] Total tests run: 2")
        !result.output.contains("[test] $classCheckErrorMessagePrefix [ftest.BarTest]")
        !result.output.contains("[test] $javaSourceCheckErrorMessagePrefix [ftest.BarTest]")
        !result.output.contains("[test] $stopOnFailureDisabled")

        !result.output.contains("running integrationTestApp()")
        !result.output.contains("running integrationTestFoo()")
        !result.output.contains("running integrationTestBar()")
        !result.output.contains("[integrationTest] Total tests run:")
        !result.output.contains("[integrationTest] $classCheckErrorMessagePrefix")
        !result.output.contains("[integrationTest] $javaSourceCheckErrorMessagePrefix")
    }

    def "detects missing unit test but command line override prevents task failure"() {
        given:
        def prj = createProjectWithout(['//unitTestBar'])
        def projectDir = prj.projectDir

        when:
        def result = createMyGradleRunner(projectDir)
                .withArguments("clean", "test", "-s", "-Ptestnotrunerror.stopOnFailure=false")
                .build()

        then:
        result.output.contains("running unitTestApp()")
        result.output.contains("running unitTestFoo()")
        !result.output.contains("running unitTestBar()")
        result.output.contains("[test] Total tests run: 2")
        result.output.contains("[test] $classCheckErrorMessagePrefix [ftest.BarTest]")
        result.output.contains("[test] $javaSourceCheckErrorMessagePrefix [ftest.BarTest]")
        result.output.contains("[test] $stopOnFailureDisabled")

        !result.output.contains("running integrationTestApp()")
        !result.output.contains("running integrationTestFoo()")
        !result.output.contains("running integrationTestBar()")
        !result.output.contains("[integrationTest] Total tests run:")
        !result.output.contains("[integrationTest] $classCheckErrorMessagePrefix")
        !result.output.contains("[integrationTest] $javaSourceCheckErrorMessagePrefix")
    }

    def "ignores incorrect property values"() {
        given:
        def prj = createProjectWithout([])
        def projectDir = prj.projectDir

        when:
        def result = createMyGradleRunner(projectDir)
                .withArguments("clean", "test", "-s", "-Ptestnotrunerror.stopOnFailure=ignore", "-Ptest.not.run=false")
                .build()

        then:
        result.output.contains("ignoring property assignment testnotrunerror.stopOnFailure=ignore: expected 'true' or 'false'")
        result.output.contains("ignoring property assignment test.not.run=false: expected 'error', 'warning' or 'ignore'")

        result.output.contains("running unitTestApp()")
        result.output.contains("running unitTestFoo()")
        result.output.contains("running unitTestBar()")
        result.output.contains("[test] Total tests run: 3")
        !result.output.contains("[test] $classCheckErrorMessagePrefix")
        !result.output.contains("[test] $javaSourceCheckErrorMessagePrefix")
        !result.output.contains("[test] $stopOnFailureDisabled")

        !result.output.contains("running integrationTestApp()")
        !result.output.contains("running integrationTestFoo()")
        !result.output.contains("running integrationTestBar()")
        !result.output.contains("[integrationTest] Total tests run:")
        !result.output.contains("[integrationTest] $classCheckErrorMessagePrefix")
        !result.output.contains("[integrationTest] $javaSourceCheckErrorMessagePrefix")
    }

    def "detects missing unit test but environment variable override prevents task failure"() {
        given:
        Assume.assumeTrue(GradleVersion.current() >= GradleVersion.version('5.2'))
        def prj = createProjectWithout(['//unitTestBar'])
        def projectDir = prj.projectDir

        when:
        def result = createMyGradleRunner(projectDir)
                .withArguments("clean", "test", "-s")
                .withEnvironment(['ORG_GRADLE_PROJECT_testnotrunerror.stopOnFailure':'false'])
                .build()

        then:
        result.output.contains("running unitTestApp()")
        result.output.contains("running unitTestFoo()")
        !result.output.contains("running unitTestBar()")
        result.output.contains("[test] Total tests run: 2")
        result.output.contains("[test] $classCheckErrorMessagePrefix [ftest.BarTest]")
        result.output.contains("[test] $javaSourceCheckErrorMessagePrefix [ftest.BarTest]")
        result.output.contains("[test] $stopOnFailureDisabled")

        !result.output.contains("running integrationTestApp()")
        !result.output.contains("running integrationTestFoo()")
        !result.output.contains("running integrationTestBar()")
        !result.output.contains("[integrationTest] Total tests run:")
        !result.output.contains("[integrationTest] $classCheckErrorMessagePrefix")
        !result.output.contains("[integrationTest] $javaSourceCheckErrorMessagePrefix")
    }

    def "detects missing unit test but system property override on command line prevents task failure"() {
        given:
        def prj = createProjectWithout(['//unitTestBar'])
        def projectDir = prj.projectDir

        when:
        def result = createMyGradleRunner(projectDir)
                .withArguments("clean", "test", "-s", "-Dorg.gradle.project.testnotrunerror.stopOnFailure=false")
                .build()

        then:
        result.output.contains("running unitTestApp()")
        result.output.contains("running unitTestFoo()")
        !result.output.contains("running unitTestBar()")
        result.output.contains("[test] Total tests run: 2")
        result.output.contains("[test] $classCheckErrorMessagePrefix [ftest.BarTest]")
        result.output.contains("[test] $javaSourceCheckErrorMessagePrefix [ftest.BarTest]")
        result.output.contains("[test] $stopOnFailureDisabled")

        !result.output.contains("running integrationTestApp()")
        !result.output.contains("running integrationTestFoo()")
        !result.output.contains("running integrationTestBar()")
        !result.output.contains("[integrationTest] Total tests run:")
        !result.output.contains("[integrationTest] $classCheckErrorMessagePrefix")
        !result.output.contains("[integrationTest] $javaSourceCheckErrorMessagePrefix")
    }

    def "detects missing unit test but system property override in gradle.properties prevents task failure"() {
        given:
        def prj = createProjectWithout(['//unitTestBar'])
        def projectDir = prj.projectDir
        prj / "gradle.properties" << """$GROOVY_STRING
            systemProp.org.gradle.project.testnotrunerror.stopOnFailure=false
        """.stripIndent()

        when:
        def result = createMyGradleRunner(projectDir)
                .withArguments("clean", "test", "-s")
                .build()

        then:
        result.output.contains("running unitTestApp()")
        result.output.contains("running unitTestFoo()")
        !result.output.contains("running unitTestBar()")
        result.output.contains("[test] Total tests run: 2")
        result.output.contains("[test] $classCheckErrorMessagePrefix [ftest.BarTest]")
        result.output.contains("[test] $javaSourceCheckErrorMessagePrefix [ftest.BarTest]")
        result.output.contains("[test] $stopOnFailureDisabled")

        !result.output.contains("running integrationTestApp()")
        !result.output.contains("running integrationTestFoo()")
        !result.output.contains("running integrationTestBar()")
        !result.output.contains("[integrationTest] Total tests run:")
        !result.output.contains("[integrationTest] $classCheckErrorMessagePrefix")
        !result.output.contains("[integrationTest] $javaSourceCheckErrorMessagePrefix")
    }

    def "detects missing unit test but gradle.properties override prevents task failure"() {
        given:
        def prj = createProjectWithout(['//unitTestBar'])
        def projectDir = prj.projectDir
        prj / "gradle.properties" << """$GROOVY_STRING
            testnotrunerror.stopOnFailure=false
        """.stripIndent()
        when:
        def result = createMyGradleRunner(projectDir)
                .withArguments("clean", "test", "-s")
                .build()

        then:
        result.output.contains("running unitTestApp()")
        result.output.contains("running unitTestFoo()")
        !result.output.contains("running unitTestBar()")
        result.output.contains("[test] Total tests run: 2")
        result.output.contains("[test] $classCheckErrorMessagePrefix [ftest.BarTest]")
        result.output.contains("[test] $javaSourceCheckErrorMessagePrefix [ftest.BarTest]")
        result.output.contains("[test] $stopOnFailureDisabled")

        !result.output.contains("running integrationTestApp()")
        !result.output.contains("running integrationTestFoo()")
        !result.output.contains("running integrationTestBar()")
        !result.output.contains("[integrationTest] Total tests run:")
        !result.output.contains("[integrationTest] $classCheckErrorMessagePrefix")
        !result.output.contains("[integrationTest] $javaSourceCheckErrorMessagePrefix")
    }

    def "detects missing unit test but command line override prevents task failure, approach 2"() {
        given:
        def prj = createProjectWithout(['//unitTestBar'])
        def projectDir = prj.projectDir

        when:
        def result = createMyGradleRunner(projectDir)
                .withArguments("clean", "test", "-s", "-Ptest.not.run=warning")
                .build()

        then:
        result.output.contains("running unitTestApp()")
        result.output.contains("running unitTestFoo()")
        !result.output.contains("running unitTestBar()")
        result.output.contains("[test] Total tests run: 2")
        result.output.contains("[test] $classCheckErrorMessagePrefix [ftest.BarTest]")
        result.output.contains("[test] $javaSourceCheckErrorMessagePrefix [ftest.BarTest]")
        result.output.contains("[test] $stopOnFailureDisabled")

        !result.output.contains("running integrationTestApp()")
        !result.output.contains("running integrationTestFoo()")
        !result.output.contains("running integrationTestBar()")
        !result.output.contains("[integrationTest] Total tests run:")
        !result.output.contains("[integrationTest] $classCheckErrorMessagePrefix")
        !result.output.contains("[integrationTest] $javaSourceCheckErrorMessagePrefix")
    }

    def "plugin may be disabled on command line, missing unit tests get ignored"() {
        given:
        def prj = createProjectWithout(['//unitTestBar'])
        def projectDir = prj.projectDir

        when:
        def result = createMyGradleRunner(projectDir)
                .withArguments("clean", "test", "integrationTest", "-s", "-Ptestnotrunerror.enabled=false")
                .build()

        then:
        result.output.contains("running unitTestApp()")
        result.output.contains("running unitTestFoo()")
        !result.output.contains("running unitTestBar()")
        !result.output.contains("[test] Total tests run: 2")
        !result.output.contains("[test] $classCheckErrorMessagePrefix [ftest.BarTest]")
        !result.output.contains("[test] $javaSourceCheckErrorMessagePrefix [ftest.BarTest]")
        !result.output.contains("[test] $stopOnFailureDisabled")

        result.output.contains("running integrationTestApp()")
        result.output.contains("running integrationTestFoo()")
        result.output.contains("running integrationTestBar()")
        !result.output.contains("[integrationTest] Total tests run:")
        !result.output.contains("[integrationTest] $classCheckErrorMessagePrefix")
        !result.output.contains("[integrationTest] $javaSourceCheckErrorMessagePrefix")
    }

    def "plugin may be disabled in build.gradle, missing unit tests get ignored"() {
        given:
        def prj = createProjectWithout(['//unitTestBar'])
        def projectDir = prj.projectDir
        prj % "build.gradle" << """$GROOVY_STRING
            testnotrunerror {
                enabled = false
            }
        """.stripIndent()
        when:
        def result = createMyGradleRunner(projectDir)
                .withArguments("clean", "test", "integrationTest", "-s")
                .build()

        then:
        result.output.contains("running unitTestApp()")
        result.output.contains("running unitTestFoo()")
        !result.output.contains("running unitTestBar()")
        !result.output.contains("[test] Total tests run: 2")
        !result.output.contains("[test] $classCheckErrorMessagePrefix [ftest.BarTest]")
        !result.output.contains("[test] $javaSourceCheckErrorMessagePrefix [ftest.BarTest]")
        !result.output.contains("[test] $stopOnFailureDisabled")

        result.output.contains("running integrationTestApp()")
        result.output.contains("running integrationTestFoo()")
        result.output.contains("running integrationTestBar()")
        !result.output.contains("[integrationTest] Total tests run:")
        !result.output.contains("[integrationTest] $classCheckErrorMessagePrefix")
        !result.output.contains("[integrationTest] $javaSourceCheckErrorMessagePrefix")
    }

    def "ignores missing unit test if asked"() {
        given:
        def prj = createProjectWithout(['//unitTestBar'])
        prj % "build.gradle" << """$GROOVY_STRING
            testnotrunerror {
                excludes {
                    // task names must not conflict with script variable names!
                    test {
                        excludeClassNames = ['ftest.BarTest']
                    }
                    integrationTest {
                        excludeClassNames = ['ftest.BarTest']
                    }
                }
            }
        """.stripIndent()
        def projectDir = prj.projectDir

        when:
        def result = createMyGradleRunner(projectDir)
                .withArguments("clean", "test", "-s")
                .build()

        then:
        result.output.contains("running unitTestApp()")
        result.output.contains("running unitTestFoo()")
        !result.output.contains("running unitTestBar()")
        result.output.contains("[test] Total tests run: 2")
        !result.output.contains("[test] $classCheckErrorMessagePrefix")
        !result.output.contains("[test] $javaSourceCheckErrorMessagePrefix")

        !result.output.contains("running integrationTestApp()")
        !result.output.contains("running integrationTestFoo()")
        !result.output.contains("running integrationTestBar()")
        !result.output.contains("[integrationTest] Total tests run:")
        !result.output.contains("[integrationTest] $classCheckErrorMessagePrefix")
        !result.output.contains("[integrationTest] $javaSourceCheckErrorMessagePrefix")
    }

    def "ignores missing integration test if asked"() {
        given:
        def prj = createProjectWithout(['//integrationTestBar'])
        prj % "build.gradle" << """$GROOVY_STRING
            testnotrunerror {
                excludes {
                    // task names must not conflict with script variable names!
                    integrationTest {
                        excludeClassNames = ['ftest.BarIntTest']
                    }
                }
            }
        """.stripIndent()
        def projectDir = prj.projectDir

        when:
        def result = createMyGradleRunner(projectDir)
                .withArguments("clean", "integrationTest", "-s")
                .build()

        then:
        !result.output.contains("running unitTestApp()")
        !result.output.contains("running unitTestFoo()")
        !result.output.contains("running unitTestBar()")
        !result.output.contains("[test] Total tests run:")
        !result.output.contains("[test] $classCheckErrorMessagePrefix")
        !result.output.contains("[test] $javaSourceCheckErrorMessagePrefix")

        result.output.contains("running integrationTestApp()")
        result.output.contains("running integrationTestFoo()")
        !result.output.contains("running integrationTestBar()")
        result.output.contains("[integrationTest] Total tests run: 2")
        !result.output.contains("[integrationTest] $classCheckErrorMessagePrefix")
        !result.output.contains("[integrationTest] $javaSourceCheckErrorMessagePrefix")
    }

    def "ignores missing unit test but not integration test with the same name"() {
        given:
        def prj = createProjectWithout(['//unitTestFoo', '//integrationTestFoo'])
        prj % "build.gradle" << """$GROOVY_STRING
            testnotrunerror {
                excludes {
                    // task names must not conflict with script variable names!
                    test {
                        excludeClassNames = ['ftest.FooTest']
                    }
                }
            }
        """.stripIndent()
        def projectDir = prj.projectDir

        when:
        def result = createMyGradleRunner(projectDir)
                .withArguments("clean", "test", "integrationTest", "-s")
                .buildAndFail()
        println(THIS_BUILD_FAILURE_IS_OK)

        then:
        result.output.contains("running unitTestApp()")
        !result.output.contains("running unitTestFoo()")
        result.output.contains("running unitTestBar()")
        result.output.contains("[test] Total tests run: 2")
        !result.output.contains("[test] $classCheckErrorMessagePrefix")
        !result.output.contains("[test] $javaSourceCheckErrorMessagePrefix")

        result.output.contains("running integrationTestApp()")
        !result.output.contains("running integrationTestFoo()")
        result.output.contains("running integrationTestBar()")
        result.output.contains("[integrationTest] Total tests run: 2")
        result.output.contains("[integrationTest] $classCheckErrorMessagePrefix [ftest.FooTest]")
        result.output.contains("[integrationTest] $javaSourceCheckErrorMessagePrefix [ftest.FooTest]")
    }

    def "ignores both unit and integration tests"() {
        given:
        def prj = createProjectWithout(['//unitTestBar', '//integrationTestBar'])
        prj % "build.gradle" << """$GROOVY_STRING
            testnotrunerror {
                excludes {
                    // task names must not conflict with script variable names!
                    test {
                        excludeClassNames = ['ftest.BarTest']
                    }
                    integrationTest {
                        excludeClassNames = ['ftest.BarIntTest']
                    }
                }
            }
        """.stripIndent()
        def projectDir = prj.projectDir

        when:
        def result = createMyGradleRunner(projectDir)
                .withArguments("clean", "test", "integrationTest", "-s")
                .build()

        then:
        result.output.contains("running unitTestApp()")
        result.output.contains("running unitTestFoo()")
        !result.output.contains("running unitTestBar()")
        result.output.contains("[test] Total tests run: 2")
        !result.output.contains("[test] $classCheckErrorMessagePrefix")
        !result.output.contains("[test] $javaSourceCheckErrorMessagePrefix")

        result.output.contains("running integrationTestApp()")
        result.output.contains("running integrationTestFoo()")
        !result.output.contains("running integrationTestBar()")
        result.output.contains("[integrationTest] Total tests run: 2")
        !result.output.contains("[integrationTest] $classCheckErrorMessagePrefix")
        !result.output.contains("[integrationTest] $javaSourceCheckErrorMessagePrefix")
    }

    def "test all as in compatibility test"() {
        given:
        def prj = createProjectWithout(['//unitTestBar', '//unitTestFoo'])
        prj % "build.gradle" << """$GROOVY_STRING
            testnotrunerror {
                excludes {
                    // task names must not conflict with script variable names!
                    test {
                        excludeClassNames = ['ftest.BarTest']
                    }
                    integrationTest { // unused in this test
                        excludeClassNames = ['ftest.BarIntTest']
                    }
                }
            }
        """.stripIndent()
        def projectDir = prj.projectDir

        when:
        def result = createMyGradleRunner(projectDir)
                .withArguments("clean", "test", "-s")
                .buildAndFail()
        println(THIS_BUILD_FAILURE_IS_OK)

        then:
        result.output.contains("running unitTestApp()")
        !result.output.contains("running unitTestFoo()")
        !result.output.contains("running unitTestBar()")
        result.output.contains("[test] Total tests run: 1")
        result.output.contains("[test] $classCheckErrorMessagePrefix [ftest.FooTest]")
        result.output.contains("[test] $javaSourceCheckErrorMessagePrefix [ftest.FooTest]")

        !result.output.contains("running integrationTestApp()")
        !result.output.contains("running integrationTestFoo()")
        !result.output.contains("running integrationTestBar()")
        !result.output.contains("[integrationTest] Total tests run:")
        !result.output.contains("[integrationTest] $classCheckErrorMessagePrefix")
        !result.output.contains("[integrationTest] $javaSourceCheckErrorMessagePrefix")
    }

    //=============== utils ==========================================
    /**
     * Create and configure a GradleRunner.
     * All tests in this test suite require the same pre-configuration.
     * @param projectDir project directory
     * @return the created and pre-configured GradleRunner
     */
    GradleRunner createMyGradleRunner(File projectDir) {
        def runner = GradleRunner.create()
                .forwardOutput()
                .withPluginClasspath()
                .withProjectDir(projectDir)
        if (useGradleVersion) {
            runner.withGradleVersion(useGradleVersion)
        }
        runner
    }

    /**
     * Create a test project, commenting out the specified lines.
     * The project's directory is deleted and created again, then project's files are created.
     * All tests in this test suite use the same test project with some modifications.
     * The lines that may be commented out are already marked with special comments,
     * and if that comment is included into the toCommentOut list, the line with that comment
     * will be commented out. We could just delete such lines, but this way it's easier
     * to understand what has happened during code generation.
     * @param toCommentOut list of strings; any line that contains one of this strings will be commented out
     * @return a SourceBaseDir object that represents the project
     */
    def createProjectWithout(Collection<String> toCommentOut) {
        def projectDir = new File("build/functionalTest")
        projectDir.deleteDir()
        projectDir.mkdirs()
        def prj = new SourceBaseDir(projectDir, toCommentOut)

        //==== build script ====
        prj / "settings.gradle" << "$pluginManagementBlock"

        prj / "build.gradle" << "$pluginsBlock" + """$GROOVY_STRING
                repositories {
                    jcenter()
                }
    
                // integrationTest {
                sourceSets {
                    integrationTest {
                        compileClasspath += sourceSets.main.output
                        runtimeClasspath += sourceSets.main.output
                    }
                }
                configurations {
                    integrationTestImplementation.extendsFrom implementation
                    integrationTestRuntimeOnly.extendsFrom runtimeOnly
                }
                dependencies {
                    integrationTestImplementation 'junit:junit:4.12'
                }
                tasks.register('integrationTest', Test) {
                    description = 'Runs integration tests.'
                    group = 'verification'
    
                    testClassesDirs = sourceSets.integrationTest.output.classesDirs
                    classpath = sourceSets.integrationTest.runtimeClasspath
                    shouldRunAfter('test')
                }
                // } integrationTest
    
                dependencies {
                    testImplementation 'junit:junit:4.12'
                }
    
                application {
                    mainClassName = 'ftest.App'
                }
    
                project.tasks.withType(Test) {
                    testLogging.showStandardStreams = true
                }
        """.stripIndent()
        //==== source code ====
        prj / "src/main/java/ftest/App.java" << """$GROOVY_STRING
            package ftest;

            public class App {
                public String getGreeting() {
                    return new Foo().foo() + "-" + new Bar().bar();
                }
            
                public static void main(String[] args) {
                    System.out.println(new App().getGreeting());
                }
            }
        """.stripIndent()
        prj / "src/main/java/ftest/Foo.java" << """$GROOVY_STRING
            package ftest;

            class Foo {
                String foo() { return "foo"; }
            }
        """.stripIndent()
        prj / "src/main/java/ftest/Bar.java" << """$GROOVY_STRING
            package ftest;

            class Bar {
                String bar() { return "bar"; }
            }
        """.stripIndent()
        //==== unit tests ====
        prj / "src/test/java/ftest/AppTest.java" << """$GROOVY_STRING
            package ftest;
            
            import org.junit.Test;
            import static org.junit.Assert.*;
            
            public class AppTest {
                @Test
                public void testAppHasAGreeting() {
                    System.out.println("running unitTestApp()");
                    App classUnderTest = new App();
                    assertNotNull("app should have a greeting", classUnderTest.getGreeting());
                }
                static class Helper {
                    int x;
                }
            }
        """.stripIndent()
        prj / "src/test/java/ftest/BarTest.java" << """$GROOVY_STRING
            package ftest;
            
            import org.junit.Test;
            
            import static org.junit.Assert.*;
            
            public class BarTest {
                @Test //unitTestBar
                public void bar() {
                    System.out.println("running unitTestBar()");
                    assertEquals("bar", new Bar().bar());
                }
                static class Helper {
                    int x;
                }
            }
        """.stripIndent()
        prj / "src/test/java/ftest/FooTest.java" << """$GROOVY_STRING
            package ftest;
            
            import org.junit.Test;
            
            import static org.junit.Assert.*;
            
            public class FooTest {
                @Test //unitTestFoo
                public void foo() {
                    System.out.println("running unitTestFoo()");
                    assertEquals("foo", new Foo().foo());
                }
                static class Helper {
                    int x;
                }
            }
        """.stripIndent()
        //==== integration tests ====
        prj / "src/integrationTest/java/ftest/AppTest.java" << """$GROOVY_STRING
            package ftest;
            
            import org.junit.Test;
            import static org.junit.Assert.*;
            
            public class AppTest {
                @Test
                public void testAppHasAGreeting() {
                    System.out.println("running integrationTestApp()");
                    App classUnderTest = new App();
                    assertEquals("foo-bar", classUnderTest.getGreeting());
                }
                static class Helper {
                    int x;
                }
            }
        """.stripIndent()
        prj / "src/integrationTest/java/ftest/BarIntTest.java" << """$GROOVY_STRING
            package ftest;
            
            import org.junit.Test;
            
            import static org.junit.Assert.assertEquals;
            
            public class BarIntTest {
                @Test //integrationTestBar
                public void bar() {
                    System.out.println("running integrationTestBar()");
                    assertEquals("bar", new Bar().bar());
                }
                static class Helper {
                    int x;
                }
            }
        """.stripIndent()
        prj / "src/integrationTest/java/ftest/FooTest.java" << """$GROOVY_STRING
            package ftest;
            
            import org.junit.Test;
            
            import static org.junit.Assert.assertEquals;
            
            public class FooTest {
                @Test //integrationTestFoo
                public void foo() {
                    System.out.println("running integrationTestFoo()");
                    assertEquals("foo", new Foo().foo());
                }
                static class Helper {
                    int x;
                }
            }
        """.stripIndent()
        //==== end ====
        return prj
    }

}

/**
 * Objects of this class appear as a result of {@code prj / "filaname"} or {@code prj % "filename"}.
 * See the class {@code SourceBaseDir}.
 */
class SourceFile {
    File file
    Collection<String> toCommentOut
    boolean fileMustExist

    SourceFile(File projectDir, String relativePath, boolean fileMustExist, Collection<String> toCommentOut) {
        this.file = new File(projectDir, relativePath)
        this.fileMustExist = fileMustExist
        this.toCommentOut = toCommentOut
    }

    /**
     * Write a string to a file. Comment out all of the lines that contain text specified in
     * any string from the {@code toCommentOut} field.
     * @param s the string to write
     * @return this
     */
    def leftShift(String s) {
        if (fileMustExist) {
            if (!file.exists()) {
                throw new GradleException("file $file does not exist!")
            }
        } else {
            if (file.exists()) {
                throw new GradleException("file $file already exists!")
            }
            if (file.parentFile) {
                file.parentFile.mkdirs()
            }
        }
        file << commentOut(toCommentOut, s)
        this
    }

    static def commentOut(Collection<String> toCommentOut, String sourceCode) {
        sourceCode
                .readLines()
                .collect({ line ->
                    toCommentOut.any { marking ->
                        line.contains(marking)
                    } ? "//" + line : line
                })
                .join("\n")
    }
}

/**
 * A class that implements syntactic sugar like {@code prj / "settings.gradle" << "$pluginManagementBlock"}
 * and {@code prj % "build.gradle" << "testnotrunerror { enabled = false }"}. An object of this class
 * represents the root directory of some project, usually called {@code prj}. There are two operations,
 * {@code /} and {@code %}, {@code prj / "filename"} creates a new file and allows writing to it
 * with {@code <<}; {@code prj % "filename"} allows writing to an existing file. An exception is thrown
 * if the file already exists (for {@code /}) or does not exist (for {@code %}).
 *  <p/>
 * So {@code prj / "settings.gradle" << "$pluginManagementBlock"} means "create 'settings.gradle' and
 * write the string that follows {@code <<} to it", and
 * {@code prj % "build.gradle" << "testnotrunerror { enabled = false }"} means "append the specified
 * code to the existing 'build.gradle'"
 */
class SourceBaseDir {
    File projectDir
    Collection<String> toCommentOut

    SourceBaseDir(File projectDir, Collection<String> toCommentOut) {
        this.projectDir = projectDir
        this.toCommentOut = toCommentOut
    }

    def div(String relativePath) {
        new SourceFile(projectDir, relativePath, false, toCommentOut)
    }

    def mod(String relativePath) {
        new SourceFile(projectDir, relativePath, true, toCommentOut)
    }
}
