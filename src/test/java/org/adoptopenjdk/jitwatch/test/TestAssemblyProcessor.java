/*
 * Copyright (c) 2013-2015 Chris Newland.
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

import org.adoptopenjdk.jitwatch.core.AssemblyProcessor;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.JITDataModel;
import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyBlock;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyInstruction;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyMethod;
import org.junit.Before;
import org.junit.Test;

public class TestAssemblyProcessor
{
	private JITDataModel model;

	private static final String[] SINGLE_ASSEMBLY_METHOD = new String[] {
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

	private IMetaMember setUpTestMember(String fqClassName, String memberName, Class<?> returnType, Class<?>[] params)
			throws ClassNotFoundException
	{
		MetaClass metaClass = model.getPackageManager().getMetaClass(fqClassName);

		if (metaClass == null)
		{
			metaClass = UnitTestUtil.createMetaClassFor(model, fqClassName);
		}

		List<String> paramList = new ArrayList<>();

		for (Class<?> clazz : params)
		{
			paramList.add(clazz.getName());
		}

		MemberSignatureParts msp = MemberSignatureParts.fromParts(fqClassName, memberName, returnType.getName(), paramList);

		IMetaMember createdMember = metaClass.getMemberForSignature(msp);

		return createdMember;
	}

	@Test
	public void testSingleAsmMethod() throws ClassNotFoundException
	{
		String[] lines = SINGLE_ASSEMBLY_METHOD;

		IMetaMember createdMember = setUpTestMember(getClass().getName(), "dummyMethod", void.class, new Class<?>[0]);

		performAssemblyParsingOn(lines);

		List<AssemblyMethod> assemblyMethods = createdMember.getAssemblyMethods();

		AssemblyMethod assemblyMethod = assemblyMethods.get(assemblyMethods.size() - 1);

		assertNotNull(assemblyMethod);

		List<AssemblyBlock> asmBlocks = assemblyMethod.getBlocks();

		assertEquals(1, asmBlocks.size());

		AssemblyBlock block = asmBlocks.get(0);

		List<AssemblyInstruction> instructions = block.getInstructions();

		assertEquals(5, instructions.size());
	}

	private void performAssemblyParsingOn(String[] lines)
	{
		AssemblyProcessor asmProcessor = new AssemblyProcessor(model.getPackageManager());

		for (String line : lines)
		{
			String trimmedLine = line.trim();
			
			if (!trimmedLine.startsWith(S_OPEN_ANGLE) && !trimmedLine.startsWith(LOADED))
			{
				asmProcessor.handleLine(trimmedLine);
			}
		}

		asmProcessor.complete();
	}

	@Test
	public void testSingleAsmMethodInterrupted() throws ClassNotFoundException
	{
		String[] lines = new String[] {
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

		IMetaMember member = setUpTestMember(getClass().getName(), "add", int.class, new Class<?>[] { int.class, int.class });

		performAssemblyParsingOn(lines);

		assertNotNull(member);

		List<AssemblyMethod> assemblyMethods = member.getAssemblyMethods();

		AssemblyMethod assemblyMethod = assemblyMethods.get(assemblyMethods.size() - 1);
		
		assertNotNull(assemblyMethod);

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

		IMetaMember member = setUpTestMember(getClass().getName(), "dummyMethod2", void.class,
				new Class<?>[] { Class.forName("[Ljava.lang.String;") });

		IMetaMember member2 = setUpTestMember(getClass().getName(), "add", int.class, new Class<?>[] { int.class, int.class });

		performAssemblyParsingOn(lines);

		List<AssemblyMethod> assemblyMethods = member.getAssemblyMethods();
		AssemblyMethod assemblyMethod = assemblyMethods.get(assemblyMethods.size() - 1);
		
		assertNotNull(assemblyMethod);

		List<AssemblyBlock> asmBlocks = assemblyMethod.getBlocks();

		assertEquals(1, asmBlocks.size());

		AssemblyBlock block = asmBlocks.get(0);

		List<AssemblyInstruction> instructions = block.getInstructions();

		assertEquals(5, instructions.size());

		List<AssemblyMethod> assemblyMethods2 = member2.getAssemblyMethods();
		AssemblyMethod assemblyMethod2 = assemblyMethods2.get(assemblyMethods2.size() - 1);
		
		assertNotNull(assemblyMethod2);

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

		IMetaMember member = setUpTestMember("java.lang.String", "hashCode", int.class, new Class<?>[0]);

		performAssemblyParsingOn(lines);

		List<AssemblyMethod> assemblyMethods = member.getAssemblyMethods();
		AssemblyMethod assemblyMethod = assemblyMethods.get(assemblyMethods.size() - 1);
		
		assertNotNull(assemblyMethod);

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

		// had to modify test class due to strong typed matching
		IMetaMember member = setUpTestMember(getClass().getName(), "add", int.class, new Class<?>[] { int.class, int.class });

		performAssemblyParsingOn(lines);

		assertNotNull(member);

		List<AssemblyMethod> assemblyMethods = member.getAssemblyMethods();
		AssemblyMethod assemblyMethod = assemblyMethods.get(assemblyMethods.size() - 1);
		
		assertNotNull(assemblyMethod);

		assertEquals(19, assemblyMethod.getMaxAnnotationWidth());

		List<AssemblyBlock> asmBlocks = assemblyMethod.getBlocks();

		assertEquals(2, asmBlocks.size());

		assertEquals(7, asmBlocks.get(0).getInstructions().size());
		assertEquals(11, asmBlocks.get(1).getInstructions().size());

		AssemblyInstruction instr9 = asmBlocks.get(1).getInstructions().get(8);

		assertEquals("79.12%   95.68%    ", instr9.getAnnotation());
		assertEquals(Long.parseLong("00007f25cd19c8b8", 16), instr9.getAddress());
		assertEquals("fstpl", instr9.getMnemonic());

		List<String> operands = new ArrayList<String>();
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

		IMetaMember member = setUpTestMember(getClass().getName(), "dummyMethod2", void.class,
				new Class<?>[] { Class.forName("[Ljava.lang.String;") });

		performAssemblyParsingOn(lines);

		List<AssemblyMethod> assemblyMethods = member.getAssemblyMethods();
		AssemblyMethod assemblyMethod = assemblyMethods.get(assemblyMethods.size() - 1);
		
		assertNotNull(assemblyMethod);

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

		IMetaMember member = setUpTestMember(getClass().getName(), "add", int.class, new Class<?>[] { int.class, int.class });

		performAssemblyParsingOn(lines);

		List<AssemblyMethod> assemblyMethods = member.getAssemblyMethods();
		AssemblyMethod assemblyMethod = assemblyMethods.get(assemblyMethods.size() - 1);
		
		assertNotNull(assemblyMethod);

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
}