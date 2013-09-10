package com.chrisnewland.jitwatch.test;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.chrisnewland.jitwatch.model.MetaConstructor;
import com.chrisnewland.jitwatch.model.MetaMethod;
import com.chrisnewland.jitwatch.util.ClassUtil;
import com.chrisnewland.jitwatch.util.ParseUtil;

public class TestParser
{
	private Method getMethod(String fqClassName, String method, Class<?>[] paramTypes)
	{
		Method m = null;

		try
		{
			Class<?> clazz = ClassUtil.loadClassWithoutInitialising(fqClassName);
			m = clazz.getDeclaredMethod(method, paramTypes);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return m;
	}

	private Constructor<?> getConstructor(String fqClassName, Class<?>[] paramTypes)
	{
		Constructor<?> c = null;

		try
		{
			Class<?> clazz = ClassUtil.loadClassWithoutInitialising(fqClassName);
			c = clazz.getDeclaredConstructor(paramTypes);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return c;
	}
	
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
		
		//array return type, no params
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
}
