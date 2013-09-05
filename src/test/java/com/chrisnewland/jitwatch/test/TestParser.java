package com.chrisnewland.jitwatch.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.chrisnewland.jitwatch.core.ParseUtil;

public class TestParser
{
	@Test
	public void testSourceSignatureMatcher()
	{
		String methodSig = "public int getIndexForTime(long,int)";
		
		String regex = "^(.*)public int getIndexForTime\\(long( )+([0-9a-zA-Z_]+),( )+int( )+([0-9a-zA-Z_]+)\\)(.*)";

		String sourceSig = "public int getIndexForTime(long searchTime, int startIndex)";
		
		// handle array params
		// handle unqualified classes
		
		Matcher matcher = Pattern.compile(regex).matcher(sourceSig);

		boolean match = matcher.find();
		
		assertTrue(match);
		
		String methodSigFQ = "public void clean(java.lang.String,com.allipo.dma.stock.IStock,boolean)";
		
		//TODO store param array in member obj
		
	}

	// method='java/lang/Object &lt;init&gt; ()V'

	@Test
	public void testBuildMethodSignature()
	{

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
