/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

public final class ClassUtil
{
    private static final Logger logger = LoggerFactory.getLogger(ClassUtil.class);

    /*
        Hide Utility Class Constructor
        Utility classes should not have a public or default constructor.
    */
    private ClassUtil() {
    }

	public static Class<?> loadClassWithoutInitialising(String fqClassName) throws ClassNotFoundException
	{
	    return Class.forName(fqClassName, false, getClassLoader());
	}
	
	public static void addURIToClasspath(URI uri)
	{
		try
		{
			URLClassLoader urlClassLoader = getClassLoader();
			URL url = uri.toURL();
			Class<?> urlClass = URLClassLoader.class;
			Method method = urlClass.getDeclaredMethod("addURL", new Class<?>[] { URL.class });
			method.setAccessible(true);
			method.invoke(urlClassLoader, new Object[] { url });
		}
		catch (Exception ex)
		{
            logger.error("Exception: {}", ex.getMessage(), ex);
		}
	}
	
	public static URL[] getClassLoaderURLs()
	{
		try
		{
			URLClassLoader urlClassLoader = getClassLoader();
			Class<?> urlClass = URLClassLoader.class;
			Method method = urlClass.getDeclaredMethod("getURLs", new Class<?>[] {});
			method.setAccessible(true);
			Object result = method.invoke(urlClassLoader, new Object[] {});
			
			return (URL[])result;
		}
		catch (Exception ex)
		{
            logger.error("Exception: {}", ex.getMessage(), ex);
		}
		
		return null;
	}
	
	private static URLClassLoader getClassLoader()
	{
	    return (URLClassLoader) ClassUtil.class.getClassLoader();
	}
}
