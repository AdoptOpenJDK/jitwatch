/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_TYPE_NAME_VOID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.JITDataModel;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.model.MetaConstructor;
import org.adoptopenjdk.jitwatch.model.MetaMethod;
import org.adoptopenjdk.jitwatch.model.MetaPackage;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.util.ClassUtil;
import org.adoptopenjdk.jitwatch.util.ParseUtil;
import org.adoptopenjdk.jitwatch.util.StringUtil;
import org.junit.Test;

public class TestParseUtil
{
	@Test
	public void testSourceSignatureRegExMatcher()
	{
		// single primitive param, void return
		Method m = UnitTestUtil.getMethod("java.lang.AbstractStringBuilder", "ensureCapacity", new Class<?>[] { int.class });
		MetaMethod method = new MetaMethod(m, null);
		String sourceSig = "public void ensureCapacity(int foo)";
		Matcher matcher = Pattern.compile(method.getSourceMethodSignatureRegEx()).matcher(sourceSig);
		boolean match = matcher.find();
		assertTrue(match);

		// 2 primitive params,void return
		Method m2 = UnitTestUtil.getMethod("java.lang.AbstractStringBuilder", "setCharAt", new Class<?>[] { int.class, char.class });
		MetaMethod method2 = new MetaMethod(m2, null);
		String sourceSig2 = "public void setCharAt(int foo, char bar)";
		Matcher matcher2 = Pattern.compile(method2.getSourceMethodSignatureRegEx()).matcher(sourceSig2);
		boolean match2 = matcher2.find();
		assertTrue(match2);

		// Object param and return type
		Method m3 = UnitTestUtil.getMethod("java.lang.AbstractStringBuilder", "append", new Class<?>[] { java.lang.String.class });
		MetaMethod methodFQ = new MetaMethod(m3, null);
		String sourceSigFQ = "public AbstractStringBuilder append(String foo)";
		Matcher matcherFQ = Pattern.compile(methodFQ.getSourceMethodSignatureRegEx()).matcher(sourceSigFQ);
		boolean matchFQ = matcherFQ.find();
		assertTrue(matchFQ);

		// constructor with primitive params
		Constructor<?> c1 = UnitTestUtil.getConstructor("java.lang.AbstractStringBuilder", new Class<?>[] { int.class });
		MetaConstructor con1 = new MetaConstructor(c1, null);
		String sourceSigC1 = "AbstractStringBuilder(int foo)";
		Matcher matcherC1 = Pattern.compile(con1.getSourceMethodSignatureRegEx()).matcher(sourceSigC1);
		boolean matchC1 = matcherC1.find();
		assertTrue(matchC1);

		// array return type, no params
		Method m4 = UnitTestUtil.getMethod("java.lang.String", "getBytes", new Class<?>[0]);
		MetaMethod method4 = new MetaMethod(m4, null);
		String sourceSig4 = "public byte[] getBytes()";
		Matcher matcher4 = Pattern.compile(method4.getSourceMethodSignatureRegEx()).matcher(sourceSig4);
		boolean match4 = matcher4.find();
		assertTrue(match4);

		// array param and object return type
		Method m5 = UnitTestUtil.getMethod("java.lang.AbstractStringBuilder", "append", new Class<?>[] { char[].class });
		MetaMethod method5 = new MetaMethod(m5, null);
		String sourceSig5 = "public AbstractStringBuilder append(char[] foo)";
		Matcher matcher5 = Pattern.compile(method5.getSourceMethodSignatureRegEx()).matcher(sourceSig5);
		boolean match5 = matcher5.find();
		assertTrue(match5);
	}

	@Test
	public void testRegressionJavaUtilPropertiesLoadConvert() // space before
																// parentheses
	{
		Method m = UnitTestUtil.getMethod("java.util.Properties", "loadConvert", new Class<?>[] { char[].class, int.class, int.class,
				char[].class });
		MetaMethod method = new MetaMethod(m, null);

		String sourceSig = "private String loadConvert (char[] in, int off, int len, char[] convtBuf) {";
		Matcher matcher = Pattern.compile(method.getSourceMethodSignatureRegEx()).matcher(sourceSig);
		boolean match = matcher.find();
		assertTrue(match);
	}

