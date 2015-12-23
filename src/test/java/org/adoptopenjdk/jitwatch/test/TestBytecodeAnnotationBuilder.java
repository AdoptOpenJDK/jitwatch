/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_ARGUMENTS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_HOLDER;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_ID;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_METHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_NAME;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_RETURN;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_DOT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SLASH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_TYPE_NAME_VOID;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_KLASS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_METHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PARSE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.adoptopenjdk.jitwatch.core.TagProcessor;
import org.adoptopenjdk.jitwatch.journal.JournalUtil;
import org.adoptopenjdk.jitwatch.loader.BytecodeLoader;
import org.adoptopenjdk.jitwatch.model.AnnotationException;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IParseDictionary;
import org.adoptopenjdk.jitwatch.model.JITDataModel;
import org.adoptopenjdk.jitwatch.model.ParseDictionary;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.model.bytecode.BCAnnotationType;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeAnnotationBuilder;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeAnnotations;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.model.bytecode.LineAnnotation;
import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;
import org.adoptopenjdk.jitwatch.util.StringUtil;
import org.junit.After;
import org.junit.Test;

public class TestBytecodeAnnotationBuilder
{
	@After
	public void checkUnhandledTags()
	{
		assertEquals(0, JournalUtil.getUnhandledTagCount());
	}

	@Test
	public void testSanityCheckInlineFail()
	{
		BytecodeInstruction instrAaload = new BytecodeInstruction();
		instrAaload.setOpcode(Opcode.AALOAD);

		assertFalse(BytecodeAnnotationBuilder.sanityCheckInline(instrAaload));
	}

	@Test
	public void testSanityCheckInlinePass()
	{
		BytecodeInstruction instrInvokeSpecial = new BytecodeInstruction();
		instrInvokeSpecial.setOpcode(Opcode.INVOKESPECIAL);

		assertTrue(BytecodeAnnotationBuilder.sanityCheckInline(instrInvokeSpecial));
	}

	@Test
	public void testSanityCheckBranchFail()
	{
		BytecodeInstruction instrAaload = new BytecodeInstruction();
		instrAaload.setOpcode(Opcode.AALOAD);

		assertFalse(BytecodeAnnotationBuilder.sanityCheckBranch(instrAaload));
	}

	@Test
	public void testSanityCheckBranchPass()
	{
		BytecodeInstruction instrIfcmpne = new BytecodeInstruction();
		instrIfcmpne.setOpcode(Opcode.IF_ICMPNE);

		assertTrue(BytecodeAnnotationBuilder.sanityCheckBranch(instrIfcmpne));
	}

	@Test
	public void testJava7NonTieredLeaf()
	{
		String[] logLines = new String[] {
				"<task compile_id='82' compile_kind='osr' method='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog testLeaf (J)V' bytes='66' count='10000' backedge_count='5122' iicount='1' osr_bci='8' stamp='11.372'>",
				"<phase name='parse' nodes='3' live='3' stamp='11.372'>",
				"<type id='636' name='void'/>",
				"<type id='635' name='long'/>",
				"<klass id='729' name='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog' flags='1'/>",
				"<method id='730' holder='729' name='testLeaf' return='636' arguments='635' flags='2' bytes='66' iicount='1'/>",
				"<parse method='730' uses='1' osr_bci='8' stamp='11.372'>",
				"<dependency type='leaf_type' ctxk='729'/>",
				"<dependency type='leaf_type' ctxk='729'/>",
				"<uncommon_trap bci='8' reason='constraint' action='reinterpret'/>",
				"<uncommon_trap bci='8' reason='predicate' action='maybe_recompile'/>",
				"<uncommon_trap bci='8' reason='loop_limit_check' action='maybe_recompile'/>",
				"<bc code='183' bci='10'/>",
				"<method id='732' holder='729' name='leaf1' return='635' arguments='635' flags='2' bytes='4' compile_id='78' compiler='C2' iicount='10013'/>",
				"<call method='732' count='11725' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='732' uses='11725' stamp='11.373'>",
				"<uncommon_trap bci='10' reason='null_check' action='maybe_recompile'/>",
				"<parse_done nodes='145' live='140' memory='42224' stamp='11.373'/>",
				"</parse>",
				"<bc code='183' bci='16'/>",
				"<method id='733' holder='729' name='leaf2' return='635' arguments='635' flags='2' bytes='6' compile_id='79' compiler='C2' iicount='10013'/>",
				"<call method='733' count='11725' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='733' uses='11725' stamp='11.373'>",
				"<parse_done nodes='163' live='157' memory='45040' stamp='11.373'/>",
				"</parse>",
				"<bc code='183' bci='22'/>",
				"<method id='734' holder='729' name='leaf3' return='635' arguments='635' flags='2' bytes='6' compile_id='80' compiler='C2' iicount='10013'/>",
				"<call method='734' count='11725' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='734' uses='11725' stamp='11.373'>",
				"<parse_done nodes='180' live='173' memory='47760' stamp='11.374'/>",
				"</parse>",
				"<bc code='183' bci='28'/>",
				"<method id='735' holder='729' name='leaf4' return='635' arguments='635' flags='2' bytes='6' compile_id='81' compiler='C2' iicount='10026'/>",
				"<call method='735' count='11724' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='735' uses='11724' stamp='11.374'>",
				"<parse_done nodes='198' live='190' memory='50896' stamp='11.374'/>",
				"</parse>",
				"<bc code='155' bci='40'/>",
				"<branch target_bci='8' taken='11724' not_taken='0' cnt='11724' prob='always'/>",
				"<bc code='183' bci='52'/>",
				"<klass id='646' name='java/lang/String' flags='17'/>",
				"<klass id='704' name='java/lang/StringBuilder' flags='17'/>",
				"<method id='738' holder='704' name='&lt;init&gt;' return='636' arguments='646' flags='1' bytes='18' iicount='9'/>",
				"<call method='738' count='0' prof_factor='1' inline='1'/>",
				"<inline_fail reason='call site not reached'/>",
				"<direct_call bci='52'/>",
				"<bc code='182' bci='56'/>",
				"<method id='739' holder='704' name='append' return='704' arguments='635' flags='1' bytes='8' iicount='9'/>",
				"<call method='739' count='0' prof_factor='1' inline='1'/>",
				"<inline_fail reason='executed &lt; MinInliningThreshold times'/>",
				"<direct_call bci='56'/>",
				"<bc code='182' bci='59'/>",
				"<method id='740' holder='704' name='toString' return='646' flags='1' bytes='17' iicount='90'/>",
				"<call method='740' count='0' prof_factor='1' inline='1'/>",
				"<inline_fail reason='executed &lt; MinInliningThreshold times'/>",
				"<direct_call bci='59'/>",
				"<uncommon_trap bci='59' reason='null_check' action='maybe_recompile'/>",
				"<bc code='182' bci='62'/>",
				"<klass id='736' name='java/io/PrintStream' flags='1'/>",
				"<method id='741' holder='736' name='println' return='636' arguments='646' flags='1' bytes='24' iicount='9'/>",
				"<dependency type='unique_concrete_method' ctxk='736' x='741'/>",
				"<call method='741' count='0' prof_factor='1' inline='1'/>",
				"<inline_fail reason='executed &lt; MinInliningThreshold times'/>",
				"<direct_call bci='62'/>",
				"<uncommon_trap bci='62' reason='null_check' action='maybe_recompile'/>",
				"<parse_done nodes='337' live='326' memory='78696' stamp='11.375'/>",
				"</parse>",
				"<phase_done name='parse' nodes='340' live='190' stamp='11.376'/>",
				"</phase>",
				"<phase name='optimizer' nodes='340' live='190' stamp='11.376'>",
				"<phase name='idealLoop' nodes='345' live='181' stamp='11.376'>",
				"<loop_tree>",
				"<loop idx='345' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='347' live='181' stamp='11.376'/>",
				"</phase>",
				"<phase name='escapeAnalysis' nodes='347' live='181' stamp='11.376'>",
				"<phase name='connectionGraph' nodes='348' live='182' stamp='11.376'>",
				"<phase_done name='connectionGraph' nodes='348' live='182' stamp='11.378'/>",
				"</phase>",
				"<phase_done name='escapeAnalysis' nodes='348' live='182' stamp='11.378'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='348' live='182' stamp='11.378'>",
				"<loop_tree>",
				"<loop idx='345' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='353' live='179' stamp='11.379'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='353' live='179' stamp='11.379'>",
				"<loop_tree>",
				"<loop idx='345' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='353' live='179' stamp='11.379'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='353' live='179' stamp='11.379'>",
				"<loop_tree>",
				"<loop idx='345' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='353' live='179' stamp='11.379'/>",
				"</phase>",
				"<phase name='ccp' nodes='353' live='179' stamp='11.380'>",
				"<phase_done name='ccp' nodes='353' live='179' stamp='11.380'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='354' live='177' stamp='11.380'>",
				"<loop_tree>",
				"<loop idx='345' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='378' live='171' stamp='11.380'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='378' live='171' stamp='11.380'>",
				"<loop_tree>",
				"<loop idx='345' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='384' live='155' stamp='11.381'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='384' live='155' stamp='11.381'>",
				"<loop_tree>",
				"<loop idx='345' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='390' live='155' stamp='11.381'/>",
				"</phase>",
				"<phase_done name='optimizer' nodes='456' live='193' stamp='11.382'/>",
				"</phase>",
				"<phase name='matcher' nodes='456' live='193' stamp='11.382'>",
				"<phase_done name='matcher' nodes='169' live='169' stamp='11.383'/>",
				"</phase>",
				"<phase name='regalloc' nodes='213' live='213' stamp='11.383'>",
				"<regalloc attempts='1' success='1'/>",
				"<phase_done name='regalloc' nodes='250' live='245' stamp='11.386'/>",
				"</phase>",
				"<phase name='output' nodes='252' live='247' stamp='11.387'>",
				"<phase_done name='output' nodes='272' live='259' stamp='11.387'/>",
				"</phase>",
				"<dependency type='leaf_type' ctxk='729'/>",
				"<dependency type='unique_concrete_method' ctxk='736' x='741'/>",
				"<code_cache total_blobs='262' nmethods='82' adapters='134' free_code_cache='49820416' largest_free_block='49797376'/>",
				"<task_done success='1' nmsize='528' count='10000' backedge_count='5245' inlined_bytes='22' stamp='11.387'/>",
				"</task>" };

		String[] bytecodeLines = new String[] {
				"0: lconst_0        ",
				"1: lstore_3        ",
				"2: iconst_0        ",
				"3: istore          5    ",
				"5: goto            35   ",
				"8: aload_0         ",
				"9: lload_3         ",
				"10: invokespecial   #225 // Method leaf1:(J)J",
				"13: lstore_3        ",
				"14: aload_0         ",
				"15: lload_3         ",
				"16: invokespecial   #228 // Method leaf2:(J)J",
				"19: lstore_3        ",
				"20: aload_0         ",
				"21: lload_3         ",
				"22: invokespecial   #231 // Method leaf3:(J)J",
				"25: lstore_3        ",
				"26: aload_0         ",
				"27: lload_3         ",
				"28: invokespecial   #234 // Method leaf4:(J)J",
				"31: lstore_3        ",
				"32: iinc            5, 1 ",
				"35: iload           5    ",
				"37: i2l             ",
				"38: lload_1         ",
				"39: lcmp            ",
				"40: iflt            8    ",
				"43: getstatic       #52  // Field java/lang/System.out:Ljava/io/PrintStream;",
				"46: new             #58  // class java/lang/StringBuilder",
				"49: dup             ",
				"50: ldc             #237 // String testLeaf:",
				"52: invokespecial   #62  // Method java/lang/StringBuilder.\"<init>\":(Ljava/lang/String;)V",
				"55: lload_3         ",
				"56: invokevirtual   #65  // Method java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;",
				"59: invokevirtual   #69  // Method java/lang/StringBuilder.toString:()Ljava/lang/String;",
				"62: invokevirtual   #73  // Method java/io/PrintStream.println:(Ljava/lang/String;)V",
				"65: return          " };

		IMetaMember member = UnitTestUtil.createTestMetaMember("org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog", "testLeaf",
				new Class[] {
						long.class },
				void.class);

		BytecodeAnnotations result = buildAnnotations("1.8.0", member, logLines, bytecodeLines);

		assertEquals(10, result.annotatedLineCount());

		checkLine(result, 10, "inline (hot)", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 16, "inline (hot)", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 22, "inline (hot)", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 28, "inline (hot)", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 40, "always", BCAnnotationType.BRANCH);
		checkLine(result, 52, "not reached", BCAnnotationType.INLINE_FAIL);
		checkLine(result, 56, "MinInliningThreshold", BCAnnotationType.INLINE_FAIL);
		checkLine(result, 59, "MinInliningThreshold", BCAnnotationType.INLINE_FAIL);
		checkLine(result, 62, "MinInliningThreshold", BCAnnotationType.INLINE_FAIL);
		checkLine(result, 62, "null_check", BCAnnotationType.UNCOMMON_TRAP);

	}

