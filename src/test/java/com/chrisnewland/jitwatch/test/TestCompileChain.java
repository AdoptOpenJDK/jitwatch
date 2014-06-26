package com.chrisnewland.jitwatch.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

import com.chrisnewland.jitwatch.chain.CompileChainWalker;
import com.chrisnewland.jitwatch.chain.CompileNode;
import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;
import com.chrisnewland.jitwatch.core.TagProcessor;
import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.JITDataModel;
import com.chrisnewland.jitwatch.model.Journal;
import com.chrisnewland.jitwatch.model.MetaClass;
import com.chrisnewland.jitwatch.model.Tag;
import com.chrisnewland.jitwatch.util.ClassUtil;

public class TestCompileChain
{
	@Test
	public void testJava7LateInlineRegression()
	{
		String[] lines = new String[] {
				"<task compile_id='70' compile_kind='osr' method='com/chrisnewland/jitwatch/demo/MakeHotSpotLog testCallChain (J)V' bytes='57' count='10000' backedge_count='5317' iicount='1' osr_bci='5' stamp='11.612'>",
				"<phase name='parse' nodes='3' live='3' stamp='11.612'>",
				"<type id='636' name='void'/>",
				"<type id='635' name='long'/>",
				"<klass id='729' name='com/chrisnewland/jitwatch/demo/MakeHotSpotLog' flags='1'/>",
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
				"</task>"		
		};

		JITDataModel testModel = new JITDataModel();

		String methodName = "testCallChain";
		String fqClassName = "com.chrisnewland.jitwatch.demo.MakeHotSpotLog";
		testModel.buildMetaClass(fqClassName, com.chrisnewland.jitwatch.demo.MakeHotSpotLog.class);

		try
		{
			String fqClassNameSB = "java.lang.AbstractStringBuilder";
			testModel.buildMetaClass(fqClassNameSB, ClassUtil.loadClassWithoutInitialising("java.lang.AbstractStringBuilder"));
		}
		catch (ClassNotFoundException cnfe)
		{
			fail();
		}

		MetaClass metaClass = testModel.getPackageManager().getMetaClass(fqClassName);
		IMetaMember testMember = metaClass.getMemberFromSignature(methodName, void.class, new Class<?>[] { long.class });

		CompileNode root = buildCompileNodeForXML(lines, testMember, testModel);
		
		// root
		// -> chainA1() -> chainA2() -> chainA3() -> chainA4() -> bigMethod()
		// -> chainB1() -> chainB2() -> chainB3()
		// -> append()
		// -> append()
		// -> toString()
		// -> println
		// -> java.lang.AbstractStringBuilder() -> java.lang.Object()

		List<CompileNode> rootChildren = root.getChildren();
		
		assertEquals(7, rootChildren.size());
		
		int pos = 0;
		
		assertEquals("chainA1", rootChildren.get(pos++).getMember().getMemberName());
		assertEquals("chainB1", rootChildren.get(pos++).getMember().getMemberName());
		assertEquals("append", rootChildren.get(pos++).getMember().getMemberName());
		assertEquals("append", rootChildren.get(pos++).getMember().getMemberName());
		assertEquals("toString", rootChildren.get(pos++).getMember().getMemberName());
		assertEquals("println", rootChildren.get(pos++).getMember().getMemberName());
		assertEquals("java.lang.AbstractStringBuilder", rootChildren.get(pos++).getMember().getMemberName());
	}
	
	
	@Test
	public void testJava8LateInlineRegression()
	{
		String[] lines = new String[] {
				"<task compile_id='68' compile_kind='osr' method='com/chrisnewland/jitwatch/demo/MakeHotSpotLog testCallChain (J)V' bytes='57' count='10000' backedge_count='5317' iicount='1' osr_bci='5' stamp='11.148'>",
				"<phase name='parse' nodes='3' live='3' stamp='11.149'>",
				"<type id='680' name='void'/>",
				"<type id='679' name='long'/>",
				"<klass id='776' name='com/chrisnewland/jitwatch/demo/MakeHotSpotLog' flags='1'/>",
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
				"<phase_done name='connectionGraph' nodes='531' live='237' stamp='11.167'/>", "</phase>",
				"<phase_done name='escapeAnalysis' nodes='531' live='237' stamp='11.167'/>", "</phase>",
				"<phase name='idealLoop' nodes='531' live='237' stamp='11.167'>", "<loop_tree>",
				"<loop idx='529' inner_loop='1' >", "</loop>", "</loop_tree>",
				"<phase_done name='idealLoop' nodes='534' live='219' stamp='11.168'/>", "</phase>",
				"<phase name='idealLoop' nodes='534' live='219' stamp='11.168'>", "<loop_tree>",
				"<loop idx='529' inner_loop='1' >", "</loop>", "</loop_tree>",
				"<phase_done name='idealLoop' nodes='534' live='219' stamp='11.169'/>", "</phase>",
				"<phase name='idealLoop' nodes='534' live='219' stamp='11.169'>", "<loop_tree>",
				"<loop idx='529' inner_loop='1' >", "</loop>", "</loop_tree>",
				"<phase_done name='idealLoop' nodes='534' live='219' stamp='11.169'/>", "</phase>",
				"<phase name='ccp' nodes='534' live='219' stamp='11.170'>",
				"<phase_done name='ccp' nodes='534' live='219' stamp='11.170'/>", "</phase>",
				"<phase name='idealLoop' nodes='535' live='215' stamp='11.170'>", "<loop_tree>",
				"<loop idx='529' inner_loop='1' >", "</loop>", "</loop_tree>",
				"<phase_done name='idealLoop' nodes='538' live='215' stamp='11.170'/>", "</phase>",
				"<phase_done name='optimizer' nodes='662' live='291' stamp='11.171'/>", "</phase>",
				"<phase name='matcher' nodes='662' live='291' stamp='11.171'>",
				"<phase_done name='matcher' nodes='255' live='255' stamp='11.174'/>", "</phase>",
				"<phase name='regalloc' nodes='317' live='317' stamp='11.175'>", "<regalloc attempts='1' success='1'/>",
				"<phase_done name='regalloc' nodes='393' live='376' stamp='11.193'/>", "</phase>",
				"<phase name='output' nodes='395' live='378' stamp='11.193'>",
				"<phase_done name='output' nodes='422' live='396' stamp='11.194'/>", "</phase>",
				"<dependency type='leaf_type' ctxk='776'/>", "<dependency type='unique_concrete_method' ctxk='779' x='785'/>",
				"<dependency type='unique_concrete_method' ctxk='747' x='819'/>",
				"<code_cache total_blobs='263' nmethods='73' adapters='142' free_code_cache='49731968'/>",
				"<task_done success='1' nmsize='800' count='10000' backedge_count='5317' inlined_bytes='79' stamp='11.194'/>",
				"</task>" };

		JITDataModel testModel = new JITDataModel();

		String methodName = "testCallChain";
		String fqClassName = "com.chrisnewland.jitwatch.demo.MakeHotSpotLog";
		testModel.buildMetaClass(fqClassName, com.chrisnewland.jitwatch.demo.MakeHotSpotLog.class);

		try
		{
			String fqClassNameSB = "java.lang.AbstractStringBuilder";
			testModel.buildMetaClass(fqClassNameSB, ClassUtil.loadClassWithoutInitialising("java.lang.AbstractStringBuilder"));
		}
		catch (ClassNotFoundException cnfe)
		{
			fail();
		}

		MetaClass metaClass = testModel.getPackageManager().getMetaClass(fqClassName);
		IMetaMember testMember = metaClass.getMemberFromSignature(methodName, void.class, new Class<?>[] { long.class });

		CompileNode root = buildCompileNodeForXML(lines, testMember, testModel);
		
		// root
		// -> chainA1() -> chainA2() -> chainA3() -> chainA4() -> bigMethod()
		// -> chainB1() -> chainB2() -> chainB3()
		// -> append()
		// -> append()
		// -> toString()
		// -> println
		// -> java.lang.AbstractStringBuilder() -> java.lang.Object()

		List<CompileNode> rootChildren = root.getChildren();
		
		assertEquals(7, rootChildren.size());
		
		int pos = 0;
		
		assertEquals("chainA1", rootChildren.get(pos++).getMember().getMemberName());
		assertEquals("chainB1", rootChildren.get(pos++).getMember().getMemberName());
		assertEquals("append", rootChildren.get(pos++).getMember().getMemberName());
		assertEquals("append", rootChildren.get(pos++).getMember().getMemberName());
		assertEquals("toString", rootChildren.get(pos++).getMember().getMemberName());
		assertEquals("println", rootChildren.get(pos++).getMember().getMemberName());
		assertEquals("java.lang.AbstractStringBuilder", rootChildren.get(pos++).getMember().getMemberName());
	}
	
	private CompileNode buildCompileNodeForXML(String[] lines, IMetaMember member, JITDataModel model)
	{
		TagProcessor tp = new TagProcessor();

		int count = 0;

		Tag tag = null;

		for (String line : lines)
		{		
			line = line.trim();
			line = line.replace(S_ENTITY_LT, S_OPEN_ANGLE);
			line = line.replace(S_ENTITY_GT, S_CLOSE_ANGLE);

			tag = tp.processLine(line);

			if (count++ < lines.length - 1)
			{
				assertNull(tag);
			}
		}

		assertNotNull(tag);
		
		member.setCompiledAttributes(new HashMap<String, String>());

		Journal journal = member.getJournal();
		journal.addEntry(tag);

		CompileChainWalker walker = new CompileChainWalker(model);

		CompileNode root = walker.buildCallTree(member);

		assertNotNull(root);
		
		return root;

	}
}