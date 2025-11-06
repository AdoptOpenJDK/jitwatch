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

import org.adoptopenjdk.jitwatch.model.assembly.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.tools.ToolProvider;

public class TestAssemblyParserARM extends AbstractAssemblyTest
{
	@Test
	public void testAssemblyParseARM64()
	{ 
		testAssemblyParse(new String[]
			{
					"[Entry Point]",
					"  # {method} {0x0000ffff50967208} 'hasValidRowNumber' '()Z' in 'com/google/zxing/pdf417/decoder/Codeword'",
					"  #           [sp+0x40]  (sp of caller)",
					"  0x0000ffff6d68c3c0:   ldr	w8, [x1, #8]",
					"  0x0000ffff6d68c3c4:   cmp	w9, w8",
					"  0x0000ffff6d68c3c8:   b.eq	0x0000ffff6d68c400  // b.none",
					"  0x0000ffff6d68c3cc:   b	0x0000ffff74acac00          ;   {runtime_call ic_miss_stub}",
					"  0x0000ffff6d68c3d0:   nop",
					"  0x0000ffff6d68c3d4:   nop",
					"  0x0000ffff6d68c3d8:   nop",
					"  0x0000ffff6d68c3dc:   nop",
					"  0x0000ffff6d68c3e0:   nop",
					"  0x0000ffff6d68c3e4:   nop",
					"  0x0000ffff6d68c3e8:   nop",
					"  0x0000ffff6d68c3ec:   nop",
					"  0x0000ffff6d68c3f0:   nop",
					"  0x0000ffff6d68c3f4:   nop",
					"  0x0000ffff6d68c3f8:   nop",
					"  0x0000ffff6d68c3fc:   nop",
					"[Verified Entry Point]",
					"  0x0000ffff6d68c400:   nop",
					"  0x0000ffff6d68c404:   sub	x9, sp, #0x14, lsl #12",
					"  0x0000ffff6d68c408:   str	xzr, [x9]",
					"  0x0000ffff6d68c40c:   sub	sp, sp, #0x40",
					"  0x0000ffff6d68c410:   stp	x29, x30, [sp, #48]",
					"  0x0000ffff6d68c414:   add	x29, sp, #0x30",
					"  0x0000ffff6d68c418:   mov	x2, #0xf688                	// #63112",
					"                                                            ;   {metadata(method data for {method} {0x0000ffff50967208} 'hasValidRowNumber' '()Z' in 'com/google/zxing/pdf417/decoder/Codeword')}",
					"  0x0000ffff6d68c41c:   movk	x2, #0x5097, lsl #16",
					"  0x0000ffff6d68c420:   movk	x2, #0xffff, lsl #32",
					"  0x0000ffff6d68c424:   ldr	w0, [x2, #252]",
					"  0x0000ffff6d68c428:   add	w0, w0, #0x2",
					"  0x0000ffff6d68c42c:   str	w0, [x2, #252]",
					"  0x0000ffff6d68c430:   and	w0, w0, #0x7fe",
					"  0x0000ffff6d68c434:   cmp	w0, #0x0",
					"  0x0000ffff6d68c438:   b.eq	0x0000ffff6d68c47c  // b.none;*aload_0 {reexecute=0 rethrow=0 return_oop=0}",
					"                                                            ; - com.google.zxing.pdf417.decoder.Codeword::hasValidRowNumber@0 (line 40)",
					"  0x0000ffff6d68c43c:   ldr	w2, [x1, #28]               ;*getfield rowNumber {reexecute=0 rethrow=0 return_oop=0}",
					"                                                            ; - com.google.zxing.pdf417.decoder.Codeword::hasValidRowNumber@2 (line 40)",
					"  0x0000ffff6d68c440:   mov	x0, x1",
					"  0x0000ffff6d68c444:   mov	x3, #0xf688                	// #63112",
					"                                                            ;   {metadata(method data for {method} {0x0000ffff50967208} 'hasValidRowNumber' '()Z' in 'com/google/zxing/pdf417/decoder/Codeword')}",
					"  0x0000ffff6d68c448:   movk	x3, #0x5097, lsl #16",
					"  0x0000ffff6d68c44c:   movk	x3, #0xffff, lsl #32",
					"  0x0000ffff6d68c450:   ldr	x8, [x3, #320]",
					"  0x0000ffff6d68c454:   add	x8, x8, #0x1",
					"  0x0000ffff6d68c458:   str	x8, [x3, #320]",
					"  0x0000ffff6d68c45c:   bl	0x0000ffff74acaf00          ; ImmutableOopMap {}",
					"                                                            ;*invokevirtual isValidRowNumber {reexecute=0 rethrow=0 return_oop=0}",
					"                                                            ; - com.google.zxing.pdf417.decoder.Codeword::hasValidRowNumber@5 (line 40)",
					"                                                            ;   {optimized virtual_call}",
					"  0x0000ffff6d68c460:   and	w0, w0, #0x1",
					"  0x0000ffff6d68c464:   ldp	x29, x30, [sp, #48]",
					"  0x0000ffff6d68c468:   add	sp, sp, #0x40",
					"  0x0000ffff6d68c46c:   ldr	x8, [x28, #832]             ;   {poll_return}",
					"  0x0000ffff6d68c470:   cmp	sp, x8",
					"  0x0000ffff6d68c474:   b.hi	0x0000ffff6d68c49c  // b.pmore",
					"  0x0000ffff6d68c478:   ret",
					"  0x0000ffff6d68c47c:   mov	x8, #0x7208                	// #29192",
					"                                                            ;   {metadata({method} {0x0000ffff50967208} 'hasValidRowNumber' '()Z' in 'com/google/zxing/pdf417/decoder/Codeword')}",
					"  0x0000ffff6d68c480:   movk	x8, #0x5096, lsl #16",
					"  0x0000ffff6d68c484:   movk	x8, #0xffff, lsl #32",
					"  0x0000ffff6d68c488:   str	x8, [sp, #8]",
					"  0x0000ffff6d68c48c:   mov	x8, #0xffffffffffffffff    	// #-1",
					"  0x0000ffff6d68c490:   str	x8, [sp]",
					"  0x0000ffff6d68c494:   bl	0x0000ffff74b83000          ; ImmutableOopMap {c_rarg1=Oop }",
					"                                                            ;*synchronization entry",
					"                                                            ; - com.google.zxing.pdf417.decoder.Codeword::hasValidRowNumber@-1 (line 40)",
					"                                                            ;   {runtime_call counter_overflow Runtime1 stub}",
					"  0x0000ffff6d68c498:   b	0x0000ffff6d68c43c",
					"  0x0000ffff6d68c49c:   adr	x8, 0x0000ffff6d68c46c      ;   {internal_word}",
					"  0x0000ffff6d68c4a0:   str	x8, [x28, #856]",
					"  0x0000ffff6d68c4a4:   b	0x0000ffff74ad1200          ;   {runtime_call SafepointBlob}",
					"  0x0000ffff6d68c4a8:   nop",
					"  0x0000ffff6d68c4ac:   nop",
					"  0x0000ffff6d68c4b0:   ldr	x0, [x28, #976]",
					"  0x0000ffff6d68c4b4:   str	xzr, [x28, #976]",
					"  0x0000ffff6d68c4b8:   str	xzr, [x28, #984]",
					"  0x0000ffff6d68c4bc:   ldp	x29, x30, [sp, #48]",
					"  0x0000ffff6d68c4c0:   add	sp, sp, #0x40",
					"  0x0000ffff6d68c4c4:   b	0x0000ffff74b7b380          ;   {runtime_call unwind_exception Runtime1 stub}",
					"  0x0000ffff6d68c4c8:   udf	#0",
					"  0x0000ffff6d68c4cc:   udf	#0",
					"  0x0000ffff6d68c4d0:   udf	#0",
					"  0x0000ffff6d68c4d4:   udf	#0",
					"  0x0000ffff6d68c4d8:   udf	#0",
					"  0x0000ffff6d68c4dc:   udf	#0",
					"  0x0000ffff6d68c4e0:   udf	#0",
					"  0x0000ffff6d68c4e4:   udf	#0",
					"  0x0000ffff6d68c4e8:   udf	#0",
					"  0x0000ffff6d68c4ec:   udf	#0",
					"  0x0000ffff6d68c4f0:   udf	#0",
					"  0x0000ffff6d68c4f4:   udf	#0",
					"  0x0000ffff6d68c4f8:   udf	#0",
					"  0x0000ffff6d68c4fc:   udf	#0",
					"[Stub Code]",
					"  0x0000ffff6d68c500:   isb                                 ;   {no_reloc}",
					"  0x0000ffff6d68c504:   mov	x12, #0x0                   	// #0",
					"                                                            ;   {metadata(NULL)}",
					"  0x0000ffff6d68c508:   movk	x12, #0x0, lsl #16",
					"  0x0000ffff6d68c50c:   movk	x12, #0x0, lsl #32",
					"  0x0000ffff6d68c510:   mov	x8, #0x0                   	// #0",
					"  0x0000ffff6d68c514:   movk	x8, #0x0, lsl #16",
					"  0x0000ffff6d68c518:   movk	x8, #0x0, lsl #32",
					"  0x0000ffff6d68c51c:   br	x8",
					"  0x0000ffff6d68c520:   ldr	x8, 0x0000ffff6d68c528      ;   {trampoline_stub}",
					"  0x0000ffff6d68c524:   br	x8",
					"  0x0000ffff6d68c528:   .inst	0x74acaf00 ; undefined",
					"  0x0000ffff6d68c52c:   udf	#65535",
					"[Exception Handler]",
					"  0x0000ffff6d68c530:   bl	0x0000ffff74b7e080          ;   {runtime_call handle_exception_from_callee Runtime1 stub}",
					"  0x0000ffff6d68c534:   dcps1	#0xdeae",
					"  0x0000ffff6d68c538:   .inst	0x8cc91c30 ; undefined",
					"  0x0000ffff6d68c53c:   udf	#65535",
					"[Deopt Handler Code]",
					"  0x0000ffff6d68c540:   adr	x30, 0x0000ffff6d68c540",
					"  0x0000ffff6d68c544:   b	0x0000ffff74ad1540          ;   {runtime_call DeoptimizationBlob}"
			}
		);
	}

