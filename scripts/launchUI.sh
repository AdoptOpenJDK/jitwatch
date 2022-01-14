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

# Requires the jar to be built using
# mvn package
# or
# ant jar
#
#
# You may need to set -Xmx (max heap) and -XX:MaxPermSize
# if your hotspot.log references a lot of classes

if [ "$unamestr" = 'Darwin' ]; then
   export CLASSPATH=../ui/target/jitwatch-ui-1.4.4-shaded-mac.jar
else
   export CLASSPATH=../ui/target/jitwatch-ui-1.4.4-shaded-linux.jar
fi

"$JAVA_HOME/bin/java" -cp "$CLASSPATH" "$@" org.adoptopenjdk.jitwatch.launch.LaunchUI