	@Test
	public void testJava7NonTieredChain()
	{
		String[] logLines = new String[] {
				"<task compile_id='73' compile_kind='osr' method='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog testCallChain (J)V' bytes='54' count='10000' backedge_count='5215' iicount='1' osr_bci='8' stamp='11.237'>",
				"<phase name='parse' nodes='3' live='3' stamp='11.237'>",
				"<type id='636' name='void'/>",
				"<type id='635' name='long'/>",
				"<klass id='729' name='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog' flags='1'/>",
				"<method id='730' holder='729' name='testCallChain' return='636' arguments='635' flags='2' bytes='54' iicount='1'/>",
				"<parse method='730' uses='1' osr_bci='8' stamp='11.238'>",
				"<dependency type='leaf_type' ctxk='729'/>",
				"<dependency type='leaf_type' ctxk='729'/>",
				"<uncommon_trap bci='8' reason='constraint' action='reinterpret'/>",
				"<uncommon_trap bci='8' reason='predicate' action='maybe_recompile'/>",
				"<uncommon_trap bci='8' reason='loop_limit_check' action='maybe_recompile'/>",
				"<bc code='183' bci='10'/>",
				"<method id='732' holder='729' name='chainA1' return='635' arguments='635' flags='2' bytes='8' compile_id='66' compiler='C2' iicount='10036'/>",
				"<call method='732' count='12009' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='732' uses='12009' stamp='11.239'>",
				"<uncommon_trap bci='10' reason='null_check' action='maybe_recompile'/>",
				"<bc code='183' bci='3'/>",
				"<method id='740' holder='729' name='chainA2' return='635' arguments='635' flags='2' bytes='10' compile_id='67' compiler='C2' iicount='10036'/>",
				"<call method='740' count='6737' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='740' uses='6737' stamp='11.239'>",
				"<bc code='183' bci='5'/>",
				"<method id='742' holder='729' name='chainA3' return='635' arguments='635' flags='2' bytes='10' compile_id='68' compiler='C2' iicount='10036'/>",
				"<call method='742' count='6737' prof_factor='0.671283' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='742' uses='4522' stamp='11.239'>",
				"<bc code='183' bci='5'/>",
				"<method id='744' holder='729' name='chainA4' return='635' arguments='635' flags='2' bytes='7' compile_id='69' compiler='C2' iicount='10036'/>",
				"<call method='744' count='6737' prof_factor='0.450578' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='744' uses='3036' stamp='11.239'>",
				"<bc code='183' bci='3'/>",
				"<type id='634' name='int'/>",
				"<method id='746' holder='729' name='bigMethod' return='635' arguments='635 634' flags='2' bytes='350' compile_id='16' compiler='C2' iicount='11219'/>",
				"<call method='746' count='6737' prof_factor='0.302511' inline='1'/>",
				"<inline_fail reason='hot method too big'/>",
				"<direct_call bci='3'/>",
				"<parse_done nodes='191' live='186' memory='50504' stamp='11.240'/>",
				"</parse>",
				"<parse_done nodes='194' live='188' memory='51680' stamp='11.240'/>",
				"</parse>",
				"<parse_done nodes='198' live='191' memory='53080' stamp='11.240'/>",
				"</parse>",
				"<parse_done nodes='202' live='194' memory='55040' stamp='11.240'/>",
				"</parse>",
				"<bc code='183' bci='16'/>",
				"<method id='733' holder='729' name='chainB1' return='635' arguments='635' flags='2' bytes='8' compile_id='70' compiler='C2' iicount='11154'/>",
				"<call method='733' count='12008' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='733' uses='12008' stamp='11.240'>",
				"<bc code='183' bci='2'/>",
				"<method id='748' holder='729' name='chainB2' return='635' arguments='635' flags='2' bytes='10' compile_id='71' compiler='C2' iicount='11154'/>",
				"<call method='748' count='7855' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='748' uses='7855' stamp='11.240'>",
				"<bc code='183' bci='2'/>",
				"<method id='750' holder='729' name='chainB3' return='635' arguments='635' flags='2' bytes='6' compile_id='72' compiler='C2' iicount='11154'/>",
				"<call method='750' count='7855' prof_factor='0.704232' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='750' uses='5532' stamp='11.240'>",
				"<parse_done nodes='243' live='234' memory='62248' stamp='11.240'/>",
				"</parse>",
				"<parse_done nodes='246' live='236' memory='62840' stamp='11.240'/>",
				"</parse>",
				"<parse_done nodes='249' live='238' memory='63336' stamp='11.240'/>",
				"</parse>",
				"<bc code='155' bci='28'/>",
				"<branch target_bci='8' taken='12008' not_taken='0' cnt='12008' prob='always'/>",
				"<bc code='183' bci='40'/>",
				"<klass id='646' name='java/lang/String' flags='17'/>",
				"<klass id='704' name='java/lang/StringBuilder' flags='17'/>",
				"<method id='736' holder='704' name='&lt;init&gt;' return='636' arguments='646' flags='1' bytes='18' iicount='7'/>",
				"<call method='736' count='0' prof_factor='1' inline='1'/>",
				"<inline_fail reason='call site not reached'/>",
				"<direct_call bci='40'/>",
				"<bc code='182' bci='44'/>",
				"<method id='737' holder='704' name='append' return='704' arguments='635' flags='1' bytes='8' iicount='7'/>",
				"<call method='737' count='0' prof_factor='1' inline='1'/>",
				"<inline_fail reason='executed &lt; MinInliningThreshold times'/>",
				"<direct_call bci='44'/>",
				"<bc code='182' bci='47'/>",
				"<method id='738' holder='704' name='toString' return='646' flags='1' bytes='17' iicount='88'/>",
				"<call method='738' count='0' prof_factor='1' inline='1'/>",
				"<inline_fail reason='executed &lt; MinInliningThreshold times'/>",
				"<direct_call bci='47'/>",
				"<uncommon_trap bci='47' reason='null_check' action='maybe_recompile'/>",
				"<bc code='182' bci='50'/>",
				"<klass id='734' name='java/io/PrintStream' flags='1'/>",
				"<method id='739' holder='734' name='println' return='636' arguments='646' flags='1' bytes='24' iicount='7'/>",
				"<dependency type='unique_concrete_method' ctxk='734' x='739'/>",
				"<call method='739' count='0' prof_factor='1' inline='1'/>",
				"<inline_fail reason='executed &lt; MinInliningThreshold times'/>",
				"<direct_call bci='50'/>",
				"<uncommon_trap bci='50' reason='null_check' action='maybe_recompile'/>",
				"<parse_done nodes='386' live='372' memory='92680' stamp='11.242'/>",
				"</parse>",
				"<phase_done name='parse' nodes='389' live='200' stamp='11.242'/>",
				"</phase>",
				"<phase name='optimizer' nodes='389' live='200' stamp='11.242'>",
				"<phase name='idealLoop' nodes='394' live='194' stamp='11.243'>",
				"<loop_tree>",
				"<loop idx='394' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='396' live='194' stamp='11.243'/>",
				"</phase>",
				"<phase name='escapeAnalysis' nodes='396' live='194' stamp='11.243'>",
				"<phase name='connectionGraph' nodes='397' live='195' stamp='11.243'>",
				"<phase_done name='connectionGraph' nodes='397' live='195' stamp='11.245'/>",
				"</phase>",
				"<phase_done name='escapeAnalysis' nodes='397' live='195' stamp='11.245'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='397' live='195' stamp='11.245'>",
				"<loop_tree>",
				"<loop idx='394' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='402' live='192' stamp='11.246'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='402' live='192' stamp='11.246'>",
				"<loop_tree>",
				"<loop idx='394' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='402' live='192' stamp='11.246'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='402' live='192' stamp='11.246'>",
				"<loop_tree>",
				"<loop idx='394' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='402' live='192' stamp='11.246'/>",
				"</phase>",
				"<phase name='ccp' nodes='402' live='192' stamp='11.247'>",
				"<phase_done name='ccp' nodes='402' live='192' stamp='11.247'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='403' live='189' stamp='11.247'>",
				"<loop_tree>",
				"<loop idx='394' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='421' live='183' stamp='11.248'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='421' live='183' stamp='11.248'>",
				"<loop_tree>",
				"<loop idx='394' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='421' live='167' stamp='11.248'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='421' live='167' stamp='11.248'>",
				"<loop_tree>",
				"<loop idx='394' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='421' live='167' stamp='11.249'/>",
				"</phase>",
				"<phase_done name='optimizer' nodes='484' live='202' stamp='11.249'/>",
				"</phase>",
				"<phase name='matcher' nodes='484' live='202' stamp='11.249'>",
				"<phase_done name='matcher' nodes='181' live='181' stamp='11.250'/>",
				"</phase>",
				"<phase name='regalloc' nodes='229' live='229' stamp='11.251'>",
				"<regalloc attempts='1' success='1'/>",
				"<phase_done name='regalloc' nodes='279' live='266' stamp='11.254'/>",
				"</phase>",
				"<phase name='output' nodes='281' live='268' stamp='11.254'>",
				"<phase_done name='output' nodes='308' live='285' stamp='11.255'/>",
				"</phase>",
				"<dependency type='leaf_type' ctxk='729'/>",
				"<dependency type='unique_concrete_method' ctxk='734' x='739'/>",
				"<code_cache total_blobs='253' nmethods='73' adapters='134' free_code_cache='49827328' largest_free_block='49803328'/>",
				"<task_done success='1' nmsize='576' count='10000' backedge_count='5296' inlined_bytes='59' stamp='11.256'/>",
				"</task>" };

		String[] bytecodeLines = new String[] {
				"0: lconst_0        ",
				"1: lstore_3        ",
				"2: iconst_0        ",
				"3: istore          5    ",
				"5: goto            23   ",
				"8: aload_0         ",
				"9: lload_3         ",
				"10: invokespecial   #190 // Method chainA1:(J)J",
				"13: lstore_3        ",
				"14: aload_0         ",
				"15: lload_3         ",
				"16: invokespecial   #194 // Method chainB1:(J)J",
				"19: lstore_3        ",
				"20: iinc            5, 1 ",
				"23: iload           5    ",
				"25: i2l             ",
				"26: lload_1         ",
				"27: lcmp            ",
				"28: iflt            8    ",
				"31: getstatic       #52  // Field java/lang/System.out:Ljava/io/PrintStream;",
				"34: new             #58  // class java/lang/StringBuilder",
				"37: dup             ",
				"38: ldc             #197 // String testCallChain:",
				"40: invokespecial   #62  // Method java/lang/StringBuilder.\"<init>\":(Ljava/lang/String;)V",
				"43: lload_3         ",
				"44: invokevirtual   #65  // Method java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;",
				"47: invokevirtual   #69  // Method java/lang/StringBuilder.toString:()Ljava/lang/String;",
				"50: invokevirtual   #73  // Method java/io/PrintStream.println:(Ljava/lang/String;)V",
				"53: return          " };

		IMetaMember member = UnitTestUtil.createTestMetaMember("org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog", "testCallChain",
				new Class[] {
						long.class },
				void.class);

		BytecodeAnnotations result = buildAnnotations("1.8.0", member, logLines, bytecodeLines);

		assertEquals(8, result.annotatedLineCount());

		checkLine(result, 10, "inline (hot)", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 16, "inline (hot)", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 28, "always", BCAnnotationType.BRANCH);
		checkLine(result, 40, "not reached", BCAnnotationType.INLINE_FAIL);
		checkLine(result, 44, "MinInliningThreshold", BCAnnotationType.INLINE_FAIL);
		checkLine(result, 47, "MinInliningThreshold", BCAnnotationType.INLINE_FAIL);
		checkLine(result, 47, "null_check", BCAnnotationType.UNCOMMON_TRAP);
		checkLine(result, 50, "MinInliningThreshold", BCAnnotationType.INLINE_FAIL);
	}

