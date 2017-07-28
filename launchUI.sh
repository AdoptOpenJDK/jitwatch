#!/bin/sh

# Get script dir, resolving symlinks; works on Linux and Mac.
jitwatch_dir=$(python -c '
from os.path import *
from sys import *
print(dirname(realpath(argv[1])))
' "$0")

CLASSPATH=$CLASSPATH:$JAVA_HOME/lib/tools.jar

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

CLASSPATH=$CLASSPATH:$JAVA_HOME/lib/tools.jar
CLASSPATH=$CLASSPATH:$JAVA_HOME/jre/lib/jfxrt.jar
CLASSPATH=$CLASSPATH:$jitwatch_dir/lib/logback-classic-1.1.2.jar
CLASSPATH=$CLASSPATH:$jitwatch_dir/lib/logback-core-1.1.2.jar
CLASSPATH=$CLASSPATH:$jitwatch_dir/lib/slf4j-api-1.7.7.jar
CLASSPATH=$CLASSPATH:$jitwatch_dir/core/target/classes
CLASSPATH=$CLASSPATH:$jitwatch_dir/ui/target/classes
CLASSPATH=$CLASSPATH:$jitwatch_dir/ui/src/main/resources

"$JAVA_HOME/bin/java" -Djava.library.path=$JAVA_HOME/lib/amd64 -cp "$CLASSPATH" $@ org.adoptopenjdk.jitwatch.launch.LaunchUI
