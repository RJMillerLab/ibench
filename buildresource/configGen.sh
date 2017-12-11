#!/bin/bash
pushd $(dirname "${0}") > /dev/null
DIR=$(pwd -L)
java -Xmx2048m -classpath .:ibench-fat.jar tresc.benchmark.iBenchDriver $*
popd > /dev/null


