/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.util;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;
import org.adoptopenjdk.jitwatch.loader.DisposableURLClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ClassUtil
{
	private static DisposableURLClassLoader disposableClassLoader = new DisposableURLClassLoader(new ArrayList<URL>());

	private static final Logger logger = LoggerFactory.getLogger(ClassUtil.class);

	private ClassUtil()
	{
	}

	public static void initialise(final List<URL> urls)
	{
		if (DEBUG_LOGGING_CLASSPATH)
		{
			for (URL url : urls)
			{
				logger.debug("Adding classpath to DisposableURLClassLoader {}", url);
			}
		}

		disposableClassLoader = new DisposableURLClassLoader(urls);
	}

	public static Class<?> loadClassWithoutInitialising(String fqClassName) throws ClassNotFoundException
	{
		if (DEBUG_LOGGING_CLASSPATH)
		{
			logger.debug("loadClassWithoutInitialising '{}'", fqClassName);
		}

		return Class.forName(fqClassName, false, disposableClassLoader);
	}

	public static Class<?> loadClassWithoutInitialising(String fqClassName, ClassLoader classLoader) throws ClassNotFoundException
	{
		return Class.forName(fqClassName, false, classLoader);
	}

	public static List<String> getCurrentClasspathElements()
	{
		String classPath = System.getProperty("java.class.path");

		String[] parts = classPath.split(File.pathSeparator);

		return Arrays.asList(parts);
	}

	public static void clear()
	{
		if (disposableClassLoader != null)
		{
			try
			{
				disposableClassLoader.close();
			}
			catch (IOException e)
			{
				logger.warn("Could not close the DisposableURLClassLoader", e);
			}
		}
		disposableClassLoader = null;
	}
}