	@Test
	public void testSignatureMatchFailWithGenerics()
	{
		// java.util.Arrays
		// public static <U,T> T[] copyOf(U[] original, int newLength, Class<?
		// extends T[]> newType) {

		Method m = UnitTestUtil.getMethod("java.util.Arrays", "copyOf", new Class<?>[] { Object[].class, int.class, Class.class });
		MetaMethod method = new MetaMethod(m, null);

		// test for failure on matching internal (type erased) representation
		// against generics signature
		String sourceSig = "public static <U,T> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {";
		Matcher matcher = Pattern.compile(method.getSourceMethodSignatureRegEx()).matcher(sourceSig);
		boolean match = matcher.find();
		assertFalse(match);
	}

	@Test
	public void testFindBestLineMatchForMemberSignature()
	{
		Method m = UnitTestUtil.getMethod("java.util.Arrays", "copyOf", new Class<?>[] { Object[].class, int.class, Class.class });

		MetaClass metaClass = new MetaClass(null, "java.util.arrays");
		IMetaMember member = new MetaMethod(m, metaClass);

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
		Method m = UnitTestUtil.getMethod("java.util.Arrays", "copyOf", new Class<?>[] { Object[].class, int.class, Class.class });

		MetaClass metaClass = new MetaClass(null, "java.util.arrays");
		IMetaMember member = new MetaMethod(m, metaClass);

		List<String> srcLinesList = new ArrayList<>();

		srcLinesList.add("public static <T extends java/lang/Object> T[] copyOf(T[], int);");
		srcLinesList
				.add("public static <T extends java/lang/Object, U extends java/lang/Object> T[] copyOf(U[], int, java.lang.Class<? extends T[]>);");
		srcLinesList.add("public static byte[] copyOf(byte[], int);");
		srcLinesList
				.add("public static <T extends java/lang/Object, U extends java/lang/Object> T[] copyOfRange(U[], int, int, java.lang.Class<? extends T[]>);");

		int bestMatchPos = ParseUtil.findBestLineMatchForMemberSignature(member, srcLinesList);

		assertEquals(1, bestMatchPos);
	}

	@Test
	public void testFindBestLineMatchForMemberSignatureBytecodeRegression()
	{
		Method m = UnitTestUtil.getMethod("java.util.Arrays", "copyOf", new Class<?>[] { Object[].class, int.class });

		MetaClass metaClass = new MetaClass(null, "java.util.arrays");
		IMetaMember member = new MetaMethod(m, metaClass);

		List<String> srcLinesList = new ArrayList<>();

		srcLinesList
				.add("public static <T extends java/lang/Object, U extends java/lang/Object> T[] copyOf(U[], int, java.lang.Class<? extends T[]>);");
		srcLinesList.add("public static byte[] copyOf(byte[], int);");
		srcLinesList.add("public static byte[] copyOf(float[], int);");
		srcLinesList
				.add("public static <T extends java/lang/Object, U extends java/lang/Object> T[] copyOfRange(U[], int, int, java.lang.Class<? extends T[]>);");
		srcLinesList.add("public static <T extends java/lang/Object> T[] copyOf(T[], int);");

		int bestMatchPos = ParseUtil.findBestLineMatchForMemberSignature(member, srcLinesList);

		assertEquals(4, bestMatchPos);
	}

	@Test
	public void testMemberSignaturePartsPrimitiveParamPrimitiveReturn() throws LogParseException
	{
		MemberSignatureParts msp = MemberSignatureParts.fromLogCompilationSignature("java.lang.String charAt (I)C");

		assertEquals("java.lang.String", msp.getFullyQualifiedClassName());
		assertEquals("charAt", msp.getMemberName());
		assertEquals("char", msp.getReturnType());
		assertEquals(1, msp.getParamTypes().size());
		assertEquals("int", msp.getParamTypes().get(0));
	}

