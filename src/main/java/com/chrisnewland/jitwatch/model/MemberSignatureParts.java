/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.model;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public class MemberSignatureParts
{
    private static final int FIRST_GROUP = 1;
    private static final int SECOND_GROUP = 2;
    private static final int THIRD_GROUP = 3;
    private static final int FOURTH_GROUP = 4;

    private int modifier;
	private List<String> modifierList;
	private Map<String, String> genericsMap;
	private String returnType;
	private String memberName;
	private List<String> paramTypeList;

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

	public MemberSignatureParts(String toParse)
	{
		modifierList = new ArrayList<>();
		genericsMap = new LinkedHashMap<>();
		paramTypeList = new ArrayList<>();
		modifier = 0;

		StringBuilder builder = new StringBuilder();

		builder.append("^[ ]*");

		for (String mod : modifierMap.keySet())
		{
			builder.append(S_OPEN_PARENTHESES).append(mod).append(S_SPACE).append(S_CLOSE_PARENTHESES).append(C_QUESTION);
		}

		String regexGenerics = "(<.*> )?";
		String regexReturnType = "(.* )?"; // optional could be constructor
		String regexMethodName = "([\\p{L}0-9\\.]+)";
		String regexParams = "(\\(.*\\))";
		String regexRest = "(.*)";

		builder.append(regexGenerics);
		builder.append(regexReturnType);
		builder.append(regexMethodName);
		builder.append(regexParams);
		builder.append(regexRest);

        final Pattern PATTERN_BC_SIGNATURE = Pattern.compile(builder.toString());

		Matcher matcher = PATTERN_BC_SIGNATURE.matcher(toParse);

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
					modifierList.add(group);

					// add bitset value for this modifier
					modifier += modifierMap.get(group);
				}

				if (i == modifierCount + FIRST_GROUP)
				{
					if (group != null)
					{
						buildGenerics(group);
					}
				}

				if (i == modifierCount + SECOND_GROUP)
				{
					if (group != null)
					{
						returnType = group;
					}
				}

				if (i == modifierCount + THIRD_GROUP)
				{
					if (group != null)
					{
						memberName = group;
					}
				}

				if (i == modifierCount + FOURTH_GROUP)
				{
					if (group != null)
					{
						buildParamTypes(group);
					}
				}
			}
		}
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

				if (c == ',')
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
				else if (c == '<')
				{
					angleBracketDepth++;
					paramBuilder.append(c);
				}
				else if (c == '>')
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

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("modifiers: ");

		if (modifierList.size() > 0)
		{

			for (String mod : modifierList)
			{
				sb.append(mod).append(',');
			}

			sb.deleteCharAt(sb.length() - 1);
		}

		sb.append("\n");

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

				sb.append(',');

			}

			sb.deleteCharAt(sb.length() - 1);
		}

		sb.append("\n");

		sb.append("returnType: ").append(returnType).append("\n");

		sb.append("memberName: ").append(memberName).append("\n");

		sb.append("paramTypes: ");

		if (paramTypeList.size() > 0)
		{

			for (String param : paramTypeList)
			{
				sb.append(param).append(',');
			}

			sb.deleteCharAt(sb.length() - 1);
		}

		sb.append("\n");

		return sb.toString();
	}
}