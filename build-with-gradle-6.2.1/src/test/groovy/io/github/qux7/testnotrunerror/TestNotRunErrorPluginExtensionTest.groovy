package io.github.qux7.testnotrunerror

import spock.lang.Specification

class TestNotRunErrorPluginExtensionTest extends Specification {
    def "test setFieldsFromMap to true"() {
        when:
        def e =
                newTestNotRunErrorPluginExtension(false)
                        .setFieldsFromMap(('testnotrunerror.' + field): 'true') { k, v -> throw new RuntimeException("failed to add pair ($k: $v)") }
        then:
        e[field] == true

        and:
        TestNotRunErrorPluginExtension.booleanFields.every { e[it] == (it == field) }

        where:
        field << TestNotRunErrorPluginExtension.booleanFields
    }

    def "test setFieldsFromMap to false"() {
        when:
        def e =
                new TestNotRunErrorPluginExtension()
                        .setFieldsFromMap(('testnotrunerror.' + field): 'false') { k, v -> throw new RuntimeException("failed to add pair ($k: $v)") }
        then:
        e[field] == false

        and:
        TestNotRunErrorPluginExtension.booleanFields.every { e[it] == (it != field) }

        where:
        field << TestNotRunErrorPluginExtension.booleanFields
    }

    def "test setFieldsFromMap to invalid string"() {
        when:
        def e = new TestNotRunErrorPluginExtension()
        def f = e.setFieldsFromMap(('testnotrunerror.' + field): 'foo') { k, v -> throw new RuntimeException("failed to add pair ($k: $v)") }
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

    def "test setMnemonicFromMap"() {
        when:
        def ee = newTestNotRunErrorPluginExtension(init)
        def ff = ee.setMnemonicFromMap('test.not.run': value) { k, v -> throw new RuntimeException("failed to add pair ($k: $v)") }
        then:
        ee.enabled == en
        ee.stopOnFailure == st
        ee.checkClasses == cc
        ee.checkJavaSources == cj
        ff

        where:
        init  | value     || cc    | cj    | en    | st
        true  | 'error'   || true  | true  | true  | true
        true  | 'warning' || true  | true  | true  | false
        true  | 'ignore'  || true  | true  | false | false
        false | 'error'   || false | false | true  | true
        false | 'warning' || false | false | true  | false
        false | 'ignore'  || false | false | false | false
    }

    def "test setMnemonicFromMap with invalid string"() {
        when:
        def e = new TestNotRunErrorPluginExtension()
        def f = e.setMnemonicFromMap('test.not.run': 'plain-wrong') { k, v -> throw new RuntimeException("failed to add pair ($k: $v)") }

        then:
        !f
        def ex = thrown(RuntimeException)
        ex
        ex.getMessage() == "failed to add pair (test.not.run: plain-wrong)"

        and:
        TestNotRunErrorPluginExtension.booleanFields.every { e[it] == true }

    }

    def "test toString"() {
        when:
        def e = new TestNotRunErrorPluginExtension()
        def eToString = e.toString()

        then:
        eToString == "TestNotRunErrorPluginExtension(stopOnFailure:true, checkClasses:true, checkJavaSources:true, readSourceFiles:true, enabled:true)"
    }

    def "toString must include all booleanFields"() {
        when:
        def e = new TestNotRunErrorPluginExtension()
        def eToString = e.toString()

        then:
        TestNotRunErrorPluginExtension.booleanFields.every { eToString.contains(it) }
    }

    def "booleanFields must include all fields from generated toString"() {
        when:
        def e = new TestNotRunErrorPluginExtension()
        def s = e.toString()
        TestNotRunErrorPluginExtension.booleanFields.each { s -= it + ':true' }
        TestNotRunErrorPluginExtension.booleanFields.drop(2).each { s -= ', ' } // leave exactly one ', '

        then:
        s == 'TestNotRunErrorPluginExtension(, )'
    }

    TestNotRunErrorPluginExtension newTestNotRunErrorPluginExtension(boolean init) {
        new TestNotRunErrorPluginExtension(stopOnFailure: init, checkClasses: init, checkJavaSources: init, readSourceFiles: init, enabled: init)
    }
}