	@Test
	public void testJDK9BytecodeAnnotations()
	{
		String[] logLines = new String[] {
				"<task compiler='C1' compile_id='881' compile_kind='osr' method='org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog testCallChain3 ()V' bytes='71' count='1' backedge_count='60529' iicount='1' osr_bci='8' level='3' stamp='18.121'>",
				"<phase name='setup' stamp='18.121'>",
				"<phase_done name='setup' stamp='18.121'/>",
				"</phase>",
				"<phase name='buildIR' stamp='18.121'>",
				"<type id='758' name='void'/>",
				"<klass id='868' name='org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog' flags='1'/>",
				"<method id='869' holder='868' name='testCallChain3' return='758' flags='2' bytes='71' iicount='1'/>",
				"<parse method='869'  stamp='18.121'>",
				"<phase name='parse_hir' stamp='18.121'>",
				"<bc code='183' bci='18'/>",
				"<type id='750' name='boolean'/>",
				"<type id='756' name='int'/>",
				"<method id='871' holder='868' name='test' return='750' arguments='756 756' flags='2' bytes='18' compile_id='879' compiler='C2' level='4' iicount='32412'/>",
				"<call method='871' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<parse method='871'>",
				"<parse_done stamp='18.121'/>",
				"</parse>",
				"<bc code='183' bci='26'/>",
				"<type id='757' name='long'/>",
				"<method id='873' holder='868' name='chainC1' return='757' arguments='757' flags='2' bytes='16' compile_id='880' compiler='C2' level='4' iicount='36824'/>",
				"<call method='873' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<parse method='873'>",
				"<bc code='183' bci='5'/>",
				"<method id='875' holder='868' name='chainC2' return='757' arguments='757' flags='2' bytes='6' compile_id='878' compiler='C1' level='3' iicount='36824'/>",
				"<call method='875' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<parse method='875'>",
				"<parse_done stamp='18.122'/>",
				"</parse>",
				"<bc code='183' bci='12'/>",
				"<method id='877' holder='868' name='chainC3' return='757' arguments='757' flags='2' bytes='6' compile_id='877' compiler='C1' level='3' iicount='36824'/>",
				"<call method='877' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<parse method='877'>",
				"<parse_done stamp='18.122'/>",
				"</parse>",
				"<parse_done stamp='18.122'/>",
				"</parse>",
				"<bc code='183' bci='35'/>",
				"<call method='875' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<parse method='875'>",
				"<parse_done stamp='18.122'/>",
				"</parse>",
				"<bc code='183' bci='52'/>",
				"<klass id='831' name='java.lang.StringBuilder' flags='17'/>",
				"<method id='881' holder='831' name='&lt;init&gt;' return='758' flags='1' bytes='7' compile_id='157' compiler='C1' level='3' iicount='3350'/>",
				"<call method='881' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<parse method='881'>",
				"<bc code='183' bci='3'/>",
				"<klass id='829' name='java.lang.AbstractStringBuilder' flags='1024'/>",
				"<method id='883' holder='829' name='&lt;init&gt;' return='758' arguments='756' flags='0' bytes='12' compile_id='127' compiler='C1' level='3' iicount='4382'/>",
				"<call method='883' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<parse method='883'>",
				"<bc code='183' bci='1'/>",
				"<klass id='763' name='java.lang.Object' flags='1'/>",
				"<method id='885' holder='763' name='&lt;init&gt;' return='758' flags='1' bytes='1' compile_id='9' compiler='C1' level='1' iicount='406725'/>",
				"<call method='885' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<parse method='885'>",
				"<parse_done stamp='18.123'/>",
				"</parse>",
				"<parse_done stamp='18.123'/>",
				"</parse>",
				"<parse_done stamp='18.123'/>",
				"</parse>",
				"<bc code='182' bci='57'/>",
				"<klass id='764' name='java.lang.String' flags='17'/>",
				"<method id='888' holder='831' name='append' return='831' arguments='764' flags='1' bytes='8' compile_id='481' compiler='C2' level='4' iicount='7576'/>",
				"<call method='888' instr='invokevirtual'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<parse method='888'>",
				"<bc code='183' bci='2'/>",
				"<method id='890' holder='829' name='append' return='829' arguments='764' flags='1' bytes='50' compile_id='480' compiler='C2' level='4' iicount='5332'/>",
				"<call method='890' instr='invokespecial'/>",
				"<inline_fail reason='callee is too large'/>",
				"<parse_done stamp='18.123'/>",
				"</parse>",
				"<bc code='182' bci='61'/>",
				"<method id='892' holder='831' name='append' return='831' arguments='757' flags='1' bytes='8' iicount='12'/>",
				"<call method='892' instr='invokevirtual'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<parse method='892'>",
				"<bc code='183' bci='2'/>",
				"<method id='894' holder='829' name='append' return='829' arguments='757' flags='1' bytes='70' iicount='12'/>",
				"<call method='894' instr='invokespecial'/>",
				"<inline_fail reason='callee is too large'/>",
				"<parse_done stamp='18.123'/>",
				"</parse>",
				"<bc code='182' bci='64'/>",
				"<method id='896' holder='831' name='toString' return='764' flags='1' bytes='17' compile_id='164' compiler='C1' level='3' iicount='3924'/>",
				"<call method='896' instr='invokevirtual'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<parse method='896'>",
				"<bc code='183' bci='13'/>",
				"<klass id='857' name='[C' flags='1041'/>",
				"<method id='898' holder='764' name='&lt;init&gt;' return='758' arguments='857 756 756' flags='1' bytes='82' compile_id='431' compiler='C1' level='3' iicount='12354'/>",
				"<call method='898' instr='invokespecial'/>",
				"<inline_fail reason='callee is too large'/>",
				"<parse_done stamp='18.124'/>",
				"</parse>",
				"<bc code='182' bci='67'/>",
				"<klass id='879' name='java.io.PrintStream' flags='1'/>",
				"<method id='900' holder='879' name='println' return='758' arguments='764' flags='1' bytes='24' iicount='11'/>",
				"<call method='900' instr='invokevirtual'/>",
				"<dependency type='leaf_type' ctxk='879'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<dependency type='leaf_type' ctxk='879'/>",
				"<parse method='900'>",
				"<bc code='182' bci='6'/>",
				"<method id='902' holder='879' name='print' return='758' arguments='764' flags='1' bytes='13' iicount='11'/>",
				"<call method='902' instr='invokevirtual'/>",
				"<dependency type='leaf_type' ctxk='879'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<dependency type='leaf_type' ctxk='879'/>",
				"<parse method='902'>",
				"<bc code='183' bci='9'/>",
				"<method id='904' holder='879' name='write' return='758' arguments='764' flags='2' bytes='83' iicount='11'/>",
				"<call method='904' instr='invokespecial'/>",
				"<dependency type='leaf_type' ctxk='879'/>",
				"<inline_fail reason='callee is too large'/>",
				"<parse_done stamp='18.124'/>",
				"</parse>",
				"<bc code='183' bci='10'/>",
				"<method id='908' holder='879' name='newLine' return='758' flags='2' bytes='73' iicount='11'/>",
				"<call method='908' instr='invokespecial'/>",
				"<dependency type='leaf_type' ctxk='879'/>",
				"<inline_fail reason='callee is too large'/>",
				"<parse_done stamp='18.124'/>",
				"</parse>",
				"<phase_done name='parse_hir' stamp='18.124'/>",
				"</phase>",
				"<parse_done stamp='18.124'/>",
				"</parse>",
				"<phase name='optimize_blocks' stamp='18.124'>",
				"<phase_done name='optimize_blocks' stamp='18.124'/>",
				"</phase>",
				"<phase name='gvn' stamp='18.124'>",
				"<phase_done name='gvn' stamp='18.125'/>",
				"</phase>",
				"<phase name='optimize_null_checks' stamp='18.125'>",
				"<phase_done name='optimize_null_checks' stamp='18.125'/>",
				"</phase>",
				"<phase_done name='buildIR' stamp='18.125'/>",
				"</phase>",
				"<phase name='emit_lir' stamp='18.125'>",
				"<phase name='lirGeneration' stamp='18.125'>",
				"<phase_done name='lirGeneration' stamp='18.125'/>",
				"</phase>",
				"<phase name='linearScan' stamp='18.125'>",
				"<phase_done name='linearScan' stamp='18.126'/>",
				"</phase>",
				"<phase_done name='emit_lir' stamp='18.126'/>",
				"</phase>",
				"<phase name='codeemit' stamp='18.126'>",
				"<phase_done name='codeemit' stamp='18.127'/>",
				"</phase>",
				"<phase name='codeinstall' stamp='18.127'>",
				"<dependency type='leaf_type' ctxk='879'/>",
				"<phase_done name='codeinstall' stamp='18.127'/>",
				"</phase>",
				"<code_cache total_blobs='1176' nmethods='854' adapters='236' free_code_cache='248442752'/>",
				"<task_done success='1' nmsize='3784' count='1' backedge_count='75645' inlined_bytes='142' stamp='18.127'/>",
				"</task>" };

		String[] bytecodeLines = new String[] {
				"         0: lconst_0",
				"         1: lstore_1",
				"         2: ldc           #81                 // int 100000",
				"         4: istore_3",
				"         5: iconst_0",
				"         6: istore        4",
				"         8: iload         4",
				"        10: iload_3",
				"        11: if_icmpge     45",
				"        14: aload_0",
				"        15: iload         4",
				"        17: iload_3",
				"        18: invokespecial #82                 // Method test:(II)Z",
				"        21: ifeq          33",
				"        24: aload_0",
				"        25: lload_1",
				"        26: invokespecial #83                 // Method chainC1:(J)J",
				"        29: lstore_1",
				"        30: goto          39",
				"        33: aload_0",
				"        34: lload_1",
				"        35: invokespecial #84                 // Method chainC2:(J)J",
				"        38: lstore_1",
				"        39: iinc          4, 1",
				"        42: goto          8",
				"        45: getstatic     #13                 // Field java/lang/System.out:Ljava/io/PrintStream;",
				"        48: new           #14                 // class java/lang/StringBuilder",
				"        51: dup",
				"        52: invokespecial #15                 // Method java/lang/StringBuilder.\"<init>\":()V",
				"        55: ldc           #85                 // String testCallChain3:",
				"        57: invokevirtual #17                 // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;",
				"        60: lload_1",
				"        61: invokevirtual #18                 // Method java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;",
				"        64: invokevirtual #19                 // Method java/lang/StringBuilder.toString:()Ljava/lang/String;",
				"        67: invokevirtual #20                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V",
				"        70: return" };

		IMetaMember member = UnitTestUtil.createTestMetaMember("org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog", "testCallChain3",
				new Class[0], void.class);

		BytecodeAnnotations result = buildAnnotations("1.8.0", member, logLines, bytecodeLines);

		assertEquals(8, result.annotatedLineCount());

		checkLine(result, 64, "Inlined: Yes", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 18, "Inlined: Yes", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 35, "Inlined: Yes", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 67, "Inlined: Yes", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 52, "Inlined: Yes", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 57, "Inlined: Yes", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 26, "Inlined: Yes", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 61, "Inlined: Yes", BCAnnotationType.INLINE_SUCCESS);
	}

	@Test
	public void testJava8NonTieredLeaf()
	{
		String[] logLines = new String[] {
				"<task compile_id='78' compile_kind='osr' method='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog testLeaf (J)V' bytes='69' count='10000' backedge_count='5059' iicount='1' osr_bci='5' stamp='11.551'>",
				"<phase name='parse' nodes='3' live='3' stamp='11.551'>",
				"<type id='680' name='void'/>",
				"<type id='679' name='long'/>",
				"<klass id='776' name='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog' flags='1'/>",
				"<method id='777' holder='776' name='testLeaf' return='680' arguments='679' flags='2' bytes='69' iicount='1'/>",
				"<parse method='777' uses='1' osr_bci='5' stamp='11.552'>",
				"<dependency type='leaf_type' ctxk='776'/>",
				"<dependency type='leaf_type' ctxk='776'/>",
				"<uncommon_trap bci='5' reason='constraint' action='reinterpret'/>",
				"<uncommon_trap bci='5' reason='predicate' action='maybe_recompile'/>",
				"<uncommon_trap bci='5' reason='loop_limit_check' action='maybe_recompile'/>",
				"<bc code='156' bci='10'/>",
				"<branch target_bci='43' taken='0' not_taken='11547' cnt='11547' prob='never'/>",
				"<bc code='183' bci='15'/>",
				"<method id='786' holder='776' name='leaf1' return='679' arguments='679' flags='2' bytes='4' compile_id='74' compiler='C2' iicount='10805'/>",
				"<call method='786' count='11547' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='786' uses='11547' stamp='11.553'>",
				"<uncommon_trap bci='15' reason='null_check' action='maybe_recompile'/>",
				"<parse_done nodes='155' live='150' memory='43768' stamp='11.553'/>",
				"</parse>",
				"<bc code='183' bci='21'/>",
				"<method id='787' holder='776' name='leaf2' return='679' arguments='679' flags='2' bytes='6' compile_id='75' compiler='C2' iicount='11803'/>",
				"<call method='787' count='11546' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='787' uses='11546' stamp='11.553'>",
				"<parse_done nodes='173' live='167' memory='46304' stamp='11.553'/>",
				"</parse>",
				"<bc code='183' bci='27'/>",
				"<method id='788' holder='776' name='leaf3' return='679' arguments='679' flags='2' bytes='6' compile_id='76' compiler='C2' iicount='13042'/>",
				"<call method='788' count='11546' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='788' uses='11546' stamp='11.553'>",
				"<parse_done nodes='190' live='183' memory='49576' stamp='11.555'/>",
				"</parse>",
				"<bc code='183' bci='33'/>",
				"<method id='789' holder='776' name='leaf4' return='679' arguments='679' flags='2' bytes='6' compile_id='77' compiler='C2' iicount='15225'/>",
				"<call method='789' count='11546' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='789' uses='11546' stamp='11.555'>",
				"<parse_done nodes='208' live='200' memory='52048' stamp='11.555'/>",
				"</parse>",
				"<bc code='183' bci='50'/>",
				"<klass id='749' name='java/lang/StringBuilder' flags='17'/>",
				"<method id='780' holder='749' name='&lt;init&gt;' return='680' flags='1' bytes='7' iicount='114'/>",
				"<call method='780' count='0' prof_factor='1' inline='1'/>",
				"<inline_fail reason='call site not reached'/>",
				"<direct_call bci='50'/>",
				"<bc code='182' bci='55'/>",
				"<klass id='686' name='java/lang/String' flags='17'/>",
				"<method id='782' holder='749' name='append' return='749' arguments='686' flags='1' bytes='8' iicount='209'/>",
				"<call method='782' count='0' prof_factor='1' inline='1'/>",
				"<inline_fail reason='executed &lt; MinInliningThreshold times'/>",
				"<direct_call bci='55'/>",
				"<bc code='182' bci='59'/>",
				"<method id='783' holder='749' name='append' return='749' arguments='679' flags='1' bytes='8' iicount='9'/>",
				"<call method='783' count='0' prof_factor='1' inline='1'/>",
				"<inline_fail reason='executed &lt; MinInliningThreshold times'/>",
				"<direct_call bci='59'/>",
				"<uncommon_trap bci='59' reason='null_check' action='maybe_recompile'/>",
				"<bc code='182' bci='62'/>",
				"<method id='784' holder='749' name='toString' return='686' flags='1' bytes='17' iicount='113'/>",
				"<call method='784' count='0' prof_factor='1' inline='1'/>",
				"<inline_fail reason='executed &lt; MinInliningThreshold times'/>",
				"<direct_call bci='62'/>",
				"<uncommon_trap bci='62' reason='null_check' action='maybe_recompile'/>",
				"<bc code='182' bci='65'/>",
				"<klass id='779' name='java/io/PrintStream' flags='1'/>",
				"<method id='785' holder='779' name='println' return='680' arguments='686' flags='1' bytes='24' iicount='9'/>",
				"<dependency type='unique_concrete_method' ctxk='779' x='785'/>",
				"<call method='785' count='0' prof_factor='1' inline='1'/>",
				"<inline_fail reason='executed &lt; MinInliningThreshold times'/>",
				"<direct_call bci='65'/>",
				"<uncommon_trap bci='65' reason='null_check' action='maybe_recompile'/>",
				"<parse_done nodes='365' live='353' memory='83024' stamp='11.557'/>",
				"</parse>",
				"<phase_done name='parse' nodes='368' live='210' stamp='11.557'/>",
				"</phase>",
				"<phase name='optimizer' nodes='368' live='210' stamp='11.557'>",
				"<phase name='idealLoop' nodes='373' live='201' stamp='11.557'>",
				"<loop_tree>",
				"<loop idx='373' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='374' live='201' stamp='11.558'/>",
				"</phase>",
				"<phase name='escapeAnalysis' nodes='374' live='201' stamp='11.558'>",
				"<phase name='connectionGraph' nodes='375' live='202' stamp='11.558'>",
				"<klass id='747' name='java/lang/AbstractStringBuilder' flags='1024'/>",
				"<type id='678' name='int'/>",
				"<method id='808' holder='747' name='expandCapacity' return='680' arguments='678' flags='0' bytes='50' iicount='157'/>",
				"<dependency type='unique_concrete_method' ctxk='747' x='808'/>",
				"<phase_done name='connectionGraph' nodes='375' live='202' stamp='11.560'/>",
				"</phase>",
				"<phase_done name='escapeAnalysis' nodes='375' live='202' stamp='11.560'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='375' live='202' stamp='11.560'>",
				"<loop_tree>",
				"<loop idx='373' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='399' live='213' stamp='11.561'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='399' live='213' stamp='11.561'>",
				"<loop_tree>",
				"<loop idx='373' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='399' live='194' stamp='11.561'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='399' live='194' stamp='11.561'>",
				"<loop_tree>",
				"<loop idx='373' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='399' live='194' stamp='11.562'/>",
				"</phase>",
				"<phase name='ccp' nodes='399' live='194' stamp='11.562'>",
				"<phase_done name='ccp' nodes='399' live='194' stamp='11.562'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='400' live='191' stamp='11.562'>",
				"<loop_tree>",
				"<loop idx='373' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='400' live='191' stamp='11.563'/>",
				"</phase>",
				"<phase_done name='optimizer' nodes='468' live='229' stamp='11.563'/>",
				"</phase>",
				"<phase name='matcher' nodes='468' live='229' stamp='11.563'>",
				"<phase_done name='matcher' nodes='210' live='210' stamp='11.564'/>",
				"</phase>",
				"<phase name='regalloc' nodes='268' live='268' stamp='11.565'>",
				"<regalloc attempts='1' success='1'/>",
				"<phase_done name='regalloc' nodes='302' live='300' stamp='11.569'/>",
				"</phase>",
				"<phase name='output' nodes='304' live='302' stamp='11.569'>",
				"<phase_done name='output' nodes='331' live='319' stamp='11.570'/>",
				"</phase>",
				"<dependency type='leaf_type' ctxk='776'/>",
				"<dependency type='unique_concrete_method' ctxk='779' x='785'/>",
				"<dependency type='unique_concrete_method' ctxk='747' x='808'/>",
				"<code_cache total_blobs='268' nmethods='78' adapters='142' free_code_cache='49725632'/>",
				"<task_done success='1' nmsize='608' count='10000' backedge_count='5598' inlined_bytes='22' stamp='11.570'/>",
				"</task>" };

		String[] bytecodeLines = new String[] {
				"0: lconst_0        ",
				"1: lstore_3        ",
				"2: iconst_0        ",
				"3: istore          5    ",
				"5: iload           5    ",
				"7: i2l             ",
				"8: lload_1         ",
				"9: lcmp            ",
				"10: ifge            43   ",
				"13: aload_0         ",
				"14: lload_3         ",
				"15: invokespecial   #70  // Method leaf1:(J)J",
				"18: lstore_3        ",
				"19: aload_0         ",
				"20: lload_3         ",
				"21: invokespecial   #71  // Method leaf2:(J)J",
				"24: lstore_3        ",
				"25: aload_0         ",
				"26: lload_3         ",
				"27: invokespecial   #72  // Method leaf3:(J)J",
				"30: lstore_3        ",
				"31: aload_0         ",
				"32: lload_3         ",
				"33: invokespecial   #73  // Method leaf4:(J)J",
				"36: lstore_3        ",
				"37: iinc            5, 1 ",
				"40: goto            5    ",
				"43: getstatic       #13  // Field java/lang/System.out:Ljava/io/PrintStream;",
				"46: new             #14  // class java/lang/StringBuilder",
				"49: dup             ",
				"50: invokespecial   #15  // Method java/lang/StringBuilder.\"<init>\":()V",
				"53: ldc             #74  // String testLeaf:",
				"55: invokevirtual   #17  // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;",
				"58: lload_3         ",
				"59: invokevirtual   #18  // Method java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;",
				"62: invokevirtual   #19  // Method java/lang/StringBuilder.toString:()Ljava/lang/String;",
				"65: invokevirtual   #20  // Method java/io/PrintStream.println:(Ljava/lang/String;)V",
				"68: return          " };

		IMetaMember member = UnitTestUtil.createTestMetaMember("org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog", "testLeaf",
				new Class[] {
						long.class },
				void.class);

		BytecodeAnnotations result = buildAnnotations("1.8.0", member, logLines, bytecodeLines);

		assertEquals(11, result.annotatedLineCount());

		checkLine(result, 10, "never", BCAnnotationType.BRANCH);
		checkLine(result, 15, "inline (hot)", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 21, "inline (hot)", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 27, "inline (hot)", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 33, "inline (hot)", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 50, "not reached", BCAnnotationType.INLINE_FAIL);
		checkLine(result, 55, "MinInliningThreshold", BCAnnotationType.INLINE_FAIL);
		checkLine(result, 59, "MinInliningThreshold", BCAnnotationType.INLINE_FAIL);
		checkLine(result, 62, "MinInliningThreshold", BCAnnotationType.INLINE_FAIL);
		checkLine(result, 65, "MinInliningThreshold", BCAnnotationType.INLINE_FAIL);
		checkLine(result, 65, "null_check", BCAnnotationType.UNCOMMON_TRAP);
	}

