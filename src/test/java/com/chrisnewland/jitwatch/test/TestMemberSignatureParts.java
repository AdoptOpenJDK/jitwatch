/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.test;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.*;

import com.chrisnewland.jitwatch.model.MemberSignatureParts;

public class TestMemberSignatureParts
{
	@Test
	public void testPackageConstructorNoParams()
	{
		String sig = "String()";
		MemberSignatureParts msp = new MemberSignatureParts(sig);

		List<String> modList = msp.getModifiers();

		assertEquals(0, modList.size());
		
		assertEquals(0, msp.getGenerics().size());

		assertEquals(null, msp.getReturnType());

		assertEquals("String", msp.getMemberName());
		
		assertEquals(0, msp.getParamTypes().size());
	}
	
	@Test
	public void testPublicConstructorNoParams()
	{
		String sig = "public String()";
		MemberSignatureParts msp = new MemberSignatureParts(sig);

		List<String> modList = msp.getModifiers();

		assertEquals(1, modList.size());
		assertEquals("public", modList.get(0));
		
		assertEquals(0, msp.getGenerics().size());

		assertEquals(null, msp.getReturnType());

		assertEquals("String", msp.getMemberName());
		
		assertEquals(0, msp.getParamTypes().size());
	}
	
	@Test
	public void testConstructorWithParams()
	{
		String sig = "public String(String, int)";
		MemberSignatureParts msp = new MemberSignatureParts(sig);

		List<String> modList = msp.getModifiers();

		assertEquals(1, modList.size());
		assertEquals("public", modList.get(0));
		
		assertEquals(0, msp.getGenerics().size());

		assertEquals(null, msp.getReturnType());

		assertEquals("String", msp.getMemberName());
		
		assertEquals(2, msp.getParamTypes().size());
		
		List<String> paramTypeList = msp.getParamTypes();
		assertEquals("String", paramTypeList.get(0));
		assertEquals("int", paramTypeList.get(1));
	}
	
	@Test
	public void testSimpleMethodNoParams()
	{
		String sig = "public void redraw()";
		MemberSignatureParts msp = new MemberSignatureParts(sig);

		List<String> modList = msp.getModifiers();

		assertEquals(1, modList.size());
		assertEquals("public", modList.get(0));
		
		assertEquals(0, msp.getGenerics().size());

		assertEquals("void", msp.getReturnType());

		assertEquals("redraw", msp.getMemberName());
		
		assertEquals(0, msp.getParamTypes().size());
	}
	
	
	@Test
	public void testSimpleMethodWithParams()
	{
		String sig = "public boolean isEven(int)";
		MemberSignatureParts msp = new MemberSignatureParts(sig);

		List<String> modList = msp.getModifiers();

		assertEquals(1, modList.size());
		assertEquals("public", modList.get(0));
		
		assertEquals(0, msp.getGenerics().size());

		assertEquals("boolean", msp.getReturnType());

		assertEquals("isEven", msp.getMemberName());
		
		assertEquals(1, msp.getParamTypes().size());
		
		List<String> paramTypeList = msp.getParamTypes();
		assertEquals("int", paramTypeList.get(0));
	}
	
	@Test
	public void testSimpleMethodWithParamsAndParamNames()
	{
		String sig = "public boolean test(int foo, boolean bar)";
		MemberSignatureParts msp = new MemberSignatureParts(sig);

		List<String> modList = msp.getModifiers();

		assertEquals(1, modList.size());
		assertEquals("public", modList.get(0));
		
		assertEquals(0, msp.getGenerics().size());

		assertEquals("boolean", msp.getReturnType());

		assertEquals("test", msp.getMemberName());
		
		assertEquals(2, msp.getParamTypes().size());
		
		List<String> paramTypeList = msp.getParamTypes();
		assertEquals("int", paramTypeList.get(0));
		assertEquals("boolean", paramTypeList.get(1));

	}
	
	@Test
	public void testSimpleGenericMethod()
	{
		String sig = "public Map<String,String> copy(Map<String,String>)";
		MemberSignatureParts msp = new MemberSignatureParts(sig);

		List<String> modList = msp.getModifiers();

		assertEquals(1, modList.size());
		assertEquals("public", modList.get(0));
		
		assertEquals(0, msp.getGenerics().size());

		assertEquals("Map<String,String>", msp.getReturnType());

		assertEquals("copy", msp.getMemberName());
		
		assertEquals(1, msp.getParamTypes().size());
		
		List<String> paramTypeList = msp.getParamTypes();
		assertEquals("Map<String,String>", paramTypeList.get(0));
	}
	
	@Test
	public void testSignatureWithGenericExtends()
	{
		String sig = "public static <T extends java.lang.Object, U extends java.lang.Object> T[] copyOf(U[], int, java.lang.Class<? extends T[]>)";
		MemberSignatureParts msp = new MemberSignatureParts(sig);

		List<String> modList = msp.getModifiers();

		assertEquals(2, modList.size());
		assertEquals("public", modList.get(0));
		assertEquals("static", modList.get(1));

		Map<String, String> genMap = msp.getGenerics();

		assertEquals(2, genMap.size());
		assertEquals(true, genMap.containsKey("T"));
		assertEquals(true, genMap.containsKey("U"));
		assertEquals("java.lang.Object", genMap.get("T"));
		assertEquals("java.lang.Object", genMap.get("U"));

		assertEquals("T[]", msp.getReturnType());

		assertEquals("copyOf", msp.getMemberName());

		List<String> paramTypes = msp.getParamTypes();

		assertEquals(3, paramTypes.size());
		assertEquals("U[]", paramTypes.get(0));
		assertEquals("int", paramTypes.get(1));
		assertEquals("java.lang.Class<? extends T[]>", paramTypes.get(2));
	}

	@Test
	public void testSignatureWithGenericNoExtends()
	{
		String sig = "public static <T,U> T[] copyOf(U[], int, java.lang.Class<? extends T[]>)";
				
		MemberSignatureParts msp = new MemberSignatureParts(sig);

		List<String> modList = msp.getModifiers();

		assertEquals(2, modList.size());
		assertEquals("public", modList.get(0));
		assertEquals("static", modList.get(1));

		Map<String, String> genMap = msp.getGenerics();

		assertEquals(2, genMap.size());
		assertEquals(true, genMap.containsKey("T"));
		assertEquals(true, genMap.containsKey("U"));
		assertEquals(null, genMap.get("T"));
		assertEquals(null, genMap.get("U"));

		assertEquals("T[]", msp.getReturnType());

		assertEquals("copyOf", msp.getMemberName());

		List<String> paramTypes = msp.getParamTypes();

		assertEquals(3, paramTypes.size());
		assertEquals("U[]", paramTypes.get(0));
		assertEquals("int", paramTypes.get(1));
		assertEquals("java.lang.Class<? extends T[]>", paramTypes.get(2));
	}
}
