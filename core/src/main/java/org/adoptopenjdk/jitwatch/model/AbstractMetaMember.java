/*
 * Copyright (c) 2013-2017 Chris Newland.
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
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_CLOSE_SQUARE_BRACKET;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_COMMA;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_DOT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_TYPE_NAME_VOID;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_ESCAPED_CLOSE_PARENTHESES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_ESCAPED_CLOSE_SQUARE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_ESCAPED_OPEN_PARENTHESES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_ESCAPED_OPEN_SQUARE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_OPEN_SQUARE_BRACKET;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_POLYMORPHIC_SIGNATURE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILE_ID;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILE_KIND;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C2N;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	private List<Compilation> compilations;
	private int selectedCompilation;

	private boolean isCompiled = false;

	protected boolean isVarArgs = false;
	protected boolean isPolymorphicSignature = false;
	protected int modifier; // bitset
	private String memberName;
	protected Class<?> returnType;
	protected List<Class<?>> paramTypes;

	public AbstractMetaMember(String memberName)
	{
		this.memberName = memberName;

		compilations = new ArrayList<>();
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
			logger.debug("nameMatches this.memberName: '{}' fq: '{}' other '{}' fq: '{}'", memberName,
					getFullyQualifiedMemberName(), msp.getMemberName(), msp.getFullyQualifiedClassName());
		}

		boolean match = memberName.equals(msp.getMemberName());

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
			matched = (isConstructor());

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
		String result = null;

		if (isConstructor() || returnType == null)
		{
			result = S_TYPE_NAME_VOID;
		}
		else
		{
			result = ParseUtil.expandParameterType(returnType.getName());
		}

		return result;
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
		return getLastCompilation() == null ? null : getLastCompilation().getQueuedAttribute(key);
	}

	@Override
	public String getCompiledAttribute(String key)
	{
		return getLastCompilation() == null ? null : getLastCompilation().getCompiledAttribute(key);
	}

	@Override
	public void setTagTaskQueued(Tag tagTaskQueued)
	{
		Compilation compilation = createCompilation();

		compilation.setTagTaskQueued(tagTaskQueued);

		compilations.add(compilation);
	}

	@Override
	public Compilation getCompilationByCompileID(String compileID)
	{
		Compilation result = null;

		for (Compilation compilation : compilations)
		{
			if (compileID.equals(compilation.getCompileID()))
			{
				result = compilation;
				break;
			}
		}

		return result;
	}

	@Override
	public Compilation getCompilationByNativeAddress(String address)
	{
		Compilation result = null;

		for (Compilation compilation : compilations)
		{
			if (address.equals(compilation.getNativeAddress()))
			{
				result = compilation;
				break;
			}
		}

		return result;
	}

	@Override
	public void setTagNMethod(Tag tagNMethod)
	{
		isCompiled = true;

		String compileID = tagNMethod.getAttributes().get(ATTR_COMPILE_ID);

		Compilation compilation = getCompilationByCompileID(compileID);

		if (compilation != null)
		{
			compilation.setTagNMethod(tagNMethod);
		}
		else
		{
			// check if C2N stub
			String compileKind = tagNMethod.getAttributes().get(ATTR_COMPILE_KIND);

			if (C2N.equals(compileKind))
			{
				compilation = createCompilation();

				compilation.setTagNMethod(tagNMethod);

				storeCompilation(compilation);
			}
			else
			{
				logger.warn("Didn't find compilation with ID {}", compileID);
			}
		}

		// inform package tree it contains class with a compiled method
		getMetaClass().getPackage().setHasCompiledClasses();
	}

	private void storeCompilation(Compilation compilation)
	{
		compilations.add(compilation);

		selectedCompilation = compilations.size() - 1;
	}

	private Compilation createCompilation()
	{
		int nextIndex = compilations.size();

		selectedCompilation = nextIndex;

		return new Compilation(this, nextIndex);
	}

	@Override
	public void setTagTask(Task tagTask)
	{
		String compileID = tagTask.getAttributes().get(ATTR_COMPILE_ID);

		Compilation compilation = getCompilationByCompileID(compileID);

		if (compilation != null)
		{
			compilation.setTagTask(tagTask);
		}
		else
		{
			logger.warn("Didn't find compilation with ID {}", compileID);
		}
	}

	@Override
	public void setTagTaskDone(String compileID, Tag tagTaskDone)
	{
		Compilation compilation = getCompilationByCompileID(compileID);

		if (compilation != null)
		{
			compilation.setTagTaskDone(tagTaskDone);
		}
		else
		{
			logger.warn("Didn't find compilation with ID {}", compileID);
		}
	}

	@Override
	public boolean isCompiled()
	{
		return isCompiled;
	}

	@Override
	public Map<String, String> getQueuedAttributes()
	{
		return getLastCompilation() == null ? null : getLastCompilation().getQueuedAttributes();
	}

	@Override
	public Map<String, String> getCompiledAttributes()
	{
		return getLastCompilation() == null ? null : getLastCompilation().getCompiledAttributes();
	}

	@Override
	public String toStringUnqualifiedMethodName(boolean visibilityAndReturnType, boolean fqParamTypes)
	{
		StringBuilder builder = new StringBuilder();

		if (visibilityAndReturnType)
		{
			if (modifier != 0)
			{
				builder.append(Modifier.toString(modifier)).append(C_SPACE);
			}

			if (!isConstructor() && returnType != null)
			{
				builder.append(expandParam(returnType.getName(), fqParamTypes)).append(C_SPACE);
			}
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
	public void addAssembly(AssemblyMethod asmMethod)
	{
		if (DEBUG_LOGGING_ASSEMBLY)
		{
			logger.debug("setAssembly on member {}", getFullyQualifiedMemberName());
		}

		Compilation compilation = getCompilationByNativeAddress(asmMethod.getNativeAddress());

		if (compilation != null)
		{
			compilation.setAssembly(asmMethod);
		}
		else
		{
			logger.warn("Didn't find compilation to attach assembly for address {}", asmMethod.getNativeAddress());
		}
	}

	@Override
	public List<Compilation> getCompilations()
	{
		return compilations;
	}

	@Override
	public boolean isConstructor()
	{
		return (this instanceof MetaConstructor);
	}

	@Override
	public String getSourceMethodSignatureRegEx()
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
		if (!isConstructor() && returnType != null)
		{
			String rt = expandParamRegEx(returnType.getName());

			builder.append(rt);
			builder.append(C_SPACE);
		}

		if (isConstructor())
		{
			builder.append(REGEX_UNICODE_PACKAGE_NAME);
			builder.append(StringUtil.getUnqualifiedClassName(memberName));
		}
		else
		{
			builder.append(memberName);
		}

		// TODO return type and name should uniquely identify a method

		/*
		 * builder.append(REGEX_ZERO_OR_MORE_SPACES);
		 * 
		 * builder.append(S_ESCAPED_OPEN_PARENTHESES);
		 * 
		 * if (paramTypes.size() > 0) { for (Class<?> paramClass : paramTypes) {
		 * builder.append(REGEX_ZERO_OR_MORE_SPACES);
		 * 
		 * String paramType = expandParamRegEx(paramClass.getName());
		 * 
		 * builder.append(paramType); builder.append(REGEX_ONE_OR_MORE_SPACES);
		 * builder.append(REGEX_UNICODE_PARAM_NAME); builder.append(S_COMMA); }
		 * 
		 * builder.deleteCharAt(builder.length() - 1); }
		 * 
		 * builder.append(REGEX_ZERO_OR_MORE_SPACES);
		 * builder.append(S_ESCAPED_CLOSE_PARENTHESES);
		 * builder.append(REGEX_GROUP_ANY); builder.append(C_DOLLAR);
		 */

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

			paramType = paramType.replace(S_OPEN_SQUARE_BRACKET, S_ESCAPED_OPEN_SQUARE).replace(S_CLOSE_SQUARE_BRACKET,
					S_ESCAPED_CLOSE_SQUARE);
		}

		if (paramType.contains(S_DOT))
		{
			paramType = REGEX_UNICODE_PACKAGE_NAME + StringUtil.getUnqualifiedClassName(paramType);
		}

		return paramType;
	}

	@Override
	public Compilation getLastCompilation()
	{
		int compilationCount = compilations.size();

		Compilation result = null;

		if (compilationCount > 0)
		{
			result = compilations.get(compilationCount - 1);
		}

		return result;
	}

	private int makeSafeIndex(int index)
	{
		return Math.max(0, Math.min(index, compilations.size() - 1));
	}

	public Compilation getCompilation(int index)
	{
		Compilation result = null;

		if (index >= 0 && index < compilations.size())
		{
			result = compilations.get(index);
		}

		return result;
	}

	@Override
	public void setSelectedCompilation(int index)
	{
		this.selectedCompilation = makeSafeIndex(index);
	}

	@Override
	public Compilation getSelectedCompilation()
	{
		return getCompilation(selectedCompilation);
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
}