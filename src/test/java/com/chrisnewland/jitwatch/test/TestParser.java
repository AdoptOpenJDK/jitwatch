/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.test;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.MetaClass;
import com.chrisnewland.jitwatch.model.MetaConstructor;
import com.chrisnewland.jitwatch.model.MetaMethod;
import com.chrisnewland.jitwatch.model.MetaPackage;
import com.chrisnewland.jitwatch.util.ParseUtil;
import com.chrisnewland.jitwatch.util.StringUtil;

import static com.chrisnewland.jitwatch.test.UnitTestUtil.*;

public class TestParser
{
    @Test
    public void testSourceSignatureRegExMatcher()
    {
        // single primitive param, void return
        Method m = getMethod("java.lang.AbstractStringBuilder", "ensureCapacity", new Class<?>[] { int.class });
        MetaMethod method = new MetaMethod(m, null);
        String sourceSig = "public void ensureCapacity(int foo)";
        Matcher matcher = Pattern.compile(method.getSignatureRegEx()).matcher(sourceSig);
        boolean match = matcher.find();
        assertTrue(match);

        // 2 primitive params,void return
        Method m2 = getMethod("java.lang.AbstractStringBuilder", "setCharAt", new Class<?>[] { int.class, char.class });
        MetaMethod method2 = new MetaMethod(m2, null);
        String sourceSig2 = "public void setCharAt(int foo, char bar)";
        Matcher matcher2 = Pattern.compile(method2.getSignatureRegEx()).matcher(sourceSig2);
        boolean match2 = matcher2.find();
        assertTrue(match2);

        // Object param and return type
        Method m3 = getMethod("java.lang.AbstractStringBuilder", "append", new Class<?>[] { java.lang.String.class });
        MetaMethod methodFQ = new MetaMethod(m3, null);
        String sourceSigFQ = "public AbstractStringBuilder append(String foo)";
        Matcher matcherFQ = Pattern.compile(methodFQ.getSignatureRegEx()).matcher(sourceSigFQ);
        boolean matchFQ = matcherFQ.find();
        assertTrue(matchFQ);

        // constructor with primitive params
        Constructor<?> c1 = getConstructor("java.lang.AbstractStringBuilder", new Class<?>[] { int.class });
        MetaConstructor con1 = new MetaConstructor(c1, null);
        String sourceSigC1 = "AbstractStringBuilder(int foo)";
        Matcher matcherC1 = Pattern.compile(con1.getSignatureRegEx()).matcher(sourceSigC1);
        boolean matchC1 = matcherC1.find();
        assertTrue(matchC1);

        // array return type, no params
        Method m4 = getMethod("java.lang.AbstractStringBuilder", "getValue", new Class<?>[0]);
        MetaMethod method4 = new MetaMethod(m4, null);
        String sourceSig4 = "final char[] getValue()";
        Matcher matcher4 = Pattern.compile(method4.getSignatureRegEx()).matcher(sourceSig4);
        boolean match4 = matcher4.find();
        assertTrue(match4);

        // array param and object return type
        Method m5 = getMethod("java.lang.AbstractStringBuilder", "append", new Class<?>[] { char[].class });
        MetaMethod method5 = new MetaMethod(m5, null);
        String sourceSig5 = "public AbstractStringBuilder append(char[] foo)";
        Matcher matcher5 = Pattern.compile(method5.getSignatureRegEx()).matcher(sourceSig5);
        boolean match5 = matcher5.find();
        assertTrue(match5);
    }
    
    @Test
    public void testRegressionJavaUtilPropertiesLoadConvert() // space before parentheses
    {
        Method m = getMethod("java.util.Properties", "loadConvert", new Class<?>[] { char[].class, int.class, int.class, char[].class });
        MetaMethod method = new MetaMethod(m, null);
                
        String sourceSig = "private String loadConvert (char[] in, int off, int len, char[] convtBuf) {";
        Matcher matcher = Pattern.compile(method.getSignatureRegEx()).matcher(sourceSig);
        boolean match = matcher.find();
        assertTrue(match);
    }
    
