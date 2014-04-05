JITWatch
========

Log analyser and visualiser for the HotSpot JIT compiler.

<h3>For instructions and screenshots see the wiki</h3>
<h3>https://github.com/AdoptOpenJDK/jitwatch/wiki</h3>

<h2>Using ant</h2>
<h3>Build</h3>
<pre>ant clean
ant</pre>
<h3>Run</h3>
<pre>ant run</pre>

<h2>Using maven</h2>
<h3>Java 7</h3>
<pre>mvn clean compile test exec:java</pre>
<h3>Java 8</h3>
<pre>mvn -f pom-java8.xml clean compile test exec:java</pre>

<h2>Using shell</h2>
<h3>Run</h3>
<pre>#GUI Version
./launchUI.sh</pre>
<pre>#Simplifed Text Version
./launchHeadless.sh</pre>


