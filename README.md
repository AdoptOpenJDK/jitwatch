JITWatch
========

Log analyser for the Java HotSpot JIT compiler.

Processes a completed hotspot.log or tails a live one.

Easily view which methods are JIT-compiled and plot a timeline of JIT compilations.

Add source trees and jars/class folders to enable opening source / bytecode from a context menu.

--------------------------
To generate the log file used by JITWatch run your program with JVM switches

-XX:+UnlockDiagnosticVMOptions -XX:+TraceClassLoading -XX:+LogCompilation -XX:+PrintAssembly 

If you want to use the -XX:+PrintAssembly switch to view the assembly language (disassembled JIT-compiled native code) then you need the to use a debug JVM build or have built the hsdis (HotSpot disassembler) binary.
Instructions for building hsdis are here http://dropzone.nfshost.com/hsdis.htm

--------------------------
NB: UI built with JavaFX so requires $JAVA_HOME/jre/lib/jfxrt.jar on compile and runtime classpath if you are using Java 7.

NB: Requires $JDK_HOME/lib/tools.jar on classpath if you want to inspect bytecode.
