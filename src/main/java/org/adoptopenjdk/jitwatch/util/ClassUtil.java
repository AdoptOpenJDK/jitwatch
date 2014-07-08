/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.util;

import java.net.URL;

import org.adoptopenjdk.jitwatch.loader.DisposableURLClassLoader;

public final class ClassUtil
{
	private static DisposableURLClassLoader disposableClassLoader = new DisposableURLClassLoader(new URL[0]);
    
    private ClassUtil()
    {
    }
    
    public static void initialise(URL[] urls)
    {
    	disposableClassLoader = new DisposableURLClassLoader(urls);
    }

	public static Class<?> loadClassWithoutInitialising(String fqClassName) throws ClassNotFoundException
	{
		try
		{
			return Class.forName(fqClassName, false, disposableClassLoader);
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}
}