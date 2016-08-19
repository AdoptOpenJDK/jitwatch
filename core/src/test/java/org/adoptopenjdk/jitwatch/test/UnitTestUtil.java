/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_ADDRESS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILE_ID;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TASK_QUEUED;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_NMETHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TASK;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TASK_DONE;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.core.IJITListener;
import org.adoptopenjdk.jitwatch.core.ILogParseErrorListener;
import org.adoptopenjdk.jitwatch.core.TagProcessor;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.JITDataModel;
import org.adoptopenjdk.jitwatch.model.JITEvent;
import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.model.MetaPackage;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.model.Task;
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
	
	public static IMetaMember setUpTestMember(JITDataModel model, String fqClassName, String memberName, Class<?> returnType, Class<?>[] params, String nmethodAddress)
			throws ClassNotFoundException
	{
		MetaClass metaClass = model.getPackageManager().getMetaClass(fqClassName);

		if (metaClass == null)
		{
			metaClass = UnitTestUtil.createMetaClassFor(model, fqClassName);
		}
		
		List<String> paramList = new ArrayList<>();
		
		for (Class<?> clazz : params)
		{
			paramList.add(clazz.getName());
		}

		MemberSignatureParts msp = MemberSignatureParts.fromParts(fqClassName, memberName, returnType.getName(), paramList);
		
		IMetaMember createdMember = metaClass.getMemberForSignature(msp);
		
		Tag tagTaskQueued = new Tag(TAG_TASK_QUEUED, ATTR_COMPILE_ID+"='1'", true);
		createdMember.setTagTaskQueued(tagTaskQueued);

		Tag tagNMethod = new Tag(TAG_NMETHOD, ATTR_COMPILE_ID+"='1' "+ATTR_ADDRESS+"='"+nmethodAddress+"'", true);
		createdMember.setTagNMethod(tagNMethod);
		
		return createdMember;
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
	
	public static void processLogLines(IMetaMember member, String[] logLines)
	{
		TagProcessor tp = new TagProcessor();

		Tag tag = null;

		for (String line : logLines)
		{
			line = line.trim();

			line = StringUtil.replaceXMLEntities(line);

			tag = tp.processLine(line);

			if (tag != null)
			{
				switch (tag.getName())
				{

				case TAG_TASK_QUEUED:
					member.setTagTaskQueued(tag);
					break;

				case TAG_NMETHOD:
					member.setTagNMethod(tag);
					break;

				case TAG_TASK:
					member.setTagTask((Task) tag);
					break;

				case TAG_TASK_DONE:
					member.setTagNMethod(tag);
					break;
				}
			}
		}
	}
}
