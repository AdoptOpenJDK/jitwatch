#!/bin/sh

unamestr=`uname`
if [ "$JAVA_HOME" = '' ]; then
  if [ "$unamestr" = 'Darwin' ]; then
     export JAVA_HOME=`/usr/libexec/java_home`
  else
     echo "JAVA_HOME has not been set."
     exit 0;
  fi
fi

export TARGET_JAR=$JAVA_HOME/jre/lib/rt.jar 

echo "Scanning $TARGET_JAR for methods above the default inlining threshold"

CLASSPATH=$CLASSPATH:lib/logback-classic-1.1.2.jar
CLASSPATH=$CLASSPATH:lib/logback-core-1.1.2.jar
CLASSPATH=$CLASSPATH:lib/slf4j-api-1.7.7.jar
CLASSPATH=$CLASSPATH:$JAVA_HOME/lib/tools.jar
CLASSPATH=$CLASSPATH:$JAVA_HOME/jre/lib/jfxrt.jar
CLASSPATH=$CLASSPATH:target/classes

$JAVA_HOME/bin/java -cp $CLASSPATH org.adoptopenjdk.jitwatch.demo.JarScan $TARGET_JAR > rtmethods.txt

echo "done"
