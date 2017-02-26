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
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;

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
class TestClassWithGenerics<K extends java.lang.Object>
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
}

public class TestMetaClass
{
	// test constructor
	public TestMetaClass()
	{

	}

	@Test
	public void testGetMemberFromSignature1() throws NoSuchMethodException, SecurityException
	{
		String thisClassName = "TestClassWithGenerics";

		MetaPackage metaPackage = new MetaPackage(StringUtil.getPackageName(thisClassName));

		MetaClass metaClass = new MetaClass(metaPackage, StringUtil.getUnqualifiedClassName(thisClassName));

		String testMethodName = "primitiveReturnPrimitiveParam";

		Method method = TestClassWithGenerics.class.getDeclaredMethod(testMethodName, new Class[] { int.class });

		MetaMethod testMethod = new MetaMethod(method, metaClass);

		metaClass.addMember(testMethod);

		String testRetType = "int";

		List<String> paramList = new ArrayList<>();
		paramList.add("int");

		IMetaMember result = metaClass.getMemberForSignature(
				MemberSignatureParts.fromParts(metaClass.getFullyQualifiedName(), testMethodName, testRetType, paramList));

		assertNotNull(result);
		assertEquals(testMethod.toString(), result.toString());
	}

	@Test
	public void testGetMemberFromSignature2() throws NoSuchMethodException, SecurityException
	{
		String thisClassName = "TestClassWithGenerics";

		MetaPackage metaPackage = new MetaPackage(StringUtil.getPackageName(thisClassName));

		MetaClass metaClass = new MetaClass(metaPackage, StringUtil.getUnqualifiedClassName(thisClassName));

		String testMethodName = "voidReturnPrimitiveParam";

		Method method = TestClassWithGenerics.class.getDeclaredMethod(testMethodName, new Class[] { int.class });

		MetaMethod testMethod = new MetaMethod(method, metaClass);

		metaClass.addMember(testMethod);

		String testRetType = S_TYPE_NAME_VOID;

		List<String> paramList = new ArrayList<>();
		paramList.add("int");

		IMetaMember result = metaClass.getMemberForSignature(
				MemberSignatureParts.fromParts(metaClass.getFullyQualifiedName(), testMethodName, testRetType, paramList));

		assertNotNull(result);
		assertEquals(testMethod.toString(), result.toString());
	}

	@Test
	public void testGetMemberFromSignature3() throws NoSuchMethodException, SecurityException
	{
		String thisClassName = "TestClassWithGenerics";

		MetaPackage metaPackage = new MetaPackage(StringUtil.getPackageName(thisClassName));

		MetaClass metaClass = new MetaClass(metaPackage, StringUtil.getUnqualifiedClassName(thisClassName));

		String testMethodName = "voidReturnNoParams";

		Method method = TestClassWithGenerics.class.getDeclaredMethod(testMethodName, new Class[0]);

		MetaMethod testMethod = new MetaMethod(method, metaClass);

		metaClass.addMember(testMethod);

		String testRetType = S_TYPE_NAME_VOID;
		List<String> paramList = new ArrayList<>();

		IMetaMember result = metaClass.getMemberForSignature(
				MemberSignatureParts.fromParts(metaClass.getFullyQualifiedName(), testMethodName, testRetType, paramList));

		assertNotNull(result);
		assertEquals(testMethod.toString(), result.toString());
	}

	@Test
	public void testGetMemberFromSignature4() throws NoSuchMethodException, SecurityException
	{
		String thisClassName = "TestClassWithGenerics";

		MetaPackage metaPackage = new MetaPackage(StringUtil.getPackageName(thisClassName));

		MetaClass metaClass = new MetaClass(metaPackage, StringUtil.getUnqualifiedClassName(thisClassName));

		String testMethodName = "objectReturnObjectParam";

		Method method = TestClassWithGenerics.class.getDeclaredMethod(testMethodName, new Class[] { java.lang.String.class });

		MetaMethod testMethod = new MetaMethod(method, metaClass);

		metaClass.addMember(testMethod);

		String testRetType = "java.lang.String";

		List<String> paramList = new ArrayList<>();
		paramList.add("java.lang.String");

		IMetaMember result = metaClass.getMemberForSignature(
				MemberSignatureParts.fromParts(metaClass.getFullyQualifiedName(), testMethodName, testRetType, paramList));

		assertNotNull(result);
		assertEquals(testMethod.toString(), result.toString());
	}