	private BytecodeAnnotations buildAnnotations(String vmVersion, IMetaMember member, String[] logLines,
			String[] bytecodeLines)
	{
		TagProcessor tp = new TagProcessor();

		int count = 0;

		Tag tag = null;

		for (String line : logLines)
		{
			line = line.trim();

			line = StringUtil.replaceXMLEntities(line);

			tag = tp.processLine(line);

			if (count++ < logLines.length - 1)
			{
				assertNull(tag);
			}
		}

		assertNotNull(tag);

		member.addJournalEntry(tag);

		// marks member as compiled
		member.setCompiledAttributes(new HashMap<String, String>());

		StringBuilder bytecodeBuilder = new StringBuilder();

		for (String bcLine : bytecodeLines)
		{
			bytecodeBuilder.append(bcLine.trim()).append(S_NEWLINE);
		}

		List<BytecodeInstruction> instructions = BytecodeLoader.parseInstructions(bytecodeBuilder.toString());

		((HelperMetaMethod) member).setInstructions(instructions);

		BytecodeAnnotations bcAnnotations = null;

		JITDataModel model = new JITDataModel();
		model.setVmVersionRelease(vmVersion);

		try
		{
			bcAnnotations = new BytecodeAnnotationBuilder().buildBytecodeAnnotations(member, model);
		}
		catch (AnnotationException annoEx)
		{
			annoEx.printStackTrace();

			fail();
		}

		return bcAnnotations;
	}

	@Test
	public void testMemberMatchesParseTag()
	{
		String methodName = "length";
		String klassName = "java.lang.String";

		Map<String, String> attrsMethod = new HashMap<>();
		Map<String, String> attrsKlass = new HashMap<>();
		Map<String, String> attrsParse = new HashMap<>();
		Map<String, String> attrsTypeInt = new HashMap<>();

		String idInt = "1";
		String nameInt = "int";

		attrsTypeInt.put(ATTR_ID, idInt);
		attrsTypeInt.put(ATTR_NAME, nameInt);

		Tag tagTypeInt = new Tag(TAG_TYPE, attrsTypeInt, true);

		String methodID = "123";
		String klassID = "456";

		attrsMethod.put(ATTR_NAME, methodName);
		attrsMethod.put(ATTR_ID, methodID);
		attrsMethod.put(ATTR_HOLDER, klassID);
		attrsMethod.put(ATTR_RETURN, idInt);

		Tag tagMethod = new Tag(TAG_METHOD, attrsMethod, true);

		attrsKlass.put(ATTR_NAME, klassName.replace(C_DOT, C_SLASH));
		attrsKlass.put(ATTR_ID, klassID);
		Tag tagKlass = new Tag(TAG_KLASS, attrsKlass, true);

		attrsParse.put(ATTR_METHOD, methodID);
		Tag tagParse = new Tag(TAG_PARSE, attrsParse, false);

		IParseDictionary parseDictionary = new ParseDictionary();
		parseDictionary.setKlass(klassID, tagKlass);
		parseDictionary.setMethod(methodID, tagMethod);
		parseDictionary.setType(idInt, tagTypeInt);

		IMetaMember member = UnitTestUtil.createTestMetaMember(klassName, methodName, new Class[0], int.class);

		String tagMethodID = tagParse.getAttribute(ATTR_METHOD);

		assertTrue(JournalUtil.memberMatchesMethodID(member, tagMethodID, parseDictionary));
	}

	@Test
	public void testIsJournalForCompile2NativeMember()
	{
		String tagText = "<nmethod address='0x00007fb0ef001550' method='sun/misc/Unsafe compareAndSwapLong (Ljava/lang/Object;JJJ)Z' consts_offset='872' count='5000' backedge_count='1' stamp='2.453' iicount='10000' entry='0x00007fb0ef0016c0' size='872' compile_kind='c2n' insts_offset='368' bytes='0' relocation_offset='296' compile_id='28'/>";

		IMetaMember member = UnitTestUtil.createTestMetaMember();

		TagProcessor tp = new TagProcessor();
		Tag tag = tp.processLine(tagText);

		member.getJournal().addEntry(tag);

		assertTrue(JournalUtil.isJournalForCompile2NativeMember(member.getJournal()));
	}

	@Test
	public void testIsNotJournalForCompile2NativeMember()
	{
		String tagText = "<task_done success='1' nmsize='120' count='5000' backedge_count='5100' stamp='14.723'/>";

		IMetaMember member = UnitTestUtil.createTestMetaMember();

		TagProcessor tp = new TagProcessor();
		Tag tag = tp.processLine(tagText);

		member.getJournal().addEntry(tag);

		assertFalse(JournalUtil.isJournalForCompile2NativeMember(member.getJournal()));
	}

	private void checkLine(BytecodeAnnotations result, int index, String annotation, BCAnnotationType type)
	{
		List<LineAnnotation> lines = result.getAnnotationsForBCI(index);

		assertNotNull(lines);

		boolean matchedAnnotation = false;
		boolean matchedType = false;

		for (LineAnnotation lineAnnotation : lines)
		{
			if (lineAnnotation.getAnnotation().contains(annotation))
			{
				matchedAnnotation = true;
				if (lineAnnotation.getType() == type)
				{
					matchedType = true;
				}
			}
		}

		assertTrue("Did not match text: " + annotation, matchedAnnotation);
		assertTrue("Did not match type: " + type, matchedType);
	}

	@Test
	public void testMemberMatchesParseTagWithExactParams()
	{
		String methodName = "print";
		String klassName = "java.io.PrintStream";
		Class<?>[] params = new Class[] {
				java.lang.String.class };

		Map<String, String> attrsMethod = new HashMap<>();
		Map<String, String> attrsKlass = new HashMap<>();
		Map<String, String> attrsParse = new HashMap<>();

		Map<String, String> attrsTypeVoid = new HashMap<>();
		Map<String, String> attrsTypeString = new HashMap<>();

		String idString = "1";
		String nameString = "java.lang.String";

		String idVoid = "2";
		String nameVoid = S_TYPE_NAME_VOID;

		attrsTypeString.put(ATTR_ID, idString);
		attrsTypeString.put(ATTR_NAME, nameString);

		attrsTypeVoid.put(ATTR_ID, idVoid);
		attrsTypeVoid.put(ATTR_NAME, nameVoid);

		Tag tagTypeString = new Tag(TAG_TYPE, attrsTypeString, true);
		Tag tagTypeVoid = new Tag(TAG_TYPE, attrsTypeVoid, true);

		String methodID = "123";
		String klassID = "456";

		attrsMethod.put(ATTR_NAME, methodName);
		attrsMethod.put(ATTR_ID, methodID);
		attrsMethod.put(ATTR_HOLDER, klassID);
		attrsMethod.put(ATTR_ARGUMENTS, idString);
		attrsMethod.put(ATTR_RETURN, idVoid);

		Tag tagMethod = new Tag(TAG_METHOD, attrsMethod, true);

		attrsKlass.put(ATTR_NAME, klassName.replace(C_DOT, C_SLASH));
		attrsKlass.put(ATTR_ID, klassID);
		Tag tagKlass = new Tag(TAG_KLASS, attrsKlass, true);

		attrsParse.put(ATTR_METHOD, methodID);
		Tag tagParse = new Tag(TAG_PARSE, attrsParse, false);

		IParseDictionary parseDictionary = new ParseDictionary();
		parseDictionary.setKlass(klassID, tagKlass);
		parseDictionary.setMethod(methodID, tagMethod);
		parseDictionary.setType(idString, tagTypeString);
		parseDictionary.setType(idVoid, tagTypeVoid);

		IMetaMember member = UnitTestUtil.createTestMetaMember(klassName, methodName, params, void.class);

		String tagMethodID = tagParse.getAttribute(ATTR_METHOD);

		assertTrue(JournalUtil.memberMatchesMethodID(member, tagMethodID, parseDictionary));
	}

	@Test
	public void testMemberDoesNotMatchParseTagWithInexactParams()
	{
		String methodName = "print";
		String klassName = "java.io.PrintStream";
		Class<?>[] params = new Class[] {
				java.lang.Object.class };

		Map<String, String> attrsMethod = new HashMap<>();
		Map<String, String> attrsKlass = new HashMap<>();
		Map<String, String> attrsParse = new HashMap<>();

		Map<String, String> attrsTypeVoid = new HashMap<>();
		Map<String, String> attrsTypeString = new HashMap<>();

		String idString = "1";
		String nameString = "java.lang.String";

		String idVoid = "2";
		String nameVoid = S_TYPE_NAME_VOID;

		attrsTypeString.put(ATTR_ID, idString);
		attrsTypeString.put(ATTR_NAME, nameString);

		attrsTypeVoid.put(ATTR_ID, idVoid);
		attrsTypeVoid.put(ATTR_NAME, nameVoid);

		Tag tagTypeString = new Tag(TAG_TYPE, attrsTypeString, true);
		Tag tagTypeVoid = new Tag(TAG_TYPE, attrsTypeVoid, true);

		String methodID = "123";
		String klassID = "456";

		attrsMethod.put(ATTR_NAME, methodName);
		attrsMethod.put(ATTR_ID, methodID);
		attrsMethod.put(ATTR_HOLDER, klassID);
		attrsMethod.put(ATTR_ARGUMENTS, idString);
		attrsMethod.put(ATTR_RETURN, idVoid);

		Tag tagMethod = new Tag(TAG_METHOD, attrsMethod, true);

		attrsKlass.put(ATTR_NAME, klassName.replace(C_DOT, C_SLASH));
		attrsKlass.put(ATTR_ID, klassID);
		Tag tagKlass = new Tag(TAG_KLASS, attrsKlass, true);

		attrsParse.put(ATTR_METHOD, methodID);
		Tag tagParse = new Tag(TAG_PARSE, attrsParse, false);

		IParseDictionary parseDictionary = new ParseDictionary();
		parseDictionary.setKlass(klassID, tagKlass);
		parseDictionary.setMethod(methodID, tagMethod);
		parseDictionary.setType(idString, tagTypeString);
		parseDictionary.setType(idVoid, tagTypeVoid);

		IMetaMember member = UnitTestUtil.createTestMetaMember(klassName, methodName, params, void.class);

		String tagMethodID = tagParse.getAttribute(ATTR_METHOD);

		assertFalse(JournalUtil.memberMatchesMethodID(member, tagMethodID, parseDictionary));
	}

