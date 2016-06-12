/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.adoptopenjdk.jitwatch.core.IJITListener;
import org.adoptopenjdk.jitwatch.core.ILogParseErrorListener;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.JITDataModel;
import org.adoptopenjdk.jitwatch.model.JITEvent;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.model.MetaPackage;
import org.adoptopenjdk.jitwatch.util.ClassUtil;
import org.adoptopenjdk.jitwatch.util.StringUtil;

public class UnitTestUtil
{
	public static MetaClass createMetaClassFor(JITDataModel model, String fqClassName) throws ClassNotFoundException
	{
		Class<?> clazz = Class.forName(fqClassName);

		return model.buildAndGetMetaClass(clazz);
	}

	public static HelperMetaMethod createTestMetaMember(String fqClassName, String methodName, Class<?>[] params,
			Class<?> returnType)
	{
		String packageName = StringUtil.getPackageName(fqClassName);
		String className = StringUtil.getUnqualifiedClassName(fqClassName);

		MetaPackage metaPackage = new MetaPackage(packageName);

		MetaClass metaClass = new MetaClass(metaPackage, className);

		HelperMetaMethod helper = null;

		try
		{
			helper = new HelperMetaMethod(methodName, metaClass, params, returnType);
		}
		catch (NoSuchMethodException | SecurityException e)
		{
			e.printStackTrace();
		}

		return helper;
	}

	public static IMetaMember createTestMetaMember()
	{
		return createTestMetaMember("java.lang.String", "length", new Class<?>[0], void.class);
	}

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