	@Test 
	public void testAssemblyParseARM64BrokenHeader()
	{ 
		testAssemblyParse(new String[] 
			{
					"[Entry Point]",
					"  # {method} {0x0000ffff50967208} 'hasValidRowNumber' '()Z' in 'com/google/zxing/",
					"pdf417/decoder/Codeword'",
					"  #           [sp+0x40]  (sp of caller)",
					"  0x0000ffff6d68c3c0:   ldr	w8, [x1, #8]",
					"  0x0000ffff6d68c3c4:   cmp	w9, w8",
					"  0x0000ffff6d68c3c8:   b.eq	0x0000ffff6d68c400  // b.none",
					"  0x0000ffff6d68c3cc:   b	0x0000ffff74acac00          ;   {runtime_call ic_miss_stub}",
					"  0x0000ffff6d68c3d0:   nop",
					"  0x0000ffff6d68c3d4:   nop",
					"  0x0000ffff6d68c3d8:   nop",
					"  0x0000ffff6d68c3dc:   nop",
					"  0x0000ffff6d68c3e0:   nop",
					"  0x0000ffff6d68c3e4:   nop",
					"  0x0000ffff6d68c3e8:   nop",
					"  0x0000ffff6d68c3ec:   nop",
					"  0x0000ffff6d68c3f0:   nop",
					"  0x0000ffff6d68c3f4:   nop",
					"  0x0000ffff6d68c3f8:   nop",
					"  0x0000ffff6d68c3fc:   nop",
					"[Verified Entry Point]",
					"  0x0000ffff6d68c400:   nop",
					"  0x0000ffff6d68c404:   sub	x9, sp, #0x14, lsl #12",
					"  0x0000ffff6d68c408:   str	xzr, [x9]",
					"  0x0000ffff6d68c40c:   sub	sp, sp, #0x40",
					"  0x0000ffff6d68c410:   stp	x29, x30, [sp, #48]",
					"  0x0000ffff6d68c414:   add	x29, sp, #0x30",
					"  0x0000ffff6d68c418:   mov	x2, #0xf688                	// #63112",
					"                                                            ;   {metadata(method data for {method} {0x0000ffff50967208} 'hasValidRowNumber' '()Z' in 'com/google/zxing/pdf417/decoder/Codeword')}",
					"  0x0000ffff6d68c41c:   movk	x2, #0x5097, lsl #16",
					"  0x0000ffff6d68c420:   movk	x2, #0xffff, lsl #32",
					"  0x0000ffff6d68c424:   ldr	w0, [x2, #252]",
					"  0x0000ffff6d68c428:   add	w0, w0, #0x2",
					"  0x0000ffff6d68c42c:   str	w0, [x2, #252]",
					"  0x0000ffff6d68c430:   and	w0, w0, #0x7fe",
					"  0x0000ffff6d68c434:   cmp	w0, #0x0",
					"  0x0000ffff6d68c438:   b.eq	0x0000ffff6d68c47c  // b.none;*aload_0 {reexecute=0 rethrow=0 return_oop=0}",
					"                                                            ; - com.google.zxing.pdf417.decoder.Codeword::hasValidRowNumber@0 (line 40)",
					"  0x0000ffff6d68c43c:   ldr	w2, [x1, #28]               ;*getfield rowNumber {reexecute=0 rethrow=0 return_oop=0}",
					"                                                            ; - com.google.zxing.pdf417.decoder.Codeword::hasValidRowNumber@2 (line 40)",
					"  0x0000ffff6d68c440:   mov	x0, x1",
					"  0x0000ffff6d68c444:   mov	x3, #0xf688                	// #63112",
					"                                                            ;   {metadata(method data for {method} {0x0000ffff50967208} 'hasValidRowNumber' '()Z' in 'com/google/zxing/pdf417/decoder/Codeword')}",
					"  0x0000ffff6d68c448:   movk	x3, #0x5097, lsl #16",
					"  0x0000ffff6d68c44c:   movk	x3, #0xffff, lsl #32",
					"  0x0000ffff6d68c450:   ldr	x8, [x3, #320]",
					"  0x0000ffff6d68c454:   add	x8, x8, #0x1",
					"  0x0000ffff6d68c458:   str	x8, [x3, #320]",
					"  0x0000ffff6d68c45c:   bl	0x0000ffff74acaf00          ; ImmutableOopMap {}",
					"                                                            ;*invokevirtual isValidRowNumber {reexecute=0 rethrow=0 return_oop=0}",
					"                                                            ; - com.google.zxing.pdf417.decoder.Codeword::hasValidRowNumber@5 (line 40)",
					"                                                            ;   {optimized virtual_call}",
					"  0x0000ffff6d68c460:   and	w0, w0, #0x1",
					"  0x0000ffff6d68c464:   ldp	x29, x30, [sp, #48]",
					"  0x0000ffff6d68c468:   add	sp, sp, #0x40",
					"  0x0000ffff6d68c46c:   ldr	x8, [x28, #832]             ;   {poll_return}",
					"  0x0000ffff6d68c470:   cmp	sp, x8",
					"  0x0000ffff6d68c474:   b.hi	0x0000ffff6d68c49c  // b.pmore",
					"  0x0000ffff6d68c478:   ret",
					"  0x0000ffff6d68c47c:   mov	x8, #0x7208                	// #29192",
					"                                                            ;   {metadata({method} {0x0000ffff50967208} 'hasValidRowNumber' '()Z' in 'com/google/zxing/pdf417/decoder/Codeword')}",
					"  0x0000ffff6d68c480:   movk	x8, #0x5096, lsl #16",
					"  0x0000ffff6d68c484:   movk	x8, #0xffff, lsl #32",
					"  0x0000ffff6d68c488:   str	x8, [sp, #8]",
					"  0x0000ffff6d68c48c:   mov	x8, #0xffffffffffffffff    	// #-1",
					"  0x0000ffff6d68c490:   str	x8, [sp]",
					"  0x0000ffff6d68c494:   bl	0x0000ffff74b83000          ; ImmutableOopMap {c_rarg1=Oop }",
					"                                                            ;*synchronization entry",
					"                                                            ; - com.google.zxing.pdf417.decoder.Codeword::hasValidRowNumber@-1 (line 40)",
					"                                                            ;   {runtime_call counter_overflow Runtime1 stub}",
					"  0x0000ffff6d68c498:   b	0x0000ffff6d68c43c",
					"  0x0000ffff6d68c49c:   adr	x8, 0x0000ffff6d68c46c      ;   {internal_word}",
					"  0x0000ffff6d68c4a0:   str	x8, [x28, #856]",
					"  0x0000ffff6d68c4a4:   b	0x0000ffff74ad1200          ;   {runtime_call SafepointBlob}",
					"  0x0000ffff6d68c4a8:   nop",
					"  0x0000ffff6d68c4ac:   nop",
					"  0x0000ffff6d68c4b0:   ldr	x0, [x28, #976]",
					"  0x0000ffff6d68c4b4:   str	xzr, [x28, #976]",
					"  0x0000ffff6d68c4b8:   str	xzr, [x28, #984]",
					"  0x0000ffff6d68c4bc:   ldp	x29, x30, [sp, #48]",
					"  0x0000ffff6d68c4c0:   add	sp, sp, #0x40",
					"  0x0000ffff6d68c4c4:   b	0x0000ffff74b7b380          ;   {runtime_call unwind_exception Runtime1 stub}",
					"  0x0000ffff6d68c4c8:   udf	#0",
					"  0x0000ffff6d68c4cc:   udf	#0",
					"  0x0000ffff6d68c4d0:   udf	#0",
					"  0x0000ffff6d68c4d4:   udf	#0",
					"  0x0000ffff6d68c4d8:   udf	#0",
					"  0x0000ffff6d68c4dc:   udf	#0",
					"  0x0000ffff6d68c4e0:   udf	#0",
					"  0x0000ffff6d68c4e4:   udf	#0",
					"  0x0000ffff6d68c4e8:   udf	#0",
					"  0x0000ffff6d68c4ec:   udf	#0",
					"  0x0000ffff6d68c4f0:   udf	#0",
					"  0x0000ffff6d68c4f4:   udf	#0",
					"  0x0000ffff6d68c4f8:   udf	#0",
					"  0x0000ffff6d68c4fc:   udf	#0",
					"[Stub Code]",
					"  0x0000ffff6d68c500:   isb                                 ;   {no_reloc}",
					"  0x0000ffff6d68c504:   mov	x12, #0x0                   	// #0",
					"                                                            ;   {metadata(NULL)}",
					"  0x0000ffff6d68c508:   movk	x12, #0x0, lsl #16",
					"  0x0000ffff6d68c50c:   movk	x12, #0x0, lsl #32",
					"  0x0000ffff6d68c510:   mov	x8, #0x0                   	// #0",
					"  0x0000ffff6d68c514:   movk	x8, #0x0, lsl #16",
					"  0x0000ffff6d68c518:   movk	x8, #0x0, lsl #32",
					"  0x0000ffff6d68c51c:   br	x8",
					"  0x0000ffff6d68c520:   ldr	x8, 0x0000ffff6d68c528      ;   {trampoline_stub}",
					"  0x0000ffff6d68c524:   br	x8",
					"  0x0000ffff6d68c528:   .inst	0x74acaf00 ; undefined",
					"  0x0000ffff6d68c52c:   udf	#65535",
					"[Exception Handler]",
					"  0x0000ffff6d68c530:   bl	0x0000ffff74b7e080          ;   {runtime_call handle_exception_from_callee Runtime1 stub}",
					"  0x0000ffff6d68c534:   dcps1	#0xdeae",
					"  0x0000ffff6d68c538:   .inst	0x8cc91c30 ; undefined",
					"  0x0000ffff6d68c53c:   udf	#65535",
					"[Deopt Handler Code]",
					"  0x0000ffff6d68c540:   adr	x30, 0x0000ffff6d68c540",
					"  0x0000ffff6d68c544:   b	0x0000ffff74ad1540          ;   {runtime_call DeoptimizationBlob}"
			}
		);
	}

