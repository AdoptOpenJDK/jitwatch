#!/bin/sh

# Scans one or more jars for methods whose bytecode size are above the 
# default HotSpot inlining threshold for hot methods (325 bytes on Linux x86_64).
# Override the threshold with -DmaxMethodSize=n

unamestr=`uname`
if [ "$JAVA_HOME" = '' ]; then
  if [ "$unamestr" = 'Darwin' ]; then
     export JAVA_HOME=`/usr/libexec/java_home`
  else
     echo "JAVA_HOME has not been set."
     exit 0;
  fi
fi

if [ $# -lt 1 ]; then
  echo "Usage: jarScan.sh <path to 1st jar> [<2nd jar> ...]"
  exit 1;
fi

# make jarScan.sh runnable from any directory (only works on Linux where readlink -f returns canonical path)
if [ "$unamestr" = 'Darwin' ]; then
  export JITWATCH=`dirname $0`
else
  export JARSCAN=`readlink -f $0`
  export JITWATCH=`dirname $JARSCAN`
fi

CLASSPATH=$CLASSPATH:$JITWATCH/lib/logback-classic-1.1.2.jar
CLASSPATH=$CLASSPATH:$JITWATCH/lib/logback-core-1.1.2.jar
CLASSPATH=$CLASSPATH:$JITWATCH/lib/slf4j-api-1.7.7.jar
CLASSPATH=$CLASSPATH:$JAVA_HOME/lib/tools.jar
CLASSPATH=$CLASSPATH:$JITWATCH/target/classes

"$JAVA_HOME/bin/java" -cp "$CLASSPATH" org.adoptopenjdk.jitwatch.jarscan.JarScan "$@"
