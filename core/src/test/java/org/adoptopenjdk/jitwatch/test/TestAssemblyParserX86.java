/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.adoptopenjdk.jitwatch.model.assembly.Architecture;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyBlock;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyInstruction;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyLabels;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyMethod;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyUtil;
import org.adoptopenjdk.jitwatch.model.assembly.IAssemblyParser;
import org.junit.Test;

public class TestAssemblyParserX86 extends AbstractAssemblyTest
{
	@Test
	public void testAssemblyParse()
	{
		testAssemblyParse(new String[] {
				"# {method} &apos;add&apos; &apos;(JJ)J&apos; in &apos;org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog&apos;",
				"# this:     rsi:rsi   = &apos;org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog&apos;",
				"# parm0:    rdx:rdx   = long",
				"# parm1:    rcx:rcx   = long",
				"#           [sp+0x20]  (sp of caller)",
				"0x00007f4475904140: mov  0x8(%rsi),%r10d",
				"0x00007f4475904144: cmp    %r10,%rax",
				"0x00007f4475904147: jne  0x00007f44758c5960  ;   {runtime_call}",
				"0x00007f447590414d: data32 xchg %ax,%ax",
				"[Verified Entry Point]",
				"0x00007f4475904150: sub    $0x18,%rsp",
				"0x00007f4475904157: mov    %rbp,0x10(%rsp)    ;*synchronization entry",
				"                                                ; - org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog::add@-1 (line 144)",
				"0x00007f447590415c: mov    %rdx,%rax",
				"0x00007f447590415f: add    %rcx,%rax          ;*ladd",
				"                                                ; - org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog::add@2 (line 144)",
				"0x00007f4475904162: add    $0x10,%rsp",
				"0x00007f4475904166: pop    %rbp",
				"0x00007f4475904167: test   %eax,0x5ce1e93(%rip)      # 0x00007f447b5e6000",
				"                                                ;   {poll_return}",
				"0x00007f447590416d: retq",
				"0x00007f447590416e: hlt",
				"0x00007f447590416f: hlt",
				"0x00007f4475904170: hlt",
				"0x00007f4475904171: hlt",
				"0x00007f4475904172: hlt",
				"0x00007f4475904173: hlt",
				"0x00007f4475904174: hlt",
				"0x00007f4475904175: hlt",
				"0x00007f4475904176: hlt",
				"0x00007f4475904177: hlt",
				"0x00007f4475904178: hlt",
				"0x00007f4475904179: hlt",
				"0x00007f447590417a: hlt",
				"0x00007f447590417b: hlt",
				"0x00007f447590417c: hlt",
				"0x00007f447590417d: hlt",
				"0x00007f447590417e: hlt",
				"0x00007f447590417f: hlt",
				"[Exception Handler]",
				"[Stub Code]",
				"0x00007f4475904180: jmpq 0x00007f44758ed2a0  ;   {no_reloc}",
				"[Deopt Handler Code]",
				"0x00007f4475904185: callq 0x00007f447590418a",
				"0x00007f447590418a: subq   $0x5,(%rsp)",
				"0x00007f447590418f: jmpq 0x00007f44758c6b00  ;   {runtime_call}",
				"0x00007f4475904194: hlt",
				"0x00007f4475904195: hlt",
				"0x00007f4475904196: hlt",
				"0x00007f4475904197: hlt", });
	}

	@Test
	public void testAssemblyParseBrokenHeader()
	{
		testAssemblyParse(new String[] {
				"# {method} &apos;add&apos; &apos;(JJ)J&apos; in &apos;org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog&apos;",
				"# this:     rsi:rsi   = &apos;org/adoptopenjdk/jitwatch",
				"/demo/MakeHotSpotLog&apos;",
				"# parm0:    rdx:rdx   = long",
				"# parm1:    rcx:rcx   = long",
				"#           [sp+0x20]  (sp of caller)",
				"0x00007f4475904140: mov  0x8(%rsi),%r10d",
				"0x00007f4475904144: cmp    %r10,%rax",
				"0x00007f4475904147: jne  0x00007f44758c5960  ;   {runtime_call}",
				"0x00007f447590414d: data32 xchg %ax,%ax",
				"[Verified Entry Point]",
				"0x00007f4475904150: sub    $0x18,%rsp",
				"0x00007f4475904157: mov    %rbp,0x10(%rsp)    ;*synchronization entry",
				"                                                ; - org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog::add@-1 (line 144)",
				"0x00007f447590415c: mov    %rdx,%rax",
				"0x00007f447590415f: add    %rcx,%rax          ;*ladd",
				"                                                ; - org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog::add@2 (line 144)",
				"0x00007f4475904162: add    $0x10,%rsp",
				"0x00007f4475904166: pop    %rbp",
				"0x00007f4475904167: test   %eax,0x5ce1e93(%rip)      # 0x00007f447b5e6000",
				"                                                ;   {poll_return}",
				"0x00007f447590416d: retq",
				"0x00007f447590416e: hlt",
				"0x00007f447590416f: hlt",
				"0x00007f4475904170: hlt",
				"0x00007f4475904171: hlt",
				"0x00007f4475904172: hlt",
				"0x00007f4475904173: hlt",
				"0x00007f4475904174: hlt",
				"0x00007f4475904175: hlt",
				"0x00007f4475904176: hlt",
				"0x00007f4475904177: hlt",
				"0x00007f4475904178: hlt",
				"0x00007f4475904179: hlt",
				"0x00007f447590417a: hlt",
				"0x00007f447590417b: hlt",
				"0x00007f447590417c: hlt",
				"0x00007f447590417d: hlt",
				"0x00007f447590417e: hlt",
				"0x00007f447590417f: hlt",
				"[Exception Handler]",
				"[Stub Code]",
				"0x00007f4475904180: jmpq 0x00007f44758ed2a0  ;   {no_reloc}",
				"[Deopt Handler Code]",
				"0x00007f4475904185: callq 0x00007f447590418a",
				"0x00007f447590418a: subq   $0x5,(%rsp)",
				"0x00007f447590418f: jmpq 0x00007f44758c6b00  ;   {runtime_call}",
				"0x00007f4475904194: hlt",
				"0x00007f4475904195: hlt",
				"0x00007f4475904196: hlt",
				"0x00007f4475904197: hlt", });
	}

