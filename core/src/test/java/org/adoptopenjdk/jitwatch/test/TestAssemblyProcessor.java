/*
 * Copyright (c) 2013-2020 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.LOADED;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_OPEN_ANGLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.JITDataModel;
import org.adoptopenjdk.jitwatch.model.assembly.Architecture;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyBlock;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyInstruction;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyMethod;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyProcessor;
import org.junit.Before;
import org.junit.Test;

public class TestAssemblyProcessor
{
	private JITDataModel model;

	private static final String[] JDK8_SINGLE_ASSEMBLY_METHOD = new String[] {
			"Decoding compiled method 0x00007f7d73364190:",
			"Code:",
			"[Disassembling for mach=&apos;i386:x86-64&apos;]",
			"[Entry Point]",
			"[Verified Entry Point]",
			"[Constants]",
			"  # {method} &apos;dummyMethod&apos; &apos;()V&apos; in &apos;org/adoptopenjdk/jitwatch/test/TestAssemblyProcessor&apos;",
			"  0x00007f7d733642e0: callq  0x00007f7d77e276f0  ;   {runtime_call}",
			"  0x00007f7d733642e5: data32 data32 nopw 0x0(%rax,%rax,1)",
			"  0x00007f7d733642f0: mov    %eax,-0x14000(%rsp)",
			"  0x00007f7d733642f7: push   %rbp",
			"  0x00007f7d733642f8: sub    $0x20,%rsp" };

	@Before
	public void setup()
	{
		model = new JITDataModel();
	}

	public void dummyMethod()
	{
	}

	public void dummyMethod2(String[] args)
	{
	}

	public int add(int a, int b)
	{
		return a + b;
	}

	@Test
	public void testArchitectureDetection()
	{
		String testX86_64 = "[Disassembling for mach='i386:x86-64']";

		assertEquals(Architecture.X86_64, Architecture.parseFromLogLine(testX86_64));
	}

	@Test
	public void testSingleAsmMethod() throws ClassNotFoundException
	{
		String[] lines = JDK8_SINGLE_ASSEMBLY_METHOD;

		String nmethodAddress = "0x00007f7d73364190";

		IMetaMember createdMember = UnitTestUtil.setUpTestMember(model, getClass().getName(), "dummyMethod", void.class,
				new Class<?>[0], nmethodAddress);
		
		AssemblyProcessor asmProcessor = performAssemblyParsingOn(lines);
		
		asmProcessor.attachAssemblyToMembers(model.getPackageManager());

		AssemblyMethod assemblyMethod = createdMember.getLastCompilation().getAssembly();

		assertNotNull(assemblyMethod);

		assertEquals(nmethodAddress, assemblyMethod.getNativeAddress());

		List<AssemblyBlock> asmBlocks = assemblyMethod.getBlocks();

		assertEquals(1, asmBlocks.size());

		AssemblyBlock block = asmBlocks.get(0);

		List<AssemblyInstruction> instructions = block.getInstructions();

		assertEquals(5, instructions.size());
	}

	@Test
	public void testARM7Assembly() throws ClassNotFoundException
	{
		String[] lines = new String[] {
				"Decoding compiled method 0x73f4a848:",
				"Code:",
				"[Disassembling for mach=&apos;arm&apos;]",
				"[Entry Point]",
				"[Constants]",
				"  # {method} &apos;add&apos; &apos;(II)I&apos; in &apos;org/adoptopenjdk/jitwatch/test/TestAssemblyProcessor&apos;",
				"  # this:     rsi:rsi   = &apos;org/adoptopenjdk/jitwatch/test/TestAssemblyProcessor&apos;",
				"  # parm0:    rdx       = int",
				"  # parm1:    rcx       = int",
				"  #           [sp+0x28]  (sp of caller)",
				"  0x73f4a940: ldr	ip, [r0, #4]",
				"  0x73f4a944: cmp	ip, r8",
				"  0x73f4a948: bne	0x73f2fe00      ;   {runtime_call}",
				"  0x73f4a94c: nop			; (mov r0, r0)",
				"[Verified Entry Point]",
				"  0x73f4a950: sub	ip, sp, #64, 24	; 0x4000",
				"  0x73f4a954: strb	r0, [ip]",
				"  0x73f4a958: push	{fp, lr}",
				"  0x73f4a95c: sub	sp, sp, #32     ;*synchronization entry",
				"                                        ; - java.lang.AbstractStringBuilder::ensureCapacityInternal@-1 (line 123)",
				"",
				"  0x73f4a960: str	r0, [sp, #20]",
				"  0x73f4a964: ldr	r5, [r0, #12]   ;*getfield value",
				"                                        ; - java.lang.AbstractStringBuilder::ensureCapacityInternal@2 (line 123)",
				"",
				"  0x73f4a968: ldr	r7, [r5, #8]    ;*arraylength",
				"                                        ; - java.lang.AbstractStringBuilder::ensureCapacityInternal@5 (line 123)",
				"                                        ; implicit exception: dispatches to 0x73f4ab9c",
				"  0x73f4a96c: sub	r4, r1, r7",
				"  0x73f4a970: cmp	r4, #0",
				"  0x73f4a974: ble	0x73f4aa6c      ;*ifle",
				"                                        ; - java.lang.AbstractStringBuilder::ensureCapacityInternal@7 (line 123)",
				"",
				"  0x73f4a978: lsl	r4, r7, #1      ;*ishl",
				"                                        ; - java.lang.AbstractStringBuilder::newCapacity@6 (line 150)",
				"                                        ; - java.lang.AbstractStringBuilder::ensureCapacityInternal@17 (line 125)",
				"",
				"  0x73f4a97c: sub	r8, r4, r1",
				"  0x73f4a980: add	r9, r8, #2",
				"  0x73f4a984: mvn	r6, #136, 4	; 0x80000008",
				"  0x73f4a988: cmp	r9, #0",
				"  0x73f4a98c: blt	0x73f4aa84      ;*ifge",
				"                                        ; - java.lang.AbstractStringBuilder::newCapacity@13 (line 151)",
				"                                        ; - java.lang.AbstractStringBuilder::ensureCapacityInternal@17 (line 125)",
				"",
				"  0x73f4a990: add	r9, r4, #2      ;*iload_2",
				"                                        ; - java.lang.AbstractStringBuilder::newCapacity@18 (line 154)",
				"                                        ; - java.lang.AbstractStringBuilder::ensureCapacityInternal@17 (line 125)",
				"",
				"  0x73f4a994: sub	fp, r6, r9      ;*isub",
				"                                        ; - java.lang.AbstractStringBuilder::newCapacity@25 (line 154)",
				"                                        ; - java.lang.AbstractStringBuilder::ensureCapacityInternal@17 (line 125)",
				"",
				"  0x73f4a998: lsl	r6, r9, #1",
				"  0x73f4a99c: add	r4, r6, #19",
				"  0x73f4a9a0: cmp	r9, #0",
				"  0x73f4a9a4: ble	0x73f4ab60      ;*ifle",
				"                                        ; - java.lang.AbstractStringBuilder::newCapacity@19 (line 154)",
				"                                        ; - java.lang.AbstractStringBuilder::ensureCapacityInternal@17 (line 125)",
				"",
				"  0x73f4a9a8: cmp	fp, #0",
				"  0x73f4a9ac: blt	0x73f4ab7c      ;*ifge",
				"                                        ; - java.lang.AbstractStringBuilder::newCapacity@26 (line 154)",
				"                                        ; - java.lang.AbstractStringBuilder::ensureCapacityInternal@17 (line 125)",
				"",
				"  0x73f4a9b0: bic	ip, r4, #7",
				"  0x73f4a9b4: cmp	r9, #128, 20	; 0x80000",
				"  0x73f4a9b8: bhi	0x73f4aacc",
				"  0x73f4a9bc: ldr	fp, [sl, #52]	; 0x34",
				"  0x73f4a9c0: ldr	r6, [sl, #60]	; 0x3c",
				"  0x73f4a9c4: add	lr, fp, ip",
				"  0x73f4a9c8: mov	r4, #1",
				"  0x73f4a9cc: movw	r8, #24880	; 0x6130",
				"                                        ;   {metadata({type array char})}",
				"  0x73f4a9d0: movt	r8, #26647	; 0x6817",
				"  0x73f4a9d4: cmp	lr, r6",
				"  0x73f4a9d8: bcs	0x73f4aacc",
				"  0x73f4a9dc: str	lr, [sl, #52]	; 0x34",
				"  0x73f4a9e0: str	r4, [fp]",
				"  0x73f4a9e4: pldw	[lr, #128]	; 0x80",
				"  0x73f4a9e8: str	r8, [fp, #4]",
				"  0x73f4a9ec: pldw	[lr, #144]	; 0x90",
				"  0x73f4a9f0: str	r9, [fp, #8]",
				"  0x73f4a9f4: pldw	[lr, #160]	; 0xa0",
				"  0x73f4a9f8: mov	r6, r9",
				"  0x73f4a9fc: cmp	r7, r9          ;*newarray",
				"                                        ; - java.util.Arrays::copyOf@1 (line 3332)",
				"                                        ; - java.lang.AbstractStringBuilder::ensureCapacityInternal@20 (line 124)",
				"",
				"  0x73f4aa00: movlt	r6, r7          ;*invokestatic min",
				"                                        ; - java.util.Arrays::copyOf@11 (line 3334)",
				"                                        ; - java.lang.AbstractStringBuilder::ensureCapacityInternal@20 (line 124)",
				"",
				"  0x73f4aa04: add	lr, fp, #12",
				"  0x73f4aa08: sub	r2, ip, #16",
				"  0x73f4aa0c: add	r1, fp, #16",
				"  0x73f4aa10: cmp	r7, r6",
				"  0x73f4aa14: bcc	0x73f4ab08",
				"  0x73f4aa18: cmp	r9, r6",
				"  0x73f4aa1c: bcc	0x73f4ab08",
				"  0x73f4aa20: cmp	r6, #0",
				"  0x73f4aa24: ble	0x73f4ab40",
				"  0x73f4aa28: add	r0, r5, #12",
				"  0x73f4aa2c: cmp	r6, r9",
				"  0x73f4aa30: blt	0x73f4aa8c",
				"  0x73f4aa34: ldr	r4, [r0]",
				"  0x73f4aa38: str	r4, [lr]",
				"  0x73f4aa3c: lsr	r2, r2, #3",
				"  0x73f4aa40: add	r0, r5, #16",
				"  0x73f4aa44: bl	Stub::jlong_disjoint_arraycopy",
				"                                        ;*newarray",
				"                                        ; - java.util.Arrays::copyOf@1 (line 3332)",
				"                                        ; - java.lang.AbstractStringBuilder::ensureCapacityInternal@20 (line 124)",
				"                                        ;   {runtime_call}",
				"  0x73f4aa48: dmb	st              ;*invokestatic arraycopy",
				"                                        ; - java.util.Arrays::copyOf@14 (line 3333)",
				"                                        ; - java.lang.AbstractStringBuilder::ensureCapacityInternal@20 (line 124)",
				"",
				"  0x73f4aa4c: movw	r5, #0",
				"  0x73f4aa50: movt	r5, #29614	; 0x73ae",
				"  0x73f4aa54: ldr	r4, [sp, #20]",
				"  0x73f4aa58: lsr	r4, r4, #9",
				"  0x73f4aa5c: movw	r7, #0",
				"  0x73f4aa60: strb	r7, [r5, r4]",
				"  0x73f4aa64: ldr	r5, [sp, #20]",
				"  0x73f4aa68: str	fp, [r5, #12]   ;*synchronization entry",
				"                                        ; - java.lang.AbstractStringBuilder::ensureCapacityInternal@-1 (line 123)",
				"",
				"  0x73f4aa6c: add	sp, sp, #32",
				"  0x73f4aa70: pop	{fp, lr}",
				"  0x73f4aa74: movw	ip, #53248	; 0xd000",
				"  0x73f4aa78: movt	ip, #30462	; 0x76fe",
				"  0x73f4aa7c: ldr	ip, [ip]        ;   {poll_return}",
				"  0x73f4aa80: bx	lr",
				"  0x73f4aa84: mov	r9, r1",
				"  0x73f4aa88: b	0x73f4a994",
				"  0x73f4aa8c: lsl	r5, r6, #1",
				"  0x73f4aa90: add	r4, r5, #16",
				"  0x73f4aa94: bic	r4, r4, #6",
				"  0x73f4aa98: add	r7, fp, r4",
				"  0x73f4aa9c: movw	r8, #0",
				"  0x73f4aaa0: str	r8, [r7, #-4]",
				"  0x73f4aaa4: sub	r4, ip, r4",
				"  0x73f4aaa8: mov	r8, #0",
				"  0x73f4aaac: mov	r5, r4",
				"  0x73f4aab0: subs	r5, r5, #4",
				"  0x73f4aab4: strge	r8, [r7, r5]",
				"  0x73f4aab8: bgt	0x73f4aab0",
				"  0x73f4aabc: mov	r2, r6",
				"  0x73f4aac0: mov	r1, lr",
				"  0x73f4aac4: bl	Stub::arrayof_jshort_disjoint_arraycopy",
				"                                        ;*invokestatic arraycopy",
				"                                        ; - java.util.Arrays::copyOf@14 (line 3333)",
				"                                        ; - java.lang.AbstractStringBuilder::ensureCapacityInternal@20 (line 124)",
				"                                        ;   {runtime_call}",
				"  0x73f4aac8: b	0x73f4aa48",
				"  0x73f4aacc: mov	fp, r0",
				"  0x73f4aad0: str	ip, [sp, #16]",
				"  0x73f4aad4: str	r7, [sp, #12]",
				"  0x73f4aad8: str	r9, [sp, #8]",
				"  0x73f4aadc: str	r5, [sp, #4]",
				"  0x73f4aae0: movw	r0, #24880	; 0x6130",
				"                                        ;   {metadata({type array char})}",
				"  0x73f4aae4: movt	r0, #26647	; 0x6817",
				"  0x73f4aae8: mov	r1, r9",
				"  0x73f4aaec: bl	0x73f4c400      ; OopMap{fp=Oop [4]=Oop [20]=Oop off=432}",
				"                                        ;*newarray",
				"                                        ; - java.util.Arrays::copyOf@1 (line 3332)",
				"                                        ; - java.lang.AbstractStringBuilder::ensureCapacityInternal@20 (line 124)",
				"                                        ;   {runtime_call}",
				"  0x73f4aaf0: ldr	r5, [sp, #4]",
				"  0x73f4aaf4: ldr	r9, [sp, #8]",
				"  0x73f4aaf8: mov	fp, r0",
				"  0x73f4aafc: ldr	r7, [sp, #12]",
				"  0x73f4ab00: ldr	ip, [sp, #16]",
				"  0x73f4ab04: b	0x73f4a9f8",
				"  0x73f4ab08: movw	r7, #0",
				"  0x73f4ab0c: str	r7, [lr]",
				"  0x73f4ab10: mov	r8, #0",
				"  0x73f4ab14: mov	r4, r2",
				"  0x73f4ab18: subs	r4, r4, #4",
				"  0x73f4ab1c: strge	r8, [r1, r4]",
				"  0x73f4ab20: bgt	0x73f4ab18",
				"  0x73f4ab24: mov	r0, r5",
				"  0x73f4ab28: movw	r1, #0",
				"  0x73f4ab2c: mov	r2, fp",
				"  0x73f4ab30: movw	r3, #0",
				"  0x73f4ab34: str	r6, [sp]",
				"  0x73f4ab38: bl	0x73f4ce00      ; OopMap{fp=Oop [20]=Oop off=508}",
				"                                        ;*invokestatic arraycopy",
				"                                        ; - java.util.Arrays::copyOf@14 (line 3333)",
				"                                        ; - java.lang.AbstractStringBuilder::ensureCapacityInternal@20 (line 124)",
				"                                        ;   {runtime_call}",
				"  0x73f4ab3c: b	0x73f4aa48",
				"  0x73f4ab40: movw	r4, #0",
				"  0x73f4ab44: str	r4, [lr]",
				"  0x73f4ab48: mov	r4, #0",
				"  0x73f4ab4c: mov	r5, r2",
				"  0x73f4ab50: subs	r5, r5, #4",
				"  0x73f4ab54: strge	r4, [r1, r5]",
				"  0x73f4ab58: bgt	0x73f4ab50      ;*invokestatic arraycopy",
				"                                        ; - java.util.Arrays::copyOf@14 (line 3333)",
				"                                        ; - java.lang.AbstractStringBuilder::ensureCapacityInternal@20 (line 124)",
				"",
				"  0x73f4ab5c: b	0x73f4aa48",
				"  0x73f4ab60: ldr	fp, [sp, #20]",
				"  0x73f4ab64: mvn	r0, #154	; 0x9a",
				"  0x73f4ab68: str	r5, [sp, #4]",
				"  0x73f4ab6c: str	r1, [sp, #12]",
				"  0x73f4ab70: str	r9, [sp, #20]",
				"  0x73f4ab74: bl	0x73f2f840      ; OopMap{fp=Oop [4]=Oop off=568}",
				"                                        ;*ifle",
				"                                        ; - java.lang.AbstractStringBuilder::newCapacity@19 (line 154)",
				"                                        ; - java.lang.AbstractStringBuilder::ensureCapacityInternal@17 (line 125)",
				"                                        ;   {runtime_call}",
				"  0x73f4ab78: udf	#16             ;*ifle",
				"                                        ; - java.lang.AbstractStringBuilder::newCapacity@19 (line 154)",
				"                                        ; - java.lang.AbstractStringBuilder::ensureCapacityInternal@17 (line 125)",
				"",
				"  0x73f4ab7c: vldr	s14, [sp, #20]",
				"  0x73f4ab80: vstr	s14, [sp, #4]",
				"  0x73f4ab84: mvn	r0, #154	; 0x9a",
				"  0x73f4ab88: str	r5, [sp, #8]",
				"  0x73f4ab8c: str	r1, [sp, #16]",
				"  0x73f4ab90: str	r9, [sp, #20]",
				"  0x73f4ab94: bl	0x73f2f840      ; OopMap{[4]=Oop [8]=Oop off=600}",
				"                                        ;*ifge",
				"                                        ; - java.lang.AbstractStringBuilder::newCapacity@26 (line 154)",
				"                                        ; - java.lang.AbstractStringBuilder::ensureCapacityInternal@17 (line 125)",
				"                                        ;   {runtime_call}",
				"  0x73f4ab98: udf	#16             ;*ifge",
				"                                        ; - java.lang.AbstractStringBuilder::newCapacity@26 (line 154)",
				"                                        ; - java.lang.AbstractStringBuilder::ensureCapacityInternal@17 (line 125)",
				"",
				"  0x73f4ab9c: mvn	r0, #9",
				"  0x73f4aba0: bl	0x73f2f840      ; OopMap{off=612}",
				"                                        ;*arraylength",
				"                                        ; - java.lang.AbstractStringBuilder::ensureCapacityInternal@5 (line 123)",
				"                                        ;   {runtime_call}",
				"  0x73f4aba4: udf	#16             ;*invokestatic arraycopy",
				"                                        ; - java.util.Arrays::copyOf@14 (line 3333)",
				"                                        ; - java.lang.AbstractStringBuilder::ensureCapacityInternal@20 (line 124)",
				"",
				"  0x73f4aba8: mov	r0, r4",
				"  0x73f4abac: b	0x73f4abb4              ;*newarray",
				"                                        ; - java.util.Arrays::copyOf@1 (line 3332)",
				"                                        ; - java.lang.AbstractStringBuilder::ensureCapacityInternal@20 (line 124)",
				"",
				"  0x73f4abb0: mov	r0, r4          ;*synchronization entry",
				"                                        ; - java.lang.AbstractStringBuilder::ensureCapacityInternal@-1 (line 123)",
				"",
				"  0x73f4abb4: add	sp, sp, #32",
				"  0x73f4abb8: pop	{fp, lr}",
				"  0x73f4abbc: b	0x73f4cd00              ;   {runtime_call}",
				"[Exception Handler]",
				"[Stub Code]",
				"  0x73f4abc0: b	0x73f49740              ;   {no_reloc}",
				"[Deopt Handler Code]",
				"  0x73f4abc4: sub	sp, sp, #4",
				"  0x73f4abc8: push	{lr}		; (str lr, [sp, #-4]!)",
				"  0x73f4abcc: sub	lr, pc, #16",
				"  0x73f4abd0: str	lr, [sp, #4]",
				"  0x73f4abd4: pop	{lr}		; (ldr lr, [sp], #4)",
				"  0x73f4abd8: b	0x73f2fb50              ;   {runtime_call}" };

		String nmethodAddress = "0x73f4a848";

		IMetaMember member = UnitTestUtil.setUpTestMember(model, getClass().getName(), "add", int.class,
				new Class<?>[] { int.class, int.class }, nmethodAddress);

		AssemblyProcessor asmProcessor = performAssemblyParsingOn(lines);
		
		asmProcessor.attachAssemblyToMembers(model.getPackageManager());
		
		AssemblyMethod assemblyMethod = member.getLastCompilation().getAssembly();

		assertNotNull(assemblyMethod);

		assertEquals(nmethodAddress, assemblyMethod.getNativeAddress());

		List<AssemblyBlock> asmBlocks = assemblyMethod.getBlocks();

		assertEquals(5, asmBlocks.size());

		AssemblyBlock block = asmBlocks.get(0);

		List<AssemblyInstruction> instructions = block.getInstructions();

		assertEquals(4, instructions.size());
	}

	private AssemblyProcessor performAssemblyParsingOn(String[] lines)
	{
		AssemblyProcessor asmProcessor = new AssemblyProcessor();

		for (String line : lines)
		{
			String trimmedLine = line.trim();

			if (!trimmedLine.startsWith(S_OPEN_ANGLE) && !trimmedLine.startsWith(LOADED))
			{
				asmProcessor.handleLine(trimmedLine);
			}
		}

		asmProcessor.complete();
		
		return asmProcessor;
	}

	@Test
	public void testSingleAsmMethodInterrupted() throws ClassNotFoundException
	{
		String[] lines = new String[] {
				"Decoding compiled method 0x00007f7d73363f90:",
				"Code:",
				"[Disassembling for mach=&apos;i386:x86-64&apos;]",
				"[Entry Point]",
				"[Constants]",
				"  # {method} &apos;add&apos; &apos;(II)I&apos; in &apos;org",
				"<writer thread='140176877946624'/>",
				"<uncommon_trap thread='140176877946624' reason='unloaded' action='reinterpret' index='39' compile_id='2' compile_kind='osr' compiler='C2' unresolved='1' name='java/lang/System' stamp='0.374'>",
				"<jvms bci='31' method='org/adoptopenjdk/jitwatch/test/TestAssemblyProcessor dummyMethod2 ([Ljava/lang/String;)V' bytes='57' count='10000' backedge_count='6024' iicount='1'/>",
				"</uncommon_trap>",
				"<writer thread='140176736904960'/>",
				"/adoptopenjdk/jitwatch/test/TestAssemblyProcessor&apos;",
				"  # this:     rsi:rsi   = &apos;org/adoptopenjdk/jitwatch/test/TestAssemblyProcessor&apos;",
				"  # parm0:    rdx       = int",
				"  # parm1:    rcx       = int",
				"  #           [sp+0x20]  (sp of caller)",
				"  0x00007f7d733640c0: mov    0x8(%rsi),%r10d",
				"  0x00007f7d733640c4: cmp    %r10,%rax",
				"  0x00007f7d733640c7: jne    0x00007f7d7333b960  ;   {runtime_call}",
				"  0x00007f7d733640cd: data32 xchg %ax,%ax" };

		String nmethodAddress = "0x00007f7d73363f90";

		IMetaMember member = UnitTestUtil.setUpTestMember(model, getClass().getName(), "add", int.class,
				new Class<?>[] { int.class, int.class }, nmethodAddress);

		AssemblyProcessor asmProcessor = performAssemblyParsingOn(lines);
				
		asmProcessor.attachAssemblyToMembers(model.getPackageManager());

		assertNotNull(member);

		AssemblyMethod assemblyMethod = member.getLastCompilation().getAssembly();

		assertNotNull(assemblyMethod);

		assertEquals(nmethodAddress, assemblyMethod.getNativeAddress());

		List<AssemblyBlock> asmBlocks = assemblyMethod.getBlocks();

		assertEquals(1, asmBlocks.size());

		AssemblyBlock block = asmBlocks.get(0);

		List<AssemblyInstruction> instructions = block.getInstructions();

		assertEquals(4, instructions.size());
	}

	@Test
	public void testTwoMethodsWithOneInterrupted() throws ClassNotFoundException
	{
		String[] lines = new String[] {
				"Decoding compiled method 0x00007f7d73364190:",
				"Code:",
				"[Disassembling for mach=&apos;i386:x86-64&apos;]",
				"[Entry Point]",
				"[Verified Entry Point]",
				"[Constants]",
				"  # {method} &apos;dummyMethod2&apos; &apos;([Ljava.lang.String;)V&apos; in &apos;org/adoptopenjdk/jitwatch/test/TestAssemblyProcessor&apos;",
				"  0x00007f7d733642e0: callq  0x00007f7d77e276f0  ;   {runtime_call}",
				"  0x00007f7d733642e5: data32 data32 nopw 0x0(%rax,%rax,1)",
				"  0x00007f7d733642f0: mov    %eax,-0x14000(%rsp)",
				"  0x00007f7d733642f7: push   %rbp",
				"  0x00007f7d733642f8: sub    $0x20,%rsp",
				"<nmethod compile_id='2' compile_kind='osr' compiler='C2' entry='0x00007f7d733642e0' size='800' address='0x00007f7d73364190' relocation_offset='288' insts_offset='336' stub_offset='528' scopes_data_offset='576' scopes_pcs_offset='632' dependencies_offset='792' oops_offset='552' method='org/adoptopenjdk/jitwatch/test/TestAssemblyProcessor dummyMethod2 ([Ljava/lang/String;)V' bytes='57' count='10000' backedge_count='5377' iicount='1' stamp='0.373'/>",
				"<writer thread='140176736904960'/>",
				"Decoding compiled method 0x00007f7d73363f90:",
				"Code:",
				"[Entry Point]",
				"[Constants]",
				"  # {method} &apos;add&apos; &apos;(II)I&apos; in &apos;org",
				"<writer thread='140176877946624'/>",
				"<uncommon_trap thread='140176877946624' reason='unloaded' action='reinterpret' index='39' compile_id='2' compile_kind='osr' compiler='C2' unresolved='1' name='java/lang/System' stamp='0.374'>",
				"<jvms bci='31' method='org/adoptopenjdk/jitwatch/test/TestAssemblyProcessor dummyMethod2 ([Ljava/lang/String;)V' bytes='57' count='10000' backedge_count='6024' iicount='1'/>",
				"</uncommon_trap>",
				"<writer thread='140176736904960'/>",
				"/adoptopenjdk/jitwatch/test/TestAssemblyProcessor&apos;",
				"  # this:     rsi:rsi   = &apos;org/adoptopenjdk/jitwatch/test/TestAssemblyProcessor&apos;",
				"  # parm0:    rdx       = int",
				"  # parm1:    rcx       = int",
				"  #           [sp+0x20]  (sp of caller)",
				"  0x00007f7d733640c0: mov    0x8(%rsi),%r10d",
				"  0x00007f7d733640c4: cmp    %r10,%rax",
				"  0x00007f7d733640c7: jne    0x00007f7d7333b960  ;   {runtime_call}",
				"  0x00007f7d733640cd: data32 xchg %ax,%ax" };

		String nmethodAddress1 = "0x00007f7d73364190";
		String nmethodAddress2 = "0x00007f7d73363f90";

		IMetaMember member = UnitTestUtil.setUpTestMember(model, getClass().getName(), "dummyMethod2", void.class,
				new Class<?>[] { Class.forName("[Ljava.lang.String;") }, nmethodAddress1);

		IMetaMember member2 = UnitTestUtil.setUpTestMember(model, getClass().getName(), "add", int.class,
				new Class<?>[] { int.class, int.class }, nmethodAddress2);

		AssemblyProcessor asmProcessor = performAssemblyParsingOn(lines);
		
		asmProcessor.attachAssemblyToMembers(model.getPackageManager());
		
		AssemblyMethod assemblyMethod = member.getLastCompilation().getAssembly();

		assertNotNull(assemblyMethod);

		assertEquals(nmethodAddress1, assemblyMethod.getNativeAddress());

		List<AssemblyBlock> asmBlocks = assemblyMethod.getBlocks();

		assertEquals(1, asmBlocks.size());

		AssemblyBlock block = asmBlocks.get(0);

		List<AssemblyInstruction> instructions = block.getInstructions();

		assertEquals(5, instructions.size());

		AssemblyMethod assemblyMethod2 = member2.getLastCompilation().getAssembly();

		assertNotNull(assemblyMethod2);

		assertEquals(nmethodAddress2, assemblyMethod2.getNativeAddress());

		List<AssemblyBlock> asmBlocks2 = assemblyMethod2.getBlocks();

		assertEquals(1, asmBlocks2.size());

		AssemblyBlock block2 = asmBlocks2.get(0);

		List<AssemblyInstruction> instructions2 = block2.getInstructions();

		assertEquals(4, instructions2.size());
	}

	@Test
	public void testRegressionWorksWithJMHIndentedLogs() throws ClassNotFoundException
	{
		String[] lines = new String[] {
				"         Loaded disassembler from /home/shade/Install/jdk8u0/jre/lib/amd64/server/libhsdis-amd64.so",
				"                  Decoding compiled method 0x00007f25cd0fedd0:",
				"                  Code:",
				"                  [Disassembling for mach=&apos;i386:x86-64&apos;]",
				"                  [Entry Point]",
				"                  [Constants]",
				"                    # {method} {0x00007f25cc804f80} &apos;hashCode&apos; &apos;()I&apos; in &apos;java/lang/String&apos;",
				"                    #           [sp+0x40]  (sp of caller)",
				"                    0x00007f25cd0fef40: mov    0x8(%rsi),%r10d",
				"                    0x00007f25cd0fef44: shl    $0x3,%r10",
				"                    0x00007f25cd0fef48: cmp    %rax,%r10",
				"                    0x00007f25cd0fef4b: jne    0x00007f25cd045b60  ;   {runtime_call}",
				"                    0x00007f25cd0fef51: nopw   0x0(%rax,%rax,1)",
				"                    0x00007f25cd0fef5c: xchg   %ax,%ax",
				"                  [Verified Entry Point]",
				"                    0x00007f25cd0fef60: mov    %eax,-0x14000(%rsp)",
				"                    0x00007f25cd0fef67: push   %rbp",
				"                    0x00007f25cd0fef68: sub    $0x30,%rsp",
				"                    0x00007f25cd0fef6c: mov    $0x7f25cc9d0b38,%rax  ;   {metadata(method data for {method} {0x00007f25cc804f80} &apos;hashCode&apos; &apos;()I&apos; in &apos;java/lang/String&apos;)}",
				"                    0x00007f25cd0fef76: mov    0x64(%rax),%edi",
				"                    0x00007f25cd0fef79: add    $0x8,%edi",
				"                    0x00007f25cd0fef7c: mov    %edi,0x64(%rax)",
				"                    0x00007f25cd0fef7f: mov    $0x7f25cc804f80,%rax  ;   {metadata({method} {0x00007f25cc804f80} &apos;hashCode&apos; &apos;()I&apos; in &apos;java/lang/String&apos;)}",
				"                    0x00007f25cd0fef89: and    $0x1ff8,%edi",
				"                    0x00007f25cd0fef8f: cmp    $0x0,%edi",
				"                    0x00007f25cd0fef92: je     0x00007f25cd0ff0cb  ;*aload_0",
				"                                                                  ; - java.lang.String::hashCode@0 (line 1453)",
				"                    0x00007f25cd0fef98: mov    0x10(%rsi),%eax    ;*getfield hash",
				"                                                                  ; - java.lang.String::hashCode@1 (line 1453)",
				"                    0x00007f25cd0fef9b: cmp    $0x0,%eax",
				"                    0x00007f25cd0fef9e: mov    $0x7f25cc9d0b38,%rdi  ;   {metadata(method data for {method} {0x00007f25cc804f80} &apos;hashCode&apos; &apos;()I&apos; in &apos;java/lang/String&apos;)}",
				"                    0x00007f25cd0fefa8: mov    $0x90,%rbx",
				"                    0x00007f25cd0fefb2: jne    0x00007f25cd0fefc2",
				"                    0x00007f25cd0fefb8: mov    $0xa0,%rbx",
				"                    0x00007f25cd0fefc2: mov    (%rdi,%rbx,1),%rdx",
				"                    0x00007f25cd0fefc6: lea    0x1(%rdx),%rdx",
				"                    0x00007f25cd0fefca: mov    %rdx,(%rdi,%rbx,1)",
				"                    0x00007f25cd0fefce: jne    0x00007f25cd0ff0bf  ;*ifne",
				"                                                                  ; - java.lang.String::hashCode@6 (line 1454)" };

		String nmethodAddress1 = "0x00007f25cd0fedd0";

		IMetaMember member = UnitTestUtil.setUpTestMember(model, "java.lang.String", "hashCode", int.class, new Class<?>[0],
				nmethodAddress1);

		AssemblyProcessor asmProcessor = performAssemblyParsingOn(lines);
		
		asmProcessor.attachAssemblyToMembers(model.getPackageManager());
		
		AssemblyMethod assemblyMethod = member.getLastCompilation().getAssembly();

		assertNotNull(assemblyMethod);

		assertEquals(nmethodAddress1, assemblyMethod.getNativeAddress());

		List<AssemblyBlock> asmBlocks = assemblyMethod.getBlocks();

		assertEquals(2, asmBlocks.size());

		assertEquals(6, asmBlocks.get(0).getInstructions().size());
		assertEquals(21, asmBlocks.get(1).getInstructions().size());
	}

	@Test
	public void testJMHPerfAnnotations() throws ClassNotFoundException
	{
		String[] lines = new String[] {
				"                  Decoding compiled method 0x00007f25cd19c690:",
				"                  Code:",
				"                  [Disassembling for mach=&apos;i386:x86-64&apos;]",
				"                  [Entry Point]",
				"                  [Constants]",
				"                  # {method} {0x00007f25ccc5bc40} &apos;add&apos; &apos;(II",
				"                 <writer thread='139800227780352'/>",
				"                 <writer thread='139800228833024'/>",
				"                 )I&apos; in &apos;org/adoptopenjdk/jitwatch/test/TestAssemblyProcessor&apos;",
				"                    # this:     rsi:rsi   = &apos;org/openjdk/jmh/samples/generated/JMHSample_08_DeadCode_measureRight&apos;",
				"                    # parm0:    rdx:rdx   = &apos;org/openjdk/jmh/runner/InfraControl&apos;",
				"                    # parm1:    rcx:rcx   = &apos;org/openjdk/jmh/results/RawResults&apos;",
				"                    # parm2:    r8:r8     = &apos;org/openjdk/jmh/samples/generated/JMHSample_08_DeadCode_measureRight$JMHSample_08_DeadCode_1_jmh&apos;",
				"                    # parm3:    r9:r9     = &apos;org/openjdk/jmh/samples/generated/JMHSample_08_DeadCode_measureRight$Blackhole_1_jmh&apos;",
				"                    #           [sp+0x30]  (sp of caller)",
				"                    0x00007f25cd19c7e0: mov    0x8(%rsi),%r10d",
				"                    0x00007f25cd19c7e4: shl    $0x3,%r10",
				"                    0x00007f25cd19c7e8: cmp    %r10,%rax",
				"                    0x00007f25cd19c7eb: jne    0x00007f25cd045b60  ;   {runtime_call}",
				"                    0x00007f25cd19c7f1: xchg   %ax,%ax",
				"                    0x00007f25cd19c7f4: nopl   0x0(%rax,%rax,1)",
				"                    0x00007f25cd19c7fc: xchg   %ax,%ax",
				"                  [Verified Entry Point]",
				"  1.34%             0x00007f25cd19c890: vmovsd 0xa0(%r13),%xmm0",
				"                    0x00007f25cd19c899: vmovsd 0x10(%r8),%xmm1    ;*getfield d1",
				"                                                                  ; - org.openjdk.jmh.infra.Blackhole::consume@2 (line 386)",
				"                                                                  ; - org.openjdk.jmh.samples.generated.JMHSample_08_DeadCode_measureRight::measureRight_avgt_jmhLoop@19 (line 160)",
				"                    0x00007f25cd19c89f: vmovsd 0xa8(%r13),%xmm2   ;*getfield d2",
				"                                                                  ; - org.openjdk.jmh.infra.Blackhole::consume@16 (line 386)",
				"                                                                  ; - org.openjdk.jmh.samples.generated.JMHSample_08_DeadCode_measureRight::measureRight_avgt_jmhLoop@19 (line 160)",
				"                    0x00007f25cd19c8a8: fldln2 ",
				"  1.61%             0x00007f25cd19c8aa: sub    $0x8,%rsp",
				"                    0x00007f25cd19c8ae: vmovsd %xmm1,(%rsp)",
				"                    0x00007f25cd19c8b3: fldl   (%rsp)",
				"  0.95%    0.02%    0x00007f25cd19c8b6: fyl2x  ",
				" 79.12%   95.68%    0x00007f25cd19c8b8: fstpl  (%rsp)",
				"  1.41%             0x00007f25cd19c8bb: vmovsd (%rsp),%xmm1",
				"  5.43%             0x00007f25cd19c8c0: add    $0x8,%rsp          ;*invokestatic log" };

		String nmethodAddress1 = "0x00007f25cd19c690";

		// had to modify test class due to strong typed matching
		IMetaMember member = UnitTestUtil.setUpTestMember(model, getClass().getName(), "add", int.class,
				new Class<?>[] { int.class, int.class }, nmethodAddress1);

		AssemblyProcessor asmProcessor = performAssemblyParsingOn(lines);
		
		asmProcessor.attachAssemblyToMembers(model.getPackageManager());
		
		assertNotNull(member);

		AssemblyMethod assemblyMethod = member.getLastCompilation().getAssembly();

		assertNotNull(assemblyMethod);

		assertEquals(nmethodAddress1, assemblyMethod.getNativeAddress());

		assertEquals(19, assemblyMethod.getMaxAnnotationWidth());

		List<AssemblyBlock> asmBlocks = assemblyMethod.getBlocks();

		assertEquals(2, asmBlocks.size());

		assertEquals(7, asmBlocks.get(0).getInstructions().size());
		assertEquals(11, asmBlocks.get(1).getInstructions().size());

		AssemblyInstruction instr9 = asmBlocks.get(1).getInstructions().get(8);

		assertEquals("79.12%   95.68%    ", instr9.getAnnotation());
		assertEquals(Long.parseLong("00007f25cd19c8b8", 16), instr9.getAddress());
		assertEquals("fstpl", instr9.getMnemonic());

		List<String> operands = new ArrayList<>();
		operands.add("(%rsp)");

		assertEquals(operands, instr9.getOperands());
	}

	@Test
	public void testCanParseAssemblyAndNMethodUnmangled() throws Exception
	{
		String[] lines = new String[] {
				"Decoding compiled method 0x00007f7d73364190:",
				"Code:",
				"[Disassembling for mach=&apos;i386:x86-64&apos;]",
				"[Entry Point]",
				"[Verified Entry Point]",
				"[Constants]",
				"  # {method} &apos;dummyMethod2&apos; &apos;([Ljava.lang.String;)V&apos; in &apos;org/adoptopenjdk/jitwatch/test/TestAssemblyProcessor&apos;",
				"  0x00007f7d733642e0: callq  0x00007f7d77e276f0  ;   {runtime_call}",
				"[Deopt Handler Code]",
				"0x00007fb5ad0fe95c: movabs $0x7fb5ad0fe95c,%r10  ;   {section_word}",
				"0x00007fb5ad0fe966: push   %r10",
				"0x00007fb5ad0fe968: jmpq   0x00007fb5ad047100  ;   {runtime_call}",
				"0x00007fb5ad0fe96d: hlt",
				"0x00007fb5ad0fe96e: hlt",
				"0x00007fb5ad0fe96f: hlt",
				"<nmethod compile_id='1' compiler='C1' level='3' entry='0x00007fb5ad0fe420' size='2504' address='0x00007fb5ad0fe290' relocation_offset='288'/>",
				"<writer thread='140418643298048'/>" };

		String nmethodAddress1 = "0x00007f7d73364190";

		IMetaMember member = UnitTestUtil.setUpTestMember(model, getClass().getName(), "dummyMethod2", void.class,
				new Class<?>[] { Class.forName("[Ljava.lang.String;") }, nmethodAddress1);

		AssemblyProcessor asmProcessor = performAssemblyParsingOn(lines);
		
		asmProcessor.attachAssemblyToMembers(model.getPackageManager());
		
		AssemblyMethod assemblyMethod = member.getLastCompilation().getAssembly();

		assertNotNull(assemblyMethod);

		assertEquals(nmethodAddress1, assemblyMethod.getNativeAddress());

		List<AssemblyBlock> asmBlocks = assemblyMethod.getBlocks();

		// code, deopt handler
		assertEquals(2, asmBlocks.size());

		AssemblyBlock block0 = asmBlocks.get(0);
		List<AssemblyInstruction> instructions0 = block0.getInstructions();
		assertEquals(1, instructions0.size());

		AssemblyBlock block1 = asmBlocks.get(1);
		List<AssemblyInstruction> instructions1 = block1.getInstructions();
		assertEquals(6, instructions1.size());
	}

	@Test
	public void testCanParseAssemblyWhenMethodCommentIsMangled() throws ClassNotFoundException
	{
		String[] lines = new String[] {
				"<writer thread='139769647986432'/>",
				"Decoding compiled method 0x00007f1eaad25d50:",
				"Code:",
				"[Disassembling for mach=&apos;i386:x86-64&apos;]",
				"[Entry Point]",
				"[Constants]",
				"  # ",
				"<writer thread='139769789089536'/>",
				"<uncommon_trap thread='139769789089536' reason='unloaded' action='reinterpret' index='39' compile_id='2' compile_kind='osr' compiler='C2' unresolved='1' name='java/lang/System' stamp='0.560'>",
				"</uncommon_trap>",
				"<writer thread='139769647986432'/>",
				"{method} &apos;add&apos; &apos;(II)I&apos; in &apos;org/adoptopenjdk/jitwatch/test/TestAssemblyProcessor&apos;",
				"  # this:     rsi:rsi   = &apos;SandboxTest&apos;",
				"  # parm0:    rdx       = int",
				"  # parm1:    rcx       = int",
				"  #           [sp+0x20]  (sp of caller)",
				"  0x00007f1eaad25e80: mov    0x8(%rsi),%r10d",
				"  0x00007f1eaad25e84: cmp    %r10,%rax",
				"  0x00007f1eaad25e87: jne    0x00007f1eaacfd960  ;   {runtime_call}",
				"  0x00007f1eaad25e8d: data32 xchg %ax,%ax",
				"[Verified Entry Point]",
				"  0x00007f1eaad25e90: sub    $0x18,%rsp",
				"  0x00007f1eaad25e97: mov    %rbp,0x10(%rsp)    ;*synchronization entry",
				"                                                ; - SandboxTest::add@-1 (line 5)",
				"  0x00007f1eaad25e9c: mov    %edx,%eax",
				"  0x00007f1eaad25e9e: add    %ecx,%eax          ;*iadd",
				"                                                ; - SandboxTest::add@2 (line 5)",
				"  0x00007f1eaad25ea0: add    $0x10,%rsp",
				"  0x00007f1eaad25ea4: pop    %rbp",
				"  0x00007f1eaad25ea5: test   %eax,0x5d06155(%rip)        # 0x00007f1eb0a2c000",
				"                                                ;   {poll_return}",
				"  0x00007f1eaad25eab: retq   ",
				"  0x00007f1eaad25eac: hlt    " };

		String nmethodAddress1 = "0x00007f1eaad25d50";

		IMetaMember member = UnitTestUtil.setUpTestMember(model, getClass().getName(), "add", int.class,
				new Class<?>[] { int.class, int.class }, nmethodAddress1);

		AssemblyProcessor asmProcessor = performAssemblyParsingOn(lines);
		
		asmProcessor.attachAssemblyToMembers(model.getPackageManager());
		
		AssemblyMethod assemblyMethod = member.getLastCompilation().getAssembly();

		assertNotNull(assemblyMethod);

		assertEquals(nmethodAddress1, assemblyMethod.getNativeAddress());

		List<AssemblyBlock> asmBlocks = assemblyMethod.getBlocks();

		// code, deopt handler
		assertEquals(2, asmBlocks.size());

		AssemblyBlock block0 = asmBlocks.get(0);
		List<AssemblyInstruction> instructions0 = block0.getInstructions();
		assertEquals(4, instructions0.size());

		AssemblyBlock block1 = asmBlocks.get(1);
		List<AssemblyInstruction> instructions1 = block1.getInstructions();
		assertEquals(9, instructions1.size());
	}
	
	@Test
	public void testJDK9NativeAndNonNativeAssembly()
	{
		String[] lines  = new String[]{
				"Compiled method (c1)     142   35       3       java.nio.DirectLongBufferU::ix (10 bytes)",
				" total in heap  [0x00007f81ed8e7b10,0x00007f81ed8e7eb0] = 928",
				" relocation     [0x00007f81ed8e7c80,0x00007f81ed8e7ca8] = 40",
				" main code      [0x00007f81ed8e7cc0,0x00007f81ed8e7da0] = 224",
				" stub code      [0x00007f81ed8e7da0,0x00007f81ed8e7e30] = 144",
				" metadata       [0x00007f81ed8e7e30,0x00007f81ed8e7e38] = 8",
				" scopes data    [0x00007f81ed8e7e38,0x00007f81ed8e7e58] = 32",
				" scopes pcs     [0x00007f81ed8e7e58,0x00007f81ed8e7ea8] = 80",
				" dependencies   [0x00007f81ed8e7ea8,0x00007f81ed8e7eb0] = 8",
				"----------------------------------------------------------------------",
				"Loaded disassembler from /home/chris/jdk-9/lib/server/hsdis-amd64.so",
				"java/nio/DirectLongBufferU.ix(I)J  [0x00007f81ed8e7cc0, 0x00007f81ed8e7e30]  368 bytes",
				"[Disassembling for mach=&apos;i386:x86-64&apos;]",
				"[Entry Point]",
				"[Constants]",
				"  # {method} {0x00007f81c0354508} &apos;ix&apos; &apos;(I)J&apos; in &apos;java/nio/DirectLongBufferU&apos;",
				"  # this:     rsi:rsi   = &apos;java/nio/DirectLongBufferU&apos;",
				"  # parm0:    rdx       = int",
				"  #           [sp+0x50]  (sp of caller)",
				"  0x00007f81ed8e7cc0: mov    0x8(%rsi),%r10d",
				"  0x00007f81ed8e7cc4: shl    $0x3,%r10",
				"  0x00007f81ed8e7cc8: cmp    %rax,%r10",
				"  0x00007f81ed8e7ccb: jne    0x00007f81ed395000  ;   {runtime_call ic_miss_stub}",
				"  0x00007f81ed8e7cd1: data16 data16 nopw 0x0(%rax,%rax,1)",
				"  0x00007f81ed8e7cdc: data16 data16 xchg %ax,%ax",
				"[Verified Entry Point]",
				"  0x00007f81ed8e7ce0: mov    %eax,-0x14000(%rsp)",
				"  0x00007f81ed8e7ce7: push   %rbp",
				"  0x00007f81ed8e7ce8: sub    $0x40,%rsp",
				"  0x00007f81ed8e7cec: movabs $0x7f81c039bed8,%rcx  ;   {metadata(method data for {method} {0x00007f81c0354508} &apos;ix&apos; &apos;(I)J&apos; in &apos;java/nio/DirectLongBufferU&apos;)}",
				"  0x00007f81ed8e7cf6: mov    0xfc(%rcx),%eax",
				"  0x00007f81ed8e7cfc: add    $0x8,%eax",
				"  0x00007f81ed8e7cff: mov    %eax,0xfc(%rcx)",
				"  0x00007f81ed8e7d05: and    $0x1ff8,%eax",
				"  0x00007f81ed8e7d0b: cmp    $0x0,%eax",
				"  0x00007f81ed8e7d9b: hlt    ",
				"  0x00007f81ed8e7d9c: hlt    ",
				"  0x00007f81ed8e7d9d: hlt    ",
				"  0x00007f81ed8e7d9e: hlt    ",
				"  0x00007f81ed8e7d9f: hlt    ",
				"[Exception Handler]",
				"[Stub Code]",
				"  0x00007f81ed8e7da0: callq  0x00007f81ed3cbb80  ;   {no_reloc}",
				"  0x00007f81ed8e7da5: mov    %rsp,-0x28(%rsp)",
				"  0x00007f81ed8e7daa: sub    $0x80,%rsp",
				"  0x00007f81ed8e7db1: mov    %rax,0x78(%rsp)",
				"  0x00007f81ed8e7df2: mov    %r14,0x8(%rsp)",
				"  0x00007f81ed8e7df7: mov    %r15,(%rsp)",
				"  0x00007f81ed8e7dfb: movabs $0x7f820db02afa,%rdi  ;   {external_word}",
				"  0x00007f81ed8e7e05: movabs $0x7f81ed8e7da5,%rsi  ;   {internal_word}",
				"  0x00007f81ed8e7e0f: mov    %rsp,%rdx",
				"  0x00007f81ed8e7e12: and    $0xfffffffffffffff0,%rsp",
				"  0x00007f81ed8e7e16: callq  0x00007f820d7ac350  ;   {runtime_call MacroAssembler::debug64(char*, long, long*)}",
				"  0x00007f81ed8e7e1b: hlt    ",
				"[Deopt Handler Code]",
				"  0x00007f81ed8e7e1c: movabs $0x7f81ed8e7e1c,%r10  ;   {section_word}",
				"  0x00007f81ed8e7e26: push   %r10",
				"  0x00007f81ed8e7e28: jmpq   0x00007f81ed396820  ;   {runtime_call DeoptimizationBlob}",
				"  0x00007f81ed8e7e2d: hlt    ",
				"  0x00007f81ed8e7e2e: hlt    ",
				"  0x00007f81ed8e7e2f: hlt    ",
				"",
				"ImmutableOopMap{rsi=Oop }pc offsets: 142 </print_nmethod>",
				"----------------------------------------------------------------------",
				"java/lang/System.arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V  [0x00007f81f4e124e0, 0x00007f81f4e126e8]  520 bytes",
				"[Entry Point]",
				"  # {method} {0x00007f81c00ca6b0} &apos;arraycopy&apos; &apos;(Ljava/lang/Object;ILjava/lang/Object;II)V&apos; in &apos;java/lang/System&apos;",
				"  # parm0:    rsi:rsi   = &apos;java/lang/Object&apos;",
				"  # parm1:    rdx       = int",
				"  # parm2:    rcx:rcx   = &apos;java/lang/Object&apos;",
				"  # parm3:    r8        = int",
				"  # parm4:    r9        = int",
				"  #           [sp+0x60]  (sp of caller)",
				"  0x00007f81f4e124e0: mov    0x8(%rsi),%r10d",
				"  0x00007f81f4e124e4: shl    $0x3,%r10",
				"  0x00007f81f4e124e8: cmp    %r10,%rax",
				"  0x00007f81f4e124eb: je     0x00007f81f4e124f8",
				"  0x00007f81f4e124f1: jmpq   0x00007f81ed395000  ;   {runtime_call ic_miss_stub}",
				"  0x00007f81f4e124f6: xchg   %ax,%ax",
				"[Verified Entry Point]",
				"  0x00007f81f4e124f8: mov    %eax,-0x14000(%rsp)",
				"  0x00007f81f4e124ff: push   %rbp",
				"  0x00007f81f4e12500: mov    %rsp,%rbp",
				"  0x00007f81f4e12503: sub    $0x50,%rsp",
				"  0x00007f81f4e126e4: hlt    ",
				"  0x00007f81f4e126e5: hlt    ",
				"  0x00007f81f4e126e6: hlt    ",
				"  0x00007f81f4e126e7: hlt    ",
				"Compiled method (c1)     144   37       3       java.lang.String::length (11 bytes)",
				" total in heap  [0x00007f81ed8e7f10,0x00007f81ed8e8458] = 1352",
				" relocation     [0x00007f81ed8e8080,0x00007f81ed8e80b8] = 56",
				" main code      [0x00007f81ed8e80c0,0x00007f81ed8e82a0] = 480",
				" stub code      [0x00007f81ed8e82a0,0x00007f81ed8e8330] = 144",
				" metadata       [0x00007f81ed8e8330,0x00007f81ed8e8340] = 16",
				" scopes data    [0x00007f81ed8e8340,0x00007f81ed8e8390] = 80",
				" scopes pcs     [0x00007f81ed8e8390,0x00007f81ed8e8440] = 176",
				" dependencies   [0x00007f81ed8e8440,0x00007f81ed8e8448] = 8",
				" nul chk table  [0x00007f81ed8e8448,0x00007f81ed8e8458] = 16",
				"----------------------------------------------------------------------",
				"java/lang/String.length()I  [0x00007f81ed8e80c0, 0x00007f81ed8e8330]  624 bytes",
				"[Entry Point]",
				"[Constants]",
				"  # {method} {0x00007f81c00adc40} &apos;length&apos; &apos;()I&apos; in &apos;java/lang/String&apos;",
				"  #           [sp+0x40]  (sp of caller)",
				"  0x00007f81ed8e80c0: mov    0x8(%rsi),%r10d",
				"  0x00007f81ed8e80c4: shl    $0x3,%r10",
				"  0x00007f81ed8e80c8: cmp    %rax,%r10",
				"  0x00007f81ed8e80cb: jne    0x00007f81ed395000  ;   {runtime_call ic_miss_stub}",
				"  0x00007f81ed8e80d1: data16 data16 nopw 0x0(%rax,%rax,1)",
				"  0x00007f81ed8e80dc: data16 data16 xchg %ax,%ax",
				"[Verified Entry Point]",
				"  0x00007f81ed8e80e0: mov    %eax,-0x14000(%rsp)",
				"  0x00007f81ed8e80e7: push   %rbp",
				"  0x00007f81ed8e80e8: sub    $0x30,%rsp"
				};
		
		AssemblyProcessor asmProcessor = performAssemblyParsingOn(lines);
		
		assertEquals(3, asmProcessor.getAssemblyMethods().size());
		
		AssemblyMethod method0 = asmProcessor.getAssemblyMethods().get(0);
		AssemblyMethod method1 = asmProcessor.getAssemblyMethods().get(1);
		AssemblyMethod method2 = asmProcessor.getAssemblyMethods().get(2);
		
		assertEquals("# {method} {0x00007f81c0354508} 'ix' '(I)J' in 'java/nio/DirectLongBufferU'", method0.getAssemblyMethodSignature());
		assertEquals("# {method} {0x00007f81c00ca6b0} 'arraycopy' '(Ljava/lang/Object;ILjava/lang/Object;II)V' in 'java/lang/System'", method1.getAssemblyMethodSignature());
		assertEquals("# {method} {0x00007f81c00adc40} 'length' '()I' in 'java/lang/String'", method2.getAssemblyMethodSignature());
		
		assertEquals("0x00007f81ed8e7b10", method0.getNativeAddress());
		assertEquals("0x00007f81ed8e7cc0", method0.getEntryAddress());

		assertEquals("0x00007f81f4e124e0", method1.getEntryAddress());
		
		assertEquals("0x00007f81ed8e7f10", method2.getNativeAddress());
		assertEquals("0x00007f81ed8e80c0", method2.getEntryAddress());		
	}
	
	@Test
	public void testRegressionGraalIssue265()
	{
		// <nmethod compile_id='125' compiler='C1' level='1' entry='0x00007fab74150300' size='736' address='0x00007fab74150190' relocation_offset='312' 
		// insts_offset='368' stub_offset='496' scopes_data_offset='648' scopes_pcs_offset='664' dependencies_offset='728' metadata_offset='640' 
		// method='java/lang/Integer intValue ()I' bytes='5' count='206' iicount='206' stamp='0.501'/>

		String[] lines = new String[] {
				"Loaded disassembler from /home/marschall/bin/java/graalvm-0.27/jre/lib/amd64/server/hsdis-amd64.so",
				"----------------------------------------------------------------------",				
				"java/lang/Integer.intValue  [0x00007fab74150300, 0x00007fab74150410]  272 bytes",
				"[Disassembling for mach=&apos;i386:x86-64&apos;]",
				"[Entry Point]",
				"[Constants]",
				"  # {method} {0x00007fab6194cf00} &apos;intValue&apos; &apos;()I&apos; in &apos;java/lang/Integer&apos;",
				"  #           [sp+0x40]  (sp of caller)",
				"  0x00007fab74150300: mov    0x8(%rsi),%r10d",
				"  0x00007fab74150304: shl    $0x3,%r10",
				"  0x00007fab74150308: cmp    %rax,%r10",
				"  0x00007fab7415030b: jne    0x00007fab74047b60  ;   {runtime_call}",
				"  0x00007fab74150311: data16 data16 nopw 0x0(%rax,%rax,1)",
				"  0x00007fab7415031c: data16 data16 xchg %ax,%ax",
				"[Verified Entry Point]",
				"  0x00007fab74150320: mov    %eax,-0x14000(%rsp)",
				"  0x00007fab74150327: push   %rbp",
				"  0x00007fab74150328: sub    $0x30,%rsp         ;*aload_0 {reexecute=0 rethrow=0 return_oop=0}",
				"                                                ; - java.lang.Integer::intValue@0 (line 893)",
				"  0x00007fab7415032c: mov    0xc(%rsi),%eax     ;*getfield value {reexecute=0 rethrow=0 return_oop=0}",
				"                                                ; - java.lang.Integer::intValue@1 (line 893)"
				};

		AssemblyProcessor asmProcessor = performAssemblyParsingOn(lines);
		
		assertEquals(1, asmProcessor.getAssemblyMethods().size());
		
		AssemblyMethod method0 = asmProcessor.getAssemblyMethods().get(0);
		
		assertEquals("# {method} {0x00007fab6194cf00} 'intValue' '()I' in 'java/lang/Integer'", method0.getAssemblyMethodSignature());

		assertEquals("0x00007fab74150300", method0.getEntryAddress());		
	}

	@Test
	public void testJDK13AssemblyWithoutMachIdentifier()
	{
		String[] lines = new String[]{
				"<print_nmethod compile_id='1' compiler='c1' level='3' stamp='0.071'>",
				"============================= C1-compiled nmethod ==============================",
				"----------------------------------- Assembly -----------------------------------",
				"Compiled method (c1)      72    1       3       java.lang.Object::&lt;init&gt; (1 bytes)",
				" total in heap  [0x00007fce68d35010,0x00007fce68d35380] = 880",
				" relocation     [0x00007fce68d35170,0x00007fce68d35198] = 40",
				" main code      [0x00007fce68d351a0,0x00007fce68d35280] = 224", // get from here
				" stub code      [0x00007fce68d35280,0x00007fce68d35310] = 144",
				" metadata       [0x00007fce68d35310,0x00007fce68d35320] = 16",
				" scopes data    [0x00007fce68d35320,0x00007fce68d35338] = 24",
				" scopes pcs     [0x00007fce68d35338,0x00007fce68d35378] = 64",
				" dependencies   [0x00007fce68d35378,0x00007fce68d35380] = 8",
				"--------------------------------------------------------------------------------",
				"[Constant Pool (empty)]",
				"--------------------------------------------------------------------------------",
				"[Entry Point]",
				"  # {method} {0x00000008003e1dc0} &apos;&lt;init&gt;&apos; &apos;()V&apos; in &apos;java/lang/Object&apos;",
				"  #           [sp+0x40]  (sp of caller)",
				"  0x00007fce68d351a0:   mov    0x8(%rsi),%r10d",
				"  0x00007fce68d351a4:   shl    $0x3,%r10",
				"  0x00007fce68d351a8:   movabs $0x800000000,%r12",
				"  0x00007fce68d351b2:   add    %r12,%r10",
				"  0x00007fce68d351b5:   xor    %r12,%r12",
				"  0x00007fce68d351b8:   cmp    %rax,%r10",
				"  0x00007fce68d351bb:   jne    0x00007fce687eed00           ;   {runtime_call ic_miss_stub}",
				"  0x00007fce68d351c1:   data16 data16 nopw 0x0(%rax,%rax,1)",
				"  0x00007fce68d351cc:   data16 data16 xchg %ax,%ax",
				"  0x00007fce68d351d0:   data16 data16 nopw 0x0(%rax,%rax,1)",
				"  0x00007fce68d351db:   data16 data16 xchg %ax,%ax",
				"  0x00007fce68d351df:   nop"
		};

		AssemblyProcessor asmProcessor = performAssemblyParsingOn(lines);

		assertEquals(1, asmProcessor.getAssemblyMethods().size());

		AssemblyMethod method0 = asmProcessor.getAssemblyMethods().get(0);

		assertEquals("# {method} {0x00000008003e1dc0} '<init>' '()V' in 'java/lang/Object'", method0.getAssemblyMethodSignature());

		assertEquals("0x00007fce68d351a0", method0.getEntryAddress());
	}
}
