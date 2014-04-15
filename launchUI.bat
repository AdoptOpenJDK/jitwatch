@REM ---------------------------------------------------------------
@REM JITWatch
@REM ---------------------------------------------------------------

@REM Startup script for JITWatch on Windows
@ECHO OFF

@REM ---------------------------------------------------------------

@set CLASSPATH="%CLASSPATH%;lib\logback-classic-1.0.1.jar"
@set CLASSPATH="%CLASSPATH%;lib\logback-core-1.0.1.jar"
@set CLASSPATH="%CLASSPATH%;lib\slf4j-api-1.6.4.jar"
@set CLASSPATH="%CLASSPATH%;%JAVA__HOME%\lib\tools.jar"
@set CLASSPATH="%CLASSPATH%;%JAVA__HOME%\jre\lib\jfxrt.jar"
@set CLASSPATH="%CLASSPATH%;target\jitwatch-1.0.0-SNAPSHOT.jar"


     java -classpath "%CLASSPATH%" com.chrisnewland.jitwatch.launch.LaunchUI
@REM ---------------------------------------------------------------

