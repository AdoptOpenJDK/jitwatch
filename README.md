JITWatch
========

Log analyser for Java HotSpot compiler.

Processes a completed hotspot.log or tails a live one.

To generate the log file used by JITWatch run your program with JVM switches

-XX:+UnlockDiagnosticVMOptions -XX:+TraceClassLoading -XX:+LogCompilation -XX:+PrintAssembly 

(hsdis is required for -XX:PrintAssembly)
If a debug JVM or HotSpot disassembly binary (hsdis) was used then you can also view the assembly produced by the JIT compiler.

Easily view which methods are compiled and plot a timeline of JIT compilations.

Add source trees and jars/class folders to enable opening source / bytecode from a context menu.

NB: UI built with JavaFX so requires $JAVA_HOME/jre/lib/jfxrt.jar on compile and runtime classpath if you are using Java 7.

NB: Requires $JDK_HOME/lib/tools.jar on classpath if you want to inspect bytecode.
