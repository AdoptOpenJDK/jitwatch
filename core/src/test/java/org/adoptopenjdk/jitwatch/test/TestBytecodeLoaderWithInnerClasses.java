/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_TYPE_NAME_VOID;

import org.adoptopenjdk.jitwatch.loader.BytecodeLoader;
import org.adoptopenjdk.jitwatch.logger.NullLogListener;
import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;
import org.adoptopenjdk.jitwatch.model.bytecode.ClassBC;
import org.adoptopenjdk.jitwatch.model.bytecode.MemberBytecode;
import org.adoptopenjdk.jitwatch.model.bytecode.SourceMapper;
import org.adoptopenjdk.jitwatch.process.compiler.CompilerJava;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/*
 javap -v * | egrep "Classfile|line|public"

 Classfile /home/chris/.pp/jitwatch/sandbox/classes/TestInner.class
 public TestInner();
 line 4: 0
 line 5: 4
 line 7: 12
 line 9: 16
 line 10: 25
 public void a();
 line 14: 0
 line 15: 8
 public static void main(java.lang.String[]);
 line 52: 0
 line 53: 8

 Classfile /home/chris/.pp/jitwatch/sandbox/classes/TestInner$Inner1.class
 public TestInner$Inner1(TestInner);
 line 20: 0
 line 21: 9
 line 23: 17
 line 25: 21
 line 26: 30
 public void b();
 line 30: 0
 line 31: 8

 Classfile /home/chris/.pp/jitwatch/sandbox/classes/TestInner$Inner1$Inner2.class
 public TestInner$Inner1$Inner2(TestInner$Inner1);
 line 36: 0
 line 37: 9
 line 39: 17
 line 40: 21
 public void c();
 line 44: 0
 line 45: 8
 */
public class TestBytecodeLoaderWithInnerClasses
{
	private String classNameOuter = "TestInner";
	private String classNameInner1 = "TestInner$Inner1";
	private String classNameInner2 = "TestInner$Inner1$Inner2";

	private Path pathToSourceDir;
	private Path pathToTempClassDir;
	private List<String> classpathLocations;

	private ClassBC classBytecodeForOuter;
	private ClassBC classBytecodeForInner1;
	private ClassBC classBytecodeForInner2;