	@Test
	public void testMemberSignaturePartsConstructor() throws LogParseException
	{
		MemberSignatureParts msp = MemberSignatureParts.fromLogCompilationSignature("java.lang.Object <init> ()V");

		assertEquals("java.lang.Object", msp.getFullyQualifiedClassName());
		assertEquals("Object", msp.getMemberName());
		assertEquals(S_TYPE_NAME_VOID, msp.getReturnType());
		assertEquals(0, msp.getParamTypes().size());
	}

	@Test
	public void testMemberSignaturePartsPrimitiveParamVoidReturn() throws LogParseException
	{
		MemberSignatureParts msp = MemberSignatureParts
				.fromLogCompilationSignature("java.lang.AbstractStringBuilder ensureCapacityInternal (I)V");

		assertEquals("java.lang.AbstractStringBuilder", msp.getFullyQualifiedClassName());
		assertEquals("ensureCapacityInternal", msp.getMemberName());
		assertEquals(S_TYPE_NAME_VOID, msp.getReturnType());
		assertEquals(1, msp.getParamTypes().size());
		assertEquals("int", msp.getParamTypes().get(0));
	}

	@Test
	public void testMemberSignaturePartsPrimitiveParamObjectReturn() throws LogParseException
	{
		MemberSignatureParts msp = MemberSignatureParts
				.fromLogCompilationSignature("java.lang.AbstractStringBuilder append (Z)Ljava.lang.AbstractStringBuilder;");

		assertEquals("java.lang.AbstractStringBuilder", msp.getFullyQualifiedClassName());
		assertEquals("append", msp.getMemberName());
		assertEquals("java.lang.AbstractStringBuilder", msp.getReturnType());
		assertEquals(1, msp.getParamTypes().size());
		assertEquals("boolean", msp.getParamTypes().get(0));
	}

	@Test
	public void testMemberSignaturePartsMultiDimensionalArrayParamPrimitiveReturn() throws LogParseException
	{
		MemberSignatureParts msp = MemberSignatureParts
				.fromLogCompilationSignature("com.sun.org.apache.xerces.internal.dom.DeferredDocumentImpl setChunkIndex ([[IIII)I");

		assertEquals("com.sun.org.apache.xerces.internal.dom.DeferredDocumentImpl", msp.getFullyQualifiedClassName());
		assertEquals("setChunkIndex", msp.getMemberName());
		assertEquals("int", msp.getReturnType());
		assertEquals(4, msp.getParamTypes().size());
		assertEquals("[[I", msp.getParamTypes().get(0));
		assertEquals("int", msp.getParamTypes().get(1));
		assertEquals("int", msp.getParamTypes().get(2));
		assertEquals("int", msp.getParamTypes().get(3));
	}

	@Test
	public void testMemberSignaturePartsClassIsArrayClone() throws LogParseException
	{
		MemberSignatureParts msp = MemberSignatureParts
				.fromLogCompilationSignature("[Ljava.lang.String; clone ()Ljava.lang.Object;");

		assertEquals("[Ljava.lang.String;", msp.getFullyQualifiedClassName());
		assertEquals("clone", msp.getMemberName());
		assertEquals("java.lang.Object", msp.getReturnType());
		assertEquals(0, msp.getParamTypes().size());
	}
	
	@Test
	public void testMemberSignaturePartsClassHasUnderscores() throws LogParseException
	{
		MemberSignatureParts msp = MemberSignatureParts
				.fromLogCompilationSignature("org.omg.CORBA_2_3.portable.ObjectImpl <init> ()V");

		assertEquals("org.omg.CORBA_2_3.portable.ObjectImpl", msp.getFullyQualifiedClassName());
		assertEquals("ObjectImpl", msp.getMemberName());
		assertEquals(S_TYPE_NAME_VOID, msp.getReturnType());
		assertEquals(0, msp.getParamTypes().size());
	}	
	
