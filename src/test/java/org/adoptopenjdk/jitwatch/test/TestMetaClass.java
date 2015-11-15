/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_TYPE_NAME_VOID;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.model.MetaConstructor;
import org.adoptopenjdk.jitwatch.model.MetaMethod;
import org.adoptopenjdk.jitwatch.model.MetaPackage;
import org.adoptopenjdk.jitwatch.model.bytecode.ClassBC;
import org.adoptopenjdk.jitwatch.util.StringUtil;
import org.junit.Test;

// needed to test generics mappings declared at class level
public class TestMetaClass<K extends java.lang.Object>
{
	// ==========================
	// methods used by this test
	// ==========================

	public int primitiveReturnPrimitiveParam(int foo)
	{
		return 1337;
	}

	public void voidReturnPrimitiveParam(int foo)
	{
	}

	public void voidReturnNoParams()
	{
	}

	public String objectReturnObjectParam(String foo)
	{
		return "When the daylight weighs a tonne";
	}

	public String[] arrayReturnArrayParam(int[] foo)
	{
		return new String[] { "and all my friends are gone" };
	}

	public static <T extends java.lang.Object> T genericReturnAndParams(T object, java.lang.String string)
	{
		return null;
	}

	public K genericReturnDeclaredOnClass()
	{
		return null;
	}
	
	public void genericParamTypeWildcard(Class<?>[] fooClasses)
	{
	}
	
	public Class<?>[] genericReturnTypeWildcard()
	{
		return null;
	}

	// test constructor
	public TestMetaClass()
	{

	}

	@Test
	public void testGetMemberFromSignature1() throws NoSuchMethodException, SecurityException
	{
		String thisClassName = getClass().getName();

		MetaPackage metaPackage = new MetaPackage(StringUtil.getPackageName(thisClassName));

		MetaClass metaClass = new MetaClass(metaPackage, StringUtil.getUnqualifiedClassName(thisClassName));

		String testMethodName = "primitiveReturnPrimitiveParam";

		Method method = getClass().getDeclaredMethod(testMethodName, new Class[] { int.class });

		MetaMethod testMethod = new MetaMethod(method, metaClass);

		metaClass.addMetaMethod(testMethod);

		String testRetType = "int";

		List<String> paramList = new ArrayList<>();
		paramList.add("int");

		IMetaMember result = metaClass.getMemberForSignature(MemberSignatureParts.fromParts(metaClass.getFullyQualifiedName(),
				testMethodName, testRetType, paramList));

		assertNotNull(result);
		assertEquals(testMethod.toString(), result.toString());
	}

	@Test
	public void testGetMemberFromSignature2() throws NoSuchMethodException, SecurityException
	{
		String thisClassName = getClass().getName();

		MetaPackage metaPackage = new MetaPackage(StringUtil.getPackageName(thisClassName));

		MetaClass metaClass = new MetaClass(metaPackage, StringUtil.getUnqualifiedClassName(thisClassName));

		String testMethodName = "voidReturnPrimitiveParam";

		Method method = getClass().getDeclaredMethod(testMethodName, new Class[] { int.class });

		MetaMethod testMethod = new MetaMethod(method, metaClass);

		metaClass.addMetaMethod(testMethod);

		String testRetType = S_TYPE_NAME_VOID;

		List<String> paramList = new ArrayList<>();
		paramList.add("int");

		IMetaMember result = metaClass.getMemberForSignature(MemberSignatureParts.fromParts(metaClass.getFullyQualifiedName(),
				testMethodName, testRetType, paramList));

		assertNotNull(result);
		assertEquals(testMethod.toString(), result.toString());
	}

