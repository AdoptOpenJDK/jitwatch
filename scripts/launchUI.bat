@REM ---------------------------------------------------------------
@REM JITWatch
@REM ---------------------------------------------------------------

@REM Startup script for JITWatch on Windows
@ECHO OFF

@REM ---------------------------------------------------------------

set CLASSPATH=..\ui\target\jitwatch-ui-1.4.4-shaded-win.jar

echo %CLASSPATH%

"%JAVA_HOME%\bin\java" -classpath "%CLASSPATH%" org.adoptopenjdk.jitwatch.launch.LaunchUI
@REM ---------------------------------------------------------------

