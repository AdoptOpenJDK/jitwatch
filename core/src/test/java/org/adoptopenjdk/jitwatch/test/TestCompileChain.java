/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.adoptopenjdk.jitwatch.chain.CompileChainWalker;
import org.adoptopenjdk.jitwatch.chain.CompileNode;
import org.adoptopenjdk.jitwatch.compilation.CompilationUtil;
import org.adoptopenjdk.jitwatch.core.TagProcessor;
import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.JITDataModel;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.model.Task;
import org.adoptopenjdk.jitwatch.util.StringUtil;
import org.junit.After;
import org.junit.Test;

public class TestCompileChain
{
	@After
	public void checkUnhandledTags()
	{
		assertEquals(0, CompilationUtil.getUnhandledTagCount());
	}
	
	@Test
	public void testRegressionTwoInlinesC2() throws Exception
	{
		String[] lines = new String[] {
				"<task compile_id='2' compile_kind='osr' method='PolymorphismTest &lt;init&gt; ()V' bytes='132' count='1' backedge_count='14563' iicount='1' osr_bci='40' blocking='1' stamp='0.500'>",
				"<phase name='parse' nodes='3' live='3' stamp='0.500'>",
				"<type id='709' name='void'/>",
				"<klass id='817' name='PolymorphismTest' flags='1'/>",
				"<method id='818' holder='817' name='&lt;init&gt;' return='709' flags='1' bytes='132' iicount='1'/>",
				"<klass id='828' name='java/lang/System' unloaded='1'/>",
				"<uncommon_trap method='818' bci='104' reason='unloaded' action='reinterpret' index='60' klass='828'/>",
				"<uncommon_trap method='818' bci='104' reason='unloaded' action='reinterpret' index='60' klass='828'/>",
				"<parse method='818' uses='1' osr_bci='40' stamp='0.500'>",
				"<uncommon_trap method='818' bci='104' reason='unloaded' action='reinterpret' index='60' klass='828'/>",
				"<uncommon_trap method='818' bci='104' reason='unloaded' action='reinterpret' index='60' klass='828'/>",
				"<klass id='821' name='PolymorphismTest$Nickel' flags='1'/>",
				"<dependency type='leaf_type' ctxk='821'/>",
				"<dependency type='leaf_type' ctxk='821'/>",
				"<klass id='820' name='PolymorphismTest$Dime' flags='1'/>",
				"<dependency type='leaf_type' ctxk='820'/>",
				"<dependency type='leaf_type' ctxk='820'/>",
				"<klass id='825' name='PolymorphismTest$Quarter' flags='1'/>",
				"<dependency type='leaf_type' ctxk='825'/>",
				"<dependency type='leaf_type' ctxk='825'/>",
				"<uncommon_trap bci='40' reason='constraint' action='reinterpret'/>",
				"<uncommon_trap bci='40' reason='predicate' action='maybe_recompile'/>",
				"<uncommon_trap bci='40' reason='loop_limit_check' action='maybe_recompile'/>",
				"<bc code='162' bci='44'/>",
				"<branch target_bci='104' taken='0' not_taken='11264' cnt='11264' prob='never'/>",
				"<uncommon_trap bci='44' reason='unstable_if' action='reinterpret' comment='taken never'/>",
				"<bc code='185' bci='93'/>",
				"<klass id='829' name='PolymorphismTest$Coin' flags='1545'/>",
				"<method id='830' holder='829' name='deposit' return='709' flags='1025' bytes='0' iicount='1'/>",
				"<call method='830' count='11264' prof_factor='1' virtual='1' inline='1' receiver='820' receiver_count='5632' receiver2='821' receiver2_count='5632'/>",
				"<method id='831' holder='820' name='deposit' return='709' flags='1' bytes='10' iicount='7281'/>",
				"<call method='831' count='11264' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<method id='832' holder='821' name='deposit' return='709' flags='1' bytes='9' iicount='7282'/>",
				"<call method='832' count='11264' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<predicted_call bci='93' klass='820'/>",
				"<uncommon_trap bci='93' reason='null_check' action='maybe_recompile'/>",
				"<predicted_call bci='93' klass='821'/>",
				"<uncommon_trap bci='93' reason='bimorphic' action='maybe_recompile'/>",
				"<parse method='832' uses='11264' stamp='0.500'>",
				"<parse_done nodes='270' live='263' memory='68048' stamp='0.500'/>",
				"</parse>",
				"<parse method='831' uses='11264' stamp='0.500'>",
				"<parse_done nodes='287' live='279' memory='71104' stamp='0.500'/>",
				"</parse>",
				"<parse_done nodes='296' live='288' memory='72840' stamp='0.500'/>",
				"</parse>",
				"<phase_done name='parse' nodes='296' live='200' stamp='0.500'/>",
				"</phase>",
				"<phase name='optimizer' nodes='296' live='200' stamp='0.500'>",
				"<phase name='idealLoop' nodes='320' live='191' stamp='0.500'>",
				"<loop_tree>",
				"<loop idx='320' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='433' live='203' stamp='0.501'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='433' live='203' stamp='0.501'>",
				"<loop_tree>",
				"<loop idx='626' main_loop='626' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='711' live='406' stamp='0.501'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='711' live='406' stamp='0.501'>",
				"<loop_tree>",
				"<loop idx='540' inner_loop='1' pre_loop='438' >",
				"</loop>",
				"<loop idx='626' inner_loop='1' main_loop='626' >",
				"</loop>",
				"<loop idx='463' inner_loop='1' post_loop='438' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='716' live='411' stamp='0.501'/>",
				"</phase>",
				"<phase name='ccp' nodes='716' live='411' stamp='0.501'>",
				"<phase_done name='ccp' nodes='716' live='411' stamp='0.501'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='734' live='407' stamp='0.501'>",
				"<loop_tree>",
				"<loop idx='540' inner_loop='1' pre_loop='438' >",
				"</loop>",
				"<loop idx='626' inner_loop='1' main_loop='626' >",
				"</loop>",
				"<loop idx='463' inner_loop='1' post_loop='438' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='774' live='404' stamp='0.502'/>",
				"</phase>",
				"<phase_done name='optimizer' nodes='796' live='401' stamp='0.502'/>",
				"</phase>",
				"<phase name='matcher' nodes='796' live='401' stamp='0.502'>",
				"<phase_done name='matcher' nodes='358' live='358' stamp='0.502'/>",
				"</phase>",
				"<phase name='regalloc' nodes='502' live='502' stamp='0.502'>",
				"<regalloc attempts='0' success='1'/>",
				"<phase_done name='regalloc' nodes='559' live='547' stamp='0.503'/>",
				"</phase>",
				"<phase name='output' nodes='559' live='547' stamp='0.503'>",
				"<phase_done name='output' nodes='599' live='554' stamp='0.503'/>",
				"</phase>",
				"<dependency type='leaf_type' ctxk='821'/>",
				"<dependency type='leaf_type' ctxk='820'/>",
				"<dependency type='leaf_type' ctxk='825'/>",
				"<code_cache total_blobs='206' nmethods='2' adapters='154' free_code_cache='49831040'/>",
				"<task_done success='1' nmsize='760' count='1' backedge_count='14563' stamp='0.794'/>",
				"</task>" };

		CompileNode root = buildCompileNodeForXML(lines);

		// root
		// -> deposit()
		// -> deposit()

		List<CompileNode> rootChildren = root.getChildren();

		assertEquals(2, rootChildren.size());

		CompileNode c0 = rootChildren.get(0);
		CompileNode c1 = rootChildren.get(1);

		assertEquals("deposit", c0.getMemberName());
		assertTrue(c0.isInlined());

		assertEquals("deposit", c1.getMemberName());
		assertTrue(c1.isInlined());
	}