	@Test
	public void testEliminatedHeapAllocationsCorrectKlass()
	{
		String[] logLines = new String[] {
				"<task compile_id='11' compile_kind='osr' method='EscapeTest run ()Ljava/lang/String;' bytes='110' count='1' backedge_count='14563' iicount='1' osr_bci='7' blocking='1' stamp='0.298'>",
				"<phase name='parse' nodes='3' live='3' stamp='0.298'>",
				"<klass name='java.lang.String' flags='17' id='766'/>",
				"<klass name='EscapeTest' flags='1' id='870'/>",
				"<method bytes='110' name='run' flags='1' holder='870' id='871' iicount='1' return='766'/>",
				"<klass id='822' name='java/lang/StringBuilder' unloaded='1'/>",
				"<uncommon_trap method='871' bci='84' reason='unloaded' action='reinterpret' index='13' klass='822'/>",
				"<parse method='871' uses='1' osr_bci='7' stamp='0.298'>",
				"<uncommon_trap method='871' bci='84' reason='unloaded' action='reinterpret' index='13' klass='822'/>",
				"<dependency type='leaf_type' ctxk='817'/>",
				"<dependency type='leaf_type' ctxk='817'/>",
				"<uncommon_trap bci='7' reason='constraint' action='reinterpret'/>",
				"<uncommon_trap bci='7' reason='predicate' action='maybe_recompile'/>",
				"<uncommon_trap bci='7' reason='loop_limit_check' action='maybe_recompile'/>",
				"<bc code='162' bci='11'/>",
				"<branch target_bci='84' taken='0' not_taken='11264' cnt='11264' prob='never'/>",
				"<uncommon_trap bci='11' reason='unstable_if' action='reinterpret' comment='taken never'/>",
				"<bc code='180' bci='22'/>",
				"<uncommon_trap bci='22' reason='null_check' action='maybe_recompile'/>",
				"<bc code='182' bci='25'/>",
				"<klass id='820' name='java/util/Random' flags='1'/>",
				"<type id='701' name='boolean'/>",
				"<method id='823' holder='820' name='nextBoolean' return='701' flags='1' bytes='14' compile_id='5' compiler='C2' iicount='10000'/>",
				"<dependency type='unique_concrete_method' ctxk='820' x='823'/>",
				"<call method='823' count='11264' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='823' uses='11264' stamp='0.298'>",
				"<uncommon_trap bci='25' reason='null_check' action='maybe_recompile'/>",
				"<bc code='182' bci='2'/>",
				"<type id='707' name='int'/>",
				"<method id='828' holder='820' name='next' return='707' arguments='707' flags='4' bytes='47' compile_id='6' compiler='C2' iicount='10000'/>",
				"<dependency type='unique_concrete_method' ctxk='820' x='828'/>",
				"<call method='828' count='6701' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='828' uses='6701' stamp='0.298'>",
				"<bc code='182' bci='8'/>",
				"<type id='708' name='long'/>",
				"<klass id='830' name='java/util/concurrent/atomic/AtomicLong' flags='1'/>",
				"<method id='831' holder='830' name='get' return='708' flags='17' bytes='5' compile_id='3' compiler='C2' iicount='10000'/>",
				"<call method='831' count='6701' prof_factor='0.6701' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='831' uses='4490' stamp='0.298'>",
				"<uncommon_trap bci='8' reason='null_check' action='maybe_recompile'/>",
				"<parse_done nodes='218' live='210' memory='55928' stamp='0.298'/>",
				"</parse>",
				"<bc code='182' bci='32'/>",
				"<method id='832' holder='830' name='compareAndSet' return='701' arguments='708 708' flags='17' bytes='13' compile_id='4' compiler='C2' iicount='10000'/>",
				"<call method='832' count='6701' prof_factor='0.6701' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='832' uses='4490' stamp='0.298'>",
				"<bc code='182' bci='9'/>",
				"<klass id='714' name='java/lang/Object' flags='1'/>",
				"<klass id='781' name='sun/misc/Unsafe' flags='17'/>",
				"<method id='836' holder='781' name='compareAndSwapLong' return='701' arguments='714 708 708 708' flags='273' bytes='0' compile_id='2' compile_kind='c2n' iicount='10000'/>",
				"<call method='836' count='6701' prof_factor='0.449' inline='1'/>",
				"<intrinsic id='_compareAndSwapLong' nodes='19'/>",
				"<parse_done nodes='256' live='247' memory='60872' stamp='0.298'/>",
				"</parse>",
				"<bc code='153' bci='35'/>",
				"<branch target_bci='6' taken='0' not_taken='6701' cnt='6701' prob='never'/>",
				"<uncommon_trap bci='35' reason='unstable_if' action='reinterpret' comment='taken never'/>",
				"<parse_done nodes='281' live='270' memory='75712' stamp='0.298'/>",
				"</parse>",
				"<bc code='153' bci='5'/>",
				"<branch target_bci='12' taken='3275' not_taken='3426' cnt='6701' prob='0.488733'/>",
				"<parse_done nodes='292' live='280' memory='77120' stamp='0.298'/>",
				"</parse>",
				"<bc code='153' bci='28'/>",
				"<branch target_bci='35' taken='5603' not_taken='5661' cnt='11264' prob='0.497425'/>",
				"<bc code='183' bci='42'/>",
				"<type id='709' name='void'/>",
				"<klass id='821' name='EscapeTest$Wrapper1' flags='1'/>",
				"<method id='824' holder='821' name='&lt;init&gt;' return='709' arguments='817 707' flags='1' bytes='15' compile_id='7' compiler='C2' iicount='10000'/>",
				"<call method='824' count='11264' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='824' uses='11264' stamp='0.298'>",
				"<bc code='183' bci='6'/>",
				"<method id='839' holder='714' name='&lt;init&gt;' return='709' flags='1' bytes='1' compile_id='1' compiler='C2' iicount='10000'/>",
				"<call method='839' count='6701' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='839' uses='6701' stamp='0.299'>",
				"<parse_done nodes='368' live='355' memory='89432' stamp='0.299'/>",
				"</parse>",
				"<parse_done nodes='380' live='366' memory='91096' stamp='0.299'/>",
				"</parse>",
				"<bc code='183' bci='54'/>",
				"<klass id='825' name='EscapeTest$Wrapper2' flags='1'/>",
				"<method id='826' holder='825' name='&lt;init&gt;' return='709' arguments='817 707' flags='1' bytes='15' compile_id='8' compiler='C2' iicount='10000'/>",
				"<call method='826' count='11264' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='826' uses='11264' stamp='0.299'>",
				"<bc code='183' bci='6'/>",
				"<call method='839' count='6701' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='839' uses='6701' stamp='0.299'>",
				"<parse_done nodes='447' live='432' memory='103856' stamp='0.299'/>",
				"</parse>",
				"<parse_done nodes='459' live='443' memory='105584' stamp='0.299'/>",
				"</parse>",
				"<bc code='182' bci='63'/>",
				"<method id='827' holder='821' name='equals' return='701' arguments='825' flags='1' bytes='17' compile_id='9' compiler='C2' iicount='10000'/>",
				"<dependency type='unique_concrete_method' ctxk='821' x='827'/>",
				"<call method='827' count='11264' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='827' uses='11264' stamp='0.299'>",
				"<bc code='182' bci='5'/>",
				"<method id='843' holder='825' name='getValue' return='707' flags='1' bytes='5' compile_id='10' compiler='C2' iicount='10000'/>",
				"<dependency type='unique_concrete_method' ctxk='825' x='843'/>",
				"<call method='843' count='6701' prof_factor='1' inline='1'/>",
				"<inline_success reason='accessor'/>",
				"<parse method='843' uses='6701' stamp='0.299'>",
				"<parse_done nodes='483' live='466' memory='109448' stamp='0.299'/>",
				"</parse>",
				"<bc code='160' bci='8'/>",
				"<branch target_bci='15' taken='3275' not_taken='3426' cnt='6701' prob='0.488733'/>",
				"<parse_done nodes='492' live='474' memory='110592' stamp='0.299'/>",
				"</parse>",
				"<bc code='153' bci='66'/>",
				"<branch target_bci='75' taken='5603' not_taken='5661' cnt='11264' prob='0.497425'/>",
				"<parse_done nodes='510' live='492' memory='114200' stamp='0.299'/>",
				"</parse>",
				"<phase_done name='parse' nodes='511' live='285' stamp='0.299'/>",
				"</phase>",
				"<phase name='optimizer' nodes='511' live='285' stamp='0.299'>",
				"<phase name='idealLoop' nodes='533' live='253' stamp='0.299'>",
				"<loop_tree>",
				"<loop idx='533' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='534' live='252' stamp='0.299'/>",
				"</phase>",
				"<phase name='escapeAnalysis' nodes='534' live='252' stamp='0.299'>",
				"<phase name='connectionGraph' nodes='535' live='253' stamp='0.299'>",
				"<phase_done name='connectionGraph' nodes='535' live='253' stamp='0.299'/>",
				"</phase>",
				"<phase_done name='escapeAnalysis' nodes='543' live='261' stamp='0.299'/>",
				"</phase>",
				"<eliminate_allocation type='825'>",
				"<jvms bci='47' method='871'/>",
				"</eliminate_allocation>",
				"<eliminate_allocation type='821'>",
				"<jvms bci='35' method='871'/>",
				"</eliminate_allocation>",
				"<phase name='idealLoop' nodes='546' live='200' stamp='0.300'>",
				"<loop_tree>",
				"<loop idx='533' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='551' live='197' stamp='0.300'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='551' live='197' stamp='0.300'>",
				"<loop_tree>",
				"<loop idx='533' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='681' live='210' stamp='0.300'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='681' live='210' stamp='0.300'>",
				"<loop_tree>",
				"<loop idx='686' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='690' live='196' stamp='0.300'/>",
				"</phase>",
				"<phase name='ccp' nodes='690' live='196' stamp='0.300'>",
				"<phase_done name='ccp' nodes='690' live='196' stamp='0.300'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='692' live='194' stamp='0.300'>",
				"<loop_tree>",
				"<loop idx='686' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='716' live='188' stamp='0.301'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='716' live='188' stamp='0.301'>",
				"<loop_tree>",
				"<loop idx='686' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='719' live='188' stamp='0.301'/>",
				"</phase>",
				"<phase_done name='optimizer' nodes='729' live='188' stamp='0.301'/>",
				"</phase>",
				"<phase name='matcher' nodes='729' live='188' stamp='0.301'>",
				"<phase_done name='matcher' nodes='172' live='172' stamp='0.301'/>",
				"</phase>",
				"<phase name='regalloc' nodes='210' live='210' stamp='0.301'>",
				"<regalloc attempts='0' success='1'/>",
				"<phase_done name='regalloc' nodes='237' live='226' stamp='0.302'/>",
				"</phase>",
				"<phase name='output' nodes='237' live='226' stamp='0.302'>",
				"<phase_done name='output' nodes='253' live='235' stamp='0.302'/>",
				"</phase>",
				"<dependency type='leaf_type' ctxk='817'/>",
				"<dependency type='unique_concrete_method' ctxk='820' x='823'/>",
				"<dependency type='unique_concrete_method' ctxk='820' x='828'/>",
				"<dependency type='unique_concrete_method' ctxk='821' x='827'/>",
				"<dependency type='unique_concrete_method' ctxk='825' x='843'/>",
				"<code_cache total_blobs='206' nmethods='11' adapters='146' free_code_cache='49831168'/>",
				"<task_done success='1' nmsize='408' count='1' backedge_count='14563' inlined_bytes='133' stamp='0.323'/>",
				"</task>" };

		String[] bytecodeLines = new String[] {
				"0: iconst_0",
				"1: istore_2",
				"2: iconst_0",
				"3: istore_3",
				"4: iconst_0",
				"5: istore 4",
				"7: iload  4",
				"9: ldc  #5   // int 100000000",
				"11: if_icmpge 84",
				"14: ldc  #6   // int 43981",
				"16: istore 5",
				"18: iconst_0",
				"19: istore 6",
				"21: aload_0",
				"22: getfield #4   // Field random:Ljava/util/Random;",
				"25: invokevirtual #7   // Method java/util/Random.nextBoolean:()Z",
				"28: ifeq  35",
				"31: ldc  #6   // int 43981",
				"33: istore 6",
				"35: new  #8   // class EscapeTest$Wrapper1",
				"38: dup",
				"39: aload_0",
				"40: iload  5",
				"42: invokespecial #9   // Method EscapeTest$Wrapper1.\"<init>\":(LEscapeTest;I)V",
				"45: astore 7",
				"47: new  #10   // class EscapeTest$Wrapper2",
				"50: dup",
				"51: aload_0",
				"52: iload  6",
				"54: invokespecial #11   // Method EscapeTest$Wrapper2.\"<init>\":(LEscapeTest;I)V",
				"57: astore 8",
				"59: aload  7",
				"61: aload  8",
				"63: invokevirtual #12   // Method EscapeTest$Wrapper1.equals:(LEscapeTest$Wrapper2;)Z",
				"66: ifeq  75",
				"69: iinc  2, 1",
				"72: goto  78",
				"75: iinc  3, 1",
				"78: iinc  4, 1",
				"81: goto  7",
				"84: new  #13   // class java/lang/StringBuilder",
				"87: dup",
				"88: invokespecial #14   // Method java/lang/StringBuilder.\"<init>\":()V",
				"91: iload_2",
				"92: invokevirtual #15   // Method java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;",
				"95: ldc  #16   // String /",
				"97: invokevirtual #17   // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;",
				"100: iload_3",
				"101: invokevirtual #15   // Method java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;",
				"104: invokevirtual #18   // Method java/lang/StringBuilder.toString:()Ljava/lang/String;",
				"107: astore_1",
				"108: aload_1",
				"109: areturn" };

		IMetaMember member = UnitTestUtil.createTestMetaMember("EscapeTest", "run", new Class[0], java.lang.String.class);

		BytecodeAnnotations result = buildAnnotations("1.8.0", member, logLines, bytecodeLines);

		assertEquals(12, result.annotatedLineCount());

		checkLine(result, 7, "constraint", BCAnnotationType.UNCOMMON_TRAP);
		checkLine(result, 7, "predicate", BCAnnotationType.UNCOMMON_TRAP);
		checkLine(result, 7, "loop_limit_check", BCAnnotationType.UNCOMMON_TRAP);
		checkLine(result, 11, "taken", BCAnnotationType.BRANCH);
		checkLine(result, 11, "unstable_if", BCAnnotationType.UNCOMMON_TRAP);
		checkLine(result, 22, "null_check", BCAnnotationType.UNCOMMON_TRAP);
		checkLine(result, 25, "hot", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 28, "taken", BCAnnotationType.BRANCH);
		checkLine(result, 35, "EscapeTest$Wrapper1", BCAnnotationType.ELIMINATED_ALLOCATION);
		checkLine(result, 42, "hot", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 47, "EscapeTest$Wrapper2", BCAnnotationType.ELIMINATED_ALLOCATION);
		checkLine(result, 54, "hot", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 63, "hot", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 66, "taken", BCAnnotationType.BRANCH);
		checkLine(result, 84, "unloaded", BCAnnotationType.UNCOMMON_TRAP);
	}

