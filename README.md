JITWatch
========

Log analyser for the Java HotSpot JIT compiler.

<h3>For instructions and screenshots see the wiki</h3>
<h3>https://github.com/AdoptOpenJDK/jitwatch/wiki</h3>

<h2>Build with ant</h2>
<pre>ant clean
ant</pre>

If you get compile errors relating to external dependencies (JDK lib/tools.jar and jre/lib/jfxrt.jar) then set your JAVA_HOME environment variable to point to a JDK

<pre>export JAVA_HOME=/path/to/jdk
ant</pre>

<h2>Build with maven</h2>
<pre>mvn package</pre>

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
