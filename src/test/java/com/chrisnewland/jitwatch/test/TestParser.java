package com.chrisnewland.jitwatch.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.chrisnewland.jitwatch.core.ParseUtil;

public class TestParser
{
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
