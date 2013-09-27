/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/LICENSE-BSD
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
			return Class.forName(fqClassName, false, ClassLoader.getSystemClassLoader());
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
			URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
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
}
