/*
 * This Groovy source file was generated by the Gradle 'init' task.
 */
package test

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project
import spock.lang.Specification

/**
 * A simple unit test for the 'test.greeting' plugin.
 */
class Test6_9_3PluginTest extends Specification {
    def "plugin registers task"() {
        given:
        def project = ProjectBuilder.builder().build()

        when:
        project.plugins.apply("test.greeting")

        then:
        project.tasks.findByName("greeting") != null
    }
}
