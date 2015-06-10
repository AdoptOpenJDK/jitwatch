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
set CLASSPATH=%CLASSPATH%;target\jitwatch-1.0.0-SNAPSHOT.jar

echo %CLASSPATH%

"%JAVA_HOME%\bin\java" -Djava.library.path=%JAVA_HOME%\lib\amd64 -classpath "%CLASSPATH%" org.adoptopenjdk.jitwatch.launch.LaunchUI
@REM ---------------------------------------------------------------