	@Test
	public void testGetMemberFromSignature5() throws NoSuchMethodException, SecurityException
	{
		String thisClassName = "TestClassWithGenerics";

		MetaPackage metaPackage = new MetaPackage(StringUtil.getPackageName(thisClassName));

		MetaClass metaClass = new MetaClass(metaPackage, StringUtil.getUnqualifiedClassName(thisClassName));

		String testMethodName = "arrayReturnArrayParam";

		Method method = TestClassWithGenerics.class.getDeclaredMethod(testMethodName, new Class[] { int[].class });

		MetaMethod testMethod = new MetaMethod(method, metaClass);

		metaClass.addMember(testMethod);

		String testRetType = "[Ljava.lang.String;";

		List<String> paramList = new ArrayList<>();
		paramList.add("[I");

		IMetaMember result = metaClass.getMemberForSignature(
				MemberSignatureParts.fromParts(metaClass.getFullyQualifiedName(), testMethodName, testRetType, paramList));

		assertNotNull(result);
		assertEquals(testMethod.toString(), result.toString());
	}

	@Test
	public void testGetMemberFromSignature6() throws NoSuchMethodException, SecurityException
	{
		String thisClassName = "TestClassWithGenerics";

		MetaPackage metaPackage = new MetaPackage(StringUtil.getPackageName(thisClassName));

		MetaClass metaClass = new MetaClass(metaPackage, StringUtil.getUnqualifiedClassName(thisClassName));

		String testMethodName = "<init>";

		Constructor<?> constructor = TestClassWithGenerics.class.getDeclaredConstructor(new Class[0]);

		MetaConstructor testConstructor = new MetaConstructor(constructor, metaClass);

		metaClass.addMember(testConstructor);

		String testRetType = "TestClassWithGenerics";
		List<String> paramList = new ArrayList<>();

		IMetaMember result = metaClass.getMemberForSignature(
				MemberSignatureParts.fromParts(metaClass.getFullyQualifiedName(), testMethodName, testRetType, paramList));

		assertNotNull(result);
		assertEquals(testConstructor.toString(), result.toString());
	}

	@Test
	public void testRegressionGenericSubstitution() throws NoSuchMethodException, SecurityException
	{
		String thisClassName = "TestClassWithGenerics";

		MetaPackage metaPackage = new MetaPackage(StringUtil.getPackageName(thisClassName));

		MetaClass metaClass = new MetaClass(metaPackage, StringUtil.getUnqualifiedClassName(thisClassName));

		String testMethodName = "genericReturnAndParams";

		Method method = TestClassWithGenerics.class.getDeclaredMethod(testMethodName, new Class[] { Object.class, String.class });

		MetaMethod testMethod = new MetaMethod(method, metaClass);

		metaClass.addMember(testMethod);

		IMetaMember result = metaClass
				.getMemberForSignature(MemberSignatureParts.fromBytecodeSignature(metaClass.getFullyQualifiedName(),
						"public static <T extends java.lang.Object> T genericReturnAndParams(T, java.lang.String);"));

		assertNotNull(result);
		assertEquals(testMethod.toString(), result.toString());
	}

	@Test
	public void testRegressionGenericDeclaredAtClassLevel() throws NoSuchMethodException, SecurityException
	{
		String thisClassName = "TestClassWithGenerics";

		MetaPackage metaPackage = new MetaPackage(StringUtil.getPackageName(thisClassName));

		MetaClass metaClass = new MetaClass(metaPackage, StringUtil.getUnqualifiedClassName(thisClassName));

		String testMethodName = "genericReturnDeclaredOnClass";

		Method method = TestClassWithGenerics.class.getDeclaredMethod(testMethodName, new Class[0]);

		MetaMethod testMethod = new MetaMethod(method, metaClass);

		metaClass.addMember(testMethod);

		MemberSignatureParts msp = MemberSignatureParts.fromBytecodeSignature(metaClass.getFullyQualifiedName(),
				"public K genericReturnDeclaredOnClass();");

		ClassBC classBytecode = new ClassBC(TestClassWithGenerics.class.getName());

		classBytecode.addGenericsMapping("K", "java.lang.Object");

		msp.setClassBC(classBytecode);

		IMetaMember result = metaClass.getMemberForSignature(msp);

		assertNotNull(result);
		assertEquals(testMethod.toString(), result.toString());
	}

