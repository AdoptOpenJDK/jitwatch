/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_CLOSE_ANGLE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_COMMA;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_OPEN_ANGLE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_QUESTION;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_CLOSE_PARENTHESES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_COMMA;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_DOT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_OPEN_PARENTHESES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_SLASH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_SPACE;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.adoptopenjdk.jitwatch.util.ParseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemberSignatureParts
{
	private String fullyQualifiedClassName;
	private int modifier;
	private List<String> modifierList;
	private Map<String, String> genericsMap;
	private String returnType;
	private String memberName;
	private List<String> paramTypeList;

	private static final Logger logger = LoggerFactory.getLogger(MemberSignatureParts.class);

	// LinkedHashMap to ensure entry set iteration matches insertion order
	private static final Map<String, Integer> modifierMap = new LinkedHashMap<String, Integer>();

	static
	{
		addModifierMapping(Modifier.PUBLIC);
		addModifierMapping(Modifier.PROTECTED);
		addModifierMapping(Modifier.PRIVATE);
		addModifierMapping(Modifier.ABSTRACT);
		addModifierMapping(Modifier.STATIC);
		addModifierMapping(Modifier.FINAL);
		addModifierMapping(Modifier.SYNCHRONIZED);
		addModifierMapping(Modifier.NATIVE);
		addModifierMapping(Modifier.STRICT);
	}

	private static void addModifierMapping(int modifier)
	{
		modifierMap.put(Modifier.toString(modifier), modifier);
	}

	private MemberSignatureParts()
	{
		modifierList = new ArrayList<>();
		genericsMap = new LinkedHashMap<>();
		paramTypeList = new ArrayList<>();
		modifier = 0;
	}

	private static void completeSignature(String origSig, MemberSignatureParts msp)
	{
		if (msp.memberName != null)
		{
			// Constructors will return void for returnType
			if (ParseUtil.CONSTRUCTOR_INIT.equals(msp.memberName) || msp.memberName.equals(msp.fullyQualifiedClassName))
			{
				msp.memberName = msp.fullyQualifiedClassName;
				msp.returnType = Void.TYPE.getName();
			}
		}
		else
		{
			logger.warn("MemberSignatureParts.memberName was null for signature: '{}'\n{}", origSig, msp);
		}
	}

	public static MemberSignatureParts fromParts(String fullyQualifiedClassName, String memberName, String returnType,
			List<String> paramTypes)
	{
		MemberSignatureParts msp = new MemberSignatureParts();

		msp.fullyQualifiedClassName = fullyQualifiedClassName;

		msp.memberName = memberName;

		msp.paramTypeList.addAll(paramTypes);

		msp.returnType = returnType;

		completeSignature(fullyQualifiedClassName+S_COMMA+memberName+S_COMMA+returnType, msp);

		return msp;
	}

	public static MemberSignatureParts fromLogCompilationSignature(String toParse) throws LogParseException
	{
		MemberSignatureParts msp = new MemberSignatureParts();

		String[] parts = ParseUtil.splitLogSignatureWithRegex(toParse);

		msp.fullyQualifiedClassName = parts[0];
		msp.memberName = parts[1];

		String paramTypes = parts[2];
		String returnType = parts[3];

		Class<?>[] paramClasses = ParseUtil.getClassTypes(paramTypes);
		Class<?>[] returnClasses = ParseUtil.getClassTypes(returnType);

		Class<?> returnClass;

		if (returnClasses.length == 1)
		{
			returnClass = returnClasses[0];
		}
		else
		{
			returnClass = Void.class;
		}

		for (Class<?> paramClass : paramClasses)
		{
			msp.paramTypeList.add(paramClass.getName());
		}

		msp.returnType = returnClass.getName();

		completeSignature(toParse, msp);

		return msp;
	}

	private static boolean isStaticInitialiser(String bytecodeSignature)
	{
		return ParseUtil.STATIC_BYTECODE_SIGNATURE.equals(bytecodeSignature);
	}

	public static MemberSignatureParts fromBytecodeSignature(String fqClassName, String toParse)
	{
		MemberSignatureParts msp = new MemberSignatureParts();

		msp.fullyQualifiedClassName = fqClassName;

		if (isStaticInitialiser(toParse))
		{
			msp.memberName = ParseUtil.STATIC_INIT;
			msp.returnType = Void.TYPE.getName();

			return msp;
		}

		StringBuilder builder = new StringBuilder();

		builder.append("^[ ]*");

		for (String mod : modifierMap.keySet())
		{
			builder.append(S_OPEN_PARENTHESES).append(mod).append(S_SPACE).append(S_CLOSE_PARENTHESES).append(C_QUESTION);
		}

		String regexGenerics = "(<.*> )?";
		String regexReturnType = "(.* )?"; // optional could be constructor
		String regexMethodName = ParseUtil.METHOD_NAME_REGEX_GROUP;
		String regexParams = "(\\(.*\\))";
		String regexRest = "(.*)";

		builder.append(regexGenerics);
		builder.append(regexReturnType);
		builder.append(regexMethodName);
		builder.append(regexParams);
		builder.append(regexRest);

		final Pattern patternBytecodeSignature = Pattern.compile(builder.toString());

		Matcher matcher = patternBytecodeSignature.matcher(toParse);

		int modifierCount = modifierMap.size();

		if (matcher.find())
		{
			int count = matcher.groupCount();

			for (int i = 1; i < count; i++)
			{
				String group = matcher.group(i);

				if (group != null)
				{
					group = group.trim();
				}

				if (group != null && i <= modifierCount)
				{
					msp.modifierList.add(group);

					// add bitset value for this modifier
					msp.modifier += modifierMap.get(group);
				}

				if (i == modifierCount + 1)
				{
					if (group != null)
					{
						msp.buildGenerics(group);
					}
				}

				if (i == modifierCount + 2)
				{
					if (group != null)
					{
						msp.returnType = group;
					}
				}

				if (i == modifierCount + 3)
				{
					if (group != null)
					{
						msp.memberName = group;
					}
				}

				if (i == modifierCount + 4)
				{
					if (group != null)
					{
						msp.buildParamTypes(group);
					}
				}
			}
		}

		completeSignature(toParse, msp);

		return msp;
	}

	private void buildGenerics(String genericsString)
	{
		String stripped = genericsString.substring(1, genericsString.length() - 1);
		String[] substitutions = stripped.split(S_COMMA);

		for (String sub : substitutions)
		{
			sub = sub.replace(S_SLASH, S_DOT); // in package names

			if (sub.contains(" extends "))
			{
				String[] pair = sub.split(" extends ");
				String child = pair[0].trim();
				String parent = pair[1].trim();
				genericsMap.put(child, parent);
			}
			else
			{
				genericsMap.put(sub, null);
			}
		}
	}

	private void buildParamTypes(String paramString)
	{
		int angleBracketDepth = 0;

		String stripped = paramString.substring(1, paramString.length() - 1);

		if (stripped.length() > 0)
		{
			StringBuilder paramBuilder = new StringBuilder();

			for (int i = 0; i < stripped.length(); i++)
			{
				char c = stripped.charAt(i);

				if (c == C_COMMA)
				{
					if (angleBracketDepth == 0)
					{
						// finished param
						addParam(paramBuilder);
					}
					else
					{
						paramBuilder.append(c);
					}
				}
				else if (c == C_OPEN_ANGLE)
				{
					angleBracketDepth++;
					paramBuilder.append(c);
				}
				else if (c == C_CLOSE_ANGLE)
				{
					angleBracketDepth--;
					paramBuilder.append(c);
				}
				else
				{
					paramBuilder.append(c);
				}
			}

			// last param
			addParam(paramBuilder);
		}

	}

	private void addParam(StringBuilder paramBuilder)
	{
		String param = paramBuilder.toString().trim();

		int lastSpacePos = param.lastIndexOf(C_SPACE);

		if (lastSpacePos != -1)
		{
			boolean first = true;
			boolean validParamName = true;

			// check every character after space to see if valid Java identifier
			for (int i = lastSpacePos + 1; i < param.length(); i++)
			{
				char c = param.charAt(i);

				if (first && !Character.isJavaIdentifierStart(c))
				{
					validParamName = false;
					break;
				}
				else if (!Character.isJavaIdentifierPart(c))
				{
					validParamName = false;
					break;
				}

				first = false;
			}

			if (validParamName)
			{
				param = param.substring(0, lastSpacePos);
			}
		}

		paramTypeList.add(param);
		paramBuilder.delete(0, paramBuilder.length());
	}

	public int getModifier()
	{
		return modifier;
	}

	public List<String> getModifiers()
	{
		return modifierList;
	}

	public Map<String, String> getGenerics()
	{
		return genericsMap;
	}

	public String getReturnType()
	{
		return returnType;
	}

	public String getMemberName()
	{
		return memberName;
	}

	public List<String> getParamTypes()
	{
		return paramTypeList;
	}

	public String getFullyQualifiedClassName()
	{
		return fullyQualifiedClassName;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append(C_NEWLINE);

		sb.append("modifiers: ");

		if (modifierList.size() > 0)
		{

			for (String mod : modifierList)
			{
				sb.append(mod).append(C_COMMA);
			}

			sb.deleteCharAt(sb.length() - 1);
		}

		sb.append(C_NEWLINE);

		sb.append("generics: ");

		if (genericsMap.size() > 0)
		{

			for (Map.Entry<String, String> entry : genericsMap.entrySet())
			{
				if (entry.getValue() != null)
				{
					sb.append(entry.getKey()).append("=>").append(entry.getValue());
				}
				else
				{
					sb.append(entry.getKey());
				}

				sb.append(C_COMMA);

			}

			sb.deleteCharAt(sb.length() - 1);
		}

		sb.append(C_NEWLINE);

		sb.append("class: ").append(fullyQualifiedClassName).append(C_NEWLINE);

		sb.append("returnType: ").append(returnType).append(C_NEWLINE);

		sb.append("memberName: ").append(memberName).append(C_NEWLINE);

		sb.append("paramTypes: ");

		if (paramTypeList.size() > 0)
		{

			for (String param : paramTypeList)
			{
				sb.append(param).append(C_COMMA);
			}

			sb.deleteCharAt(sb.length() - 1);
		}

		sb.append(C_NEWLINE);

		return sb.toString();
	}
}