@REM ---------------------------------------------------------------
@REM JITWatch
@REM ---------------------------------------------------------------

@REM Startup script for JITWatch on Windows
@ECHO OFF

@REM ---------------------------------------------------------------

set CLASSPATH=..\ui\target\jitwatch-ui-shaded.jar

echo %CLASSPATH%

"%JAVA_HOME%\bin\java" -classpath "%CLASSPATH%" org.adoptopenjdk.jitwatch.jarscan.visualiser.HistoPlotter %1
@REM ---------------------------------------------------------------

