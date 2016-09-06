@ECHO OFF

rem This script makes an example HotSpot log for trying out JITWatch.

rem It executes the Java class org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog
rem which contains methods that exercise various parts of the HotSpot JIT compilers
rem such as inlining, intrinsics, and branch analysis.

rem Make sure you have first built JITWatch using
rem mvn clean compile test

rem Start JITWatch using
rem mvn exec:java

rem When you start JITWatch open up the configuration (Config button) and mount
rem JDK's src.zip (use the JDK src zip button)
rem Java source files for the demo (src/main/java)
rem Class files for the demo (target/classes)

rem Now open the HotSpot log file created by this script and press the Start button :)

rem -------------------------------------------------------
rem Required VM switches
rem -------------------------------------------------------

rem Unlock the HotSpot logging options
set unlock=-XX:+UnlockDiagnosticVMOptions

rem Log each time a class is loaded (how JITWatch builds the class model)
set trace=-XX:+TraceClassLoading

rem Enable XML format HotSpot log output
set compilation=-XX:+LogCompilation

set REQUIRED_SWITCHES=%unlock% %trace% %compilation%

rem ---------------------------------------------------------------------
rem Optional VM switches (add as required to $OPTIONAL_SWITCHES variable
rem ---------------------------------------------------------------------

rem Enable disassembly of native code into assembly language (AT&T / GNU format)
rem Requires the hsdis (HotSpot disassembler) binary to be added to your JRE
rem For hsdis build instructions see http://www.chrisnewland.com/building-hsdis-on-linux-amd64-on-debian-369
set assembly=-XX:+PrintAssembly

rem Change disassembly format from AT&T to Intel assembly
set intel=-XX:PrintAssemblyOptions=intel

rem Disable tiered compilation (enabled by default on Java 8, optional on Java 7)
set notiered=-XX:-TieredCompilation

rem Enable tiered compilation
set tiered=-XX:+TieredCompilation

rem Disable compressed oops (makes assembly easier to read)
set nocompressedoops=-XX:-UseCompressedOops

set OPTIONAL_SWITCHES=%assembly%

"%JAVA_HOME%\bin\java" -version

echo "VM Switches %REQUIRED_SWITCHES% %OPTIONAL_SWITCHES%"

echo "Building example HotSpot log"

set CLASSPATH=core\target\classes
set CLASSPATH=ui\target\classes
set CLASSPATH=%CLASSPATH%;lib\logback-classic-1.1.2.jar
set CLASSPATH=%CLASSPATH%;lib\logback-core-1.1.2.jar
set CLASSPATH=%CLASSPATH%;lib\slf4j-api-1.7.7.jar

"%JAVA_HOME%\bin\java" %REQUIRED_SWITCHES% %OPTIONAL_SWITCHES% -cp %CLASSPATH% org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog
echo "Done"
