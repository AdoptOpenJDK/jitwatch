/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SEMICOLON;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_OPTIMIZED_VIRTUAL_CALL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.adoptopenjdk.jitwatch.core.JITWatchConstants;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.JITDataModel;
import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyInstruction;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyLabels;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.model.bytecode.ClassBC;
import org.adoptopenjdk.jitwatch.model.bytecode.MemberBytecode;
import org.adoptopenjdk.jitwatch.optimizedvcall.OptimizedVirtualCall;
import org.adoptopenjdk.jitwatch.optimizedvcall.OptimizedVirtualCallFinder;
import org.adoptopenjdk.jitwatch.optimizedvcall.VirtualCallSite;
import org.adoptopenjdk.jitwatch.util.ClassUtil;
import org.junit.Test;

public class TestFindOptimizedVirtualCall
{
	@Test
	public void testNoOptimizedVirtualCallFoundWithEmptyComment()
	{
		String annotation = null;
		long address = 1;
		String mnemonic = "ADD";
		List<String> operands = new ArrayList<>();
		operands.add("%rdx");
		operands.add("%rax");
		String firstComment = S_EMPTY;

		AssemblyInstruction ins = new AssemblyInstruction(annotation, address, Collections.<String>emptyList(), mnemonic, operands, firstComment, new AssemblyLabels());

		assertFalse(ins.isOptimizedVCall());

		assertNull(ins.getOptimizedVirtualCallSiteOrNull());

		JITDataModel model = new JITDataModel();

		List<String> classLocations = new ArrayList<>();

		OptimizedVirtualCallFinder finder = new OptimizedVirtualCallFinder(model, classLocations);

		OptimizedVirtualCall vCall = finder.findOptimizedCall(ins);

		assertNull(vCall);
	}

	@Test
	public void testNoOptimizedVirtualCallFoundWithNullComment()
	{
		String annotation = null;
		long address = 1;
		String mnemonic = "ADD";
		List<String> operands = new ArrayList<>();
		operands.add("%rdx");
		operands.add("%rax");
		String firstComment = null;

		AssemblyInstruction ins = new AssemblyInstruction(annotation, address, Collections.<String>emptyList(), mnemonic, operands, firstComment, new AssemblyLabels());

		assertFalse(ins.isOptimizedVCall());

		assertNull(ins.getOptimizedVirtualCallSiteOrNull());

		JITDataModel model = new JITDataModel();

		List<String> classLocations = new ArrayList<>();

		OptimizedVirtualCallFinder finder = new OptimizedVirtualCallFinder(model, classLocations);

		OptimizedVirtualCall vCall = finder.findOptimizedCall(ins);

		assertNull(vCall);
	}

	@Test
	public void testNoOptimizedVirtualCallFoundWithNonVCallComment()
	{
		String annotation = null;
		long address = 1;
		String mnemonic = "ADD";
		List<String> operands = new ArrayList<>();
		operands.add("%rdx");
		operands.add("%rax");
		String firstComment = "; Every day sends future a past.";

		AssemblyInstruction ins = new AssemblyInstruction(annotation, address, Collections.<String>emptyList(), mnemonic, operands, firstComment, new AssemblyLabels());

		assertFalse(ins.isOptimizedVCall());

		assertNull(ins.getOptimizedVirtualCallSiteOrNull());

		JITDataModel model = new JITDataModel();

		List<String> classLocations = new ArrayList<>();

		OptimizedVirtualCallFinder finder = new OptimizedVirtualCallFinder(model, classLocations);

		OptimizedVirtualCall vCall = finder.findOptimizedCall(ins);

		assertNull(vCall);
	}

	@Test
	public void testNoOptimizedVirtualCallFoundWithOnlyVCallSiteComment()
	{
		String annotation = null;
		long address = 1;
		String mnemonic = "ADD";
		List<String> operands = new ArrayList<>();
		operands.add("%rdx");
		operands.add("%rax");
		String firstComment = "; - FooClass::fooMethod@1 (line 42)";

		AssemblyInstruction ins = new AssemblyInstruction(annotation, address, Collections.<String>emptyList(), mnemonic, operands, firstComment, new AssemblyLabels());

		assertFalse(ins.isOptimizedVCall());

		assertNull(ins.getOptimizedVirtualCallSiteOrNull());

		JITDataModel model = new JITDataModel();

		List<String> classLocations = new ArrayList<>();

		OptimizedVirtualCallFinder finder = new OptimizedVirtualCallFinder(model, classLocations);

		OptimizedVirtualCall vCall = finder.findOptimizedCall(ins);
		assertNull(vCall);
	}