	@Test
	public void testAssemblyParseBrokenInsn()
	{
		testAssemblyParse(new String[] {
				"# {method} &apos;add&apos; &apos;(JJ)J&apos; in &apos;org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog&apos;",
				"# this:     rsi:rsi   = &apos;org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog&apos;",
				"# parm0:    rdx:rdx   = long",
				"# parm1:    rcx:rcx   = long",
				"#           [sp+0x20]  (sp of caller)",
				"0x00007f4475904140: mov  0x8(%rsi),%r10d",
				"0x00007f4475904144: cmp    %r10,%rax",
				"0x00007f4475904147: jne  0x00007f44758c5960  ;   {runtime_call}",
				"0x00007f447590414d: data32 xchg %ax,%ax",
				"[Verified Entry Point]",
				"0x00007f4475904150: sub    $0x18,%rsp",
				"0x00007f4475904157: mov    %rbp,0x10(%rsp)    ;*synchronization entry",
				"                                                ; - org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog::add@-1 (line 144)",
				"0x00007f447590415c: mov    %rdx,",
				"%rax",
				"0x00007f447590415f: add    %rcx,%rax          ;*ladd",
				"                                                ; - org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog::add@2 (line 144)",
				"0x00007f4475904162: add    $0x10,%rsp",
				"0x00007f4475904166: pop    %rbp",
				"0x00007f4475904167: test   %eax,0x5ce1e93(%rip)      # 0x00007f447b5e6000",
				"                                                ;   {poll_return}",
				"0x00007f447590416d: retq",
				"0x00007f447590416e: hlt",
				"0x00007f447590416f: hlt",
				"0x00007f4475904170: hlt",
				"0x00007f4475904171: hlt",
				"0x00007f4475904172: hlt",
				"0x00007f4475904173: hlt",
				"0x00007f4475904174: hlt",
				"0x00007f4475904175: hlt",
				"0x00007f4475904176: hlt",
				"0x00007f4475904177: hlt",
				"0x00007f4475904178: hlt",
				"0x00007f4475904179: hlt",
				"0x00007f447590417a: hlt",
				"0x00007f447590417b: hlt",
				"0x00007f447590417c: hlt",
				"0x00007f447590417d: hlt",
				"0x00007f447590417e: hlt",
				"0x00007f447590417f: hlt",
				"[Exception Handler]",
				"[Stub Code]",
				"0x00007f4475904180: jmpq 0x00007f44758ed2a0  ;   {no_reloc}",
				"[Deopt Handler Code]",
				"0x00007f4475904185: callq 0x00007f447590418a",
				"0x00007f447590418a: subq   $0x5,(%rsp)",
				"0x00007f447590418f: jmpq 0x00007f44758c6b00  ;   {runtime_call}",
				"0x00007f4475904194: hlt",
				"0x00007f4475904195: hlt",
				"0x00007f4475904196: hlt",
				"0x00007f4475904197: hlt", });
	}

	@Test
	public void testAssemblyParseBrokenComment()
	{
		testAssemblyParse(new String[] {
				"# {method} &apos;add&apos; &apos;(JJ)J&apos; in &apos;org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog&apos;",
				"# this:     rsi:rsi   = &apos;org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog&apos;",
				"# parm0:    rdx:rdx   = long",
				"# parm1:    rcx:rcx   = long",
				"#           [sp+0x20]  (sp of caller)",
				"0x00007f4475904140: mov  0x8(%rsi),%r10d",
				"0x00007f4475904144: cmp    %r10,%rax",
				"0x00007f4475904147: jne  0x00007f44758c5960  ;   {runtime_call}",
				"0x00007f447590414d: data32 xchg %ax,%ax",
				"[Verified Entry Point]",
				"0x00007f4475904150: sub    $0x18,%rsp",
				"0x00007f4475904157: mov    %rbp,0x10(%rsp)    ;*synchronization entry",
				"                                                ; - org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog::add@-1 (line 144)",
				"0x00007f447590415c: mov    %rdx,%rax",
				"0x00007f447590415f: add    %rcx,%rax          ;*ladd",
				"                                                ; ",
				"- org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog::add@2 (line 144)",
				"0x00007f4475904162: add    $0x10,%rsp",
				"0x00007f4475904166: pop    %rbp",
				"0x00007f4475904167: test   %eax,0x5ce1e93(%rip)      # 0x00007f447b5e6000",
				"                                                ;   {poll_return}",
				"0x00007f447590416d: retq",
				"0x00007f447590416e: hlt",
				"0x00007f447590416f: hlt",
				"0x00007f4475904170: hlt",
				"0x00007f4475904171: hlt",
				"0x00007f4475904172: hlt",
				"0x00007f4475904173: hlt",
				"0x00007f4475904174: hlt",
				"0x00007f4475904175: hlt",
				"0x00007f4475904176: hlt",
				"0x00007f4475904177: hlt",
				"0x00007f4475904178: hlt",
				"0x00007f4475904179: hlt",
				"0x00007f447590417a: hlt",
				"0x00007f447590417b: hlt",
				"0x00007f447590417c: hlt",
				"0x00007f447590417d: hlt",
				"0x00007f447590417e: hlt",
				"0x00007f447590417f: hlt",
				"[Exception Handler]",
				"[Stub Code]",
				"0x00007f4475904180: jmpq 0x00007f44758ed2a0  ;   {no_reloc}",
				"[Deopt Handler Code]",
				"0x00007f4475904185: callq 0x00007f447590418a",
				"0x00007f447590418a: subq   $0x5,(%rsp)",
				"0x00007f447590418f: jmpq 0x00007f44758c6b00  ;   {runtime_call}",
				"0x00007f4475904194: hlt",
				"0x00007f4475904195: hlt",
				"0x00007f4475904196: hlt",
				"0x00007f4475904197: hlt", });
	}