	public void testJava8TieredCompilation() throws Exception
	{
		String[] lines = new String[] {
				"<task osr_bci='8' method='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog testCallChain3 ()V' compile_kind='osr' level='3' bytes='71' count='1' backedge_count='60494' stamp='13.088' compile_id='127' iicount='1'>",
				"<phase name='buildIR' stamp='13.088'>",
				"<type name='void' id='680'/>",
				"<klass name='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog' flags='1' id='776'/>",
				"<method bytes='71' name='testCallChain3' flags='2' holder='776' id='777' iicount='1' return='680'/>",
				"<parse method='777' stamp='13.088'><!-- void org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog.testCallChain3() -->",
				"<bc code='183' bci='18'/>",
				"<type name='boolean' id='672'/>",
				"<type name='int' id='678'/>",
				"<method level='4' bytes='18' name='test' flags='2' holder='776' arguments='678 678' id='779' compile_id='125' compiler='C2' iicount='7613' return='672'/>",
				"<call method='779' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='26'/>",
				"<type name='long' id='679'/>",
				"<method level='4' bytes='16' name='chainC1' flags='2' holder='776' arguments='679' id='781' compile_id='126' compiler='C2' iicount='32985' return='679'/>",
				"<call method='781' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='5'/>",
				"<method level='2' bytes='6' name='chainC2' flags='2' holder='776' arguments='679' id='783' compile_id='123' compiler='C1' iicount='32985' return='679'/>",
				"<call method='783' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='12'/>",
				"<method level='3' bytes='6' name='chainC3' flags='2' holder='776' arguments='679' id='785' compile_id='124' compiler='C1' iicount='32985' return='679'/>",
				"<call method='785' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='35'/>",
				"<call method='783' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='52'/>",
				"<klass name='java/lang/StringBuilder' flags='17' id='749'/>",
				"<method bytes='7' name='<init>' flags='1' holder='749' id='789' iicount='117' return='680'/>",
				"<call method='789' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='3'/>",
				"<klass name='java/lang/AbstractStringBuilder' flags='1024' id='747'/>",
				"<method bytes='12' name='<init>' flags='0' holder='747' arguments='678' id='791' iicount='130' return='680'/>",
				"<call method='791' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='1'/>",
				"<klass name='java/lang/Object' flags='1' id='685'/>",
				"<method level='1' bytes='1' name='<init>' flags='1' holder='685' id='793' compile_id='12' compiler='C1' iicount='749967' return='680'/>",
				"<call method='793' instr='invokespecial'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='182' bci='57'/>",
				"<klass name='java/lang/String' flags='17' id='686'/>",
				"<method bytes='8' name='append' flags='1' holder='749' arguments='686' id='796' iicount='215' return='749'/>",
				"<call method='796' instr='invokevirtual'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='2'/>",
				"<method bytes='50' name='append' flags='1' holder='747' arguments='686' id='798' iicount='248' return='747'/>",
				"<call method='798' instr='invokespecial'/>",
				"<inline_fail reason='callee is too large'/>",
				"<bc code='182' bci='61'/>",
				"<method bytes='8' name='append' flags='1' holder='749' arguments='679' id='800' iicount='9' return='749'/>",
				"<call method='800' instr='invokevirtual'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='2'/>",
				"<method bytes='70' name='append' flags='1' holder='747' arguments='679' id='802' iicount='9' return='747'/>",
				"<call method='802' instr='invokespecial'/>",
				"<inline_fail reason='callee is too large'/>",
				"<bc code='182' bci='64'/>",
				"<method bytes='17' name='toString' flags='1' holder='749' id='804' iicount='116' return='686'/>",
				"<call method='804' instr='invokevirtual'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='13'/>",
				"<klass name='[C' flags='1041' id='765'/>",
				"<method bytes='62' name='<init>' flags='1' holder='686' arguments='765 678 678' id='806' iicount='278' return='680'/>",
				"<call method='806' instr='invokespecial'/>",
				"<inline_fail reason='callee is too large'/>",
				"<bc code='182' bci='67'/>",
				"<klass name='java/io/PrintStream' flags='1' id='787'/>",
				"<method bytes='24' name='println' flags='1' holder='787' arguments='686' id='808' iicount='9' return='680'/>",
				"<call method='808' instr='invokevirtual'/>",
				"<dependency ctxk='787' type='leaf_type'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='182' bci='6'/>",
				"<method bytes='13' name='print' flags='1' holder='787' arguments='686' id='810' iicount='9' return='680'/>",
				"<call method='810' instr='invokevirtual'/>",
				"<dependency ctxk='787' type='leaf_type'/>",
				"<inline_success reason='receiver is statically known'/>",
				"<bc code='183' bci='9'/>",
				"<method bytes='83' name='write' flags='2' holder='787' arguments='686' id='812' iicount='9' return='680'/>",
				"<call method='812' instr='invokespecial'/>",
				"<dependency ctxk='787' type='leaf_type'/>",
				"<inline_fail reason='callee is too large'/>",
				"<bc code='183' bci='10'/>",
				"<method bytes='73' name='newLine' flags='2' holder='787' id='816' iicount='9' return='680'/>",
				"<call method='816' instr='invokespecial'/>",
				"<dependency ctxk='787' type='leaf_type'/>",
				"<inline_fail reason='callee is too large'/>",
				"<parse_done stamp='13.091'/>",
				"</parse>",
				"<phase name='optimize_blocks' stamp='13.091'>",
				"<phase_done name='optimize_blocks' stamp='13.091'/>",
				"</phase>",
				"<phase name='optimize_null_checks' stamp='13.092'>",
				"<phase_done name='optimize_null_checks' stamp='13.092'/>",
				"</phase>",
				"<phase_done name='buildIR' stamp='13.092'/>",
				"</phase>",
				"<phase name='emit_lir' stamp='13.092'>",
				"<phase name='lirGeneration' stamp='13.092'>",
				"<phase_done name='lirGeneration' stamp='13.092'/>",
				"</phase>",
				"<phase name='linearScan' stamp='13.092'>",
				"<phase_done name='linearScan' stamp='13.094'/>",
				"</phase>",
				"<phase_done name='emit_lir' stamp='13.094'/>",
				"</phase>",
				"<phase name='codeemit' stamp='13.094'>",
				"<phase_done name='codeemit' stamp='13.094'/>",
				"</phase>",
				"<phase name='codeinstall' stamp='13.094'>",
				"<dependency ctxk='787' type='leaf_type'/>",
				"<phase_done name='codeinstall' stamp='13.173'/>",
				"</phase>",
				"<code_cache nmethods='124' free_code_cache='250227008' adapters='142' total_blobs='349' stamp='13.088'/>",
				"<task_done inlined_bytes='142' success='1' count='1' backedge_count='100000' stamp='13.173' nmsize='3624'/>",
				"</task>" };

		CompileNode root = buildCompileNodeForXML(lines);

		// private void testCallChain3()
		// {
		// long count = 0;
		// int iterations = 100_000;
		// for (int i = 0; i < iterations; i++)
		// {
		// if (test(i, iterations))
		// {
		// count = chainC1(count);
		// }
		// else
		// {
		// count = chainC2(count);
		// }
		// }
		// System.out.println("testCallChain2: " + count);
		// }

		// root
		// -> test()
		// -> chainC1() -> chainC2()
		// -> chainC3()
		// -> chainC2()
		// -> java.lang.AbstractStringBuilder() -> java.lang.Object()
		// -> append()
		// -> append()
		// -> toString()
		// -> println

		List<CompileNode> rootChildren = root.getChildren();

		assertEquals(8, rootChildren.size());

		int pos = 0;

		assertEquals("test", rootChildren.get(pos++).getMemberName());
		assertEquals("chainC1", rootChildren.get(pos++).getMemberName());
		assertEquals("chainC2", rootChildren.get(pos++).getMemberName());
		assertEquals("java.lang.AbstractStringBuilder", rootChildren.get(pos++).getMemberName());
		assertEquals("append", rootChildren.get(pos++).getMemberName());
		assertEquals("append", rootChildren.get(pos++).getMemberName());
		assertEquals("toString", rootChildren.get(pos++).getMemberName());
		assertEquals("println", rootChildren.get(pos++).getMemberName());
	}

