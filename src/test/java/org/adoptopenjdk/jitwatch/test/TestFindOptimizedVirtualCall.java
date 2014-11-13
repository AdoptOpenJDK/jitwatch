package org.adoptopenjdk.jitwatch.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.core.JITWatchConstants;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyInstruction;
import org.adoptopenjdk.jitwatch.optimizedvcall.VirtualCallSite;
import org.adoptopenjdk.jitwatch.optimizedvcall.OptimizedVirtualCall;
import org.adoptopenjdk.jitwatch.optimizedvcall.OptimizedVirtualCallFinder;
import org.junit.Test;

public class TestFindOptimizedVirtualCall
{
	@Test
	public void testNoOptimizedVirtualCallFoundWithNullComment()
	{
		String annotation = null;
		long address = 1;
		String modifier = null;
		String mnemonic = "ADD";
		List<String> operands = new ArrayList<>();
		operands.add("%rdx");
		operands.add("%rax");
		String firstComment = null;

		AssemblyInstruction ins = new AssemblyInstruction(annotation, address, modifier, mnemonic, operands, firstComment);

		OptimizedVirtualCall vCall = OptimizedVirtualCallFinder.findOptimizedCall(null, ins);

		assertNull(vCall);
	}

	@Test
	public void testNoOptimizedVirtualCallFoundWithComment()
	{
		String annotation = null;
		long address = 1;
		String modifier = null;
		String mnemonic = "ADD";
		List<String> operands = new ArrayList<>();
		operands.add("%rdx");
		operands.add("%rax");
		String firstComment = "; - FooClass::fooMethod@1 (line 42)";

		AssemblyInstruction ins = new AssemblyInstruction(annotation, address, modifier, mnemonic, operands, firstComment);

		OptimizedVirtualCall vCall = OptimizedVirtualCallFinder.findOptimizedCall(null, ins);

		assertNull(vCall);
	}

	@Test
	public void testOptimizedVirtualCallRegex()
	{
		String valid = "; - FooClass::fooMethod@1 (line 42)";
		VirtualCallSite callSite = OptimizedVirtualCallFinder.buildCallSiteForLine(valid);

		assertNotNull(callSite);
		assertEquals("FooClass", callSite.getClassName());
		assertEquals("fooMethod", callSite.getMemberName());
		assertEquals(1, callSite.getBytecodeOffset());
		assertEquals(42, callSite.getSourceLine());
	}

	@Test
	public void testOptimizedVirtualCallRegexConstructor()
	{
		String valid = "; - FooClass::<init>@1 (line 42)";
		VirtualCallSite callSite = OptimizedVirtualCallFinder.buildCallSiteForLine(valid);

		assertNotNull(callSite);
		assertEquals("FooClass", callSite.getClassName());
		assertEquals("<init>", callSite.getMemberName());
		assertEquals(1, callSite.getBytecodeOffset());
		assertEquals(42, callSite.getSourceLine());
	}

	@Test
	public void testOptimizedVirtualCallRegexNegativeBCI()
	{
		String valid = "; - FooClass::foo@-1 (line 42)";
		VirtualCallSite callSite = OptimizedVirtualCallFinder.buildCallSiteForLine(valid);

		assertNotNull(callSite);
		assertEquals("FooClass", callSite.getClassName());
		assertEquals("foo", callSite.getMemberName());
		assertEquals(-1, callSite.getBytecodeOffset());
		assertEquals(42, callSite.getSourceLine());
	}

	@Test
	public void testOptimizedVirtualCallRegexInnerClass()
	{
		String valid = "; - FooClass$Inner::foo@-1 (line 42)";
		VirtualCallSite callSite = OptimizedVirtualCallFinder.buildCallSiteForLine(valid);

		assertNotNull(callSite);
		assertEquals("FooClass$Inner", callSite.getClassName());
		assertEquals("foo", callSite.getMemberName());
		assertEquals(-1, callSite.getBytecodeOffset());
		assertEquals(42, callSite.getSourceLine());
	}

	@Test
	public void testOptimizedVirtualCallRegexInvalid()
	{
		assertNull(OptimizedVirtualCallFinder.buildCallSiteForLine("; - FooClass::foo@ (line 42)"));
		assertNull(OptimizedVirtualCallFinder.buildCallSiteForLine("; - FooClass::foo@bar (line 42)"));
		assertNull(OptimizedVirtualCallFinder.buildCallSiteForLine("; - FooClass::foo@5 (line )"));
		assertNull(OptimizedVirtualCallFinder.buildCallSiteForLine("; - FooClass::foo@5 (line bar)"));
		assertNull(OptimizedVirtualCallFinder.buildCallSiteForLine("; - ::@"));
		assertNull(OptimizedVirtualCallFinder.buildCallSiteForLine("There is no knowledge that is not power."));
	}

	@Test
	public void testOptimizedVirtualCallFound()
	{
		String annotation = null;
		long address = 1;
		String modifier = null;
		String mnemonic = "ADD";
		List<String> operands = new ArrayList<>();
		operands.add("%rdx");
		operands.add("%rax");
		String comment1 = "; - BarClass::barMethod@22 (line 19)";
		String comment2 = "; - FooClass::fooMethod@11 (line 76)";
		String comment3 = ";  " + JITWatchConstants.S_OPTIMIZED_VIRTUAL_CALL;

		AssemblyInstruction ins = new AssemblyInstruction(annotation, address, modifier, mnemonic, operands, comment1);
		ins.addCommentLine(comment2);
		ins.addCommentLine(comment3);

		OptimizedVirtualCall vCall = OptimizedVirtualCallFinder.findOptimizedCall(null, ins);

		assertNotNull(vCall);

		VirtualCallSite caller = vCall.getCaller();
		VirtualCallSite callee = vCall.getCallee();

		assertEquals("FooClass", caller.getClassName());
		assertEquals("fooMethod", caller.getMemberName());
		assertEquals(11, caller.getBytecodeOffset());
		assertEquals(76, caller.getSourceLine());

		assertEquals("BarClass", callee.getClassName());
		assertEquals("barMethod", callee.getMemberName());
		assertEquals(22, callee.getBytecodeOffset());
		assertEquals(19, callee.getSourceLine());
	}
}
