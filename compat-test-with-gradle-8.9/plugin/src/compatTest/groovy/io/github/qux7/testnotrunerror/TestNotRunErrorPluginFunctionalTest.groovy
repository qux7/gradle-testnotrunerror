package io.github.qux7.testnotrunerror

import org.gradle.api.GradleException
import spock.lang.Specification
import org.gradle.testkit.runner.GradleRunner

/**
 * A simple functional test for the 'io.github.qux7.testnotrunerror.greeting' plugin.
 */
class TestNotRunErrorPluginFunctionalTest extends Specification {
    static String classCheckErrorMessagePrefix = "Test classes are present but tests were not executed:"
    static String javaSourceCheckErrorMessagePrefix = "Java source files are present but tests were not executed:"
    static String testFilterDetected = "no error because --tests was specified on the command line"
    static String stopOnFailureDisabled = "no error because `testnotrunerror { stopOnFailure = false }` was specified"
    private static String THIS_BUILD_FAILURE_IS_OK = "^^^^^ ^^^^^^ THIS BUILD WAS EXPECTED TO FAIL"

    def "test project runs ok"() {
        given:
        def projectDir = createProjectWithout([]).projectDir

        when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("clean", "test", "integrationTest")
        runner.withProjectDir(projectDir)
        def result = runner.build()

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
    }
    def "test project runs ok with --tests"() {
        given:
        def projectDir = createProjectWithout([]).projectDir

        when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("clean", "test", "--tests", "BarTest")
        runner.withProjectDir(projectDir)
        def result = runner.build()

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
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("clean", "test", "-s")
        runner.withProjectDir(projectDir)
        def result = runner.buildAndFail()
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
    def "detects missing unit test but stopOnFailure = false prevents task failure"() {
        given:
        def prj = createProjectWithout(['//unitTestBar'])
        def projectDir = prj.projectDir
        prj % "build.gradle" << """
            testnotrunerror {
                stopOnFailure = false
            }
        """.stripIndent()
        when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("clean", "test", "-s")
        runner.withProjectDir(projectDir)
        def result = runner.build()

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
    def "ignores missing unit test if asked"() {
        given:
        def prj = createProjectWithout(['//unitTestBar'])
        prj % "build.gradle" << """
            println('#####################')
            println(project.testnotrunerror)
            println(project.testnotrunerror.excludes)
            println(project.testnotrunerror.excludes.getClass())
            println('#{')
            project.testnotrunerror.excludes.forEach { x ->
                println("= "+x)
            }
            println('#}')
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
            println('#####################')
            println(project.testnotrunerror)
            println(project.testnotrunerror.excludes)
            println(project.testnotrunerror.excludes.getClass())
            println('#{')
            project.testnotrunerror.excludes.forEach { x ->
                println("= "+x)
            }
            println('#}')
            println("integrationTest.getClass()"+integrationTest.getClass())
        """.stripIndent()
        def projectDir = prj.projectDir

        when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("clean", "test", "-s")
        runner.withProjectDir(projectDir)
        def result = runner.build()

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
        prj % "build.gradle" << """
            println('#####################')
            println(project.testnotrunerror)
            println(project.testnotrunerror.excludes)
            println(project.testnotrunerror.excludes.getClass())
            println('#{')
            project.testnotrunerror.excludes.forEach { x ->
                println("= "+x)
            }
            println('#}')
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
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("clean", "integrationTest", "-s")
        runner.withProjectDir(projectDir)
        def result = runner.build()

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
    def "ignores missing unit test but not integration test"() {
        given:
        def prj = createProjectWithout(['//unitTestFoo','//integrationTestFoo'])
        prj % "build.gradle" << """
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
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("clean", "test", "integrationTest", "-s")
        runner.withProjectDir(projectDir)
        def result = runner.buildAndFail()
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
        def prj = createProjectWithout(['//unitTestBar','//integrationTestBar'])
        prj % "build.gradle" << """
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
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("clean", "test", "integrationTest", "-s")
        runner.withProjectDir(projectDir)
        def result = runner.build()

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
        def prj = createProjectWithout(['//unitTestBar','//unitTestFoo'])
        prj % "build.gradle" << """
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
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("clean", "test", "-s")
        runner.withProjectDir(projectDir)
        def result = runner.buildAndFail()
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
//    @spock.lang.Ignore
//    def "just create" () {
//        given:
//        createProjectWithout([])
//        when:
//        println('------------ok-----------')
//        then:
//        true == true
//    }

    //=============== utils ==========================================
    def somewhereAbove(String path) {
        File res,  cur = new File('..');
        while(cur && !(res = new File(cur, path)).exists()) {
            cur = cur.toPath().toAbsolutePath().normalize().toFile().parentFile
        };
        if (!cur) { throw new IllegalArgumentException("'" + path + "' not found in ancestor directories") };
        //new File('.').relatvePath(res)
        res
    }
    def getProjectVersion(File path) {
        String vLine = path.readLines().find { it.matches('version\\s.*') }
        getVersion(vLine)
    }
    def getVersion(String vLine) {
        if (vLine) {
            def va = vLine.split('\\s')
            vLine = va.size()>1?va[1]:null
        }
        if (vLine?.contains('//')) {
            vLine = vLine.split('//')[0]
        }
        vLine?.replaceAll('"','')?.replaceAll("'",'')?.trim()?:null
    }
 
    def createProjectWithout(Collection<String> toCommentOut) {
        String taskName = System.getProperty('taskName')
        def projectDir = new File("build/functionalTest")
        projectDir.deleteDir()
        projectDir.mkdirs()
        def prj = new SourceBaseDir(projectDir, toCommentOut)

        if (taskName == "scriptFunctionalTest") {
            prj / "settings.gradle" << """
                pluginManagement {
                    repositories {
                        mavenLocal()
                        gradlePluginPortal()
                    }
                }
            """.stripIndent()
        } else {
            prj / "settings.gradle" << """
            """.stripIndent()
        }

        String pluginsBlock = "";
        if (taskName == "functionalTest") {
            pluginsBlock = """
                plugins {
                    id 'application'
                    id 'io.github.qux7.testnotrunerror'
                }
            """.stripIndent()
        } else if (taskName.startsWith("compatTest")) {
            pluginsBlock = """\
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
        } else if (taskName == "scriptFunctionalTest") {
            String pluginVersion = getProjectVersion(somewhereAbove('build-with-gradle-6.2.1/build.gradle'))
            pluginsBlock = """\
                plugins {
                    id 'application'
                    id 'io.github.qux7.testnotrunerror' version '$pluginVersion'
                }
            """.stripIndent()
        } else {
            throw new IllegalArgumentException("task name=["+taskName+"] unsupported task name")
        }
        println("task name=["+taskName+"]")
        println("pluginsBlock=[$pluginsBlock]")
        prj / "build.gradle" << """
            $pluginsBlock

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
        prj / "src/main/java/ftest/App.java" << """
            package ftest;

            public class App {
                public String getGreeting() {
                    return new Foo().foo() + "-" + new Bar().bar();
                }
            
                public static void main(String[] args) {
                    System.out.println(new App().getGreeting());
                }
            }
            class Foo {
                String foo() { return "foo"; }
            }
            class Bar {
                String bar() { return "bar"; }
            }
        """.stripIndent()
        //====test====
        prj / "src/test/java/ftest/AppTest.java" << """
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
        prj / "src/test/java/ftest/BarTest.java" << """
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
        prj / "src/test/java/ftest/FooTest.java" << """
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
        //====integrationTest====
        prj / "src/integrationTest/java/ftest/AppTest.java" << """
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
        prj / "src/integrationTest/java/ftest/BarIntTest.java" << """
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
        prj / "src/integrationTest/java/ftest/FooTest.java" << """
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
        return prj
    }

}
class SourceFile {
    File file
    Collection<String> toCommentOut
    boolean fileMustExist

    SourceFile(File projectDir, String relativePath, boolean fileMustExist, Collection<String> toCommentOut) {
        this.file = new File(projectDir, relativePath)
        this.fileMustExist = fileMustExist
        this.toCommentOut = toCommentOut
    }
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
    }
    def commentOut(Collection<String> toCommentOut, String sourceCode) {
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
