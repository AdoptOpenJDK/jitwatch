/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.util;

import com.chrisnewland.jitwatch.model.*;

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

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

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

	public static final char TYPE_SHORT = 'S';
	public static final char TYPE_CHARACTER = 'C';
	public static final char TYPE_BYTE = 'B';
	public static final char TYPE_VOID = 'V';
	public static final char TYPE_LONG = 'J';
	public static final char TYPE_DOUBLE = 'D';
	public static final char TYPE_BOOLEAN = 'Z';
	public static final char TYPE_INTEGER = 'I';
	public static final char TYPE_FLOAT = 'F';

    /*
        Hide Utility Class Constructor
        Utility classes should not have a public or default constructor.
    */
    private ParseUtil() {
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

	public static String[] splitLogSignatureWithRegex(String logSignature)
	{
		Matcher matcher = PATTERN_LOG_SIGNATURE.matcher(logSignature);

		String[] parts = null;

		if (matcher.find())
		{
			String className = matcher.group(1);
			String methodName = matcher.group(2);
			String paramTypes = matcher.group(3).replace(S_OPEN_PARENTHESES, S_EMPTY).replace(S_CLOSE_PARENTHESES, S_EMPTY);
			String returnType = matcher.group(4);

			parts = new String[] { className, methodName, paramTypes, returnType };
		}

		return parts;
	}

	/*
	 * Parses a log file signature into a class name and java declaration-style
	 * method signature
	 * 
	 * @return String[] 0=className 1=methodSignature
	 */
	public static String[] parseLogSignature(String logSignature) throws Exception {
		String result[] = null;

		String[] parts = splitLogSignatureWithRegex(logSignature);

		if (parts != null)
		{
			String className = parts[0];
			String methodName = parts[1];
			String paramTypes = parts[2];
			String returnType = parts[3];

			Class<?>[] paramClasses = ParseUtil.getClassTypes(paramTypes);
			Class<?>[] returnClasses = ParseUtil.getClassTypes(returnType);

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
                calculateTagPosition(types, classes, typeLen, builder);
			}
			catch (ClassNotFoundException cnf)
			{
                logger.error("ClassNotFoundException:", cnf);
				throw new Exception("ClassNotFoundException: " + builder.toString());
			}
			catch (NoClassDefFoundError ncdf)
			{
                logger.error("NoClassDefFoundError:", ncdf);
                throw new Exception("NoClassDefFoundError: " + builder.toString());
			}
			catch (Exception ex)
			{
                logger.error("Exception:", ex);
                throw new Exception("Exception: " + ex.getMessage());
			}
			catch (Error err)
			{
                logger.error("Error:", err);
                throw new Exception("Error: " + err.getMessage());
			}

		} // end if empty

		return classes.toArray(new Class<?>[classes.size()]);
	}

    private static void calculateTagPosition(String types, List<Class<?>> classes,
                                             int typeLen,
                                             StringBuilder builder) throws ClassNotFoundException {
        int pos = 0;

        while (pos < types.length())
        {
            char c = types.charAt(pos);

            switch (c)
            {
            case C_OPEN_SQUARE_BRACKET:
                pos = parseCOpenSquareBracket(types, classes, typeLen, builder, pos, c);
                break;
            case C_OBJECT_REF:
                // ref type
                pos = parseCObjectRef(types, classes, typeLen, builder, pos);
                break;
            default:
                // primitive
                pos = parsePrimitive(classes, pos, c);
                break;
            } // end switch

        } // end while
    }

    private static int parsePrimitive(List<Class<?>> classes, int pos, char c) {
        Class<?> primitiveClass = ParseUtil.getPrimitiveClass(c);
        classes.add(primitiveClass);
        pos++;
        return pos;
    }

    private static int parseCObjectRef(String types,
                                       List<Class<?>> classes,
                                       int typeLen,
                                       StringBuilder builder,
                                       int inPos) throws ClassNotFoundException {
        // ref type
        int pos = inPos;
        char c;
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
        return pos;
    }

    private static int parseCOpenSquareBracket(String types,
                                               List<Class<?>> classes,
                                               int typeLen,
                                               StringBuilder builder,
                                               int inPos,
                                               char inC) throws ClassNotFoundException {
        // Could be
        // [Ljava.lang.String; Object array
        // [I primitive array
        // [..[I multidimensional primitive array
        // [..[Ljava.lang.String multidimensional Object array
        char c = inC;
        int pos = inPos;

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
        return pos;
    }

    public static String findBestMatchForMemberSignature(IMetaMember member, List<String> lines)
	{
		int index = findBestLineMatchForMemberSignature(member, lines);

		String match = null;

		if (index > 0 && index < lines.size())
		{
			match = lines.get(index);
		}

		return match;
	}

	public static int findBestLineMatchForMemberSignature(IMetaMember member, List<String> lines)
	{
		String memberName = member.getMemberName();
		int modifier = member.getModifier();
		String returnTypeName = member.getReturnTypeName();
		String[] paramTypeNames = member.getParamTypeNames();

		int bestScoreLine = 0;
		int bestScore = 0;

		for (int i = 0; i < lines.size(); i++)
		{

			String line = lines.get(i);

			int score = 0;

			if (line.contains(memberName))
			{
				MemberSignatureParts msp = new MemberSignatureParts(line);

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

			if (metaClass != null)
			{
				result = metaClass.getMemberFromSignature(methodName, returnType, argumentTypes);
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
		String line = inLine.replace(ENTITY_APOS, S_QUOTE);

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
}