	@Test 
	public void testAssemblyParseARM64BrokenInsn()
	{ 
		testAssemblyParse(
			new String[]{
					"[Entry Point]",
					"  # {method} {0x0000ffff50967208} 'hasValidRowNumber' '()Z' in 'com/google/zxing/pdf417/decoder/Codeword'",
					"  #           [sp+0x40]  (sp of caller)",
					"  0x0000ffff6d68c3c0:   ldr	w8, [x1, #8]",
					"  0x0000ffff6d68c3c4:   cmp	w9, w8",
					"  0x0000ffff6d68c3c8:   b.eq	0x0000ffff6d68c400  // b.none",
					"  0x0000ffff6d68c3cc:   b	0x0000ffff74acac00          ;   {runtime_call ic_miss_stub}",
					"  0x0000ffff6d68c3d0:   nop",
					"  0x0000ffff6d68c3d4:   nop",
					"  0x0000ffff6d68c3d8:   nop",
					"  0x0000ffff6d68c3dc:   nop",
					"  0x0000ffff6d68c3e0:   nop",
					"  0x0000ffff6d68c3e4:   nop",
					"  0x0000ffff6d68c3e8:   nop",
					"  0x0000ffff6d68c3ec:   nop",
					"  0x0000ffff6d68c3f0:   nop",
					"  0x0000ffff6d68c3f4:   nop",
					"  0x0000ffff6d68c3f8:   nop",
					"  0x0000ffff6d68c3fc:   nop",
					"[Verified Entry Point]",
					"  0x0000ffff6d68c400:   nop",
					"  0x0000ffff6d68c404:   sub	x9, sp, #0x14, lsl #12",
					"  0x0000ffff6d68c408:   str	x0, [xzr, #0x1000000]",
					"  0x0000ffff6d68c40c:   sub	sp, sp, #0x40",
					"  0x0000ffff6d68c410:   stp	x29, x30, [sp, #48]",
					"  0x0000ffff6d68c414:   add	x29, sp, #0x30",
					"  0x0000ffff6d68c418:   mov	x2, #0xf688                	// #63112",
					"                                                            ;   {metadata(method data for {method} {0x0000ffff50967208} 'hasValidRowNumber' '()Z' in 'com/google/zxing/pdf417/decoder/Codeword')}",
					"  0x0000ffff6d68c41c:   movk	x2, #0x5097, lsl #16",
					"  0x0000ffff6d68c420:   movk	x2, #0xffff, lsl #32",
					"  0x0000ffff6d68c424:   ldr	w0, [x2, #252]",
					"  0x0000ffff6d68c428:   add	w0, w0, #0x2",
					"  0x0000ffff6d68c42c:   str	w0, [x2, #252]",
					"  0x0000ffff6d68c430:   and	w0, w0, #0x7fe",
					"  0x0000ffff6d68c434:   cmp	w0, #0x0",
					"  0x0000ffff6d68c438:   b.eq	0x0000ffff6d68c47c  // b.none;*aload_0 {reexecute=0 rethrow=0 return_oop=0}",
					"                                                            ; - com.google.zxing.pdf417.decoder.Codeword::hasValidRowNumber@0 (line 40)",
					"  0x0000ffff6d68c43c:   ldr	w2, [x1, #28]               ;*getfield rowNumber {reexecute=0 rethrow=0 return_oop=0}",
					"                                                            ; - com.google.zxing.pdf417.decoder.Codeword::hasValidRowNumber@2 (line 40)",
					"  0x0000ffff6d68c440:   mov	x0, x1",
					"  0x0000ffff6d68c444:   mov	x3, #0xf688                	// #63112",
					"                                                            ;   {metadata(method data for {method} {0x0000ffff50967208} 'hasValidRowNumber' '()Z' in 'com/google/zxing/pdf417/decoder/Codeword')}",
					"  0x0000ffff6d68c448:   movk	x3, #0x5097, lsl #16",
					"  0x0000ffff6d68c44c:   movk	x3, #0xffff, lsl #32",
					"  0x0000ffff6d68c450:   ldr	x8, [x3, #320]",
					"  0x0000ffff6d68c454:   add	x8, x8, #0x1",
					"  0x0000ffff6d68c458:   str	x8, [x3, #320]",
					"  0x0000ffff6d68c45c:   bl	0x0000ffff74acaf00          ; ImmutableOopMap {}",
					"                                                            ;*invokevirtual isValidRowNumber {reexecute=0 rethrow=0 return_oop=0}",
					"                                                            ; - com.google.zxing.pdf417.decoder.Codeword::hasValidRowNumber@5 (line 40)",
					"                                                            ;   {optimized virtual_call}",
					"  0x0000ffff6d68c460:   and	w0, w0, #0x1",
					"  0x0000ffff6d68c464:   ldp	x29, x30, [sp, #48]",
					"  0x0000ffff6d68c468:   add	sp, sp, #0x40",
					"  0x0000ffff6d68c46c:   ldr	x8, [x28, #832]             ;   {poll_return}",
					"  0x0000ffff6d68c470:   cmp	sp, x8",
					"  0x0000ffff6d68c474:   b.hi	0x0000ffff6d68c49c  // b.pmore",
					"  0x0000ffff6d68c478:   ret",
					"  0x0000ffff6d68c47c:   mov	x8, #0x7208                	// #29192",
					"                                                            ;   {metadata({method} {0x0000ffff50967208} 'hasValidRowNumber' '()Z' in 'com/google/zxing/pdf417/decoder/Codeword')}",
					"  0x0000ffff6d68c480:   movk	x8, #0x5096, lsl #16",
					"  0x0000ffff6d68c484:   movk	x8, #0xffff, lsl #32",
					"  0x0000ffff6d68c488:   str	x8, [sp, #8]",
					"  0x0000ffff6d68c48c:   mov	x8, #0xffffffffffffffff    	// #-1",
					"  0x0000ffff6d68c490:   str	x8, [sp]",
					"  0x0000ffff6d68c494:   bl	0x0000ffff74b83000          ; ImmutableOopMap {c_rarg1=Oop }",
					"                                                            ;*synchronization entry",
					"                                                            ; - com.google.zxing.pdf417.decoder.Codeword::hasValidRowNumber@-1 (line 40)",
					"                                                            ;   {runtime_call counter_overflow Runtime1 stub}",
					"  0x0000ffff6d68c498:   b	0x0000ffff6d68c43c",
					"  0x0000ffff6d68c49c:   adr	x8, 0x0000ffff6d68c46c      ;   {internal_word}",
					"  0x0000ffff6d68c4a0:   str	x8, [x28, #856]",
					"  0x0000ffff6d68c4a4:   b	0x0000ffff74ad1200          ;   {runtime_call SafepointBlob}",
					"  0x0000ffff6d68c4a8:   nop",
					"  0x0000ffff6d68c4ac:   nop",
					"  0x0000ffff6d68c4b0:   ldr	x0, [x28, #976]",
					"  0x0000ffff6d68c4b4:   str	xzr, [x28, #976]",
					"  0x0000ffff6d68c4b8:   str	xzr, [x28, #984]",
					"  0x0000ffff6d68c4bc:   ldp	x29, x30, [sp, #48]",
					"  0x0000ffff6d68c4c0:   add	sp, sp, #0x40",
					"  0x0000ffff6d68c4c4:   b	0x0000ffff74b7b380          ;   {runtime_call unwind_exception Runtime1 stub}",
					"  0x0000ffff6d68c4c8:   udf	#0",
					"  0x0000ffff6d68c4cc:   udf	#0",
					"  0x0000ffff6d68c4d0:   udf	#0",
					"  0x0000ffff6d68c4d4:   udf	#0",
					"  0x0000ffff6d68c4d8:   udf	#0",
					"  0x0000ffff6d68c4dc:   udf	#0",
					"  0x0000ffff6d68c4e0:   udf	#0",
					"  0x0000ffff6d68c4e4:   udf	#0",
					"  0x0000ffff6d68c4e8:   udf	#0",
					"  0x0000ffff6d68c4ec:   udf	#0",
					"  0x0000ffff6d68c4f0:   udf	#0",
					"  0x0000ffff6d68c4f4:   udf	#0",
					"  0x0000ffff6d68c4f8:   udf	#0",
					"  0x0000ffff6d68c4fc:   udf	#0",
					"[Stub Code]",
					"  0x0000ffff6d68c500:   isb                                 ;   {no_reloc}",
					"  0x0000ffff6d68c504:   mov	x12, #0x0                   	// #0",
					"                                                            ;   {metadata(NULL)}",
					"  0x0000ffff6d68c508:   movk	x12, #0x0, lsl #16",
					"  0x0000ffff6d68c50c:   movk	x12, #0x0, lsl #32",
					"  0x0000ffff6d68c510:   mov	x8, #0x0                   	// #0",
					"  0x0000ffff6d68c514:   movk	x8, #0x0, lsl #16",
					"  0x0000ffff6d68c518:   movk	x8, #0x0, lsl #32",
					"  0x0000ffff6d68c51c:   br	x8",
					"  0x0000ffff6d68c520:   ldr	x8, 0x0000ffff6d68c528      ;   {trampoline_stub}",
					"  0x0000ffff6d68c524:   br	x8",
					"  0x0000ffff6d68c528:   .inst	0x74acaf00 ; undefined",
					"  0x0000ffff6d68c52c:   udf	#65535",
					"[Exception Handler]",
					"  0x0000ffff6d68c530:   bl	0x0000ffff74b7e080          ;   {runtime_call handle_exception_from_callee Runtime1 stub}",
					"  0x0000ffff6d68c534:   dcps1	#0xdeae",
					"  0x0000ffff6d68c538:   .inst	0x8cc91c30 ; undefined",
					"  0x0000ffff6d68c53c:   udf	#65535",
					"[Deopt Handler Code]",
					"  0x0000ffff6d68c540:   adr	x30, 0x0000ffff6d68c540",
					"  0x0000ffff6d68c544:   b	0x0000ffff74ad1540          ;   {runtime_call DeoptimizationBlob}"
			}
		);
	}

