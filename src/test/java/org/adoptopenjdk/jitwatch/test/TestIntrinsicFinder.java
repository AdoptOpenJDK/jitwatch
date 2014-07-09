/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Map;

import org.adoptopenjdk.jitwatch.core.IntrinsicFinder;
import org.adoptopenjdk.jitwatch.core.TagProcessor;
import org.adoptopenjdk.jitwatch.model.Journal;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.junit.Test;

public class TestIntrinsicFinder
{
	@Test
	public void testRegressionIntrinsicFinderNeedParseDictionary()
	{
		String[] lines = new String[] {
				"<task compile_id='3557' method='org/eclipse/jdt/internal/compiler/util/SimpleLookupTable get (Ljava/lang/Object;)Ljava/lang/Object;' bytes='59' count='4064' backedge_count='5293' iicount='4722' stamp='597.034'>",
				"<phase name='parse' nodes='3' live='3' stamp='597.034'>",
				"<klass id='645' name='java/lang/Object' flags='1'/>",
				"<klass id='729' name='org/eclipse/jdt/internal/compiler/util/SimpleLookupTable' flags='17'/>",
				"<method id='730' holder='729' name='get' return='645' arguments='645' flags='1' bytes='59' iicount='4722'/>",
				"<parse method='730' uses='4722' stamp='597.035'>",
				"<bc code='190' bci='4'/>",
				"<uncommon_trap bci='4' reason='null_check' action='maybe_recompile'/>",
				"<bc code='182' bci='7'/>",
				"<klass id='646' name='java/lang/String' flags='17'/>",
				"<klass id='732' name='org/eclipse/jdt/internal/compiler/lookup/UnresolvedReferenceBinding' flags='1'/>",
				"<type id='634' name='int'/>",
				"<method id='734' holder='645' name='hashCode' return='634' flags='257' bytes='0' compile_id='379' compile_kind='c2n' iicount='14362'/>",
				"<call method='734' count='2932' prof_factor='1' virtual='1' inline='1' receiver='646' receiver_count='2318' receiver2='732' receiver2_count='297'/>",
				"<uncommon_trap bci='7' reason='null_check' action='maybe_recompile'/>",
				"<intrinsic id='_hashCode' virtual='1' nodes='67'/>",
				"<bc code='112' bci='14'/>",
				"<uncommon_trap bci='14' reason='div0_check' action='maybe_recompile'/>",
				"<uncommon_trap bci='45' reason='predicate' action='maybe_recompile'/>",
				"<uncommon_trap bci='45' reason='loop_limit_check' action='maybe_recompile'/>",
				"<bc code='50' bci='50'/>",
				"<parse_done nodes='388' live='373' memory='83160' stamp='597.035'/>",
				"</parse>",
				"<phase_done name='parse' nodes='390' live='281' stamp='597.035'/>",
				"</phase>",
				"<code_cache total_blobs='3752' nmethods='3051' adapters='652' free_code_cache='40367872' largest_free_block='40161088'/>",
				"<task_done success='1' nmsize='632' count='4078' backedge_count='5353' stamp='597.038'/>", "</task>" };

		TagProcessor tp = new TagProcessor();

		int count = 0;

		Tag tag = null;

		for (String line : lines)
		{
			tag = tp.processLine(line);
			if (count++ < lines.length - 1)
			{
				assertNull(tag);
			}
		}

		assertNotNull(tag);

		Journal journal = new Journal();
		journal.addEntry(tag);

		Map<String, String> intrinsics = IntrinsicFinder.findIntrinsics(journal);

		assertEquals(1, intrinsics.size());

		String expectedMethod = "java.lang.Object.hashCode";

		assertEquals(true, intrinsics.keySet().contains(expectedMethod));
		assertEquals("_hashCode", intrinsics.get(expectedMethod));
	}

