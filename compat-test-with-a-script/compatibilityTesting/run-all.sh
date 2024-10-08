#!/bin/bash

if test -t 1 && command -v tput &>/dev/null
then
    red=`tput setaf 9`
    green=`tput setaf 10`
    yellow=`tput setaf 11`
    normal=`tput sgr0`
else
    red=""
    green=""
    yellow=""
    normal=""
fi

clean=clean
build=build
masterfile=master-source/TestNotRunErrorPluginFunctionalTest.groovy
linkedfileold=src/functionalTest/groovy/io/github/qux7/testnotrunerror/TestNotRunErrorPluginFunctionalTest.groovy
linkedfilenew=plugin/$linkedfileold
for f in *; do
    if [ -d "$f" ] && [ -f "$f/gradlew" ]; then
        # $f is a directory
        echo $yellow$f$normal
        # override path to use java11 instead of java13
        if [ -f "$f/substitutejava" ]; then
            overridepath="`cat $f/substitutejava`:"
        else
            overridepath=""
        fi
        # the subdrectory 'plugin' is generated by 'gradlew init' since some 6.x version
        if [ -d "$f/plugin" ]; then
            linkedfile=$linkedfilenew
        else
            linkedfile=$linkedfileold
        fi
        if ! cmp $masterfile $f/$linkedfile ; then
            echo $red"cannot run compatibility test: symbolic link broken!"$normal
            echo "link from: $f/$linkedfile"
            echo "link to: $masterfile"
            echo "Linux symbolic links may get broken after checking out the project on a Windows machine:"
            echo "instead of a symbolic link, you get a text file with a path."
            # If you just want to run the tests, you can replace the link with a copy of the target file,
            # but please DO NOT COMMIT IT back, the thing will become unmaintainable!
            break
        fi
        pushd $f
        PATH="$overridepath$PATH" ./gradlew $clean $build; rc=$?
        popd
        if [ $rc -eq 0 ]; then
            echo $green"$f: success"$normal
        else
            echo $red"$f: failure !!!!"$normal
            break
        fi
    fi
done
(exit $rc)