	@Test
	public void testNoOptimizedVirtualCallFoundWithOVCTagNoCallSiteComment()
	{
		String annotation = null;
		long address = 1;
		String mnemonic = "ADD";
		List<String> operands = new ArrayList<>();
		operands.add("%rdx");
		operands.add("%rax");
		String firstComment = C_SEMICOLON + S_OPTIMIZED_VIRTUAL_CALL;

		AssemblyInstruction ins = new AssemblyInstruction(annotation, address, Collections.<String>emptyList(), mnemonic, operands, firstComment, new AssemblyLabels());

		assertFalse(ins.isOptimizedVCall());

		assertNull(ins.getOptimizedVirtualCallSiteOrNull());

		JITDataModel model = new JITDataModel();

		List<String> classLocations = new ArrayList<>();

		OptimizedVirtualCallFinder finder = new OptimizedVirtualCallFinder(model, classLocations);

		OptimizedVirtualCall vCall = finder.findOptimizedCall(ins);

		assertNull(vCall);
	}

	private AssemblyInstruction buildInstructionForValidCallSite(String callSiteComment)
	{
		String annotation = null;
		long address = 1;
		String mnemonic = "ADD";
		List<String> operands = new ArrayList<>();
		operands.add("%rdx");
		operands.add("%rax");

		String comment0 = " ; OopMap{rbp=Oop off=940}";

		AssemblyInstruction ins = new AssemblyInstruction(annotation, address, Collections.<String>emptyList(), mnemonic, operands, comment0, new AssemblyLabels());
		ins.addCommentLine(";*invokevirtual toString");
		ins.addCommentLine(callSiteComment);
		ins.addCommentLine("; " + S_OPTIMIZED_VIRTUAL_CALL);

		return ins;
	}

	@Test
	public void testBuildCallSite()
	{
		AssemblyInstruction ins = buildInstructionForValidCallSite("; - FooClass::fooMethod@1 (line 42)");

		assertTrue(ins.isOptimizedVCall());

		VirtualCallSite callSite = ins.getOptimizedVirtualCallSiteOrNull();

		assertNotNull(callSite);

		assertEquals("FooClass", callSite.getClassName());
		assertEquals("fooMethod", callSite.getMemberName());
		assertEquals(1, callSite.getBytecodeOffset());
		assertEquals(42, callSite.getSourceLine());
	}

	@Test
	public void testOptimizedVirtualCallRegexConstructor()
	{
		AssemblyInstruction ins = buildInstructionForValidCallSite("; - FooClass::<init>@1 (line 42)");

		assertTrue(ins.isOptimizedVCall());

		VirtualCallSite callSite = ins.getOptimizedVirtualCallSiteOrNull();

		assertNotNull(callSite);
		assertEquals("FooClass", callSite.getClassName());
		assertEquals("<init>", callSite.getMemberName());
		assertEquals(1, callSite.getBytecodeOffset());
		assertEquals(42, callSite.getSourceLine());
	}

	@Test
	public void testOptimizedVirtualCallRegexNegativeBCI()
	{
		AssemblyInstruction ins = buildInstructionForValidCallSite("; - FooClass::foo@-1 (line 42)");

		assertTrue(ins.isOptimizedVCall());

		VirtualCallSite callSite = ins.getOptimizedVirtualCallSiteOrNull();

		assertNotNull(callSite);
		assertEquals("FooClass", callSite.getClassName());
		assertEquals("foo", callSite.getMemberName());
		assertEquals(-1, callSite.getBytecodeOffset());
		assertEquals(42, callSite.getSourceLine());
	}

	@Test
	public void testOptimizedVirtualCallRegexInnerClass()
	{

		AssemblyInstruction ins = buildInstructionForValidCallSite("; - FooClass$Inner::foo@-1 (line 42)");

		assertTrue(ins.isOptimizedVCall());

		VirtualCallSite callSite = ins.getOptimizedVirtualCallSiteOrNull();

		assertNotNull(callSite);
		assertEquals("FooClass$Inner", callSite.getClassName());
		assertEquals("foo", callSite.getMemberName());
		assertEquals(-1, callSite.getBytecodeOffset());
		assertEquals(42, callSite.getSourceLine());
	}