	@Test
	public void testRegressionIntrinsicFinderCallTagChangesContext()
	{
		String[] lines = new String[] {
				"<task compile_id='1564' method='java/util/TimSort mergeLo (IIII)V' bytes='655' count='84' backedge_count='5110' iicount='88' stamp='22.534'>",
				"<phase name='parse' nodes='3' live='3' stamp='22.534'>",
				"<type id='636' name='void'/>",
				"<type id='634' name='int'/>",
				"<klass id='729' name='java/util/TimSort' flags='0'/>",
				"<method id='730' holder='729' name='mergeLo' return='636' arguments='634 634 634 634' flags='2' bytes='655' iicount='88'/>",
				"<klass id='741' name='java/lang/AssertionError' flags='1'/>",
				"<uncommon_trap method='730' bci='634' reason='unloaded' action='reinterpret' index='82' klass='741'/>",
				"<uncommon_trap method='730' bci='615' reason='unloaded' action='reinterpret' index='82' klass='741'/>",
				"<klass id='742' name='java/lang/IllegalArgumentException' flags='1'/>",
				"<uncommon_trap method='730' bci='594' reason='unloaded' action='reinterpret' index='84' klass='742'/>",
				"<uncommon_trap method='730' bci='553' reason='unloaded' action='reinterpret' index='82' klass='741'/>",
				"<uncommon_trap method='730' bci='285' reason='unloaded' action='reinterpret' index='82' klass='741'/>",
				"<uncommon_trap method='730' bci='165' reason='unloaded' action='reinterpret' index='82' klass='741'/>",
				"<uncommon_trap method='730' bci='22' reason='unloaded' action='reinterpret' index='82' klass='741'/>",
				"<parse method='730' uses='88' stamp='22.534'>",
				"<bc code='154' bci='3'/>",
				"<branch target_bci='30' taken='47' not_taken='0' cnt='47' prob='always'/>",
				"<bc code='183' bci='38'/>",
				"<klass id='734' name='[Ljava/lang/Object;' flags='1041'/>",
				"<method id='735' holder='729' name='ensureCapacity' return='734' arguments='634' flags='2' bytes='85' iicount='229'/>",
				"<call method='735' count='47' prof_factor='1' inline='1'/>",
				"<inline_fail reason='too big'/>",
				"<direct_call bci='38'/>",
				"<bc code='184' bci='50'/>",
				"<klass id='645' name='java/lang/Object' flags='1'/>",
				"<klass id='651' name='java/lang/System' flags='17'/>",
				"<method id='736' holder='651' name='arraycopy' return='636' arguments='645 634 645 634 634' flags='265' bytes='0' compile_id='25' compile_kind='c2n' iicount='10000'/>",
				"<call method='736' count='47' prof_factor='1' inline='1'/>",
				"<uncommon_trap bci='50' reason='null_check' action='maybe_recompile'/>",
				"<uncommon_trap bci='50' reason='null_check' action='maybe_recompile'/>",
				"<intrinsic id='_arraycopy' nodes='169'/>",
				"<bc code='50' bci='76'/>",
				"<uncommon_trap bci='76' reason='range_check' action='make_not_entrant' comment='range_check'/>",
				"<bc code='83' bci='77'/>",
				"<uncommon_trap bci='77' reason='range_check' action='make_not_entrant' comment='range_check'/>",
				"<uncommon_trap bci='77' reason='array_check' action='maybe_recompile' klass='734'/>",
				"<cast_up reason='monomorphic_array' from='734' to='(exact)'/>",
				"<bc code='154' bci='83'/>",
				"<branch target_bci='99' taken='47' not_taken='0' cnt='47' prob='always'/>",
				"<bc code='184' bci='95'/>",
				"<call method='736' count='0' prof_factor='1' inline='1'/>",
				"<intrinsic id='_arraycopy' nodes='149'/>",
				"<bc code='160' bci='101'/>",
				"<branch target_bci='131' taken='47' not_taken='0' cnt='47' prob='always'/>",
				"<bc code='184' bci='114'/>",
				"<call method='736' count='0' prof_factor='1' inline='1'/>",
				"<intrinsic id='_arraycopy' nodes='99'/>",
				"<bc code='50' bci='128'/>",
				"<uncommon_trap bci='128' reason='range_check' action='make_not_entrant' comment='range_check'/>",
				"<bc code='83' bci='129'/>",
				"<uncommon_trap bci='129' reason='range_check' action='make_not_entrant' comment='range_check'/>",
				"<uncommon_trap bci='129' reason='array_check' action='maybe_recompile' klass='734'/>",
				"<cast_up reason='monomorphic_array' from='734' to='(exact)'/>",
				"<uncommon_trap bci='149' reason='predicate' action='maybe_recompile'/>",
				"<uncommon_trap bci='149' reason='loop_limit_check' action='maybe_recompile'/>",
				"<bc code='154' bci='152'/>",
				"<branch target_bci='173' taken='7120' not_taken='0' cnt='7120' prob='always'/>",
				"<bc code='50' bci='179'/>",
				"<uncommon_trap bci='179' reason='range_check' action='make_not_entrant' comment='range_check'/>",
				"<bc code='50' bci='184'/>",
				"<uncommon_trap bci='184' reason='range_check' action='make_not_entrant' comment='range_check'/>",
				"<bc code='185' bci='185'/>",
				"<klass id='733' name='org/eclipse/dltk/internal/core/index2/SourceModulesRequest$1' flags='0'/>",
				"<klass id='737' name='java/util/Comparator' flags='1537'/>",
				"<method id='738' holder='737' name='compare' return='634' arguments='645 645' flags='1025' bytes='0' iicount='1'/>",
				"<call method='738' count='7120' prof_factor='1' virtual='1' inline='1' receiver='733' receiver_count='7120'/>",
				"<method id='746' holder='733' name='compare' return='634' arguments='645 645' flags='4161' bytes='13' compile_id='1562' compiler='C2' iicount='17524'/>",
				"<call method='746' count='7120' prof_factor='1' inline='1'/>",
				"<inline_fail reason='already compiled into a big method'/>",
				"<predicted_call bci='185' klass='733'/>",
				"<uncommon_trap bci='185' reason='null_check' action='maybe_recompile'/>",
				"<uncommon_trap bci='185' reason='class_check' action='maybe_recompile' comment='monomorphic vcall checkcast'/>",
				"<direct_call bci='185'/>",
				"<bc code='156' bci='190'/>",
				"<branch target_bci='226' taken='3554' not_taken='3565' cnt='7119' prob='0.499227'/>",
				"<bc code='50' bci='207'/>",
				"<uncommon_trap bci='207' reason='range_check' action='make_not_entrant' comment='range_check'/>",
				"<bc code='83' bci='208'/>",
				"<uncommon_trap bci='208' reason='range_check' action='make_not_entrant' comment='range_check'/>",
				"<uncommon_trap bci='208' reason='array_check' action='maybe_recompile' klass='734'/>",
				"<cast_up reason='monomorphic_array' from='734' to='(exact)'/>",
				"<bc code='154' bci='220'/>",
				"<branch target_bci='259' taken='3547' not_taken='18' cnt='3565' prob='0.994951'/>",
				"<bc code='50' bci='240'/>",
				"<uncommon_trap bci='240' reason='range_check' action='make_not_entrant' comment='range_check'/>",
				"<bc code='83' bci='241'/>",
				"<uncommon_trap bci='241' reason='range_check' action='make_not_entrant' comment='range_check'/>",
				"<uncommon_trap bci='241' reason='array_check' action='maybe_recompile' klass='734'/>",
				"<cast_up reason='monomorphic_array' from='734' to='(exact)'/>",
				"<bc code='160' bci='253'/>",
				"<branch target_bci='259' taken='3525' not_taken='29' cnt='3554' prob='0.99184'/>",
				"<bc code='161' bci='266'/>",
				"<branch target_bci='149' taken='7072' not_taken='0' cnt='7072' prob='always'/>",
				"<uncommon_trap bci='269' reason='predicate' action='maybe_recompile'/>",
				"<uncommon_trap bci='269' reason='loop_limit_check' action='maybe_recompile'/>",
				"<bc code='154' bci='272'/>",
				"<branch target_bci='293' taken='0' not_taken='0'/>",
				"<bc code='50' bci='297'/>",
				"<uncommon_trap bci='297' reason='range_check' action='make_not_entrant' comment='range_check'/>",
				"<bc code='184' bci='306'/>",
				"<method id='739' holder='729' name='gallopRight' return='634' arguments='645 734 634 634 634 737' flags='10' bytes='335' iicount='262'/>",
				"<call method='739' count='0' prof_factor='1' inline='1'/>",
				"<uncommon_trap method='739' bci='324' reason='unloaded' action='reinterpret' index='82' klass='741'/>",
				"<uncommon_trap method='739' bci='248' reason='unloaded' action='reinterpret' index='82' klass='741'/>",
				"<uncommon_trap method='739' bci='21' reason='unloaded' action='reinterpret' index='82' klass='741'/>",
				"<inline_fail reason='too big'/>",
				"<direct_call bci='306'/>",
				"<bc code='153' bci='313'/>",
				"<branch target_bci='356' taken='0' not_taken='0'/>",
				"<bc code='184' bci='326'/>",
				"<call method='736' count='0' prof_factor='1' inline='1'/>",
				"<intrinsic id='_arraycopy' nodes='147'/>",
				"<bc code='163' bci='350'/>",
				"<branch target_bci='356' taken='0' not_taken='0'/>",
				"<bc code='50' bci='370'/>",
				"<uncommon_trap bci='370' reason='range_check' action='make_not_entrant' comment='range_check'/>",
				"<bc code='83' bci='371'/>",
				"<uncommon_trap bci='371' reason='range_check' action='make_not_entrant' comment='range_check'/>",
				"<uncommon_trap bci='371' reason='array_check' action='maybe_recompile' klass='734'/>",
				"<cast_up reason='monomorphic_array' from='734' to='(exact)'/>",
				"<bc code='154' bci='377'/>",
				"<branch target_bci='383' taken='0' not_taken='0'/>",
				"<bc code='50' bci='387'/>",
				"<uncommon_trap bci='387' reason='range_check' action='make_not_entrant' comment='range_check'/>",
				"<bc code='184' bci='397'/>",
				"<method id='740' holder='729' name='gallopLeft' return='634' arguments='645 734 634 634 634 737' flags='10' bytes='335' iicount='255'/>",
				"<call method='740' count='0' prof_factor='1' inline='1'/>",
				"<uncommon_trap method='740' bci='324' reason='unloaded' action='reinterpret' index='82' klass='741'/>",
				"<uncommon_trap method='740' bci='248' reason='unloaded' action='reinterpret' index='82' klass='741'/>",
				"<uncommon_trap method='740' bci='21' reason='unloaded' action='reinterpret' index='82' klass='741'/>",
				"<uncommon_trap method='740' bci='248' reason='unloaded' action='reinterpret' index='82' klass='741'/>",
				"<uncommon_trap method='740' bci='324' reason='unloaded' action='reinterpret' index='82' klass='741'/>",
				"<inline_fail reason='too big'/>",
				"<direct_call bci='397'/>",
				"<bc code='153' bci='404'/>",
				"<branch target_bci='449' taken='0' not_taken='0'/>",
				"<bc code='184' bci='417'/>",
				"<call method='736' count='0' prof_factor='1' inline='1'/>",
				"<intrinsic id='_arraycopy' nodes='97'/>",
				"<bc code='154' bci='443'/>",
				"<branch target_bci='449' taken='0' not_taken='0'/>",
				"<bc code='50' bci='463'/>",
				"<uncommon_trap bci='463' reason='range_check' action='make_not_entrant' comment='range_check'/>",
				"<bc code='83' bci='464'/>",
				"<uncommon_trap bci='464' reason='range_check' action='make_not_entrant' comment='range_check'/>",
				"<uncommon_trap bci='464' reason='array_check' action='maybe_recompile' klass='734'/>",
				"<cast_up reason='monomorphic_array' from='734' to='(exact)'/>",
				"<bc code='160' bci='470'/>",
				"<branch target_bci='476' taken='0' not_taken='0'/>",
				"<bc code='162' bci='525'/>",
				"<branch target_bci='532' taken='47' not_taken='0' cnt='47' prob='always'/>",
				"<bc code='160' bci='539'/>",
				"<branch target_bci='590' taken='18' not_taken='29' cnt='47' prob='0.382979'/>",
				"<bc code='154' bci='545'/>",
				"<branch target_bci='561' taken='29' not_taken='0'/>",
				"<bc code='184' bci='571'/>",
				"<call method='736' count='29' prof_factor='1' inline='1'/>",
				"<intrinsic id='_arraycopy' nodes='83'/>",
				"<bc code='50' bci='585'/>",
				"<uncommon_trap bci='585' reason='range_check' action='make_not_entrant' comment='range_check'/>",
				"<bc code='83' bci='586'/>",
				"<uncommon_trap bci='586' reason='range_check' action='make_not_entrant' comment='range_check'/>",
				"<uncommon_trap bci='586' reason='array_check' action='maybe_recompile' klass='734'/>",
				"<cast_up reason='monomorphic_array' from='734' to='(exact)'/>",
				"<bc code='154' bci='591'/>",
				"<branch target_bci='604' taken='18' not_taken='0'/>",
				"<bc code='187' bci='594'/>",
				"<uncommon_trap bci='594' reason='unloaded' action='reinterpret' index='84'/>",
				"<bc code='154' bci='607'/>",
				"<branch target_bci='623' taken='18' not_taken='0'/>",
				"<bc code='154' bci='626'/>",
				"<branch target_bci='642' taken='18' not_taken='0'/>",
				"<bc code='184' bci='651'/>",
				"<call method='736' count='18' prof_factor='1' inline='1'/>",
				"<intrinsic id='_arraycopy' nodes='147'/>",
				"<bc code='161' bci='483'/>",
				"<branch target_bci='490' taken='0' not_taken='0'/>",
				"<bc code='161' bci='495'/>",
				"<branch target_bci='502' taken='0' not_taken='0'/>",
				"<bc code='154' bci='504'/>",
				"<branch target_bci='269' taken='0' not_taken='0'/>",
				"<bc code='156' bci='509'/>",
				"<branch target_bci='515' taken='0' not_taken='0'/>",
				"<parse_done nodes='1981' live='1944' memory='416408' stamp='22.536'/>",
				"</parse>",
				"<phase_done name='parse' nodes='1983' live='1267' stamp='22.536'/>",
				"</phase>",
				"<phase name='optimizer' nodes='1983' live='1267' stamp='22.536'>",
				"<phase name='idealLoop' nodes='2072' live='1224' stamp='22.537'>",
				"<loop_tree>",
				"<loop idx='2085' >",
				"<loop idx='2098' inner_loop='1' >",
				"</loop>",
				"<loop idx='2099' inner_loop='1' >",
				"</loop>",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='2148' live='1185' stamp='22.538'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='2148' live='1185' stamp='22.538'>",
				"<loop_tree>",
				"<loop idx='2085' >",
				"<loop idx='2098' inner_loop='1' >",
				"</loop>",
				"<loop idx='2099' inner_loop='1' >",
				"</loop>",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='2148' live='1185' stamp='22.538'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='2148' live='1185' stamp='22.538'>",
				"<loop_tree>",
				"<loop idx='2085' >",
				"<loop idx='2098' inner_loop='1' >",
				"</loop>",
				"<loop idx='2099' inner_loop='1' >",
				"</loop>",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='2148' live='1185' stamp='22.539'/>",
				"</phase>",
				"<phase name='ccp' nodes='2148' live='1185' stamp='22.539'>",
				"<phase_done name='ccp' nodes='2148' live='1185' stamp='22.539'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='2155' live='1184' stamp='22.539'>",
				"<loop_tree>",
				"<loop idx='2085' >",
				"<loop idx='2098' inner_loop='1' >",
				"</loop>",
				"<loop idx='2099' inner_loop='1' >",
				"</loop>",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='2167' live='1148' stamp='22.540'/>",
				"</phase>",
				"<phase name='idealLoop' nodes='2167' live='1148' stamp='22.540'>",
				"<loop_tree>",
				"<loop idx='2085' >",
				"<loop idx='2098' inner_loop='1' >",
				"</loop>",
				"<loop idx='2099' inner_loop='1' >",
				"</loop>",
				"</loop>",
				"</loop_tree>",
				"<phase_done name='idealLoop' nodes='2179' live='1148' stamp='22.541'/>",
				"</phase>",
				"<phase_done name='optimizer' nodes='2206' live='1155' stamp='22.541'/>",
				"</phase>",
				"<phase name='matcher' nodes='2206' live='1155' stamp='22.541'>",
				"<phase_done name='matcher' nodes='1096' live='1096' stamp='22.542'/>",
				"</phase>",
				"<phase name='regalloc' nodes='1481' live='1481' stamp='22.543'>",
				"<regalloc attempts='2' success='1'/>",
				"<phase_done name='regalloc' nodes='2635' live='2219' stamp='22.558'/>",
				"</phase>",
				"<phase name='output' nodes='2643' live='2227' stamp='22.558'>",
				"<phase_done name='output' nodes='2701' live='2260' stamp='22.559'/>",
				"</phase>",
				"<code_cache total_blobs='2037' nmethods='1386' adapters='603' free_code_cache='45737472' largest_free_block='45650432'/>",
				"<task_done success='1' nmsize='5136' count='5000' backedge_count='5045' stamp='22.559'/>", "</task>" };

		TagProcessor tp = new TagProcessor();

		int count = 0;

		Tag tag = null;

		for (String line : lines)
		{
			tag = tp.processLine(line);
			if (count++ < lines.length - 1)
			{
				assertNull(tag);
			}
		}

		assertNotNull(tag);

		Journal journal = new Journal();
		journal.addEntry(tag);

		Map<String, String> intrinsics = IntrinsicFinder.findIntrinsics(journal);

		assertEquals(1, intrinsics.size());

		String expectedMethod = "java.lang.System.arraycopy";

		assertEquals(true, intrinsics.keySet().contains(expectedMethod));
		assertEquals("_arraycopy", intrinsics.get(expectedMethod));
	}

}
