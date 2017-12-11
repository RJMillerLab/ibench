#!/bin/bash
pushd $(dirname "${0}") > /dev/null
DIR=$(pwd -L)
java -Xmx4096m -jar ibench-fat.jar $*
popd > /dev/null
