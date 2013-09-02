#!/bin/sh

#uncomment the next line and set JDK_HOME if required
#export JDK_HOME=

export CP=$JDK_HOME/lib/tools.jar:$JDK_HOME/jre/lib/jfxrt.jar:bin
$JDK_HOME/bin/java -cp $CP com.chrisnewland.jitwatch.launch.LaunchUI
