/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.adoptopenjdk.jitwatch.model.assembly.AssemblyMethod;
import org.adoptopenjdk.jitwatch.model.bytecode.ClassBC;
import org.adoptopenjdk.jitwatch.util.ParseUtil;
import org.adoptopenjdk.jitwatch.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

public abstract class AbstractMetaMember implements IMetaMember, Comparable<IMetaMember>
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractMetaMember.class);

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

	@Override
	public String getMemberName()
	{
		return memberName;
	}

	@Override
	public String getFullyQualifiedMemberName()
	{
		return methodClass.getFullyQualifiedName() + C_DOT + memberName;
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
	public boolean signatureMatches(String inMemberName, Class<?> inReturnType, Class<?>[] inParamTypes)
	{
		boolean result = false;

		if (memberName.equals(inMemberName))
		{
			if (this.returnType.getName().equals(inReturnType.getName()))
			{
				if (this.paramTypes.length == inParamTypes.length)
				{
					boolean allMatch = true;

					for (int i = 0; i < this.paramTypes.length; i++)
					{
						Class<?> c1 = this.paramTypes[i];
						Class<?> c2 = inParamTypes[i];

						if (!c1.getName().equals(c2.getName()))
						{
							allMatch = false;
						}
					}

					result = allMatch;
				}
			}
		}

		return result;
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
	public String toStringUnqualifiedMethodName(boolean fqParamTypes)
	{
		StringBuilder builder = new StringBuilder();

		if (modifier != 0)
		{
			builder.append(Modifier.toString(modifier)).append(C_SPACE);
		}

		if (returnType != null)
		{
			builder.append(expandParam(returnType.getName(), fqParamTypes)).append(C_SPACE);
		}

		builder.append(memberName);
		builder.append(C_OPEN_PARENTHESES);

		if (paramTypes.length > 0)
		{
			for (Class<?> paramClass : paramTypes)
			{
				builder.append(expandParam(paramClass.getName(), fqParamTypes)).append(C_COMMA);
			}

			builder.deleteCharAt(builder.length() - 1);
		}

		builder.append(C_CLOSE_PARENTHESES);

		return builder.toString();
	}

	@Override
	public boolean matchesSignature(String input)
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
	public boolean matchesBytecodeSignature(String signature)
	{
		// bytecode signatures have fully qualified object param types
		// public static void main(java.lang.String[])
		// constructor is fully qualified
		// methods are not fully qualified

		boolean match = toString().equals(signature) || toStringUnqualifiedMethodName(true).equals(signature);

		return match;
	}

	@Override
	public AssemblyMethod getAssembly()
	{
		return asmMethod;
	}

	@Override
	public void setAssembly(AssemblyMethod asmMethod)
	{
		if (DEBUG_LOGGING_ASSEMBLY)
		{
			logger.debug("setAssembly on member {}", getFullyQualifiedMemberName());
		}
		
		this.asmMethod = asmMethod;
	}

	@Override
	public String getSignatureRegEx()
	{
		StringBuilder builder = new StringBuilder();

		builder.append(C_HAT);
		builder.append(REGEX_GROUP_ANY);

		String modifiers = Modifier.toString(modifier);

		if (modifiers.length() > 0)
		{
			builder.append(modifiers).append(C_SPACE);
		}

		// return type of constructor is not declared in signature
		if (!(this instanceof MetaConstructor) && returnType != null)
		{
			String rt = expandParamRegEx(returnType.getName());

			builder.append(rt);
			builder.append(C_SPACE);
		}

		if (this instanceof MetaConstructor)
		{
			builder.append(REGEX_UNICODE_PACKAGE_NAME);
			builder.append(StringUtil.makeUnqualified(memberName));
		}
		else
		{
			builder.append(memberName);
		}

		builder.append(REGEX_ZERO_OR_MORE_SPACES);

		builder.append(S_ESCAPED_OPEN_PARENTHESES);

		if (paramTypes.length > 0)
		{
			for (Class<?> paramClass : paramTypes)
			{
				builder.append(REGEX_ZERO_OR_MORE_SPACES);

				String paramType = expandParamRegEx(paramClass.getName());

				builder.append(paramType);
				builder.append(REGEX_ONE_OR_MORE_SPACES);
				builder.append(REGEX_UNICODE_PARAM_NAME);
				builder.append(S_COMMA);
			}

			builder.deleteCharAt(builder.length() - 1);
		}

		builder.append(REGEX_ZERO_OR_MORE_SPACES);
		builder.append(S_ESCAPED_CLOSE_PARENTHESES);
		builder.append(REGEX_GROUP_ANY);
		builder.append(C_DOLLAR);

		return builder.toString();
	}

	public static String expandParam(String inParamType, boolean fullyQualifiedType)
	{
		String paramType = inParamType;

		if (paramType.charAt(0) == C_OPEN_SQUARE_BRACKET)
		{
			paramType = ParseUtil.expandParameterType(paramType);
		}

		if (paramType.contains(S_DOT) && !fullyQualifiedType)
		{
			paramType = StringUtil.makeUnqualified(paramType);
		}

		return paramType;
	}

	public static String expandParamRegEx(String inParamType)
	{
		String paramType = inParamType;
		if (paramType.charAt(0) == C_OPEN_SQUARE_BRACKET)
		{
			paramType = ParseUtil.expandParameterType(paramType);

			paramType = paramType.replace(S_OPEN_SQUARE, S_ESCAPED_OPEN_SQUARE).replace(S_CLOSE_SQUARE, S_ESCAPED_CLOSE_SQUARE);
		}

		if (paramType.contains(S_DOT))
		{
			paramType = REGEX_UNICODE_PACKAGE_NAME + StringUtil.makeUnqualified(paramType);
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

	public ClassBC getClassBytecodeForMember(List<String> classLocations)
	{
		return getMetaClass().getClassBytecode(classLocations);
	}
}
