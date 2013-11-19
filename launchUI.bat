@REM ---------------------------------------------------------------
@REM JITWatch
@REM ---------------------------------------------------------------

@REM Startup script for JITWatch on Windows
@ECHO OFF

@REM ---------------------------------------------------------------
     @set CP="%JAVA_HOME%\lib\tools.jar;%JAVA_HOME%\jre\lib\jfxrt.jar;target\jitwatch-1.0.0-SNAPSHOT.jar" 
     java -classpath "%JAVA_HOME%\lib\tools.jar;%JAVA_HOME%\jre\lib\jfxrt.jar;target\jitwatch-1.0.0-SNAPSHOT.jar" com.chrisnewland.jitwatch.launch.LaunchUI
@REM ---------------------------------------------------------------


