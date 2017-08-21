/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.process.javap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReflectionJavap
{
	private static final Logger logger = LoggerFactory.getLogger(ReflectionJavap.class);

	private static final String JAVAP_CLASS = "com.sun.tools.javap.JavapTask";
	
	private static final int BUFFER_SIZE = 64 * 1024;
		
	public static boolean canUseReflectionJavap()
	{
		boolean available = false;

		try
		{
			Class<?> javapClass = Class.forName(JAVAP_CLASS);

			javapClass.getMethod("setLog", new Class[] { OutputStream.class });
			javapClass.getMethod("handleOptions", new Class[] { String[].class });
			javapClass.getMethod("call", new Class[] {});

			available = true;
		}
		catch (ClassNotFoundException | NoSuchMethodException | SecurityException e)
		{
		}

		return available;
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

		try
		{
			Class<?> javapClass = Class.forName(JAVAP_CLASS);

			Object javapObject = javapClass.newInstance();

			Method methodSetLog = javapClass.getMethod("setLog", new Class[] { OutputStream.class });
			Method methodHandleOptions = javapClass.getMethod("handleOptions", new Class[] { String[].class });
			Method methodCall = javapClass.getMethod("call", new Class[] {});

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

		return byteCodeString;
	}

	private static String[] buildClassPathFromClassLocations(List<String> classLocations, String fqClassName)
	{
		String[] args;

		if (classLocations == null || classLocations.size() == 0)
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
