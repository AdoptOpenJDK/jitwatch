#!/bin/sh

unamestr=`uname`
if [[ "$unamestr" == 'Darwin' ]]; then
   export JDK_HOME=`/usr/libexec/java_home`
fi

export CP=$JDK_HOME/lib/tools.jar:$JDK_HOME/jre/lib/jfxrt.jar:target/jitwatch-1.0.0-SNAPSHOT.jar
$JDK_HOME/bin/java -cp $CP com.chrisnewland.jitwatch.launch.LaunchUI
