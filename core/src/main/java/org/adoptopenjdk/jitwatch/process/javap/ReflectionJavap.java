/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.process.javap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.loader.DisposableURLClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReflectionJavap
{
	private static final Logger logger = LoggerFactory.getLogger(ReflectionJavap.class);

	private static final String JAVAPTASK_CLASS = "com.sun.tools.javap.JavapTask";
	
	private static final int BUFFER_SIZE = 64 * 1024;
	
	private static boolean hasCheckedForToolsJar = false;
	
	private static boolean canUseReflectionJavap = false;
	
	private static Class<?> classJavapTask;
	
	private static URL locateToolsJar()
	{
		Path javaHome = Paths.get(System.getProperty("java.home"));
		
		Path toolsJarPath = Paths.get(javaHome.toString(), "..", "lib", "tools.jar").normalize();

		URL result = null;
				
		if (toolsJarPath.toFile().exists())
		{
			try
			{
				result = toolsJarPath.toFile().toURI().toURL();
			}
			catch (MalformedURLException e)
			{
				e.printStackTrace();
			}
		}
		
		return result;
	}

	public static boolean canUseReflectionJavap()
	{
		if (!canUseReflectionJavap)
		{
			boolean available = false;
						
			try
			{
				classJavapTask = Class.forName(JAVAPTASK_CLASS);
			}
			catch (ClassNotFoundException cnfe)
			{
				URL toolsJarURL = locateToolsJar();
				
				if (toolsJarURL != null)
				{
					List<URL> urls = new ArrayList<>();
					
					urls.add(toolsJarURL);
					
					DisposableURLClassLoader disposableClassLoader = new DisposableURLClassLoader(urls);
					
					try
					{
						classJavapTask = Class.forName(JAVAPTASK_CLASS, false, disposableClassLoader);
					}
					catch (ClassNotFoundException cnfe2)
					{
						cnfe2.printStackTrace();
					}
				}
			}
			
			if (classJavapTask != null)
			{
				try
				{
					classJavapTask.getMethod("setLog", new Class[] { OutputStream.class });
					classJavapTask.getMethod("handleOptions", new Class[] { String[].class });
					classJavapTask.getMethod("call", new Class[] {});
		
					available = true;
				}
				catch (NoSuchMethodException | SecurityException e)
				{
					e.printStackTrace();
				}
			}
			
			canUseReflectionJavap = available;
		}

		return canUseReflectionJavap;
	}

	public static String getBytecode(List<String> classLocations, String fqClassName)
	{
		String[] args = buildClassPathFromClassLocations(classLocations, fqClassName);

		String byteCodeString = createJavapTaskFromArguments(fqClassName, args);

		return byteCodeString;
	}

	private static String createJavapTaskFromArguments(String fqClassName, String[] args)
	{
		String byteCodeString = null;
		
		if (classJavapTask != null)
		{
			try
			{
				Constructor<?> constructor = classJavapTask.getDeclaredConstructor();
				Object javapObject = constructor.newInstance();
	
				Method methodSetLog = classJavapTask.getMethod("setLog", new Class[] { OutputStream.class });
				Method methodHandleOptions = classJavapTask.getMethod("handleOptions", new Class[] { String[].class });
				Method methodCall = classJavapTask.getMethod("call", new Class[] {});
	
				try (ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFFER_SIZE))
				{
					methodSetLog.invoke(javapObject, baos);
					methodHandleOptions.invoke(javapObject, new Object[] {args});
					methodCall.invoke(javapObject);
	
					byteCodeString = baos.toString();
				}
			}
			catch (Exception e)
			{
				logger.error("Could not load bytecode via reflection", e);
			}
		}
		return byteCodeString;
	}

	private static String[] buildClassPathFromClassLocations(List<String> classLocations, String fqClassName)
	{
		String[] args;

		if (classLocations == null || classLocations.isEmpty())
		{
			args = new String[] { "-c", "-p", "-v", fqClassName };
		}
		else
		{
			StringBuilder classPathBuilder = new StringBuilder();

			for (String cp : classLocations)
			{
				classPathBuilder.append(cp).append(File.pathSeparatorChar);
			}

			classPathBuilder.deleteCharAt(classPathBuilder.length() - 1);

			args = new String[] { "-c", "-p", "-v", "-classpath", classPathBuilder.toString(), fqClassName };
		}

		return args;
	}
}
