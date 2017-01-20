/*
 * Copyright (c) 2013-2017 Chris Newland.
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
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_TYPE_NAME_VOID;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_KLASS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_METHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_PARSE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TYPE;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.adoptopenjdk.jitwatch.compilation.CompilationUtil;
import org.adoptopenjdk.jitwatch.core.TagProcessor;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IParseDictionary;
import org.adoptopenjdk.jitwatch.model.JITDataModel;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.model.ParseDictionary;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.model.bytecode.BCAnnotationType;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeAnnotationBuilder;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeAnnotationList;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeAnnotations;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.model.bytecode.Opcode;
import org.junit.After;
import org.junit.Test;

public class TestBytecodeAnnotationBuilder
{
	@After
	public void checkUnhandledTags()
	{
		assertEquals(0, CompilationUtil.getUnhandledTagCount());
	}

	private Tag createTag(String tag, Map<String, String> attrs, boolean selfClosing)
	{
		StringBuilder builder = new StringBuilder();

		for (Map.Entry<String, String> entry : attrs.entrySet())
		{
			builder.append(entry.getKey()).append("='").append(entry.getValue()).append("' ");
		}

		return new Tag(tag, builder.toString().trim(), selfClosing);
	}

	@Test
	public void testSanityCheckInlineFail()
	{
		BytecodeInstruction instrAaload = new BytecodeInstruction();
		instrAaload.setOpcode(Opcode.AALOAD);

		assertFalse(new BytecodeAnnotationBuilder(true).sanityCheckInline(instrAaload));
	}

	@Test
	public void testSanityCheckInlinePass()
	{
		BytecodeInstruction instrInvokeSpecial = new BytecodeInstruction();
		instrInvokeSpecial.setOpcode(Opcode.INVOKESPECIAL);

		assertTrue(new BytecodeAnnotationBuilder(true).sanityCheckInline(instrInvokeSpecial));
	}

	@Test
	public void testSanityCheckBranchFail()
	{
		BytecodeInstruction instrAaload = new BytecodeInstruction();
		instrAaload.setOpcode(Opcode.AALOAD);

		assertFalse(new BytecodeAnnotationBuilder(true).sanityCheckBranch(instrAaload));
	}

	@Test
	public void testSanityCheckBranchPass()
	{
		BytecodeInstruction instrIfcmpne = new BytecodeInstruction();
		instrIfcmpne.setOpcode(Opcode.IF_ICMPNE);

		assertTrue(new BytecodeAnnotationBuilder(true).sanityCheckBranch(instrIfcmpne));
	}

	@Test
	public void testJava7NonTieredLeaf()
	{
		String[] logLines = new String[] {
				"<task_queued compile_id='82' method='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog testLeaf (J)V' bytes='55' count='520' backedge_count='5000' iicount='520' stamp='0.083' comment='count' hot_count='520'/>",
				"<nmethod compile_id='82' compiler='C2' entry='0x000000010744c060' size='1256' address='0x000000010744bf10' relocation_offset='296' insts_offset='336' stub_offset='688' scopes_data_offset='720' scopes_pcs_offset='832' dependencies_offset='1232' nul_chk_table_offset='1240' method='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog testLeaf (J)V' bytes='55' count='546' backedge_count='5389' iicount='546' stamp='0.105'/>",
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
				new Class[] { long.class }, void.class);

		JITDataModel model = new JITDataModel();
		model.setVmVersionRelease("1.8.0");

		BytecodeAnnotations result = UnitTestUtil.buildAnnotations(true, false, model, member, logLines, bytecodeLines);

		BytecodeAnnotationList list = result.getAnnotationList(member);

		assertEquals(10, list.annotatedLineCount());

		UnitTestUtil.checkAnnotation(list, 10, "inline (hot)", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 16, "inline (hot)", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 22, "inline (hot)", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 28, "inline (hot)", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 40, "always", BCAnnotationType.BRANCH);
		UnitTestUtil.checkAnnotation(list, 52, "not reached", BCAnnotationType.INLINE_FAIL);
		UnitTestUtil.checkAnnotation(list, 56, "MinInliningThreshold", BCAnnotationType.INLINE_FAIL);
		UnitTestUtil.checkAnnotation(list, 59, "MinInliningThreshold", BCAnnotationType.INLINE_FAIL);
		UnitTestUtil.checkAnnotation(list, 62, "MinInliningThreshold", BCAnnotationType.INLINE_FAIL);
		UnitTestUtil.checkAnnotation(list, 62, "null_check", BCAnnotationType.UNCOMMON_TRAP);
	}

	@Test
	public void testJava7NonTieredChain()
	{
		String[] logLines = new String[] {
				"<task_queued compile_id='73' method='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog testCallChain (J)V' bytes='55' count='520' backedge_count='5000' iicount='520' stamp='0.083' comment='count' hot_count='520'/>",
				"<nmethod compile_id='73' compiler='C2' entry='0x000000010744c060' size='1256' address='0x000000010744bf10' relocation_offset='296' insts_offset='336' stub_offset='688' scopes_data_offset='720' scopes_pcs_offset='832' dependencies_offset='1232' nul_chk_table_offset='1240' method='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog testCallChain (J)V' bytes='55' count='546' backedge_count='5389' iicount='546' stamp='0.105'/>",
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
				new Class[] { long.class }, void.class);

		JITDataModel model = new JITDataModel();
		model.setVmVersionRelease("1.8.0");

		BytecodeAnnotations result = UnitTestUtil.buildAnnotations(true, false, model, member, logLines, bytecodeLines);

		BytecodeAnnotationList list = result.getAnnotationList(member);

		assertEquals(8, list.annotatedLineCount());

		UnitTestUtil.checkAnnotation(list, 10, "inline (hot)", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 16, "inline (hot)", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 28, "always", BCAnnotationType.BRANCH);
		UnitTestUtil.checkAnnotation(list, 40, "not reached", BCAnnotationType.INLINE_FAIL);
		UnitTestUtil.checkAnnotation(list, 44, "MinInliningThreshold", BCAnnotationType.INLINE_FAIL);
		UnitTestUtil.checkAnnotation(list, 47, "MinInliningThreshold", BCAnnotationType.INLINE_FAIL);
		UnitTestUtil.checkAnnotation(list, 47, "null_check", BCAnnotationType.UNCOMMON_TRAP);
		UnitTestUtil.checkAnnotation(list, 50, "MinInliningThreshold", BCAnnotationType.INLINE_FAIL);
	}

	@Test
	public void testJDK9BytecodeAnnotations()
	{
		String[] logLines = new String[] {
				"<task_queued compile_id='881' method='org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog testCallChain3 ()V' bytes='55' count='520' backedge_count='5000' iicount='520' stamp='0.083' comment='count' hot_count='520'/>",
				"<nmethod compile_id='881' compiler='C2' entry='0x000000010744c060' size='1256' address='0x000000010744bf10' relocation_offset='296' insts_offset='336' stub_offset='688' scopes_data_offset='720' scopes_pcs_offset='832' dependencies_offset='1232' nul_chk_table_offset='1240' method='org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog testCallChain3 ()V' bytes='55' count='546' backedge_count='5389' iicount='546' stamp='0.105'/>",
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

		JITDataModel model = new JITDataModel();
		model.setVmVersionRelease("1.8.0");

		BytecodeAnnotations result = UnitTestUtil.buildAnnotations(true, false, model, member, logLines, bytecodeLines);

		BytecodeAnnotationList list = result.getAnnotationList(member);

		assertEquals(8, list.annotatedLineCount());

		UnitTestUtil.checkAnnotation(list, 64, "Inlined: Yes", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 18, "Inlined: Yes", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 35, "Inlined: Yes", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 67, "Inlined: Yes", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 52, "Inlined: Yes", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 57, "Inlined: Yes", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 26, "Inlined: Yes", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 61, "Inlined: Yes", BCAnnotationType.INLINE_SUCCESS);
	}

	@Test
	public void testJava8NonTieredLeaf()
	{
		String[] logLines = new String[] {
				"<task_queued compile_id='78' method='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog testLeaf (J)V' bytes='55' count='520' backedge_count='5000' iicount='520' stamp='0.083' comment='count' hot_count='520'/>",
				"<nmethod compile_id='78' compiler='C2' entry='0x000000010744c060' size='1256' address='0x000000010744bf10' relocation_offset='296' insts_offset='336' stub_offset='688' scopes_data_offset='720' scopes_pcs_offset='832' dependencies_offset='1232' nul_chk_table_offset='1240' method='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog testLeaf (J)V' bytes='55' count='546' backedge_count='5389' iicount='546' stamp='0.105'/>",
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
				new Class[] { long.class }, void.class);

		JITDataModel model = new JITDataModel();
		model.setVmVersionRelease("1.8.0");

		BytecodeAnnotations result = UnitTestUtil.buildAnnotations(true, false, model, member, logLines, bytecodeLines);

		BytecodeAnnotationList list = result.getAnnotationList(member);

		assertEquals(11, list.annotatedLineCount());

		UnitTestUtil.checkAnnotation(list, 10, "never", BCAnnotationType.BRANCH);
		UnitTestUtil.checkAnnotation(list, 15, "inline (hot)", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 21, "inline (hot)", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 27, "inline (hot)", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 33, "inline (hot)", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 50, "not reached", BCAnnotationType.INLINE_FAIL);
		UnitTestUtil.checkAnnotation(list, 55, "MinInliningThreshold", BCAnnotationType.INLINE_FAIL);
		UnitTestUtil.checkAnnotation(list, 59, "MinInliningThreshold", BCAnnotationType.INLINE_FAIL);
		UnitTestUtil.checkAnnotation(list, 62, "MinInliningThreshold", BCAnnotationType.INLINE_FAIL);
		UnitTestUtil.checkAnnotation(list, 65, "MinInliningThreshold", BCAnnotationType.INLINE_FAIL);
		UnitTestUtil.checkAnnotation(list, 65, "null_check", BCAnnotationType.UNCOMMON_TRAP);
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

		Tag tagTypeInt = createTag(TAG_TYPE, attrsTypeInt, true);

		String methodID = "123";
		String klassID = "456";

		attrsMethod.put(ATTR_NAME, methodName);
		attrsMethod.put(ATTR_ID, methodID);
		attrsMethod.put(ATTR_HOLDER, klassID);
		attrsMethod.put(ATTR_RETURN, idInt);

		Tag tagMethod = createTag(TAG_METHOD, attrsMethod, true);

		attrsKlass.put(ATTR_NAME, klassName.replace(C_DOT, C_SLASH));
		attrsKlass.put(ATTR_ID, klassID);
		Tag tagKlass = createTag(TAG_KLASS, attrsKlass, true);

		attrsParse.put(ATTR_METHOD, methodID);
		Tag tagParse = createTag(TAG_PARSE, attrsParse, false);

		IParseDictionary parseDictionary = new ParseDictionary(methodID);
		parseDictionary.putKlass(klassID, tagKlass);
		parseDictionary.putMethod(methodID, tagMethod);
		parseDictionary.putType(idInt, tagTypeInt);

		IMetaMember member = UnitTestUtil.createTestMetaMember(klassName, methodName, new Class[0], int.class);

		String tagMethodID = tagParse.getAttributes().get(ATTR_METHOD);

		assertTrue(CompilationUtil.memberMatchesMethodID(member, tagMethodID, parseDictionary));
	}

	@Test
	public void testIsJournalForCompile2NativeMember()
	{
		String tagText = "<nmethod address='0x00007fb0ef001550' method='sun/misc/Unsafe compareAndSwapLong (Ljava/lang/Object;JJJ)Z' consts_offset='872' count='5000' backedge_count='1' stamp='2.453' iicount='10000' entry='0x00007fb0ef0016c0' size='872' compile_kind='c2n' insts_offset='368' bytes='0' relocation_offset='296' compile_id='28'/>";

		IMetaMember member = UnitTestUtil.createTestMetaMember();

		TagProcessor tp = new TagProcessor();
		Tag tag = tp.processLine(tagText);

		member.setTagNMethod(tag);

		assertTrue(CompilationUtil.isJournalForCompile2NativeMember(tag));
	}

	@Test
	public void testIsNotJournalForCompile2NativeMember()
	{
		String tagText = "<task_done success='1' nmsize='120' count='5000' backedge_count='5100' stamp='14.723'/>";

		IMetaMember member = UnitTestUtil.createTestMetaMember();

		TagProcessor tp = new TagProcessor();
		Tag tag = tp.processLine(tagText);

		member.setTagNMethod(tag);

		assertFalse(CompilationUtil.isJournalForCompile2NativeMember(tag));
	}

	@Test
	public void testMemberMatchesParseTagWithExactParams()
	{
		String methodName = "print";
		String klassName = "java.io.PrintStream";
		Class<?>[] params = new Class[] { java.lang.String.class };

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

		Tag tagTypeString = createTag(TAG_TYPE, attrsTypeString, true);
		Tag tagTypeVoid = createTag(TAG_TYPE, attrsTypeVoid, true);

		String methodID = "123";
		String klassID = "456";

		attrsMethod.put(ATTR_NAME, methodName);
		attrsMethod.put(ATTR_ID, methodID);
		attrsMethod.put(ATTR_HOLDER, klassID);
		attrsMethod.put(ATTR_ARGUMENTS, idString);
		attrsMethod.put(ATTR_RETURN, idVoid);

		Tag tagMethod = createTag(TAG_METHOD, attrsMethod, true);

		attrsKlass.put(ATTR_NAME, klassName.replace(C_DOT, C_SLASH));
		attrsKlass.put(ATTR_ID, klassID);
		Tag tagKlass = createTag(TAG_KLASS, attrsKlass, true);

		attrsParse.put(ATTR_METHOD, methodID);
		Tag tagParse = createTag(TAG_PARSE, attrsParse, false);

		IParseDictionary parseDictionary = new ParseDictionary(methodID);
		parseDictionary.putKlass(klassID, tagKlass);
		parseDictionary.putMethod(methodID, tagMethod);
		parseDictionary.putType(idString, tagTypeString);
		parseDictionary.putType(idVoid, tagTypeVoid);

		IMetaMember member = UnitTestUtil.createTestMetaMember(klassName, methodName, params, void.class);

		String tagMethodID = tagParse.getAttributes().get(ATTR_METHOD);

		assertTrue(CompilationUtil.memberMatchesMethodID(member, tagMethodID, parseDictionary));
	}

	@Test
	public void testMemberDoesNotMatchParseTagWithInexactParams()
	{
		String methodName = "print";
		String klassName = "java.io.PrintStream";
		Class<?>[] params = new Class[] { java.lang.Object.class };

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

		Tag tagTypeString = createTag(TAG_TYPE, attrsTypeString, true);
		Tag tagTypeVoid = createTag(TAG_TYPE, attrsTypeVoid, true);

		String methodID = "123";
		String klassID = "456";

		attrsMethod.put(ATTR_NAME, methodName);
		attrsMethod.put(ATTR_ID, methodID);
		attrsMethod.put(ATTR_HOLDER, klassID);
		attrsMethod.put(ATTR_ARGUMENTS, idString);
		attrsMethod.put(ATTR_RETURN, idVoid);

		Tag tagMethod = createTag(TAG_METHOD, attrsMethod, true);

		attrsKlass.put(ATTR_NAME, klassName.replace(C_DOT, C_SLASH));
		attrsKlass.put(ATTR_ID, klassID);
		Tag tagKlass = createTag(TAG_KLASS, attrsKlass, true);

		attrsParse.put(ATTR_METHOD, methodID);
		Tag tagParse = createTag(TAG_PARSE, attrsParse, false);

		IParseDictionary parseDictionary = new ParseDictionary(methodID);
		parseDictionary.putKlass(klassID, tagKlass);
		parseDictionary.putMethod(methodID, tagMethod);
		parseDictionary.putType(idString, tagTypeString);
		parseDictionary.putType(idVoid, tagTypeVoid);

		IMetaMember member = UnitTestUtil.createTestMetaMember(klassName, methodName, params, void.class);

		String tagMethodID = tagParse.getAttributes().get(ATTR_METHOD);

		assertFalse(CompilationUtil.memberMatchesMethodID(member, tagMethodID, parseDictionary));
	}

	@Test
	public void testEliminatedHeapAllocationsCorrectKlass()
	{
		String[] logLines = new String[] {
				"<task_queued compile_id='11' method='EscapeTest run ()Ljava/lang/String;' bytes='55' count='520' backedge_count='5000' iicount='520' stamp='0.083' comment='count' hot_count='520'/>",
				"<nmethod compile_id='11' compiler='C2' entry='0x000000010744c060' size='1256' address='0x000000010744bf10' relocation_offset='296' insts_offset='336' stub_offset='688' scopes_data_offset='720' scopes_pcs_offset='832' dependencies_offset='1232' nul_chk_table_offset='1240' method='EscapeTest run ()Ljava/lang/String;' bytes='55' count='546' backedge_count='5389' iicount='546' stamp='0.105'/>",
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

		JITDataModel model = new JITDataModel();
		model.setVmVersionRelease("1.8.0");

		BytecodeAnnotations result = UnitTestUtil.buildAnnotations(true, false, model, member, logLines, bytecodeLines);

		BytecodeAnnotationList list = result.getAnnotationList(member);

		assertEquals(12, list.annotatedLineCount());

		UnitTestUtil.checkAnnotation(list, 7, "constraint", BCAnnotationType.UNCOMMON_TRAP);
		UnitTestUtil.checkAnnotation(list, 7, "predicate", BCAnnotationType.UNCOMMON_TRAP);
		UnitTestUtil.checkAnnotation(list, 7, "loop_limit_check", BCAnnotationType.UNCOMMON_TRAP);
		UnitTestUtil.checkAnnotation(list, 11, "taken", BCAnnotationType.BRANCH);
		UnitTestUtil.checkAnnotation(list, 11, "unstable_if", BCAnnotationType.UNCOMMON_TRAP);
		UnitTestUtil.checkAnnotation(list, 22, "null_check", BCAnnotationType.UNCOMMON_TRAP);
		UnitTestUtil.checkAnnotation(list, 25, "hot", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 28, "taken", BCAnnotationType.BRANCH);
		UnitTestUtil.checkAnnotation(list, 35, "EscapeTest$Wrapper1", BCAnnotationType.ELIMINATED_ALLOCATION);
		UnitTestUtil.checkAnnotation(list, 42, "hot", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 47, "EscapeTest$Wrapper2", BCAnnotationType.ELIMINATED_ALLOCATION);
		UnitTestUtil.checkAnnotation(list, 54, "hot", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 63, "hot", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 66, "taken", BCAnnotationType.BRANCH);
		UnitTestUtil.checkAnnotation(list, 84, "unloaded", BCAnnotationType.UNCOMMON_TRAP);
	}

	@Test
	public void testEliminatedHeapAllocationsJDK9()
	{
		String[] logLines = new String[] {
				"<task_queued compile_id='11' method='EscapeTest run ()Ljava/lang/String;' bytes='55' count='520' backedge_count='5000' iicount='520' stamp='0.083' comment='count' hot_count='520'/>",
				"<nmethod compile_id='11' compiler='C2' entry='0x000000010744c060' size='1256' address='0x000000010744bf10' relocation_offset='296' insts_offset='336' stub_offset='688' scopes_data_offset='720' scopes_pcs_offset='832' dependencies_offset='1232' nul_chk_table_offset='1240' method='EscapeTest run ()Ljava/lang/String;' bytes='55' count='546' backedge_count='5389' iicount='546' stamp='0.105'/>",
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

		JITDataModel model = new JITDataModel();
		model.setVmVersionRelease("1.9.0");

		BytecodeAnnotations result = UnitTestUtil.buildAnnotations(true, false, model, member, logLines, bytecodeLines);

		BytecodeAnnotationList list = result.getAnnotationList(member);

		assertEquals(12, list.annotatedLineCount());

		UnitTestUtil.checkAnnotation(list, 7, "constraint", BCAnnotationType.UNCOMMON_TRAP);
		UnitTestUtil.checkAnnotation(list, 7, "predicate", BCAnnotationType.UNCOMMON_TRAP);
		UnitTestUtil.checkAnnotation(list, 7, "loop_limit_check", BCAnnotationType.UNCOMMON_TRAP);
		UnitTestUtil.checkAnnotation(list, 11, "taken", BCAnnotationType.BRANCH);
		UnitTestUtil.checkAnnotation(list, 11, "unstable_if", BCAnnotationType.UNCOMMON_TRAP);
		UnitTestUtil.checkAnnotation(list, 22, "null_check", BCAnnotationType.UNCOMMON_TRAP);
		UnitTestUtil.checkAnnotation(list, 25, "hot", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 28, "taken", BCAnnotationType.BRANCH);
		UnitTestUtil.checkAnnotation(list, 35, "EscapeTest$Wrapper1", BCAnnotationType.ELIMINATED_ALLOCATION);
		UnitTestUtil.checkAnnotation(list, 42, "hot", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 47, "EscapeTest$Wrapper2", BCAnnotationType.ELIMINATED_ALLOCATION);
		UnitTestUtil.checkAnnotation(list, 54, "hot", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 63, "hot", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 66, "taken", BCAnnotationType.BRANCH);
		UnitTestUtil.checkAnnotation(list, 84, "unloaded", BCAnnotationType.UNCOMMON_TRAP);
	}

	@Test
	public void testJDK9NoParsePhaseTag()
	{
		String[] logLines = new String[] {
				"<task_queued compile_id='730' method='java/io/BufferedInputStream read ()I' bytes='55' count='520' backedge_count='5000' iicount='520' stamp='0.083' comment='count' hot_count='520'/>",
				"<nmethod compile_id='730' compiler='C2' entry='0x000000010744c060' size='1256' address='0x000000010744bf10' relocation_offset='296' insts_offset='336' stub_offset='688' scopes_data_offset='720' scopes_pcs_offset='832' dependencies_offset='1232' nul_chk_table_offset='1240' method='java/io/BufferedInputStream read ()I' bytes='55' count='546' backedge_count='5389' iicount='546' stamp='0.105'/>",
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

		JITDataModel model = new JITDataModel();
		model.setVmVersionRelease("1.9.0");

		BytecodeAnnotations result = UnitTestUtil.buildAnnotations(true, false, model, member, logLines, bytecodeLines);

		BytecodeAnnotationList list = result.getAnnotationList(member);

		assertEquals(5, list.annotatedLineCount());

		UnitTestUtil.checkAnnotation(list, 8, "taken", BCAnnotationType.BRANCH);
		UnitTestUtil.checkAnnotation(list, 12, "No, too big", BCAnnotationType.INLINE_FAIL);
		UnitTestUtil.checkAnnotation(list, 23, "taken", BCAnnotationType.BRANCH);
		UnitTestUtil.checkAnnotation(list, 29, "hot", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 43, "range_check", BCAnnotationType.UNCOMMON_TRAP);
	}

	@Test
	public void testRegressionMemberMatchesParseTagWithArrayParams()
	{
//		String[] lines = new String[] {
//				"<parse method='831' stamp='0.138'>",
//				" <bc code='183' bci='9'/>",
//				" <klass unloaded='1' name='java/lang/StringIndexOutOfBoundsException' id='833'/>",
//				" <method unloaded='1' name='&lt;init&gt;' holder='833' arguments='721' id='834' return='723'/>",
//				" <call method='834' instr='invokespecial'/>",
//				" <inline_fail reason='not inlineable'/>",
//				" <bc code='183' bci='27'/>",
//				" <call method='834' instr='invokespecial'/>",
//				" <inline_fail reason='not inlineable'/>",
//				" <bc code='183' bci='43'/>",
//				" <call method='834' instr='invokespecial'/>",
//				" <inline_fail reason='not inlineable'/>",
//				" <bc code='184' bci='58'/>",
//				" <klass name='java/lang/Object' flags='1' id='728'/>",
//				" <klass name='java/lang/System' flags='17' id='734'/>",
//				" <method compile_kind='c2n' level='0' bytes='0' name='arraycopy' flags='265' holder='734' arguments='728 721 728 721 721' id='835' compile_id='5' iicount='640' return='723'/>",
//				" <call method='835' instr='invokestatic'/>",
//				" <inline_success reason='intrinsic'/>",
//				" <parse_done stamp='0.139'/>",
//				"</parse>" };

		String methodName = "getChars";
		String klassName = "java.lang.String";
		Class<?>[] params = new Class[] { int.class, int.class, char[].class, int.class };

		IParseDictionary parseDictionary = new ParseDictionary(methodName);

		TagProcessor tagProcessor = new TagProcessor();

		Tag tagKlass820 = tagProcessor.processLine("<klass name='[C' flags='1041' id='820'/>");
		Tag tagKlass833 = tagProcessor
				.processLine("<klass unloaded='1' name='java/lang/StringIndexOutOfBoundsException' id='833'/>");
		Tag tagKlass734 = tagProcessor.processLine("<klass name='java/lang/System' flags='17' id='734'/>");
		Tag tagKlass728 = tagProcessor.processLine("<klass name='java/lang/Object' flags='1' id='728'/>");
		Tag tagKlass729 = tagProcessor.processLine("<klass name='java/lang/String' flags='17' id='729'/>");
		parseDictionary.putKlass("820", tagKlass820);
		parseDictionary.putKlass("833", tagKlass833);
		parseDictionary.putKlass("728", tagKlass728);
		parseDictionary.putKlass("734", tagKlass734);
		parseDictionary.putKlass("728", tagKlass728);
		parseDictionary.putKlass("729", tagKlass729);

		Tag tagType721 = tagProcessor.processLine("<type name='int' id='721'/>");
		Tag tagType723 = tagProcessor.processLine("<type name='void' id='723'/>");
		parseDictionary.putType("721", tagType721);
		parseDictionary.putType("723", tagType723);

		Tag tagMethod831 = tagProcessor.processLine(
				"<method bytes='62' name='getChars' flags='1' holder='729' arguments='721 721 820 721' id='831' iicount='256' return='723'/>");
		Tag tagMethod834 = tagProcessor
				.processLine("<method unloaded='1' name='&lt;init&gt;' holder='833' arguments='721' id='834' return='723'/>");
		Tag tagMethod835 = tagProcessor.processLine(
				"<method compile_kind='c2n' level='0' bytes='0' name='arraycopy' flags='265' holder='734' arguments='728 721 728 721 721' id='835' compile_id='5' iicount='640' return='723'/>");
		parseDictionary.putMethod("831", tagMethod831);
		parseDictionary.putMethod("834", tagMethod834);
		parseDictionary.putMethod("835", tagMethod835);

		IMetaMember member = UnitTestUtil.createTestMetaMember(klassName, methodName, params, void.class);

		assertTrue(CompilationUtil.memberMatchesMethodID(member, "831", parseDictionary));
	}

	@Test
	public void testRegressionMemberMatchesParseTagForConstructor()
	{
//		String[] lines = new String[] {
//				"<parse method='823' stamp='2.515' uses='16823'>",
//				"   <bc code='183' bci='1'/>",
//				"   <klass name='java/lang/Object' flags='1' id='720'/>",
//				"   <method level='1' bytes='1' name='&lt;init&gt;' flags='1' holder='720' id='825' compile_id='24' compiler='C1' iicount='370647' return='715'/>",
//				"   <call method='825' inline='1' count='16398' prof_factor='1'/>",
//				"   <inline_success reason='inline (hot)'/>",
//				"   <parse method='825' stamp='2.515' uses='16823'>",
//				"     <parse_done nodes='51' memory='24584' stamp='2.515' live='50'/>",
//				"   </parse>",
//				"   <parse_done nodes='69' memory='28312' stamp='2.515' live='67'/>",
//				" </parse>" };

		String methodName = "String"; // constructor
										// java.lang.String(char[],boolean)
		String klassName = "java.lang.String";
		Class<?>[] params = new Class[] { char[].class, boolean.class };

		IParseDictionary parseDictionary = new ParseDictionary(methodName);

		TagProcessor tagProcessor = new TagProcessor();

		Tag tagType715 = tagProcessor.processLine("<type name='void' id='715'/>");
		Tag tagType707 = tagProcessor.processLine("<type name='boolean' id='707'/>");
		parseDictionary.putType("715", tagType715);
		parseDictionary.putType("707", tagType707);

		Tag tagKlass720 = tagProcessor.processLine("<klass name='java/lang/Object' flags='1' id='720'/>");
		Tag tagKlass721 = tagProcessor.processLine("<klass name='java/lang/String' flags='17' id='721'/>");
		Tag tagKlass812 = tagProcessor.processLine("<klass name='[C' flags='1041' id='812'/>");
		parseDictionary.putKlass("720", tagKlass720);
		parseDictionary.putKlass("721", tagKlass721);
		parseDictionary.putKlass("812", tagKlass812);

		Tag tagMethod823 = tagProcessor.processLine(
				"<method level='3' bytes='10' name='&lt;init&gt;' flags='0' holder='721' arguments='812 707' id='823' compile_id='149' compiler='C1' iicount='16823' return='715'/>");
		Tag tagMethod825 = tagProcessor.processLine(
				"<method level='1' bytes='1' name='&lt;init&gt;' flags='1' holder='720' id='825' compile_id='24' compiler='C1' iicount='370647' return='715'/>");
		parseDictionary.putMethod("823", tagMethod823);
		parseDictionary.putMethod("825", tagMethod825);

		IMetaMember member = UnitTestUtil.createTestMetaMember(klassName, methodName, params, void.class);

		assertTrue(CompilationUtil.memberMatchesMethodID(member, "823", parseDictionary));
	}

	@Test
	public void testParseTagForLateInline()
	{
		String[] logLines = new String[] {
				"<task_queued decompiles='1' unstable_if_traps='1' method='java/io/BufferedReader readLine (Z)Ljava/lang/String;' bytes='304' count='5000' backedge_count='5000' stamp='2.384' comment='count' hot_count='5001' compile_id='184' iicount='664'/>",
				"<nmethod stub_offset='4272' dependencies_offset='5560' address='0x00007f2f150e1450' unstable_if_traps='1' method='java/io/BufferedReader readLine (Z)Ljava/lang/String;' count='5000' backedge_count='5000' stamp='2.404' nul_chk_table_offset='5784' scopes_data_offset='4448' iicount='790' handler_table_offset='5568' entry='0x00007f2f150e1620' decompiles='1' size='5816' scopes_pcs_offset='5240' insts_offset='464' bytes='304' relocation_offset='296' compile_id='184' compiler='C2'/>",
				"<task decompiles='1' unstable_if_traps='1' method='java/io/BufferedReader readLine (Z)Ljava/lang/String;' bytes='304' count='5000' backedge_count='5098' stamp='2.384' compile_id='184' iicount='694'>",
				"  <phase nodes='3' name='parse' stamp='2.385' live='3'>",
				"    <klass name='java/lang/String' flags='17' id='729'/>",
				"    <type name='boolean' id='715'/>",
				"    <klass name='java/io/BufferedReader' flags='1' id='831'/>",
				"    <method bytes='304' name='readLine' flags='0' holder='831' arguments='715' id='832' iicount='694' return='729'/>",
				"    <parse method='832' stamp='2.385' uses='694'>",
				"      <observe total='1' count='1' trap='unstable_if'/>",
				"      <observe that='has_exception_handlers'/>",
				"      <bc code='194' bci='9'/>",
				"      <uncommon_trap reason='null_check' bci='9' action='maybe_recompile'/>",
				"      <bc code='183' bci='11'/>",
				"      <type name='void' id='723'/>",
				"      <method bytes='18' name='ensureOpen' flags='2' holder='831' id='835' iicount='696' return='723'/>",
				"      <call method='835' inline='1' count='538' prof_factor='1'/>",
				"      <klass name='java/io/IOException' flags='1' id='842'/>",
				"      <uncommon_trap reason='unloaded' method='835' klass='842' bci='7' action='reinterpret' index='16'/>",
				"      <inline_success reason='inline (hot)'/>",
				"      <parse method='835' stamp='2.385' uses='538'>",
				"        <bc code='187' bci='7'/>",
				"        <uncommon_trap reason='unloaded' bci='7' action='reinterpret' index='16'/>",
				"        <parse_done nodes='81' memory='32192' stamp='2.385' live='78'/>",
				"      </parse>",
				"      <bc code='154' bci='15'/>",
				"      <branch prob='never' not_taken='538' taken='0' cnt='538' target_bci='25'/>",
				"      <uncommon_trap reason='unstable_if' bci='15' action='reinterpret' comment='taken never'/>",
				"      <bc code='153' bci='22'/>",
				"      <branch prob='0.903346' not_taken='52' taken='486' cnt='538' target_bci='29'/>",
				"      <uncommon_trap reason='predicate' bci='32' action='maybe_recompile'/>",
				"      <uncommon_trap reason='loop_limit_check' bci='32' action='maybe_recompile'/>",
				"      <bc code='161' bci='40'/>",
				"      <branch prob='0.976147' not_taken='13' taken='532' cnt='545' target_bci='47'/>",
				"      <bc code='183' bci='44'/>",
				"      <method bytes='170' name='fill' flags='2' holder='831' id='841' iicount='18' return='723'/>",
				"      <call method='841' inline='1' count='13' prof_factor='1'/>",
				"      <inline_fail reason='too big'/>",
				"      <direct_call bci='44'/>",
				"      <bc code='161' bci='55'/>",
				"      <branch prob='0.992661' not_taken='4' taken='541' cnt='545' target_bci='82'/>",
				"      <bc code='198' bci='59'/>",
				"      <branch not_taken='1' taken='3' target_bci='77'/>",
				"      <bc code='182' bci='63'/>",
				"      <type name='int' id='721'/>",
				"      <klass name='java/lang/StringBuffer' flags='17' id='793'/>",
				"      <method bytes='5' name='length' flags='33' holder='793' id='840' iicount='71' return='721'/>",
				"      <call method='840' inline='1' count='1' prof_factor='1'/>",
				"      <inline_success reason='accessor'/>",
				"      <parse method='840' stamp='2.385' uses='1'>",
				"        <parse_done nodes='236' memory='60840' stamp='2.385' live='229'/>",
				"      </parse>",
				"      <bc code='158' bci='66'/>",
				"      <branch not_taken='0' taken='1' target_bci='77'/>",
				"      <bc code='182' bci='70'/>",
				"      <method bytes='36' name='toString' flags='33' holder='793' id='838' iicount='2899' return='729'/>",
				"      <call method='838' inline='1' count='0' prof_factor='1'/>",
				"      <inline_fail reason='too big'/>",
				"      <direct_call bci='70'/>",
				"      <bc code='153' bci='90'/>",
				"      <branch prob='0.903882' not_taken='52' taken='489' cnt='541' target_bci='117'/>",
				"      <bc code='52' bci='101'/>",
				"      <uncommon_trap reason='null_check' bci='101' action='maybe_recompile'/>",
				"      <uncommon_trap reason='range_check' bci='101' action='make_not_entrant' comment='range_check'/>",
				"      <bc code='160' bci='104'/>",
				"      <branch prob='never' not_taken='52' taken='0' cnt='52' target_bci='117'/>",
				"      <uncommon_trap reason='unstable_if' bci='104' action='reinterpret' comment='taken never'/>",
				"      <bc code='162' bci='137'/>",
				"      <branch prob='0.000125585' not_taken='55732' taken='7' cnt='55739' target_bci='175'/>",
				"      <uncommon_trap reason='predicate' bci='140' action='maybe_recompile'/>",
				"      <uncommon_trap reason='loop_limit_check' bci='140' action='maybe_recompile'/>",
				"      <bc code='52' bci='146'/>",
				"      <uncommon_trap reason='null_check' bci='146' action='maybe_recompile'/>",
				"      <uncommon_trap reason='range_check' bci='146' action='make_not_entrant' comment='range_check'/>",
				"      <bc code='159' bci='153'/>",
				"      <branch prob='0.00861265' not_taken='55252' taken='480' cnt='55732' target_bci='163'/>",
				"      <bc code='160' bci='160'/>",
				"      <branch prob='0.999059' not_taken='52' taken='55200' cnt='55252' target_bci='169'/>",
				"      <bc code='162' bci='137'/>",
				"      <branch prob='0.000125585' not_taken='55732' taken='7' cnt='55200' target_bci='175'/>",
				"      <bc code='153' bci='188'/>",
				"      <branch prob='0.012987' not_taken='532' taken='7' cnt='539' target_bci='264'/>",
				"      <bc code='199' bci='192'/>",
				"      <branch prob='0.0131332' not_taken='526' taken='7' cnt='533' target_bci='216'/>",
				"      <bc code='183' bci='208'/>",
				"      <klass name='[C' flags='1041' id='820'/>",
				"      <method bytes='82' name='&lt;init&gt;' flags='1' holder='729' arguments='820 721 721' id='839' compile_id='96' compiler='C2' iicount='10337' return='723'/>",
				"      <call method='839' inline='1' count='526' prof_factor='1'/>",
				"      <klass unloaded='1' name='java/lang/StringIndexOutOfBoundsException' id='851'/>",
				"      <uncommon_trap reason='unloaded' method='839' klass='851' bci='58' action='reinterpret' index='6'/>",
				"      <uncommon_trap reason='unloaded' method='839' klass='851' bci='25' action='reinterpret' index='6'/>",
				"      <uncommon_trap reason='unloaded' method='839' klass='851' bci='8' action='reinterpret' index='6'/>",
				"      <inline_fail reason='already compiled into a big method'/>",
				"      <direct_call bci='208'/>",
				"      <bc code='182' bci='226'/>",
				"      <method bytes='15' name='append' flags='33' holder='793' arguments='820 721 721' id='836' iicount='15' return='793'/>",
				"      <call method='836' inline='1' count='7' prof_factor='1'/>",
				"      <inline_fail reason='executed &lt; MinInliningThreshold times'/>",
				"      <direct_call bci='226'/>",
				"      <bc code='182' bci='231'/>",
				"      <call method='838' inline='1' count='7' prof_factor='1'/>",
				"      <inline_fail reason='too big'/>",
				"      <direct_call bci='231'/>",
				"      <bc code='160' bci='250'/>",
				"      <branch prob='0.902439' not_taken='52' taken='481' cnt='533' target_bci='258'/>",
				"      <bc code='199' bci='265'/>",
				"      <branch not_taken='7' taken='0' target_bci='279'/>",
				"      <bc code='183' bci='275'/>",
				"      <method bytes='6' name='&lt;init&gt;' flags='1' holder='793' arguments='721' id='837' iicount='1185' return='723'/>",
				"      <call method='837' inline='1' count='7' prof_factor='1'/>",
				"      <inline_success reason='inline (hot)'/>",
				"      <direct_call bci='275'/>",
				"      <bc code='182' bci='289'/>",
				"      <call method='836' inline='1' count='7' prof_factor='1'/>",
				"      <inline_fail reason='executed &lt; MinInliningThreshold times'/>",
				"      <direct_call bci='289'/>",
				"      <bc code='191' bci='303'/>",
				"      <uncommon_trap reason='null_check' bci='303' action='maybe_recompile'/>",
				"      <parse_done nodes='728' memory='163168' stamp='2.386' live='710'/>",
				"    </parse>",
				"    <late_inline method='837'>",
				"      <jvms method='832' bci='275'/>",
				"    </late_inline>",
				"    <parse method='837' stamp='2.386' uses='7'>",
				"      <bc code='183' bci='2'/>",
				"      <klass name='java/lang/AbstractStringBuilder' flags='1024' id='792'/>",
				"      <method bytes='12' name='&lt;init&gt;' flags='0' holder='792' arguments='721' id='855' iicount='6908' return='723'/>",
				"      <call method='855' inline='1' count='-1' prof_factor='0.00590717'/>",
				"      <inline_success reason='inline (hot)'/>",
				"      <parse method='855' stamp='2.386' uses='-1'>",
				"        <bc code='183' bci='1'/>",
				"        <klass name='java/lang/Object' flags='1' id='728'/>",
				"        <method bytes='1' name='&lt;init&gt;' flags='1' holder='728' id='850' compile_id='10' compiler='C2' iicount='10171' return='723'/>",
				"        <call method='850' inline='1' count='3610' prof_factor='1'/>",
				"        <inline_success reason='inline (hot)'/>",
				"        <parse method='850' stamp='2.386' uses='6910'>",
				"          <parse_done nodes='783' memory='173688' stamp='2.386' live='555'/>",
				"        </parse>",
				"        <parse_done nodes='1000' memory='207344' stamp='2.386' live='771'/>",
				"      </parse>",
				"      <parse_done nodes='1009' memory='210416' stamp='2.386' live='779'/>",
				"    </parse>",
				"    <phase_done nodes='1009' name='parse' stamp='2.386' live='663'/>",
				"  </phase>",
				"  <phase nodes='1009' name='optimizer' stamp='2.386' live='663'>",
				"    <phase nodes='1021' name='idealLoop' stamp='2.387' live='565'>",
				"      <loop_tree>",
				"        <loop idx='1021'>",
				"          <loop idx='1028'>",
				"          </loop>",
				"        </loop>",
				"      </loop_tree>",
				"      <phase_done nodes='1029' name='idealLoop' stamp='2.387' live='563'/>",
				"    </phase>",
				"    <phase nodes='1029' name='escapeAnalysis' stamp='2.387' live='563'>",
				"      <phase nodes='1029' name='connectionGraph' stamp='2.387' live='563'>",
				"        <phase_done nodes='1029' name='connectionGraph' stamp='2.387' live='563'/>",
				"      </phase>",
				"      <phase_done nodes='1029' name='escapeAnalysis' stamp='2.387' live='563'/>",
				"    </phase>",
				"    <phase nodes='1029' name='idealLoop' stamp='2.387' live='563'>",
				"      <loop_tree>",
				"        <loop idx='1021'>",
				"          <loop idx='1028' inner_loop='1'>",
				"          </loop>",
				"        </loop>",
				"      </loop_tree>",
				"      <phase_done nodes='1110' name='idealLoop' stamp='2.388' live='565'/>",
				"    </phase>",
				"    <phase nodes='1110' name='idealLoop' stamp='2.388' live='565'>",
				"      <loop_tree>",
				"        <loop idx='1021'>",
				"          <loop idx='1028' inner_loop='1'>",
				"          </loop>",
				"        </loop>",
				"      </loop_tree>",
				"      <phase_done nodes='1129' name='idealLoop' stamp='2.388' live='567'/>",
				"    </phase>",
				"    <phase nodes='1129' name='idealLoop' stamp='2.388' live='567'>",
				"      <loop_tree>",
				"        <loop idx='1021'>",
				"          <loop idx='1236' main_loop='1236' inner_loop='1'>",
				"          </loop>",
				"        </loop>",
				"      </loop_tree>",
				"      <phase_done nodes='1254' name='idealLoop' stamp='2.389' live='670'/>",
				"    </phase>",
				"    <phase nodes='1254' name='ccp' stamp='2.389' live='670'>",
				"      <phase_done nodes='1254' name='ccp' stamp='2.389' live='670'/>",
				"    </phase>",
				"    <phase nodes='1272' name='idealLoop' stamp='2.389' live='655'>",
				"      <loop_tree>",
				"        <loop idx='1021'>",
				"          <loop pre_loop='1028' idx='1188' inner_loop='1'>",
				"          </loop>",
				"          <loop idx='1346' main_loop='1346' inner_loop='1'>",
				"          </loop>",
				"          <loop idx='1143' post_loop='1028' inner_loop='1'>",
				"          </loop>",
				"        </loop>",
				"      </loop_tree>",
				"      <phase_done nodes='1396' name='idealLoop' stamp='2.390' live='700'/>",
				"    </phase>",
				"    <phase nodes='1396' name='idealLoop' stamp='2.390' live='700'>",
				"      <loop_tree>",
				"        <loop idx='1021'>",
				"          <loop pre_loop='1028' idx='1188' inner_loop='1'>",
				"          </loop>",
				"          <loop idx='1346' main_loop='1346' inner_loop='1'>",
				"          </loop>",
				"          <loop idx='1143' post_loop='1028' inner_loop='1'>",
				"          </loop>",
				"        </loop>",
				"      </loop_tree>",
				"      <phase_done nodes='1472' name='idealLoop' stamp='2.391' live='706'/>",
				"    </phase>",
				"    <phase nodes='1472' name='idealLoop' stamp='2.391' live='706'>",
				"      <loop_tree>",
				"        <loop idx='1021'>",
				"          <loop pre_loop='1028' idx='1188' inner_loop='1'>",
				"          </loop>",
				"          <loop idx='1555' main_loop='1555' inner_loop='1'>",
				"          </loop>",
				"          <loop idx='1143' post_loop='1028' inner_loop='1'>",
				"          </loop>",
				"        </loop>",
				"      </loop_tree>",
				"      <phase_done nodes='1640' name='idealLoop' stamp='2.391' live='794'/>",
				"    </phase>",
				"    <phase nodes='1640' name='idealLoop' stamp='2.391' live='794'>",
				"      <loop_tree>",
				"        <loop idx='1021'>",
				"          <loop pre_loop='1028' idx='1188' inner_loop='1'>",
				"          </loop>",
				"          <loop idx='1555' main_loop='1555' inner_loop='1'>",
				"          </loop>",
				"          <loop idx='1143' post_loop='1028' inner_loop='1'>",
				"          </loop>",
				"        </loop>",
				"      </loop_tree>",
				"      <phase_done nodes='1706' name='idealLoop' stamp='2.392' live='767'/>",
				"    </phase>",
				"    <phase nodes='1706' name='idealLoop' stamp='2.392' live='767'>",
				"      <loop_tree>",
				"        <loop idx='1021'>",
				"          <loop pre_loop='1028' idx='1188' inner_loop='1'>",
				"          </loop>",
				"          <loop idx='1555' main_loop='1555' inner_loop='1'>",
				"          </loop>",
				"          <loop idx='1143' post_loop='1028' inner_loop='1'>",
				"          </loop>",
				"        </loop>",
				"      </loop_tree>",
				"      <phase_done nodes='1764' name='idealLoop' stamp='2.393' live='746'/>",
				"    </phase>",
				"    <phase_done nodes='2155' name='optimizer' stamp='2.393' live='1037'/>",
				"  </phase>",
				"  <phase nodes='2155' name='matcher' stamp='2.393' live='1037'>",
				"    <phase_done nodes='960' name='matcher' stamp='2.395' live='960'/>",
				"  </phase>",
				"  <phase nodes='1287' name='regalloc' stamp='2.396' live='1284'>",
				"    <regalloc success='1' attempts='1'/>",
				"    <phase_done nodes='1931' name='regalloc' stamp='2.403' live='1688'/>",
				"  </phase>",
				"  <phase nodes='1937' name='output' stamp='2.403' live='1694'>",
				"    <phase_done nodes='1985' name='output' stamp='2.404' live='1715'/>",
				"  </phase>",
				"  <code_cache nmethods='187' free_code_cache='49351872' adapters='234' total_blobs='471'/>",
				"  <task_done inlined_bytes='42' success='1' count='5000' backedge_count='5000' stamp='2.404' nmsize='3920'/>",
				"</task>" };

		String[] bytecodeLines = new String[] {
				"  0: aconst_null     ",
				"  1: astore_2        ",
				"  2: aload_0         ",
				"  3: getfield        #21  // Field lock:Ljava/lang/Object;",
				"  6: dup             ",
				"  7: astore          4    ",
				"  9: monitorenter    ",
				" 10: aload_0         ",
				" 11: invokespecial   #22  // Method ensureOpen:()V",
				" 14: iload_1         ",
				" 15: ifne            25   ",
				" 18: aload_0         ",
				" 19: getfield        #5   // Field skipLF:Z",
				" 22: ifeq            29   ",
				" 25: iconst_1        ",
				" 26: goto            30   ",
				" 29: iconst_0        ",
				" 30: istore          5    ",
				" 32: aload_0         ",
				" 33: getfield        #13  // Field nextChar:I",
				" 36: aload_0         ",
				" 37: getfield        #12  // Field nChars:I",
				" 40: if_icmplt       47   ",
				" 43: aload_0         ",
				" 44: invokespecial   #23  // Method fill:()V",
				" 47: aload_0         ",
				" 48: getfield        #13  // Field nextChar:I",
				" 51: aload_0         ",
				" 52: getfield        #12  // Field nChars:I",
				" 55: if_icmplt       82   ",
				" 58: aload_2         ",
				" 59: ifnull          77   ",
				" 62: aload_2         ",
				" 63: invokevirtual   #29  // Method java/lang/StringBuffer.length:()I",
				" 66: ifle            77   ",
				" 69: aload_2         ",
				" 70: invokevirtual   #30  // Method java/lang/StringBuffer.toString:()Ljava/lang/String;",
				" 73: aload           4    ",
				" 75: monitorexit     ",
				" 76: areturn         ",
				" 77: aconst_null     ",
				" 78: aload           4    ",
				" 80: monitorexit     ",
				" 81: areturn         ",
				" 82: iconst_0        ",
				" 83: istore          6    ",
				" 85: iconst_0        ",
				" 86: istore          7    ",
				" 88: iload           5    ",
				" 90: ifeq            117  ",
				" 93: aload_0         ",
				" 94: getfield        #11  // Field cb:[C",
				" 97: aload_0         ",
				" 98: getfield        #13  // Field nextChar:I",
				"101: caload          ",
				"102: bipush          10   ",
				"104: if_icmpne       117  ",
				"107: aload_0         ",
				"108: dup             ",
				"109: getfield        #13  // Field nextChar:I",
				"112: iconst_1        ",
				"113: iadd            ",
				"114: putfield        #13  // Field nextChar:I",
				"117: aload_0         ",
				"118: iconst_0        ",
				"119: putfield        #5   // Field skipLF:Z",
				"122: iconst_0        ",
				"123: istore          5    ",
				"125: aload_0         ",
				"126: getfield        #13  // Field nextChar:I",
				"129: istore          8    ",
				"131: iload           8    ",
				"133: aload_0         ",
				"134: getfield        #12  // Field nChars:I",
				"137: if_icmpge       175  ",
				"140: aload_0         ",
				"141: getfield        #11  // Field cb:[C",
				"144: iload           8    ",
				"146: caload          ",
				"147: istore          7    ",
				"149: iload           7    ",
				"151: bipush          10   ",
				"153: if_icmpeq       163  ",
				"156: iload           7    ",
				"158: bipush          13   ",
				"160: if_icmpne       169  ",
				"163: iconst_1        ",
				"164: istore          6    ",
				"166: goto            175  ",
				"169: iinc            8, 1 ",
				"172: goto            131  ",
				"175: aload_0         ",
				"176: getfield        #13  // Field nextChar:I",
				"179: istore_3        ",
				"180: aload_0         ",
				"181: iload           8    ",
				"183: putfield        #13  // Field nextChar:I",
				"186: iload           6    ",
				"188: ifeq            264  ",
				"191: aload_2         ",
				"192: ifnonnull       216  ",
				"195: new             #31  // class java/lang/String",
				"198: dup             ",
				"199: aload_0         ",
				"200: getfield        #11  // Field cb:[C",
				"203: iload_3         ",
				"204: iload           8    ",
				"206: iload_3         ",
				"207: isub            ",
				"208: invokespecial   #32  // Method java/lang/String.\"<init>\":([CII)V",
				"211: astore          9    ",
				"213: goto            236  ",
				"216: aload_2         ",
				"217: aload_0         ",
				"218: getfield        #11  // Field cb:[C",
				"221: iload_3         ",
				"222: iload           8    ",
				"224: iload_3         ",
				"225: isub            ",
				"226: invokevirtual   #33  // Method java/lang/StringBuffer.append:([CII)Ljava/lang/StringBuffer;",
				"229: pop             ",
				"230: aload_2         ",
				"231: invokevirtual   #30  // Method java/lang/StringBuffer.toString:()Ljava/lang/String;",
				"234: astore          9    ",
				"236: aload_0         ",
				"237: dup             ",
				"238: getfield        #13  // Field nextChar:I",
				"241: iconst_1        ",
				"242: iadd            ",
				"243: putfield        #13  // Field nextChar:I",
				"246: iload           7    ",
				"248: bipush          13   ",
				"250: if_icmpne       258  ",
				"253: aload_0         ",
				"254: iconst_1        ",
				"255: putfield        #5   // Field skipLF:Z",
				"258: aload           9    ",
				"260: aload           4    ",
				"262: monitorexit     ",
				"263: areturn         ",
				"264: aload_2         ",
				"265: ifnonnull       279  ",
				"268: new             #34  // class java/lang/StringBuffer",
				"271: dup             ",
				"272: getstatic       #35  // Field defaultExpectedLineLength:I",
				"275: invokespecial   #36  // Method java/lang/StringBuffer.\"<init>\":(I)V",
				"278: astore_2        ",
				"279: aload_2         ",
				"280: aload_0         ",
				"281: getfield        #11  // Field cb:[C",
				"284: iload_3         ",
				"285: iload           8    ",
				"287: iload_3         ",
				"288: isub            ",
				"289: invokevirtual   #33  // Method java/lang/StringBuffer.append:([CII)Ljava/lang/StringBuffer;",
				"292: pop             ",
				"293: goto            32   ",
				"296: astore          10   ",
				"298: aload           4    ",
				"300: monitorexit     ",
				"301: aload           10   ",
				"303: athrow          " };

		IMetaMember member = UnitTestUtil.createTestMetaMember("java.io.BufferedReader", "readLine", new Class[] { boolean.class },
				java.lang.String.class);

		JITDataModel model = new JITDataModel();
		model.setVmVersionRelease("1.8.0");

		BytecodeAnnotations result = UnitTestUtil.buildAnnotations(true, false, model, member, logLines, bytecodeLines);

		BytecodeAnnotationList list = result.getAnnotationList(member);

		assertEquals(30, list.annotatedLineCount());

		UnitTestUtil.checkAnnotation(list, 9, "null_check", BCAnnotationType.UNCOMMON_TRAP);
		UnitTestUtil.checkAnnotation(list, 11, "BufferedReader", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 15, "taken", BCAnnotationType.BRANCH);
		UnitTestUtil.checkAnnotation(list, 15, "unstable_if", BCAnnotationType.UNCOMMON_TRAP);
		UnitTestUtil.checkAnnotation(list, 22, "taken", BCAnnotationType.BRANCH);
		UnitTestUtil.checkAnnotation(list, 32, "predicate", BCAnnotationType.UNCOMMON_TRAP);
		UnitTestUtil.checkAnnotation(list, 32, "loop_limit_check", BCAnnotationType.UNCOMMON_TRAP);
		UnitTestUtil.checkAnnotation(list, 40, "taken", BCAnnotationType.BRANCH);
		UnitTestUtil.checkAnnotation(list, 44, "BufferedReader", BCAnnotationType.INLINE_FAIL);
		UnitTestUtil.checkAnnotation(list, 55, "taken", BCAnnotationType.BRANCH);
		UnitTestUtil.checkAnnotation(list, 59, "taken", BCAnnotationType.BRANCH);
		UnitTestUtil.checkAnnotation(list, 63, "StringBuffer", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 66, "taken", BCAnnotationType.BRANCH);
		UnitTestUtil.checkAnnotation(list, 70, "StringBuffer", BCAnnotationType.INLINE_FAIL);
		UnitTestUtil.checkAnnotation(list, 90, "taken", BCAnnotationType.BRANCH);
		UnitTestUtil.checkAnnotation(list, 101, "null_check", BCAnnotationType.UNCOMMON_TRAP);
		UnitTestUtil.checkAnnotation(list, 101, "range_check", BCAnnotationType.UNCOMMON_TRAP);
		UnitTestUtil.checkAnnotation(list, 104, "taken", BCAnnotationType.BRANCH);
		UnitTestUtil.checkAnnotation(list, 104, "unstable_if", BCAnnotationType.UNCOMMON_TRAP);
		UnitTestUtil.checkAnnotation(list, 137, "taken", BCAnnotationType.BRANCH);
		UnitTestUtil.checkAnnotation(list, 140, "predicate", BCAnnotationType.UNCOMMON_TRAP);
		UnitTestUtil.checkAnnotation(list, 140, "loop_limit_check", BCAnnotationType.UNCOMMON_TRAP);
		UnitTestUtil.checkAnnotation(list, 146, "null_check", BCAnnotationType.UNCOMMON_TRAP);
		UnitTestUtil.checkAnnotation(list, 146, "range_check", BCAnnotationType.UNCOMMON_TRAP);
		UnitTestUtil.checkAnnotation(list, 153, "taken", BCAnnotationType.BRANCH);
		UnitTestUtil.checkAnnotation(list, 160, "taken", BCAnnotationType.BRANCH);
		UnitTestUtil.checkAnnotation(list, 188, "taken", BCAnnotationType.BRANCH);
		UnitTestUtil.checkAnnotation(list, 192, "taken", BCAnnotationType.BRANCH);
		UnitTestUtil.checkAnnotation(list, 208, "java.lang.String", BCAnnotationType.INLINE_FAIL);
		UnitTestUtil.checkAnnotation(list, 226, "java.lang.StringBuffer", BCAnnotationType.INLINE_FAIL);
		UnitTestUtil.checkAnnotation(list, 231, "java.lang.StringBuffer", BCAnnotationType.INLINE_FAIL);
		UnitTestUtil.checkAnnotation(list, 250, "0.902439", BCAnnotationType.BRANCH);
		UnitTestUtil.checkAnnotation(list, 265, "taken", BCAnnotationType.BRANCH);
		UnitTestUtil.checkAnnotation(list, 275, "java.lang.StringBuffer", BCAnnotationType.INLINE_SUCCESS);
		UnitTestUtil.checkAnnotation(list, 289, "java.lang.StringBuffer", BCAnnotationType.INLINE_FAIL);
		UnitTestUtil.checkAnnotation(list, 303, "null_check", BCAnnotationType.UNCOMMON_TRAP);
	}

	@Test
	public void testRegressionMemberMatchesParseTagForVarArgs() throws ClassNotFoundException, LogParseException
	{
		String methodName = "format";
		String klassName = "java.lang.String";

		IParseDictionary parseDictionary = new ParseDictionary(methodName);

		TagProcessor tagProcessor = new TagProcessor();

		parseDictionary.putType("722", tagProcessor.processLine("<type name='int' id='722'/>"));
		parseDictionary.putType("724", tagProcessor.processLine("<type name='void' id='724'/>"));
		parseDictionary.putType("716", tagProcessor.processLine("<type name='boolean' id='716'/>"));
		parseDictionary.putType("717", tagProcessor.processLine("<type name='char' id='717'/>"));

		parseDictionary.putKlass("793",
				tagProcessor.processLine("<klass name='java/lang/AbstractStringBuilder' flags='1024' id='793'/>"));
		parseDictionary.putKlass("795", tagProcessor.processLine("<klass name='java/lang/StringBuilder' flags='17' id='795'/>"));
		parseDictionary.putKlass("850", tagProcessor.processLine("<klass name='java/lang/Appendable' flags='1537' id='850'/>"));
		parseDictionary.putKlass("730", tagProcessor.processLine("<klass name='java/lang/String' flags='17' id='730'/>"));
		parseDictionary.putKlass("841", tagProcessor.processLine("<klass name='java/util/Locale' flags='17' id='841'/>"));
		parseDictionary.putKlass("832", tagProcessor.processLine("<klass name='[Ljava/lang/Object;' flags='1041' id='832'/>"));
		parseDictionary.putKlass("876",
				tagProcessor.processLine("<klass unloaded='1' name='java/util/FormatterClosedException' id='876'/>"));
		parseDictionary.putKlass("855",
				tagProcessor.processLine("<klass name='java/text/DecimalFormatSymbols' flags='1' id='855'/>"));
		parseDictionary.putKlass("835", tagProcessor.processLine("<klass name='java/util/Formatter' flags='17' id='835'/>"));
		parseDictionary.putKlass("838",
				tagProcessor.processLine("<klass name='java/util/Locale$Category' flags='16409' id='838'/>"));
		parseDictionary.putKlass("729", tagProcessor.processLine("<klass name='java/lang/Object' flags='1' id='729'/>"));

		parseDictionary.putMethod("861", tagProcessor.processLine(
				"<method level='3' bytes='37' name='getInstance' flags='25' holder='855' arguments='841' id='861' compile_id='1395' compiler='C1' iicount='1448' return='855'/>"));
		parseDictionary.putMethod("872", tagProcessor
				.processLine("<method bytes='12' name='toString' flags='1' holder='835' id='872' iicount='1454' return='730'/>"));
		parseDictionary.putMethod("851", tagProcessor.processLine(
				"<method bytes='23' name='&lt;init&gt;' flags='2' holder='835' arguments='841 850' id='851' iicount='1441' return='724'/>"));
		parseDictionary.putMethod("874", tagProcessor.processLine(
				"<method level='3' bytes='16' name='ensureOpen' flags='2' holder='835' id='874' compile_id='1364' compiler='C1' iicount='2911' return='724'/>"));
		parseDictionary.putMethod("842", tagProcessor.processLine(
				"<method bytes='132' name='getDefault' flags='9' holder='841' arguments='838' id='842' iicount='1434' return='841'/>"));
		parseDictionary.putMethod("853", tagProcessor.processLine(
				"<method bytes='27' name='getZero' flags='10' holder='835' arguments='841' id='853' iicount='1444' return='717'/>"));
		parseDictionary.putMethod("865", tagProcessor.processLine(
				"<method level='1' bytes='5' name='getZeroDigit' flags='1' holder='855' id='865' compile_id='1332' compiler='C1' iicount='151' return='717'/>"));
		parseDictionary.putMethod("833", tagProcessor.processLine(
				"<method bytes='16' name='format' flags='137' holder='730' arguments='730 832' id='833' iicount='1430' return='730'/>"));
		parseDictionary.putMethod("844", tagProcessor.processLine(
				"<method level='4' bytes='7' name='&lt;init&gt;' flags='1' holder='795' id='844' compile_id='1357' compiler='C2' iicount='5341' return='724'/>"));
		parseDictionary.putMethod("877",
				tagProcessor.processLine("<method unloaded='1' name='&lt;init&gt;' holder='876' id='877' return='724'/>"));
		parseDictionary.putMethod("867", tagProcessor.processLine(
				"<method bytes='11' name='format' flags='129' holder='835' arguments='730 832' id='867' iicount='1452' return='835'/>"));
		parseDictionary.putMethod("878", tagProcessor
				.processLine("<method bytes='36' name='toString' flags='1' holder='729' id='878' iicount='1' return='730'/>"));
		parseDictionary.putMethod("846", tagProcessor.processLine(
				"<method level='3' bytes='12' name='&lt;init&gt;' flags='0' holder='793' arguments='722' id='846' compile_id='53' compiler='C1' iicount='8594' return='724'/>"));
		parseDictionary.putMethod("836", tagProcessor.processLine(
				"<method bytes='18' name='&lt;init&gt;' flags='1' holder='835' id='836' iicount='1432' return='724'/>"));
		parseDictionary.putMethod("858", tagProcessor.processLine(
				"<method level='3' bytes='75' name='equals' flags='1' holder='841' arguments='729' id='858' compile_id='1377' compiler='C1' iicount='1501' return='716'/>"));
		parseDictionary.putMethod("869", tagProcessor.processLine(
				"<method level='3' bytes='271' name='format' flags='129' holder='835' arguments='841 730 832' id='869' compile_id='1383' compiler='C1' iicount='1453' return='835'/>"));
		parseDictionary.putMethod("848", tagProcessor.processLine(
				"<method level='1' bytes='1' name='&lt;init&gt;' flags='1' holder='729' id='848' compile_id='19' compiler='C1' iicount='80409' return='724'/>"));

		MetaClass metaClass = UnitTestUtil.createMetaClassFor(new JITDataModel(), klassName);
		
		MemberSignatureParts msp = MemberSignatureParts.fromLogCompilationSignature("java/lang/String format (Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;");
				
		IMetaMember member = metaClass.getMemberForSignature(msp);

		assertTrue(CompilationUtil.memberMatchesMethodID(member, "833", parseDictionary));
	}
}