	@Test
	public void testGetMemberFromSignature3() throws NoSuchMethodException, SecurityException
	{
		String thisClassName = getClass().getName();

		MetaPackage metaPackage = new MetaPackage(StringUtil.getPackageName(thisClassName));

		MetaClass metaClass = new MetaClass(metaPackage, StringUtil.getUnqualifiedClassName(thisClassName));

		String testMethodName = "voidReturnNoParams";

		Method method = getClass().getDeclaredMethod(testMethodName, new Class[0]);

		MetaMethod testMethod = new MetaMethod(method, metaClass);

		metaClass.addMetaMethod(testMethod);

		String testRetType = S_TYPE_NAME_VOID;
		List<String> paramList = new ArrayList<>();

		IMetaMember result = metaClass.getMemberForSignature(MemberSignatureParts.fromParts(metaClass.getFullyQualifiedName(),
				testMethodName, testRetType, paramList));

		assertNotNull(result);
		assertEquals(testMethod.toString(), result.toString());
	}

	@Test
	public void testGetMemberFromSignature4() throws NoSuchMethodException, SecurityException
	{
		String thisClassName = getClass().getName();

		MetaPackage metaPackage = new MetaPackage(StringUtil.getPackageName(thisClassName));

		MetaClass metaClass = new MetaClass(metaPackage, StringUtil.getUnqualifiedClassName(thisClassName));

		String testMethodName = "objectReturnObjectParam";

		Method method = getClass().getDeclaredMethod(testMethodName, new Class[] { java.lang.String.class });

		MetaMethod testMethod = new MetaMethod(method, metaClass);

		metaClass.addMetaMethod(testMethod);

		String testRetType = "java.lang.String";

		List<String> paramList = new ArrayList<>();
		paramList.add("java.lang.String");

		IMetaMember result = metaClass.getMemberForSignature(MemberSignatureParts.fromParts(metaClass.getFullyQualifiedName(),
				testMethodName, testRetType, paramList));

		assertNotNull(result);
		assertEquals(testMethod.toString(), result.toString());
	}

	@Test
	public void testGetMemberFromSignature5() throws NoSuchMethodException, SecurityException
	{
		String thisClassName = getClass().getName();

		MetaPackage metaPackage = new MetaPackage(StringUtil.getPackageName(thisClassName));

		MetaClass metaClass = new MetaClass(metaPackage, StringUtil.getUnqualifiedClassName(thisClassName));

		String testMethodName = "arrayReturnArrayParam";

		Method method = getClass().getDeclaredMethod(testMethodName, new Class[] { int[].class });

		MetaMethod testMethod = new MetaMethod(method, metaClass);

		metaClass.addMetaMethod(testMethod);

		String testRetType = "[Ljava.lang.String;";

		List<String> paramList = new ArrayList<>();
		paramList.add("[I");

		IMetaMember result = metaClass.getMemberForSignature(MemberSignatureParts.fromParts(metaClass.getFullyQualifiedName(),
				testMethodName, testRetType, paramList));

		assertNotNull(result);
		assertEquals(testMethod.toString(), result.toString());
	}

	@Test
	public void testGetMemberFromSignature6() throws NoSuchMethodException, SecurityException
	{
		String thisClassName = getClass().getName();

		MetaPackage metaPackage = new MetaPackage(StringUtil.getPackageName(thisClassName));

		MetaClass metaClass = new MetaClass(metaPackage, StringUtil.getUnqualifiedClassName(thisClassName));

		String testMethodName = "<init>";

		Constructor<?> constructor = getClass().getDeclaredConstructor(new Class[0]);

		MetaConstructor testConstructor = new MetaConstructor(constructor, metaClass);

		metaClass.addMetaConstructor(testConstructor);

		String testRetType = getClass().getName();
		List<String> paramList = new ArrayList<>();

		IMetaMember result = metaClass.getMemberForSignature(MemberSignatureParts.fromParts(metaClass.getFullyQualifiedName(),
				testMethodName, testRetType, paramList));

		assertNotNull(result);
		assertEquals(testConstructor.toString(), result.toString());
	}

