/*
 * This Groovy source file was generated by the Gradle 'init' task.
 */
package io.github.qux7.testnotrunerror

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import java.nio.file.Files
import java.util.concurrent.atomic.AtomicInteger

/**
 * Unit tests for the 'io.github.qux7.testnotrunerror' plugin.
 */
public class TestNotRunErrorPluginTest extends Specification {
    /** Force GStringImpl: Groovy's stripIntent() ignores the length of the last blank line */
    static String GROOVY_STRING = ""

    def "plugin does not register tasks"() {
        given:
        def project0 = ProjectBuilder.builder().build()
        def project = ProjectBuilder.builder().build()

        when:
        project.plugins.apply("io.github.qux7.testnotrunerror")

        then:
        project.tasks.toSet() == project0.tasks.toSet()
    }

    def "plugin creates extension"() {
        given:
        def project = ProjectBuilder.builder().build()

        when:
        project.plugins.apply("io.github.qux7.testnotrunerror")

        then:
        project.testnotrunerror instanceof TestNotRunErrorPluginExtension
    }

    def "can get class names from classes dir"() {
        given:
        def projectDir = new File("build/unitTest")
        projectDir.deleteDir()
        projectDir.mkdirs()
        new File(projectDir, "foo/bar/baz/qux").mkdirs()
        new File(projectDir, "foo/bar/org/corge").mkdirs()
        new File(projectDir, "foo/bar/org/grault").mkdirs()
        new File(projectDir, "foo/baar/baz/qux").mkdirs()
        new File(projectDir, "fooo/bar/baz/qux").mkdirs()
        new File(projectDir, "foo/bar/baz/qux/Foo.class") << ""
        new File(projectDir, "foo/bar/baz/qux/Bar.class") << ""
        new File(projectDir, "foo/bar/baz/qux/Boo.clazz") << ""
        new File(projectDir, "foo/bar/org/corge/Baz.class") << ""
        new File(projectDir, "foo/bar/org/grault/Qux.class") << ""
        new File(projectDir, "foo/baar/baz/qux/Baar.class") << ""
        new File(projectDir, "fooo/bar/baz/qux/FooBar.class") << ""

        when:
        def classNames1 = TestNotRunErrorPlugin.getCompiledClassNames(['foo/bar'].collect { new File(projectDir, it) })
        def classNames2 = TestNotRunErrorPlugin.getCompiledClassNames(['foo/bar', 'fooo/bar'].collect { new File(projectDir, it) })
        def classNames3 = TestNotRunErrorPlugin.getCompiledClassNames(['foo/baar'].collect { new File(projectDir, it) })

        then:
        classNames1 == ['baz.qux.Foo', 'baz.qux.Bar', 'org.corge.Baz', 'org.grault.Qux'].toSet()
        classNames2 == ['baz.qux.Foo', 'baz.qux.Bar', 'org.corge.Baz', 'org.grault.Qux', 'baz.qux.FooBar'].toSet()
        classNames3 == ['baz.qux.Baar'].toSet()
    }

