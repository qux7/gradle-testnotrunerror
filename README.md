# gradle-testnotrunerror
A Gradle plugin to report a build error if some test(s) do not run (e.g. due to a configuration error).

## Why

By default, Gradle does not complain if [some of] your tests do not run.

If you use more than one testing framework, e.g. both Junit 4 and Junit 5, it may take _weeks_ before you mention that some of your tests do not run.
You see, when developers do a dependency clean-up, they check that tests run, but they never check if _all_ tests run, they are just not paranoid enough, and this is normal.

This plugin makes Gradle complain if at least some of your tests do not run.

## How
### Basic usage
```
plugins {
    id 'io.github.qux7.testnotrunerror' version '0.1.0'
}
```
(This plugin already works, but has not yet been released; you have to `publishToMavenLocal` and use `mavenLocal()`)

TODO: release it

### Fine-tuning
```
testnotrunerror {
    enabled = false         // default: true. When false, this plugin does nothing.
    stopOnFailure = false   // default: true. When false, print a warning about tests that did not run,
                            // but do not cause build failure.
    checkClasses = false    // default: true. When false, do not check if there are .class files
                            // from which no tests were run.
    checkJavaSources = true // default: true. When false, do not check if there are .java files
                            // from which no tests were run.

    // specify which files do not contain tests (or whose tests are allowed not to run)
    excludes {
        // Note: task names must not conflict with script variable names! This is a DSL gotcha.
        test {              // Here, 'test' is a task name and a source set name (from project.sourceSets)
            excludeClassNames = ['com.foo.BarTest']
            // we write 'com.foo.BarTest' for both src/test/java/com/foo/BarTest.java
            //                                and build/classes/java/test/com/foo/BarTest.class 
        }
        integrationTest {   // Here, 'integrationTest' is a task name and a source set name
            excludeClassNames = ['com.foo.BarIntTest', 'com.foo.BazIntTest']
        }
    }
}
```

### Command-line overrides

You can override the boolean configuration parameters on the command line, the syntax looks like:
```
-Ptestnotrunerror.<parameter>=[true|false]
```
The valid combinations are:
```
-Ptestnotrunerror.enabled=true                enable this plugin, even if it was disabled in build.gradle
-Ptestnotrunerror.stopOnFailure=true          report problems as errors (the build will fail)
-Ptestnotrunerror.checkClasses=true           check .class files, are there any whose tests do not run?
-Ptestnotrunerror.checkJavaSources=true       check .java files, are there any whose tests do not run?
-Ptestnotrunerror.enabled=false               disable this plugin
-Ptestnotrunerror.stopOnFailure=false         report problems as warnings (no build failure)
-Ptestnotrunerror.checkClasses=false          do not check .class files
-Ptestnotrunerror.checkJavaSources=false      do not check .java files
```
For example:
```
./gradlew clean build -Ptestnotrunerror.stopOnFailure=false -Ptestnotrunerror.checkClasses=true
```
