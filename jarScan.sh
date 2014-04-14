#!/bin/sh

unamestr=`uname`
if [ "$unamestr" = 'Darwin' ]; then
   export JAVA_HOME=`/usr/libexec/java_home`
fi

export TARGET_JAR=$JAVA_HOME/jre/lib/rt.jar 

echo "Scanning $TARGET_JAR for methods above the default inlining threshold"

CLASSPATH=$CLASSPATH:lib/logback-classic-1.0.1.jar
CLASSPATH=$CLASSPATH:lib/logback-core-1.0.1.jar
CLASSPATH=$CLASSPATH:lib/slf4j-api-1.6.4.jar
CLASSPATH=$CLASSPATH:$JAVA_HOME/lib/tools.jar
CLASSPATH=$CLASSPATH:$JAVA_HOME/jre/lib/jfxrt.jar
CLASSPATH=$CLASSPATH:target/classes

$JAVA_HOME/bin/java -cp $CLASSPATH com.chrisnewland.jitwatch.demo.JarScan $TARGET_JAR > rtmethods.txt

echo "done"
