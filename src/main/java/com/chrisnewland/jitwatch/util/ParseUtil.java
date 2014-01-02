/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/blob/master/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
package com.chrisnewland.jitwatch.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.chrisnewland.jitwatch.model.IMetaMember;

public class ParseUtil
{
	private static final Pattern PATTERN_LOG_SIGNATURE = Pattern.compile("^([0-9a-zA-Z\\.\\$_]+) ([0-9a-zA-Z<>_\\$]+) (\\(.*\\))(.*)");
	
	private static final String S_OPEN_PARENTHESES = "(";
	private static final String S_CLOSE_PARENTHESES = ")";
	private static final String S_AT = "@";
	private static final String S_HASH = "#";
	private static final String S_SPACE = " ";
	private static final String S_EMPTY = "";
	private static final String S_SEMICOLON = ";";
	
	private static final String SQUARE_BRACKET_PAIR = "[]";
	private static final String CONSTRUCTOR_INIT = "<init>";
	
	private static final String NAME_SHORT = "short";
	private static final String NAME_CHARACTER = "char";
	private static final String NAME_BYTE = "byte";
	private static final String NAME_LONG = "long";
	private static final String NAME_DOUBLE = "double";
	private static final String NAME_BOOLEAN = "boolean";
	private static final String NAME_INTEGER = "int";
	private static final String NAME_FLOAT = "float";

	private static final char C_COMMA = ',';
	private static final char C_DOT = '.';
	private static final char C_OBJECT_REF = 'L';
	private static final char C_SEMICOLON = ';';
	private static final char C_OPEN_SQUARE_BRACKET = '[';
	
	private static final char TYPE_SHORT = 'S';
	private static final char TYPE_CHARACTER = 'C';
	private static final char TYPE_BYTE = 'B';
	private static final char TYPE_VOID = 'V';
	private static final char TYPE_LONG = 'J';
	private static final char TYPE_DOUBLE = 'D';
	private static final char TYPE_BOOLEAN = 'Z';
	private static final char TYPE_INTEGER = 'I';
	private static final char TYPE_FLOAT = 'F';

	public static long parseStamp(String stamp)
	{
	    return (long)(Double.parseDouble(stamp) * 1000);
	}
	
	public static Class<?> getPrimitiveClass(char c)
	{
		switch (c)
		{
		case TYPE_SHORT:
			return Short.TYPE;
		case TYPE_CHARACTER:
			return Character.TYPE;
		case TYPE_BYTE:
			return Byte.TYPE;
		case TYPE_VOID:
			return Void.TYPE;
		case TYPE_LONG:
			return Long.TYPE;
		case TYPE_DOUBLE:
			return Double.TYPE;
		case TYPE_BOOLEAN:
			return Boolean.TYPE;
		case TYPE_INTEGER:
			return Integer.TYPE;
		case TYPE_FLOAT:
			return Float.TYPE;
		}

		throw new RuntimeException("Unknown class for " + c);
	}

	/*
	 * [C => char[] [[I => int[][]
	 * [Ljava.lang.Object; => java.lang.Object[]
	 */
	public static String expandParameterType(String name)
	{		
		StringBuilder builder = new StringBuilder();

		int arrayDepth = 0;
		int pos = 0;

		outerloop: while (pos < name.length())
		{
			char c = name.charAt(pos);

			switch (c)
			{
			case C_OPEN_SQUARE_BRACKET:
				arrayDepth++;
				break;
			case TYPE_SHORT:
				builder.append(NAME_SHORT);
				break;
			case TYPE_CHARACTER:
				builder.append(NAME_CHARACTER);
				break;
			case TYPE_BYTE:
				builder.append(NAME_BYTE);
				break;
			case TYPE_LONG:
				builder.append(NAME_LONG);
				break;
			case TYPE_DOUBLE:
				builder.append(NAME_DOUBLE);
				break;
			case TYPE_BOOLEAN:
				builder.append(NAME_BOOLEAN);
				break;
			case TYPE_INTEGER:
				builder.append(NAME_INTEGER);
				break;
			case TYPE_FLOAT:
				builder.append(NAME_FLOAT);
				break;
			case C_SEMICOLON:
				break;
			default:
				if (name.charAt(pos) == C_OBJECT_REF && name.endsWith(S_SEMICOLON))
				{
					builder.append(name.substring(pos + 1, name.length() - 1));
				}
				else
				{
					builder.append(name.substring(pos));
				}
				break outerloop;
			}

			pos++;
		}

		for (int i = 0; i < arrayDepth; i++)
		{
			builder.append(SQUARE_BRACKET_PAIR);
		}

		return builder.toString();
	}

	/*
	 * Parses a log file signature into a class name and java declaration-style
	 * method signature
	 * 
	 * @return String[] 0=className 1=methodSignature
	 */
	public static String[] parseLogSignature(String logSignature) throws Exception
	{
		String result[] = null;

		Matcher matcher = PATTERN_LOG_SIGNATURE.matcher(logSignature);

		if (matcher.find())
		{
			String className = matcher.group(1);
			String methodName = matcher.group(2);
			String paramTypes = matcher.group(3).replace(S_OPEN_PARENTHESES, S_EMPTY).replace(S_CLOSE_PARENTHESES, S_EMPTY);
			String returnType = matcher.group(4);

			Class<?>[] paramClasses = ParseUtil.getClassTypes(paramTypes);
			Class<?>[] returnClasses = ParseUtil.getClassTypes(returnType); // length 1

			Class<?> returnClass;

			if (returnClasses.length == 0)
			{
				returnClass = Void.class;
			}
			else
			{
				returnClass = returnClasses[0];
			}
			
			String signature = ParseUtil.buildMethodSignature(className, methodName, paramClasses, returnClass);

			result = new String[] { className, signature };
		}

		return result;
	}

