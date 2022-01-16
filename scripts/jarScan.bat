@REM ---------------------------------------------------------------
@REM JITWatch
@REM ---------------------------------------------------------------

@REM Static analysis of bytecode
@ECHO OFF

@REM ---------------------------------------------------------------

set CLASSPATH=..\ui\target\jitwatch-ui-shaded.jar

"%JAVA_HOME%\bin\java" -classpath "%CLASSPATH%" org.adoptopenjdk.jitwatch.jarscan.JarScan %*
@REM ---------------------------------------------------------------