	@Test
	public void testJava7LateInlineRegression()
	{
		String[] lines = new String[] {
				"<task compile_id='70' compile_kind='osr' method='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog testCallChain (J)V' bytes='57' count='10000' backedge_count='5317' iicount='1' osr_bci='5' stamp='11.612'>",
				"<phase name='parse' nodes='3' live='3' stamp='11.612'>",
				"<type id='636' name='void'/>",
				"<type id='635' name='long'/>",
				"<klass id='729' name='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog' flags='1'/>",
				"<method id='730' holder='729' name='testCallChain' return='636' arguments='635' flags='2' bytes='57' iicount='1'/>",
				"<parse method='730' uses='1' osr_bci='5' stamp='11.612'>",
				"<dependency type='leaf_type' ctxk='729'/>",
				"<dependency type='leaf_type' ctxk='729'/>",
				"<uncommon_trap bci='5' reason='constraint' action='reinterpret'/>",
				"<uncommon_trap bci='5' reason='predicate' action='maybe_recompile'/>",
				"<uncommon_trap bci='5' reason='loop_limit_check' action='maybe_recompile'/>",
				"<bc code='156' bci='10'/>",
				"<branch target_bci='31' taken='1' not_taken='16701' cnt='16702' prob='5.98731e-05'/>",
				"<bc code='183' bci='15'/>",
				"<method id='739' holder='729' name='chainA1' return='635' arguments='635' flags='2' bytes='8' compile_id='63' compiler='C2' iicount='10929'/>",
				"<call method='739' count='16701' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='739' uses='16701' stamp='11.613'>",
				"<uncommon_trap bci='15' reason='null_check' action='maybe_recompile'/>",
				"<bc code='183' bci='3'/>",
				"<method id='741' holder='729' name='chainA2' return='635' arguments='635' flags='2' bytes='10' compile_id='64' compiler='C2' iicount='10929'/>",
				"<call method='741' count='7630' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='741' uses='7630' stamp='11.613'>",
				"<bc code='183' bci='5'/>",
				"<method id='743' holder='729' name='chainA3' return='635' arguments='635' flags='2' bytes='10' compile_id='65' compiler='C2' iicount='10929'/>",
				"<call method='743' count='7630' prof_factor='0.698143' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='743' uses='5327' stamp='11.614'>",
				"<bc code='183' bci='5'/>",
				"<method id='745' holder='729' name='chainA4' return='635' arguments='635' flags='2' bytes='7' compile_id='66' compiler='C2' iicount='10929'/>",
				"<call method='745' count='7630' prof_factor='0.487419' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='745' uses='3719' stamp='11.614'>",
				"<bc code='183' bci='3'/>",
				"<type id='634' name='int'/>",
				"<method id='747' holder='729' name='bigMethod' return='635' arguments='635 634' flags='2' bytes='350' compile_id='15' compiler='C2' iicount='11233'/>",
				"<call method='747' count='7630' prof_factor='0.340287' inline='1'/>",
				"<inline_fail reason='hot method too big'/>",
				"<direct_call bci='3'/>",
				"<parse_done nodes='201' live='196' memory='51832' stamp='11.614'/>",
				"</parse>",
				"<parse_done nodes='204' live='198' memory='53008' stamp='11.614'/>",
				"</parse>",
				"<parse_done nodes='208' live='201' memory='54408' stamp='11.614'/>",
				"</parse>",
				"<parse_done nodes='212' live='204' memory='56944' stamp='11.614'/>",
				"</parse>",
				"<bc code='183' bci='21'/>",
				"<method id='740' holder='729' name='chainB1' return='635' arguments='635' flags='2' bytes='8' compile_id='67' compiler='C2' iicount='15458'/>",
				"<call method='740' count='16701' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='740' uses='16701' stamp='11.614'>",
				"<bc code='183' bci='2'/>",
				"<method id='749' holder='729' name='chainB2' return='635' arguments='635' flags='2' bytes='10' iicount='15458'/>",
				"<call method='749' count='12159' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='749' uses='15458' stamp='11.615'>",
				"<bc code='183' bci='2'/>",
				"<method id='751' holder='729' name='chainB3' return='635' arguments='635' flags='2' bytes='6' compile_id='69' compiler='C2' iicount='15458'/>",
				"<call method='751' count='12159' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='751' uses='15458' stamp='11.615'>",
				"<parse_done nodes='253' live='244' memory='64664' stamp='11.615'/>",
				"</parse>",
				"<parse_done nodes='256' live='246' memory='65256' stamp='11.615'/>",
				"</parse>",
				"<parse_done nodes='259' live='248' memory='70896' stamp='11.615'/>",
				"</parse>",
				"<bc code='183' bci='38'/>",
				"<klass id='704' name='java/lang/StringBuilder' flags='17'/>",
				"<method id='733' holder='704' name='&lt;init&gt;' return='636' flags='1' bytes='7' iicount='94'/>",
				"<call method='733' count='1' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<direct_call bci='38'/>",
				"<bc code='182' bci='43'/>",
				"<klass id='646' name='java/lang/String' flags='17'/>",
				"<method id='735' holder='704' name='append' return='704' arguments='646' flags='1' bytes='8' iicount='170'/>",
				"<call method='735' count='1' prof_factor='1' inline='1'/>",
				"<inline_fail reason='executed &lt; MinInliningThreshold times'/>",
				"<direct_call bci='43'/>",
				"<bc code='182' bci='47'/>",
				"<method id='736' holder='704' name='append' return='704' arguments='635' flags='1' bytes='8' iicount='8'/>",
				"<call method='736' count='1' prof_factor='1' inline='1'/>",
				"<inline_fail reason='executed &lt; MinInliningThreshold times'/>",
				"<direct_call bci='47'/>",
				"<uncommon_trap bci='47' reason='null_check' action='maybe_recompile'/>",
				"<bc code='182' bci='50'/>",
				"<method id='737' holder='704' name='toString' return='646' flags='1' bytes='17' iicount='93'/>",
				"<call method='737' count='1' prof_factor='1' inline='1'/>",
				"<inline_fail reason='executed &lt; MinInliningThreshold times'/>",
				"<direct_call bci='50'/>",
				"<uncommon_trap bci='50' reason='null_check' action='maybe_recompile'/>",
				"<bc code='182' bci='53'/>",
				"<klass id='732' name='java/io/PrintStream' flags='1'/>",
				"<method id='738' holder='732' name='println' return='636' arguments='646' flags='1' bytes='24' iicount='8'/>",
				"<dependency type='unique_concrete_method' ctxk='732' x='738'/>",
				"<call method='738' count='1' prof_factor='1' inline='1'/>",
				"<inline_fail reason='executed &lt; MinInliningThreshold times'/>",
				"<direct_call bci='53'/>",
				"<uncommon_trap bci='53' reason='null_check' action='maybe_recompile'/>",
				"<parse_done nodes='416' live='401' memory='96240' stamp='11.626'/>",
				"</parse>",
				"<late_inline method='733'>",
				"<jvms bci='38' method='730'/>",
				"</late_inline>",
				"<parse method='733' uses='1' stamp='11.626'>",
				"<bc code='183' bci='3'/>",
				"<klass id='702' name='java/lang/AbstractStringBuilder' flags='1024'/>",
				"<method id='755' holder='702' name='&lt;init&gt;' return='636' arguments='634' flags='0' bytes='12' iicount='104'/>",
				"<call method='755' count='-1' prof_factor='0.0106383' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='755' uses='-1' stamp='11.626'>",
				"<bc code='183' bci='1'/>",
				"<klass id='645' name='java/lang/Object' flags='1'/>",
				"<method id='763' holder='645' name='&lt;init&gt;' return='636' flags='1' bytes='1' compile_id='26' compiler='C2' iicount='15818'/>",
				"<call method='763' count='-1' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='763' uses='-1' stamp='11.626'>",
				"<parse_done nodes='467' live='269' memory='104112' stamp='11.627'/>",
				"</parse>",
				"<parse_done nodes='518' live='319' memory='119936' stamp='11.627'/>",
				"</parse>",
				"<parse_done nodes='524' live='324' memory='121624' stamp='11.627'/>",
				"</parse>",
				"<phase_done name='parse' nodes='524' live='243' stamp='11.627'/>",
				"</phase>",
				"<phase name='optimizer' nodes='524' live='243' stamp='11.627'>",
				"<phase name='idealLoop' nodes='529' live='236' stamp='11.627'>",
				"<loop_tree>",
				"<loop idx='529' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='530' live='236' stamp='11.628'/>",
				"</phase>",
				"<phase name='escapeAnalysis' nodes='530' live='236' stamp='11.628'>",
				"<phase name='connectionGraph' nodes='531' live='237' stamp='11.628'>",
				"<method id='772' holder='702' name='expandCapacity' return='636' arguments='634' flags='0' bytes='50' iicount='126'/>",
				"<dependency type='unique_concrete_method' ctxk='702' x='772'/>",
				"<phase_done name='connectionGraph' nodes='531' live='237' stamp='11.632'/>",
				"</phase>",
				"<phase_done name='escapeAnalysis' nodes='531' live='237' stamp='11.632'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='531' live='237' stamp='11.632'>",
				"<loop_tree>",
				"<loop idx='529' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='534' live='219' stamp='11.633'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='534' live='219' stamp='11.633'>",
				"<loop_tree>",
				"<loop idx='529' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='534' live='219' stamp='11.633'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='534' live='219' stamp='11.633'>",
				"<loop_tree>",
				"<loop idx='529' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='534' live='219' stamp='11.634'/>",
				"</phase>",
				"<phase name='ccp' nodes='534' live='219' stamp='11.635'>",
				"<phase_done name='ccp' nodes='534' live='219' stamp='11.635'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='535' live='215' stamp='11.635'>",
				"<loop_tree>",
				"<loop idx='529' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='538' live='215' stamp='11.635'/>",
				"</phase>",
				"<phase_done name='optimizer' nodes='662' live='291' stamp='11.636'/>",
				"</phase>",
				"<phase name='matcher' nodes='662' live='291' stamp='11.636'>",
				"<phase_done name='matcher' nodes='253' live='253' stamp='11.638'/>",
				"</phase>",
				"<phase name='regalloc' nodes='315' live='315' stamp='11.639'>",
				"<regalloc attempts='1' success='1'/>",
				"<phase_done name='regalloc' nodes='391' live='374' stamp='11.652'/>",
				"</phase>",
				"<phase name='output' nodes='393' live='376' stamp='11.652'>",
				"<phase_done name='output' nodes='420' live='394' stamp='11.661'/>",
				"</phase>",
				"<dependency type='leaf_type' ctxk='729'/>",
				"<dependency type='unique_concrete_method' ctxk='732' x='738'/>",
				"<dependency type='unique_concrete_method' ctxk='702' x='772'/>",
				"<code_cache total_blobs='255' nmethods='75' adapters='134' free_code_cache='49824384' largest_free_block='49810304'/>",
				"<task_done success='1' nmsize='800' count='10000' backedge_count='5317' inlined_bytes='79' stamp='11.662'/>",
				"</task>" };

		CompileNode root = buildCompileNodeForXML(lines);

		// root
		// -> chainA1() -> chainA2() -> chainA3() -> chainA4() -> bigMethod()
		// -> chainB1() -> chainB2() -> chainB3()
		// -> java.lang.AbstractStringBuilder() -> java.lang.Object()
		// -> append()
		// -> append()
		// -> toString()
		// -> println
		// -> java.lang.AbstractStringBuilder() -> java.lang.Object()

		List<CompileNode> rootChildren = root.getChildren();

		assertEquals(8, rootChildren.size());

		CompileNode c0 = rootChildren.get(0);
		CompileNode c1 = rootChildren.get(1);
		CompileNode c2 = rootChildren.get(2);
		CompileNode c3 = rootChildren.get(3);
		CompileNode c4 = rootChildren.get(4);
		CompileNode c5 = rootChildren.get(5);
		CompileNode c6 = rootChildren.get(6);
		CompileNode c7 = rootChildren.get(7);

		assertEquals("chainA1", c0.getMemberName());
		assertTrue(c0.isInlined());

		assertEquals("chainB1", c1.getMemberName());
		assertTrue(c1.isInlined());

		assertEquals("<init>", c2.getMemberName());
		assertTrue(c2.isInlined());

		assertEquals("append", c3.getMemberName());
		assertTrue(!c3.isInlined());

		assertEquals("append", c4.getMemberName());
		assertTrue(!c4.isInlined());

		assertEquals("toString", c5.getMemberName());
		assertTrue(!c5.isInlined());

		assertEquals("println", c6.getMemberName());
		assertTrue(!c6.isInlined());

		assertEquals("<init>", c7.getMemberName());
		assertTrue(c7.isInlined());

		List<CompileNode> c0Children = c0.getChildren();
		assertEquals(1, c0Children.size());
		CompileNode c0c0 = c0Children.get(0);
		assertEquals("chainA2", c0c0.getMemberName());
		assertTrue(c0c0.isInlined());

		List<CompileNode> c0c0Children = c0c0.getChildren();
		assertEquals(1, c0c0Children.size());
		CompileNode c0c0c0 = c0c0Children.get(0);
		assertEquals("chainA3", c0c0c0.getMemberName());
		assertTrue(c0c0c0.isInlined());

		List<CompileNode> c0c0c0Children = c0c0c0.getChildren();
		assertEquals(1, c0c0c0Children.size());
		CompileNode c0c0c0c0 = c0c0c0Children.get(0);
		assertEquals("chainA4", c0c0c0c0.getMemberName());
		assertTrue(c0c0c0c0.isInlined());

		List<CompileNode> c0c0c0c0Children = c0c0c0c0.getChildren();
		assertEquals(1, c0c0c0c0Children.size());
		CompileNode c0c0c0c0c0 = c0c0c0c0Children.get(0);
		assertEquals("bigMethod", c0c0c0c0c0.getMemberName());
		assertTrue(!c0c0c0c0c0.isInlined());

	}

