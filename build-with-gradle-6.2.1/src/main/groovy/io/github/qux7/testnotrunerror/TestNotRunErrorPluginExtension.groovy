package io.github.qux7.testnotrunerror

import groovy.transform.ToString
import org.gradle.api.Project

@ToString(includeNames = true, includePackage = false)
class TestNotRunErrorPluginExtension {
    boolean stopOnFailure = true
    boolean checkClasses = true
    boolean checkJavaSources = true
    boolean readSourceFiles = true
    boolean enabled = true

    static final List booleanFields = ['stopOnFailure', 'checkClasses', 'checkJavaSources', 'readSourceFiles', 'enabled'].asImmutable()

    def setFromProjectProperties(Project theProject) {
        def projectProperties = theProject.properties
        def wasMnemonic = setMnemonicFromMap(projectProperties) { key, value ->
            System.err.println("ignoring property assignment $key=$value: expected 'error', 'warning' or 'ignore'")
        }
        setFieldsFromMap(projectProperties) { key, value ->
            println("ignoring property assignment $key=$value: expected 'true' or 'false'")
        }
        if (wasMnemonic && enabled && !checkJavaSources && !checkClasses) {
            println("WARNING: both checkJavaSources and checkClasses were disabled, setting them both to true")
            checkClasses = checkJavaSources = true
        }
    }

    TestNotRunErrorPluginExtension setFieldsFromMap(Map<String, String> map, Closure reportProblem) {
        booleanFields.each {
            def key = 'testnotrunerror.' + it
            if (map.containsKey(key)) {
                String value = map[key]
                if (value in ['true', 'false']) {
                    this[it] = value == 'true'
                } else {
                    reportProblem(key, value)
                }
            }
        }
        return this
    }

    boolean setMnemonicFromMap(Map<String, String> map, Closure reportProblem) {
        def modified = false
        def key = 'test.not.run'
        if (map.containsKey(key)) {
            modified = true
            String value = map[key]
            if (value == 'error') {
                stopOnFailure = enabled = true
            } else if (value == 'warning') {
                stopOnFailure = false
                enabled = true
            } else if (value == 'ignore') {
                stopOnFailure = enabled = false
            } else {
                modified = false
                reportProblem(key, value)
            }
        }
        return modified
    }

}
