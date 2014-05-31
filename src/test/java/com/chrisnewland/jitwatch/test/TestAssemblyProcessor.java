/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.test;

import com.chrisnewland.jitwatch.core.AssemblyProcessor;
import com.chrisnewland.jitwatch.core.IMemberFinder;
import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.MetaClass;
import com.chrisnewland.jitwatch.model.MetaMethod;
import com.chrisnewland.jitwatch.model.MetaPackage;
import com.chrisnewland.jitwatch.model.assembly.AssemblyBlock;
import com.chrisnewland.jitwatch.model.assembly.AssemblyInstruction;
import com.chrisnewland.jitwatch.model.assembly.AssemblyMethod;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.LOADED;
import static com.chrisnewland.jitwatch.core.JITWatchConstants.S_OPEN_ANGLE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class TestAssemblyProcessor
{
    private static final int ZERO = 0;
    private Map<String, IMetaMember> map;
    private IMemberFinder memberFinder;

    private static final String[] SINGLE_ASSEMBLY_METHOD = new String[]{
            "Decoding compiled method 0x00007f7d73364190:",
            "Code:",
            "[Disassembling for mach=&apos;i386:x86-64&apos;]",
            "[Entry Point]",
            "[Verified Entry Point]",
            "[Constants]",
            "  # {method} &apos;main&apos; &apos;([Ljava/lang/String;)V&apos; in &apos;com/chrisnewland/jitwatch/demo/SandboxTestLoad&apos;",
            "  0x00007f7d733642e0: callq  0x00007f7d77e276f0  ;   {runtime_call}",
            "  0x00007f7d733642e5: data32 data32 nopw 0x0(%rax,%rax,1)",
            "  0x00007f7d733642f0: mov    %eax,-0x14000(%rsp)",
            "  0x00007f7d733642f7: push   %rbp",
            "  0x00007f7d733642f8: sub    $0x20,%rsp"
    };

	@Before
	public void setup()
	{
		map = new HashMap<>();

		memberFinder = new IMemberFinder()
		{
			@Override
			public IMetaMember findMemberWithSignature(String logSignature)
			{
				Method objToStringMethod = UnitTestUtil.getMethod("java.lang.Object", "toString",  new Class[0]);			
				
				MetaPackage fakePackage = new MetaPackage("java.lang");
				
				MetaClass fakeClass = new MetaClass(fakePackage, "java.lang.Object");
				
				IMetaMember fakeMember = new MetaMethod(objToStringMethod, fakeClass);
				
				map.put(logSignature, fakeMember);
				
				return map.get(logSignature);
			}
		};
	}

	@Test
	public void testSingleAsmMethod()
	{
		String[] lines = SINGLE_ASSEMBLY_METHOD;

        performAssemblyParsingOn(lines);
		
		IMetaMember member = map.get("com.chrisnewland.jitwatch.demo.SandboxTestLoad main ([Ljava.lang.String;)V");
		
		assertNotNull(member);
		
		AssemblyMethod assemblyMethod = member.getAssembly();
		
		assertNotNull(assemblyMethod);
		
		List<AssemblyBlock> asmBlocks = assemblyMethod.getBlocks();
		
		assertEquals(1, asmBlocks.size());

		AssemblyBlock block = asmBlocks.get(0);
		
		List<AssemblyInstruction> instructions = block.getInstructions();
		
		assertEquals(5, instructions.size());
	}

    private void performAssemblyParsingOn(String[] lines) {
        AssemblyProcessor asmProcessor = new AssemblyProcessor(memberFinder);

        for (String line : lines)
        {
            if (!line.startsWith(S_OPEN_ANGLE) && !line.startsWith(LOADED))
            {
                asmProcessor.handleLine(line);
            }
        }

        asmProcessor.complete();
    }

    @Test
	public void testSingleAsmMethodInterrupted()
	{
		String[] lines = new String[]{
				"Decoding compiled method 0x00007f7d73363f90:",
				"Code:",
				"[Entry Point]",
				"[Constants]",
				"  # {method} &apos;add&apos; &apos;(II)I&apos; in &apos;com",
				"<writer thread='140176877946624'/>",
				"<uncommon_trap thread='140176877946624' reason='unloaded' action='reinterpret' index='39' compile_id='2' compile_kind='osr' compiler='C2' unresolved='1' name='java/lang/System' stamp='0.374'>",
				"<jvms bci='31' method='com/chrisnewland/jitwatch/demo/SandboxTestLoad main ([Ljava/lang/String;)V' bytes='57' count='10000' backedge_count='6024' iicount='1'/>",
				"</uncommon_trap>",
				"<writer thread='140176736904960'/>",
				"/chrisnewland/jitwatch/demo/SandboxTest&apos;",
				"  # this:     rsi:rsi   = &apos;com/chrisnewland/jitwatch/demo/SandboxTest&apos;",
				"  # parm0:    rdx       = int",
				"  # parm1:    rcx       = int",
				"  #           [sp+0x20]  (sp of caller)",
				"  0x00007f7d733640c0: mov    0x8(%rsi),%r10d",
				"  0x00007f7d733640c4: cmp    %r10,%rax",
				"  0x00007f7d733640c7: jne    0x00007f7d7333b960  ;   {runtime_call}",
				"  0x00007f7d733640cd: data32 xchg %ax,%ax"
		};

        performAssemblyParsingOn(lines);
		
		IMetaMember member = map.get("com.chrisnewland.jitwatch.demo.SandboxTest add (II)I");
		
		assertNotNull(member);
		
		AssemblyMethod assemblyMethod = member.getAssembly();
		
		assertNotNull(assemblyMethod);
		
		List<AssemblyBlock> asmBlocks = assemblyMethod.getBlocks();
		
		assertEquals(1, asmBlocks.size());

		AssemblyBlock block = asmBlocks.get(0);
		
		List<AssemblyInstruction> instructions = block.getInstructions();
		
		assertEquals(4, instructions.size());
	}
	
	@Test
	public void testTwoMethodsWithOneInterrupted()
	{
		String[] lines = new String[]{
				"Decoding compiled method 0x00007f7d73364190:",
				"Code:",
				"[Disassembling for mach=&apos;i386:x86-64&apos;]",
				"[Entry Point]",
				"[Verified Entry Point]",
				"[Constants]",
				"  # {method} &apos;main&apos; &apos;([Ljava/lang/String;)V&apos; in &apos;com/chrisnewland/jitwatch/demo/SandboxTestLoad&apos;",
				"  0x00007f7d733642e0: callq  0x00007f7d77e276f0  ;   {runtime_call}",
				"  0x00007f7d733642e5: data32 data32 nopw 0x0(%rax,%rax,1)",
				"  0x00007f7d733642f0: mov    %eax,-0x14000(%rsp)",
				"  0x00007f7d733642f7: push   %rbp",
				"  0x00007f7d733642f8: sub    $0x20,%rsp",
				"<nmethod compile_id='2' compile_kind='osr' compiler='C2' entry='0x00007f7d733642e0' size='800' address='0x00007f7d73364190' relocation_offset='288' insts_offset='336' stub_offset='528' scopes_data_offset='576' scopes_pcs_offset='632' dependencies_offset='792' oops_offset='552' method='com/chrisnewland/jitwatch/demo/SandboxTestLoad main ([Ljava/lang/String;)V' bytes='57' count='10000' backedge_count='5377' iicount='1' stamp='0.373'/>",
				"<writer thread='140176736904960'/>",
				"Decoding compiled method 0x00007f7d73363f90:",
				"Code:",
				"[Entry Point]",
				"[Constants]",
				"  # {method} &apos;add&apos; &apos;(II)I&apos; in &apos;com",
				"<writer thread='140176877946624'/>",
				"<uncommon_trap thread='140176877946624' reason='unloaded' action='reinterpret' index='39' compile_id='2' compile_kind='osr' compiler='C2' unresolved='1' name='java/lang/System' stamp='0.374'>",
				"<jvms bci='31' method='com/chrisnewland/jitwatch/demo/SandboxTestLoad main ([Ljava/lang/String;)V' bytes='57' count='10000' backedge_count='6024' iicount='1'/>",
				"</uncommon_trap>",
				"<writer thread='140176736904960'/>",
				"/chrisnewland/jitwatch/demo/SandboxTest&apos;",
				"  # this:     rsi:rsi   = &apos;com/chrisnewland/jitwatch/demo/SandboxTest&apos;",
				"  # parm0:    rdx       = int",
				"  # parm1:    rcx       = int",
				"  #           [sp+0x20]  (sp of caller)",
				"  0x00007f7d733640c0: mov    0x8(%rsi),%r10d",
				"  0x00007f7d733640c4: cmp    %r10,%rax",
				"  0x00007f7d733640c7: jne    0x00007f7d7333b960  ;   {runtime_call}",
				"  0x00007f7d733640cd: data32 xchg %ax,%ax"

		};

        performAssemblyParsingOn(lines);
		
		IMetaMember member = map.get("com.chrisnewland.jitwatch.demo.SandboxTestLoad main ([Ljava.lang.String;)V");
		
		assertNotNull(member);
		
		AssemblyMethod assemblyMethod = member.getAssembly();
		
		assertNotNull(assemblyMethod);
		
		List<AssemblyBlock> asmBlocks = assemblyMethod.getBlocks();
		
		assertEquals(1, asmBlocks.size());

		AssemblyBlock block = asmBlocks.get(0);
		
		List<AssemblyInstruction> instructions = block.getInstructions();
		
		assertEquals(5, instructions.size());
		
		IMetaMember member2 = map.get("com.chrisnewland.jitwatch.demo.SandboxTest add (II)I");
		
		assertNotNull(member2);
		
		AssemblyMethod assemblyMethod2 = member2.getAssembly();
		
		assertNotNull(assemblyMethod2);
		
		List<AssemblyBlock> asmBlocks2 = assemblyMethod2.getBlocks();
		
		assertEquals(1, asmBlocks2.size());

		AssemblyBlock block2 = asmBlocks2.get(0);
		
		List<AssemblyInstruction> instructions2 = block2.getInstructions();
		
		assertEquals(4, instructions2.size());
	}

    /*
        Scenario: Parsing a line that does not start with the Native Code
            Given a line with some text
            When the assembly processor parses such a line
            Then no assembly instructions are returned
    */
    @Test
    public void givenBlockOfCodeWithoutTheNativeCodeStart_WhenTheAssemblyProcessorActionsIt_ThenNoInstructionsAreReturned() {
        // Given
        int expectedAssemblyResults = ZERO;
        String[] lines = new String[] {
                "Does not start with Decoding compiled method 0x00007f7d73364190:",
                "Code:"
        };

        // When
        performAssemblyParsingOn(lines);
        int actualAssemblyResults = map.size();

        // Then
        assertThat("No assembly results should have been returned.",
                actualAssemblyResults,
                is(equalTo(expectedAssemblyResults)));
    }
}