	@Test
	public void testRegressionGenericWildcardReturnType() throws NoSuchMethodException, SecurityException
	{
		String thisClassName = "TestClassWithGenerics";

		MetaPackage metaPackage = new MetaPackage(StringUtil.getPackageName(thisClassName));

		MetaClass metaClass = new MetaClass(metaPackage, StringUtil.getUnqualifiedClassName(thisClassName));

		String testMethodName = "genericReturnTypeWildcard";

		Method method = TestClassWithGenerics.class.getDeclaredMethod(testMethodName, new Class[0]);

		MetaMethod testMethod = new MetaMethod(method, metaClass);

		metaClass.addMember(testMethod);

		MemberSignatureParts msp = MemberSignatureParts.fromBytecodeSignature(metaClass.getFullyQualifiedName(),
				"public java.lang.Class<?>[] genericReturnTypeWildcard();");

		IMetaMember result = metaClass.getMemberForSignature(msp);

		assertNotNull(result);
		assertEquals(testMethod.toString(), result.toString());
	}

	@Test
	public void testRegressionGenericWildcardParamType() throws NoSuchMethodException, SecurityException
	{
		String thisClassName = "TestClassWithGenerics";

		MetaPackage metaPackage = new MetaPackage(StringUtil.getPackageName(thisClassName));

		MetaClass metaClass = new MetaClass(metaPackage, StringUtil.getUnqualifiedClassName(thisClassName));

		String testMethodName = "genericParamTypeWildcard";

		Method method = TestClassWithGenerics.class.getDeclaredMethod(testMethodName, new Class[] { Class[].class });

		MetaMethod testMethod = new MetaMethod(method, metaClass);

		metaClass.addMember(testMethod);

		MemberSignatureParts msp = MemberSignatureParts.fromBytecodeSignature(metaClass.getFullyQualifiedName(),
				"public void genericParamTypeWildcard(java.lang.Class<?>[]);");

		IMetaMember result = metaClass.getMemberForSignature(msp);

		assertNotNull(result);
		assertEquals(testMethod.toString(), result.toString());
	}

	@Test
	public void testRegressionDefaultPackageParamMatchPrimitive() throws Throwable
	{
		String className = "FooClassInDefaultPackage";

		Class<?> defaultPackageTestClass = Class.forName(className);

		Class<?> defaultPackageTestClassParam = int.class;
		
		String expectedParamClassName = "int";

		MetaClass metaClass = new MetaClass(new MetaPackage(S_EMPTY), className);

		Method getterPrimitiveParam = defaultPackageTestClass.getDeclaredMethod("getPrimitiveParam", new Class[] {});

		MetaMethod metaMethodGetPrimitiveParam = new MetaMethod(getterPrimitiveParam, metaClass);

		String[] paramTypeNamesGetPrimitiveParam = metaMethodGetPrimitiveParam.getParamTypeNames();

		assertEquals(0, paramTypeNamesGetPrimitiveParam.length);

		String returnTypeNameGetPrimitiveParam = metaMethodGetPrimitiveParam.getReturnTypeName();

		assertEquals(expectedParamClassName, returnTypeNameGetPrimitiveParam);

		Method setterPrimitiveParam = defaultPackageTestClass.getDeclaredMethod("setPrimitiveParam",
				new Class[] { defaultPackageTestClassParam });

		MetaMethod metaMethodSetPrimitiveParam = new MetaMethod(setterPrimitiveParam, metaClass);

		String[] paramTypeNamesSetPrimitiveParam = metaMethodSetPrimitiveParam.getParamTypeNames();

		assertEquals(1, paramTypeNamesSetPrimitiveParam.length);

		assertEquals(expectedParamClassName, paramTypeNamesSetPrimitiveParam[0]);

		String returnTypeNameSetParam = metaMethodSetPrimitiveParam.getReturnTypeName();

		assertEquals(S_TYPE_NAME_VOID, returnTypeNameSetParam);
	}
	
	@Test
	public void testRegressionDefaultPackageParamMatchClass() throws Throwable
	{
		String className = "FooClassInDefaultPackage";

		String paramClassName = "IsUsedForTestingDefaultPackage";

		Class<?> defaultPackageTestClass = Class.forName(className);

		Class<?> defaultPackageTestClassParam = Class.forName(paramClassName);
		
		MetaClass metaClass = new MetaClass(new MetaPackage(S_EMPTY), className);

		Method getterPrimitiveParam = defaultPackageTestClass.getDeclaredMethod("getClassParam", new Class[] {});

		MetaMethod metaMethodGetPrimitiveParam = new MetaMethod(getterPrimitiveParam, metaClass);

		String[] paramTypeNamesGetPrimitiveParam = metaMethodGetPrimitiveParam.getParamTypeNames();

		assertEquals(0, paramTypeNamesGetPrimitiveParam.length);

		String returnTypeNameGetPrimitiveParam = metaMethodGetPrimitiveParam.getReturnTypeName();

		assertEquals(paramClassName, returnTypeNameGetPrimitiveParam);

		Method setterPrimitiveParam = defaultPackageTestClass.getDeclaredMethod("setClassParam",
				new Class[] { defaultPackageTestClassParam });

		MetaMethod metaMethodSetPrimitiveParam = new MetaMethod(setterPrimitiveParam, metaClass);

		String[] paramTypeNamesSetPrimitiveParam = metaMethodSetPrimitiveParam.getParamTypeNames();

		assertEquals(1, paramTypeNamesSetPrimitiveParam.length);

		assertEquals(paramClassName, paramTypeNamesSetPrimitiveParam[0]);

		String returnTypeNameSetPrimitive = metaMethodSetPrimitiveParam.getReturnTypeName();

		assertEquals(S_TYPE_NAME_VOID, returnTypeNameSetPrimitive);
	}
	
