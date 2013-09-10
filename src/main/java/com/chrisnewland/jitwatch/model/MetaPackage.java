package com.chrisnewland.jitwatch.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MetaPackage implements Comparable<MetaPackage>
{
	private String packageName;

	private List<MetaPackage> childPackages = new CopyOnWriteArrayList<>();

	private List<MetaClass> packageClasses = new CopyOnWriteArrayList<>();

	public MetaPackage(String packageName)
	{
		this.packageName = packageName;
	}

	public List<MetaPackage> getChildPackages()
	{
		MetaPackage[] asArray = childPackages.toArray(new MetaPackage[childPackages.size()]);
		Arrays.sort(asArray);
		return new ArrayList<>(Arrays.asList(asArray));
	}

	public void addChildPackage(MetaPackage child)
	{
		childPackages.add(child);
	}

	public MetaPackage getChildPackage(String name)
	{
		for (MetaPackage mp : childPackages)
		{
			if (mp.getName().equals(name))
			{
				return mp;
			}
		}

		return null;
	}

	public List<MetaClass> getPackageClasses()
	{
		MetaClass[] asArray = packageClasses.toArray(new MetaClass[packageClasses.size()]);
		Arrays.sort(asArray);
		return new ArrayList<>(Arrays.asList(asArray));
	}

	public void addClass(MetaClass metaClass)
	{
		packageClasses.add(metaClass);
	}

	public String getName()
	{
		return packageName;
	}

	@Override
	public String toString()
	{
		return getName();
	}

	@Override
	public int compareTo(MetaPackage other)
	{
		return this.getName().compareTo(other.getName());
	}

	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		else
		{
			return toString().equals(obj.toString());
		}
	}
}
