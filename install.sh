#!/bin/bash
JAVA_HOME=/usr/lib/jvm/java-6-openjdk-amd64
mkdir -p ~/iBench
if [ ! -d ~/iBench/codebase ]; then
    svn co https://dblab.cs.toronto.edu/svn/millercode/iBench/branches/2525-Mehrnaz ~/iBench/codebase
fi
cd ~/iBench
if [ ! -d ~/iBench/scalability ]; then
    svn co https://dblab.cs.toronto.edu/svn/millercode/iBench/docs/experiments/iBench/ ~/iBench/scalability/
fi
cd ~/iBench/codebase
ant
