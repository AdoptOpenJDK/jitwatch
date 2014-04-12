#!/bin/sh

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

#-------------------------------------------------------
# Optional VM switches (add as required to the java command
#-------------------------------------------------------

# Enable disassembly of native code into assembly language (AT&T / GNU format)
export assembly="-XX:+PrintAssembly"

# Change disassembly format to Intel assembly
export intel="-XX:PrintAssemblyOptions=intel"

# Disable tiered compilation (enabled by default on Java 8, optional on Java 7)
export notiered="-XX:-TieredCompilation"

# Enable tiered compilation
export tiered="-XX:+TieredCompilation"

export OPTIONAL_SWITCHES="$assembly $notiered"

$JAVA_HOME/bin/java -version

echo "VM Switches $REQUIRED_SWITCHES $OPTIONAL_SWITCHES"

echo "Building example HotSpot log"
$JAVA_HOME/bin/java $REQUIRED_SWITCHES $OPTIONAL_SWITCHES -cp target/classes com.chrisnewland.jitwatch.demo.MakeHotSpotLog 2>&1 >/dev/null
echo "Done"
