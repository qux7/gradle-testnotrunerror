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

    /**
     * Given a project, examine its properties and set this object's fields according
     * to the relevant project properties.
     * @param theProject Gradle project
     * @return this
     */
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
        return this
    }

    /**
     * Set the boolean fields of this object from the project properties map.
     * For each boolean field {@code <foo>}, the corresponding property name is {@code testnotrunerror.<foo>}.
     * @param map contains all project properties
     * @param reportProblem a closure taking two arguments, key and value, invoked if the value specified
     *                      for a boolean field is not 'true' or 'false'
     * @return this
     */
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

    /**
     * If the passed map contains a "mnemonic override", that is,
     * the property {@code 'test.not.run'} with the value of either {@code 'error'}, {@code 'warning'},
     * or {@code 'ignore'}, set the fields {@code enabled} and {@code stopOnFailure} correspondingly.
     * It is called "mnemonic" because it looks like "test.not.run=error", easy to remember.
     * @param map contains all project properties
     * @param reportProblem a closure taking two arguments, key and value, invoked if the value specified
     *                      for a boolean field is not 'error', 'warning' or 'ignore'
     * @return true if there was a "mnemonic override" in the map
     */
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
