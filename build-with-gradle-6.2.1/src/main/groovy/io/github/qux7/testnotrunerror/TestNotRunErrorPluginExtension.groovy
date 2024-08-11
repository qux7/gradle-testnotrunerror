package io.github.qux7.testnotrunerror

import org.gradle.api.Project

class TestNotRunErrorPluginExtension {
    boolean stopOnFailure = true
    boolean checkClasses = true
    boolean checkJavaSources = true
    boolean enabled = true

    static final List booleanFields = ['stopOnFailure', 'checkClasses', 'checkJavaSources', 'enabled'].asImmutable()

    @Override
    public String toString() {
        return "TestNotRunErrorPluginExtension{" +
                "stopOnFailure=" + stopOnFailure +
                ", checkClasses=" + checkClasses +
                ", checkJavaSources=" + checkJavaSources +
                ", enabled=" + enabled +
                '}';
    }

    def setFromCommandLineProperties(Project theProject) {
        setFromMap(theProject.gradle.startParameter.projectProperties) { key, value ->
            println("ignoring '-P$key=$value': expected 'true' or 'false'")
        }
    }

    TestNotRunErrorPluginExtension setFromMap(Map<String, String> map, Closure reportProblem) {
        booleanFields.each {
            def key = 'testnotrunerror.' + it
            if (map.containsKey(key)) {
                String value = map[key]
                if (['true', 'false'].contains(value)) {
                    this[it] = value == 'true'
                } else {
                    reportProblem(key, value)
                }
            }
        }
        return this
    }
}