	@Test
	public void testJava8LateInlineRegression()
	{
		String[] lines = new String[] {
				"<task compile_id='68' compile_kind='osr' method='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog testCallChain (J)V' bytes='57' count='10000' backedge_count='5317' iicount='1' osr_bci='5' stamp='11.148'>",
				"<phase name='parse' nodes='3' live='3' stamp='11.149'>",
				"<type id='680' name='void'/>",
				"<type id='679' name='long'/>",
				"<klass id='776' name='org/adoptopenjdk/jitwatch/demo/MakeHotSpotLog' flags='1'/>",
				"<method id='777' holder='776' name='testCallChain' return='680' arguments='679' flags='2' bytes='57' iicount='1'/>",
				"<parse method='777' uses='1' osr_bci='5' stamp='11.149'>",
				"<dependency type='leaf_type' ctxk='776'/>",
				"<dependency type='leaf_type' ctxk='776'/>",
				"<uncommon_trap bci='5' reason='constraint' action='reinterpret'/>",
				"<uncommon_trap bci='5' reason='predicate' action='maybe_recompile'/>",
				"<uncommon_trap bci='5' reason='loop_limit_check' action='maybe_recompile'/>",
				"<bc code='156' bci='10'/>",
				"<branch target_bci='31' taken='1' not_taken='16701' cnt='16702' prob='5.98731e-05'/>",
				"<bc code='183' bci='15'/>",
				"<method id='786' holder='776' name='chainA1' return='679' arguments='679' flags='2' bytes='8' compile_id='61' compiler='C2' iicount='10785'/>",
				"<call method='786' count='16701' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='786' uses='16701' stamp='11.151'>",
				"<uncommon_trap bci='15' reason='null_check' action='maybe_recompile'/>",
				"<bc code='183' bci='3'/>",
				"<method id='788' holder='776' name='chainA2' return='679' arguments='679' flags='2' bytes='10' compile_id='62' compiler='C2' iicount='10785'/>",
				"<call method='788' count='7486' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='788' uses='7486' stamp='11.151'>",
				"<bc code='183' bci='5'/>",
				"<method id='790' holder='776' name='chainA3' return='679' arguments='679' flags='2' bytes='10' compile_id='63' compiler='C2' iicount='10785'/>",
				"<call method='790' count='7486' prof_factor='0.694112' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='790' uses='5196' stamp='11.151'>",
				"<bc code='183' bci='5'/>",
				"<method id='792' holder='776' name='chainA4' return='679' arguments='679' flags='2' bytes='7' compile_id='64' compiler='C2' iicount='10785'/>",
				"<call method='792' count='7486' prof_factor='0.48178' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='792' uses='3607' stamp='11.151'>",
				"<bc code='183' bci='3'/>",
				"<type id='678' name='int'/>",
				"<method id='794' holder='776' name='bigMethod' return='679' arguments='679 678' flags='2' bytes='350' compile_id='15' compiler='C2' iicount='11290'/>",
				"<call method='794' count='7486' prof_factor='0.334446' inline='1'/>",
				"<inline_fail reason='hot method too big'/>",
				"<direct_call bci='3'/>",
				"<parse_done nodes='201' live='196' memory='51664' stamp='11.152'/>",
				"</parse>",
				"<parse_done nodes='204' live='198' memory='52800' stamp='11.152'/>",
				"</parse>",
				"<parse_done nodes='208' live='201' memory='54160' stamp='11.152'/>",
				"</parse>",
				"<parse_done nodes='212' live='204' memory='56656' stamp='11.152'/>",
				"</parse>",
				"<bc code='183' bci='21'/>",
				"<method id='787' holder='776' name='chainB1' return='679' arguments='679' flags='2' bytes='8' compile_id='65' compiler='C2' iicount='16240'/>",
				"<call method='787' count='16701' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='787' uses='16701' stamp='11.152'>",
				"<bc code='183' bci='2'/>",
				"<method id='796' holder='776' name='chainB2' return='679' arguments='679' flags='2' bytes='10' compile_id='66' compiler='C2' iicount='16240'/>",
				"<call method='796' count='12941' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='796' uses='16240' stamp='11.152'>",
				"<bc code='183' bci='2'/>",
				"<method id='798' holder='776' name='chainB3' return='679' arguments='679' flags='2' bytes='6' compile_id='67' compiler='C2' iicount='16240'/>",
				"<call method='798' count='12941' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='798' uses='16240' stamp='11.153'>",
				"<parse_done nodes='253' live='244' memory='64280' stamp='11.153'/>",
				"</parse>",
				"<parse_done nodes='256' live='246' memory='64872' stamp='11.153'/>",
				"</parse>",
				"<parse_done nodes='259' live='248' memory='70896' stamp='11.153'/>",
				"</parse>",
				"<bc code='183' bci='38'/>",
				"<klass id='749' name='java/lang/StringBuilder' flags='17'/>",
				"<method id='780' holder='749' name='&lt;init&gt;' return='680' flags='1' bytes='7' iicount='116'/>",
				"<call method='780' count='1' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<direct_call bci='38'/>",
				"<bc code='182' bci='43'/>",
				"<klass id='686' name='java/lang/String' flags='17'/>",
				"<method id='782' holder='749' name='append' return='749' arguments='686' flags='1' bytes='8' iicount='214'/>",
				"<call method='782' count='1' prof_factor='1' inline='1'/>",
				"<inline_fail reason='executed &lt; MinInliningThreshold times'/>",
				"<direct_call bci='43'/>",
				"<bc code='182' bci='47'/>",
				"<method id='783' holder='749' name='append' return='749' arguments='679' flags='1' bytes='8' iicount='8'/>",
				"<call method='783' count='1' prof_factor='1' inline='1'/>",
				"<inline_fail reason='executed &lt; MinInliningThreshold times'/>",
				"<direct_call bci='47'/>",
				"<uncommon_trap bci='47' reason='null_check' action='maybe_recompile'/>",
				"<bc code='182' bci='50'/>",
				"<method id='784' holder='749' name='toString' return='686' flags='1' bytes='17' iicount='115'/>",
				"<call method='784' count='1' prof_factor='1' inline='1'/>",
				"<inline_fail reason='executed &lt; MinInliningThreshold times'/>",
				"<direct_call bci='50'/>",
				"<uncommon_trap bci='50' reason='null_check' action='maybe_recompile'/>",
				"<bc code='182' bci='53'/>",
				"<klass id='779' name='java/io/PrintStream' flags='1'/>",
				"<method id='785' holder='779' name='println' return='680' arguments='686' flags='1' bytes='24' iicount='8'/>",
				"<dependency type='unique_concrete_method' ctxk='779' x='785'/>",
				"<call method='785' count='1' prof_factor='1' inline='1'/>",
				"<inline_fail reason='executed &lt; MinInliningThreshold times'/>",
				"<direct_call bci='53'/>",
				"<uncommon_trap bci='53' reason='null_check' action='maybe_recompile'/>",
				"<parse_done nodes='416' live='401' memory='96128' stamp='11.163'/>",
				"</parse>",
				"<late_inline method='780'>",
				"<jvms bci='38' method='777'/>",
				"</late_inline>",
				"<parse method='780' uses='1' stamp='11.163'>",
				"<bc code='183' bci='3'/>",
				"<klass id='747' name='java/lang/AbstractStringBuilder' flags='1024'/>",
				"<method id='802' holder='747' name='&lt;init&gt;' return='680' arguments='678' flags='0' bytes='12' iicount='130'/>",
				"<call method='802' count='-1' prof_factor='0.00862069' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='802' uses='-1' stamp='11.163'>",
				"<bc code='183' bci='1'/>",
				"<klass id='685' name='java/lang/Object' flags='1'/>",
				"<method id='810' holder='685' name='&lt;init&gt;' return='680' flags='1' bytes='1' compile_id='26' compiler='C2' iicount='19740'/>",
				"<call method='810' count='-1' prof_factor='1' inline='1'/>",
				"<inline_success reason='inline (hot)'/>",
				"<parse method='810' uses='-1' stamp='11.164'>",
				"<parse_done nodes='467' live='269' memory='103648' stamp='11.164'/>",
				"</parse>",
				"<parse_done nodes='518' live='319' memory='119680' stamp='11.164'/>",
				"</parse>",
				"<parse_done nodes='524' live='324' memory='121352' stamp='11.164'/>",
				"</parse>",
				"<phase_done name='parse' nodes='524' live='243' stamp='11.164'/>",
				"</phase>",
				"<phase name='optimizer' nodes='524' live='243' stamp='11.164'>",
				"<phase name='idealLoop' nodes='529' live='236' stamp='11.165'>",
				"<loop_tree>",
				"<loop idx='529' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='530' live='236' stamp='11.165'/>",
				"</phase>",
				"<phase name='escapeAnalysis' nodes='530' live='236' stamp='11.165'>",
				"<phase name='connectionGraph' nodes='531' live='237' stamp='11.165'>",
				"<method id='819' holder='747' name='expandCapacity' return='680' arguments='678' flags='0' bytes='50' iicount='162'/>",
				"<dependency type='unique_concrete_method' ctxk='747' x='819'/>",
				"<phase_done name='connectionGraph' nodes='531' live='237' stamp='11.167'/>",
				"</phase>",
				"<phase_done name='escapeAnalysis' nodes='531' live='237' stamp='11.167'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='531' live='237' stamp='11.167'>",
				"<loop_tree>",
				"<loop idx='529' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='534' live='219' stamp='11.168'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='534' live='219' stamp='11.168'>",
				"<loop_tree>",
				"<loop idx='529' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='534' live='219' stamp='11.169'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='534' live='219' stamp='11.169'>",
				"<loop_tree>",
				"<loop idx='529' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='534' live='219' stamp='11.169'/>",
				"</phase>",
				"<phase name='ccp' nodes='534' live='219' stamp='11.170'>",
				"<phase_done name='ccp' nodes='534' live='219' stamp='11.170'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='535' live='215' stamp='11.170'>",
				"<loop_tree>",
				"<loop idx='529' inner_loop='1' >",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='538' live='215' stamp='11.170'/>",
				"</phase>",
				"<phase_done name='optimizer' nodes='662' live='291' stamp='11.171'/>",
				"</phase>",
				"<phase name='matcher' nodes='662' live='291' stamp='11.171'>",
				"<phase_done name='matcher' nodes='255' live='255' stamp='11.174'/>",
				"</phase>",
				"<phase name='regalloc' nodes='317' live='317' stamp='11.175'>",
				"<regalloc attempts='1' success='1'/>",
				"<phase_done name='regalloc' nodes='393' live='376' stamp='11.193'/>",
				"</phase>",
				"<phase name='output' nodes='395' live='378' stamp='11.193'>",
				"<phase_done name='output' nodes='422' live='396' stamp='11.194'/>",
				"</phase>",
				"<dependency type='leaf_type' ctxk='776'/>",
				"<dependency type='unique_concrete_method' ctxk='779' x='785'/>",
				"<dependency type='unique_concrete_method' ctxk='747' x='819'/>",
				"<code_cache total_blobs='263' nmethods='73' adapters='142' free_code_cache='49731968'/>",
				"<task_done success='1' nmsize='800' count='10000' backedge_count='5317' inlined_bytes='79' stamp='11.194'/>",
				"</task>" };

		CompileNode root = buildCompileNodeForXML(lines);

		// root
		// -> chainA1() -> chainA2() -> chainA3() -> chainA4() -> bigMethod()
		// -> chainB1() -> chainB2() -> chainB3()
		// -> java.lang.AbstractStringBuilder() -> java.lang.Object()
		// -> append()
		// -> append()
		// -> toString()
		// -> println
		// -> java.lang.AbstractStringBuilder() -> java.lang.Object()

		List<CompileNode> rootChildren = root.getChildren();

		assertEquals(8, rootChildren.size());

		CompileNode c0 = rootChildren.get(0);
		CompileNode c1 = rootChildren.get(1);
		CompileNode c2 = rootChildren.get(2);
		CompileNode c3 = rootChildren.get(3);
		CompileNode c4 = rootChildren.get(4);
		CompileNode c5 = rootChildren.get(5);
		CompileNode c6 = rootChildren.get(6);
		CompileNode c7 = rootChildren.get(7);

		assertEquals("chainA1", c0.getMemberName());
		assertTrue(c0.isInlined());

		assertEquals("chainB1", c1.getMemberName());
		assertTrue(c1.isInlined());

		assertEquals("<init>", c2.getMemberName());
		assertTrue(c2.isInlined());

		assertEquals("append", c3.getMemberName());
		assertTrue(!c3.isInlined());

		assertEquals("append", c4.getMemberName());
		assertTrue(!c4.isInlined());

		assertEquals("toString", c5.getMemberName());
		assertTrue(!c5.isInlined());

		assertEquals("println", c6.getMemberName());
		assertTrue(!c6.isInlined());

		assertEquals("<init>", c7.getMemberName());
		assertTrue(c7.isInlined());

		List<CompileNode> c0Children = c0.getChildren();
		assertEquals(1, c0Children.size());
		CompileNode c0c0 = c0Children.get(0);
		assertEquals("chainA2", c0c0.getMemberName());
		assertTrue(c0c0.isInlined());

		List<CompileNode> c0c0Children = c0c0.getChildren();
		assertEquals(1, c0c0Children.size());
		CompileNode c0c0c0 = c0c0Children.get(0);
		assertEquals("chainA3", c0c0c0.getMemberName());
		assertTrue(c0c0c0.isInlined());

		List<CompileNode> c0c0c0Children = c0c0c0.getChildren();
		assertEquals(1, c0c0c0Children.size());
		CompileNode c0c0c0c0 = c0c0c0Children.get(0);
		assertEquals("chainA4", c0c0c0c0.getMemberName());
		assertTrue(c0c0c0c0.isInlined());

		List<CompileNode> c0c0c0c0Children = c0c0c0c0.getChildren();
		assertEquals(1, c0c0c0c0Children.size());
		CompileNode c0c0c0c0c0 = c0c0c0c0Children.get(0);
		assertEquals("bigMethod", c0c0c0c0c0.getMemberName());
		assertTrue(!c0c0c0c0c0.isInlined());
	}

