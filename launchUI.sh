#!/bin/sh

#uncomment the next line and set JDK_HOME if required
export JDK_HOME=/Library/Java/JavaVirtualMachines//jdk1.7.0_25.jdk/Contents/Home

export CP=$JDK_HOME/lib/tools.jar:$JDK_HOME/jre/lib/jfxrt.jar:bin
$JDK_HOME/bin/java -cp $CP com.chrisnewland.jitwatch.launch.LaunchUI
