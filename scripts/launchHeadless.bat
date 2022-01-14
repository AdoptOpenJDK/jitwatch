@ECHO OFF

set CLASSPATH=..\ui\target\jitwatch-ui-1.4.4-shaded-win.jar

"%JAVA_HOME%\bin\java" -cp "%CLASSPATH%" org.adoptopenjdk.jitwatch.launch.LaunchHeadless %*
