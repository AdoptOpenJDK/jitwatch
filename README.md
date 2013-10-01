JITWatch
========

Log analyser for the Java HotSpot JIT compiler.

For more information and screenshots see the wiki https://github.com/AdoptOpenJDK/jitwatch/wiki

Processes a completed hotspot.log or tails a live one.

Easily view which methods are JIT-compiled and plot a timeline of JIT compilations.

Add source trees and jars/class folders to enable opening source / bytecode from a context menu.

To generate the log file used by JITWatch run your program with JVM switches

<pre>-XX:+UnlockDiagnosticVMOptions -XX:+TraceClassLoading -XX:+LogCompilation -XX:+PrintAssembly</pre>

If you want to use the -XX:+PrintAssembly switch to view the assembly language (disassembled JIT-compiled native code) then you need to use a debug JVM build or have built the hsdis (HotSpot disassembler) binary.

Instructions for building hsdis are here: http://dropzone.nfshost.com/hsdis.htm
<h1>Building JITWatch</h1>
<h2>Build with ant</h2>
<pre>ant clean
ant</pre>

If you get compile errors relating to external dependencies (JDK lib/tools.jar and jre/lib/jfxrt.jar) then set your JAVA_HOME environment variable to point to a JDK

<pre>export JAVA_HOME=/path/to/jdk
ant</pre>

<h2>Build with maven</h2>
<pre>mvn package</pre>
<h1>Running JITWatch</h1>
<h2>Run with launch scripts</h2>

<pre>#GUI Version
./launchUI.sh</pre>

<pre>#Simplifed Text Version
./launchHeadless.sh</pre>

If required, edit the launch scripts to set up your JDK_HOME variable by uncommenting and completing the line
<pre>#export JDK_HOME=</pre>

<h2>Run with ant</h2>
<pre>ant run</pre>

<h2>Run with maven</h2>
<pre>mvn exec:java</pre>
