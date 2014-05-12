/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.model;

import com.chrisnewland.jitwatch.model.assembly.AssemblyMethod;
import com.chrisnewland.jitwatch.model.bytecode.ClassBC;
import com.chrisnewland.jitwatch.model.bytecode.Instruction;
import com.chrisnewland.jitwatch.util.ParseUtil;
import com.chrisnewland.jitwatch.util.StringUtil;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public abstract class AbstractMetaMember implements IMetaMember, Comparable<IMetaMember>
{
	protected MetaClass methodClass;
    private AssemblyMethod asmMethod = null;

    private boolean isQueued = false;
    private boolean isCompiled = false;

    private Journal journal = new Journal();

    private Map<String, String> queuedAttributes = new ConcurrentHashMap<>();
    private Map<String, String> compiledAttributes = new ConcurrentHashMap<>();

    protected int modifier; // bitset
    protected String memberName;
    protected Class<?> returnType;
    protected Class<?>[] paramTypes;

	private static final String anyChars = "(.*)";
	private static final String spaceZeroOrMore = "( )*";
	private static final String spaceOneOrMore = "( )+";
	private static final String paramName = "([0-9\\p{L}_]+)";
	private static final String regexPackage = "([0-9\\p{L}_\\.]*)";
	
	@Override
	public String getMemberName()
	{
		return memberName;
	}
	
	@Override
	public int getModifier()
	{
		return modifier;
	}

	@Override
	public String getModifierString()
	{
		return Modifier.toString(modifier);
	}
	
	@Override
	public String getReturnTypeName()
	{
		return (returnType == null) ? S_EMPTY : ParseUtil.expandParameterType(returnType.getName());
	}

	@Override
	public String[] getParamTypeNames()
	{
		List<String> typeNames = new ArrayList<>();

		for (Class<?> paramClass : paramTypes)
		{
			typeNames.add(ParseUtil.expandParameterType(paramClass.getName()));
		}
		
		return typeNames.toArray(new String[typeNames.size()]);
	}

	@Override
	public List<String> getQueuedAttributes()
	{
		List<String> attrList = new ArrayList<String>(queuedAttributes.keySet());
		Collections.sort(attrList);

		return attrList;
	}

	@Override
	public MetaClass getMetaClass()
	{
		return methodClass;
	}

	@Override
	public String getQueuedAttribute(String key)
	{
		return queuedAttributes.get(key);
	}

	@Override
	public List<String> getCompiledAttributes()
	{
		List<String> attrList = new ArrayList<String>(compiledAttributes.keySet());
		Collections.sort(attrList);

		return attrList;
	}

	@Override
	public String getCompiledAttribute(String key)
	{
		return compiledAttributes.get(key);
	}

	@Override
	public void addCompiledAttribute(String key, String value)
	{
		compiledAttributes.put(key, value);
	}

	@Override
	public void setQueuedAttributes(Map<String, String> queuedAttributes)
	{
		isQueued = true;
		this.queuedAttributes = queuedAttributes;
	}

	@Override
	public boolean isQueued()
	{
		return isQueued;
	}

	@Override
	public void setCompiledAttributes(Map<String, String> compiledAttributes)
	{
		isCompiled = true;
		isQueued = false;
		this.compiledAttributes = compiledAttributes;

		// inform package tree it contains class with a compiled method
		getMetaClass().getPackage().setHasCompiledClasses();
	}

	@Override
	public void addCompiledAttributes(Map<String, String> additionalAttrs)
	{
		compiledAttributes.putAll(additionalAttrs);
	}

	@Override
	public boolean isCompiled()
	{
		return isCompiled;
	}

	@Override
	public String toStringUnqualifiedMethodName()
	{
		StringBuilder builder = new StringBuilder();
				
		if (modifier != 0)
		{
			builder.append(Modifier.toString(modifier)).append(' ');
		}
		
		if (returnType != null)
		{
			builder.append(expandParam(returnType.getName())).append(' ');
		}

		builder.append(memberName);
		builder.append('(');

		if (paramTypes.length > 0)
		{
			for (Class<?> paramClass : paramTypes)
			{
				builder.append(expandParam(paramClass.getName())).append(',');
			}

			builder.deleteCharAt(builder.length() - 1);
		}

		builder.append(')');

		return builder.toString();
	}

	@Override
	public boolean matches(String input)
	{
		// strip access mode and modifiers
		String nameToMatch = this.toString();

		for (String mod : MODIFIERS)
		{
			nameToMatch = nameToMatch.replace(mod + S_SPACE, S_EMPTY);
		}

		return nameToMatch.equals(input);
	}

	@Override
	public AssemblyMethod getAssembly()
	{
		return asmMethod;
	}

	@Override
	public void setAssembly(AssemblyMethod asmMethod)
	{
		this.asmMethod = asmMethod;
	}

	@Override
	public String getSignatureRegEx()
	{
		StringBuilder builder = new StringBuilder();

		builder.append("^");
		builder.append(anyChars);

		String modifiers = Modifier.toString(modifier);

		if (modifiers.length() > 0)
		{
			builder.append(modifiers).append(' ');
		}

		// return type of constructor is not declared in signature
		if (!(this instanceof MetaConstructor) && returnType != null)
		{
			String rt = expandParamRegEx(returnType.getName());

			builder.append(rt);
			builder.append(' ');
		}

		if (this instanceof MetaConstructor)
		{
			builder.append(regexPackage);
			builder.append(StringUtil.makeUnqualified(memberName));
		}
		else
		{
			builder.append(memberName);
		}
		
		builder.append(spaceZeroOrMore);

		builder.append("\\(");

		if (paramTypes.length > 0)
		{
			for (Class<?> paramClass : paramTypes)
			{
				builder.append(spaceZeroOrMore);

				String paramType = expandParamRegEx(paramClass.getName());

				builder.append(paramType);
				builder.append(spaceOneOrMore);
				builder.append(paramName);
				builder.append(S_COMMA);
			}

			builder.deleteCharAt(builder.length() - 1);
		}

		builder.append(spaceZeroOrMore);
		builder.append("\\)");
		builder.append(anyChars);
		builder.append("$");

		return builder.toString();
	}

	public static String expandParam(String inParamType)
	{
        String paramType = inParamType;
		if (paramType.charAt(0) == '[')
		{
			paramType = ParseUtil.expandParameterType(paramType);
		}

		if (paramType.contains(S_DOT))
		{
			paramType = StringUtil.makeUnqualified(paramType);
		}

		return paramType;
	}

	public static String expandParamRegEx(String inParamType)
	{
        String paramType = inParamType;
		if (paramType.charAt(0) == '[')
		{
			paramType = ParseUtil.expandParameterType(paramType);

			paramType = paramType.replace("[", "\\[").replace("]", "\\]");
		}

		if (paramType.contains(S_DOT))
		{
			paramType = regexPackage + StringUtil.makeUnqualified(paramType);
		}

		return paramType;
	}

	@Override
	public List<String> getTreePath()
	{
		MetaClass metaClass = getMetaClass();
		MetaPackage metaPackage = metaClass.getPackage();

		List<String> path = metaPackage.getPackageComponents();
		path.add(metaClass.getName());

		return path;
	}
	
    @Override
    public int compareTo(IMetaMember other)
    {
        if (other == null)
        {
            return -1;
        }
        else
        {
            return getMemberName().compareTo(other.getMemberName());
        }
    }
    
    public Journal getJournal()
    {
    	return journal;
    }
    
    public void addJournalEntry(Tag entry)
    {        
        journal.addEntry(entry);
    }
    
	public List<Instruction> getBytecodeForMember(List<String> classLocations)
	{
		ClassBC classBytecode = getMetaClass().getClassBytecode(classLocations);

		List<Instruction> result = classBytecode.getMemberBytecode(this);

		return result;
	}
}
