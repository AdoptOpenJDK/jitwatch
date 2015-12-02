/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;
import static org.junit.Assert.*;

import java.util.List;

import org.adoptopenjdk.jitwatch.model.assembly.AssemblyBlock;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyInstruction;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyLabels;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyMethod;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyUtil;
import org.junit.Test;

public class TestAssemblyUtil
{
	@Test
	public void testAssemblyParse()
	{
		testAssemblyParse(new String[]{
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
				"0x00007f4475904197: hlt",
		});
	}

	@Test
	public void testAssemblyParseBrokenHeader()
	{
		testAssemblyParse(new String[]{
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
				"0x00007f4475904197: hlt",
		});
	}

	@Test
	public void testAssemblyParseBrokenInsn()
	{
		testAssemblyParse(new String[]{
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
				"0x00007f4475904197: hlt",
		});
	}

	@Test
	public void testAssemblyParseBrokenComment()
	{
		testAssemblyParse(new String[]{
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
				"0x00007f4475904197: hlt",
		});
	}

	public void testAssemblyParse(String[] asm)
	{
		StringBuilder builder = new StringBuilder();
		
		for (String line : asm)
		{
			builder.append(line).append(S_NEWLINE);
		}
		
		AssemblyMethod asmMethod = AssemblyUtil.parseAssembly(builder.toString());
		
		String header = asmMethod.getHeader();
		
		assertNotNull(header);
		assertEquals("# this:     rsi:rsi   = 'org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog'", header.split("\n")[1]);
		
		List<AssemblyBlock> blocks = asmMethod.getBlocks();
		
		assertEquals(5, blocks.size());
		
		AssemblyBlock block0 = blocks.get(0);
		assertNull(block0.getTitle());
		
		List<AssemblyInstruction> instructions0 = block0.getInstructions();
		assertEquals(4, instructions0.size());
		
		AssemblyBlock block1 = blocks.get(1);
		assertEquals("[Verified Entry Point]", block1.getTitle());
		
		List<AssemblyInstruction> instructions1 = block1.getInstructions();
		assertEquals(26, instructions1.size());
		assertEquals(2, instructions1.get(2).getOperands().size());
		assertEquals(";*ladd\n; - org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog::add@2 (line 144)", instructions1.get(3).getComment());
		
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
		
		AssemblyInstruction instr = AssemblyUtil.createInstruction(new AssemblyLabels(), line);
		
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
		
		AssemblyInstruction instr = AssemblyUtil.createInstruction(new AssemblyLabels(),line);
		
		assertNotNull(instr);
		
		assertEquals(Long.parseLong("7f447590414d", 16), instr.getAddress());
		
		assertEquals("data32", instr.getModifier());
		
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
		
		AssemblyInstruction instr = AssemblyUtil.createInstruction(new AssemblyLabels(), line);
		
		assertNotNull(instr);
		
		assertEquals(Long.parseLong("7f447590416e", 16), instr.getAddress());
		
		assertEquals(null, instr.getModifier());
		
		assertEquals("hlt", instr.getMnemonic());
		
		List<String> operands = instr.getOperands();
		
		assertEquals(0, operands.size());
		
		assertEquals(S_EMPTY, instr.getComment());		
	}
	
	@Test
	public void testInstructionParseMultiplePrefixes()
	{
		String line = "0x00007fbbc41082e5: data32 data32 nopw 0x0(%rax,%rax,1)";
		
		AssemblyInstruction instr = AssemblyUtil.createInstruction(new AssemblyLabels(), line);
		
		assertNotNull(instr);
		
		assertEquals(Long.parseLong("7fbbc41082e5", 16), instr.getAddress());
		
		assertEquals("data32 data32", instr.getModifier());
		
		assertEquals("nopw", instr.getMnemonic());
		
		List<String> operands = instr.getOperands();
		
		assertEquals(1, operands.size());
		
		assertEquals("0x0(%rax,%rax,1)", operands.get(0));		
	}
	
	@Test
	public void testInstructionParseRegression1()
	{
		String line = "0x00007f54f9bfd2f0: mov    %eax,-0x14000(%rsp)";
		
		AssemblyInstruction instr = AssemblyUtil.createInstruction(new AssemblyLabels(), line);
		
		assertNotNull(instr);
		
		assertEquals(Long.parseLong("7f54f9bfd2f0", 16), instr.getAddress());
		
		assertNull(instr.getModifier());
		
		assertEquals("mov", instr.getMnemonic());
		
		List<String> operands = instr.getOperands();
		
		assertEquals(2, operands.size());
		
		assertEquals("%eax", operands.get(0));	
		assertEquals("-0x14000(%rsp)", operands.get(1));		

	}
	
	
}