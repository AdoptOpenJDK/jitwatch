/*
 * Copyright (c) 2017 Chris Newland.
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

import org.junit.Test;

import java.util.List;

import org.adoptopenjdk.jitwatch.model.assembly.Architecture;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyBlock;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyInstruction;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyLabels;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyMethod;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyUtil;
import org.adoptopenjdk.jitwatch.model.assembly.IAssemblyParser;
import org.junit.Test;

public class TestAssemblyParserARM extends AbstractAssemblyTest
{
	@Test
	public void testAssemblyParse()
	{ 
		testAssemblyParse(new String[]
			{ 
				"# {method} {0x0000ffff50917ec8} 'toNarrowWidePattern' '([I)I' in 'com/google/zxing/oned/Code39Reader'",
				"# parm0:    c_rarg1:c_rarg1 ",
				"                        = '[I'",
				"#           [sp+0x40]  (sp of caller)",
				"0x0000ffff6d685400:   nop",
				"0x0000ffff6d685404:   sub	x9, sp, #0x14, lsl #12",
				"0x0000ffff6d685408:   str	xzr, [x9]",
				"0x0000ffff6d68540c:   sub	sp, sp, #0x40",
				"0x0000ffff6d685410:   stp	x29, x30, [sp, #48]",
				"0x0000ffff6d685414:   add	x29, sp, #0x30",
				"0x0000ffff6d685418:   mov	x0, #0x37b0                	// #14256",
				"                                                            ;   {metadata(method data for {method} {0x0000ffff50917ec8} 'toNarrowWidePattern' '([I)I' in 'com/google/zxing/oned/Code39Reader')}",
				"0x0000ffff6d68541c:   movk	x0, #0x5094, lsl #16",
				"0x0000ffff6d685420:   movk	x0, #0xffff, lsl #32",
				"0x0000ffff6d685424:   ldr	w2, [x0, #252]",
				"0x0000ffff6d685428:   add	w2, w2, #0x2",
				"0x0000ffff6d68542c:   str	w2, [x0, #252]",
				"0x0000ffff6d685430:   and	w2, w2, #0x7fe",
				"0x0000ffff6d685434:   cmp	w2, #0x0",
				"0x0000ffff6d685438:   b.eq	0x0000ffff6d685828  // b.none;*aload_0 {reexecute=0 rethrow=0 return_oop=0}",
				"                                                            ; - com.google.zxing.oned.Code39Reader::toNarrowWidePattern@0 (line 218)",
				"0x0000ffff6d68543c:   ldr	w0, [x1, #12]               ; implicit exception: dispatches to 0x0000ffff6d685848",
				"                                                            ;*arraylength {reexecute=0 rethrow=0 return_oop=0}",
				"                                                            ; - com.google.zxing.oned.Code39Reader::toNarrowWidePattern@1 (line 218)",
				"0x0000ffff6d685440:   movz	w2, #0x0, lsl #16           ;*ldc {reexecute=0 rethrow=0 return_oop=0}",
				"                                                            ; - com.google.zxing.oned.Code39Reader::toNarrowWidePattern@5 (line 222)",
				"0x0000ffff6d685444:   movz	w3, #0x0, lsl #16",
				"0x0000ffff6d685448:   orr	w4, wzr, #0x7fffffff        ;*iload {reexecute=0 rethrow=0 return_oop=0}",
				"                                                            ; - com.google.zxing.oned.Code39Reader::toNarrowWidePattern@20 (line 223)",
				"0x0000ffff6d68544c:   cmp	w3, w0",
				"0x0000ffff6d685450:   mov	x5, #0x37b0                	// #14256",
				"                                                            ;   {metadata(method data for {method} {0x0000ffff50917ec8} 'toNarrowWidePattern' '([I)I' in 'com/google/zxing/oned/Code39Reader')}",
				"0x0000ffff6d685454:   movk	x5, #0x5094, lsl #16",
				"0x0000ffff6d685458:   movk	x5, #0xffff, lsl #32",
				"0x0000ffff6d68545c:   mov	x8, #0x140                 	// #320",
				"0x0000ffff6d685460:   mov	x9, #0x150                 	// #336",
				"0x0000ffff6d685464:   csel	x6, x8, x9, ge  // ge = tcont",
				"0x0000ffff6d685468:   ldr	x7, [x5, x6]",
				"0x0000ffff6d68546c:   add	x7, x7, #0x1",
				"0x0000ffff6d685470:   str	x7, [x5, x6]",
				"0x0000ffff6d685474:   b.ge	0x0000ffff6d685530  // b.tcont;*if_icmpge {reexecute=0 rethrow=0 return_oop=0}",
				"                                                            ; - com.google.zxing.oned.Code39Reader::toNarrowWidePattern@24 (line 223)"
			}
		);
	}

	@Test 
	public void testAssemblyParseBrokenHeader()
	{ 
		testAssemblyParse(new String[] 
			{
				"# {method} {0x0000ffff50924fc0} 'isFinderPattern' '([I)Z' in 'com/google/zxing/oned/rss/AbstractRSSReader'",
				"# parm0:    c_rarg1:c_rarg1 ",
				"                        = '[I'",
				"#           [sp+0x40]  (sp of caller)",
				"0x0000ffff6d6830c0:   nop                                 ;   {no_reloc}",
				"0x0000ffff6d6830c4:   sub	x9, sp, #0x14, lsl #12",
				"0x0000ffff6d6830c8:   str	xzr, [x9]",
				"0x0000ffff6d6830cc:   sub	sp, sp, #0x40",
				"0x0000ffff6d6830d0:   stp	x29, x30, [sp, #48]",
				"0x0000ffff6d6830d4:   add	x29, sp, #0x30",
				"0x0000ffff6d6830d8:   mov	x0, #0x2e98                	// #11928",
				"                                                            ;   {metadata(method data for {method} {0x0000ffff50924fc0} 'isFinderPattern' '([I)Z' in 'com/google/zxing/oned/rss/AbstractRSSReader')}",
				"0x0000ffff6d6830dc:   movk	x0, #0x5094, lsl #16",
				"0x0000ffff6d6830e0:   movk	x0, #0xffff, lsl #32",
				"0x0000ffff6d6830e4:   ldr	w2, [x0, #252]",
				"0x0000ffff6d6830e8:   add	w2, w2, #0x2",
				"0x0000ffff6d6830ec:   str	w2, [x0, #252]",
				"0x0000ffff6d6830f0:   and	w2, w2, #0x7fe",
				"0x0000ffff6d6830f4:   cmp	w2, #0x0",
				"0x0000ffff6d6830f8:   b.eq	0x0000ffff6d683358  // b.none;*aload_0 {reexecute=0 rethrow=0 return_oop=0}",
				"                                                            ; - com.google.zxing.oned.rss.AbstractRSSReader::isFinderPattern@0 (line 123)",
				"0x0000ffff6d6830fc:   ldr	w0, [x1, #12]               ; implicit exception: dispatches to 0x0000ffff6d683378",
				"0x0000ffff6d683100:   cmp	w0, #0x0",
				"0x0000ffff6d683104:   b.ls	0x0000ffff6d68337c  // b.plast",
				"0x0000ffff6d683108:   ldp	w2, w0, [x1, #12]",
				"0x0000ffff6d68310c:   cmp	w2, #0x1",
				"0x0000ffff6d683110:   b.ls	0x0000ffff6d683394  // b.plast",
				"0x0000ffff6d683114:   ldr	w2, [x1, #20]               ;*iaload {reexecute=0 rethrow=0 return_oop=0}",
				"                                                            ; - com.google.zxing.oned.rss.AbstractRSSReader::isFinderPattern@5 (line 123)",
				"0x0000ffff6d683118:   add	w0, w0, w2",
				"0x0000ffff6d68311c:   ldr	w2, [x1, #12]",
				"0x0000ffff6d683120:   cmp	w2, #0x2",
				"0x0000ffff6d683124:   b.ls	0x0000ffff6d6833ac  // b.plast",
				"0x0000ffff6d683128:   ldr	w2, [x1, #24]               ;*iaload {reexecute=0 rethrow=0 return_oop=0}",
				"                                                            ; - com.google.zxing.oned.rss.AbstractRSSReader::isFinderPattern@11 (line 124)",
				"0x0000ffff6d68312c:   ldr	w3, [x1, #12]",
				"0x0000ffff6d683130:   cmp	w3, #0x3",
				"0x0000ffff6d683134:   b.ls	0x0000ffff6d6833c4  // b.plast",
				"0x0000ffff6d683138:   ldr	w3, [x1, #28]               ;*iaload {reexecute=0 rethrow=0 return_oop=0}",
				"                                                            ; - com.google.zxing.oned.rss.AbstractRSSReader::isFinderPattern@15 (line 124)",
				"0x0000ffff6d68313c:   add	w2, w2, w0",
				"0x0000ffff6d683140:   add	w2, w2, w3",
				"0x0000ffff6d683144:   scvtf	s0, w0",
				"0x0000ffff6d683148:   scvtf	s1, w2",
				"0x0000ffff6d68314c:   fdiv	s2, s0, s1",
				"0x0000ffff6d683150:   adr	x8, 0x0000ffff6d683080      ;   {section_word}",
				"0x0000ffff6d683154:   ldr	s0, [x8]",
				"0x0000ffff6d683158:   fcmp	s2, s0",
				"0x0000ffff6d68315c:   cset	x0, ne  // ne = any",
				"0x0000ffff6d683160:   cneg	x0, x0, lt  // lt = tstop   ;*fcmpl {reexecute=0 rethrow=0 return_oop=0}",
				"                                                            ; - com.google.zxing.oned.rss.AbstractRSSReader::isFinderPattern@27 (line 126)",
				"0x0000ffff6d683164:   cmp	w0, #0x0",
				"0x0000ffff6d683168:   mov	x0, #0x2e98                	// #11928",
				"                                                            ;   {metadata(method data for {method} {0x0000ffff50924fc0} 'isFinderPattern' '([I)Z' in 'com/google/zxing/oned/rss/AbstractRSSReader')}"
			}
		);
	}

	@Test 
	public void testAssemblyParseBrokenInsn()
	{ 
		testAssemblyParse(
			new String[]{ 
				"# {method} {0x0000ffff50919c00} 'toPattern' '([I)I' in 'com/google/zxing/oned/Code93Reader'",
				"# parm0:    c_rarg1:c_rarg1 ",
				"                        = '[I'",
				"#           [sp+0x50]  (sp of caller)",
				"0x0000ffff6d685f80:   nop",
				"0x0000ffff6d685f84:   sub	x9, sp, #0x14, lsl #12",
				"0x0000ffff6d685f88:   str	xzr, [x9]",
				"0x0000ffff6d685f8c:   sub	sp, sp, #0x50",
				"0x0000ffff6d685f90:   stp	x29, x30, [sp, #64]",
				"0x0000ffff6d685f94:   add	x29, sp, #0x40",
				"0x0000ffff6d685f98:   str	x1, [sp, #48]",
				"0x0000ffff6d685f9c:   mov	x0, #0x3c80                	// #15488",
				"                                                            ;   {metadata(method data for {method} {0x0000ffff50919c00} 'toPattern' '([I)I' in 'com/google/zxing/oned/Code93Reader')}",
				"0x0000ffff6d685fa0:   movk	x0, #0x5094, lsl #16",
				"0x0000ffff6d685fa4:   movk	x0, #0xffff, lsl #32",
				"0x0000ffff6d685fa8:   ldr	w2, [x0, #252]",
				"0x0000ffff6d685fac:   add	w2, w2, #0x2",
				"0x0000ffff6d685fb0:   str	w2, [x0, #252]",
				"0x0000ffff6d685fb4:   and	w2, w2, #0x7fe",
				"0x0000ffff6d685fb8:   cmp	w2, #0x0",
				"0x0000ffff6d685fbc:   b.eq	0x0000ffff6d6862b8  // b.none;*iconst_0 {reexecute=0 rethrow=0 return_oop=0}",
				"                                                            ; - com.google.zxing.oned.Code93Reader::toPattern@0 (line 169)",
				"0x0000ffff6d685fc0:   ldr	w0, [x1, #12]               ; implicit exception: dispatches to 0x0000ffff6d6862d8",
				"                                                            ;*arraylength {reexecute=0 rethrow=0 return_oop=0}",
				"                                                            ; - com.google.zxing.oned.Code93Reader::toPattern@5 (line 170)",
				"0x0000ffff6d685fc4:   str	w0, [sp, #40]",
				"0x0000ffff6d685fc8:   movz	w2, #0x0, lsl #16",
				"0x0000ffff6d685fcc:   movz	w3, #0x0, lsl #16",
				"0x0000ffff6d685fd0:   b	0x0000ffff6d686030          ;*iload {reexecute=0 rethrow=0 return_oop=0}",
				"                                                            ; - com.google.zxing.oned.Code93Reader::toPattern@10 (line 170)",
				"0x0000ffff6d685fd4:   sxtw	x4, w2",
				"0x0000ffff6d685fd8:   lsl	x4, x4, #2",
				"0x0000ffff6d685fdc:   add	x4, x4, #0x10",
				"0x0000ffff6d685fe0:   ldr	w4, [x1, x4]                ;*iaload {reexecute=0 rethrow=0 return_oop=0}",
				"                                                            ; - com.google.zxing.oned.Code93Reader::toPattern@19 (line 170)",
				"0x0000ffff6d685fe4:   add	w3, w4, w3",
				"0x0000ffff6d685fe8:   add	w2, w2, #0x1",
				"0x0000ffff6d685fec:   mov	x4, #0x3c80                	// #15488",
				"                                                            ;   {metadata(method data for {method} {0x0000ffff50919c00} 'toPattern' '([I)I' in 'com/google/zxing/oned/Code93Reader')}",
				"0x0000ffff6d685ff0:   movk	x4, #0x5094, lsl #16",
				"0x0000ffff6d685ff4:   movk	x4, #0xffff, lsl #32",
				"0x0000ffff6d685ff8:   ldr	w5, [x4, #256]",
				"0x0000ffff6d685ffc:   add	w5, w5, #0x2",
				"0x0000ffff6d686000:   str	w5, [x4, #256]",
				"0x0000ffff6d686004:   and	w5, w5, #0x3ffe"
			}
		);
	}

	@Test
	public void testAssemblyParseBrokenComment()
	{
		testAssemblyParse(
			new String[]
			{ 
				"0x0000ffff6d6862f4:   bl	0x0000ffff74b83000          ; ImmutableOopMap {c_rarg1=Oop [48]=Oop }",
				"                                                            ;*goto {reexecute=1 rethrow=0 return_oop=0}",
				"                                                            ; - (reexecute) com.google.zxing.oned.Code93Reader::toPattern@30 (line 170)",
				"                                                            ;   {runtime_call counter_overflow Runtime1 stub}",
				"0x0000ffff6d6862f8:   b	0x0000ffff6d686010",
				"0x0000ffff6d6862fc:   mov	x8, #0x9c00                	// #39936",
				"                                                            ;   {metadata({method} {0x0000ffff50919c00} 'toPattern' '([I)I' in 'com/google/zxing/oned/Code93Reader')}",
				"0x0000ffff6d686300:   movk	x8, #0x5091, lsl #16",
				"0x0000ffff6d686304:   movk	x8, #0xffff, lsl #32",
				"0x0000ffff6d686308:   str	x8, [sp, #8]",
				"0x0000ffff6d68630c:   mov	x8, #0x67                  	// #103",
				"0x0000ffff6d686310:   str	x8, [sp]",
				"0x0000ffff6d686314:   bl	0x0000ffff74b83000          ; ImmutableOopMap {[48]=Oop }",
				"                                                            ;*goto {reexecute=1 rethrow=0 return_oop=0}",
				"                                                            ; - (reexecute) com.google.zxing.oned.Code93Reader::toPattern@103 (line 181)",
				"                                                            ;   {runtime_call counter_overflow Runtime1 stub}",
				"0x0000ffff6d686318:   b	0x0000ffff6d6861a8",
				"0x0000ffff6d68631c:   mov	x8, #0x9c00                 // #39936",
				"                                                            ;   {metadata({method} {0x0000ffff50919c00} 'toPattern' '([I)I' in 'com/google/zxing/oned/Code93Reader')}",
				"0x0000ffff6d686320:   movk	x8, #0x5091, lsl #16",
				"0x0000ffff6d686324:   movk	x8, #0xffff, lsl #32",
				"0x0000ffff6d686328:   str	x8, [sp, #8]",
				"0x0000ffff6d68632c:   mov	x8, #0x75                  	// #117",
				"0x0000ffff6d686330:   str	x8, [sp]",
				"0x0000ffff6d686334:   bl	0x0000ffff74b83000          ; ImmutableOopMap {[48]=Oop }",
				"                                                            ;*goto {reexecute=1 rethrow=0 return_oop=0}",
				"                                                            ; - (reexecute) com.google.zxing.oned.Code93Reader::toPattern@117 (line 175)",
				"                                                            ;   {runtime_call counter_overflow Runtime1 stub}",
				"0x0000ffff6d686338:   b	0x0000ffff6d686248",
				"0x0000ffff6d68633c:   adr	x8, 0x0000ffff6d686288      ;   {internal_word}",
				"0x0000ffff6d686340:   str	x8, [x28, #856]",
				"0x0000ffff6d686344:   b	0x0000ffff74ad1200          ;   {runtime_call SafepointBlob}",
				"0x0000ffff6d686348:   adr	x8, 0x0000ffff6d6862a8      ;   {internal_word}",
				"0x0000ffff6d68634c:   str	x8, [x28, #856]",
				"0x0000ffff6d686350:   b	0x0000ffff74ad1200          ;   {runtime_call SafepointBlob}"
			}
		);
	}

	@Test 
	public void testARMInstructionParse()
	{
		String line = "0x0000ffff6d686a10:   ldr	x8, [x28, #840]             ; ImmutableOopMap {c_rarg1=Oop }";
		IAssemblyParser parser = AssemblyUtil.getParserForArchitecture(Architecture.ARM_64);
		AssemblyInstruction instruction = parser.createInstruction(new AssemblyLabels(), line);

		assertNotNull(instruction);

		assertEquals("0x0000ffff6d686a10", instruction.getAddress()); // address? 
		assertEquals("ldr", instruction.getMnemonic()); // mnemonia? 

		//operands?
		List<String> operands = instruction.getOperands();
		assertEquals(2, operands.size());
		assertEquals("x8", operands.get(0));
		assertEquals("[x28, #840]", operands.get(1));

		assertEquals("ImmutableOopMap {c_rarg1=Oop }", instruction.getComment()); // comment?
	}

	@Test 
	public void testARMInstructionParseNOP()
	{ 
		String line = "0x0000ffff6d686b04:   nop";

		IAssemblyParser parser = AssemblyUtil.getParserForArchitecture(Architecture.ARM_64);
		AssemblyInstruction instruction = parser.createInstruction(new AssemblyLabels(), line);

		assertNotNull(instruction);
		assertEquals(Long.parseLong("0x0000ffff6d686b04", 16), instruction.getAddressAsLong());
		assertEquals("nop", instruction.getMnemonic());
		assertEquals(0, instruction.getOperands().size());
		assertEquals(S_EMPTY, instruction.getComment());
	}

	// helper method 
	public void testAssemblyParse(String[] asm)
	{ 
		StringBuilder builder = new StringBuilder();

		for (String line : asm)
		{
			builder.append(line).append(S_NEWLINE);
		}

		IAssemblyParser parser = AssemblyUtil.getParserForArchitecture(Architecture.ARM_64);

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
}