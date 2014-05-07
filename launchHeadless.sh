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

CLASSPATH=$CLASSPATH:lib/logback-classic-1.1.2.jar
CLASSPATH=$CLASSPATH:lib/logback-core-1.1.2.jar
CLASSPATH=$CLASSPATH:lib/slf4j-api-1.7.7.jar
CLASSPATH=$CLASSPATH:$JAVA_HOME/lib/tools.jar
CLASSPATH=$CLASSPATH:target/jitwatch-1.0.0-SNAPSHOT.jar

$JAVA_HOME/bin/java -cp $CLASSPATH com.chrisnewland.jitwatch.launch.LaunchHeadless $1 $2
