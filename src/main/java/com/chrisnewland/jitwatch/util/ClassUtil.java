/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/blob/master/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
package com.chrisnewland.jitwatch.util;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

public class ClassUtil
{
	public static Class<?> loadClassWithoutInitialising(String fqClassName) throws ClassNotFoundException
	{
		try
		{
			return Class.forName(fqClassName, false, getClassLoader());
		}
		catch (Throwable t)
		{
			throw t;
		}
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
		catch (Throwable t)
		{
			t.printStackTrace();
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
		catch (Throwable t)
		{
			t.printStackTrace();
		}
		
		return null;
	}
	
	private static URLClassLoader getClassLoader()
	{
	    return (URLClassLoader) ClassUtil.class.getClassLoader();
	}
}