	@Test
	public void testEliminatedHeapAllocationsJDK9()
	{
		String[] logLines = new String[] {
				"<task compile_id='11' compile_kind='osr' method='EscapeTest run ()Ljava/lang/String;' bytes='110' count='1' backedge_count='14563' iicount='1' osr_bci='7' blocking='1' stamp='0.298'>",
				"<klass name='java.lang.String' flags='17' id='766'/>",
				"<klass name='EscapeTest' flags='1' id='870'/>",
				"<method bytes='110' name='run' flags='1' holder='870' id='871' iicount='1' return='766'/>",
				"  <klass unloaded='1' name='java.lang.StringBuilder' id='875'/>",
				"  <uncommon_trap reason='unloaded' method='871' klass='875' bci='84' action='reinterpret' index='13'/>",
				"  <parse osr_bci='7' method='871' stamp='0.627' uses='1.000000'> <!-- java.lang.String EscapeTest.run() -->",
				"    <uncommon_trap reason='unloaded' method='871' klass='875' bci='84' action='reinterpret' index='13'/>",
				"    <dependency ctxk='870' type='leaf_type'/>",
				"    <dependency ctxk='870' type='leaf_type'/>",
				"    <uncommon_trap reason='constraint' bci='7' action='reinterpret'/>",
				"    <uncommon_trap reason='predicate' bci='7' action='maybe_recompile'/>",
				"    <uncommon_trap reason='loop_limit_check' bci='7' action='maybe_recompile'/>",
				"    <bc code='162' bci='11'/>",
				"    <branch prob='never' not_taken='11264' taken='0' cnt='11264.000000' target_bci='84'/>",
				"    <uncommon_trap reason='unstable_if' bci='11' action='reinterpret' comment='taken never'/>",
				"    <bc code='180' bci='22'/>",
				"    <uncommon_trap reason='null_check' bci='22' action='maybe_recompile'/>",
				"    <bc code='182' bci='25'/>",
				"    <klass name='java.util.Random' flags='1' id='873'/>",
				"    <type name='boolean' id='752'/>",
				"    <method bytes='14' name='nextBoolean' flags='1' holder='873' id='876' compile_id='103' compiler='C2' iicount='10000' return='752'/>",
				"    <dependency x='876' ctxk='873' type='unique_concrete_method'/>",
				"    <call method='876' inline='1' count='11264' prof_factor='1.000000'/>",
				"    <inline_success reason='inline (hot)'/>",
				"    <parse method='876' stamp='0.627' uses='11264.000000'> <!-- boolean java.util.Random.nextBoolean() -->",
				"      <uncommon_trap reason='null_check' bci='25' action='maybe_recompile'/>",
				"      <bc code='182' bci='2'/>",
				"      <type name='int' id='758'/>",
				"      <method bytes='47' name='next' flags='4' holder='873' arguments='758' id='881' compile_id='104' compiler='C2' iicount='10000' return='758'/>",
				"      <dependency x='881' ctxk='873' type='unique_concrete_method'/>",
				"      <call method='881' inline='1' count='6701' prof_factor='1.000000'/>",
				"      <inline_success reason='inline (hot)'/>",
				"      <parse method='881' stamp='0.627' uses='6701.000000'> <!-- int java.util.Random.next(int) -->",
				"        <bc code='182' bci='8'/>",
				"        <type name='long' id='759'/>",
				"        <klass name='java.util.concurrent.atomic.AtomicLong' flags='1' id='883'/>",
				"        <method bytes='5' name='get' flags='17' holder='883' id='884' compile_id='101' compiler='C2' iicount='10000' return='759'/>",
				"        <call method='884' inline='1' count='6701' prof_factor='0.670100'/>",
				"        <inline_success reason='inline (hot)'/>",
				"        <parse method='884' stamp='0.627' uses='4490.000000'> <!-- long java.util.concurrent.atomic.AtomicLong.get() -->",
				"          <uncommon_trap reason='null_check' bci='8' action='maybe_recompile'/>",
				"          <parse_done nodes='213' memory='53320' stamp='0.627' live='205'/>",
				"        </parse>",
				"        <bc code='182' bci='32'/>",
				"        <method bytes='13' name='compareAndSet' flags='17' holder='883' arguments='759 759' id='885' compile_id='102' compiler='C2' iicount='10000' return='752'/>",
				"        <call method='885' inline='1' count='6701' prof_factor='0.670100'/>",
				"        <inline_success reason='inline (hot)'/>",
				"        <parse method='885' stamp='0.627' uses='4490.000000'> <!-- boolean java.util.concurrent.atomic.AtomicLong.compareAndSet(long,long) -->",
				"          <bc code='182' bci='9'/>",
				"          <klass name='java.lang.Object' flags='1' id='765'/>",
				"          <klass name='sun.misc.Unsafe' flags='17' id='834'/>",
				"          <method compile_kind='c2n' bytes='0' name='compareAndSwapLong' flags='273' holder='834' arguments='765 759 759 759' id='889' compile_id='100' iicount='10000' return='752'/>",
				"          <call method='889' inline='1' count='6701' prof_factor='0.449000'/>",
				"          <intrinsic nodes='19' id='_compareAndSwapLong'/>",
				"          <parse_done nodes='251' memory='58264' stamp='0.627' live='242'/>",
				"        </parse>",
				"        <bc code='153' bci='35'/>",
				"        <branch prob='never' not_taken='6701' taken='0' cnt='6701.000000' target_bci='6'/>",
				"        <uncommon_trap reason='unstable_if' bci='35' action='reinterpret' comment='taken never'/>",
				"        <parse_done nodes='276' memory='67736' stamp='0.627' live='265'/>",
				"      </parse>",
				"      <bc code='153' bci='5'/>",
				"      <branch prob='0.500373' not_taken='3348' taken='3353' cnt='6701.000000' target_bci='12'/>",
				"      <parse_done nodes='287' memory='69144' stamp='0.627' live='275'/>",
				"    </parse>",
				"    <bc code='153' bci='28'/>",
				"    <branch prob='0.502575' not_taken='5603' taken='5661' cnt='11264.000000' target_bci='35'/>",
				"    <bc code='183' bci='42'/>",
				"    <type name='void' id='760'/>",
				"    <klass name='EscapeTest$Wrapper1' flags='1' id='874'/>",
				"    <method bytes='15' name='&lt;init&gt;' flags='1' holder='874' arguments='870 758' id='877' compile_id='105' compiler='C2' iicount='10000' return='760'/>",
				"    <call method='877' inline='1' count='11264' prof_factor='1.000000'/>",
				"    <inline_success reason='inline (hot)'/>",
				"    <parse method='877' stamp='0.627' uses='11264.000000'> <!-- void EscapeTest$Wrapper1.&lt;init&gt;(EscapeTest,int) -->",
				"      <bc code='183' bci='6'/>",
				"      <method bytes='1' name='&lt;init&gt;' flags='1' holder='765' id='892' compile_id='40' compiler='C2' iicount='10000' return='760'/>",
				"      <call method='892' inline='1' count='6701' prof_factor='1.000000'/>",
				"      <inline_success reason='inline (hot)'/>",
				"      <parse method='892' stamp='0.627' uses='6701.000000'> <!-- void java.lang.Object.&lt;init&gt;() -->",
				"        <parse_done nodes='361' memory='81288' stamp='0.627' live='348'/>",
				"      </parse>",
				"      <parse_done nodes='373' memory='83016' stamp='0.628' live='359'/>",
				"    </parse>",
				"    <bc code='183' bci='54'/>",
				"    <klass name='EscapeTest$Wrapper2' flags='1' id='878'/>",
				"    <method bytes='15' name='&lt;init&gt;' flags='1' holder='878' arguments='870 758' id='879' compile_id='106' compiler='C2' iicount='10000' return='760'/>",
				"    <call method='879' inline='1' count='11264' prof_factor='1.000000'/>",
				"    <inline_success reason='inline (hot)'/>",
				"    <parse method='879' stamp='0.628' uses='11264.000000'> <!-- void EscapeTest$Wrapper2.&lt;init&gt;(EscapeTest,int) -->",
				"      <bc code='183' bci='6'/>",
				"      <call method='892' inline='1' count='6701' prof_factor='1.000000'/>",
				"      <inline_success reason='inline (hot)'/>",
				"      <parse method='892' stamp='0.628' uses='6701.000000'> <!-- void java.lang.Object.&lt;init&gt;() -->",
				"        <parse_done nodes='437' memory='95472' stamp='0.628' live='422'/>",
				"      </parse>",
				"      <parse_done nodes='449' memory='97136' stamp='0.628' live='433'/>",
				"    </parse>",
				"    <bc code='182' bci='63'/>",
				"    <method bytes='17' name='equals' flags='1' holder='874' arguments='878' id='880' compile_id='107' compiler='C2' iicount='10000' return='752'/>",
				"    <dependency x='880' ctxk='874' type='unique_concrete_method'/>",
				"    <call method='880' inline='1' count='11264' prof_factor='1.000000'/>",
				"    <inline_success reason='inline (hot)'/>",
				"    <parse method='880' stamp='0.628' uses='11264.000000'> <!-- boolean EscapeTest$Wrapper1.equals(EscapeTest$Wrapper2) -->",
				"      <bc code='182' bci='5'/>",
				"      <method bytes='5' name='getValue' flags='1' holder='878' id='896' compile_id='108' compiler='C2' iicount='10000' return='758'/>",
				"      <dependency x='896' ctxk='878' type='unique_concrete_method'/>",
				"      <call method='896' inline='1' count='6701' prof_factor='1.000000'/>",
				"      <inline_success reason='accessor'/>",
				"      <parse method='896' stamp='0.628' uses='6701.000000'> <!-- int EscapeTest$Wrapper2.getValue() -->",
				"        <parse_done nodes='473' memory='101128' stamp='0.628' live='456'/>",
				"      </parse>",
				"      <bc code='160' bci='8'/>",
				"      <branch prob='0.500373' not_taken='3348' taken='3353' cnt='6701.000000' target_bci='15'/>",
				"      <parse_done nodes='482' memory='102272' stamp='0.628' live='464'/>",
				"    </parse>",
				"    <bc code='153' bci='66'/>",
				"    <branch prob='0.502575' not_taken='5603' taken='5661' cnt='11264.000000' target_bci='75'/>",
				"    <parse_done nodes='500' memory='105880' stamp='0.628' live='482'/>",
				"  </parse>",
				"  <loop_tree>",
				"    <loop idx='523' inner_loop='1'>",
				"    </loop>",
				"  </loop_tree>",
				"  <eliminate_allocation type='878'>",
				"    <jvms method='871' bci='47'/>",
				"  </eliminate_allocation>",
				"  <eliminate_allocation type='874'>",
				"    <jvms method='871' bci='35'/>",
				"  </eliminate_allocation>",
				"  <loop_tree>",
				"    <loop idx='523' inner_loop='1'>",
				"    </loop>",
				"  </loop_tree>",
				"  <loop_tree>",
				"    <loop idx='523' inner_loop='1'>",
				"    </loop>",
				"  </loop_tree>",
				"  <loop_tree>",
				"    <loop idx='673'>",
				"    </loop>",
				"  </loop_tree>",
				"  <loop_tree>",
				"    <loop idx='673' inner_loop='1'>",
				"    </loop>",
				"  </loop_tree>",
				"  <loop_tree>",
				"    <loop idx='673' inner_loop='1'>",
				"    </loop>",
				"  </loop_tree>",
				"  <regalloc success='1' attempts='0'/>",
				"  <dependency ctxk='870' type='leaf_type'/>",
				"  <dependency x='876' ctxk='873' type='unique_concrete_method'/>",
				"  <dependency x='881' ctxk='873' type='unique_concrete_method'/>",
				"  <dependency x='880' ctxk='874' type='unique_concrete_method'/>",
				"  <dependency x='896' ctxk='878' type='unique_concrete_method'/>",
				"  <code_cache nmethods='109' free_code_cache='49690752' adapters='192' total_blobs='354' stamp='0.627'/>",
				"  <task_done inlined_bytes='133' success='1' count='1' backedge_count='14563' stamp='0.630' nmsize='440'/>",
				"</task>" };

		String[] bytecodeLines = new String[] {
				"0: iconst_0",
				"1: istore_2",
				"2: iconst_0",
				"3: istore_3",
				"4: iconst_0",
				"5: istore 4",
				"7: iload  4",
				"9: ldc  #5   // int 100000000",
				"11: if_icmpge 84",
				"14: ldc  #6   // int 43981",
				"16: istore 5",
				"18: iconst_0",
				"19: istore 6",
				"21: aload_0",
				"22: getfield #4   // Field random:Ljava/util/Random;",
				"25: invokevirtual #7   // Method java/util/Random.nextBoolean:()Z",
				"28: ifeq  35",
				"31: ldc  #6   // int 43981",
				"33: istore 6",
				"35: new  #8   // class EscapeTest$Wrapper1",
				"38: dup",
				"39: aload_0",
				"40: iload  5",
				"42: invokespecial #9   // Method EscapeTest$Wrapper1.\"<init>\":(LEscapeTest;I)V",
				"45: astore 7",
				"47: new  #10   // class EscapeTest$Wrapper2",
				"50: dup",
				"51: aload_0",
				"52: iload  6",
				"54: invokespecial #11   // Method EscapeTest$Wrapper2.\"<init>\":(LEscapeTest;I)V",
				"57: astore 8",
				"59: aload  7",
				"61: aload  8",
				"63: invokevirtual #12   // Method EscapeTest$Wrapper1.equals:(LEscapeTest$Wrapper2;)Z",
				"66: ifeq  75",
				"69: iinc  2, 1",
				"72: goto  78",
				"75: iinc  3, 1",
				"78: iinc  4, 1",
				"81: goto  7",
				"84: new  #13   // class java/lang/StringBuilder",
				"87: dup",
				"88: invokespecial #14   // Method java/lang/StringBuilder.\"<init>\":()V",
				"91: iload_2",
				"92: invokevirtual #15   // Method java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;",
				"95: ldc  #16   // String /",
				"97: invokevirtual #17   // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;",
				"100: iload_3",
				"101: invokevirtual #15   // Method java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;",
				"104: invokevirtual #18   // Method java/lang/StringBuilder.toString:()Ljava/lang/String;",
				"107: astore_1",
				"108: aload_1",
				"109: areturn" };

		IMetaMember member = UnitTestUtil.createTestMetaMember("EscapeTest", "run", new Class[0], java.lang.String.class);

		BytecodeAnnotations result = buildAnnotations("1.9.0", member, logLines, bytecodeLines);

		assertEquals(12, result.annotatedLineCount());

		checkLine(result, 7, "constraint", BCAnnotationType.UNCOMMON_TRAP);
		checkLine(result, 7, "predicate", BCAnnotationType.UNCOMMON_TRAP);
		checkLine(result, 7, "loop_limit_check", BCAnnotationType.UNCOMMON_TRAP);
		checkLine(result, 11, "taken", BCAnnotationType.BRANCH);
		checkLine(result, 11, "unstable_if", BCAnnotationType.UNCOMMON_TRAP);
		checkLine(result, 22, "null_check", BCAnnotationType.UNCOMMON_TRAP);
		checkLine(result, 25, "hot", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 28, "taken", BCAnnotationType.BRANCH);
		checkLine(result, 35, "EscapeTest$Wrapper1", BCAnnotationType.ELIMINATED_ALLOCATION);
		checkLine(result, 42, "hot", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 47, "EscapeTest$Wrapper2", BCAnnotationType.ELIMINATED_ALLOCATION);
		checkLine(result, 54, "hot", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 63, "hot", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 66, "taken", BCAnnotationType.BRANCH);
		checkLine(result, 84, "unloaded", BCAnnotationType.UNCOMMON_TRAP);
	}

