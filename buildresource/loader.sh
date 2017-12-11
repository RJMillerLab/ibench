#!/bin/bash
pushd $(dirname "${0}") > /dev/null
DIR=$(pwd -L)
java -Xmx2048m -classpath .:ibench-fat.jar org.vagabond.commandline.loader.CommandLineLoader $*
popd > /dev/null

