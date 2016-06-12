/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_CLOSE_PARENTHESES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_COMMA;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_DOLLAR;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_DOT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_HAT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_OPEN_PARENTHESES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_OPEN_SQUARE_BRACKET;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING_ASSEMBLY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING_SIG_MATCH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.REGEX_GROUP_ANY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.REGEX_ONE_OR_MORE_SPACES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.REGEX_UNICODE_PACKAGE_NAME;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.REGEX_UNICODE_PARAM_NAME;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.REGEX_ZERO_OR_MORE_SPACES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_CLOSE_SQUARE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_COMMA;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_DOT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_ESCAPED_CLOSE_PARENTHESES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_ESCAPED_CLOSE_SQUARE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_ESCAPED_OPEN_PARENTHESES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_ESCAPED_OPEN_SQUARE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_OPEN_SQUARE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_POLYMORPHIC_SIGNATURE;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.adoptopenjdk.jitwatch.model.assembly.AssemblyMethod;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.model.bytecode.ClassBC;
import org.adoptopenjdk.jitwatch.model.bytecode.MemberBytecode;
import org.adoptopenjdk.jitwatch.util.ParseUtil;
import org.adoptopenjdk.jitwatch.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMetaMember implements IMetaMember, Comparable<IMetaMember>
{
	protected static final Logger logger = LoggerFactory.getLogger(AbstractMetaMember.class);

	protected MetaClass metaClass;
	private List<AssemblyMethod> assemblyMethods = new ArrayList<>();

	private boolean isQueued = false;
	private boolean isCompiled = false;

	private Journal journal = new Journal();

	private Map<String, String> queuedAttributes = new ConcurrentHashMap<>();
	private Map<String, String> compiledAttributes = new ConcurrentHashMap<>();

	protected boolean isVarArgs = false;
	protected boolean isPolymorphicSignature = false;
	protected int modifier; // bitset
	private String memberName;
	protected Class<?> returnType;
	protected List<Class<?>> paramTypes;

	public AbstractMetaMember(String memberName)
	{
		this.memberName = memberName;
	}
	
	protected void checkPolymorphicSignature(Method method)
	{
		for (Annotation anno : method.getAnnotations())
		{
			if (S_POLYMORPHIC_SIGNATURE.equals(anno.annotationType().getSimpleName()))
			{
				isPolymorphicSignature = true;
				break;
			}
		}
	}

	@Override
	public String getMemberName()
	{
		return memberName;
	}

	@Override
	public String getFullyQualifiedMemberName()
	{
		return metaClass.getFullyQualifiedName() + C_DOT + memberName;
	}

	@Override
	public String getAbbreviatedFullyQualifiedMemberName()
	{
		return metaClass.getAbbreviatedFullyQualifiedName() + C_DOT + memberName;
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

	private boolean nameMatches(MemberSignatureParts msp)
	{
		if (DEBUG_LOGGING_SIG_MATCH)
		{
			logger.debug("nameMatches this.memberName: '{}' fq: '{}' other '{}' fq: '{}'", memberName, getFullyQualifiedMemberName(), msp.getMemberName(), msp.getFullyQualifiedClassName());
		}
		
		boolean match =  memberName.equals(msp.getMemberName());

		if (DEBUG_LOGGING_SIG_MATCH)
		{
			logger.debug("nameMatches {}", match);
		}
		
		return match;
	}

	private boolean returnTypeMatches(MemberSignatureParts msp) throws ClassNotFoundException
	{
		boolean matched = false;

		String returnTypeClassName = msp.applyGenericSubstitutionsForClassLoading(msp.getReturnType());
		
		if (returnTypeClassName != null)
		{
			Class<?> sigReturnType = ParseUtil.findClassForLogCompilationParameter(returnTypeClassName);
			matched = returnType.equals(sigReturnType);

			if (DEBUG_LOGGING_SIG_MATCH)
			{
				logger.debug("Return: '{}' === '{}' ? {}", returnType.getName(), sigReturnType.getName(), matched);
			}
		}
		else
		{
			matched = (this instanceof MetaConstructor);

			if (DEBUG_LOGGING_SIG_MATCH)
			{
				logger.debug("Constructor found");
			}
		}

		return matched;
	}

	@Override
	public boolean matchesSignature(MemberSignatureParts msp, boolean matchTypesExactly)
	{
		boolean result = false;

		if (nameMatches(msp))
		{
			if (DEBUG_LOGGING_SIG_MATCH)
			{
				logger.debug("Comparing:\n--------------\n{}\n--------------\n{}\n--------------", this, msp);
			}

			if (isPolymorphicSignature)
			{
				// assumption: method overloading not possible
				// with polymorphic signatures so this is a match
				if (DEBUG_LOGGING_SIG_MATCH)
				{
					logger.debug("Member has PolymorphicSignature");
				}

				result = true;
			}
			else
			{
				try
				{
					if (returnTypeMatches(msp))
					{
						List<Class<?>> mspClassTypes = getClassesForParamTypes(msp);

						if (ParseUtil.paramClassesMatch(isVarArgs, this.paramTypes, mspClassTypes, matchTypesExactly))
						{
							result = true;
						}
					}
				}
				catch (ClassNotFoundException cnfe)
				{
					logger.error("Class not found while matching signature:\n{}", msp, cnfe);
				}
			}

			if (DEBUG_LOGGING_SIG_MATCH)
			{
				logger.debug("Match: {}", result);
			}
		}

		return result;
	}

	private List<Class<?>> getClassesForParamTypes(MemberSignatureParts msp) throws ClassNotFoundException
	{
		List<Class<?>> result = new ArrayList<>();

		for (String param : msp.getParamTypes())
		{
			String paramClassName = msp.applyGenericSubstitutionsForClassLoading(param);

			Class<?> clazz = ParseUtil.findClassForLogCompilationParameter(paramClassName);

			result.add(clazz);
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
	public MemberBytecode getMemberBytecode()
	{
		MemberBytecode result = null;
		
		if (metaClass != null)
		{
			ClassBC classBytecode = metaClass.getClassBytecode();
			
			if (classBytecode != null)
			{
				result = classBytecode.getMemberBytecode(this);
			}
		}
				
		return result;
	}

	@Override
	public List<BytecodeInstruction> getInstructions()
	{
		List<BytecodeInstruction> result = null;
		
		MemberBytecode memberBytecode = getMemberBytecode();
		
		if (memberBytecode != null)
		{
			result = memberBytecode.getInstructions();
		}
		else
		{
			result = new ArrayList<>();
		}
		
		return result;
	}


	@Override
	public MetaClass getMetaClass()
	{
		return metaClass;
	}

	@Override
	public String getQueuedAttribute(String key)
	{
		return queuedAttributes.get(key);
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
	public Map<String, String> getQueuedAttributes()
	{
		return queuedAttributes;
	}
	
	@Override
	public Map<String, String> getCompiledAttributes()
	{
		return compiledAttributes;
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

		if (paramTypes.size() > 0)
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
	public List<AssemblyMethod> getAssemblyMethods()
	{
		return assemblyMethods;
	}

	@Override
	public void addAssembly(AssemblyMethod asmMethod)
	{
		if (DEBUG_LOGGING_ASSEMBLY)
		{
			logger.debug("setAssembly on member {}", getFullyQualifiedMemberName());
		}

		assemblyMethods.add(asmMethod);
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
			builder.append(StringUtil.getUnqualifiedClassName(memberName));
		}
		else
		{
			builder.append(memberName);
		}

		builder.append(REGEX_ZERO_OR_MORE_SPACES);

		builder.append(S_ESCAPED_OPEN_PARENTHESES);

		if (paramTypes.size() > 0)
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
			paramType = StringUtil.getUnqualifiedClassName(paramType);
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
			paramType = REGEX_UNICODE_PACKAGE_NAME + StringUtil.getUnqualifiedClassName(paramType);
		}

		return paramType;
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

	@Override
	public Journal getJournal()
	{
		return journal;
	}

	@Override
	public void addJournalEntry(Tag entry)
	{
		journal.addEntry(entry);
	}
}