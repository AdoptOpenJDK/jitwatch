jitwatch
========

Log analyser for Java HotSpot compiler.

Processes a completed hotspot.log or tails a live one.

Easily view which methods are compiled and plot a timeline of JIT compilations.

Add source trees and jars/class folders to enable opening source / bytecode from a context menu.

If a debug JVM or HotSpot disassembly binary (hsdis) was used then you can also view the assembly produced by the JIT compiler.

Visualiser separated from core code via a listener interface to allow alternative UIs to sit on top.

NB: User interface uses JavaFX so requires $JAVA_HOME/jre/lib/jfxrt.jar on compile and runtime classpath if you are using Java 7.
