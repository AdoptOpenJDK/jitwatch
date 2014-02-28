/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.util;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.MemberSignatureParts;

public class ParseUtil
{
	//http://stackoverflow.com/questions/68633/regex-that-will-match-a-java-method-declaration
	//http://stackoverflow.com/questions/4304928/unicode-equivalents-for-w-and-b-in-java-regular-expressions
	
	// class<SPACE>METHOD<SPACE>(PARAMS)RETURN
    private static final Pattern PATTERN_LOG_SIGNATURE = Pattern
            .compile("^([0-9\\p{L}\\.\\$_]+) ([0-9\\p{L}<>_\\$]+) (\\(.*\\))(.*)");

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
            System.err.println(pe.toString());
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
            
            parts = new String[]{className, methodName, paramTypes, returnType};
        }
        
        return parts;
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

    private static boolean compareTypeEquality(String memberTypeName, String mspTypeName, Map<String, String> genericsMap)
    {
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
}
