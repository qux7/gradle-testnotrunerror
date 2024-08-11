/*
 * This Groovy source file was generated by the Gradle 'init' task.
 */
package test

import org.gradle.api.Project
import org.gradle.api.Plugin

/**
 * A simple 'hello world' plugin.
 */
class Test7_0Plugin implements Plugin<Project> {
    void apply(Project project) {
        // Register a task
        project.tasks.register("greeting") {
            doLast {
                println("Hello from plugin 'test.greeting'")
            }
        }
    }
}
