@REM ---------------------------------------------------------------
@REM JITWatch
@REM ---------------------------------------------------------------

@REM Static analysis of bytecode
@ECHO OFF

@REM ---------------------------------------------------------------

set CLASSPATH=lib\logback-classic-1.1.2.jar
set CLASSPATH=%CLASSPATH%;lib\logback-core-1.1.2.jar
set CLASSPATH=%CLASSPATH%;lib\slf4j-api-1.7.7.jar
set CLASSPATH=%CLASSPATH%;%JAVA_HOME%\jre\lib\jfxrt.jar
set CLASSPATH=%CLASSPATH%;core\target\classes
set CLASSPATH=%CLASSPATH%;ui\target\classes
set CLASSPATH=%CLASSPATH%;core\build\classes\java\main
set CLASSPATH=%CLASSPATH%;ui\build\classes\java\main

"%JAVA_HOME%\bin\java" -classpath "%CLASSPATH%" org.adoptopenjdk.jitwatch.jarscan.JarScan %*
@REM ---------------------------------------------------------------