	public void testAssemblyParse(String[] asm)
	{
		StringBuilder builder = new StringBuilder();

		for (String line : asm)
		{
			builder.append(line).append(S_NEWLINE);
		}

		IAssemblyParser parser = AssemblyUtil.getParserForArchitecture(Architecture.X86_64);

		AssemblyMethod asmMethod = parser.parseAssembly(builder.toString());

		String header = asmMethod.getHeader();

		assertNotNull(header);
		assertEquals("# this:     rsi:rsi   = 'org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog'", header.split("\n")[1]);

		List<AssemblyBlock> blocks = asmMethod.getBlocks();

		assertEquals(5, blocks.size());

		AssemblyBlock block0 = blocks.get(0);
		assertEquals("[Entry Point]", block0.getTitle());

		List<AssemblyInstruction> instructions0 = block0.getInstructions();
		assertEquals(4, instructions0.size());

		AssemblyBlock block1 = blocks.get(1);
		assertEquals("[Verified Entry Point]", block1.getTitle());

		List<AssemblyInstruction> instructions1 = block1.getInstructions();
		assertEquals(26, instructions1.size());
		assertEquals(2, instructions1.get(2).getOperands().size());
		assertEquals(";*ladd\n; - org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog::add@2 (line 144)",
				instructions1.get(3).getComment());

		AssemblyBlock block2 = blocks.get(2);
		assertEquals("[Exception Handler]", block2.getTitle());

		List<AssemblyInstruction> instructions2 = block2.getInstructions();
		assertEquals(0, instructions2.size());

		AssemblyBlock block3 = blocks.get(3);
		assertEquals("[Stub Code]", block3.getTitle());

		List<AssemblyInstruction> instructions3 = block3.getInstructions();
		assertEquals(1, instructions3.size());

		AssemblyBlock block4 = blocks.get(4);
		assertEquals("[Deopt Handler Code]", block4.getTitle());

		List<AssemblyInstruction> instructions4 = block4.getInstructions();
		assertEquals(7, instructions4.size());
	}

	@Test
	public void testInstructionParse()
	{
		String line = "0x00007f4475904140: mov  0x8(%rsi),%r10d ;comment";

		IAssemblyParser parser = AssemblyUtil.getParserForArchitecture(Architecture.X86_64);

		AssemblyInstruction instr = parser.createInstruction(new AssemblyLabels(), line);

		assertNotNull(instr);

		assertEquals(Long.parseLong("7f4475904140", 16), instr.getAddress());

		assertEquals("mov", instr.getMnemonic());

		List<String> operands = instr.getOperands();

		assertEquals(2, operands.size());

		assertEquals("0x8(%rsi)", operands.get(0));
		assertEquals("%r10d", operands.get(1));

		assertEquals(";comment", instr.getComment());
	}

	@Test
	public void testInstructionParseWithModifier()
	{
		String line = "0x00007f447590414d: data32 xchg %ax,%ax";

		IAssemblyParser parser = AssemblyUtil.getParserForArchitecture(Architecture.X86_64);

		AssemblyInstruction instr = parser.createInstruction(new AssemblyLabels(), line);

		assertNotNull(instr);

		assertEquals(Long.parseLong("7f447590414d", 16), instr.getAddress());

		assertEquals(1, instr.getPrefixes().size());

		assertEquals("data32", instr.getPrefixes().get(0));

		assertEquals("xchg", instr.getMnemonic());

		List<String> operands = instr.getOperands();

		assertEquals(2, operands.size());

		assertEquals("%ax", operands.get(0));
		assertEquals("%ax", operands.get(1));

		assertEquals(S_EMPTY, instr.getComment());
	}

	@Test
	public void testInstructionParseNoOperands()
	{
		String line = "0x00007f447590416e: hlt";

		IAssemblyParser parser = AssemblyUtil.getParserForArchitecture(Architecture.X86_64);

		AssemblyInstruction instr = parser.createInstruction(new AssemblyLabels(), line);

		assertNotNull(instr);

		assertEquals(Long.parseLong("7f447590416e", 16), instr.getAddress());

		assertEquals(0, instr.getPrefixes().size());

		assertEquals("hlt", instr.getMnemonic());

		List<String> operands = instr.getOperands();

		assertEquals(0, operands.size());

		assertEquals(S_EMPTY, instr.getComment());
	}