	// test varargs method
	public void doSomethingWithVarArgs(String... args)
	{
		// DO NOT REMOVE, NEEDED BY UNIT TEST
	}

	// test varargs method
	public void doSomethingWithParamAndVarArgs(int foo, String... args)
	{
		// DO NOT REMOVE, NEEDED BY UNIT TEST
	}

	// test varargs method
	public void method_with_underscores()
	{
		// DO NOT REMOVE, NEEDED BY UNIT TEST
	}

	@Test
	public void testVarArgsInBytecodeSignatureMatches()
	{
		String coreClassWithVarArgs = getClass().getName();

		JITDataModel model = new JITDataModel();

		MetaClass metaClass = null;

		try
		{
			metaClass = model.buildAndGetMetaClass(ClassUtil.loadClassWithoutInitialising(coreClassWithVarArgs));
		}
		catch (ClassNotFoundException cnfe)
		{
			cnfe.printStackTrace();
			fail();
		}

		String bytecodeSig = "public void doSomethingWithVarArgs(java.lang.String...)";

		MemberSignatureParts msp = MemberSignatureParts.fromBytecodeSignature("org.adoptopenjdk.jitwatch.test.TestParseUtil",
				bytecodeSig);

		IMetaMember foundVarArgsMethod = metaClass.getMemberForSignature(msp);

		assertNotNull(foundVarArgsMethod);
	}

	@Test
	public void testMethodWithUnderscores()
	{
		String thisClass = getClass().getName();

		JITDataModel model = new JITDataModel();

		MetaClass metaClass = null;

		try
		{
			metaClass = model.buildAndGetMetaClass(ClassUtil.loadClassWithoutInitialising(thisClass));
		}
		catch (ClassNotFoundException cnfe)
		{
			cnfe.printStackTrace();
			fail();
		}

		String bytecodeSig = "public void method_with_underscores()";

		MemberSignatureParts msp = MemberSignatureParts.fromBytecodeSignature("org.adoptopenjdk.jitwatch.test.TestParseUtil",
				bytecodeSig);

		IMetaMember foundVarArgsMethod = metaClass.getMemberForSignature(msp);

		assertNotNull(foundVarArgsMethod);
	}

	@Test
	public void testMethodWithInnerClassDollarSign()
	{
		String awtWindowClass = "java.awt.Window";

		JITDataModel model = new JITDataModel();

		MetaClass metaClass = null;

		try
		{
			metaClass = model.buildAndGetMetaClass(ClassUtil.loadClassWithoutInitialising(awtWindowClass));
		}
		catch (ClassNotFoundException cnfe)
		{
			cnfe.printStackTrace();
			fail();
		}

		String bytecodeSig = "static int access$600(java.awt.Window)";

		MemberSignatureParts msp = MemberSignatureParts.fromBytecodeSignature(awtWindowClass, bytecodeSig);

		IMetaMember foundVarArgsMethod = metaClass.getMemberForSignature(msp);

		assertNotNull(foundVarArgsMethod);
	}

