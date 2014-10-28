/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.util;

import org.adoptopenjdk.jitwatch.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

public final class ParseUtil
{
	private static final Logger logger = LoggerFactory.getLogger(ParseUtil.class);

	// class<SPACE>METHOD<SPACE>(PARAMS)RETURN
	private static final Pattern PATTERN_LOG_SIGNATURE = Pattern
			.compile("^([0-9\\p{L}\\.\\$_]+) ([0-9\\p{L}<>_\\$]+) (\\(.*\\))(.*)");

	private static final Pattern PATTERN_ASSEMBLY_SIGNATURE = Pattern.compile("^(.*)\\s'(.*)'\\s'(.*)'\\sin\\s'(.*)'");

	public static final String SQUARE_BRACKET_PAIR = "[]";
	public static final String CONSTRUCTOR_INIT = "<init>";

	public static final String NAME_SHORT = "short";
	public static final String NAME_CHARACTER = "char";
	public static final String NAME_BYTE = "byte";
	public static final String NAME_LONG = "long";
	public static final String NAME_DOUBLE = "double";
	public static final String NAME_BOOLEAN = "boolean";
	public static final String NAME_INTEGER = "int";
	public static final String NAME_FLOAT = "float";
	public static final String NAME_VOID = "void";

	public static final char TYPE_SHORT = 'S';
	public static final char TYPE_CHARACTER = 'C';
	public static final char TYPE_BYTE = 'B';
	public static final char TYPE_VOID = 'V';
	public static final char TYPE_LONG = 'J';
	public static final char TYPE_DOUBLE = 'D';
	public static final char TYPE_BOOLEAN = 'Z';
	public static final char TYPE_INTEGER = 'I';
	public static final char TYPE_FLOAT = 'F';

	private ParseUtil()
	{
	}

	public static long parseStamp(String stamp)
	{
		double number = parseLocaleSafeDouble(stamp);

		return (long) (number * 1000);
	}

	public static double parseLocaleSafeDouble(String str)
	{
		NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());

		double result = 0;

		try
		{
			result = nf.parse(str).doubleValue();
		}
		catch (ParseException pe)
		{
			logger.error("", pe);
		}

