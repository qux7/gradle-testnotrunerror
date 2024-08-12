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
You can override the boolean configuration parameters on the command line.
#### Mnemonic overrides
The valid combinations are:
```
-Ptest.not.run=error    enable this plugin, report problems as errors (the build will fail)
-Ptest.not.run=warning  enable this plugin, report problems as warnings (the build will not fail)
-Ptest.not.run=ignore   disable this plugin, do not report problems
```
If both `checkJavaSources` and `checkClasses` are set to `false`, these overrides
set both `checkJavaSources` and `checkClasses` to `true`.

For example:
```
./gradlew clean build -Ptest.not.run=error
```
#### Flag overrides

You can override each of the boolean configuration parameters separately, the syntax looks like:
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
#### Combining these two kinds of overrides

First, the configuration in the build file is applied.
Then, one mnemonic override, if present, is applied.
(Gradle uses a map for command-line properties, so it is impossible to have two values for the same key).
Then, the flag overrides are applied.
Then, if there was a mnemonic override that enabled the plugin (and thus expressed an intention to perform a check),
but both `checkJavaSources` and `checkClasses` are set to `false` (so that both types of checking are disabled),
then both `checkJavaSources` and `checkClasses` get set to `true` and thus both types of checking get enabled.
(And if only one method of checking is enabled, the other one remains disabled.)

## About this repository

### Linux symbolic links and Git

When you see a file that contains a path to another file and nothing else, it's how Github displays symbolic
links (symlinks), and what Git on Windows does with the symbolic links. Git on Linux has no problems with symlinks.
If you are not going to modify the code, it is safe to replace a symbolic link by the file that it points to,
provided that you can handle symlinks to symlinks.

### Three projects?

I compile the plugin with Gradle 6, but test it with Gradle 8.9.

This is _not_ normal, the problem is that Groovy 2 cannot run code compiled with Groovy 3,
but the code compiled with Groovy 2 runs on both Groovy 2 and Groovy 3.
Gradle 8.9 uses Groovy 3.0.21, Gradle 5.0 uses Groovy 2.5.4 (while the latest stable Groovy version is 4.0.22).

So I compile the plugin with Gradle 6.

But to test the plugin for compatibility with various Gradle versions, I use the latest version
of the `stutter` plugin, that requires the latest Gradle.

So we have two subdirectories, `build-with-gradle-6.2.1/` and `compat-test-with-gradle-8.9/`.

The 3rd subdirectory, `compat-test-with-a-script/`, does compatibility testing without any plugin.
It sort of does what `stutter` is for, but does not require one version of Gradle to run another Gradle,
so that you can rule out a conflict between them. It is because of these tests that I concluded
that the conflict between two i/o module versions happens because of Groovy, not `stutter`.

On the other hand, `stutter` is not perfect either, it cannot test against Gradle version X.Y.0 if there is X.Y.1.

In an ideal world, only one project would be needed.

