/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
package com.chrisnewland.jitwatch.model;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PackageManager
{
	// class name -> MetaClass
	private Map<String, MetaClass> metaClasses;

	// package name -> MetaPackage
	private Map<String, MetaPackage> metaPackages;

	private List<MetaPackage> roots;

	public PackageManager()
	{
		clear();
	}

	public void clear()
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
		return metaClasses.get(className);
	}

	public MetaPackage getMetaPackage(String packageName)
	{
		return metaPackages.get(packageName);
	}

	public MetaPackage buildPackage(String packageName)
	{
		String[] parts = packageName.split("\\.");

		StringBuilder builder = new StringBuilder();

		MetaPackage mp = null;
		MetaPackage parent = null;

		int depth = 0;

		for (String part : parts)
		{
			if (builder.length() > 0)
			{
				builder.append('.');
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

				metaPackages.put(nameBuild, mp);
			}

			parent = mp;

			depth++;
		}

		if (mp == null)
		{
			// default package ""
			mp = new MetaPackage("");
			metaPackages.put("", mp);
		}

		return mp;
	}

	public List<MetaPackage> getRootPackages()
	{
		return roots;
	}
}