    def "classes with dollar are ignored"() {
        given:
        def projectDir = new File("build/unitTest")
        projectDir.deleteDir()
        projectDir.mkdirs()
        new File(projectDir, "foo/bar/baz/qux").mkdirs()
        new File(projectDir, "foo/bar/org/corge").mkdirs()
        new File(projectDir, "foo/bar/org/grault").mkdirs()
        new File(projectDir, "foo/baar/baz/qux").mkdirs()
        new File(projectDir, "fooo/bar/baz/qux").mkdirs()
        new File(projectDir, 'foo/bar/baz/qux/Foo.class') << ""
        new File(projectDir, 'foo/bar/baz/qux/Bar.class') << ""
        new File(projectDir, 'foo/bar/baz/qux/Boo.clazz') << ""
        new File(projectDir, 'foo/bar/org/corge/Baz.class') << ""
        new File(projectDir, 'foo/bar/org/grault/Qux.class') << ""
        new File(projectDir, 'foo/baar/baz/qux/Baar.class') << ""
        new File(projectDir, 'fooo/bar/baz/qux/FooBar.class') << ""
        new File(projectDir, 'foo/bar/baz/qux/Foo$X.class') << ""
        new File(projectDir, 'foo/bar/baz/qux/Foo$X$Y.class') << ""
        new File(projectDir, 'foo/bar/baz/qux/Bar$Helper.class') << ""
        new File(projectDir, 'foo/bar/baz/qux/Boo$A.clazz') << ""
        new File(projectDir, 'foo/bar/org/corge/Baz$Y.class') << ""
        new File(projectDir, 'foo/bar/org/grault/Qux$A$B.class') << ""
        new File(projectDir, 'foo/baar/baz/qux/Baar$Q.class') << ""
        new File(projectDir, 'fooo/bar/baz/qux/FooBar$Aa$Bb.class') << ""

        when:
        def classNames1 = TestNotRunErrorPlugin.getCompiledClassNames(['foo/bar'].collect { new File(projectDir, it) })
        def classNames2 = TestNotRunErrorPlugin.getCompiledClassNames(['foo/bar', 'fooo/bar'].collect { new File(projectDir, it) })
        def classNames3 = TestNotRunErrorPlugin.getCompiledClassNames(['foo/baar'].collect { new File(projectDir, it) })

        then:
        classNames1 == ['baz.qux.Foo', 'baz.qux.Bar', 'org.corge.Baz', 'org.grault.Qux'].toSet()
        classNames2 == ['baz.qux.Foo', 'baz.qux.Bar', 'org.corge.Baz', 'org.grault.Qux', 'baz.qux.FooBar'].toSet()
        classNames3 == ['baz.qux.Baar'].toSet()
    }

    def "can get class names from java dir"() {
        given:
        def projectDir = new File("build/unitTest")
        projectDir.deleteDir()
        projectDir.mkdirs()
        new File(projectDir, "foo/bar/baz/qux").mkdirs()
        new File(projectDir, "foo/bar/org/corge").mkdirs()
        new File(projectDir, "foo/bar/org/grault").mkdirs()
        new File(projectDir, "foo/baar/baz/qux").mkdirs()
        new File(projectDir, "fooo/bar/baz/qux").mkdirs()
        new File(projectDir, "foo/bar/baz/qux/Foo.java") << ""
        new File(projectDir, "foo/bar/baz/qux/Bar.java") << ""
        new File(projectDir, "foo/bar/baz/qux/Boo.txt") << ""
        new File(projectDir, "foo/bar/org/corge/Baz.java") << ""
        new File(projectDir, "foo/bar/org/grault/Qux.java") << ""
        new File(projectDir, "foo/baar/baz/qux/Baar.java") << ""
        new File(projectDir, "fooo/bar/baz/qux/FooBar.java") << ""

        when:
        def classNames1 = TestNotRunErrorPlugin.getClassNamesFromSources(['foo/bar'].collect { new File(projectDir, it) })
        def classNames2 = TestNotRunErrorPlugin.getClassNamesFromSources(['foo/bar', 'fooo/bar'].collect { new File(projectDir, it) })
        def classNames3 = TestNotRunErrorPlugin.getClassNamesFromSources(['foo/baar'].collect { new File(projectDir, it) })

        then:
        classNames1 == ['baz.qux.Foo', 'baz.qux.Bar', 'org.corge.Baz', 'org.grault.Qux'].toSet()
        classNames2 == ['baz.qux.Foo', 'baz.qux.Bar', 'org.corge.Baz', 'org.grault.Qux', 'baz.qux.FooBar'].toSet()
        classNames3 == ['baz.qux.Baar'].toSet()
    }

