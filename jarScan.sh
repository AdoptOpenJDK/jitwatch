#!/bin/sh

# Scans one or more jars for methods whose bytecode size are above the 
# default HotSpot inlining threshold for hot methods (325 bytes on Linux x86_64).
# Override the threshold with -DmaxMethodSize=n

unamestr=`uname`
if [ "$unamestr" = 'Darwin' ]; then
   export JAVA_HOME=`/usr/libexec/java_home`
else
  if [ "$JAVA_HOME" = '' ]; then
     echo "JAVA_HOME has not been set."
     exit 0;
  fi
fi

if [ $# -lt 1 ]; then
  echo "Usage: jarScan.sh <path to 1st jar> [<2nd jar> ...]"
  exit 1;
fi

CLASSPATH=$CLASSPATH:lib/logback-classic-1.1.2.jar
CLASSPATH=$CLASSPATH:lib/logback-core-1.1.2.jar
CLASSPATH=$CLASSPATH:lib/slf4j-api-1.7.7.jar
CLASSPATH=$CLASSPATH:$JAVA_HOME/lib/tools.jar
CLASSPATH=$CLASSPATH:target/classes

$JAVA_HOME/bin/java -cp $CLASSPATH org.adoptopenjdk.jitwatch.jarscan.JarScan "$@"