	@Test
	public void testOptimizedVirtualCallRegexInvalid()
	{
		assertNull(buildInstructionForValidCallSite("; - FooClass::foo@ (line 42)").getOptimizedVirtualCallSiteOrNull());
		assertNull(buildInstructionForValidCallSite("; - FooClass::foo@bar (line 42)").getOptimizedVirtualCallSiteOrNull());
		assertNull(buildInstructionForValidCallSite("; - FooClass::foo@5 (line )").getOptimizedVirtualCallSiteOrNull());
		assertNull(buildInstructionForValidCallSite("; - FooClass::foo@5 (line bar)").getOptimizedVirtualCallSiteOrNull());
		assertNull(buildInstructionForValidCallSite("; - ::@").getOptimizedVirtualCallSiteOrNull());
		assertNull(buildInstructionForValidCallSite("In the trail of fire I know we will be free again")
				.getOptimizedVirtualCallSiteOrNull());
	}

	private static final int sourceLine = 279; // TODO this must match call to test2() within test1() method

	public void test1()
	{
		test2();
	}

	public void test2()
	{
		System.out.println("Raise the anchor, bring it on home");
	}
	
	@Test
	public void testCallSiteFoundMultiLineComment() throws ClassNotFoundException
	{	
		String fqClassName = getClass().getName();

		String annotation = null;
		long address = 16;
		String mnemonic = "ADD";
		List<String> operands = new ArrayList<>();
		operands.add("%rdx");
		operands.add("%rax");

		String callerMethod = "test1";
		String calleeMethod = "test2";

		int vCallBCI = 1;

		String comment0 = "; OopMap{rbp=Oop off=940}";
		String comment1 = "; *invokespecial " + calleeMethod;
		String comment2 = "; - " + fqClassName + "::" + callerMethod + "@" + vCallBCI + " (line " + sourceLine + ")";
		String comment3 = ";  " + JITWatchConstants.S_OPTIMIZED_VIRTUAL_CALL;

		AssemblyInstruction instruction = new AssemblyInstruction(annotation, address, Collections.<String>emptyList(), mnemonic, operands, comment0, new AssemblyLabels());
		instruction.addCommentLine(comment1);
		instruction.addCommentLine(comment2);
		instruction.addCommentLine(comment3);

		assertTrue(instruction.isOptimizedVCall());

		VirtualCallSite callSite = instruction.getOptimizedVirtualCallSiteOrNull();

		assertNotNull(callSite);

		assertEquals(fqClassName, callSite.getClassName());
		assertEquals(callerMethod, callSite.getMemberName());
		assertEquals(vCallBCI, callSite.getBytecodeOffset());
		assertEquals(sourceLine, callSite.getSourceLine());

		JITDataModel model = new JITDataModel();

		MetaClass metaClass = UnitTestUtil.createMetaClassFor(model, fqClassName);

		String bcSigTest1 = "public void test1();";
		String bcSigTest2 = "public void test2();";

		MemberSignatureParts mspTest1 = MemberSignatureParts.fromBytecodeSignature(fqClassName, bcSigTest1);
		MemberSignatureParts mspTest2 = MemberSignatureParts.fromBytecodeSignature(fqClassName, bcSigTest2);
		
		IMetaMember memberTest1 = metaClass.getMemberForSignature(mspTest1);
		IMetaMember memberTest2 = metaClass.getMemberForSignature(mspTest2);

		List<String> classLocations = ClassUtil.getCurrentClasspathElements();

		OptimizedVirtualCallFinder finder = new OptimizedVirtualCallFinder(model, classLocations);

		OptimizedVirtualCall ovc = finder.findOptimizedCall(instruction);

		assertNotNull(ovc);

		assertEquals(callSite, ovc.getCallsite());

		assertEquals(memberTest1, ovc.getCallerMember());

		assertEquals(memberTest2, ovc.getCalleeMember());

		ClassBC classBytecode = metaClass.getClassBytecode(model, classLocations);

		MemberBytecode bytecodeMemberTest1 = classBytecode.getMemberBytecode(memberTest1);

		BytecodeInstruction bytecodeInstruction = bytecodeMemberTest1.getBytecodeAtOffset(vCallBCI);

		assertEquals(bytecodeInstruction, ovc.getBytecodeInstruction());
	}
}
