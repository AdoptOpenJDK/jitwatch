package com.chrisnewland.jitwatch.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseUtil
{
	private static final Pattern PATTERN_LOG_SIGNATURE = Pattern.compile("^([0-9a-zA-Z\\.\\$_]+) ([0-9a-zA-Z<>_\\$]+) (\\(.*\\))(.*)");
	
	public static Class<?> getPrimitiveClass(char c)
	{
		switch (c)
		{
		case 'S':
			return Short.TYPE;
		case 'C':
			return Character.TYPE;
		case 'B':
			return Byte.TYPE;
		case 'V':
			return Void.TYPE;
		case 'J':
			return Long.TYPE;
		case 'D':
			return Double.TYPE;
		case 'Z':
			return Boolean.TYPE;
		case 'I':
			return Integer.TYPE;
		case 'F':
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
			case '[':
				arrayDepth++;
				break;
			case 'S':
				builder.append("short");
				break;
			case 'C':
				builder.append("char");
				break;
			case 'B':
				builder.append("byte");
				break;
			case 'J':
				builder.append("long");
				break;
			case 'D':
				builder.append("double");
				break;
			case 'Z':
				builder.append("boolean");
				break;
			case 'I':
				builder.append("int");
				break;
			case 'F':
				builder.append("float");
				break;
			case ';':
				break;
			default:
				if (name.charAt(pos) == 'L' && name.endsWith(";"))
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
			builder.append("[]");
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
			String paramTypes = matcher.group(3).replace("(", "").replace(")", "");
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

		if ("<init>".equals(methodName))
		{
			builder.append(className);
		}
		else
		{
			builder.append(rName).append(" ").append(className).append(".").append(methodName);
		}

		builder.append("(");

		for (Class<?> c : paramTypes)
		{
			String cName = c.getName();
			cName = ParseUtil.expandParameterType(cName);

			builder.append(cName).append(",");
		}

		if (paramTypes.length > 0)
		{
			builder.deleteCharAt(builder.length() - 1);
		}

		builder.append(")");

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
					case '[':
						// Could be
						// [Ljava.lang.String; Object array
						// [I primitive array
						// [..[I multidimensional primitive array
						// [..[Ljava.lang.String multidimensional Object array
						builder.delete(0, builder.length());
						builder.append(c);
						pos++;
						c = types.charAt(pos);

						while (c == '[')
						{
							builder.append(c);
							pos++;
							c = types.charAt(pos);
						}

						if (c == 'L')
						{
							// array of ref type
							while (pos < typeLen)
							{
								c = types.charAt(pos++);
								builder.append(c);

								if (c == ';')
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
					case 'L':
						// ref type
						while (pos < typeLen)
						{
							pos++;
							c = types.charAt(pos);

							if (c == ';')
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
}