	@Test
	public void testAssemblyParseARM64BrokenComment()
	{
		testAssemblyParse(
			new String[]
			{
					"[Entry Point]",
					"  # {method} {0x0000ffff50967208} 'hasValidRowNumber' '()Z' in 'com/google/zxing/pdf417/decoder/Codeword'",
					"  #           [sp+0x40]  (sp of caller)",
					"  0x0000ffff6d68c3c0:   ldr	w8, [x1, #8]",
					"  0x0000ffff6d68c3c4:   cmp	w9, w8",
					"  0x0000ffff6d68c3c8:   b.eq	0x0000ffff6d68c400  // b.none",
					"  0x0000ffff6d68c3cc:   b	0x0000ffff74acac00          ;   {runtime_call ic_miss_stub}",
					"  0x0000ffff6d68c3d0:   nop",
					"  0x0000ffff6d68c3d4:   nop",
					"  0x0000ffff6d68c3d8:   nop",
					"  0x0000ffff6d68c3dc:   nop",
					"  0x0000ffff6d68c3e0:   nop",
					"  0x0000ffff6d68c3e4:   nop",
					"  0x0000ffff6d68c3e8:   nop",
					"  0x0000ffff6d68c3ec:   nop",
					"  0x0000ffff6d68c3f0:   nop",
					"  0x0000ffff6d68c3f4:   nop",
					"  0x0000ffff6d68c3f8:   nop",
					"  0x0000ffff6d68c3fc:   nop",
					"[Verified Entry Point]",
					"  0x0000ffff6d68c400:   nop",
					"  0x0000ffff6d68c404:   sub	x9, sp, #0x14, lsl #12",
					"  0x0000ffff6d68c408:   str	xzr, [x9]",
					"  0x0000ffff6d68c40c:   sub	sp, sp, #0x40",
					"  0x0000ffff6d68c410:   stp	x29, x30, [sp, #48]",
					"  0x0000ffff6d68c414:   add	x29, sp, #0x30",
					"  0x0000ffff6d68c418:   mov	x2, #0xf688                	// #63112",
					"                                                            ;",
					"{metadata(method data for {method} {0x0000ffff50967208} 'hasValidRowNumber' '()Z' in 'com/google/zxing/pdf417/decoder/Codeword')}",
					"  0x0000ffff6d68c41c:   movk	x2, #0x5097, lsl #16",
					"  0x0000ffff6d68c420:   movk	x2, #0xffff, lsl #32",
					"  0x0000ffff6d68c424:   ldr	w0, [x2, #252]",
					"  0x0000ffff6d68c428:   add	w0, w0, #0x2",
					"  0x0000ffff6d68c42c:   str	w0, [x2, #252]",
					"  0x0000ffff6d68c430:   and	w0, w0, #0x7fe",
					"  0x0000ffff6d68c434:   cmp	w0, #0x0",
					"  0x0000ffff6d68c438:   b.eq	0x0000ffff6d68c47c  // b.none;*aload_0 {reexecute=0 rethrow=0 return_oop=0}",
					"                                                            ; - com.google.zxing.pdf417.decoder.Codeword::hasValidRowNumber@0 (line 40)",
					"  0x0000ffff6d68c43c:   ldr	w2, [x1, #28]               ;*getfield rowNumber {reexecute=0 rethrow=0 return_oop=0}",
					"                                                            ; - com.google.zxing.pdf417.decoder.Codeword::hasValidRowNumber@2 (line 40)",
					"  0x0000ffff6d68c440:   mov	x0, x1",
					"  0x0000ffff6d68c444:   mov	x3, #0xf688                	// #63112",
					"                                                            ;   {metadata(method data for {method} {0x0000ffff50967208} 'hasValidRowNumber' '()Z' in 'com/google/zxing/pdf417/decoder/Codeword')}",
					"  0x0000ffff6d68c448:   movk	x3, #0x5097, lsl #16",
					"  0x0000ffff6d68c44c:   movk	x3, #0xffff, lsl #32",
					"  0x0000ffff6d68c450:   ldr	x8, [x3, #320]",
					"  0x0000ffff6d68c454:   add	x8, x8, #0x1",
					"  0x0000ffff6d68c458:   str	x8, [x3, #320]",
					"  0x0000ffff6d68c45c:   bl	0x0000ffff74acaf00          ; ImmutableOopMap {}",
					"                                                            ;*invokevirtual isValidRowNumber {reexecute=0 rethrow=0 return_oop=0}",
					"                                                            ; - com.google.zxing.pdf417.decoder.Codeword::hasValidRowNumber@5 (line 40)",
					"                                                            ;   {optimized virtual_call}",
					"  0x0000ffff6d68c460:   and	w0, w0, #0x1",
					"  0x0000ffff6d68c464:   ldp	x29, x30, [sp, #48]",
					"  0x0000ffff6d68c468:   add	sp, sp, #0x40",
					"  0x0000ffff6d68c46c:   ldr	x8, [x28, #832]             ;   {poll_return}",
					"  0x0000ffff6d68c470:   cmp	sp, x8",
					"  0x0000ffff6d68c474:   b.hi	0x0000ffff6d68c49c  // b.pmore",
					"  0x0000ffff6d68c478:   ret",
					"  0x0000ffff6d68c47c:   mov	x8, #0x7208                	// #29192",
					"                                                            ;   {metadata({method} {0x0000ffff50967208} 'hasValidRowNumber' '()Z' in 'com/google/zxing/pdf417/decoder/Codeword')}",
					"  0x0000ffff6d68c480:   movk	x8, #0x5096, lsl #16",
					"  0x0000ffff6d68c484:   movk	x8, #0xffff, lsl #32",
					"  0x0000ffff6d68c488:   str	x8, [sp, #8]",
					"  0x0000ffff6d68c48c:   mov	x8, #0xffffffffffffffff    	// #-1",
					"  0x0000ffff6d68c490:   str	x8, [sp]",
					"  0x0000ffff6d68c494:   bl	0x0000ffff74b83000          ; ImmutableOopMap {c_rarg1=Oop }",
					"                                                            ;*synchronization entry",
					"                                                            ; - com.google.zxing.pdf417.decoder.Codeword::hasValidRowNumber@-1 (line 40)",
					"                                                            ;   {runtime_call counter_overflow Runtime1 stub}",
					"  0x0000ffff6d68c498:   b	0x0000ffff6d68c43c",
					"  0x0000ffff6d68c49c:   adr	x8, 0x0000ffff6d68c46c      ;   {internal_word}",
					"  0x0000ffff6d68c4a0:   str	x8, [x28, #856]",
					"  0x0000ffff6d68c4a4:   b	0x0000ffff74ad1200          ;   {runtime_call SafepointBlob}",
					"  0x0000ffff6d68c4a8:   nop",
					"  0x0000ffff6d68c4ac:   nop",
					"  0x0000ffff6d68c4b0:   ldr	x0, [x28, #976]",
					"  0x0000ffff6d68c4b4:   str	xzr, [x28, #976]",
					"  0x0000ffff6d68c4b8:   str	xzr, [x28, #984]",
					"  0x0000ffff6d68c4bc:   ldp	x29, x30, [sp, #48]",
					"  0x0000ffff6d68c4c0:   add	sp, sp, #0x40",
					"  0x0000ffff6d68c4c4:   b	0x0000ffff74b7b380          ;   {runtime_call unwind_exception Runtime1 stub}",
					"  0x0000ffff6d68c4c8:   udf	#0",
					"  0x0000ffff6d68c4cc:   udf	#0",
					"  0x0000ffff6d68c4d0:   udf	#0",
					"  0x0000ffff6d68c4d4:   udf	#0",
					"  0x0000ffff6d68c4d8:   udf	#0",
					"  0x0000ffff6d68c4dc:   udf	#0",
					"  0x0000ffff6d68c4e0:   udf	#0",
					"  0x0000ffff6d68c4e4:   udf	#0",
					"  0x0000ffff6d68c4e8:   udf	#0",
					"  0x0000ffff6d68c4ec:   udf	#0",
					"  0x0000ffff6d68c4f0:   udf	#0",
					"  0x0000ffff6d68c4f4:   udf	#0",
					"  0x0000ffff6d68c4f8:   udf	#0",
					"  0x0000ffff6d68c4fc:   udf	#0",
					"[Stub Code]",
					"  0x0000ffff6d68c500:   isb                                 ;   {no_reloc}",
					"  0x0000ffff6d68c504:   mov	x12, #0x0                   	// #0",
					"                                                            ;   {metadata(NULL)}",
					"  0x0000ffff6d68c508:   movk	x12, #0x0, lsl #16",
					"  0x0000ffff6d68c50c:   movk	x12, #0x0, lsl #32",
					"  0x0000ffff6d68c510:   mov	x8, #0x0                   	// #0",
					"  0x0000ffff6d68c514:   movk	x8, #0x0, lsl #16",
					"  0x0000ffff6d68c518:   movk	x8, #0x0, lsl #32",
					"  0x0000ffff6d68c51c:   br	x8",
					"  0x0000ffff6d68c520:   ldr	x8, 0x0000ffff6d68c528      ;   {trampoline_stub}",
					"  0x0000ffff6d68c524:   br	x8",
					"  0x0000ffff6d68c528:   .inst	0x74acaf00 ; undefined",
					"  0x0000ffff6d68c52c:   udf	#65535",
					"[Exception Handler]",
					"  0x0000ffff6d68c530:   bl	0x0000ffff74b7e080          ;   {runtime_call handle_exception_from_callee Runtime1 stub}",
					"  0x0000ffff6d68c534:   dcps1	#0xdeae",
					"  0x0000ffff6d68c538:   .inst	0x8cc91c30 ; undefined",
					"  0x0000ffff6d68c53c:   udf	#65535",
					"[Deopt Handler Code]",
					"  0x0000ffff6d68c540:   adr	x30, 0x0000ffff6d68c540",
					"  0x0000ffff6d68c544:   b	0x0000ffff74ad1540          ;   {runtime_call DeoptimizationBlob}"
			}
		);
	}

