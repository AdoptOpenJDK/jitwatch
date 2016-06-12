@REM ---------------------------------------------------------------
@REM JITWatch
@REM ---------------------------------------------------------------

@REM Startup script for JITWatch on Windows
@ECHO OFF

@REM ---------------------------------------------------------------

set CLASSPATH=lib\logback-classic-1.1.2.jar
set CLASSPATH=%CLASSPATH%;lib\logback-core-1.1.2.jar
set CLASSPATH=%CLASSPATH%;lib\slf4j-api-1.7.7.jar
set CLASSPATH=%CLASSPATH%;%JAVA_HOME%\lib\tools.jar
set CLASSPATH=%CLASSPATH%;%JAVA_HOME%\jre\lib\jfxrt.jar
set CLASSPATH=%CLASSPATH%;core\target\classes
set CLASSPATH=%CLASSPATH%;ui\target\classes

echo %CLASSPATH%

"%JAVA_HOME%\bin\java" -Djava.library.path=%JAVA_HOME%\lib\amd64 -classpath "%CLASSPATH%" org.adoptopenjdk.jitwatch.jarscan.visualiser.HistoPlotter %1
@REM ---------------------------------------------------------------

