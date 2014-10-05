/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.adoptopenjdk.jitwatch.core.IJITListener;
import org.adoptopenjdk.jitwatch.core.ILogParseErrorListener;
import org.adoptopenjdk.jitwatch.model.JITEvent;
import org.adoptopenjdk.jitwatch.util.ClassUtil;

public class UnitTestUtil
{
	public static Method getMethod(String fqClassName, String method, Class<?>[] paramTypes)
	{
		Method m = null;

		try
		{
			Class<?> clazz = ClassUtil.loadClassWithoutInitialising(fqClassName);
			m = clazz.getDeclaredMethod(method, paramTypes);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return m;
	}

	public static Constructor<?> getConstructor(String fqClassName, Class<?>[] paramTypes)
	{
		Constructor<?> c = null;

		try
		{
			Class<?> clazz = ClassUtil.loadClassWithoutInitialising(fqClassName);
			c = clazz.getDeclaredConstructor(paramTypes);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return c;
	}

	public static IJITListener getNoOpJITListener()
	{
		return new IJITListener()
		{
			@Override
			public void handleLogEntry(String entry)
			{
			}

			@Override
			public void handleErrorEntry(String entry)
			{
			}

			@Override
			public void handleReadStart()
			{
			}

			@Override
			public void handleReadComplete()
			{
			}

			@Override
			public void handleJITEvent(JITEvent event)
			{
			}
		};
	}

	public static ILogParseErrorListener getNoOpParseErrorListener()
	{
		return new ILogParseErrorListener()
		{

			@Override
			public void handleError(String title, String body)
			{
			}
		};
	}
}