	@Test
	public void testInstructionParseMultiplePrefixes()
	{
		String line = "0x00007fbbc41082e5: data32 data32 nopw 0x0(%rax,%rax,1)";

		IAssemblyParser parser = AssemblyUtil.getParserForArchitecture(Architecture.X86_64);

		AssemblyInstruction instr = parser.createInstruction(new AssemblyLabels(), line);

		assertNotNull(instr);

		assertEquals(Long.parseLong("7fbbc41082e5", 16), instr.getAddress());

		assertEquals(2, instr.getPrefixes().size());

		assertEquals("data32", instr.getPrefixes().get(0));
		assertEquals("data32", instr.getPrefixes().get(1));

		assertEquals("nopw", instr.getMnemonic());

		List<String> operands = instr.getOperands();

		assertEquals(1, operands.size());

		assertEquals("0x0(%rax,%rax,1)", operands.get(0));
	}

	@Test
	public void testInstructionParseRegression1()
	{
		String line = "0x00007f54f9bfd2f0: mov    %eax,-0x14000(%rsp)";

		IAssemblyParser parser = AssemblyUtil.getParserForArchitecture(Architecture.X86_64);

		AssemblyInstruction instr = parser.createInstruction(new AssemblyLabels(), line);

		assertNotNull(instr);

		assertEquals(Long.parseLong("7f54f9bfd2f0", 16), instr.getAddress());

		assertEquals(0, instr.getPrefixes().size());

		assertEquals("mov", instr.getMnemonic());

		List<String> operands = instr.getOperands();

		assertEquals(2, operands.size());

		assertEquals("%eax", operands.get(0));
		assertEquals("-0x14000(%rsp)", operands.get(1));
	}

	@Test
	public void testInstructionParseIntelFormat()
	{
		String line = "0x0000000110aa2ee5: mov    QWORD PTR [rsp+0x78],rax";

		IAssemblyParser parser = AssemblyUtil.getParserForArchitecture(Architecture.X86_64);

		AssemblyInstruction instr = parser.createInstruction(new AssemblyLabels(), line);

		assertNotNull(instr);

		assertEquals(Long.parseLong("110aa2ee5", 16), instr.getAddress());

		assertEquals(0, instr.getPrefixes().size());

		assertEquals("mov", instr.getMnemonic());

		List<String> operands = instr.getOperands();

		assertEquals(2, operands.size());

		assertEquals("QWORD PTR [rsp+0x78]", operands.get(0));
		assertEquals("rax", operands.get(1));
	}

	@Test
	public void testInstructionParseIntelFormatWithPrefixes()
	{
		String line = "0x0000000110aa2ee5: data32 data32 nop WORD PTR [rax+rax*1+0x0]";

		IAssemblyParser parser = AssemblyUtil.getParserForArchitecture(Architecture.X86_64);

		AssemblyInstruction instr = parser.createInstruction(new AssemblyLabels(), line);

		assertNotNull(instr);

		assertEquals(Long.parseLong("110aa2ee5", 16), instr.getAddress());

		assertEquals(2, instr.getPrefixes().size());

		assertEquals("data32", instr.getPrefixes().get(0));
		assertEquals("data32", instr.getPrefixes().get(1));

		assertEquals("nop", instr.getMnemonic());

		List<String> operands = instr.getOperands();

		assertEquals(1, operands.size());

		assertEquals("WORD PTR [rax+rax*1+0x0]", operands.get(0));
	}

	@Test
	public void testInstructionParseIntelFormatWithPrefixes2()
	{
		String line = "0x00000001024a210c: lock idiv DWORD PTR [rax]";

		IAssemblyParser parser = AssemblyUtil.getParserForArchitecture(Architecture.X86_64);

		AssemblyInstruction instr = parser.createInstruction(new AssemblyLabels(), line);

		assertNotNull(instr);

		assertEquals(Long.parseLong("1024a210c", 16), instr.getAddress());

		assertEquals(1, instr.getPrefixes().size());

		assertEquals("lock", instr.getPrefixes().get(0));

		assertEquals("idiv", instr.getMnemonic());

		List<String> operands = instr.getOperands();

		assertEquals(1, operands.size());

		assertEquals("DWORD PTR [rax]", operands.get(0));
	}

	@Test
	public void testInstructionParseIntelFormatWithPrefixes3()
	{
		String line = "0x00000001024a18bc: data16 data16 xchg ax,ax";

		IAssemblyParser parser = AssemblyUtil.getParserForArchitecture(Architecture.X86_64);

		AssemblyInstruction instr = parser.createInstruction(new AssemblyLabels(), line);

		assertNotNull(instr);

		assertEquals(Long.parseLong("1024a18bc", 16), instr.getAddress());

		assertEquals(2, instr.getPrefixes().size());

		assertEquals("data16", instr.getPrefixes().get(0));
		assertEquals("data16", instr.getPrefixes().get(1));

		assertEquals("xchg", instr.getMnemonic());

		List<String> operands = instr.getOperands();

		assertEquals(2, operands.size());

		assertEquals("ax", operands.get(0));
		assertEquals("ax", operands.get(1));
	}