	public static String buildMethodSignature(String className, String methodName, Class<?>[] paramTypes, Class<?> returnType)
	{
		StringBuilder builder = new StringBuilder();

		String rName = returnType.getName();
		rName = ParseUtil.expandParameterType(rName);

		if (CONSTRUCTOR_INIT.equals(methodName))
		{
			builder.append(className);
		}
		else
		{
			builder.append(rName).append(S_SPACE).append(className).append(C_DOT).append(methodName);
		}

		builder.append(S_OPEN_PARENTHESES);

		for (Class<?> c : paramTypes)
		{
			String cName = c.getName();
			cName = ParseUtil.expandParameterType(cName);

			builder.append(cName).append(C_COMMA);
		}

		if (paramTypes.length > 0)
		{
			builder.deleteCharAt(builder.length() - 1);
		}

		builder.append(S_CLOSE_PARENTHESES);

		String toMatch = builder.toString();

		return toMatch;
	}

	public static Class<?>[] getClassTypes(String types) throws Exception
	{
		List<Class<?>> classes = new ArrayList<Class<?>>();

		final int typeLen = types.length();

		if (typeLen > 0)
		{
			StringBuilder builder = new StringBuilder();

			try
			{
				int pos = 0;

				while (pos < types.length())
				{
					char c = types.charAt(pos);

					switch (c)
					{
					case C_OPEN_SQUARE_BRACKET:
						// Could be
						// [Ljava.lang.String; Object array
						// [I primitive array
						// [..[I multidimensional primitive array
						// [..[Ljava.lang.String multidimensional Object array
						builder.delete(0, builder.length());
						builder.append(c);
						pos++;
						c = types.charAt(pos);

						while (c == C_OPEN_SQUARE_BRACKET)
						{
							builder.append(c);
							pos++;
							c = types.charAt(pos);
						}

						if (c == C_OBJECT_REF)
						{
							// array of ref type
							while (pos < typeLen)
							{
								c = types.charAt(pos++);
								builder.append(c);

								if (c == C_SEMICOLON)
								{
									break;
								}
							}
						}
						else
						{
							// array of primitive
							builder.append(c);
							pos++;
						}

						Class<?> arrayClass = ClassUtil.loadClassWithoutInitialising(builder.toString());
						classes.add(arrayClass);
						builder.delete(0, builder.length());
						break;
					case C_OBJECT_REF:
						// ref type
						while (pos < typeLen)
						{
							pos++;
							c = types.charAt(pos);

							if (c == C_SEMICOLON)
							{
								pos++;
								break;
							}

							builder.append(c);
						}
						Class<?> refClass = ClassUtil.loadClassWithoutInitialising(builder.toString());
						classes.add(refClass);
						builder.delete(0, builder.length());
						break;
					default:
						// primitive
						Class<?> primitiveClass = ParseUtil.getPrimitiveClass(c);
						classes.add(primitiveClass);
						pos++;

					} // end switch

				} // end while

			}
			catch (ClassNotFoundException cnf)
			{
				throw new Exception("ClassNotFoundException: " + builder.toString());
			}
			catch (NoClassDefFoundError ncdf)
			{
				throw new Exception("NoClassDefFoundError: " + builder.toString());
			}
			catch (Exception ex)
			{
				throw new Exception("Exception: " + ex.getMessage());
			}
			catch (Error err)
			{
				throw new Exception("Error: " + err.getMessage());
			}

		} // end if empty

		return classes.toArray(new Class<?>[classes.size()]);
	}
	
	public static int findBestLineMatchForMemberSignature(IMetaMember member, List<String> lines)
	{	
		String methodName = member.getMemberName();
		String modifier = member.getModifier();
		String returnTypeName = member.getReturnTypeName();
		String[] paramTypeNames = member.getParamTypeNames();
		
		int index = 0;
		int bestScoreLine = 0;
		int bestScore = 0;
		
		for (String line : lines)
		{
			int score = 0;
			
			// method name match
			if (line.contains(methodName+S_OPEN_PARENTHESES) || line.contains(methodName+S_SPACE))
			{								
				if (line.contains(S_AT) || line.contains(S_HASH))
				{
					// possibly a Javadoc reference to the method
					continue;
				}
				
				// modifiers matched
				if (line.contains(modifier))
				{
					score++;
				}
				
				int commaCount = 0;
				int expectedCommas = paramTypeNames.length == 0 ? 0 : paramTypeNames.length - 1;
				
				String betweenParentheses = StringUtil.getSubstringBetween(line, S_OPEN_PARENTHESES, S_CLOSE_PARENTHESES);
				
				if (betweenParentheses != null)
				{
					for (int i = 0; i < betweenParentheses.length(); i++)
					{
						if (betweenParentheses.charAt(i) == C_COMMA)
						{
							commaCount++;
						}
					}
									
					// correct number of parameters
					if (commaCount == expectedCommas)
					{
						for (String paramType : paramTypeNames)
						{
							if (betweenParentheses.contains(paramType))
							{
								score++;
							}
						}
					}				
					
					// return type matched
					if (line.contains(returnTypeName))
					{
						score++;
					}
					
					if (score > bestScore)
					{
						bestScoreLine = index;
						bestScore = score;
					}
				}
				
			}

			index++;
		}
		
		return bestScoreLine;	
	}
}