	@Before
	public void setUp()
	{
		try
		{
			SourceMapper.clear();

			pathToSourceDir = Paths.get("src", "main", "resources", "examples");

			pathToTempClassDir = Files.createTempDirectory("testInnerClasses");

			Path pathToSourceFile = Paths.get(pathToSourceDir.toString(), classNameOuter + ".java");

			List<File> sources = new ArrayList<>();
			sources.add(pathToSourceFile.toFile());

			classpathLocations = new ArrayList<String>();
			classpathLocations.add(pathToTempClassDir.toString());

			CompilerJava compiler = new CompilerJava(System.getProperty("java.home"));

			List<String> compileClasspath = new ArrayList<>();

			boolean success = compiler.compile(sources, compileClasspath, pathToTempClassDir.toFile(), new NullLogListener());
			if (!success)
			{
				System.err.println(compiler.getErrorStream());
				fail();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();

			fail();
		}

		classBytecodeForOuter = BytecodeLoader.fetchBytecodeForClass(classpathLocations, classNameOuter, true);

		BytecodeLoader.fetchBytecodeForClass(classpathLocations, classNameInner1, true);
		BytecodeLoader.fetchBytecodeForClass(classpathLocations, classNameInner2, true);

		List<ClassBC> classBytecodeListForOuter = SourceMapper.getClassBytecodeList(classBytecodeForOuter);

		classBytecodeForOuter = classBytecodeListForOuter.get(0);
		classBytecodeForInner1 = classBytecodeListForOuter.get(1);
		classBytecodeForInner2 = classBytecodeListForOuter.get(2);
	}

	@After
	public void tearDown()
	{
	}

	private void checkMemberNames(ClassBC classBytecode, String... memberNames)
	{
		List<MemberBytecode> memberBytecodeList = classBytecode.getMemberBytecodeList();

		Set<String> memberNameSet = new HashSet<>(Arrays.asList(memberNames));

		for (MemberBytecode memberBytecode : memberBytecodeList)
		{
			assertTrue(memberNameSet.contains(memberBytecode.getMemberSignatureParts().getMemberName()));

		}

		assertEquals(memberNames.length, memberBytecodeList.size());
	}

	@Test
	public void testCompilationCreatedCorrectOutputs()
	{
		assertTrue(Paths.get(pathToTempClassDir.toString(), classNameOuter + ".class").toFile().exists());
		assertTrue(Paths.get(pathToTempClassDir.toString(), classNameInner1 + ".class").toFile().exists());
		assertTrue(Paths.get(pathToTempClassDir.toString(), classNameInner2 + ".class").toFile().exists());

		assertEquals(classNameOuter + ".java", classBytecodeForOuter.getSourceFile());

		checkMemberNames(classBytecodeForOuter, "main", "TestInner", "a");

		List<String> innerClasses = classBytecodeForOuter.getInnerClassNames();

		assertEquals(1, innerClasses.size());

		assertEquals(classNameInner1, innerClasses.get(0));

		assertEquals(classNameOuter + ".java", classBytecodeForInner1.getSourceFile());

		checkMemberNames(classBytecodeForInner1, "TestInner$Inner1", "b");

		List<String> innerClassesOfInner1 = classBytecodeForInner1.getInnerClassNames();

		assertEquals(1, innerClassesOfInner1.size());

		assertEquals(classNameInner2, innerClassesOfInner1.get(0));

		assertEquals(classNameOuter + ".java", classBytecodeForInner2.getSourceFile());

		checkMemberNames(classBytecodeForInner2, "TestInner$Inner1$Inner2", "c");

		List<ClassBC> classBytecodeListForOuter = SourceMapper.getClassBytecodeList(classBytecodeForOuter);

		assertNotNull(classBytecodeListForOuter);

		assertEquals(3, classBytecodeListForOuter.size());
	}

	@Test
	public void testSearchFromSourceOuterClassConstructor()
	{
		String fqClassNameOuter = classBytecodeForOuter.getFullyQualifiedClassName();

		assertEquals(classNameOuter, fqClassNameOuter);

		MemberBytecode memberBytecodeForConstructor = SourceMapper.getMemberBytecodeForSourceLine(classBytecodeForOuter, 4);

		assertNotNull(memberBytecodeForConstructor);

		MemberSignatureParts mspOuterConstructor = memberBytecodeForConstructor.getMemberSignatureParts();

		assertNotNull(mspOuterConstructor);

		assertEquals(S_TYPE_NAME_VOID, mspOuterConstructor.getReturnType());
		assertEquals(0, mspOuterConstructor.getParamTypes().size());
		assertEquals(classNameOuter, mspOuterConstructor.getMemberName());
	}

	@Test
	public void testSearchFromSourceOuterClassMethod()
	{
		MemberBytecode memberBytecodeForMethod = SourceMapper.getMemberBytecodeForSourceLine(classBytecodeForOuter, 14);

		assertNotNull(memberBytecodeForMethod);

		MemberSignatureParts mspOuterMethod = memberBytecodeForMethod.getMemberSignatureParts();

		assertNotNull(mspOuterMethod);

		assertEquals(S_TYPE_NAME_VOID, mspOuterMethod.getReturnType());
		assertEquals(0, mspOuterMethod.getParamTypes().size());
		assertEquals("a", mspOuterMethod.getMemberName());
	}

	@Test
	public void testSearchFromSourceInner1ClassConstructor()
	{
		String fqClassNameInner1 = classBytecodeForInner1.getFullyQualifiedClassName();

		assertEquals(classNameInner1, fqClassNameInner1);

		MemberBytecode memberBytecodeForInner1Constructor = SourceMapper.getMemberBytecodeForSourceLine(classBytecodeForInner1, 20);

		assertNotNull(memberBytecodeForInner1Constructor);

		MemberSignatureParts mspInner1Constructor = memberBytecodeForInner1Constructor.getMemberSignatureParts();

		assertNotNull(mspInner1Constructor);

		assertEquals(S_TYPE_NAME_VOID, mspInner1Constructor.getReturnType());
		assertEquals(1, mspInner1Constructor.getParamTypes().size());
		assertEquals(classNameOuter, mspInner1Constructor.getParamTypes().get(0));

		assertEquals(classNameInner1, mspInner1Constructor.getMemberName());
	}

	@Test
	public void testSearchFromSourceInner1ClassMethod()
	{
		MemberBytecode memberBytecodeForInner1Method = SourceMapper.getMemberBytecodeForSourceLine(classBytecodeForInner1, 30);

		assertNotNull(memberBytecodeForInner1Method);

		MemberSignatureParts mspInner1Method = memberBytecodeForInner1Method.getMemberSignatureParts();

		assertNotNull(mspInner1Method);

		assertEquals(S_TYPE_NAME_VOID, mspInner1Method.getReturnType());
		assertEquals(0, mspInner1Method.getParamTypes().size());
		assertEquals("b", mspInner1Method.getMemberName());
	}

	@Test
	public void testSearchFromSourceInner2ClassConstructor()
	{
		String fqClassNameInner2 = classBytecodeForInner2.getFullyQualifiedClassName();

		assertEquals(classNameInner2, fqClassNameInner2);

		MemberBytecode memberBytecodeForInner2Constructor = SourceMapper.getMemberBytecodeForSourceLine(classBytecodeForInner2, 36);

		assertNotNull(memberBytecodeForInner2Constructor);

		MemberSignatureParts mspInner2Constructor = memberBytecodeForInner2Constructor.getMemberSignatureParts();

		assertNotNull(mspInner2Constructor);

		assertEquals(S_TYPE_NAME_VOID, mspInner2Constructor.getReturnType());
		assertEquals(1, mspInner2Constructor.getParamTypes().size());
		assertEquals(classNameInner1, mspInner2Constructor.getParamTypes().get(0));

		assertEquals(classNameInner2, mspInner2Constructor.getMemberName());
	}

	@Test
	public void testSearchFromSourceInner2ClassMethod()
	{
		MemberBytecode memberBytecodeForInner2Method = SourceMapper.getMemberBytecodeForSourceLine(classBytecodeForInner2, 44);

		assertNotNull(memberBytecodeForInner2Method);

		MemberSignatureParts mspInner2Method = memberBytecodeForInner2Method.getMemberSignatureParts();

		assertNotNull(mspInner2Method);

		assertEquals(S_TYPE_NAME_VOID, mspInner2Method.getReturnType());
		assertEquals(0, mspInner2Method.getParamTypes().size());
		assertEquals("c", mspInner2Method.getMemberName());
	}

	@Test
	public void testSearchFromBytecodeOuterClassConstructor()
	{
		MemberBytecode memberBytecode = SourceMapper.getMemberBytecodeForSourceLine(classBytecodeForOuter, 4);

		// line 4: 0
		// line 5: 4
		// line 7: 12
		// line 9: 16
		// line 10: 25

		assertEquals(4, SourceMapper.getSourceLineFromBytecode(memberBytecode, 0));
		assertEquals(5, SourceMapper.getSourceLineFromBytecode(memberBytecode, 4));
		assertEquals(7, SourceMapper.getSourceLineFromBytecode(memberBytecode, 12));
		assertEquals(9, SourceMapper.getSourceLineFromBytecode(memberBytecode, 16));
		assertEquals(10, SourceMapper.getSourceLineFromBytecode(memberBytecode, 25));
	}

	@Test
	public void testSearchFromBytecodeOuterClassMethod()
	{
		MemberBytecode memberBytecode = SourceMapper.getMemberBytecodeForSourceLine(classBytecodeForOuter, 14);

		// line 14: 0
		// line 15: 8

		assertEquals(14, SourceMapper.getSourceLineFromBytecode(memberBytecode, 0));
		assertEquals(15, SourceMapper.getSourceLineFromBytecode(memberBytecode, 8));
	}

	@Test
	public void testSearchFromBytecodeInner1ClassConstructor()
	{
		MemberBytecode memberBytecode = SourceMapper.getMemberBytecodeForSourceLine(classBytecodeForInner1, 20);

		// line 20: 0
		// line 21: 9
		// line 23: 17
		// line 25: 21
		// line 26: 30

		assertEquals(20, SourceMapper.getSourceLineFromBytecode(memberBytecode, 0));
		assertEquals(21, SourceMapper.getSourceLineFromBytecode(memberBytecode, 9));
		assertEquals(23, SourceMapper.getSourceLineFromBytecode(memberBytecode, 17));
		assertEquals(25, SourceMapper.getSourceLineFromBytecode(memberBytecode, 21));
		assertEquals(26, SourceMapper.getSourceLineFromBytecode(memberBytecode, 30));
	}

	@Test
	public void testSearchFromBytecodeInner1ClassMethod()
	{
		MemberBytecode memberBytecode = SourceMapper.getMemberBytecodeForSourceLine(classBytecodeForInner1, 30);

		// line 30: 0
		// line 31: 8

		assertEquals(30, SourceMapper.getSourceLineFromBytecode(memberBytecode, 0));
		assertEquals(31, SourceMapper.getSourceLineFromBytecode(memberBytecode, 8));
	}
	
	@Test
	public void testSearchFromBytecodeInner2ClassConstructor()
	{
		MemberBytecode memberBytecode = SourceMapper.getMemberBytecodeForSourceLine(classBytecodeForInner2, 36);

//		 line 36: 0
//		 line 37: 9
//		 line 39: 17
//		 line 40: 21

		assertEquals(36, SourceMapper.getSourceLineFromBytecode(memberBytecode, 0));
		assertEquals(37, SourceMapper.getSourceLineFromBytecode(memberBytecode, 9));
		assertEquals(39, SourceMapper.getSourceLineFromBytecode(memberBytecode, 17));
		assertEquals(40, SourceMapper.getSourceLineFromBytecode(memberBytecode, 21));
	}

	@Test
	public void testSearchFromBytecodeInner2ClassMethod()
	{
		MemberBytecode memberBytecode = SourceMapper.getMemberBytecodeForSourceLine(classBytecodeForInner2, 44);

//		 line 44: 0
//		 line 45: 8

		assertEquals(44, SourceMapper.getSourceLineFromBytecode(memberBytecode, 0));
		assertEquals(45, SourceMapper.getSourceLineFromBytecode(memberBytecode, 8));
	}
}