	@Test
	public void testBadParseThrowsException()
	{
		try
		{
			MemberSignatureParts.fromLogCompilationSignature("hkl sdfkghlkshdfglkh sfkldgh ksh dfg");

			fail();
		}
		catch (LogParseException lpe)
		{

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

		Method m = UnitTestUtil.getMethod(className, methodName, new Class<?>[] { int.class });
		MetaMethod method = new MetaMethod(m, null);

		String uqToString = method.toStringUnqualifiedMethodName(true, false);

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

		MetaClass metaClass = new MetaClass(metaPackage, StringUtil.getUnqualifiedClassName(thisClassName));

		String testMethodName = "unicodeMethodNameµµµµµ";

		Method method = getClass().getDeclaredMethod(testMethodName, new Class[0]);

		MetaMethod testMethod = new MetaMethod(method, metaClass);

		String sourceSig = "public void unicodeMethodNameµµµµµ()";

		Matcher matcher = Pattern.compile(testMethod.getSourceMethodSignatureRegEx()).matcher(sourceSig);
		boolean match = matcher.find();
		assertTrue(match);
	}

	// tests for the regex used to parse the hotspot.log method signature format
	// format is
	// class method(params)return
	// e.g.
	// java.util.ArrayList elementData (I)Ljava.lang.Object;

	@Test
	public void testASCIILogSignatures() throws Exception
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
	public void testNonASCIILogSignatures() throws Exception
	{
		String sig = "org.adoptopenjdk.jitwatch.test.TestParser unicodeMethodNameµµµµµ (V)V";
		String[] parts = ParseUtil.splitLogSignatureWithRegex(sig);

		assertNotNull(parts);
		assertEquals("org.adoptopenjdk.jitwatch.test.TestParser", parts[0]);
		assertEquals("unicodeMethodNameµµµµµ", parts[1]);
		assertEquals("V", parts[2]);
		assertEquals("V", parts[3]);
	}

	@Test
	public void testNonASCIILogSignaturesRegression() throws Exception
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
	public void testASCIILogSignaturesArrayWithArrayParam() throws Exception
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
	public void testEclipseLogParseRegression() throws Exception
	{
		String sig = "org.eclipse.e4.ui.css.swt.engine.AbstractCSSSWTEngineImpl getElement (Ljava.lang.Object;)Lorg.w3c.dom.Element;";

		String[] parts = ParseUtil.splitLogSignatureWithRegex(sig);

		assertNotNull(parts);
		assertEquals("org.eclipse.e4.ui.css.swt.engine.AbstractCSSSWTEngineImpl", parts[0]);
		assertEquals("getElement", parts[1]);
		assertEquals("Ljava.lang.Object;", parts[2]);
		assertEquals("Lorg.w3c.dom.Element;", parts[3]);
	}

	@Test
	public void testParseMemberFromBytecodeInvokeCommentConstructor() throws Exception
	{
		String comment1 = "java/lang/Object.\"<init>\":()V";

		JITDataModel model = new JITDataModel();

		model.buildAndGetMetaClass(Object.class);

		BytecodeInstruction instruction = new BytecodeInstruction();
		instruction.setComment(comment1);

		IMetaMember member1 = ParseUtil.getMemberFromBytecodeComment(model, null, instruction);

		assertEquals("public java.lang.Object()", member1.toString());
	}

	@Test
	public void testParseMemberFromBytecodeInvokeCommentObjectReturn() throws Exception
	{
		String comment2 = "java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;";

		JITDataModel model = new JITDataModel();

		model.buildAndGetMetaClass(StringBuilder.class);

		BytecodeInstruction instruction = new BytecodeInstruction();
		instruction.setComment(comment2);

		IMetaMember member2 = ParseUtil.getMemberFromBytecodeComment(model, null, instruction);

		assertEquals("public java.lang.StringBuilder java.lang.StringBuilder.append(int)", member2.toString());
	}

	@Test
	public void testParseMemberFromBytecodeInvokeCommentPrimitive() throws Exception
	{
		String comment3 = "org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog.chainA1:(J)J";

		JITDataModel model = new JITDataModel();

		model.buildAndGetMetaClass(MakeHotSpotLog.class);

		BytecodeInstruction instruction = new BytecodeInstruction();
		instruction.setComment(comment3);

		IMetaMember member3 = ParseUtil.getMemberFromBytecodeComment(model, null, instruction);

		assertEquals("private long org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog.chainA1(long)", member3.toString());
	}

	@Test
	public void testLambdaSignatureRegression() throws Exception
	{
		String sig = "uk.co.foo.bar.Anonymised$$Lambda$40 applyAsInt (Ljava.lang.Object;)I";

		String[] parts = ParseUtil.splitLogSignatureWithRegex(sig);

		assertNotNull(parts);
		assertEquals("uk.co.foo.bar.Anonymised$$Lambda$40", parts[0]);
		assertEquals("applyAsInt", parts[1]);
		assertEquals("Ljava.lang.Object;", parts[2]);
		assertEquals("I", parts[3]);

		JITDataModel model = new JITDataModel();

		try
		{
			ParseUtil.findMemberWithSignature(model, sig);
			fail();
		}
		catch (LogParseException lpe)
		{
		}
	}

	@Test
	public void testFindClassForLogCompilationParameter() throws Exception
	{
		assertEquals(Class.forName("java.lang.String"), ParseUtil.findClassForLogCompilationParameter("java.lang.String"));

		assertEquals(Class.forName("[Ljava.lang.String;"), ParseUtil.findClassForLogCompilationParameter("java.lang.String[]"));

		assertEquals(Class.forName("[[Ljava.lang.String;"), ParseUtil.findClassForLogCompilationParameter("java.lang.String[][]"));

		assertEquals(Class.forName("[Ljava.lang.String;"), ParseUtil.findClassForLogCompilationParameter("java.lang.String..."));

		assertEquals(int.class, ParseUtil.findClassForLogCompilationParameter("int"));

		assertEquals(Class.forName("[I"), ParseUtil.findClassForLogCompilationParameter("int[]"));

		assertEquals(Class.forName("[[I"), ParseUtil.findClassForLogCompilationParameter("int[][]"));
	}

	@Test
	public void testFindClassForLogCompilationParameterRegressionForGenerics() throws Exception
	{
		assertEquals(Class.forName("java.util.List"), ParseUtil.findClassForLogCompilationParameter("java.util.List<?>"));
		assertEquals(Class.forName("java.util.List"), ParseUtil.findClassForLogCompilationParameter("java.util.List<T>"));
	}

	@Test
	public void testStripGenerics()
	{
		assertEquals("int", ParseUtil.stripGenerics("int"));
		assertEquals("java.util.List", ParseUtil.stripGenerics("java.util.List"));
		assertEquals("java.util.List", ParseUtil.stripGenerics("java.util.List<T>"));
		assertEquals("java.util.List", ParseUtil.stripGenerics("java.util.List<Class<T>>"));
		assertEquals("java.util.List", ParseUtil.stripGenerics("java.util.List<? super T>"));
		assertEquals("java.util.List[]", ParseUtil.stripGenerics("java.util.List<? super T>[]"));
		assertEquals("java.util.List[]", ParseUtil.stripGenerics("java.util.List<?>[]"));
	}

	@Test
	public void paramClassesMatchPrimitiveNone()
	{
		List<Class<?>> memberClassList = new ArrayList<>();
		List<Class<?>> sigclassList = new ArrayList<>();

		assertTrue(ParseUtil.paramClassesMatch(false, memberClassList, sigclassList, true));
	}

	@Test
	public void paramClassesMatchPrimitiveSingle()
	{
		List<Class<?>> memberClassList = new ArrayList<>();
		List<Class<?>> sigclassList = new ArrayList<>();

		memberClassList.add(int.class);
		sigclassList.add(int.class);

		assertTrue(ParseUtil.paramClassesMatch(false, memberClassList, sigclassList, true));
	}

	@Test
	public void paramClassesMatchPrimitiveMultiple()
	{
		List<Class<?>> memberClassList = new ArrayList<>();
		List<Class<?>> sigclassList = new ArrayList<>();

		memberClassList.add(int.class);
		memberClassList.add(float.class);
		memberClassList.add(double.class);
		memberClassList.add(long.class);

		sigclassList.add(int.class);
		sigclassList.add(float.class);
		sigclassList.add(double.class);
		sigclassList.add(long.class);

		assertTrue(ParseUtil.paramClassesMatch(false, memberClassList, sigclassList, true));
	}

	@Test
	public void paramClassesMatchObjectSingle()
	{
		List<Class<?>> memberClassList = new ArrayList<>();
		List<Class<?>> sigclassList = new ArrayList<>();

		memberClassList.add(java.lang.String.class);
		sigclassList.add(java.lang.String.class);

		assertTrue(ParseUtil.paramClassesMatch(false, memberClassList, sigclassList, true));
	}

	@Test
	public void paramClassesMatchObjectMultiple()
	{
		List<Class<?>> memberClassList = new ArrayList<>();
		List<Class<?>> sigclassList = new ArrayList<>();

		memberClassList.add(java.lang.String.class);
		memberClassList.add(java.lang.Integer.class);
		memberClassList.add(java.lang.Double.class);

		sigclassList.add(java.lang.String.class);
		sigclassList.add(java.lang.Integer.class);
		sigclassList.add(java.lang.Double.class);

		assertTrue(ParseUtil.paramClassesMatch(false, memberClassList, sigclassList, true));
	}

	@Test
	public void paramClassesMatchObjectAssignableFromExactMatch()
	{
		List<Class<?>> memberClassList = new ArrayList<>();
		List<Class<?>> sigclassList = new ArrayList<>();

		memberClassList.add(java.lang.Object.class);

		sigclassList.add(java.lang.String.class);

		assertFalse(ParseUtil.paramClassesMatch(false, memberClassList, sigclassList, true));
	}

	@Test
	public void paramClassesMatchObjectAssignableFrom()
	{
		List<Class<?>> memberClassList = new ArrayList<>();
		List<Class<?>> sigclassList = new ArrayList<>();

		memberClassList.add(java.lang.Object.class);

		sigclassList.add(java.lang.String.class);

		assertTrue(ParseUtil.paramClassesMatch(false, memberClassList, sigclassList, false));
	}

	@Test
	public void paramClassesMatchObjectVarArgs() throws Exception
	{
		List<Class<?>> memberClassList = new ArrayList<>();
		List<Class<?>> sigclassList = new ArrayList<>();

		memberClassList.add(Class.forName("[Ljava.lang.Object;"));

		sigclassList.add(java.lang.Object.class);
		sigclassList.add(java.lang.Object.class);
		sigclassList.add(java.lang.Object.class);

		assertTrue(ParseUtil.paramClassesMatch(true, memberClassList, sigclassList, true));
	}

	@Test
	public void paramClassesMatchObjectVarArgsAssignable() throws Exception
	{
		List<Class<?>> memberClassList = new ArrayList<>();
		List<Class<?>> sigclassList = new ArrayList<>();

		memberClassList.add(Class.forName("[Ljava.lang.Object;"));

		sigclassList.add(java.lang.String.class);
		sigclassList.add(java.lang.Float.class);
		sigclassList.add(java.lang.Integer.class);

		assertTrue(ParseUtil.paramClassesMatch(true, memberClassList, sigclassList, true));
	}

	@Test
	public void testMethodWithPolymorphicSignature() throws Exception
	{
		JITDataModel model = new JITDataModel();
		model.buildAndGetMetaClass(java.lang.invoke.MethodHandle.class);

		MemberSignatureParts msp = MemberSignatureParts
				.fromLogCompilationSignature("java.lang.invoke.MethodHandle linkToStatic (Ljava.lang.Object;Ljava.lang.invoke.MemberName;)V");

		IMetaMember member = model.findMetaMember(msp);

		assertNotNull(member);
	}

	@Test
	public void testRegressionStringIndexOf() throws Exception
	{
		JITDataModel model = new JITDataModel();
		model.buildAndGetMetaClass(java.lang.String.class);

		MemberSignatureParts msp = MemberSignatureParts.fromLogCompilationSignature("java.lang.String indexOf (II)I");

		IMetaMember member = model.findMetaMember(msp);

		assertNotNull(member);
	}

	@Test
	public void testRegressionStringToUpper() throws Exception
	{
		JITDataModel model = new JITDataModel();
		model.buildAndGetMetaClass(java.lang.String.class);

		MemberSignatureParts mspToUpperCase = MemberSignatureParts.fromBytecodeSignature("java.lang.String",
				"  public java.lang.String toUpperCase();");

		IMetaMember memberToUpperCase = model.findMetaMember(mspToUpperCase);

		assertNotNull(memberToUpperCase);

		assertEquals(0, memberToUpperCase.getParamTypeNames().length);
	}

	@Test
	public void testRegressionStringToUpperLocale() throws Exception
	{
		JITDataModel model = new JITDataModel();
		model.buildAndGetMetaClass(java.lang.String.class);

		MemberSignatureParts mspToUpperCaseLocale = MemberSignatureParts.fromBytecodeSignature("java.lang.String",
				"  public java.lang.String toUpperCase(java.util.Locale);");

		IMetaMember memberToUpperCaseLocale = model.findMetaMember(mspToUpperCaseLocale);

		assertNotNull(memberToUpperCaseLocale);

		assertEquals(1, memberToUpperCaseLocale.getParamTypeNames().length);
		assertEquals("java.util.Locale", memberToUpperCaseLocale.getParamTypeNames()[0]);
	}

	@Test
	public void testValhallaRegressionBytecodeNameContainsCharacterNotValidAsJavaName() throws Exception
	{
		String sig = "ArrayList${0=I} ensureCapacity (I)V";

		String[] parts = ParseUtil.splitLogSignatureWithRegex(sig);

		assertNotNull(parts);
		assertEquals("ArrayList${0=I}", parts[0]);
		assertEquals("ensureCapacity", parts[1]);
		assertEquals("I", parts[2]);
		assertEquals("V", parts[3]);
	}
	
	@Test
	public void testGetClassTypesPrimitive() throws LogParseException
	{
		Class<?>[] result = ParseUtil.getClassTypes("I");
		
		assertEquals(1, result.length);
		assertEquals(int.class, result[0]);
	}
	
	@Test
	public void testGetClassTypesClass() throws LogParseException
	{
		Class<?>[] result = ParseUtil.getClassTypes("Ljava.lang.String;");
		
		assertEquals(1, result.length);
		assertEquals(java.lang.String.class, result[0]);
	}
	
	@Test
	public void testGetClassTypesPrimitiveArray() throws LogParseException
	{
		Class<?>[] result = ParseUtil.getClassTypes("[C");
		
		assertEquals(1, result.length);
		assertEquals(char[].class, result[0]);
	}
	
	@Test
	public void testGetClassTypesClassArray() throws LogParseException
	{
		Class<?>[] result = ParseUtil.getClassTypes("[Ljava.math.BigInteger;");
		
		assertEquals(1, result.length);
		assertEquals(java.math.BigInteger[].class, result[0]);
	}
	
	@Test
	public void testExpandParseDictionaryTypeNameClass() throws LogParseException
	{
		String result = ParseUtil.expandParseDictionaryTypeName("java/math/BigInteger");
		
		assertEquals("java.math.BigInteger", result);
	}
	
	@Test
	public void testExpandParseDictionaryTypeNamePrimitive() throws LogParseException
	{
		String result = ParseUtil.expandParseDictionaryTypeName("int");
		
		assertEquals("int", result);
	}
	
	@Test
	public void testExpandParseDictionaryTypeNamePrimitiveArray() throws LogParseException
	{
		String result = ParseUtil.expandParseDictionaryTypeName("[C");
		
		assertEquals("char[]", result);
	}
	
	@Test
	public void testMemberSignaturePartsNativeSignature() throws LogParseException
	{
		MemberSignatureParts msp = MemberSignatureParts
				.fromLogCompilationSignature("java.lang.System arraycopy (Ljava/lang/Object;ILjava/lang/Object;II)V");

		assertEquals("java.lang.System", msp.getFullyQualifiedClassName());
		assertEquals("arraycopy", msp.getMemberName());
		assertEquals("void", msp.getReturnType());
		assertEquals(5, msp.getParamTypes().size());
		assertEquals("java.lang.Object", msp.getParamTypes().get(0));
		assertEquals("int", msp.getParamTypes().get(1));
		assertEquals("java.lang.Object", msp.getParamTypes().get(2));
		assertEquals("int", msp.getParamTypes().get(3));
		assertEquals("int", msp.getParamTypes().get(4));
	}
}