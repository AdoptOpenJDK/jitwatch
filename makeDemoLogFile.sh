#!/bin/sh

export unlock="-XX:+UnlockDiagnosticVMOptions"
export trace="-XX:+TraceClassLoading"
export compilation="-XX:+LogCompilation"

# Remove this option from the java line if you have not built the hsdis (HotSpot disassembly) binary
export assembly="-XX:+PrintAssembly"

echo "building example hotspot.log"
$JAVA_HOME/bin/java $unlock $trace $compilation $assembly -cp target/classes com.chrisnewland.jitwatch.demo.MakeHotSpotLog 2>&1 >/dev/null
echo "done"
