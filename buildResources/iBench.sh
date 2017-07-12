#!/bin/bash
pushd $(dirname "${0}") > /dev/null
DIR=$(pwd -L)
java -Xmx4096m -classpath . -jar iBench.jar $*
popd > /dev/null