	@Test
	public void testRegressionDefaultPackageParamMatchPrimitiveArray() throws Throwable
	{
		String className = "FooClassInDefaultPackage";

		Class<?> defaultPackageTestClass = Class.forName(className);

		Class<?> defaultPackageTestClassParam = int[].class;
		
		String expectedParamClassName = "int[]";

		MetaClass metaClass = new MetaClass(new MetaPackage(S_EMPTY), className);

		Method getterPrimitiveArrayParam = defaultPackageTestClass.getDeclaredMethod("getPrimitiveArrayParam", new Class[] {});

		MetaMethod metaMethodGetPrimitiveArrayParam = new MetaMethod(getterPrimitiveArrayParam, metaClass);

		String[] paramTypeNamesGetPrimitiveArrayParam = metaMethodGetPrimitiveArrayParam.getParamTypeNames();

		assertEquals(0, paramTypeNamesGetPrimitiveArrayParam.length);

		String returnTypeNameGetPrimitiveArrayParam = metaMethodGetPrimitiveArrayParam.getReturnTypeName();

		assertEquals(expectedParamClassName, returnTypeNameGetPrimitiveArrayParam);

		Method setterPrimitiveArrayParam = defaultPackageTestClass.getDeclaredMethod("setPrimitiveArrayParam",
				new Class[] { defaultPackageTestClassParam });

		MetaMethod metaMethodSetPrimitiveArrayParam = new MetaMethod(setterPrimitiveArrayParam, metaClass);

		String[] paramTypeNamesSetPrimitiveArrayParam = metaMethodSetPrimitiveArrayParam.getParamTypeNames();

		assertEquals(1, paramTypeNamesSetPrimitiveArrayParam.length);

		assertEquals(expectedParamClassName, paramTypeNamesSetPrimitiveArrayParam[0]);

		String returnTypeNameSetParam = metaMethodSetPrimitiveArrayParam.getReturnTypeName();

		assertEquals(S_TYPE_NAME_VOID, returnTypeNameSetParam);
	}
	
	@Test
	public void testRegressionDefaultPackageParamMatchClassArray() throws Throwable
	{
		String className = "FooClassInDefaultPackage";

		String paramClassName = "IsUsedForTestingDefaultPackage";

		Class<?> defaultPackageTestClass = Class.forName(className);

		Class<?> defaultPackageTestClassParam = Class.forName("[L" + paramClassName + ";");
		
		String expectedParamClassName = paramClassName + "[]";
		
		MetaClass metaClass = new MetaClass(new MetaPackage(S_EMPTY), className);

		Method getterClassArrayParam = defaultPackageTestClass.getDeclaredMethod("getClassArrayParam", new Class[] {});

		MetaMethod metaMethodGetClassArrayParam = new MetaMethod(getterClassArrayParam, metaClass);

		String[] paramTypeNamesGetClassArrayParam = metaMethodGetClassArrayParam.getParamTypeNames();

		assertEquals(0, paramTypeNamesGetClassArrayParam.length);

		String returnTypeNameGetClassArrayParam = metaMethodGetClassArrayParam.getReturnTypeName();

		assertEquals(expectedParamClassName, returnTypeNameGetClassArrayParam);

		Method setterClassArrayParam = defaultPackageTestClass.getDeclaredMethod("setClassArrayParam",
				new Class[] {  defaultPackageTestClassParam });

		MetaMethod metaMethodSetClassArrayParam = new MetaMethod(setterClassArrayParam, metaClass);

		String[] paramTypeNamesSetClassArrayParam = metaMethodSetClassArrayParam.getParamTypeNames();

		assertEquals(1, paramTypeNamesSetClassArrayParam.length);

		assertEquals(expectedParamClassName, paramTypeNamesSetClassArrayParam[0]);

		String returnTypeNameSetClassArray = metaMethodSetClassArrayParam.getReturnTypeName();

		assertEquals(S_TYPE_NAME_VOID, returnTypeNameSetClassArray);
	}
}