	@Test
	public void testJDK9NoParsePhaseTag()
	{
		String[] logLines = new String[] {
				"<task method='java.io.BufferedInputStream read ()I' bytes='49' count='5485' stamp='3.616' compiler='C2' compile_id='730' iicount='5489'>",
				"  <type name='int' id='756'/>",
				"  <klass name='java.io.BufferedInputStream' flags='1' id='868'/>",
				"  <method level='3' bytes='49' name='read' flags='33' holder='868' id='869' compile_id='611' compiler='C1' iicount='5621' return='756'/>",
				"  <parse method='869' stamp='3.617' uses='5621.000000'>",
				"    <bc code='161' bci='8'/>",
				"    <branch prob='0.999822' not_taken='1' taken='5604' cnt='5605.000000' target_bci='28'/>",
				"    <bc code='183' bci='12'/>",
				"    <type name='void' id='758'/>",
				"    <method bytes='233' name='fill' flags='2' holder='868' id='872' iicount='16' return='758'/>",
				"    <call method='872' inline='1' count='1' prof_factor='1.000000'/>",
				"    <klass name='java.io.IOException' flags='1' id='881'/>",
				"    <uncommon_trap reason='unloaded' method='872' klass='881' bci='178' action='reinterpret' index='2'/>",
				"    <klass name='java.lang.OutOfMemoryError' flags='1' id='786'/>",
				"    <uncommon_trap reason='unloaded' method='872' klass='786' bci='100' action='reinterpret' index='18'/>",
				"    <inline_fail reason='too big'/>",
				"    <direct_call bci='12'/>",
				"    <bc code='161' bci='23'/>",
				"    <branch not_taken='0' taken='1' target_bci='28'/>",
				"    <bc code='183' bci='29'/>",
				"    <klass name='[B' flags='1041' id='860'/>",
				"    <method level='3' bytes='21' name='getBufIfOpen' flags='2' holder='868' id='871' compile_id='610' compiler='C1' iicount='8108' return='860'/>",
				"    <call method='871' inline='1' count='5601' prof_factor='1.000000'/>",
				"    <uncommon_trap reason='unloaded' method='871' klass='881' bci='9' action='reinterpret' index='2'/>",
				"    <inline_success reason='inline (hot)'/>",
				"    <parse method='871' stamp='3.618' uses='5605.000000'>",
				"      <bc code='199' bci='6'/>",
				"      <branch prob='always' not_taken='0' taken='9784' cnt='9784.000000' target_bci='19'/>",
				"      <uncommon_trap reason='unstable_if' bci='6' action='reinterpret' comment='taken always'/>",
				"      <parse_done nodes='118' memory='36952' stamp='3.618' live='115'/>",
				"    </parse>",
				"    <bc code='51' bci='43'/>",
				"    <uncommon_trap reason='range_check' bci='43' action='make_not_entrant' comment='range_check'/>",
				"    <parse_done nodes='166' memory='45816' stamp='3.619' live='161'/>",
				"  </parse>",
				"  <dependency ctxk='868' type='leaf_type'/>",
				"  <regalloc success='1' attempts='1'/>",
				"  <dependency ctxk='868' type='leaf_type'/>",
				"  <code_cache nmethods='709' free_code_cache='248903424' adapters='229' total_blobs='1025' stamp='3.616'/>",
				"  <task_done inlined_bytes='21' success='1' count='8763' stamp='3.634' nmsize='1096'/>",
				"</task>" };

		String[] bytecodeLines = new String[] {
				" 0: aload_0         ",
				" 1: getfield        #14  // Field pos:I",
				" 4: aload_0         ",
				" 5: getfield        #23  // Field count:I",
				" 8: if_icmplt       28   ",
				"11: aload_0         ",
				"12: invokespecial   #26  // Method fill:()V",
				"15: aload_0         ",
				"16: getfield        #14  // Field pos:I",
				"19: aload_0         ",
				"20: getfield        #23  // Field count:I",
				"23: if_icmplt       28   ",
				"26: iconst_m1       ",
				"27: ireturn         ",
				"28: aload_0         ",
				"29: invokespecial   #13  // Method getBufIfOpen:()[B",
				"32: aload_0         ",
				"33: dup             ",
				"34: getfield        #14  // Field pos:I",
				"37: dup_x1          ",
				"38: iconst_1        ",
				"39: iadd            ",
				"40: putfield        #14  // Field pos:I",
				"43: baload          ",
				"44: sipush          255  ",
				"47: iand            ",
				"48: ireturn         " };

		IMetaMember member = UnitTestUtil.createTestMetaMember("java.io.BufferedInputStream", "read", new Class[0], int.class);

		BytecodeAnnotations result = buildAnnotations("1.9.0", member, logLines, bytecodeLines);

		assertEquals(5, result.annotatedLineCount());

		checkLine(result, 8, "taken", BCAnnotationType.BRANCH);
		checkLine(result, 12, "No, too big", BCAnnotationType.INLINE_FAIL);
		checkLine(result, 23, "taken", BCAnnotationType.BRANCH);
		checkLine(result, 29, "hot", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 43, "range_check", BCAnnotationType.UNCOMMON_TRAP);
	}
	
