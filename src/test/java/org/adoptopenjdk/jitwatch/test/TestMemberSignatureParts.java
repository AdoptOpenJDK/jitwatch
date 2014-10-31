/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import java.util.List;
import java.util.Map;

import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;
import org.adoptopenjdk.jitwatch.util.ParseUtil;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestMemberSignatureParts
{
	private void checkSame(MemberSignatureParts bc, MemberSignatureParts log)
	{
		assertEquals("return type", bc.getReturnType(), log.getReturnType());
		assertEquals("member name", bc.getMemberName(), log.getMemberName());
		assertEquals("param count", bc.getParamTypes().size(), log.getParamTypes().size());

		for (int i = 0; i < bc.getParamTypes().size(); i++)
		{
			assertEquals("param " + i, bc.getParamTypes().get(i), log.getParamTypes().get(i));
		}
	}

	@Test
	public void testPackageConstructorNoParams() throws Exception
	{
		String sigBC = "java.lang.String();";
		String sigLog = "java.lang.String <init> ()V";

		MemberSignatureParts mspBC = MemberSignatureParts.fromBytecodeSignature("java.lang.String", sigBC);
		MemberSignatureParts mspLog = MemberSignatureParts.fromLogCompilationSignature(sigLog);

		List<String> modListBC = mspBC.getModifiers();
		assertEquals(0, modListBC.size());
		assertEquals(0, mspBC.getGenerics().size());
		assertEquals("void", mspBC.getReturnType());
		assertEquals("java.lang.String", mspBC.getMemberName());
		assertEquals(0, mspBC.getParamTypes().size());

		checkSame(mspBC, mspLog);
	}

	@Test
	public void testPublicConstructorNoParams() throws Exception
	{
		String sigBC = "public java.lang.String()";
		String sigLog = "java.lang.String <init> ()V";

		MemberSignatureParts mspBC = MemberSignatureParts.fromBytecodeSignature("java.lang.String", sigBC);
		MemberSignatureParts mspLog = MemberSignatureParts.fromLogCompilationSignature(sigLog);

		List<String> modListBC = mspBC.getModifiers();

		assertEquals(1, modListBC.size());
		assertEquals("public", modListBC.get(0));
		assertEquals(0, mspBC.getGenerics().size());
		assertEquals("void", mspBC.getReturnType());
		assertEquals("java.lang.String", mspBC.getMemberName());
		assertEquals(0, mspBC.getParamTypes().size());

		checkSame(mspBC, mspLog);
	}

	@Test
	public void testConstructorWithParams() throws Exception
	{
		String sigBC = "public java.lang.String(java.lang.String, int)";
		String sigLog = "java.lang.String <init> (Ljava.lang.String;I)V";

		MemberSignatureParts mspBC = MemberSignatureParts.fromBytecodeSignature("java.lang.String", sigBC);
		MemberSignatureParts mspLog = MemberSignatureParts.fromLogCompilationSignature(sigLog);

		List<String> modListBC = mspBC.getModifiers();

		assertEquals(1, modListBC.size());
		assertEquals("public", modListBC.get(0));
		assertEquals(0, mspBC.getGenerics().size());
		assertEquals("void", mspBC.getReturnType());
		assertEquals("java.lang.String", mspBC.getMemberName());
		assertEquals(2, mspBC.getParamTypes().size());

		List<String> paramTypeListBC = mspBC.getParamTypes();
		assertEquals("java.lang.String", paramTypeListBC.get(0));
		assertEquals("int", paramTypeListBC.get(1));

		checkSame(mspBC, mspLog);
	}

	@Test
	public void testSimpleMethodNoParams() throws Exception
	{
		String sigBC = "public void gc()";
		String sigLog = "java.lang.System gc ()V";

		MemberSignatureParts mspBC = MemberSignatureParts.fromBytecodeSignature("java.lang.String", sigBC);
		MemberSignatureParts mspLog = MemberSignatureParts.fromLogCompilationSignature(sigLog);

		List<String> modListBC = mspBC.getModifiers();

		assertEquals(1, modListBC.size());
		assertEquals("public", modListBC.get(0));
		assertEquals(0, mspBC.getGenerics().size());
		assertEquals("void", mspBC.getReturnType());
		assertEquals("gc", mspBC.getMemberName());
		assertEquals(0, mspBC.getParamTypes().size());
		
		checkSame(mspBC, mspLog);
	}

	@Test
	public void testSimpleMethodWithParams() throws Exception
	{
		String sigBC = "public boolean matches(java.lang.String)";
		String sigLog = "java.lang.String matches (Ljava.lang.String;)Z";

		MemberSignatureParts mspBC = MemberSignatureParts.fromBytecodeSignature("java.lang.String", sigBC);
		MemberSignatureParts mspLog = MemberSignatureParts.fromLogCompilationSignature(sigLog);

		List<String> modListBC = mspBC.getModifiers();
		assertEquals(1, modListBC.size());
		assertEquals("public", modListBC.get(0));
		assertEquals(0, mspBC.getGenerics().size());
		assertEquals("boolean", mspBC.getReturnType());
		assertEquals("matches", mspBC.getMemberName());
		assertEquals(1, mspBC.getParamTypes().size());
		List<String> paramTypeList = mspBC.getParamTypes();
		assertEquals("java.lang.String", paramTypeList.get(0));
		
		checkSame(mspBC, mspLog);
	}

	@Test
	public void testSimpleMethodWithParamsAndParamNames() throws Exception
	{
		String sigBC = "public boolean startsWith(java.lang.String foo, int bar)";
		String sigLog = "java.lang.String startsWith (Ljava.lang.String;I)Z";

		MemberSignatureParts mspBC = MemberSignatureParts.fromBytecodeSignature("java.lang.String", sigBC);
		MemberSignatureParts mspLog = MemberSignatureParts.fromLogCompilationSignature(sigLog);

		List<String> modListBC = mspBC.getModifiers();

		assertEquals(1, modListBC.size());
		assertEquals("public", modListBC.get(0));
		assertEquals(0, mspBC.getGenerics().size());
		assertEquals("boolean", mspBC.getReturnType());
		assertEquals("startsWith", mspBC.getMemberName());
		assertEquals(2, mspBC.getParamTypes().size());
		List<String> paramTypeListBC = mspBC.getParamTypes();
		assertEquals("java.lang.String", paramTypeListBC.get(0));
		assertEquals("int", paramTypeListBC.get(1));
		
		checkSame(mspBC, mspLog);
	}

	@Test
	public void testSimpleGenericMethod()
	{
		String sig = "public Map<String,String> copy(Map<String,String>)";
		MemberSignatureParts msp = MemberSignatureParts.fromBytecodeSignature("com.chrisnewland.Test", sig);

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
		MemberSignatureParts msp = MemberSignatureParts.fromBytecodeSignature("java.util.Arrays", sig);

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

		MemberSignatureParts msp = MemberSignatureParts.fromBytecodeSignature("java.util.Arrays", sig);

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
	
	@Test
	public void testStaticInitialiserBytecode() throws Exception
	{
		String sigBC = "static {}";

		MemberSignatureParts mspBC = MemberSignatureParts.fromBytecodeSignature("java.lang.String", sigBC);

		List<String> modListBC = mspBC.getModifiers();
		assertEquals(0, modListBC.size());
		assertEquals(0, mspBC.getGenerics().size());
		assertEquals("void", mspBC.getReturnType());
		assertEquals(ParseUtil.STATIC_INIT, mspBC.getMemberName());
		assertEquals(0, mspBC.getParamTypes().size());
	}
}