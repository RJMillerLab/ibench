#!/bin/bash
JAVA_HOME=/usr/lib/jvm/java-6-openjdk-amd64
svn update ~/iBench/codebase
svn update ~/iBench/scalability
cd ~/iBench/codebase
ant
