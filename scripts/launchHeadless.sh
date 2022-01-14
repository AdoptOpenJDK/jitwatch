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

if [ "$unamestr" = 'Darwin' ]; then
   export CLASSPATH=../ui/target/jitwatch-ui-1.4.4-shaded-mac.jar
else
   export CLASSPATH=../ui/target/jitwatch-ui-1.4.4-shaded-linux.jar
fi

"$JAVA_HOME/bin/java" -cp "$CLASSPATH" org.adoptopenjdk.jitwatch.launch.LaunchHeadless $@