		return result;
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
	 * [C => char[] [[I => int[][] [Ljava.lang.Object; => java.lang.Object[]
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

	public static String[] splitLogSignatureWithRegex(String logSignature) throws LogParseException
	{
		Matcher matcher = PATTERN_LOG_SIGNATURE.matcher(logSignature);

		if (matcher.find())
		{
			String className = matcher.group(1);
			String methodName = matcher.group(2);
			String paramTypes = matcher.group(3).replace(S_OPEN_PARENTHESES, S_EMPTY).replace(S_CLOSE_PARENTHESES, S_EMPTY);
			String returnType = matcher.group(4);

			return new String[] { className, methodName, paramTypes, returnType };
		}

		throw new LogParseException("Could not split signature with regex: " + logSignature);
	}

	public static IMetaMember findMemberWithSignature(IReadOnlyJITDataModel model, String logSignature) throws LogParseException
	{
		IMetaMember metaMember = null;

		if (logSignature != null)
		{
			MemberSignatureParts msp = MemberSignatureParts.fromLogCompilationSignature(logSignature);

			metaMember = model.findMetaMember(msp);

			if (metaMember == null)
			{
				throw new LogParseException("MetaMember not found for " + logSignature);
			}
		}

		return metaMember;
	}

	public static Class<?>[] getClassTypes(String typesString) throws LogParseException
	{
		List<Class<?>> classes = new ArrayList<Class<?>>();

		if (typesString.length() > 0)
		{
			try
			{
				findClassesForTypeString(typesString, classes);
			}
			catch (Throwable t)
			{
				throw new LogParseException("Could not parse types: " + typesString, t);
			}

		} // end if empty

		return classes.toArray(new Class<?>[classes.size()]);
	}

	public static Class<?> findClassForLogCompilationParameter(String param) throws ClassNotFoundException
	{
		StringBuilder builder = new StringBuilder();

		if (isPrimitive(param))
		{
			return classForPrimitive(param);
		}
		else
		{
			int arrayBracketCount = getArrayBracketCount(param);

			if (arrayBracketCount == 0)
			{
				if (param.endsWith(S_VARARGS_DOTS))
				{
					String partBeforeDots = param.substring(0, param.length() - S_VARARGS_DOTS.length());

					if (isPrimitive(partBeforeDots))
					{
						builder.append(S_OPEN_ANGLE).append(classForPrimitive(partBeforeDots));
					}
					else
					{
						builder.append(S_OBJECT_ARRAY_DEF).append(partBeforeDots);
						builder.append(C_SEMICOLON);
					}
				}
				else
				{
					builder.append(param);
				}
			}
			else
			{
				int arrayBracketChars = 2 * arrayBracketCount;

				String partBeforeArrayBrackets = param.substring(0, param.length() - arrayBracketChars);

				for (int i = 0; i < arrayBracketCount - 1; i++)
				{
					builder.append(C_OPEN_SQUARE_BRACKET);
				}

				if (isPrimitive(partBeforeArrayBrackets))
				{
					builder.append(C_OPEN_SQUARE_BRACKET);

					builder.append(getClassTypeCharForPrimitiveTypeString(partBeforeArrayBrackets));
				}
				else
				{
					builder.append(S_OBJECT_ARRAY_DEF);

					builder.append(param);

					builder.delete(builder.length() - arrayBracketChars, builder.length());

					builder.append(C_SEMICOLON);
				}
			}

			return ClassUtil.loadClassWithoutInitialising(builder.toString());
		}
	}

	public static boolean paramClassesMatch(boolean memberHasVarArgs, List<Class<?>> memberParamClasses,
			List<Class<?>> signatureParamClasses)
	{
		boolean result = true;

		final int memberParamCount = memberParamClasses.size();
		final int signatureParamCount = signatureParamClasses.size();

		if (DEBUG_LOGGING_SIG_MATCH)
		{
			logger.debug("M Count: {} S Count: {}", memberParamCount, signatureParamCount);
		}

		if (memberParamCount > 0 && signatureParamCount > 0)
		{
			int memPos = memberParamCount - 1;

			for (int sigPos = signatureParamCount - 1; sigPos >= 0; sigPos--)
			{
				Class<?> sigParamClass = signatureParamClasses.get(sigPos);

				Class<?> memParamClass = memberParamClasses.get(memPos);

				boolean memberParamCouldBeVarArgs = false;

				boolean isLastParameter = (memPos == memberParamCount - 1);

				if (memberHasVarArgs && isLastParameter)
				{
					memberParamCouldBeVarArgs = true;
				}

				if (memParamClass.isAssignableFrom(sigParamClass))
				{
					if (DEBUG_LOGGING_SIG_MATCH)
					{
						logger.debug("{} isAssignableFrom {}", memParamClass, sigParamClass);
					}

					if (memPos > 0)
					{
						// move to previous member parameter
						memPos--;
					}
					else if (sigPos > 0)
					{
						if (DEBUG_LOGGING_SIG_MATCH)
						{
							logger.debug("More signature params but no more member params to try");
						}

						result = false;
						break;
					}
				}
				else
				{
					if (memberParamCouldBeVarArgs)
					{
						// check assignable
						Class<?> componentType = memParamClass.getComponentType();

						if (!componentType.isAssignableFrom(sigParamClass))
						{
							result = false;
							break;
						}
					}
					else
					{
						result = false;
						break;
					}
				}
			}

			boolean unusedMemberParams = (memPos > 0);

			if (unusedMemberParams)
			{
				result = false;
			}
		}

		return result;
	}

	public static boolean typeIsVarArgs(String type)
	{
		return type != null && type.endsWith(S_VARARGS_DOTS);
	}

	public static char getClassTypeCharForPrimitiveTypeString(String type)
	{
		switch (type)
		{
		case NAME_INTEGER:
			return TYPE_INTEGER;
		case NAME_BOOLEAN:
			return TYPE_BOOLEAN;
		case NAME_LONG:
			return TYPE_LONG;
		case NAME_DOUBLE:
			return TYPE_DOUBLE;
		case NAME_FLOAT:
			return TYPE_FLOAT;
		case NAME_SHORT:
			return TYPE_SHORT;
		case NAME_BYTE:
			return TYPE_BYTE;
		case NAME_CHARACTER:
			return TYPE_CHARACTER;
		case NAME_VOID:
			return TYPE_VOID;
		}

		throw new RuntimeException(type + " is not a primitive type");
	}

	public static boolean isPrimitive(String type)
	{
		boolean result = false;

		if (type != null)
		{
			switch (type)
			{
			case NAME_INTEGER:
			case NAME_BOOLEAN:
			case NAME_LONG:
			case NAME_DOUBLE:
			case NAME_FLOAT:
			case NAME_SHORT:
			case NAME_BYTE:
			case NAME_CHARACTER:
			case NAME_VOID:
				result = true;
			}
		}

		return result;
	}

	public static Class<?> classForPrimitive(String primitiveType)
	{
		if (primitiveType != null)
		{
			switch (primitiveType)
			{
			case NAME_INTEGER:
				return int.class;
			case NAME_BOOLEAN:
				return boolean.class;
			case NAME_LONG:
				return long.class;
			case NAME_DOUBLE:
				return double.class;
			case NAME_FLOAT:
				return float.class;
			case NAME_SHORT:
				return short.class;
			case NAME_BYTE:
				return byte.class;
			case NAME_CHARACTER:
				return char.class;
			case NAME_VOID:
				return void.class;
			}
		}

		throw new RuntimeException(primitiveType + " is not a primitive type");
	}

	public static int getArrayBracketCount(String param)
	{
		int count = 0;

		if (param != null)
		{
			int index = param.indexOf(S_ARRAY_BRACKET_PAIR, 0);

			while (index != -1)
			{
				count++;

				index = param.indexOf(S_ARRAY_BRACKET_PAIR, index + 2);
			}
		}

		return count;
	}

	/*
	 * Converts (III[Ljava.lang.String;) into a list of Class<?>
	 */
	private static void findClassesForTypeString(String typesString, List<Class<?>> classes) throws ClassNotFoundException
	{

		int pos = 0;

		StringBuilder builder = new StringBuilder();

		final int stringLen = typesString.length();

		while (pos < stringLen)
		{
			char c = typesString.charAt(pos);

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
				c = typesString.charAt(pos);

				while (c == C_OPEN_SQUARE_BRACKET)
				{
					builder.append(c);
					pos++;
					c = typesString.charAt(pos);
				}

				if (c == C_OBJECT_REF)
				{
					// array of ref type
					while (pos < stringLen)
					{
						c = typesString.charAt(pos++);
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
				while (pos < stringLen - 1)
				{
					pos++;
					c = typesString.charAt(pos);

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

	public static String findBestMatchForMemberSignature(IMetaMember member, List<String> lines)
	{
		String match = null;

		if (lines != null)
		{
			int index = findBestLineMatchForMemberSignature(member, lines);

			if (index > 0 && index < lines.size())
			{
				match = lines.get(index);
			}
		}

		return match;
	}

	public static int findBestLineMatchForMemberSignature(IMetaMember member, List<String> lines)
	{
		int bestScoreLine = 0;

		if (lines != null)
		{
			String memberName = member.getMemberName();
			int modifier = member.getModifier();
			String returnTypeName = member.getReturnTypeName();
			String[] paramTypeNames = member.getParamTypeNames();

			int bestScore = 0;

			for (int i = 0; i < lines.size(); i++)
			{
				String line = lines.get(i);

				int score = 0;

				if (line.contains(memberName))
				{
					MemberSignatureParts msp = MemberSignatureParts.fromBytecodeSignature(member.getMetaClass()
							.getFullyQualifiedName(), line);

					if (!memberName.equals(msp.getMemberName()))
					{
						continue;
					}

					// modifiers matched
					if (msp.getModifier() != modifier)
					{
						continue;
					}

					List<String> mspParamTypes = msp.getParamTypes();

					if (mspParamTypes.size() != paramTypeNames.length)
					{
						continue;
					}

					int pos = 0;

					for (String memberParamType : paramTypeNames)
					{
						String mspParamType = msp.getParamTypes().get(pos++);

						if (compareTypeEquality(memberParamType, mspParamType, msp.getGenerics()))
						{
							score++;
						}
					}

					// return type matched
					if (compareTypeEquality(returnTypeName, msp.getReturnType(), msp.getGenerics()))
					{
						score++;
					}

					if (score > bestScore)
					{
						bestScoreLine = i;
						bestScore = score;
					}
				}
			}
		}

		return bestScoreLine;
	}

	private static boolean compareTypeEquality(String memberTypeName, String inMspTypeName, Map<String, String> genericsMap)
	{
		String mspTypeName = inMspTypeName;
		if (memberTypeName != null && memberTypeName.equals(mspTypeName))
		{
			return true;
		}
		else if (mspTypeName != null)
		{
			// Substitute generics to match with non-generic signature
			// public static <T extends java.lang.Object, U extends
			// java.lang.Object> T[] copyOf(U[], int, java.lang.Class<? extends
			// T[]>)";
			// U[] -> java.lang.Object[]
			String mspTypeNameWithoutArray = getParamTypeWithoutArrayBrackets(mspTypeName);
			String genericSubstitution = genericsMap.get(mspTypeNameWithoutArray);

			if (genericSubstitution != null)
			{
				mspTypeName = mspTypeName.replace(mspTypeNameWithoutArray, genericSubstitution);

				if (memberTypeName != null && memberTypeName.equals(mspTypeName))
				{
					return true;
				}
			}
		}

		return false;
	}

	public static String getParamTypeWithoutArrayBrackets(String paramType)
	{
		int bracketsIndex = paramType.indexOf(SQUARE_BRACKET_PAIR);

		if (bracketsIndex != -1)
		{
			return paramType.substring(0, bracketsIndex);
		}
		else
		{
			return paramType;
		}
	}

	public static IMetaMember lookupMember(String methodId, IParseDictionary parseDictionary, IReadOnlyJITDataModel model)
	{
		IMetaMember result = null;

		Tag methodTag = parseDictionary.getMethod(methodId);

		if (methodTag != null)
		{
			String methodName = methodTag.getAttribute(ATTR_NAME);

			String klassId = methodTag.getAttribute(ATTR_HOLDER);

			Tag klassTag = parseDictionary.getKlass(klassId);

			String metaClassName = klassTag.getAttribute(ATTR_NAME);

			metaClassName = metaClassName.replace(S_SLASH, S_DOT);

			String returnTypeId = methodTag.getAttribute(ATTR_RETURN);

			String argumentsTypeId = methodTag.getAttribute(ATTR_ARGUMENTS);

			String returnType = lookupType(returnTypeId, parseDictionary);

			String[] argumentTypes = new String[0];

			if (argumentsTypeId != null)
			{
				String[] typeIDs = argumentsTypeId.split(S_SPACE);

				argumentTypes = new String[typeIDs.length];

				int pos = 0;

				for (String typeID : typeIDs)
				{
					argumentTypes[pos++] = lookupType(typeID, parseDictionary);
				}
			}

			PackageManager pm = model.getPackageManager();

			MetaClass metaClass = pm.getMetaClass(metaClassName);

			if (metaClass == null)
			{
				logger.debug("metaClass not found: {}. Attempting classload", metaClassName);

				// Possible that TraceClassLoading did not log this class
				// try to classload and add to model

				Class<?> clazz = null;

				try
				{
					clazz = ClassUtil.loadClassWithoutInitialising(metaClassName);

					if (clazz != null)
					{
						metaClass = model.buildAndGetMetaClass(clazz);
					}
				}
				catch (ClassNotFoundException cnf)
				{
					logger.error("ClassNotFoundException: '" + metaClassName + C_QUOTE);
				}
				catch (NoClassDefFoundError ncdf)
				{
					logger.error("NoClassDefFoundError: '" + metaClassName + C_SPACE + ncdf.getMessage() + C_QUOTE);
				}
			}

			if (metaClass != null)
			{
				MemberSignatureParts msp = MemberSignatureParts.fromParts(metaClass.getFullyQualifiedName(), methodName,
						returnType, argumentTypes);
				result = metaClass.getMemberFromSignature(msp);
			}
			else
			{
				logger.error("metaClass not found: {}", metaClassName);
			}
		}

		return result;
	}

	public static String lookupType(String typeOrKlassID, IParseDictionary parseDictionary)
	{
		String result = null;

		if (typeOrKlassID != null)
		{
			Tag typeTag = parseDictionary.getType(typeOrKlassID);

			if (typeTag == null)
			{
				typeTag = parseDictionary.getKlass(typeOrKlassID);
			}

			if (typeTag != null)
			{
				result = typeTag.getAttribute(ATTR_NAME).replace(S_SLASH, S_DOT);

				result = ParseUtil.expandParameterType(result);
			}
		}

		return result;
	}

	public static String convertNativeCodeMethodName(String inLine)
	{
		String line = inLine.replace(S_ENTITY_APOS, S_QUOTE);

		Matcher matcher = PATTERN_ASSEMBLY_SIGNATURE.matcher(line);

		String result = null;

		if (matcher.find())
		{
			String memberName = matcher.group(2);
			String params = matcher.group(3).replace(S_SLASH, S_DOT);
			String className = matcher.group(4).replace(S_SLASH, S_DOT);

			StringBuilder builder = new StringBuilder();
			builder.append(className).append(C_SPACE);
			builder.append(memberName).append(C_SPACE);
			builder.append(params);

			result = builder.toString();
		}

		return result;
	}

	public static String getPackageFromSource(String source)
	{
		String result = null;

		String[] lines = source.split(S_NEWLINE);

		for (String line : lines)
		{
			line = line.trim();

			if (line.startsWith(S_PACKAGE) && line.endsWith(S_SEMICOLON))
			{
				result = line.substring(S_PACKAGE.length(), line.length() - 1).trim();
			}
		}

		if (result == null)
		{
			result = S_EMPTY;
		}

		return result;
	}

	public static String getClassFromSource(String source)
	{
		String result = null;

		String[] lines = source.split(S_NEWLINE);

		String classToken = S_SPACE + S_CLASS + S_SPACE;

		for (String line : lines)
		{
			line = line.trim();

			int classTokenPos = line.indexOf(classToken);

			if (classTokenPos != -1)
			{
				result = line.substring(classTokenPos + classToken.length());
			}
		}

		if (result == null)
		{
			result = "";
		}

		return result;
	}

	public static IMetaMember getMemberFromComment(IReadOnlyJITDataModel model, final String comment) throws LogParseException
	{
		// java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;

		String replace1 = comment.replace(C_DOT, C_SPACE);
		String replace2 = replace1.replace(C_COLON, C_SPACE);
		String replace3 = replace2.replace(C_SLASH, C_DOT);
		String replace4 = replace3.replace(S_DOUBLE_QUOTE, S_EMPTY);

		return ParseUtil.findMemberWithSignature(model, replace4);
	}
}
