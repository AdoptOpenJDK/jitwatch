#!/bin/sh

unamestr=`uname`
if [ "$unamestr" = 'Darwin' ]; then
   export JAVA_HOME=`/usr/libexec/java_home`
fi

export TARGET_JAR=$JAVA_HOME/jre/lib/rt.jar 

echo "Scanning $TARGET_JAR for methods above the default inlining threshold"

$JAVA_HOME/bin/java -cp $JAVA_HOME/lib/tools.jar:target/classes/ com.chrisnewland.jitwatch.demo.JarScan $TARGET_JAR > rtmethods.txt

echo "done"
