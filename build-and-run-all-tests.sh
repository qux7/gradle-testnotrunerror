#!/bin/bash

# Build the plugin and run all tests.
#
# This script is "runnable documentation".
# It shows you how the three subdirectories are used.
# If this script succeeds, your have no problems with your build environment.
#
# The subdirectories contain:
# build-with-gradle-6.2.1 -- the plugin source, unit tests, functional tests
# compat-test-with-a-script/compatibilityTesting -- the quick compatibility test
# compat-test-with-gradle-8.9 -- the thorough compatibility test

pushd build-with-gradle-6.2.1 \
&& ./runme \
&& popd \
&& pushd compat-test-with-a-script/compatibilityTesting \
&& ./run-all.sh \
&& popd \
&& pushd compat-test-with-gradle-8.9 \
&& ./runme \
&& popd \
; rc=$?

echo "rc=$rc"
if [ $rc -eq 0 ]; then
    echo "!!!! SUCCESS !!!!"
else
    echo "!!!! FAILURE !!!!"
fi

# restore the return code
(exit $rc)

