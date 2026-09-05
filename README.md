JITWatch
========

Log analyser and visualiser for the HotSpot JIT compiler.

* Video introduction to JITWatch [video](https://www.youtube.com/watch?v=p7ipmAa9_9E)
* Slides from my LJC lightning talk on JITWatch  [slides](https://chriswhocodes.com/LJC2022.pdf)

<h3>For instructions and screenshots see the wiki</h3>
<h3>https://github.com/chriswhocodes/jitwatch/wiki</h3>

The JITWatch user interface is built using JavaFX which is downloaded as a maven dependency for JDK11+.

For pre-JDK11 you will need to use a Java runtime that includes JavaFX.

<h2>maven</h2>
<pre>mvn clean package && java -jar ui/target/jitwatch-ui-shaded.jar</pre>

<h2>Build an example HotSpot log</h2>
<pre># Build the code and then run
cd scripts && ./makeDemoLogFile.sh</pre>

AdoptOpenJDK
------------
Ownership of the JITWatch repository was transferred to the AdoptOpenJDK organisation in 2013. This organisation was closely linked with the London Java Community and incubated several non-JDK-build projects. AdoptOpenJDK became part of the Eclipse Foundation and now release JDK builds under the new name of Eclipse Temurin. It was agreed with them that this was no longer the right home for JITWatch so on 5th September 2026 ownership of the JITWatch project was transferred back to my chriswhocodes GitHub account.

JITWatch has always been and will remain a free and open source tool for the benefit of the Java community.