	// Specific ARM instruction invariants
	@Test 
	public void testARM64InstructionParse()
	{
		String line = "  0x0000ffff6d68c50c:   movk	x12, #0x0, lsl #32; comment here";
		IAssemblyParser parser = AssemblyUtil.getParserForArchitecture(Architecture.ARM_64);
		AssemblyInstruction instruction = parser.createInstruction(new AssemblyLabels(), line);

		assertNotNull(instruction);

		assertEquals(Long.parseLong("0000ffff6d68c50c", 16), instruction.getAddress()); // address?
		assertEquals("movk", instruction.getMnemonic());

		//operands?
		List<String> operands = instruction.getOperands();
		assertEquals(3, operands.size());
		assertEquals("x12", operands.get(0));
		assertEquals("#0x0", operands.get(1).trim());
		assertEquals("lsl #32", operands.get(2).trim());
		assertEquals("; comment here", instruction.getComment()); // comment?
	}

	@Test
	public void testARM64InstructionParseNOP()
	{
		String line = "0x0000ffff6d686b04:   nop";

		IAssemblyParser parser = AssemblyUtil.getParserForArchitecture(Architecture.ARM_64);
		AssemblyInstruction instruction = parser.createInstruction(new AssemblyLabels(), line);

		assertNotNull(instruction);
		assertEquals(Long.parseLong("0000ffff6d686b04", 16), instruction.getAddress());
		assertEquals("nop", instruction.getMnemonic());
		assertEquals(0, instruction.getOperands().size());
		assertEquals(S_EMPTY, instruction.getComment());
	}

