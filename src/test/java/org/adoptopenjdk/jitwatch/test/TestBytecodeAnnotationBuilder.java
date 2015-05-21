/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_ID;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_METHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_NAME;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_RETURN;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_DOT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SLASH;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_CLOSE_ANGLE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_OPEN_ANGLE;
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

import javafx.scene.paint.Color;

import org.adoptopenjdk.jitwatch.core.TagProcessor;
import org.adoptopenjdk.jitwatch.journal.JournalUtil;
import org.adoptopenjdk.jitwatch.loader.BytecodeLoader;
import org.adoptopenjdk.jitwatch.model.AnnotationException;
import org.adoptopenjdk.jitwatch.model.CompilerName;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IParseDictionary;
import org.adoptopenjdk.jitwatch.model.LineAnnotation;
import org.adoptopenjdk.jitwatch.model.ParseDictionary;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeAnnotationBuilder;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;
import org.junit.Test;

public class TestBytecodeAnnotationBuilder
{
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
		String[] logLines = new String[]{
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
				"</task>"
		};

		String[] bytecodeLines = new String[]{
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
				"65: return          "
		};

		IMetaMember member = UnitTestUtil.createTestMetaMember("org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog", "testLeaf", new Class[]{long.class});

		Map<Integer, LineAnnotation> result = buildAnnotations(member, CompilerName.C2, logLines, bytecodeLines);

		assertEquals(9, result.size());

