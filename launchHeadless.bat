#!\bin\sh

CLASSPATH=%CLASSPATH%;lib\logback-classic-1.1.2.jar
CLASSPATH=%CLASSPATH%;lib\logback-core-1.1.2.jar
CLASSPATH=%CLASSPATH%;lib\slf4j-api-1.7.7.jar
CLASSPATH=%CLASSPATH%;%JAVA_HOME%\lib\tools.jar
CLASSPATH=%CLASSPATH%;target\jitwatch-1.0.0-SNAPSHOT.jar

"%JAVA_HOME%\bin\java" -cp %CLASSPATH% org.adoptopenjdk.jitwatch.launch.LaunchHeadless %1 %2