	private CompileNode buildCompileNodeForXML(String[] lines)
	{
		TagProcessor tp = new TagProcessor();

		int count = 0;

		Tag tag = null;

		for (String line : lines)
		{
			line = line.trim();
			line = StringUtil.replaceXMLEntities(line);

			tag = tp.processLine(line);

			if (count++ < lines.length - 1)
			{
				assertNull(tag);
			}
		}

		assertNotNull(tag);

		Compilation compilation = new Compilation(null, 0);
		compilation.setTagTask((Task)tag);

		CompileChainWalker walker = new CompileChainWalker(new JITDataModel());

		CompileNode root = walker.buildCallTree(compilation);

		assertNotNull(root);

		return root;
	}

	@Test
	public void testJDK9XMLWithHIRPhase()
	{
		String[] lines = new String[] {
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

		CompileNode root = buildCompileNodeForXML(lines);

		// root
		// -> test()
		// -> chainC1() -> chainC2()
		// -> chainC3()
		// -> chainC2()
		// -> java.lang.AbstractStringBuilder() -> java.lang.Object()
		// -> append()
		// -> append()
		// -> toString()
		// -> println
		// -> java.lang.AbstractStringBuilder() -> java.lang.Object()

		List<CompileNode> rootChildren = root.getChildren();

		assertEquals(8, rootChildren.size());

		CompileNode c0 = rootChildren.get(0);
		CompileNode c1 = rootChildren.get(1);
		CompileNode c2 = rootChildren.get(2);
		CompileNode c3 = rootChildren.get(3);
		CompileNode c4 = rootChildren.get(4);
		CompileNode c5 = rootChildren.get(5);
		CompileNode c6 = rootChildren.get(6);
		CompileNode c7 = rootChildren.get(7);

		assertEquals("test", c0.getMemberName());
		assertTrue(c0.isInlined());

		assertEquals("chainC1", c1.getMemberName());
		assertTrue(c1.isInlined());

		assertEquals("chainC2", c2.getMemberName());
		assertTrue(c2.isInlined());

		assertEquals("<init>", c3.getMemberName());
		assertTrue(c3.isInlined());

		assertEquals("append", c4.getMemberName());
		assertTrue(c4.isInlined());

		assertEquals("append", c5.getMemberName());
		assertTrue(c5.isInlined());

		assertEquals("toString", c6.getMemberName());
		assertTrue(c6.isInlined());

		assertEquals("println", c7.getMemberName());
		assertTrue(c7.isInlined());

		List<CompileNode> c0Children = c0.getChildren();
		assertEquals(0, c0Children.size());

		List<CompileNode> c1Children = c1.getChildren();
		assertEquals(2, c1Children.size());

		CompileNode c1c0 = c1Children.get(0);
		assertEquals("chainC2", c1c0.getMemberName());
		assertTrue(c1c0.isInlined());

		CompileNode c1c1 = c1Children.get(1);
		assertEquals("chainC3", c1c1.getMemberName());
		assertTrue(c1c1.isInlined());

		assertEquals(0, c2.getChildren().size());

		assertEquals(1, c3.getChildren().size());

		CompileNode c3c0 = c3.getChildren().get(0);
		assertEquals("<init>", c3c0.getMemberName());
		assertTrue(c3c0.isInlined());

		assertEquals(1, c3c0.getChildren().size());

		CompileNode c3c0c0 = c3c0.getChildren().get(0);
		assertEquals("<init>", c3c0c0.getMemberName());
		assertTrue(c3c0c0.isInlined());

		assertEquals(1, c4.getChildren().size());

		CompileNode c4c0 = c4.getChildren().get(0);
		assertEquals("append", c4c0.getMemberName());
		assertTrue(!c4c0.isInlined());

		assertEquals(1, c5.getChildren().size());

		CompileNode c5c0 = c5.getChildren().get(0);
		assertEquals("append", c5c0.getMemberName());
		assertTrue(!c5c0.isInlined());

		assertEquals(1, c6.getChildren().size());

		CompileNode c6c0 = c6.getChildren().get(0);
		assertEquals("<init>", c6c0.getMemberName());
		assertTrue(!c6c0.isInlined());

		assertEquals(2, c7.getChildren().size());

		CompileNode c7c0 = c7.getChildren().get(0);
		assertEquals("print", c7c0.getMemberName());
		assertTrue(c7c0.isInlined());

		assertEquals(1, c7c0.getChildren().size());

		CompileNode c7c0c0 = c7c0.getChildren().get(0);
		assertEquals("write", c7c0c0.getMemberName());
		assertTrue(!c7c0c0.isInlined());

		CompileNode c7c1 = c7.getChildren().get(1);
		assertEquals("newLine", c7c1.getMemberName());
		assertTrue(!c7c1.isInlined());
	}

	@Test
	public void testJDK9CompileTaskWithNoParsePhase()
	{
		String[] lines = new String[] {
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

		CompileNode root = buildCompileNodeForXML(lines);
		
		// root
		// -> fill()
		// -> getBufIfOpen()
		
		List<CompileNode> rootChildren = root.getChildren();

		assertEquals(2, rootChildren.size());
		
		CompileNode c0 = rootChildren.get(0);
		CompileNode c1 = rootChildren.get(1);

		assertEquals("fill", c0.getMemberName());
		assertTrue(!c0.isInlined());

		assertEquals("getBufIfOpen", c1.getMemberName());
		assertTrue(c1.isInlined());
	}
}