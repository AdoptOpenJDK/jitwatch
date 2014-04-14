#!/bin/sh

if [ $JAVA_HOME = '' ]; then
   echo "JAVA_HOME has not been set"
   exit 0;
fi
export JDK_HOME=$JAVA_HOME

unamestr=`uname`
if [ "$unamestr" = 'Darwin' ]; then
   export JDK_HOME=`/usr/libexec/java_home`
fi

# Requires the jar to be built using
# mvn package
# or
# ant jar
#
#
# You may need to set -Xmx (max heap) and -XX:MaxPermSize
# if your hotspot.log references a lot of classes

export CP=$JDK_HOME/lib/tools.jar:$JDK_HOME/jre/lib/jfxrt.jar:target/jitwatch-1.0.0-SNAPSHOT.jar
$JDK_HOME/bin/java -cp $CP com.chrisnewland.jitwatch.launch.LaunchUI
