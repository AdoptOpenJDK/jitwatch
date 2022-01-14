@REM ---------------------------------------------------------------
@REM JITWatch
@REM ---------------------------------------------------------------

@REM Static analysis of bytecode
@ECHO OFF

@REM ---------------------------------------------------------------

set CLASSPATH=..\ui\target\jitwatch-ui-1.4.4-shaded-win.jar

"%JAVA_HOME%\bin\java" -classpath "%CLASSPATH%" org.adoptopenjdk.jitwatch.jarscan.JarScan %*
@REM ---------------------------------------------------------------

