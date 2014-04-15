#!/bin/sh

unamestr=`uname`
if [ "$unamestr" = 'Darwin' ]; then
   export JAVA_HOME=`/usr/libexec/java_home`
else
  if [ "$JAVA_HOME" = '' ]; then
     echo "JAVA_HOME has not been set."
     exit 0;
  fi
fi

CLASSPATH=$CLASSPATH:lib/logback-classic-1.0.1.jar
CLASSPATH=$CLASSPATH:lib/logback-core-1.0.1.jar
CLASSPATH=$CLASSPATH:lib/slf4j-api-1.6.4.jar
CLASSPATH=$CLASSPATH:$JAVA_HOME/lib/tools.jar
CLASSPATH=$CLASSPATH:$JAVA_HOME/jre/lib/jfxrt.jar
CLASSPATH=$CLASSPATH:target/jitwatch-1.0.0-SNAPSHOT.jar

$JAVA_HOME/bin/java -cp $CLASSPATH com.chrisnewland.jitwatch.launch.LaunchHeadless $1 $2
