This project has moved to <a href="https://github.com/AdoptOpenJDK/jitwatch">AdoptOpenJDK/jitwatch</a> to benefit the wider Java community.

All further development will take place on the AdoptOpenJDK fork.

JITWatch
========

Log analyser and visualiser for the HotSpot JIT compiler.

<h3>For instructions and screenshots see the wiki</h3>
<h3>https://github.com/AdoptOpenJDK/jitwatch/wiki</h3>

JITwatch depends on tools.jar (from JDK_HOME/lib) for javap bytecode display.

JITWatch depends on jfxrt.jar (from JDK_HOME/jre/lib/) for the user interface.

<h2>Using ant</h2>
<h3>Build</h3>
<pre>ant clean
ant</pre>
<h3>Run</h3>
<pre>ant run</pre>

<h2>Using maven</h2>
<h3>Build</h3>
<pre>mvn package</pre>
<h3>Run</h3>
<pre>mvn exec:java</pre>

<h2>Using shell</h2>
<h3>Run</h3>
<pre>#GUI Version
./launchUI.sh</pre>
<pre>#Simplifed Text Version
./launchHeadless.sh</pre>


