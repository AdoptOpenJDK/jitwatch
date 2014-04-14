#!/bin/sh

#uncomment the next line and set JDK_HOME as required
#export JDK_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_25.jdk/Contents/Home

if [ $JAVA_HOME = '' ]; then
   echo "JAVA_HOME has not been set"
   exit 0;
fi
export JDK_HOME=$JAVA_HOME

unamestr=`uname`
if [ "$unamestr" = 'Darwin' ]; then
   export JDK_HOME=`/usr/libexec/java_home`
fi
export CP=$JDK_HOME/lib/tools.jar:$JDK_HOME/jre/lib/jfxrt.jar:target/jitwatch-1.0.0-SNAPSHOT.jar
$JDK_HOME/bin/java -cp $CP com.chrisnewland.jitwatch.launch.LaunchHeadless $1 $2
