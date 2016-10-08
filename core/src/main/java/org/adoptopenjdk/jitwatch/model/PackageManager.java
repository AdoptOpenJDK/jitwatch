/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_DOT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_ESCAPED_DOT;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackageManager
{
	private static final Logger logger = LoggerFactory.getLogger(PackageManager.class);

	// class name -> MetaClass
	private Map<String, MetaClass> metaClasses;

	// package name -> MetaPackage
	private Map<String, MetaPackage> metaPackages;

	private List<MetaPackage> roots;

	public PackageManager()
	{
		clear();
	}

	public final void clear()
	{
		metaClasses = new ConcurrentHashMap<>();
		metaPackages = new ConcurrentHashMap<>();
		roots = new CopyOnWriteArrayList<>();
	}

	public void addMetaClass(MetaClass metaClass)
	{		
		metaClasses.put(metaClass.getFullyQualifiedName(), metaClass);		
	}

	public MetaClass getMetaClass(String className)
	{
		MetaClass result = null;

		if (className != null)
		{
			result = metaClasses.get(className);
		}
		
		return result;
	}

	public MetaPackage getMetaPackage(String packageName)
	{
		MetaPackage result = null;

		if (packageName != null)
		{
			result = metaPackages.get(packageName);
		}

		return result;
	}

	public MetaPackage buildPackage(String packageName)
	{
		if (DEBUG_LOGGING)
		{
			logger.debug("Building package {}", packageName);
		}

		String[] parts = packageName.split(S_ESCAPED_DOT);

		StringBuilder builder = new StringBuilder();

		MetaPackage mp = null;
		MetaPackage parent = null;

		int depth = 0;

		for (String part : parts)
		{
			if (builder.length() > 0)
			{
				builder.append(C_DOT);
			}

			builder.append(part);

			String nameBuild = builder.toString();

			mp = metaPackages.get(nameBuild);

			if (mp == null)
			{
				mp = new MetaPackage(nameBuild);

				if (depth == 0)
				{
					roots.add(mp);
				}
				else
				{
					parent.addChildPackage(mp);
				}

				storePackage(nameBuild, mp);
			}

			parent = mp;

			depth++;
		}

		if (mp == null)
		{
			// default package ""
			mp = new MetaPackage(S_EMPTY);
			storePackage(S_EMPTY, mp);
		}
		
		return mp;
	}
	
	private void storePackage(String name, MetaPackage metaPackage)
	{
		metaPackages.put(name, metaPackage);
	}

	public List<MetaPackage> getRootPackages()
	{
		return roots;
	}
}
