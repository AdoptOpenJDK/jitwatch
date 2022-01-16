@ECHO OFF

set CLASSPATH=..\ui\target\jitwatch-ui-shaded.jar

"%JAVA_HOME%\bin\java" -cp "%CLASSPATH%" org.adoptopenjdk.jitwatch.launch.LaunchHeadless %*
