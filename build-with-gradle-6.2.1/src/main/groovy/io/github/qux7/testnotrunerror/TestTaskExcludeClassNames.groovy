package io.github.qux7.testnotrunerror

class TestTaskExcludeClassNames {
    final String name
    Collection<String> excludeClassNames = []

    TestTaskExcludeClassNames(String name) {
        this.name = name
    }

    @Override
    public String toString() {
        return super.toString() + ":TestTaskExcludeClassNames{" +
                "name='" + name + '\'' +
                ", excludeClassNames=" + excludeClassNames +
                '}';
    }
}
