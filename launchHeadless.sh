#!/bin/sh
export CP=bin
$JDK_HOME/bin/java -cp $CP com.chrisnewland.jitwatch.launch.LaunchHeadless $1 $2
