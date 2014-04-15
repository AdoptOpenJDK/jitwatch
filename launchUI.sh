#!/bin/sh

unamestr=`uname`
if [ "$unamestr" = 'Darwin' ]; then
   export JDK_HOME=`/usr/libexec/java_home`
else
  if [ "$JAVA_HOME" = '' ]; then
     echo "JAVA_HOME has not been set."
     exit 0;
  fi
  export JDK_HOME=$JAVA_HOME
fi

# Requires the jar to be built using
# mvn package
# or
# ant jar
#
#
# You may need to set -Xmx (max heap) and -XX:MaxPermSize
# if your hotspot.log references a lot of classes

CLASSPATH=$CLASSPATH:lib/logback-classic-1.0.1.jar
CLASSPATH=$CLASSPATH:lib/logback-core-1.0.1.jar
CLASSPATH=$CLASSPATH:lib/slf4j-api-1.6.4.jar
CLASSPATH=$CLASSPATH:$JDK_HOME/lib/tools.jar
CLASSPATH=$CLASSPATH:$JDK_HOME/jre/lib/jfxrt.jar
CLASSPATH=$CLASSPATH:target/jitwatch-1.0.0-SNAPSHOT.jar

$JDK_HOME/bin/java -cp $CLASSPATH com.chrisnewland.jitwatch.launch.LaunchUI