    def "can detect @test.not.run=ignore"() {
        given:
        def projectDir = new File("build/unitTest")
        projectDir.deleteDir()
        projectDir.mkdirs()
        new File(projectDir, "src/main/java/foo/bar").mkdirs()
        new File(projectDir, "src/main/java/foo/bar/Foo.java") << """$GROOVY_STRING
            /**
             * this is a test
             */
            package foo.bar;

            //@test.not.run=ignore
            class Foo {
            } 
            """.stripIndent()
        new File(projectDir, "src/main/java/foo/bar/Bar.java") << """$GROOVY_STRING
            /**
             * this is a test
             */
            package foo.bar;

            class Bar {
            } 
            """.stripIndent()
        new File(projectDir, "src/main/java/foo/bar/Baz.java") << """$GROOVY_STRING
            /**
             * this is a test
             */
            package foo.bar;

            //@test.not.run!=ignore
            class Baz {
            } 
            """.stripIndent()
        new File(projectDir, "src/main/java/foo/bar/Qux.java") << """$GROOVY_STRING
            /**
             * this is a test
             */
            package foo.bar;

            //@test.not.run=ignore// this is to let the plugin know that it's not a test
            class Qux {
            } 
            """.stripIndent()
        new File(projectDir, "src/main/java/foo/bar/Corge.java") << """$GROOVY_STRING
            /**
             * this is a test
             */
            package foo.bar;

            class Corge {
            }
            //@test.not.run=ignore""".stripIndent()
        when:
        def isFooMarked = TestNotRunErrorPlugin.isMarkedForIgnoring(new File(projectDir, "src/main/java/foo/bar/Foo.java"))
        def isBarMarked = TestNotRunErrorPlugin.isMarkedForIgnoring(new File(projectDir, "src/main/java/foo/bar/Bar.java"))
        def isBazMarked = TestNotRunErrorPlugin.isMarkedForIgnoring(new File(projectDir, "src/main/java/foo/bar/Baz.java"))
        def isQuxMarked = TestNotRunErrorPlugin.isMarkedForIgnoring(new File(projectDir, "src/main/java/foo/bar/Qux.java"))
        def isCorgeMarked = TestNotRunErrorPlugin.isMarkedForIgnoring(new File(projectDir, "src/main/java/foo/bar/Corge.java"))

        then:
        isFooMarked
        !isBarMarked
        !isBazMarked
        isQuxMarked
        isCorgeMarked
    }

    def "a source file is read only up to the first occurrence of @test.not.run=ignore"() {
        given:
        def projectDir = new File("build/unitTest")
        projectDir.deleteDir()
        projectDir.mkdirs()
        new File(projectDir, "src/main/java/foo/bar").mkdirs()

        def sourceCode = """$GROOVY_STRING
            /**
             * this is a test
             */
            package foo.bar;

            // ------------------------------------------------
            // ------------------------------------------------
            // ------------------------------------------------
            // ------------------------------------------------
            // ------------------------------------------------
            // ------------------------------------------------

            //@test.not.run=ignore
            class Foo {
                // ================================================
                // ================================================
                // ================================================
                // ================================================
                // ================================================
                // ================================================
            }
        """.stripIndent()
        new File(projectDir, "src/main/java/foo/bar/Foo.java") << sourceCode

        when:
        def lineCount = new AtomicInteger();
        def origFile = new File(projectDir, "src/main/java/foo/bar/Foo.java")
        def spyStream = TestNotRunErrorPlugin.linesFromFile(origFile).peek {
            lineCount.incrementAndGet()
        }

        GroovySpy(TestNotRunErrorPlugin, global: true)
        TestNotRunErrorPlugin.linesFromFile(origFile) >> spyStream

        def res = TestNotRunErrorPlugin.isMarkedForIgnoring(origFile)

        then:
        res
        lineCount.get() == 14
        sourceCode.lines().count() == 22
    }

    def "test classNameToFileName"() {
        expect:
        'foo/bar/Baz.java' == TestNotRunErrorPlugin.classNameToFileName('foo.bar.Baz', '.java')
    }

    def "check that hard-coded html link to docs is valid"() {
        given:
        // read the docs file
        def readme = new File("../README.md").text
        // restore the section title
        def anchor = TestNotRunErrorPlugin.propertiesDocsUrl.split('#')[1]
        def title = anchor.replaceAll('-', ' ')

        expect:
        // containsIgnoreCase() requires Groovy 3
        readme.toLowerCase().contains('# ' + title) // the section title must be there in the docs file, otherwise the link is broken
    }
}
