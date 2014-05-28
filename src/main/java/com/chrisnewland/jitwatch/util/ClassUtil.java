/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chrisnewland.jitwatch.loader.DisposableURLClassLoader;

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