    @Test
    public void testSignatureMatchFailWithGenerics()
    {
    	// java.util.Arrays
    	// public static <U,T> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
    	
        Method m = getMethod("java.util.Arrays", "copyOf", new Class<?>[] { Object[].class, int.class, Class.class });
        MetaMethod method = new MetaMethod(m, null);
                
        // test for failure on matching internal (type erased) representation against generics signature
        String sourceSig = "public static <U,T> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {";
        Matcher matcher = Pattern.compile(method.getSignatureRegEx()).matcher(sourceSig);
        boolean match = matcher.find();
        assertFalse(match);   	
    }
    
    @Test
    public void testFindBestLineMatchForMemberSignature()
    {
        Method m = getMethod("java.util.Arrays", "copyOf", new Class<?>[] { Object[].class, int.class, Class.class });
        IMetaMember member = new MetaMethod(m, null);
            	
    	List<String> srcLinesList = new ArrayList<>();
    	
    	srcLinesList.add("public static <T> T[] copyOf(T[] original, int newLength) {");
    	srcLinesList.add("	return (T[]) copyOf(original, newLength, original.getClass());");
    	srcLinesList.add("}");
    	srcLinesList.add("public static byte[] copyOf(byte[] original, int newLength) {");
    	srcLinesList.add("public static short[] copyOf(short[] original, int newLength) {");
    	srcLinesList.add("public static int[] copyOf(int[] original, int newLength) {");
    	srcLinesList.add("public static long[] copyOf(long[] original, int newLength) {");
    	srcLinesList.add("public static char[] copyOf(char[] original, int newLength) {");
    	srcLinesList.add("public static <T,U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {"); // best
    	srcLinesList.add("T[] copy = ((Object)newType == (Object)Object[].class)");
    	srcLinesList.add("? (T[]) new Object[newLength]");
    	srcLinesList.add(": (T[]) Array.newInstance(newType.getComponentType(), newLength);");
    	srcLinesList.add("System.arraycopy(original, 0, copy, 0,");
    	srcLinesList.add("Math.min(original.length, newLength));");
    	srcLinesList.add("return copy;");
    	srcLinesList.add("}");
    	srcLinesList.add("public static float[] copyOf(float[] original, int newLength) {");
    	srcLinesList.add("public static double[] copyOf(double[] original, int newLength) {");
    	srcLinesList.add("public static boolean[] copyOf(boolean[] original, int newLength) {");
    	srcLinesList.add("public static <T> T[] copyOfRange(T[] original, int from, int to) {");
    	srcLinesList.add("public static <T,U> T[] copyOfRange(U[] original, int from, int to, Class<? extends T[]> newType) {");
    	srcLinesList.add("public static byte[] copyOfRange(byte[] original, int from, int to) {");
    	
    	int bestMatchPos = ParseUtil.findBestLineMatchForMemberSignature(member, srcLinesList);
    	
    	assertEquals(8, bestMatchPos);
    }
    
    @Test
    public void testFindBestLineMatchForMemberSignatureBytecode()
    {
        Method m = getMethod("java.util.Arrays", "copyOf", new Class<?>[] { Object[].class, int.class, Class.class });
        IMetaMember member = new MetaMethod(m, null);
    	
    	List<String> srcLinesList = new ArrayList<>();
    	
    	srcLinesList.add("public static <T extends java/lang/Object> T[] copyOf(T[], int);");
    	srcLinesList.add("public static <T extends java/lang/Object, U extends java/lang/Object> T[] copyOf(U[], int, java.lang.Class<? extends T[]>);");
    	srcLinesList.add("public static byte[] copyOf(byte[], int);");
    	srcLinesList.add("public static <T extends java/lang/Object, U extends java/lang/Object> T[] copyOfRange(U[], int, int, java.lang.Class<? extends T[]>);");
        	
    	int bestMatchPos = ParseUtil.findBestLineMatchForMemberSignature(member, srcLinesList);
    	
    	assertEquals(1, bestMatchPos);
    }
    
