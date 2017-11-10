@ECHO OFF

set CLASSPATH=lib\logback-classic-1.1.2.jar
set CLASSPATH=%CLASSPATH%;lib\logback-core-1.1.2.jar
set CLASSPATH=%CLASSPATH%;lib\slf4j-api-1.7.7.jar
set CLASSPATH=%CLASSPATH%;core\target\classes
set CLASSPATH=%CLASSPATH%;ui\target\classes
set CLASSPATH=%CLASSPATH%;core\build\classes\java\main
set CLASSPATH=%CLASSPATH%;ui\build\classes\java\main

"%JAVA_HOME%\bin\java" -cp "%CLASSPATH%" org.adoptopenjdk.jitwatch.launch.LaunchHeadless %*