	@Test
	public void testInstructionParseIntelFormatStubCall()
	{
		String line = "0x0000000106d035e0: call   Stub::jshort_disjoint_arraycopy";

		IAssemblyParser parser = AssemblyUtil.getParserForArchitecture(Architecture.X86_64);

		AssemblyInstruction instr = parser.createInstruction(new AssemblyLabels(), line);

		assertNotNull(instr);

		assertEquals(Long.parseLong("106d035e0", 16), instr.getAddress());

		assertEquals(0, instr.getPrefixes().size());

		assertEquals("call", instr.getMnemonic());

		List<String> operands = instr.getOperands();

		assertEquals(1, operands.size());

		assertEquals("Stub::jshort_disjoint_arraycopy", operands.get(0));
	}

	private void testOperand(OperandType type, String mnemonic, String operand)
	{
		IAssemblyParser parser = AssemblyUtil.getParserForArchitecture(Architecture.X86_64);

		switch (type)
		{
		case ADDRESS:
			assertTrue(parser.isAddress(mnemonic, operand));
			assertFalse(parser.isConstant(mnemonic, operand));
			assertFalse(parser.isRegister(mnemonic, operand));
			break;
		case CONSTANT:
			assertFalse(parser.isAddress(mnemonic, operand));
			assertTrue(parser.isConstant(mnemonic, operand));
			assertFalse(parser.isRegister(mnemonic, operand));
			break;
		case REGISTER:
			assertFalse(parser.isAddress(mnemonic, operand));
			assertFalse(parser.isConstant(mnemonic, operand));
			assertTrue(parser.isRegister(mnemonic, operand));
			break;
		}
	}

	@Test
	public void testIdentifyOperands()
	{
		testOperand(OperandType.ADDRESS, "callq", "0x00000001118fb8e0");
		testOperand(OperandType.ADDRESS, "jmpq", "0x00000001118473c0");
		testOperand(OperandType.ADDRESS, "call", "2f9f160h");

		testOperand(OperandType.CONSTANT, "movabs", "0x00000001118473c0");
		testOperand(OperandType.CONSTANT, "movabs", "0x11d38f7f0");
		testOperand(OperandType.CONSTANT, "movabs", "$0x0");
		testOperand(OperandType.CONSTANT, "movabs", "0x0");
		testOperand(OperandType.CONSTANT, "mov", "118h");

		testOperand(OperandType.REGISTER, "mov", "0x48(%rsp)");
		testOperand(OperandType.REGISTER, "mov", "QWORD PTR [rsp+0x60]");
		testOperand(OperandType.REGISTER, "pop", "rbp");
		testOperand(OperandType.REGISTER, "pop", "%rbp");
		testOperand(OperandType.REGISTER, "mov", "-0x14000(%rsp)");
		testOperand(OperandType.REGISTER, "lea", "[rdi+1h]");
		testOperand(OperandType.REGISTER, "mov", "dword ptr [rsp+0ffffffffffffa000h]");
	}

	@Test
	public void testExtractRegisterName()
	{
		IAssemblyParser parser = AssemblyUtil.getParserForArchitecture(Architecture.X86_64);

		assertEquals("rsp", parser.extractRegisterName("rsp"));
		assertEquals("rbp", parser.extractRegisterName("%rbp"));
		assertEquals("r10", parser.extractRegisterName("*%r10"));
		assertEquals("rsp", parser.extractRegisterName("QWORD PTR [rsp+0x60]"));
		assertEquals("rsp", parser.extractRegisterName("-0x14000(%rsp)"));
		assertEquals("rax", parser.extractRegisterName("[rax+rax*1+0x0]"));
		assertEquals("rdx", parser.extractRegisterName("(%rdx,%rbx,1)"));
	}