	// 
	/*
	@Test
	public void testLambdaEscapeAnalysis() throws ClassNotFoundException
	{
		String[] logLines = new String[]{
				"<task decompiles='1' unstable_if_traps='1' method='org.adoptopenjdk.jitwatch.test.examples.OptionalTest countOptional (Lorg.adoptopenjdk.jitwatch.test.examples.OptionalTest$Name;I)V' bytes='58' count='2' backedge_count='676143' stamp='2.315' compile_id='186' iicount='2'>",
				"  <phase nodes='3' name='parse' stamp='2.315' live='3'>",
				"    <type name='void' id='711'/>",
				"    <klass name='org.adoptopenjdk.jitwatch.test.examples.OptionalTest$Name' flags='0' id='820'/>",
				"    <type name='int' id='709'/>",
				"    <klass name='org.adoptopenjdk.jitwatch.test.examples.OptionalTest' flags='1' id='819'/>",
				"    <method level='3' bytes='58' name='countOptional' flags='10' holder='819' arguments='820 709' id='821' compile_id='182' compiler='C1' iicount='2' return='711'/>",
				"    <parse method='821' stamp='2.315' uses='2'> <!-- void org.adoptopenjdk.jitwatch.test.examples.OptionalTest.countOptional(org.adoptopenjdk.jitwatch.test.examples.OptionalTest$Name,int) -->",
				"      <observe total='1' count='1' trap='unstable_if'/>",
				"      <bc code='162' bci='4'/>",
				"      <branch prob='1.6626e-06' not_taken='601468' taken='1' cnt='601469' target_bci='26'/>",
				"      <uncommon_trap reason='predicate' bci='7' action='maybe_recompile'/>",
				"      <uncommon_trap reason='loop_limit_check' bci='7' action='maybe_recompile'/>",
				"      <bc code='182' bci='8'/>",
				"      <klass name='java/util/Optional' flags='17' id='823'/>",
				"      <method level='4' bytes='8' name='getOptionalName' flags='1' holder='820' id='831' compile_id='179' compiler='C2' iicount='617882' return='823'/>",
				"      <dependency x='831' ctxk='820' type='unique_concrete_method'/>",
				"      <call method='831' inline='1' count='601468' prof_factor='1'/>",
				"      <inline_success reason='inline (hot)'/>",
				"      <parse method='831' stamp='2.315' uses='601468'> <!-- java.util.Optional org.adoptopenjdk.jitwatch.test.examples.OptionalTest$Name.getOptionalName() -->",
				"        <uncommon_trap reason='null_check' bci='8' action='maybe_recompile'/>",
				"        <bc code='184' bci='4'/>",
				"        <klass name='java/lang/Object' flags='1' id='716'/>",
				"        <method level='3' bytes='15' name='ofNullable' flags='9' holder='823' arguments='716' id='833' compile_id='175' compiler='C1' iicount='617882' return='823'/>",
				"        <call method='833' inline='1' count='617498' prof_factor='0.973435'/>",
				"        <inline_success reason='inline (hot)'/>",
				"        <parse method='833' stamp='2.315' uses='601468'> <!-- java.util.Optional java.util.Optional.ofNullable(java.lang.Object) -->",
				"          <bc code='199' bci='1'/>",
				"          <branch prob='always' not_taken='0' taken='617882' cnt='617882' target_bci='10'/>",
				"          <uncommon_trap reason='unstable_if' bci='1' action='reinterpret' comment='taken always'/>",
				"          <bc code='184' bci='11'/>",
				"          <method level='2' bytes='9' name='of' flags='9' holder='823' arguments='716' id='835' compile_id='176' compiler='C1' iicount='617882' return='823'/>",
				"          <call method='835' inline='1' count='617457' prof_factor='0.973435'/>",
				"          <inline_success reason='inline (hot)'/>",
				"          <parse method='835' stamp='2.315' uses='601468'> <!-- java.util.Optional java.util.Optional.of(java.lang.Object) -->",
				"            <bc code='183' bci='5'/>",
				"            <method level='3' bytes='13' name='&lt;init&gt;' flags='2' holder='823' arguments='716' id='838' compile_id='177' compiler='C1' iicount='617882' return='711'/>",
				"            <call method='838' inline='1' count='617370' prof_factor='0.973435'/>",
				"            <inline_success reason='inline (hot)'/>",
				"            <parse method='838' stamp='2.315' uses='601468'> <!-- void java.util.Optional.\"&lt;init&gt;\"(java.lang.Object) -->",
				"              <bc code='183' bci='1'/>",
				"              <method level='1' bytes='1' name='&lt;init&gt;' flags='1' holder='716' id='840' compile_id='19' compiler='C1' iicount='624932' return='711'/>",
				"              <call method='840' inline='1' count='617370' prof_factor='0.973435'/>",
				"              <inline_success reason='inline (hot)'/>",
				"              <parse method='840' stamp='2.315' uses='601468'> <!-- void java.lang.Object.&lt;init&gt;() -->",
				"                <parse_done nodes='195' memory='49512' stamp='2.315' live='190'/>",
				"              </parse>",
				"              <bc code='184' bci='6'/>",
				"              <klass name='java/util/Objects' flags='17' id='841'/>",
				"              <method level='3' bytes='14' name='requireNonNull' flags='9' holder='841' arguments='716' id='842' compile_id='79' compiler='C1' iicount='619098' return='716'/>",
				"              <call method='842' inline='1' count='617370' prof_factor='0.973435'/>",
				"              <klass name='java/lang/NullPointerException' flags='1' id='815'/>",
				"              <uncommon_trap reason='unloaded' method='842' klass='815' bci='4' action='reinterpret' index='12'/>",
				"              <inline_success reason='inline (hot)'/>",
				"              <parse method='842' stamp='2.315' uses='601468'> <!-- java.lang.Object java.util.Objects.requireNonNull(java.lang.Object) -->",
				"                <bc code='199' bci='1'/>",
				"                <branch prob='always' not_taken='0' taken='619098' cnt='619098' target_bci='12'/>",
				"                <parse_done nodes='215' memory='53160' stamp='2.315' live='209'/>",
				"              </parse>",
				"              <parse_done nodes='235' memory='56400' stamp='2.315' live='228'/>",
				"            </parse>",
				"            <parse_done nodes='235' memory='56528' stamp='2.315' live='227'/>",
				"          </parse>",
				"          <parse_done nodes='238' memory='57432' stamp='2.315' live='229'/>",
				"        </parse>",
				"        <parse_done nodes='240' memory='58392' stamp='2.315' live='230'/>",
				"      </parse>",
				"      <bc code='182' bci='11'/>",
				"      <method level='4' bytes='22' name='get' flags='1' holder='823' id='832' compile_id='180' compiler='C2' iicount='615816' return='716'/>",
				"      <call method='832' inline='1' count='601468' prof_factor='1'/>",
				"      <klass unloaded='1' name='java/util/NoSuchElementException' id='846'/>",
				"      <uncommon_trap reason='unloaded' method='832' klass='846' bci='7' action='reinterpret' index='9'/>",
				"      <inline_success reason='inline (hot)'/>",
				"      <parse method='832' stamp='2.315' uses='601468'> <!-- java.lang.Object java.util.Optional.get() -->",
				"        <bc code='199' bci='4'/>",
				"        <branch prob='always' not_taken='0' taken='615816' cnt='615816' target_bci='17'/>",
				"        <uncommon_trap reason='unstable_if' bci='4' action='reinterpret' comment='taken always'/>",
				"        <parse_done nodes='272' memory='68416' stamp='2.315' live='260'/>",
				"      </parse>",
				"      <bc code='192' bci='14'/>",
				"      <uncommon_trap reason='class_check' bci='14' action='maybe_recompile'/>",
				"      <bc code='162' bci='4'/>",
				"      <branch prob='1.6626e-06' not_taken='601468' taken='1' cnt='601467' target_bci='26'/>",
				"      <bc code='183' bci='33'/>",
				"      <klass name='java/lang/StringBuilder' flags='17' id='782'/>",
				"      <method level='3' bytes='7' name='&lt;init&gt;' flags='1' holder='782' id='825' compile_id='169' compiler='C1' iicount='569' return='711'/>",
				"      <call method='825' inline='1' count='1' prof_factor='1'/>",
				"      <inline_success reason='inline (hot)'/>",
				"      <direct_call bci='33'/>",
				"      <bc code='182' bci='37'/>",
				"      <method bytes='8' name='append' flags='1' holder='782' arguments='709' id='826' iicount='113' return='782'/>",
				"      <call method='826' inline='1' count='1' prof_factor='1'/>",
				"      <inline_fail reason='executed &lt; MinInliningThreshold times'/>",
				"      <direct_call bci='37'/>",
				"      <bc code='182' bci='42'/>",
				"      <klass name='java/lang/String' flags='17' id='717'/>",
				"      <method level='3' bytes='8' name='append' flags='1' holder='782' arguments='717' id='828' compile_id='13' compiler='C1' iicount='1406' return='782'/>",
				"      <call method='828' inline='1' count='1' prof_factor='1'/>",
				"      <inline_success reason='inline (hot)'/>",
				"      <direct_call bci='42'/>",
				"      <uncommon_trap reason='null_check' bci='42' action='maybe_recompile'/>",
				"      <bc code='182' bci='48'/>",
				"      <call method='828' inline='1' count='1' prof_factor='1'/>",
				"      <inline_success reason='inline (hot)'/>",
				"      <direct_call bci='48'/>",
				"      <uncommon_trap reason='null_check' bci='48' action='maybe_recompile'/>",
				"      <bc code='182' bci='51'/>",
				"      <method level='3' bytes='17' name='toString' flags='1' holder='782' id='829' compile_id='88' compiler='C1' iicount='690' return='717'/>",
				"      <call method='829' inline='1' count='1' prof_factor='1'/>",
				"      <inline_success reason='inline (hot)'/>",
				"      <direct_call bci='51'/>",
				"      <uncommon_trap reason='null_check' bci='51' action='maybe_recompile'/>",
				"      <bc code='182' bci='54'/>",
				"      <klass name='java/io/PrintStream' flags='1' id='824'/>",
				"      <method bytes='24' name='println' flags='1' holder='824' arguments='717' id='830' iicount='2' return='711'/>",
				"      <dependency x='830' ctxk='824' type='unique_concrete_method'/>",
				"      <call method='830' inline='1' count='1' prof_factor='1'/>",
				"      <inline_fail reason='executed &lt; MinInliningThreshold times'/>",
				"      <direct_call bci='54'/>",
				"      <uncommon_trap reason='null_check' bci='54' action='maybe_recompile'/>",
				"      <parse_done nodes='517' memory='116920' stamp='2.316' live='499'/>",
				"    </parse>",
				"    <replace_string_concat multiple='0' arguments='3' string_alloc='0'>",
				"      <jvms method='821' bci='29'/>",
				"    </replace_string_concat>",
				"    <uncommon_trap reason='predicate' bci='29' action='maybe_recompile'/>",
				"    <uncommon_trap reason='loop_limit_check' bci='29' action='maybe_recompile'/>",
				"    <uncommon_trap reason='intrinsic' bci='29' action='make_not_entrant'/>",
				"    <uncommon_trap reason='predicate' bci='29' action='maybe_recompile'/>",
				"    <uncommon_trap reason='loop_limit_check' bci='29' action='maybe_recompile'/>",
				"    <phase_done nodes='879' name='parse' stamp='2.316' live='454'/>",
				"  </phase>",
				"  <phase nodes='879' name='optimizer' stamp='2.316' live='454'>",
				"    <phase nodes='884' name='idealLoop' stamp='2.316' live='432'>",
				"      <loop_tree>",
				"        <loop idx='892'>",
				"        </loop>",
				"        <loop idx='885' inner_loop='1'>",
				"        </loop>",
				"        <loop idx='886' inner_loop='1'>",
				"        </loop>",
				"      </loop_tree>",
				"      <phase_done nodes='893' name='idealLoop' stamp='2.317' live='430'/>",
				"    </phase>",
				"    <phase nodes='893' name='escapeAnalysis' stamp='2.317' live='430'>",
				"      <phase nodes='894' name='connectionGraph' stamp='2.317' live='431'>",
				"        <phase_done nodes='894' name='connectionGraph' stamp='2.317' live='431'/>",
				"      </phase>",
				"      <phase_done nodes='897' name='escapeAnalysis' stamp='2.317' live='434'/>",
				"    </phase>",
				"    <eliminate_allocation type='823'>",
				"      <jvms method='835' bci='0'/>",
				"      <jvms method='833' bci='11'/>",
				"      <jvms method='831' bci='4'/>",
				"      <jvms method='821' bci='8'/>",
				"    </eliminate_allocation>",
				"    <phase nodes='898' name='idealLoop' stamp='2.317' live='383'>",
				"      <loop_tree>",
				"        <loop idx='892' inner_loop='1'>",
				"        </loop>",
				"        <loop idx='885' inner_loop='1'>",
				"        </loop>",
				"        <loop idx='886' inner_loop='1'>",
				"        </loop>",
				"      </loop_tree>",
				"      <phase_done nodes='923' name='idealLoop' stamp='2.317' live='385'/>",
				"    </phase>",
				"    <phase nodes='923' name='idealLoop' stamp='2.317' live='385'>",
				"      <loop_tree>",
				"        <loop idx='1044' main_loop='1044' inner_loop='1'>",
				"        </loop>",
				"        <loop idx='885' inner_loop='1'>",
				"        </loop>",
				"        <loop idx='886' inner_loop='1'>",
				"        </loop>",
				"      </loop_tree>",
				"      <phase_done nodes='1070' name='idealLoop' stamp='2.318' live='506'/>",
				"    </phase>",
				"    <phase nodes='1070' name='idealLoop' stamp='2.318' live='506'>",
				"      <loop_tree>",
				"        <loop pre_loop='892' idx='989' inner_loop='1'>",
				"        </loop>",
				"        <loop idx='1104' main_loop='1104' inner_loop='1'>",
				"        </loop>",
				"        <loop idx='945' post_loop='892' inner_loop='1'>",
				"        </loop>",
				"        <loop idx='885' inner_loop='1'>",
				"        </loop>",
				"        <loop idx='886' inner_loop='1'>",
				"        </loop>",
				"      </loop_tree>",
				"      <phase_done nodes='1140' name='idealLoop' stamp='2.318' live='560'/>",
				"    </phase>",
				"    <phase nodes='1140' name='ccp' stamp='2.318' live='560'>",
				"      <phase_done nodes='1140' name='ccp' stamp='2.318' live='560'/>",
				"    </phase>",
				"    <phase nodes='1142' name='idealLoop' stamp='2.318' live='559'>",
				"      <loop_tree>",
				"        <loop pre_loop='892' idx='989' inner_loop='1'>",
				"        </loop>",
				"        <loop idx='1104' main_loop='1104' inner_loop='1'>",
				"        </loop>",
				"        <loop idx='945' post_loop='892' inner_loop='1'>",
				"        </loop>",
				"        <loop idx='885' inner_loop='1'>",
				"        </loop>",
				"        <loop idx='886' inner_loop='1'>",
				"        </loop>",
				"      </loop_tree>",
				"      <phase_done nodes='1174' name='idealLoop' stamp='2.318' live='505'/>",
				"    </phase>",
				"    <phase nodes='1174' name='idealLoop' stamp='2.318' live='505'>",
				"      <loop_tree>",
				"        <loop pre_loop='892' idx='989' inner_loop='1'>",
				"        </loop>",
				"        <loop idx='1104' main_loop='1104' inner_loop='1'>",
				"        </loop>",
				"        <loop idx='945' post_loop='892' inner_loop='1'>",
				"        </loop>",
				"        <loop idx='885' inner_loop='1'>",
				"        </loop>",
				"        <loop idx='886' inner_loop='1'>",
				"        </loop>",
				"      </loop_tree>",
				"      <phase_done nodes='1207' name='idealLoop' stamp='2.319' live='491'/>",
				"    </phase>",
				"    <phase_done nodes='1345' name='optimizer' stamp='2.319' live='549'/>",
				"  </phase>",
				"  <phase nodes='1345' name='matcher' stamp='2.319' live='549'>",
				"    <phase_done nodes='487' name='matcher' stamp='2.319' live='487'/>",
				"  </phase>",
				"  <phase nodes='605' name='regalloc' stamp='2.320' live='604'>",
				"    <regalloc success='1' attempts='2'/>",
				"    <phase_done nodes='759' name='regalloc' stamp='2.322' live='683'/>",
				"  </phase>",
				"  <phase nodes='759' name='output' stamp='2.322' live='683'>",
				"    <phase_done nodes='789' name='output' stamp='2.322' live='695'/>",
				"  </phase>",
				"  <dependency x='831' ctxk='820' type='unique_concrete_method'/>",
				"  <dependency x='830' ctxk='824' type='unique_concrete_method'/>",
				"  <code_cache nmethods='186' free_code_cache='250226176' adapters='167' total_blobs='440' stamp='2.315'/>",
				"  <task_done inlined_bytes='82' success='1' count='2' backedge_count='677167' stamp='2.322' nmsize='1256'/>",
				"</task>"
		};
		
		String[] bytecodeLines = new String[]{
				"         0: iconst_0",
				"         1: istore_2",
				"         2: iload_2",
				"         3: iload_1",
				"         4: if_icmpge     26",
				"         7: aload_0",
				"         8: invokevirtual #18                 // Method org.adoptopenjdk.jitwatch.test.examples.OptionalTest$Name.getOptionalName:()Ljava/util/Optional;",
				"        11: invokevirtual #19                 // Method java/util/Optional.get:()Ljava/lang/Object;",
				"        14: checkcast     #20                 // class java/lang/String",
				"        17: putstatic     #21                 // Field NAME:Ljava/lang/String;",
				"        20: iinc          2, 1",
				"        23: goto          2",
				"        26: getstatic     #13                 // Field java/lang/System.out:Ljava/io/PrintStream;",
				"        29: new           #22                 // class java/lang/StringBuilder",
				"        32: dup",
				"        33: invokespecial #23                 // Method java/lang/StringBuilder.&quote<init>&quote:()V",
				"        36: iload_1",
				"        37: invokevirtual #24                 // Method java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;",
				"        40: ldc           #25                 // String  optional iterations",
				"        42: invokevirtual #26                 // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;",
				"        45: getstatic     #21                 // Field NAME:Ljava/lang/String;",
				"        48: invokevirtual #26                 // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;",
				"        51: invokevirtual #27                 // Method java/lang/StringBuilder.toString:()Ljava/lang/String;",
				"        54: invokevirtual #15                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V",
				"        57: return"
		};
		
		IMetaMember member = UnitTestUtil.createTestMetaMember("org.adoptopenjdk.jitwatch.test.examples.OptionalTest", "countOptional", new Class[]{Class.forName("org.adoptopenjdk.jitwatch.test.examples.OptionalTest$Name"), int.class}, void.class);

		BytecodeAnnotations result = buildAnnotations("1.8.0", member, logLines, bytecodeLines);
		
		assertEquals(11, result.annotatedLineCount());

		checkLine(result, 4, "taken", BCAnnotationType.BRANCH);
		checkLine(result, 7, "predicate", BCAnnotationType.UNCOMMON_TRAP);
		checkLine(result, 8, "(hot)", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 11, "(hot)", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 14, "class_check", BCAnnotationType.UNCOMMON_TRAP);
		checkLine(result, 33, "(hot)", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 37, "MinInliningThreshold", BCAnnotationType.INLINE_FAIL);
		checkLine(result, 42, "(hot)", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 42, "null_check", BCAnnotationType.UNCOMMON_TRAP);
		checkLine(result, 48, "(hot)", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 48, "null_check", BCAnnotationType.UNCOMMON_TRAP);
		checkLine(result, 51, "(hot)", BCAnnotationType.INLINE_SUCCESS);
		checkLine(result, 51, "null_check", BCAnnotationType.UNCOMMON_TRAP);
		checkLine(result, 54, "MinInliningThreshold", BCAnnotationType.INLINE_FAIL);
		checkLine(result, 54, "null_check", BCAnnotationType.UNCOMMON_TRAP);
	}	
	
	*/
	
}