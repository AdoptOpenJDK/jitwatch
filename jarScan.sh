#!/bin/sh

# Static analysis of bytecode
# JarScan [flags] --mode=<mode> [options] [params] <jars and class folders>
#---------------------------------------------------------------------------------------------------
# Flags:
#     --verbose            Log progress information to stderr
#---------------------------------------------------------------------------------------------------
# Options:
#     --packages=a,b,c     Only include methods from named packages. E.g. --packages=java.util.*
#---------------------------------------------------------------------------------------------------
# Modes:
#---------------------------------------------------------------------------------------------------
#  maxMethodSize            List every method with bytecode larger than specified limit.
#     --limit=n             Report methods larger than n bytes.
#---------------------------------------------------------------------------------------------------
#  sequenceCount            Count instruction sequences.
#     --length=n            Report sequences of length n.
#---------------------------------------------------------------------------------------------------
#  invokeCount              Count the most called methods for each invoke instruction.
#    [--limit=n]            Limit to top n results per invoke type.
#---------------------------------------------------------------------------------------------------
#  nextInstructionFreq      List the most popular next instruction for each bytecode instruction.
#    [--limit=n]            Limit to top n results per instruction.
#---------------------------------------------------------------------------------------------------
#  allocationCount          Count the most allocated types.
#    [--limit=n]            Limit to top n results.
#---------------------------------------------------------------------------------------------------
#  instructionCount         Count occurences of each bytecode instruction.
#    [--limit=n]            Limit to top n results.
#---------------------------------------------------------------------------------------------------
#  sequenceSearch           List methods containing the specified bytecode sequence.
#     --sequence=a,b,c,...  Comma separated sequence of bytecode instructions.
#---------------------------------------------------------------------------------------------------
#  methodSizeHisto          List frequencies of method bytecode sizes.
#---------------------------------------------------------------------------------------------------
#  methodLength             List methods of the given bytecode size.
#    --length=n             Size of methods to find.
#---------------------------------------------------------------------------------------------------

unamestr=`uname`
if [ "$JAVA_HOME" = '' ]; then
  if [ "$unamestr" = 'Darwin' ]; then
     export JAVA_HOME=`/usr/libexec/java_home`
  else
     echo "JAVA_HOME has not been set."
     exit 0;
  fi
fi

# make jarScan.sh runnable from any directory (only works on Linux where readlink -f returns canonical path)
if [ "$unamestr" = 'Darwin' ]; then
  export JITWATCH=`dirname $0`
else
  export JARSCAN=`readlink -f $0`
  export JITWATCH=`dirname $JARSCAN`
fi

CLASSPATH=$CLASSPATH:$JITWATCH/lib/FreeLogJ-0.0.1.jar
CLASSPATH=$CLASSPATH:$JITWATCH/core/target/classes
CLASSPATH=$CLASSPATH:$JITWATCH/ui/target/classes
CLASSPATH=$CLASSPATH:$JITWATCH/core/build/classes/java/main
CLASSPATH=$CLASSPATH:$JITWATCH/ui/build/classes/java/main

"$JAVA_HOME/bin/java" -cp "$CLASSPATH" org.adoptopenjdk.jitwatch.jarscan.JarScan "$@"