	@Test
	public void testRegressionGenericSubstitution() throws NoSuchMethodException, SecurityException
	{
		String thisClassName = getClass().getName();

		MetaPackage metaPackage = new MetaPackage(StringUtil.getPackageName(thisClassName));

		MetaClass metaClass = new MetaClass(metaPackage, StringUtil.getUnqualifiedClassName(thisClassName));

		String testMethodName = "genericReturnAndParams";

		Method method = getClass().getDeclaredMethod(testMethodName, new Class[] { Object.class, String.class });

		MetaMethod testMethod = new MetaMethod(method, metaClass);

		metaClass.addMetaMethod(testMethod);

		IMetaMember result = metaClass.getMemberForSignature(MemberSignatureParts.fromBytecodeSignature(
				metaClass.getFullyQualifiedName(),
				"public static <T extends java.lang.Object> T genericReturnAndParams(T, java.lang.String);"));

		assertNotNull(result);
		assertEquals(testMethod.toString(), result.toString());
	}

	@Test
	public void testRegressionGenericDeclaredAtClassLevel() throws NoSuchMethodException, SecurityException
	{
		String thisClassName = getClass().getName();

		MetaPackage metaPackage = new MetaPackage(StringUtil.getPackageName(thisClassName));

		MetaClass metaClass = new MetaClass(metaPackage, StringUtil.getUnqualifiedClassName(thisClassName));

		String testMethodName = "genericReturnDeclaredOnClass";

		Method method = getClass().getDeclaredMethod(testMethodName, new Class[0]);

		MetaMethod testMethod = new MetaMethod(method, metaClass);

		metaClass.addMetaMethod(testMethod);

		MemberSignatureParts msp = MemberSignatureParts.fromBytecodeSignature(metaClass.getFullyQualifiedName(),
				"public K genericReturnDeclaredOnClass();");
		
		ClassBC classBytecode = new ClassBC(getClass().getName());
		
		classBytecode.addGenericsMapping("K", "java.lang.Object");

		msp.setClassBC(classBytecode);
		
		IMetaMember result = metaClass.getMemberForSignature(msp);

		assertNotNull(result);
		assertEquals(testMethod.toString(), result.toString());
	}
	
	@Test
	public void testRegressionGenericWildcardReturnType() throws NoSuchMethodException, SecurityException
	{
		String thisClassName = getClass().getName();

		MetaPackage metaPackage = new MetaPackage(StringUtil.getPackageName(thisClassName));

		MetaClass metaClass = new MetaClass(metaPackage, StringUtil.getUnqualifiedClassName(thisClassName));

		String testMethodName = "genericReturnTypeWildcard";

		Method method = getClass().getDeclaredMethod(testMethodName, new Class[0]);

		MetaMethod testMethod = new MetaMethod(method, metaClass);

		metaClass.addMetaMethod(testMethod);

		MemberSignatureParts msp = MemberSignatureParts.fromBytecodeSignature(metaClass.getFullyQualifiedName(),
				"public java.lang.Class<?>[] genericReturnTypeWildcard();");
			
		IMetaMember result = metaClass.getMemberForSignature(msp);

		assertNotNull(result);
		assertEquals(testMethod.toString(), result.toString());
	}
	
	@Test
	public void testRegressionGenericWildcardParamType() throws NoSuchMethodException, SecurityException
	{
		String thisClassName = getClass().getName();

		MetaPackage metaPackage = new MetaPackage(StringUtil.getPackageName(thisClassName));

		MetaClass metaClass = new MetaClass(metaPackage, StringUtil.getUnqualifiedClassName(thisClassName));

		String testMethodName = "genericParamTypeWildcard";

		Method method = getClass().getDeclaredMethod(testMethodName, new Class[]{Class[].class});

		MetaMethod testMethod = new MetaMethod(method, metaClass);

		metaClass.addMetaMethod(testMethod);

		MemberSignatureParts msp = MemberSignatureParts.fromBytecodeSignature(metaClass.getFullyQualifiedName(),
				"public void genericParamTypeWildcard(java.lang.Class<?>[]);");
					
		IMetaMember result = metaClass.getMemberForSignature(msp);

		assertNotNull(result);
		assertEquals(testMethod.toString(), result.toString());
	}
}