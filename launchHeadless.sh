#!/bin/sh

#uncomment the next line and set JDK_HOME if required
#export JDK_HOME= 

export CP=bin
$JDK_HOME/bin/java -cp $CP com.chrisnewland.jitwatch.launch.LaunchHeadless $1 $2