	@Test
	public void testARM32InstructionParseNOP()
	{
		String line = "0x00008b04:   nop";
		IAssemblyParser parser = AssemblyUtil.getParserForArchitecture(Architecture.ARM_32);

		AssemblyInstruction instruction = parser.createInstruction(new AssemblyLabels(), line);

		assertNotNull(instruction);
		assertEquals(Long.parseLong("00008b04", 16), instruction.getAddress());
		assertEquals("nop", instruction.getMnemonic());
		assertEquals(0, instruction.getOperands().size());
		assertEquals(S_EMPTY, instruction.getComment());
	}

	@Test
	public void testIdentifyOperandsARM()
	{
		testOperand(OperandType.REGISTER, "mov", "x12");
		testOperand(OperandType.CONSTANT, "movk", "#0x0");

		testOperand(OperandType.REGISTER, "ldr", "x0");
		testOperand(OperandType.ADDRESS, "bl", "0x0000000000401000");

		testOperand(OperandType.REGISTER, "str", "[x28, #840]");
	}

	@Test
	public void testExtractRegisterNameARM64() {
		IAssemblyParser parser64 = AssemblyUtil.getParserForArchitecture(Architecture.ARM_64);

		assertEquals("x28", parser64.extractRegisterName("x28"));
		assertEquals("x28", parser64.extractRegisterName("[x28, #840]"));
		assertEquals("w10", parser64.extractRegisterName("w10, lsl #2"));
		assertEquals("x12", parser64.extractRegisterName("x12"));
		assertEquals("x0", parser64.extractRegisterName("[x0]"));

	}