    @Test
    public void testFindBestLineMatchForMemberSignatureBytecodeRegression()
    {
        Method m = getMethod("java.util.Arrays", "copyOf", new Class<?>[] { Object[].class, int.class});
        IMetaMember member = new MetaMethod(m, null);
    	
    	List<String> srcLinesList = new ArrayList<>();
    	
    	srcLinesList.add("public static <T extends java/lang/Object, U extends java/lang/Object> T[] copyOf(U[], int, java.lang.Class<? extends T[]>);");
    	srcLinesList.add("public static byte[] copyOf(byte[], int);");
    	srcLinesList.add("public static byte[] copyOf(float[], int);");
    	srcLinesList.add("public static <T extends java/lang/Object, U extends java/lang/Object> T[] copyOfRange(U[], int, int, java.lang.Class<? extends T[]>);");
    	srcLinesList.add("public static <T extends java/lang/Object> T[] copyOf(T[], int);");             
        	
    	int bestMatchPos = ParseUtil.findBestLineMatchForMemberSignature(member, srcLinesList);
    	
    	assertEquals(4, bestMatchPos);
    }
    
    @Test
    public void testBuildMethodSignature()
    {
        Map<String, String[]> expected = new HashMap<>();

        // primitive param and return
        expected.put("java.lang.String charAt (I)C", new String[] { "java.lang.String", "char java.lang.String.charAt(int)" });

        // constructor
        expected.put("java.lang.Object <init> ()V", new String[] { "java.lang.Object", "java.lang.Object()" });

        // void return
        expected.put("java.lang.AbstractStringBuilder ensureCapacityInternal (I)V", new String[] {
                "java.lang.AbstractStringBuilder", "void java.lang.AbstractStringBuilder.ensureCapacityInternal(int)" });

        // object return
        expected.put("java.lang.AbstractStringBuilder append (Z)Ljava.lang.AbstractStringBuilder;", new String[] {
                "java.lang.AbstractStringBuilder",
                "java.lang.AbstractStringBuilder java.lang.AbstractStringBuilder.append(boolean)" });

        // multidimensional param
        expected.put("com.sun.org.apache.xerces.internal.dom.DeferredDocumentImpl setChunkIndex ([[IIII)I", new String[] {
                "com.sun.org.apache.xerces.internal.dom.DeferredDocumentImpl",
                "int com.sun.org.apache.xerces.internal.dom.DeferredDocumentImpl.setChunkIndex(int[][],int,int,int)" });

        // invalid method
        expected.put("hkl sdfkghlkshdfglkh sfkldgh ksh dfg", null);

        try
        {
            for (Map.Entry<String, String[]> entry : expected.entrySet())
            {
                String[] expectedResult = entry.getValue();
                String[] actualResult = ParseUtil.parseLogSignature(entry.getKey());

                if (expectedResult == null)
                {
                    assertNull(actualResult);
                }
                else
                {
                    assertEquals(expectedResult[0], actualResult[0]);
                    assertEquals(expectedResult[1], actualResult[1]);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testExpandParameterType()
    {
        Map<String, String> expected = new HashMap<>();

        expected.put("I", "int");
        expected.put("C", "char");
        expected.put("Z", "boolean");
        expected.put("B", "byte");
        expected.put("S", "short");
        expected.put("J", "long");
        expected.put("F", "float");
        expected.put("D", "double");
        expected.put("Ljava.lang.String;", "java.lang.String");

        for (Map.Entry<String, String> entry : expected.entrySet())
        {
            assertEquals(entry.getValue(), ParseUtil.expandParameterType(entry.getKey()));
        }

        // 1D array
        for (Map.Entry<String, String> entry : expected.entrySet())
        {
            assertEquals(entry.getValue() + "[]", ParseUtil.expandParameterType("[" + entry.getKey()));
        }

        // 2D array
        for (Map.Entry<String, String> entry : expected.entrySet())
        {
            assertEquals(entry.getValue() + "[][]", ParseUtil.expandParameterType("[[" + entry.getKey()));
        }
    }

    @Test
    public void testModifierRegression()
    {
        // bad use of modifier was making volatile appear in method signatures
        
        String className = "java.lang.StringBuilder";
        String methodName = "charAt";

        Method m = getMethod(className, methodName, new Class<?>[] { int.class });
        MetaMethod method = new MetaMethod(m, null);

        String uqToString = method.toStringUnqualifiedMethodName(false);
        
        assertEquals(-1, uqToString.indexOf("volatile"));
    }
	
	public void unicodeMethodNameµµµµµ()
	{
	}
    
    @Test
    public void testNonASCIIMethod() throws NoSuchMethodException, SecurityException
    {
    	String thisClassName = getClass().getName();
    	
    	MetaPackage metaPackage = new MetaPackage(StringUtil.getPackageName(thisClassName));
    	
    	MetaClass metaClass = new MetaClass(metaPackage, StringUtil.makeUnqualified(thisClassName));
    	
    	String testMethodName = "unicodeMethodNameµµµµµ";
    	
    	Method method = getClass().getDeclaredMethod(testMethodName, new Class[0]);
    	    	
    	MetaMethod testMethod = new MetaMethod(method, metaClass);
                
        String sourceSig = "public void unicodeMethodNameµµµµµ()";
        
        Matcher matcher = Pattern.compile(testMethod.getSignatureRegEx()).matcher(sourceSig);
        boolean match = matcher.find();
        assertTrue(match);
    }
    
    // tests for the regex used to parse the hotspot.log method signature format
	// format is 
	// class method(params)return
	// e.g.
	// java.util.ArrayList elementData (I)Ljava.lang.Object;
    
    @Test
    public void testASCIILogSignatures()
    {
    	String sig = "java.util.ArrayList elementData (I)Ljava.lang.Object;";
    	String[] parts = ParseUtil.splitLogSignatureWithRegex(sig);
    	
    	assertNotNull(parts);
    	assertEquals("java.util.ArrayList", parts[0]);
    	assertEquals("elementData", parts[1]);
    	assertEquals("I", parts[2]);
    	assertEquals("Ljava.lang.Object;", parts[3]);   	
    }
    
    @Test
    public void testNonASCIILogSignatures()
    {
    	String sig = "com.chrisnewland.jitwatch.test.TestParser unicodeMethodNameµµµµµ (V)V";
    	String[] parts = ParseUtil.splitLogSignatureWithRegex(sig);
    	
    	assertNotNull(parts);
    	assertEquals("com.chrisnewland.jitwatch.test.TestParser", parts[0]);
    	assertEquals("unicodeMethodNameµµµµµ", parts[1]);
    	assertEquals("V", parts[2]);
    	assertEquals("V", parts[3]);    	
    }
    
    @Test
    public void testNonASCIILogSignaturesRegression()
    {
    	String sig = "frege.compiler.gen.Util layoutXS (Lfrege.lib.PP$TDoc;)Lfrege.prelude.PreludeBase$TList;";
    	String[] parts = ParseUtil.splitLogSignatureWithRegex(sig);
    	
    	assertNotNull(parts);
    	assertEquals("frege.compiler.gen.Util", parts[0]);
    	assertEquals("layoutXS", parts[1]);
    	assertEquals("Lfrege.lib.PP$TDoc;", parts[2]);
    	assertEquals("Lfrege.prelude.PreludeBase$TList;", parts[3]);    	
    }
    
    @Test
    public void testASCIILogSignaturesArrayWithArrayParam()
    {
    	String sig = "java.util.ComparableTimSort gallopLeft (Ljava.lang.Comparable;[Ljava.lang.Object;III)I";
    	
    	String[] parts = ParseUtil.splitLogSignatureWithRegex(sig);
    	
    	assertNotNull(parts);
    	assertEquals("java.util.ComparableTimSort", parts[0]);
    	assertEquals("gallopLeft", parts[1]);
    	assertEquals("Ljava.lang.Comparable;[Ljava.lang.Object;III", parts[2]);
    	assertEquals("I", parts[3]);   	
    }
    
    @Test
    public void testJava7DisassemblySignature()
    {
    	String sig = "# {method} &apos;chainA2&apos; &apos;(J)J&apos; in &apos;com/chrisnewland/jitwatch/demo/MakeHotSpotLog&apos;";
    	    	
    	String name = ParseUtil.convertNativeCodeMethodName(sig);
    	
    	assertEquals("com.chrisnewland.jitwatch.demo.MakeHotSpotLog chainA2 (J)J", name);
    }
    
    @Test
    public void testJava8DisassemblySignature()
    {
    	String sig = "  # {method} {0x00007fb6a89c4f80} &apos;hashCode&apos; &apos;()I&apos; in &apos;java/lang/String&apos;";
    
    	String name = ParseUtil.convertNativeCodeMethodName(sig);

    	assertEquals("java.lang.String hashCode ()I", name);
    }    
}