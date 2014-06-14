/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.model;

import com.chrisnewland.jitwatch.loader.BytecodeLoader;
import com.chrisnewland.jitwatch.util.ParseUtil;
import com.chrisnewland.jitwatch.model.bytecode.ClassBC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public class MetaClass implements Comparable<MetaClass>
{
	private String className;
	private MetaPackage classPackage;

	private boolean isInterface = false;
	private boolean missingDef = false;

	private List<MetaMethod> classMethods = new CopyOnWriteArrayList<MetaMethod>();
	private List<MetaConstructor> classConstructors = new CopyOnWriteArrayList<MetaConstructor>();

	private int compiledMethodCount = 0;

	private ClassBC classBytecode = null;

	//private static final Logger logger = LoggerFactory.getLogger(MetaClass.class);

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

	public ClassBC getClassBytecode(List<String> classLocations)
	{
		if (classBytecode == null)
		{
			classBytecode = BytecodeLoader.fetchBytecodeForClass(classLocations, getFullyQualifiedName());
		}

		return classBytecode;
	}

	public String toString2()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(classPackage.getName()).append(S_DOT).append(className).append(C_SPACE).append(compiledMethodCount)
				.append(S_SLASH).append(classMethods.size());

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
		StringBuilder builder = new StringBuilder();

		if (classPackage != null && classPackage.getName().length() > 0)
		{
			builder.append(classPackage.getName()).append(C_DOT);
		}

		builder.append(className);

		return builder.toString();
	}

	public String getAbbreviatedFullyQualifiedName()
	{
		StringBuilder builder = new StringBuilder();

		if (classPackage != null && classPackage.getName().length() > 0)
		{
			String[] parts = classPackage.getName().split("\\.");

			for (String part : parts)
			{
				builder.append(part.charAt(0)).append(C_DOT);
			}
		}

		builder.append(className);

		return builder.toString();
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

	public IMetaMember findMemberByBytecodeSignature(String bytecodeSignature)
	{
		IMetaMember result = null;

		if (bytecodeSignature != null)
		{
			for (IMetaMember mm : getMetaMembers())
			{
				if (mm.matchesBytecodeSignature(bytecodeSignature))
				{
					result = mm;
					break;
				}
			}
		}

		return result;
	}

	public IMetaMember getMemberFromSignature(String inName, String inReturnType, String[] paramTypes)
	{
		String returnType = inReturnType;
		String name = inName;
		IMetaMember result = null;

		if (ParseUtil.CONSTRUCTOR_INIT.equals(name))
		{
			name = getFullyQualifiedName();
			returnType = name;
		}

		for (IMetaMember member : getMetaMembers())
		{
			if (memberMatches(member, name, returnType, paramTypes))
			{
				result = member;
				break;
			}
		}

		return result;
	}

	private boolean memberMatches(IMetaMember member, String name, String returnType, String[] paramTypes)
	{
		boolean match = false;

		boolean nameMatch = member.getMemberName().equals(name);

		if (nameMatch)
		{
			boolean returnMatch = false;
			boolean paramsMatch = false;

			String memberReturnTypeName = member.getReturnTypeName();
			String[] memberArgumentTypeNames = member.getParamTypeNames();

			if (memberReturnTypeName == null && returnType == null)
			{
				returnMatch = true;
			}
			else if (memberReturnTypeName != null && returnType != null && memberReturnTypeName.equals(returnType))
			{
				returnMatch = true;
			}

			if (memberArgumentTypeNames != null && paramTypes != null && memberArgumentTypeNames.length == paramTypes.length)
			{
				paramsMatch = true;

				for (int i = 0; i < memberArgumentTypeNames.length; i++)
				{
					String memberParam = memberArgumentTypeNames[i];
					String checkParam = paramTypes[i];

					if (!memberParam.equals(checkParam))
					{
						paramsMatch = false;
						break;
					}
				}
			}

			match = returnMatch && paramsMatch;
		}

		return match;
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