	@Test
	public void testExtractRegisterNameARM32() {
		IAssemblyParser parser32 = AssemblyUtil.getParserForArchitecture(Architecture.ARM_32);

		assertEquals("r10", parser32.extractRegisterName("r10"));
		assertEquals("r10", parser32.extractRegisterName("[r10, #840]"));
		assertEquals("r5", parser32.extractRegisterName("r5, lsl #2"));
		assertEquals("sp", parser32.extractRegisterName("sp"));
		assertEquals("r0", parser32.extractRegisterName("[r0]"));
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

		List<AssemblyBlock> blocks = asmMethod.getBlocks();

		assertEquals(5, blocks.size());

		AssemblyBlock block0 = blocks.get(0);
		assertEquals("[Entry Point]", block0.getTitle());

		List<AssemblyInstruction> instructions0 = block0.getInstructions();
		assertEquals(16, instructions0.size());

		AssemblyBlock block1 = blocks.get(1);
		assertEquals("[Verified Entry Point]", block1.getTitle());

		List<AssemblyInstruction> instructions1 = block1.getInstructions();
		assertEquals(64, instructions1.size()); // from (0x4FC to 0x400) / 4 + 1 = 64
		assertEquals(2, instructions1.get(2).getOperands().size());

		assertEquals(";*getfield rowNumber {reexecute=0 rethrow=0 return_oop=0}\n; - com.google.zxing.pdf417.decoder.Codeword::hasValidRowNumber@2 (line 40)",
				instructions1.get(15).getComment());

		AssemblyBlock block2 = blocks.get(2);
		assertEquals("[Stub Code]", block2.getTitle());

		List<AssemblyInstruction> instructions2 = block2.getInstructions();
		assertEquals(12, instructions2.size());

		AssemblyBlock block3 = blocks.get(3);
		assertEquals("[Exception Handler]", block3.getTitle());

		List<AssemblyInstruction> instructions3 = block3.getInstructions();
		assertEquals(4, instructions3.size());

		AssemblyBlock block4 = blocks.get(4);
		assertEquals("[Deopt Handler Code]", block4.getTitle());

		List<AssemblyInstruction> instructions4 = block4.getInstructions();
		assertEquals(2, instructions4.size());
	}

	private void testOperand(OperandType type, String mnemonic, String operand)
	{
		IAssemblyParser parser = AssemblyUtil.getParserForArchitecture(Architecture.ARM_64);

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
}