	@Test
	public void testParseJDK9Assembly()
	{

		// <nmethod compile_id='2' compiler='c1' level='3'
		// entry='0x00007fa38a13d640' size='1288' address='0x00007fa38a13d490'
		// relocation_offset='368' insts_offset='432' stub_offset='816'
		// scopes_data_offset='968' scopes_pcs_offset='1072'
		// dependencies_offset='1264' nul_chk_table_offset='1272'
		// metadata_offset='960' method='java.lang.StringLatin1 hashCode ([B)I'
		// bytes='42' count='115' backedge_count='2257' iicount='115'
		// stamp='0.039'/>

		String[] lines = new String[] {

//				"<print_nmethod stamp='0.039'>",
				"Compiled method (c1)      39    2       3       java.lang.StringLatin1::hashCode (42 bytes)",
				" total in heap  [0x00007fa38a13d490,0x00007fa38a13d998] = 1288",
				" relocation     [0x00007fa38a13d600,0x00007fa38a13d638] = 56",
				" main code      [0x00007fa38a13d640,0x00007fa38a13d7c0] = 384",
				" stub code      [0x00007fa38a13d7c0,0x00007fa38a13d850] = 144",
				" metadata       [0x00007fa38a13d850,0x00007fa38a13d858] = 8",
				" scopes data    [0x00007fa38a13d858,0x00007fa38a13d8c0] = 104",
				" scopes pcs     [0x00007fa38a13d8c0,0x00007fa38a13d980] = 192",
				" dependencies   [0x00007fa38a13d980,0x00007fa38a13d988] = 8",
				" nul chk table  [0x00007fa38a13d988,0x00007fa38a13d998] = 16",
				"----------------------------------------------------------------------",
				"java/lang/StringLatin1.hashCode([B)I  [0x00007fa38a13d640, 0x00007fa38a13d850]  528 bytes",
				"[Entry Point]",
				"[Verified Entry Point]",
				"[Constants]",
				"  # {method} {0x00007fa29d92e888} &apos;hashCode&apos; &apos;([B)I&apos; in &apos;java/lang/StringLatin1&apos;",
				"  # parm0:    rsi:rsi   = &apos;[B&apos;",
				"  #           [sp+0x40]  (sp of caller)",
				"  0x00007fa38a13d640: mov    %eax,-0x14000(%rsp)",
				"  0x00007fa38a13d647: push   %rbp",
				"  0x00007fa38a13d648: sub    $0x30,%rsp",
				"  0x00007fa38a13d64c: movabs $0x7fa29da725e8,%rax  ;   {metadata(method data for {method} {0x00007fa29d92e888} &apos;hashCode&apos; &apos;([B)I&apos; in &apos;java/lang/StringLatin1&apos;)}",
				"  0x00007fa38a13d656: mov    0xfc(%rax),%edi",
				"  0x00007fa38a13d65c: add    $0x8,%edi",
				"  0x00007fa38a13d65f: mov    %edi,0xfc(%rax)",
				"  0x00007fa38a13d665: and    $0x1ff8,%edi",
				"  0x00007fa38a13d66b: cmp    $0x0,%edi",
				"  0x00007fa38a13d66e: je     0x00007fa38a13d727  ;*iconst_0 {reexecute=0 rethrow=0 return_oop=0}",
				"                                                ; - java.lang.StringLatin1::hashCode@0 (line 173)",
				"",
				"  0x00007fa38a13d674: mov    0x10(%rsi),%eax    ;*arraylength {reexecute=0 rethrow=0 return_oop=0}",
				"                                                ; - java.lang.StringLatin1::hashCode@5 (line 174)",
				"                                                ; implicit exception: dispatches to 0x00007fa38a13d748",
				"  0x00007fa38a13d677: mov    $0x0,%edi",
				"  0x00007fa38a13d67c: mov    $0x0,%ebx",
				"  0x00007fa38a13d681: jmpq   0x00007fa38a13d6e0  ;*iload {reexecute=0 rethrow=0 return_oop=0}",
				"                                                ; - java.lang.StringLatin1::hashCode@10 (line 174)",
				"",
				"  0x00007fa38a13d686: xchg   %ax,%ax",
				"  0x00007fa38a13d688: movslq %edi,%rdx",
				"  0x00007fa38a13d68b: movsbl 0x18(%rsi,%rdx,1),%edx  ;*baload {reexecute=0 rethrow=0 return_oop=0}",
				"                                                ; - java.lang.StringLatin1::hashCode@19 (line 174)",
				"",
				"  0x00007fa38a13d690: mov    %rbx,%rcx",
				"  0x00007fa38a13d693: shl    $0x5,%ebx",
				"  0x00007fa38a13d696: sub    %ecx,%ebx",
				"  0x00007fa38a13d698: and    $0xff,%edx",
				"  0x00007fa38a13d69e: add    %edx,%ebx",
				"  0x00007fa38a13d6a0: inc    %edi",
				"  0x00007fa38a13d6a2: movabs $0x7fa29da725e8,%rdx  ;   {metadata(method data for {method} {0x00007fa29d92e888} &apos;hashCode&apos; &apos;([B)I&apos; in &apos;java/lang/StringLatin1&apos;)}",
				"  0x00007fa38a13d6ac: mov    0x100(%rdx),%ecx",
				"  0x00007fa38a13d6b2: add    $0x8,%ecx",
				"  0x00007fa38a13d6b5: mov    %ecx,0x100(%rdx)",
				"  0x00007fa38a13d6bb: and    $0xfff8,%ecx",
				"  0x00007fa38a13d6c1: cmp    $0x0,%ecx",
				"  0x00007fa38a13d6c4: je     0x00007fa38a13d74d  ; ImmutableOopMap{rsi=Oop }",
				"                                                ;*goto {reexecute=1 rethrow=0 return_oop=0}",
				"                                                ; - java.lang.StringLatin1::hashCode@37 (line 174)",
				"",
				"  0x00007fa38a13d6ca: test   %eax,0x229d9930(%rip)        # 0x00007fa3acb17000",
				"                                                ;   {poll}",
				"  0x00007fa38a13d6d0: movabs $0x7fa29da725e8,%rdx  ;   {metadata(method data for {method} {0x00007fa29d92e888} &apos;hashCode&apos; &apos;([B)I&apos; in &apos;java/lang/StringLatin1&apos;)}",
				"  0x00007fa38a13d6da: incl   0x158(%rdx)        ;*goto {reexecute=0 rethrow=0 return_oop=0}",
				"                                                ; - java.lang.StringLatin1::hashCode@37 (line 174)",
				"",
				"  0x00007fa38a13d6e0: cmp    %eax,%edi",
				"  0x00007fa38a13d6e2: movabs $0x7fa29da725e8,%rdx  ;   {metadata(method data for {method} {0x00007fa29d92e888} &apos;hashCode&apos; &apos;([B)I&apos; in &apos;java/lang/StringLatin1&apos;)}",
				"  0x00007fa38a13d6ec: movabs $0x148,%rcx",
				"  0x00007fa38a13d6f6: jl     0x00007fa38a13d706",
				"  0x00007fa38a13d6fc: movabs $0x138,%rcx",
				"  0x00007fa38a13d706: mov    (%rdx,%rcx,1),%r8",
				"  0x00007fa38a13d70a: lea    0x1(%r8),%r8",
				"  0x00007fa38a13d70e: mov    %r8,(%rdx,%rcx,1)",
				"  0x00007fa38a13d712: jl     0x00007fa38a13d688  ;*if_icmpge {reexecute=0 rethrow=0 return_oop=0}",
				"                                                ; - java.lang.StringLatin1::hashCode@13 (line 174)",
				"",
				"  0x00007fa38a13d718: mov    %rbx,%rax",
				"  0x00007fa38a13d71b: add    $0x30,%rsp",
				"  0x00007fa38a13d71f: pop    %rbp",
				"  0x00007fa38a13d720: test   %eax,0x229d98da(%rip)        # 0x00007fa3acb17000",
				"                                                ;   {poll_return}",
				"  0x00007fa38a13d726: retq   ",
				"  0x00007fa38a13d727: movabs $0x7fa29d92e888,%r10  ;   {metadata({method} {0x00007fa29d92e888} &apos;hashCode&apos; &apos;([B)I&apos; in &apos;java/lang/StringLatin1&apos;)}",
				"  0x00007fa38a13d731: mov    %r10,0x8(%rsp)",
				"  0x00007fa38a13d736: movq   $0xffffffffffffffff,(%rsp)",
				"  0x00007fa38a13d73e: callq  0x00007fa389cc1200  ; ImmutableOopMap{rsi=Oop }",
				"                                                ;*synchronization entry",
				"                                                ; - java.lang.StringLatin1::hashCode@-1 (line 173)",
				"                                                ;   {runtime_call counter_overflow Runtime1 stub}",
				"  0x00007fa38a13d743: jmpq   0x00007fa38a13d674",
				"  0x00007fa38a13d748: callq  0x00007fa389cbc420  ; ImmutableOopMap{rsi=Oop }",
				"                                                ;*arraylength {reexecute=0 rethrow=0 return_oop=0}",
				"                                                ; - java.lang.StringLatin1::hashCode@5 (line 174)",
				"                                                ;   {runtime_call throw_null_pointer_exception Runtime1 stub}",
				"  0x00007fa38a13d74d: movabs $0x7fa29d92e888,%r10  ;   {metadata({method} {0x00007fa29d92e888} &apos;hashCode&apos; &apos;([B)I&apos; in &apos;java/lang/StringLatin1&apos;)}",
				"  0x00007fa38a13d757: mov    %r10,0x8(%rsp)",
				"  0x00007fa38a13d75c: movq   $0x25,(%rsp)",
				"  0x00007fa38a13d764: callq  0x00007fa389cc1200  ; ImmutableOopMap{rsi=Oop }",
				"                                                ;*goto {reexecute=1 rethrow=0 return_oop=0}",
				"                                                ; - java.lang.StringLatin1::hashCode@37 (line 174)",
				"                                                ;   {runtime_call counter_overflow Runtime1 stub}",
				"  0x00007fa38a13d769: jmpq   0x00007fa38a13d6ca",
				"  0x00007fa38a13d76e: nop",
				"  0x00007fa38a13d76f: nop",
				"  0x00007fa38a13d770: mov    0x2f8(%r15),%rax",
				"  0x00007fa38a13d777: movabs $0x0,%r10",
				"  0x00007fa38a13d781: mov    %r10,0x2f8(%r15)",
				"  0x00007fa38a13d788: movabs $0x0,%r10",
				"  0x00007fa38a13d792: mov    %r10,0x300(%r15)",
				"  0x00007fa38a13d799: add    $0x30,%rsp",
				"  0x00007fa38a13d79d: pop    %rbp",
				"  0x00007fa38a13d79e: jmpq   0x00007fa389cbb980  ;   {runtime_call unwind_exception Runtime1 stub}",
				"  0x00007fa38a13d7a3: hlt    ",
				"  0x00007fa38a13d7a4: hlt    ",
				"  0x00007fa38a13d7a5: hlt    ",
				"  0x00007fa38a13d7a6: hlt    ",
				"  0x00007fa38a13d7a7: hlt    ",
				"  0x00007fa38a13d7a8: hlt    ",
				"  0x00007fa38a13d7a9: hlt    ",
				"  0x00007fa38a13d7aa: hlt    ",
				"  0x00007fa38a13d7ab: hlt    ",
				"  0x00007fa38a13d7ac: hlt    ",
				"  0x00007fa38a13d7ad: hlt    ",
				"  0x00007fa38a13d7ae: hlt    ",
				"  0x00007fa38a13d7af: hlt    ",
				"  0x00007fa38a13d7b0: hlt    ",
				"  0x00007fa38a13d7b1: hlt    ",
				"  0x00007fa38a13d7b2: hlt    ",
				"  0x00007fa38a13d7b3: hlt    ",
				"  0x00007fa38a13d7b4: hlt    ",
				"  0x00007fa38a13d7b5: hlt    ",
				"  0x00007fa38a13d7b6: hlt    ",
				"  0x00007fa38a13d7b7: hlt    ",
				"  0x00007fa38a13d7b8: hlt    ",
				"  0x00007fa38a13d7b9: hlt    ",
				"  0x00007fa38a13d7ba: hlt    ",
				"  0x00007fa38a13d7bb: hlt    ",
				"  0x00007fa38a13d7bc: hlt    ",
				"  0x00007fa38a13d7bd: hlt    ",
				"  0x00007fa38a13d7be: hlt    ",
				"  0x00007fa38a13d7bf: hlt    ",
				"[Exception Handler]",
				"[Stub Code]",
				"  0x00007fa38a13d7c0: callq  0x00007fa389cbdf80  ;   {no_reloc}",
				"  0x00007fa38a13d7c5: mov    %rsp,-0x28(%rsp)",
				"  0x00007fa38a13d7ca: sub    $0x80,%rsp",
				"  0x00007fa38a13d7d1: mov    %rax,0x78(%rsp)",
				"  0x00007fa38a13d7d6: mov    %rcx,0x70(%rsp)",
				"  0x00007fa38a13d7db: mov    %rdx,0x68(%rsp)",
				"  0x00007fa38a13d7e0: mov    %rbx,0x60(%rsp)",
				"  0x00007fa38a13d7e5: mov    %rbp,0x50(%rsp)",
				"  0x00007fa38a13d7ea: mov    %rsi,0x48(%rsp)",
				"  0x00007fa38a13d7ef: mov    %rdi,0x40(%rsp)",
				"  0x00007fa38a13d7f4: mov    %r8,0x38(%rsp)",
				"  0x00007fa38a13d7f9: mov    %r9,0x30(%rsp)",
				"  0x00007fa38a13d7fe: mov    %r10,0x28(%rsp)",
				"  0x00007fa38a13d803: mov    %r11,0x20(%rsp)",
				"  0x00007fa38a13d808: mov    %r12,0x18(%rsp)",
				"  0x00007fa38a13d80d: mov    %r13,0x10(%rsp)",
				"  0x00007fa38a13d812: mov    %r14,0x8(%rsp)",
				"  0x00007fa38a13d817: mov    %r15,(%rsp)",
				"  0x00007fa38a13d81b: movabs $0x7fa3ab715afa,%rdi  ;   {external_word}",
				"  0x00007fa38a13d825: movabs $0x7fa38a13d7c5,%rsi  ;   {internal_word}",
				"  0x00007fa38a13d82f: mov    %rsp,%rdx",
				"  0x00007fa38a13d832: and    $0xfffffffffffffff0,%rsp",
				"  0x00007fa38a13d836: callq  0x00007fa3ab3bf350  ;   {runtime_call MacroAssembler::debug64(char*, long, long*)}",
				"  0x00007fa38a13d83b: hlt    ",
				"[Deopt Handler Code]",
				"  0x00007fa38a13d83c: movabs $0x7fa38a13d83c,%r10  ;   {section_word}",
				"  0x00007fa38a13d846: push   %r10",
				"  0x00007fa38a13d848: jmpq   0x00007fa389bfa820  ;   {runtime_call DeoptimizationBlob}",
				"  0x00007fa38a13d84d: hlt    ",
				"  0x00007fa38a13d84e: hlt    ",
				"  0x00007fa38a13d84f: hlt    ",
				"",
				"ImmutableOopMap{rsi=Oop }pc offsets: 138 259 269 297 </print_nmethod>" };

		StringBuilder builder = new StringBuilder();

		for (String line : lines)
		{
			builder.append(line).append(S_NEWLINE);
		}

		IAssemblyParser parser = AssemblyUtil.getParserForArchitecture(Architecture.X86_64);

		AssemblyMethod asmMethod = parser.parseAssembly(builder.toString());

		String header = asmMethod.getHeader();

		assertNotNull(header);
		assertEquals("# parm0:    rsi:rsi   = '[B'", header.split("\n")[1].trim());

		List<AssemblyBlock> blocks = asmMethod.getBlocks();
		assertEquals(5, blocks.size());


		AssemblyBlock block0 = blocks.get(0);
		assertEquals("[Entry Point]", block0.getTitle());

		List<AssemblyInstruction> instructions0 = block0.getInstructions();
		assertEquals(0, instructions0.size());

		AssemblyBlock block1 = blocks.get(1);
		assertEquals("[Verified Entry Point]", block1.getTitle());

		List<AssemblyInstruction> instructions1 = block1.getInstructions();
		assertEquals(97, instructions1.size());
		assertEquals(2, instructions1.get(2).getOperands().size());
		assertEquals(";   {metadata(method data for {method} {0x00007fa29d92e888} 'hashCode' '([B)I' in 'java/lang/StringLatin1')}",
				instructions1.get(3).getComment());

		AssemblyBlock block2 = blocks.get(2);
		assertEquals("[Exception Handler]", block2.getTitle());

		List<AssemblyInstruction> instructions2 = block2.getInstructions();
		assertEquals(0, instructions2.size());

		AssemblyBlock block3 = blocks.get(3);
		assertEquals("[Stub Code]", block3.getTitle());

		List<AssemblyInstruction> instructions3 = block3.getInstructions();
		assertEquals(24, instructions3.size());

		AssemblyBlock block4 = blocks.get(4);
		assertEquals("[Deopt Handler Code]", block4.getTitle());

		List<AssemblyInstruction> instructions4 = block4.getInstructions();
		assertEquals(6, instructions4.size());
	}
}
