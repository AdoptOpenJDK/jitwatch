#!/bin/sh

# This script makes an example HotSpot log for trying out JITWatch.

# It executes the Java class org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog
# which contains methods that exercise various parts of the HotSpot JIT compilers
# such as inlining, intrinsics, and branch analysis.

# Make sure you have first built JITWatch using
# mvn clean compile test

# Start JITWatch using
# mvn exec:java

# When you start JITWatch open up the configuration (Config button) and mount
# JDK's src.zip (use the JDK src zip button)
# Java source files for the demo (src/main/java)
# Class files for the demo (target/classes)

# Now open the HotSpot log file created by this script and press the Start button :)

#-------------------------------------------------------
# Required VM switches
#-------------------------------------------------------

# Unlock the HotSpot logging options
export unlock="-XX:+UnlockDiagnosticVMOptions"

# Log each time a class is loaded (how JITWatch builds the class model)
export trace="-XX:+TraceClassLoading"

# Enable XML format HotSpot log output
export compilation="-XX:+LogCompilation"

export REQUIRED_SWITCHES="$unlock $trace $compilation"

#---------------------------------------------------------------------
# Optional VM switches (add as required to $OPTIONAL_SWITCHES variable
#---------------------------------------------------------------------

# Enable disassembly of native code into assembly language (AT&T / GNU format)
# Requires the hsdis (HotSpot disassembler) binary to be added to your JRE
# For hsdis build instructions see http://www.chrisnewland.com/building-hsdis-on-linux-amd64-on-debian-369
export assembly="-XX:+PrintAssembly"

# Change disassembly format from AT&T to Intel assembly
export intel="-XX:PrintAssemblyOptions=intel"

# Disable tiered compilation (enabled by default on Java 8, optional on Java 7)
export notiered="-XX:-TieredCompilation"

# Enable tiered compilation
export tiered="-XX:+TieredCompilation"

# Disable compressed oops (makes assembly easier to read)
export nocompressedoops="-XX:-UseCompressedOops"

export OPTIONAL_SWITCHES="$assembly $nocompressedoops"

unamestr=`uname`

if [ "$JAVA_HOME" = '' ]; then
  if [ "$unamestr" = 'Darwin' ]; then
     export JAVA_HOME=`/usr/libexec/java_home`
  else
     echo "JAVA_HOME has not been set."
     exit 0;
  fi
fi


$JAVA_HOME/bin/java -version

echo "VM Switches $REQUIRED_SWITCHES $OPTIONAL_SWITCHES"

echo "Building example HotSpot log"

CLASSPATH=$CLASSPATH:core/target/classes
CLASSPATH=$CLASSPATH:ui/target/classes
CLASSPATH=$CLASSPATH:lib/logback-classic-1.1.2.jar
CLASSPATH=$CLASSPATH:lib/logback-core-1.1.2.jar
CLASSPATH=$CLASSPATH:lib/slf4j-api-1.7.7.jar

"$JAVA_HOME/bin/java" $REQUIRED_SWITCHES $OPTIONAL_SWITCHES -cp "$CLASSPATH" org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog 2>&1 >/dev/null
echo "Done"