		checkLine(result, 10, "inline (hot)", Color.GREEN);
		checkLine(result, 16, "inline (hot)", Color.GREEN);
		checkLine(result, 22, "inline (hot)", Color.GREEN);
		checkLine(result, 28, "inline (hot)", Color.GREEN);
		checkLine(result, 40, "always", Color.BLUE);
		checkLine(result, 52, "not reached", Color.RED);
		checkLine(result, 56, "MinInliningThreshold", Color.RED);
		checkLine(result, 59, "MinInliningThreshold", Color.RED);
		checkLine(result, 62, "MinInliningThreshold", Color.RED);
	}

	@Test
	public void testJava7NonTieredChain()
	{
		String[] logLines = new String[]{
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
				"</task>"
		};

		String[] bytecodeLines = new String[]{
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
				"53: return          "
		};

		IMetaMember member = UnitTestUtil.createTestMetaMember("org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog", "testCallChain", new Class[]{long.class});

		Map<Integer, LineAnnotation> result = buildAnnotations(member, CompilerName.C2, logLines, bytecodeLines);

		assertEquals(7, result.size());

		checkLine(result, 10, "inline (hot)", Color.GREEN);
		checkLine(result, 16, "inline (hot)", Color.GREEN);
		checkLine(result, 28, "always", Color.BLUE);
		checkLine(result, 40, "not reached", Color.RED);
		checkLine(result, 44, "MinInliningThreshold", Color.RED);
		checkLine(result, 47, "MinInliningThreshold", Color.RED);
		checkLine(result, 50, "MinInliningThreshold", Color.RED);
	}

	@Test
	public void testJava7TieredLeaf()
	{
		String[] logLines = new String[]{
				"<task compile_id='153' compile_kind='osr' method='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog testLeaf (J)V' bytes='66' count='1' backedge_count='60509' iicount='1' osr_bci='8' level='3' stamp='12.700'>",
				"<phase name='buildIR' stamp='12.700'>",
				"<type id='636' name='void'/>",
				"<type id='635' name='long'/>",
				"<klass id='729' name='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog' flags='1'/>",
				"<method id='730' holder='729' name='testLeaf' return='636' arguments='635' flags='2' bytes='66' iicount='1'/>",
				"<parse method='730'  stamp='12.700'>",
				"<bc code='183' bci='52'/>",
				"<klass id='646' name='java/lang/String' flags='17'/>",
				"<klass id='704' name='java/lang/StringBuilder' flags='17'/>",
				"<method id='735' holder='704' name='&lt;init&gt;' return='636' arguments='646' flags='1' bytes='18' iicount='9'/>",
				"<call method='735' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='182' bci='2'/>",
				"<type id='634' name='int'/>",
				"<method id='737' holder='646' name='length' return='634' flags='1' bytes='6' compile_id='6' compiler='C1' level='3' iicount='541'/>",
				"<call method='737' instr='invokevirtual'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='8'/>",
				"<klass id='702' name='java/lang/AbstractStringBuilder' flags='1024'/>",
				"<method id='739' holder='702' name='&lt;init&gt;' return='636' arguments='634' flags='0' bytes='12' iicount='101'/>",
				"<call method='739' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='1'/>",
				"<klass id='645' name='java/lang/Object' flags='1'/>",
				"<method id='741' holder='645' name='&lt;init&gt;' return='636' flags='1' bytes='1' compile_id='46' compiler='C1' level='1' iicount='20694'/>",
				"<call method='741' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='182' bci='13'/>",
				"<method id='743' holder='704' name='append' return='704' arguments='646' flags='1' bytes='8' iicount='163'/>",
				"<call method='743' instr='invokevirtual'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='2'/>",
				"<method id='745' holder='702' name='append' return='702' arguments='646' flags='1' bytes='48' iicount='187'/>",
				"<call method='745' instr='invokespecial'/>",
				"<inline_fail reason='callee is too large'/>",
				"<bc code='182' bci='56'/>",
				"<method id='747' holder='704' name='append' return='704' arguments='635' flags='1' bytes='8' iicount='9'/>",
				"<call method='747' instr='invokevirtual'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='2'/>",
				"<method id='749' holder='702' name='append' return='702' arguments='635' flags='1' bytes='70' iicount='9'/>",
				"<call method='749' instr='invokespecial'/>",
				"<inline_fail reason='callee is too large'/>",
				"<bc code='182' bci='59'/>",
				"<method id='751' holder='704' name='toString' return='646' flags='1' bytes='17' iicount='90'/>",
				"<call method='751' instr='invokevirtual'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='13'/>",
				"<klass id='720' name='[C' flags='1041'/>",
				"<method id='753' holder='646' name='&lt;init&gt;' return='636' arguments='720 634 634' flags='1' bytes='67' iicount='215'/>",
				"<call method='753' instr='invokespecial'/>",
				"<inline_fail reason='callee is too large'/>",
				"<bc code='182' bci='62'/>",
				"<klass id='732' name='java/io/PrintStream' flags='1'/>",
				"<method id='755' holder='732' name='println' return='636' arguments='646' flags='1' bytes='24' iicount='9'/>",
				"<call method='755' instr='invokevirtual'/>",
				"<dependency type='leaf_type' ctxk='732'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='182' bci='6'/>",
				"<method id='757' holder='732' name='print' return='636' arguments='646' flags='1' bytes='13' iicount='9'/>",
				"<call method='757' instr='invokevirtual'/>",
				"<dependency type='leaf_type' ctxk='732'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='9'/>",
				"<method id='759' holder='732' name='write' return='636' arguments='646' flags='2' bytes='83' iicount='9'/>",
				"<call method='759' instr='invokespecial'/>",
				"<dependency type='leaf_type' ctxk='732'/>",
				"<inline_fail reason='callee is too large'/>",
				"<bc code='183' bci='10'/>",
				"<method id='763' holder='732' name='newLine' return='636' flags='2' bytes='73' iicount='9'/>",
				"<call method='763' instr='invokespecial'/>",
				"<dependency type='leaf_type' ctxk='732'/>",
				"<inline_fail reason='callee is too large'/>",
				"<bc code='183' bci='10'/>",
				"<method id='766' holder='729' name='leaf1' return='635' arguments='635' flags='2' bytes='4' compile_id='149' compiler='C1' level='1' iicount='1962'/>",
				"<call method='766' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='16'/>",
				"<method id='768' holder='729' name='leaf2' return='635' arguments='635' flags='2' bytes='6' compile_id='150' compiler='C1' level='1' iicount='2685'/>",
				"<call method='768' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='22'/>",
				"<method id='770' holder='729' name='leaf3' return='635' arguments='635' flags='2' bytes='6' compile_id='151' compiler='C1' level='1' iicount='9780'/>",
				"<call method='770' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='28'/>",
				"<method id='772' holder='729' name='leaf4' return='635' arguments='635' flags='2' bytes='6' compile_id='152' compiler='C1' level='1' iicount='11340'/>",
				"<call method='772' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<parse_done stamp='12.703'/>",
				"</parse>",
				"<phase name='optimizeIR' stamp='12.703'>",
				"<phase_done stamp='12.703'/>",
				"</phase>",
				"<phase_done stamp='12.703'/>",
				"</phase>",
				"<phase name='emit_lir' stamp='12.703'>",
				"<phase name='lirGeneration' stamp='12.703'>",
				"<phase_done stamp='12.704'/>",
				"</phase>",
				"<phase name='linearScan' stamp='12.704'>",
				"<phase_done stamp='12.705'/>",
				"</phase>",
				"<phase_done stamp='12.705'/>",
				"</phase>",
				"<phase name='codeemit' stamp='12.705'>",
				"<phase_done stamp='12.706'/>",
				"</phase>",
				"<phase name='codeinstall' stamp='12.706'>",
				"<dependency type='leaf_type' ctxk='732'/>",
				"<phase_done stamp='12.706'/>",
				"</phase>",
				"<code_cache total_blobs='365' nmethods='153' adapters='134' free_code_cache='99240832' largest_free_block='99229888'/>",
				"<task_done success='1' nmsize='3560' count='1' backedge_count='70671' inlined_bytes='129' stamp='12.706'/>",
				"</task>"
		};

		String[] bytecodeLines = new String[]{
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
				"65: return          "
		};

		IMetaMember member = UnitTestUtil.createTestMetaMember("org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog", "testLeaf", new Class[]{long.class});

		Map<Integer, LineAnnotation> result = buildAnnotations(member, CompilerName.C1, logLines, bytecodeLines);

		assertEquals(8, result.size());

		int bcOffsetStringBuilderInit = 52;
		int bcOffsetMakeHotSpotLogLeaf1 = 10;
		int bcOffsetMakeHotSpotLogLeaf2 = 16;
		int bcOffsetMakeHotSpotLogLeaf3 = 22;
		int bcOffsetMakeHotSpotLogLeaf4 = 28;
		int bcOffsetStringBuilderAppend = 56;
		int bcOffsetStringBuilderToString = 59;
		int bcOffsetPrintStreamPrintln = 62;

		checkLine(result, bcOffsetStringBuilderInit, "Inlined: Yes", Color.GREEN);
		checkLine(result, bcOffsetMakeHotSpotLogLeaf1, "Inlined: Yes", Color.GREEN);
		checkLine(result, bcOffsetMakeHotSpotLogLeaf2, "Inlined: Yes", Color.GREEN);
		checkLine(result, bcOffsetMakeHotSpotLogLeaf3, "Inlined: Yes", Color.GREEN);
		checkLine(result, bcOffsetMakeHotSpotLogLeaf4, "Inlined: Yes", Color.GREEN);
		checkLine(result, bcOffsetStringBuilderAppend, "Inlined: Yes", Color.GREEN);
		checkLine(result, bcOffsetStringBuilderToString, "Inlined: Yes", Color.GREEN);
		checkLine(result, bcOffsetPrintStreamPrintln, "Inlined: Yes", Color.GREEN);
	}

	@Test
	public void testJava7TieredChain()
	{
		String[] logLines = new String[]{
				"<task compile_id='133' compile_kind='osr' method='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog testCallChain (J)V' bytes='54' count='1' backedge_count='60635' iicount='1' osr_bci='8' level='3' stamp='12.538'>",
				"<phase name='buildIR' stamp='12.538'>",
				"<type id='636' name='void'/>",
				"<type id='635' name='long'/>",
				"<klass id='729' name='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog' flags='1'/>",
				"<method id='730' holder='729' name='testCallChain' return='636' arguments='635' flags='2' bytes='54' iicount='1'/>",
				"<parse method='730'  stamp='12.538'>",
				"<bc code='183' bci='40'/>",
				"<klass id='646' name='java/lang/String' flags='17'/>",
				"<klass id='704' name='java/lang/StringBuilder' flags='17'/>",
				"<method id='735' holder='704' name='&lt;init&gt;' return='636' arguments='646' flags='1' bytes='18' iicount='7'/>",
				"<call method='735' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='182' bci='2'/>",
				"<type id='634' name='int'/>",
				"<method id='737' holder='646' name='length' return='634' flags='1' bytes='6' compile_id='6' compiler='C1' level='3' iicount='533'/>",
				"<call method='737' instr='invokevirtual'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='8'/>",
				"<klass id='702' name='java/lang/AbstractStringBuilder' flags='1024'/>",
				"<method id='739' holder='702' name='&lt;init&gt;' return='636' arguments='634' flags='0' bytes='12' iicount='99'/>",
				"<call method='739' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='1'/>",
				"<klass id='645' name='java/lang/Object' flags='1'/>",
				"<method id='741' holder='645' name='&lt;init&gt;' return='636' flags='1' bytes='1' compile_id='46' compiler='C1' level='1' iicount='20694'/>",
				"<call method='741' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='182' bci='13'/>",
				"<method id='743' holder='704' name='append' return='704' arguments='646' flags='1' bytes='8' iicount='161'/>",
				"<call method='743' instr='invokevirtual'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='2'/>",
				"<method id='745' holder='702' name='append' return='702' arguments='646' flags='1' bytes='48' iicount='185'/>",
				"<call method='745' instr='invokespecial'/>",
				"<inline_fail reason='callee is too large'/>",
				"<bc code='182' bci='44'/>",
				"<method id='747' holder='704' name='append' return='704' arguments='635' flags='1' bytes='8' iicount='7'/>",
				"<call method='747' instr='invokevirtual'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='2'/>",
				"<method id='749' holder='702' name='append' return='702' arguments='635' flags='1' bytes='70' iicount='7'/>",
				"<call method='749' instr='invokespecial'/>",
				"<inline_fail reason='callee is too large'/>",
				"<bc code='182' bci='47'/>",
				"<method id='751' holder='704' name='toString' return='646' flags='1' bytes='17' iicount='88'/>",
				"<call method='751' instr='invokevirtual'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='13'/>",
				"<klass id='720' name='[C' flags='1041'/>",
				"<method id='753' holder='646' name='&lt;init&gt;' return='636' arguments='720 634 634' flags='1' bytes='67' iicount='213'/>",
				"<call method='753' instr='invokespecial'/>",
				"<inline_fail reason='callee is too large'/>",
				"<bc code='182' bci='50'/>",
				"<klass id='732' name='java/io/PrintStream' flags='1'/>",
				"<method id='755' holder='732' name='println' return='636' arguments='646' flags='1' bytes='24' iicount='7'/>",
				"<call method='755' instr='invokevirtual'/>",
				"<dependency type='leaf_type' ctxk='732'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='182' bci='6'/>",
				"<method id='757' holder='732' name='print' return='636' arguments='646' flags='1' bytes='13' iicount='7'/>",
				"<call method='757' instr='invokevirtual'/>",
				"<dependency type='leaf_type' ctxk='732'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='9'/>",
				"<method id='759' holder='732' name='write' return='636' arguments='646' flags='2' bytes='83' iicount='7'/>",
				"<call method='759' instr='invokespecial'/>",
				"<dependency type='leaf_type' ctxk='732'/>",
				"<inline_fail reason='callee is too large'/>",
				"<bc code='183' bci='10'/>",
				"<method id='763' holder='732' name='newLine' return='636' flags='2' bytes='73' iicount='7'/>",
				"<call method='763' instr='invokespecial'/>",
				"<dependency type='leaf_type' ctxk='732'/>",
				"<inline_fail reason='callee is too large'/>",
				"<bc code='183' bci='10'/>",
				"<method id='766' holder='729' name='chainA1' return='635' arguments='635' flags='2' bytes='8' compile_id='131' compiler='C2' level='4' iicount='11516'/>",
				"<call method='766' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='3'/>",
				"<method id='768' holder='729' name='chainA2' return='635' arguments='635' flags='2' bytes='10' iicount='11516'/>",
				"<call method='768' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='5'/>",
				"<method id='770' holder='729' name='chainA3' return='635' arguments='635' flags='2' bytes='10' iicount='11516'/>",
				"<call method='770' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='5'/>",
				"<method id='772' holder='729' name='chainA4' return='635' arguments='635' flags='2' bytes='7' iicount='11516'/>",
				"<call method='772' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='3'/>",
				"<method id='774' holder='729' name='bigMethod' return='635' arguments='635 634' flags='2' bytes='350' compile_id='41' compiler='C2' level='4' iicount='6537'/>",
				"<call method='774' instr='invokespecial'/>",
				"<inline_fail reason='callee is too large'/>",
				"<bc code='183' bci='16'/>",
				"<method id='776' holder='729' name='chainB1' return='635' arguments='635' flags='2' bytes='8' compile_id='132' compiler='C2' level='4' iicount='16492'/>",
				"<call method='776' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='2'/>",
				"<method id='778' holder='729' name='chainB2' return='635' arguments='635' flags='2' bytes='10' iicount='16492'/>",
				"<call method='778' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='2'/>",
				"<method id='780' holder='729' name='chainB3' return='635' arguments='635' flags='2' bytes='6' iicount='16492'/>",
				"<call method='780' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<parse_done stamp='12.542'/>",
				"</parse>",
				"<phase name='optimizeIR' stamp='12.542'>",
				"<phase_done stamp='12.542'/>",
				"</phase>",
				"<phase_done stamp='12.542'/>",
				"</phase>",
				"<phase name='emit_lir' stamp='12.542'>",
				"<phase name='lirGeneration' stamp='12.542'>",
				"<phase_done stamp='12.543'/>",
				"</phase>",
				"<phase name='linearScan' stamp='12.543'>",
				"<phase_done stamp='12.544'/>",
				"</phase>",
				"<phase_done stamp='12.544'/>",
				"</phase>",
				"<phase name='codeemit' stamp='12.544'>",
				"<phase_done stamp='12.545'/>",
				"</phase>",
				"<phase name='codeinstall' stamp='12.545'>",
				"<dependency type='leaf_type' ctxk='732'/>",
				"<phase_done stamp='12.545'/>",
				"</phase>",
				"<code_cache total_blobs='345' nmethods='133' adapters='134' free_code_cache='99281280' largest_free_block='99255360'/>",
				"<task_done success='1' nmsize='3960' count='1' backedge_count='82045' inlined_bytes='166' stamp='12.545'/>",
				"</task>"
		};

		String[] bytecodeLines = new String[]{
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
				"53: return          "
		};

		IMetaMember member = UnitTestUtil.createTestMetaMember("org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog", "testCallChain", new Class[]{long.class});

		Map<Integer, LineAnnotation> result = buildAnnotations(member, CompilerName.C1, logLines, bytecodeLines);

		assertEquals(6, result.size());

		checkLine(result, 10, "Inlined: Yes", Color.GREEN);
		checkLine(result, 16, "Inlined: Yes", Color.GREEN);
		checkLine(result, 40, "Inlined: Yes", Color.GREEN);
		checkLine(result, 44, "Inlined: Yes", Color.GREEN);
		checkLine(result, 47, "Inlined: Yes", Color.GREEN);
		checkLine(result, 50, "Inlined: Yes", Color.GREEN);
	}

	@Test
	public void testJava8NonTieredLeaf()
	{
		String[] logLines = new String[]{
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
				"</task>"
			};

		String[] bytecodeLines = new String[]{
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
				"68: return          "
				};

		IMetaMember member = UnitTestUtil.createTestMetaMember("org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog", "testLeaf", new Class[]{long.class});

		Map<Integer, LineAnnotation> result = buildAnnotations(member, CompilerName.C2, logLines, bytecodeLines);

		assertEquals(10, result.size());

		checkLine(result, 10, "never", Color.BLUE);
		checkLine(result, 15, "inline (hot)", Color.GREEN);
		checkLine(result, 21, "inline (hot)", Color.GREEN);
		checkLine(result, 27, "inline (hot)", Color.GREEN);
		checkLine(result, 33, "inline (hot)", Color.GREEN);
		checkLine(result, 50, "not reached", Color.RED);
		checkLine(result, 55, "MinInliningThreshold", Color.RED);
		checkLine(result, 59, "MinInliningThreshold", Color.RED);
		checkLine(result, 62, "MinInliningThreshold", Color.RED);
		checkLine(result, 65, "MinInliningThreshold", Color.RED);
	}

	/*
	@Test
	public void testJava8NonTieredChain()
	{
		String[] logLines = new String[]{
				"<task compile_id='73' compile_kind='osr' method='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog testCallChain2 (J)V' bytes='57' count='10000' backedge_count='5171' iicount='1' osr_bci='5' stamp='11.507'>",
				"<phase name='parse' nodes='3' live='3' stamp='11.507'>",
				"<type id='680' name='void'/>",
				"<type id='679' name='long'/>",
				"<klass id='776' name='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog' flags='1'/>",
				"<method id='777' holder='776' name='testCallChain2' return='680' arguments='679' flags='2' bytes='57' iicount='1'/>",
				"<parse method='777' uses='1' osr_bci='5' stamp='11.508'>",
				"<dependency type='leaf_type' ctxk='776'/>",
				"<dependency type='leaf_type' ctxk='776'/>",
				"<uncommon_trap bci='5' reason='constraint' action='reinterpret'/>",
				"<uncommon_trap bci='5' reason='predicate' action='maybe_recompile'/>",
				"<uncommon_trap bci='5' reason='loop_limit_check' action='maybe_recompile'/>",
				"<bc code='156' bci='10'/>",
				"<branch target_bci='31' taken='0' not_taken='14038' cnt='14038' prob='never'/>",
				"<bc code='183' bci='15'/>",
				"<method id='786' holder='776' name='chainC1' return='679' arguments='679' flags='2' bytes='14' compile_id='71' compiler='C2' iicount='12250'/>",
				"<call method='786' count='14038' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='786' uses='14038' stamp='11.509'>",
				"<uncommon_trap bci='15' reason='null_check' action='maybe_recompile'/>",
				"<bc code='183' bci='3'/>",
				"<method id='787' holder='776' name='chainC2' return='679' arguments='679' flags='2' bytes='6' compile_id='70' compiler='C2' iicount='11615'/>",
				"<call method='787' count='8951' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='787' uses='8951' stamp='11.509'>",
				"<parse_done nodes='165' live='160' memory='45488' stamp='11.509'/>",
				"</parse>",
				"<bc code='183' bci='10'/>",
				"<method id='788' holder='776' name='chainC3' return='679' arguments='679' flags='2' bytes='6' compile_id='72' compiler='C2' iicount='12250'/>",
				"<call method='788' count='8951' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='788' uses='8951' stamp='11.509'>",
				"<parse_done nodes='182' live='176' memory='48192' stamp='11.509'/>",
				"</parse>",
				"<parse_done nodes='183' live='176' memory='49104' stamp='11.509'/>",
				"</parse>",
				"<bc code='183' bci='21'/>",
				"<call method='787' count='14037' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='787' uses='14037' stamp='11.509'>",
				"<parse_done nodes='200' live='192' memory='51352' stamp='11.509'/>",
				"</parse>",
				"<bc code='183' bci='38'/>",
				"<klass id='749' name='java/lang/StringBuilder' flags='17'/>",
				"<method id='780' holder='749' name='&lt;init&gt;' return='680' flags='1' bytes='7' iicount='113'/>",
				"<call method='780' count='0' prof_factor='1' inline='1'/>",
				"<inline_fail reason='call site not reached'/>",
				"<direct_call bci='38'/>",
				"<bc code='182' bci='43'/>",
				"<klass id='686' name='java/lang/String' flags='17'/>",
				"<method id='782' holder='749' name='append' return='749' arguments='686' flags='1' bytes='8' iicount='208'/>",
				"<call method='782' count='0' prof_factor='1' inline='1'/>",
				"<inline_fail reason='executed &lt; MinInliningThreshold times'/>",
				"<direct_call bci='43'/>",
				"<bc code='182' bci='47'/>",
				"<method id='783' holder='749' name='append' return='749' arguments='679' flags='1' bytes='8' iicount='8'/>",
				"<call method='783' count='0' prof_factor='1' inline='1'/>",
				"<inline_fail reason='executed &lt; MinInliningThreshold times'/>",
				"<direct_call bci='47'/>",
				"<uncommon_trap bci='47' reason='null_check' action='maybe_recompile'/>",
				"<bc code='182' bci='50'/>",
				"<method id='784' holder='749' name='toString' return='686' flags='1' bytes='17' iicount='112'/>",
				"<call method='784' count='0' prof_factor='1' inline='1'/>",
				"<inline_fail reason='executed &lt; MinInliningThreshold times'/>",
				"<direct_call bci='50'/>",
				"<uncommon_trap bci='50' reason='null_check' action='maybe_recompile'/>",
				"<bc code='182' bci='53'/>",
				"<klass id='779' name='java/io/PrintStream' flags='1'/>",
				"<method id='785' holder='779' name='println' return='680' arguments='686' flags='1' bytes='24' iicount='8'/>",
				"<dependency type='unique_concrete_method' ctxk='779' x='785'/>",
				"<call method='785' count='0' prof_factor='1' inline='1'/>",
				"<inline_fail reason='executed &lt; MinInliningThreshold times'/>",
				"<direct_call bci='53'/>",
				"<uncommon_trap bci='53' reason='null_check' action='maybe_recompile'/>",
				"<parse_done nodes='357' live='345' memory='81672' stamp='11.511'/>",
				"</parse>",
				"<phase_done name='parse' nodes='360' live='211' stamp='11.512'/>",
				"</phase>",
				"<phase name='optimizer' nodes='360' live='211' stamp='11.512'>",
				"<phase name='idealLoop' nodes='365' live='202' stamp='11.512'>",
				"<loop_tree>",
				"<loop idx='365' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='366' live='202' stamp='11.513'/>",
				"</phase>",
				"<phase name='escapeAnalysis' nodes='366' live='202' stamp='11.513'>",
				"<phase name='connectionGraph' nodes='367' live='203' stamp='11.513'>",
				"<klass id='747' name='java/lang/AbstractStringBuilder' flags='1024'/>",
				"<type id='678' name='int'/>",
				"<method id='806' holder='747' name='expandCapacity' return='680' arguments='678' flags='0' bytes='50' iicount='156'/>",
				"<dependency type='unique_concrete_method' ctxk='747' x='806'/>",
				"<phase_done name='connectionGraph' nodes='367' live='203' stamp='11.515'/>",
				"</phase>",
				"<phase_done name='escapeAnalysis' nodes='367' live='203' stamp='11.515'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='367' live='203' stamp='11.515'>",
				"<loop_tree>",
				"<loop idx='365' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='392' live='215' stamp='11.516'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='392' live='215' stamp='11.516'>",
				"<loop_tree>",
				"<loop idx='365' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='392' live='196' stamp='11.516'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='392' live='196' stamp='11.516'>",
				"<loop_tree>",
				"<loop idx='365' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='392' live='196' stamp='11.516'/>",
				"</phase>",
				"<phase name='ccp' nodes='392' live='196' stamp='11.517'>",
				"<phase_done name='ccp' nodes='392' live='196' stamp='11.517'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='393' live='193' stamp='11.517'>",
				"<loop_tree>",
				"<loop idx='365' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='393' live='193' stamp='11.517'/>",
				"</phase>",
				"<phase_done name='optimizer' nodes='461' live='231' stamp='11.518'/>",
				"</phase>",
				"<phase name='matcher' nodes='461' live='231' stamp='11.518'>",
				"<phase_done name='matcher' nodes='214' live='214' stamp='11.519'/>",
				"</phase>",
				"<phase name='regalloc' nodes='272' live='272' stamp='11.520'>",
				"<regalloc attempts='1' success='1'/>",
				"<phase_done name='regalloc' nodes='306' live='304' stamp='11.525'/>",
				"</phase>",
				"<phase name='output' nodes='308' live='306' stamp='11.525'>",
				"<phase_done name='output' nodes='337' live='325' stamp='11.526'/>",
				"</phase>",
				"<dependency type='leaf_type' ctxk='776'/>",
				"<dependency type='unique_concrete_method' ctxk='779' x='785'/>",
				"<dependency type='unique_concrete_method' ctxk='747' x='806'/>",
				"<code_cache total_blobs='263' nmethods='73' adapters='142' free_code_cache='49729600'/>",
				"<task_done success='1' nmsize='640' count='10000' backedge_count='5906' inlined_bytes='32' stamp='11.526'/>",
				"</task>"
		};

		String[] bytecodeLines = new String[]{
				"0: lconst_0        ",
				"1: lstore_3        ",
				"2: iconst_0        ",
				"3: istore          5    ",
				"5: iload           5    ",
				"7: i2l             ",
				"8: lload_1         ",
				"9: lcmp            ",
				"10: ifge            31   ",
				"13: aload_0         ",
				"14: lload_3         ",
				"15: invokespecial   #58  // Method chainA1:(J)J",
				"18: lstore_3        ",
				"19: aload_0         ",
				"20: lload_3         ",
				"21: invokespecial   #59  // Method chainB1:(J)J",
				"24: lstore_3        ",
				"25: iinc            5, 1 ",
				"28: goto            5    ",
				"31: getstatic       #13  // Field java/lang/System.out:Ljava/io/PrintStream;",
				"34: new             #14  // class java/lang/StringBuilder",
				"37: dup             ",
				"38: invokespecial   #15  // Method java/lang/StringBuilder.\"<init>\":()V",
				"41: ldc             #60  // String testCallChain:",
				"43: invokevirtual   #17  // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;",
				"46: lload_3         ",
				"47: invokevirtual   #18  // Method java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;",
				"50: invokevirtual   #19  // Method java/lang/StringBuilder.toString:()Ljava/lang/String;",
				"53: invokevirtual   #20  // Method java/io/PrintStream.println:(Ljava/lang/String;)V",
				"56: return          "
		};

		IMetaMember member = UnitTestUtil.createTestMetaMember("org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog", "testCallChain2", new Class[]{long.class});

		Map<Integer, LineAnnotation> result = buildAnnotations(member, CompilerName.C2, logLines, bytecodeLines);

		assertEquals(8, result.size());

		checkLine(result, 10, "never", Color.BLUE);
		checkLine(result, 15, "inline (hot)", Color.GREEN);
		checkLine(result, 21, "inline (hot)", Color.GREEN);
		checkLine(result, 38, "not reached", Color.RED);
		checkLine(result, 43, "MinInliningThreshold", Color.RED);
		checkLine(result, 47, "MinInliningThreshold", Color.RED);
		checkLine(result, 50, "MinInliningThreshold", Color.RED);
		checkLine(result, 53, "MinInliningThreshold", Color.RED);
	}
	*/

	@Test
	public void testJava8TieredLeaf()
	{
		String[] logLines = new String[]{
				"<task compile_id='153' compile_kind='osr' method='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog testLeaf (J)V' bytes='69' count='1' backedge_count='60592' iicount='1' osr_bci='5' level='3' stamp='12.193'>",
				"<phase name='buildIR' stamp='12.193'>",
				"<type id='680' name='void'/>",
				"<type id='679' name='long'/>",
				"<klass id='776' name='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog' flags='1'/>",
				"<method id='777' holder='776' name='testLeaf' return='680' arguments='679' flags='2' bytes='69' iicount='1'/>",
				"<parse method='777'  stamp='12.193'>",
				"<bc code='183' bci='15'/>",
				"<method id='779' holder='776' name='leaf1' return='679' arguments='679' flags='2' bytes='4' compile_id='149' compiler='C1' level='1' iicount='6137'/>",
				"<call method='779' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='21'/>",
				"<method id='781' holder='776' name='leaf2' return='679' arguments='679' flags='2' bytes='6' compile_id='150' compiler='C1' level='1' iicount='5327'/>",
				"<call method='781' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='27'/>",
				"<method id='783' holder='776' name='leaf3' return='679' arguments='679' flags='2' bytes='6' compile_id='151' compiler='C1' level='1' iicount='10055'/>",
				"<call method='783' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='33'/>",
				"<method id='785' holder='776' name='leaf4' return='679' arguments='679' flags='2' bytes='6' compile_id='152' compiler='C1' level='1' iicount='11585'/>",
				"<call method='785' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='50'/>",
				"<klass id='749' name='java/lang/StringBuilder' flags='17'/>",
				"<method id='789' holder='749' name='&lt;init&gt;' return='680' flags='1' bytes='7' iicount='114'/>",
				"<call method='789' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='3'/>",
				"<type id='678' name='int'/>",
				"<klass id='747' name='java/lang/AbstractStringBuilder' flags='1024'/>",
				"<method id='791' holder='747' name='&lt;init&gt;' return='680' arguments='678' flags='0' bytes='12' iicount='127'/>",
				"<call method='791' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='1'/>",
				"<klass id='685' name='java/lang/Object' flags='1'/>",
				"<method id='793' holder='685' name='&lt;init&gt;' return='680' flags='1' bytes='1' compile_id='10' compiler='C1' level='1' iicount='447535'/>",
				"<call method='793' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='182' bci='55'/>",
				"<klass id='686' name='java/lang/String' flags='17'/>",
				"<method id='796' holder='749' name='append' return='749' arguments='686' flags='1' bytes='8' iicount='209'/>",
				"<call method='796' instr='invokevirtual'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='2'/>",
				"<method id='798' holder='747' name='append' return='747' arguments='686' flags='1' bytes='50' iicount='242'/>",
				"<call method='798' instr='invokespecial'/>",
				"<inline_fail reason='callee is too large'/>",
				"<bc code='182' bci='59'/>",
				"<method id='800' holder='749' name='append' return='749' arguments='679' flags='1' bytes='8' iicount='9'/>",
				"<call method='800' instr='invokevirtual'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='2'/>",
				"<method id='802' holder='747' name='append' return='747' arguments='679' flags='1' bytes='70' iicount='9'/>",
				"<call method='802' instr='invokespecial'/>",
				"<inline_fail reason='callee is too large'/>",
				"<bc code='182' bci='62'/>",
				"<method id='804' holder='749' name='toString' return='686' flags='1' bytes='17' iicount='113'/>",
				"<call method='804' instr='invokevirtual'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='13'/>",
				"<klass id='765' name='[C' flags='1041'/>",
				"<method id='806' holder='686' name='&lt;init&gt;' return='680' arguments='765 678 678' flags='1' bytes='62' iicount='262'/>",
				"<call method='806' instr='invokespecial'/>",
				"<inline_fail reason='callee is too large'/>",
				"<bc code='182' bci='65'/>",
				"<klass id='787' name='java/io/PrintStream' flags='1'/>",
				"<method id='808' holder='787' name='println' return='680' arguments='686' flags='1' bytes='24' iicount='9'/>",
				"<call method='808' instr='invokevirtual'/>",
				"<dependency type='leaf_type' ctxk='787'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='182' bci='6'/>",
				"<method id='810' holder='787' name='print' return='680' arguments='686' flags='1' bytes='13' iicount='9'/>",
				"<call method='810' instr='invokevirtual'/>",
				"<dependency type='leaf_type' ctxk='787'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='9'/>",
				"<method id='812' holder='787' name='write' return='680' arguments='686' flags='2' bytes='83' iicount='9'/>",
				"<call method='812' instr='invokespecial'/>",
				"<dependency type='leaf_type' ctxk='787'/>",
				"<inline_fail reason='callee is too large'/>",
				"<bc code='183' bci='10'/>",
				"<method id='816' holder='787' name='newLine' return='680' flags='2' bytes='73' iicount='9'/>",
				"<call method='816' instr='invokespecial'/>",
				"<dependency type='leaf_type' ctxk='787'/>",
				"<inline_fail reason='callee is too large'/>",
				"<parse_done stamp='12.196'/>",
				"</parse>",
				"<phase name='optimize_blocks' stamp='12.196'>",
				"<phase_done name='optimize_blocks' stamp='12.197'/>",
				"</phase>",
				"<phase name='optimize_null_checks' stamp='12.197'>",
				"<phase_done name='optimize_null_checks' stamp='12.197'/>",
				"</phase>",
				"<phase_done name='buildIR' stamp='12.197'/>",
				"</phase>",
				"<phase name='emit_lir' stamp='12.197'>",
				"<phase name='lirGeneration' stamp='12.197'>",
				"<phase_done name='lirGeneration' stamp='12.197'/>",
				"</phase>",
				"<phase name='linearScan' stamp='12.197'>",
				"<phase_done name='linearScan' stamp='12.198'/>",
				"</phase>",
				"<phase_done name='emit_lir' stamp='12.198'/>",
				"</phase>",
				"<phase name='codeemit' stamp='12.198'>",
				"<phase_done name='codeemit' stamp='12.199'/>",
				"</phase>",
				"<phase name='codeinstall' stamp='12.199'>",
				"<dependency type='leaf_type' ctxk='787'/>",
				"<phase_done name='codeinstall' stamp='12.199'/>",
				"</phase>",
				"<code_cache total_blobs='377' nmethods='152' adapters='142' free_code_cache='250142208'/>",
				"<task_done success='1' nmsize='3272' count='1' backedge_count='73158' inlined_bytes='112' stamp='12.199'/>",
				"</task>"
		};

		String[] bytecodeLines = new String[]{
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
				"68: return          "
					};

		IMetaMember member = UnitTestUtil.createTestMetaMember("org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog", "testLeaf", new Class[]{long.class});


		Map<Integer, LineAnnotation> result = buildAnnotations(member, CompilerName.C1, logLines, bytecodeLines);

		assertEquals(9, result.size());

		checkLine(result, 15, "Inlined: Yes", Color.GREEN);
		checkLine(result, 21, "Inlined: Yes", Color.GREEN);
		checkLine(result, 27, "Inlined: Yes", Color.GREEN);
		checkLine(result, 33, "Inlined: Yes", Color.GREEN);
		checkLine(result, 50, "Inlined: Yes", Color.GREEN);
		checkLine(result, 55, "Inlined: Yes", Color.GREEN);
		checkLine(result, 59, "Inlined: Yes", Color.GREEN);
		checkLine(result, 62, "Inlined: Yes", Color.GREEN);
		checkLine(result, 65, "Inlined: Yes", Color.GREEN);
	}

	@Test
	public void testJava8TieredChain()
	{
		String[] logLines = new String[]{
				"<task compile_id='133' compile_kind='osr' method='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog testCallChain (J)V' bytes='57' count='1' backedge_count='60651' iicount='1' osr_bci='5' level='3' stamp='12.043'>",
				"<phase name='buildIR' stamp='12.043'>",
				"<type id='680' name='void'/>",
				"<type id='679' name='long'/>",
				"<klass id='776' name='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog' flags='1'/>",
				"<method id='777' holder='776' name='testCallChain' return='680' arguments='679' flags='2' bytes='57' iicount='1'/>",
				"<parse method='777'  stamp='12.043'>",
				"<bc code='183' bci='15'/>",
				"<method id='779' holder='776' name='chainA1' return='679' arguments='679' flags='2' bytes='8' compile_id='131' compiler='C2' level='4' iicount='12111'/>",
				"<call method='779' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='3'/>",
				"<method id='781' holder='776' name='chainA2' return='679' arguments='679' flags='2' bytes='10' iicount='12111'/>",
				"<call method='781' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='5'/>",
				"<method id='783' holder='776' name='chainA3' return='679' arguments='679' flags='2' bytes='10' iicount='12111'/>",
				"<call method='783' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='5'/>",
				"<method id='785' holder='776' name='chainA4' return='679' arguments='679' flags='2' bytes='7' iicount='12111'/>",
				"<call method='785' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='3'/>",
				"<type id='678' name='int'/>",
				"<method id='787' holder='776' name='bigMethod' return='679' arguments='679 678' flags='2' bytes='350' compile_id='43' compiler='C2' level='4' iicount='6047'/>",
				"<call method='787' instr='invokespecial'/>",
				"<inline_fail reason='callee is too large'/>",
				"<bc code='183' bci='21'/>",
				"<method id='789' holder='776' name='chainB1' return='679' arguments='679' flags='2' bytes='8' compile_id='132' compiler='C2' level='4' iicount='16858'/>",
				"<call method='789' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='2'/>",
				"<method id='791' holder='776' name='chainB2' return='679' arguments='679' flags='2' bytes='10' compile_id='130' compiler='C1' level='3' iicount='16858'/>",
				"<call method='791' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='2'/>",
				"<method id='793' holder='776' name='chainB3' return='679' arguments='679' flags='2' bytes='6' iicount='16858'/>",
				"<call method='793' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='38'/>",
				"<klass id='749' name='java/lang/StringBuilder' flags='17'/>",
				"<method id='797' holder='749' name='&lt;init&gt;' return='680' flags='1' bytes='7' iicount='112'/>",
				"<call method='797' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='3'/>",
				"<klass id='747' name='java/lang/AbstractStringBuilder' flags='1024'/>",
				"<method id='799' holder='747' name='&lt;init&gt;' return='680' arguments='678' flags='0' bytes='12' iicount='125'/>",
				"<call method='799' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='1'/>",
				"<klass id='685' name='java/lang/Object' flags='1'/>",
				"<method id='801' holder='685' name='&lt;init&gt;' return='680' flags='1' bytes='1' compile_id='10' compiler='C1' level='1' iicount='447535'/>",
				"<call method='801' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='182' bci='43'/>",
				"<klass id='686' name='java/lang/String' flags='17'/>",
				"<method id='804' holder='749' name='append' return='749' arguments='686' flags='1' bytes='8' iicount='207'/>",
				"<call method='804' instr='invokevirtual'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='2'/>",
				"<method id='806' holder='747' name='append' return='747' arguments='686' flags='1' bytes='50' iicount='240'/>",
				"<call method='806' instr='invokespecial'/>",
				"<inline_fail reason='callee is too large'/>",
				"<bc code='182' bci='47'/>",
				"<method id='808' holder='749' name='append' return='749' arguments='679' flags='1' bytes='8' iicount='7'/>",
				"<call method='808' instr='invokevirtual'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='2'/>",
				"<method id='810' holder='747' name='append' return='747' arguments='679' flags='1' bytes='70' iicount='7'/>",
				"<call method='810' instr='invokespecial'/>",
				"<inline_fail reason='callee is too large'/>",
				"<bc code='182' bci='50'/>",
				"<method id='812' holder='749' name='toString' return='686' flags='1' bytes='17' iicount='111'/>",
				"<call method='812' instr='invokevirtual'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='13'/>",
				"<klass id='765' name='[C' flags='1041'/>",
				"<method id='814' holder='686' name='&lt;init&gt;' return='680' arguments='765 678 678' flags='1' bytes='62' iicount='260'/>",
				"<call method='814' instr='invokespecial'/>",
				"<inline_fail reason='callee is too large'/>",
				"<bc code='182' bci='53'/>",
				"<klass id='795' name='java/io/PrintStream' flags='1'/>",
				"<method id='816' holder='795' name='println' return='680' arguments='686' flags='1' bytes='24' iicount='7'/>",
				"<call method='816' instr='invokevirtual'/>",
				"<dependency type='leaf_type' ctxk='795'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='182' bci='6'/>",
				"<method id='818' holder='795' name='print' return='680' arguments='686' flags='1' bytes='13' iicount='7'/>",
				"<call method='818' instr='invokevirtual'/>",
				"<dependency type='leaf_type' ctxk='795'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='9'/>",
				"<method id='820' holder='795' name='write' return='680' arguments='686' flags='2' bytes='83' iicount='7'/>",
				"<call method='820' instr='invokespecial'/>",
				"<dependency type='leaf_type' ctxk='795'/>",
				"<inline_fail reason='callee is too large'/>",
				"<bc code='183' bci='10'/>",
				"<method id='824' holder='795' name='newLine' return='680' flags='2' bytes='73' iicount='7'/>",
				"<call method='824' instr='invokespecial'/>",
				"<dependency type='leaf_type' ctxk='795'/>",
				"<inline_fail reason='callee is too large'/>",
				"<parse_done stamp='12.046'/>",
				"</parse>",
				"<phase name='optimize_blocks' stamp='12.046'>",
				"<phase_done name='optimize_blocks' stamp='12.046'/>",
				"</phase>",
				"<phase name='optimize_null_checks' stamp='12.046'>",
				"<phase_done name='optimize_null_checks' stamp='12.046'/>",
				"</phase>",
				"<phase_done name='buildIR' stamp='12.046'/>",
				"</phase>",
				"<phase name='emit_lir' stamp='12.047'>",
				"<phase name='lirGeneration' stamp='12.047'>",
				"<phase_done name='lirGeneration' stamp='12.047'/>",
				"</phase>",
				"<phase name='linearScan' stamp='12.047'>",
				"<phase_done name='linearScan' stamp='12.048'/>",
				"</phase>",
				"<phase_done name='emit_lir' stamp='12.048'/>",
				"</phase>",
				"<phase name='codeemit' stamp='12.048'>",
				"<phase_done name='codeemit' stamp='12.049'/>",
				"</phase>",
				"<phase name='codeinstall' stamp='12.049'>",
				"<dependency type='leaf_type' ctxk='795'/>",
				"<phase_done name='codeinstall' stamp='12.049'/>",
				"</phase>",
				"<code_cache total_blobs='357' nmethods='132' adapters='142' free_code_cache='250181248'/>",
				"<task_done success='1' nmsize='3704' count='1' backedge_count='80786' inlined_bytes='149' stamp='12.049'/>",
				"</task>"
			};

		String[] bytecodeLines = new String[]{
				"0: lconst_0        ",
				"1: lstore_3        ",
				"2: iconst_0        ",
				"3: istore          5    ",
				"5: iload           5    ",
				"7: i2l             ",
				"8: lload_1         ",
				"9: lcmp            ",
				"10: ifge            31   ",
				"13: aload_0         ",
				"14: lload_3         ",
				"15: invokespecial   #58  // Method chainA1:(J)J",
				"18: lstore_3        ",
				"19: aload_0         ",
				"20: lload_3         ",
				"21: invokespecial   #59  // Method chainB1:(J)J",
				"24: lstore_3        ",
				"25: iinc            5, 1 ",
				"28: goto            5    ",
				"31: getstatic       #13  // Field java/lang/System.out:Ljava/io/PrintStream;",
				"34: new             #14  // class java/lang/StringBuilder",
				"37: dup             ",
				"38: invokespecial   #15  // Method java/lang/StringBuilder.\"<init>\":()V",
				"41: ldc             #60  // String testCallChain:",
				"43: invokevirtual   #17  // Method java/lang/StringBuilder.append:(Ljava/lang/String;)Ljava/lang/StringBuilder;",
				"46: lload_3         ",
				"47: invokevirtual   #18  // Method java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;",
				"50: invokevirtual   #19  // Method java/lang/StringBuilder.toString:()Ljava/lang/String;",
				"53: invokevirtual   #20  // Method java/io/PrintStream.println:(Ljava/lang/String;)V",
				"56: return          "
		};

		IMetaMember member = UnitTestUtil.createTestMetaMember("org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog", "testCallChain", new Class[]{long.class});

		Map<Integer, LineAnnotation> result = buildAnnotations(member, CompilerName.C1, logLines, bytecodeLines);

		assertEquals(7, result.size());

		checkLine(result, 15, "Inlined: Yes", Color.GREEN);
		checkLine(result, 21, "Inlined: Yes", Color.GREEN);
		checkLine(result, 38, "Inlined: Yes", Color.GREEN);
		checkLine(result, 43, "Inlined: Yes", Color.GREEN);
		checkLine(result, 47, "Inlined: Yes", Color.GREEN);
		checkLine(result, 50, "Inlined: Yes", Color.GREEN);
		checkLine(result, 53, "Inlined: Yes", Color.GREEN);
	}

	private Map<Integer, LineAnnotation> buildAnnotations(IMetaMember member, CompilerName compiler, String[] logLines, String[] bytecodeLines)
	{
		TagProcessor tp = new TagProcessor();

		tp.setCompiler(compiler);

		int count = 0;

		Tag tag = null;

		for (String line : logLines)
		{
			line = line.replace("&lt;", S_OPEN_ANGLE);
			line = line.replace("&gt;", S_CLOSE_ANGLE);

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

		Map<Integer, LineAnnotation> result = new HashMap<>();

		try
		{
			result = new BytecodeAnnotationBuilder().buildBytecodeAnnotations(member, instructions);
		}
		catch (AnnotationException annoEx)
		{
			annoEx.printStackTrace();

			fail();
		}

		return result;
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

		IMetaMember member = UnitTestUtil.createTestMetaMember(klassName, methodName, new Class[0]);

		assertTrue(JournalUtil.memberMatchesParseTag(member, tagParse, parseDictionary));
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

	private void checkLine(Map<Integer, LineAnnotation> result, int index, String annotation, Color colour)
	{
		LineAnnotation line = result.get(index);

		assertNotNull(line);

		assertTrue(line.getAnnotation().contains(annotation));
		assertEquals(colour, line.getColour());
	}
	
	@Test
	public void testMemberMatchesParseTagWithExactParams()
	{
		String methodName = "print";
		String klassName = "java.io.PrintStream";
		Class<?>[] params = new Class[]{java.lang.String.class};

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

		IMetaMember member = UnitTestUtil.createTestMetaMember(klassName, methodName, params);

		assertTrue(JournalUtil.memberMatchesParseTag(member, tagParse, parseDictionary));
	}
	
	@Test
	public void testMemberDoesNotMatchParseTagWithInexactParams()
	{
		String methodName = "print";
		String klassName = "java.io.PrintStream";
		Class<?>[] params = new Class[]{java.lang.Object.class};

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

		IMetaMember member = UnitTestUtil.createTestMetaMember(klassName, methodName, params);

		assertFalse(JournalUtil.memberMatchesParseTag(member, tagParse, parseDictionary));
	}
}