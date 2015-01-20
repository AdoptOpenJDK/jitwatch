@ECHO OFF

set CLASSPATH=lib\logback-classic-1.1.2.jar
set CLASSPATH=%CLASSPATH%;lib\logback-core-1.1.2.jar
set CLASSPATH=%CLASSPATH%;lib\slf4j-api-1.7.7.jar
set CLASSPATH=%CLASSPATH%;%JAVA_HOME%\lib\tools.jar
set CLASSPATH=%CLASSPATH%;target\jitwatch-1.0.0-SNAPSHOT.jar

"%JAVA_HOME%\bin\java" -cp "%CLASSPATH%" org.adoptopenjdk.jitwatch.launch.LaunchHeadless %*
