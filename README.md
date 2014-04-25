This project has moved to <a href="https://github.com/AdoptOpenJDK/jitwatch">AdoptOpenJDK/jitwatch</a> to benefit the wider Java community.

All further development will take place on the AdoptOpenJDK fork.

JITWatch
========

Log analyser and visualiser for the HotSpot JIT compiler.

[![Build Status](https://adopt-openjdk.ci.cloudbees.com/buildStatus/icon?job=jitwatch)](https://adopt-openjdk.ci.cloudbees.com/job/jitwatch/)

```JDK 1.7```  [![Build Status](https://adopt-openjdk.ci.cloudbees.com/buildStatus/icon?job=jitwatch)](https://adopt-openjdk.ci.cloudbees.com/job/jitwatch/)

```OpenJDK 8```  [![Build Status](https://adopt-openjdk.ci.cloudbees.com/buildStatus/icon?job=jitwatch/jdk=OpenJDK8)](https://adopt-openjdk.ci.cloudbees.com/job/jitwatch/jdk=OpenJDK8/)

[![Built on CloudBees](http://www.cloudbees.com/sites/default/files/Button-Built-on-CB-1.png)](https://adopt-openjdk.ci.cloudbees.com/job/jitwatch/)


<h3>For instructions and screenshots see the wiki</h3>
<h3>https://github.com/AdoptOpenJDK/jitwatch/wiki</h3>

<h2>ant</h2>
<pre>ant clean compile test run</pre>

<h2>maven</h2>
<pre># Java 7
mvn clean compile test exec:java</pre>
<pre># Java 8
mvn -f pom-java8.xml clean compile test exec:java</pre>

<h2>Build an example HotSpot log</h2>
<pre># Build the code first with ant / maven / IDE
./makeDemoLogFile.sh</pre>


Latest binaries from Jenkins
----------------------------
JDK 1.7 binary: https://adopt-openjdk.ci.cloudbees.com/job/jitwatch/jdk=JDK_1.7/ws/jitwatch-1.0.0-SNAPSHOT-JDK_1.7.tar.gz

OpenJDK 8 binary: https://adopt-openjdk.ci.cloudbees.com/job/jitwatch/jdk=OpenJDK8/ws/jitwatch-1.0.0-SNAPSHOT-OpenJDK8.tar.gz


Java 8 Compatibility
--------------------
<b>[Find out how you can also use this logo with your F/OSS projects](https://java.net/projects/adoptopenjdk/pages/TestingJava8)</b>

![Compatibility Badge](https://java.net/downloads/adoptopenjdk/compat.svg)
