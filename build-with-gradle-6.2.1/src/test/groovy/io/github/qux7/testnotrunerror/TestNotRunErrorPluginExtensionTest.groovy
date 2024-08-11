package io.github.qux7.testnotrunerror

import spock.lang.Specification

class TestNotRunErrorPluginExtensionTest extends Specification {
    def "test setFromMap to true"() {
        when:
        def e =
                new TestNotRunErrorPluginExtension(stopOnFailure: false, checkClasses: false, checkJavaSources: false, enabled: false)
                        .setFromMap(('testnotrunerror.' + field): 'true') { k, v -> throw new RuntimeException("failed to add pair ($k: $v)") }
        then:
        e[field] == true

        and:
        TestNotRunErrorPluginExtension.booleanFields.every { e[it] == (it == field) }

        where:
        field << TestNotRunErrorPluginExtension.booleanFields
    }

    def "test setFromMap to false"() {
        when:
        def e =
                new TestNotRunErrorPluginExtension()
                        .setFromMap(('testnotrunerror.' + field): 'false') { k, v -> throw new RuntimeException("failed to add pair ($k: $v)") }
        then:
        e[field] == false

        and:
        TestNotRunErrorPluginExtension.booleanFields.every { e[it] == (it != field) }

        where:
        field << TestNotRunErrorPluginExtension.booleanFields
    }

    def "test setFromMap to invalid string"() {
        when:
        def e = new TestNotRunErrorPluginExtension()
        def f = e.setFromMap(('testnotrunerror.' + field): 'foo') { k, v -> throw new RuntimeException("failed to add pair ($k: $v)") }
        then:
        e[field] == true
        !f
        def ex = thrown(RuntimeException)
        ex
        ex.getMessage() == "failed to add pair (testnotrunerror.$field: foo)"

        and:
        TestNotRunErrorPluginExtension.booleanFields.every { e[it] == true }

        where:
        field << TestNotRunErrorPluginExtension.booleanFields
    }
}
