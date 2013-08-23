package com.chrisnewland.jitwatch.meta;

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
		// System.out.println("pm.getMetaClass() " + className);

		return metaClasses.get(className);
	}

	public MetaPackage getMetaPackage(String packageName)
	{
		// System.out.println("pm.getMetaPackage() " + packageName);

		return metaPackages.get(packageName);
	}

	public MetaPackage buildPackage(String packageName)
	{
		// System.out.println("buildPackage: " + packageName);

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

			// System.out.println("Checking package " + nameBuild);

			mp = metaPackages.get(nameBuild);

			if (mp == null)
			{
				// System.out.println("not found: " + nameBuild);
				mp = new MetaPackage(nameBuild);

				if (depth == 0)
				{
					// System.out.println("ROOT: " + mp);
					roots.add(mp);
				}
				else
				{
					parent.addChildPackage(mp);
				}

				metaPackages.put(nameBuild, mp);
				// System.out.println("stored package: " + mp);

			}
			else
			{
				// System.out.println("found");
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

		// System.out.println("Built package " + mp);

		return mp;
	}

	public List<MetaPackage> getRootPackages()
	{
		return roots;
	}
}
