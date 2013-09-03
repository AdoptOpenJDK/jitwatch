JITWatch
========

Log analyser for the Java HotSpot JIT compiler.

Processes a completed hotspot.log or tails a live one.

Easily view which methods are JIT-compiled and plot a timeline of JIT compilations.

Add source trees and jars/class folders to enable opening source / bytecode from a context menu.

--------------------------
To generate the log file used by JITWatch run your program with JVM switches

<pre>-XX:+UnlockDiagnosticVMOptions -XX:+TraceClassLoading -XX:+LogCompilation -XX:+PrintAssembly</pre>

If you want to use the -XX:+PrintAssembly switch to view the assembly language (disassembled JIT-compiled native code) then you need the to use a debug JVM build or have built the hsdis (HotSpot disassembler) binary.

<pre>Instructions for building hsdis are here
http://dropzone.nfshost.com/hsdis.htm</pre>
--------------------------
How to build:

<pre>ant</pre>

If you get compile errors relating to external dependencies (JDK lib/tools.jar and jre/lib/jfxrt.jar) then set your JAVA_HOME environment variable to point to a JDK

<pre>export JAVA_HOME=/path/to/jdk
ant</pre>
--------------------------
How to run with (GUI version)

<pre>./launchUI.sh</pre>

How to run (Console version - simplified output)

<pre>./launchHeadless.sh</pre>

If required, edit the launch scripts to set up your JDK_HOME variable by uncommenting and completing the line
<pre>#export JDK_HOME=</pre>
