/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/blob/master/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
package com.chrisnewland.jitwatch.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.chrisnewland.jitwatch.loader.BytecodeLoader;

public class MetaClass implements Comparable<MetaClass>
{
	private String className;
	private MetaPackage classPackage;

	private boolean isInterface = false;
	private boolean missingDef = false;

	private List<MetaMethod> classMethods = new CopyOnWriteArrayList<MetaMethod>();
	private List<MetaConstructor> classConstructors = new CopyOnWriteArrayList<MetaConstructor>();
	
	private int compiledMethodCount = 0;
	
	private Map<String, String> bytecodeCache = null;

	public MetaClass(MetaPackage classPackage, String className)
	{
		this.classPackage = classPackage;
		this.className = className;		
	}

	public boolean isInterface()
	{
		return isInterface;
	}

	public void incCompiledMethodCount()
	{
		compiledMethodCount++;
	}
	
	public boolean hasCompiledMethods()
	{
	    return compiledMethodCount > 0;
	}
	
	public void setInterface(boolean isInterface)
	{
		this.isInterface = isInterface;
	}
	
	public boolean isMissingDef()
	{
		return missingDef;
	}

	public void setMissingDef(boolean missingDef)
	{
		this.missingDef = missingDef;
	}
	
	public Map<String, String> getBytecodeCache(List<String> classLocations)
	{
		if (bytecodeCache == null)
		{
			bytecodeCache = BytecodeLoader.fetchByteCodeForClass(classLocations, getFullyQualifiedName());
		}
		
		return bytecodeCache;
	}

	public String toString2()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(classPackage.getName()).append(".").append(className).append(" ").append(compiledMethodCount)
				.append("/").append(classMethods.size());

		return builder.toString();
	}

	@Override
	public String toString()
	{
		return getName();
	}

	public String getName()
	{
		return className;
	}

	public String getFullyQualifiedName()
	{
		return classPackage.getName() + '.' + className;
	}

	public MetaPackage getPackage()
	{
		return classPackage;
	}

	public void addMetaMethod(MetaMethod method)
	{
		classMethods.add(method);
	}
	
	public void addMetaConstructor(MetaConstructor constructor)
	{
		classConstructors.add(constructor);
	}

	//alpha
	public List<IMetaMember> getMetaMembers()
	{
		List<IMetaMember> result = new ArrayList<>();
		
		IMetaMember[] constructorsArray = classConstructors.toArray(new MetaConstructor[classConstructors.size()]);
		Arrays.sort(constructorsArray);
		
		IMetaMember[] methodsArray = classMethods.toArray(new MetaMethod[classMethods.size()]);
		Arrays.sort(methodsArray);

		result.addAll(Arrays.asList(constructorsArray));
		result.addAll(Arrays.asList(methodsArray));
		
		return result;
	}

	@Override
	public int compareTo(MetaClass other)
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
