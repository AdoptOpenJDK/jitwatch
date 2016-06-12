/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_RELEASE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TWEAK_VM;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_VM_VERSION;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.core.JITWatchConstants;
import org.adoptopenjdk.jitwatch.core.TagProcessor;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.junit.Test;

public class TestTagProcessor
{
	@Test
	public void testSingleLine()
	{
		TagProcessor tp = new TagProcessor();

		String line = "<klass id='632' name='java/lang/String' flags='17'/>";
		Tag tag = tp.processLine(line);

		assertEquals("klass", tag.getName());

		assertEquals(3, tag.getAttributes().size());

		assertEquals("632", tag.getAttributes().get("id"));

		assertEquals("java/lang/String", tag.getAttributes().get("name"));

		assertEquals("17", tag.getAttributes().get("flags"));

		assertTrue(!tp.wasFragmentSeen());
	}

	@Test
	public void testSingleTag2Lines()
	{
		String line1 = "<loop idx='1012' inner_loop='1' >";
		String line2 = "</loop>";

		TagProcessor tp = new TagProcessor();

		Tag tag = tp.processLine(line1);

		assertNull(tag);

		tag = tp.processLine(line2);

		assertEquals("loop", tag.getName());

		assertEquals(2, tag.getAttributes().size());

		assertEquals("1012", tag.getAttributes().get("idx"));

		assertEquals("1", tag.getAttributes().get("inner_loop"));
	}

	@Test
	public void testNestedTags()
	{
		String line1 = "<phase name='idealLoop' nodes='1119' stamp='14.151'>";
		String line2 = "<loop_tree>";
		String line3 = "<loop idx='1124' >";
		String line4 = "</loop>";
		String line5 = "<loop idx='1012' inner_loop='1' >";
		String line6 = "</loop>";
		String line7 = "</loop_tree>";
		String line8 = "<phase_done nodes='1144' stamp='14.151'/>";
		String line9 = "</phase>";

		String[] lines = new String[] { line1, line2, line3, line4, line5, line6, line7, line8, line9 };

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
		assertEquals("phase", tag.getName());
		assertEquals(2, tag.getChildren().size());

		Tag child0 = tag.getChildren().get(0);
		assertEquals("loop_tree", child0.getName());
		assertEquals(0, child0.getAttributes().size());
		assertEquals(2, child0.getChildren().size());

		Tag child01 = child0.getChildren().get(0);
		assertEquals("loop", child01.getName());
		assertEquals("1124", child01.getAttributes().get("idx"));

		Tag child02 = child0.getChildren().get(1);
		assertEquals("loop", child02.getName());
		assertEquals("1012", child02.getAttributes().get("idx"));
		assertEquals("1", child02.getAttributes().get("inner_loop"));

		Tag child1 = tag.getChildren().get(1);
		assertEquals("phase_done", child1.getName());
		assertEquals(2, child1.getAttributes().size());
		assertEquals(0, child1.getChildren().size());
		assertEquals("1144", child1.getAttributes().get("nodes"));
		assertEquals("14.151", child1.getAttributes().get("stamp"));
	}

	@Test
	public void testTask()
	{
		String line1 = "<task compile_id='21' method='java/util/Properties loadConvert ([CII[C)Ljava/lang/String;' bytes='505' count='10000' backedge_count='5668' iicount='108' stamp='6.801'>";
		String line2 = "<task_done success='1' nmsize='3160' count='10000' backedge_count='5723' stamp='6.744'/>";
		String line3 = "</task>";

		String[] lines = new String[] { line1, line2, line3 };

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

		assertEquals(7, tag.getAttributes().size());
		assertEquals("java/util/Properties loadConvert ([CII[C)Ljava/lang/String;", tag.getAttributes().get("method"));
	}

	@Test
	public void testGetChildren()
	{
		String line1 = "<a foo='1'>";
		String line2 = "<b foo='2' bar='baz'/>";
		String line3 = "<b foo='2'/>";
		String line4 = "<b foo='bar'/>";
		String line5 = "</a>";

		String[] lines = new String[] { line1, line2, line3, line4, line5 };

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

		List<Tag> children = tag.getNamedChildren("b");
		assertEquals(3, children.size());

		Tag firstChild = tag.getFirstNamedChild("b");
		assertEquals("2", firstChild.getAttributes().get("foo"));
		assertEquals("baz", firstChild.getAttributes().get("bar"));

		List<Tag> childrenWithAttr = tag.getNamedChildrenWithAttribute("b", "bar", "baz");
		assertEquals(1, childrenWithAttr.size());
	}

	@Test
	public void testTextNodes()
	{
		String line0 = "<vm_version>";
		String line1 = "<name>";
		String line2 = "Java HotSpot(TM) 64-Bit Server VM";
		String line3 = "</name>";
		String line4 = "<release>";
		String line5 = "25.0-b70";
		String line6 = "</release>";
		String line7 = "<info>";
		String line8 = "Java HotSpot(TM) 64-Bit Server VM (25.0-b70) for linux-amd64 JRE (1.8.0-b132), built on Mar  4 2014 03:07:25 by &quot;java_re&quot; with gcc 4.3.0 20080428 (RedHat 4.3.0-8)";
		String line9 = "</info>";
		String line10 = "</vm_version>";

		String[] lines = new String[] { line0, line1, line2, line3, line4, line5, line6, line7, line8, line9, line10 };

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

		assertEquals(TAG_VM_VERSION, tag.getName());

		List<Tag> children = tag.getChildren();

		assertEquals(3, children.size());

		Tag tagRelease = children.get(1);

		assertEquals(TAG_RELEASE, tagRelease.getName());
		assertEquals(line5, tagRelease.getTextContent());
	}

	@Test
	public void testTextNodesWithClosingTagOnSameLine()
	{
		String line0 = "<vm_version>";
		String line1 = "<name>";
		String line2 = "Java HotSpot(TM) 64-Bit Server VM</name>";
		String line4 = "<release>";
		String line5 = "25.0-b70";
		String line6 = "</release>";
		String line7 = "<info>";
		String line8 = "Java HotSpot(TM) 64-Bit Server VM (25.0-b70) for linux-amd64 JRE (1.8.0-b132), built on Mar  4 2014 03:07:25 by &quot;java_re&quot; with gcc 4.3.0 20080428 (RedHat 4.3.0-8)";
		String line9 = "</info>";
		String line10 = "</vm_version>";

		String[] lines = new String[] { line0, line1, line2, line4, line5, line6, line7, line8, line9, line10 };

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
		
		assertEquals(TAG_VM_VERSION, tag.getName());

		List<Tag> children = tag.getChildren();

		assertEquals(3, children.size());

		Tag tagRelease = children.get(1);

		assertEquals(TAG_RELEASE, tagRelease.getName());
		assertEquals(line5, tagRelease.getTextContent());
	}
	
	@Test
	public void testTweakSelfClosingTag()
	{
		String line0 = "<vm_version>";
		String line1 = "<TweakVM/>";
		String line2 = "<name>";
		String line3 = "Java HotSpot(TM) 64-Bit Server VM";
		String line4 = "</name>";
		String line5 = "<release>";
		String line6 = "25.0-b70";
		String line7 = "</release>";
		String line8 = "<info>";
		String line9 = "Java HotSpot(TM) 64-Bit Server VM (25.0-b70) for linux-amd64 JRE (1.8.0-b132), built on Mar  4 2014 03:07:25 by &quot;java_re&quot; with gcc 4.3.0 20080428 (RedHat 4.3.0-8)";
		String line10 = "</info>";
		String line11 = "</vm_version>";

		String[] lines = new String[] { line0, line1, line2, line3, line4, line5, line6, line7, line8, line9, line10, line11 };

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

		assertEquals(TAG_VM_VERSION, tag.getName());

		List<Tag> children = tag.getChildren();

		assertEquals(4, children.size());

		Tag tagTweakVM = children.get(0);

		assertEquals(TAG_TWEAK_VM, tagTweakVM.getName());
		assertEquals(null, tagTweakVM.getTextContent());

		Tag tagRelease = children.get(2);

		assertEquals(TAG_RELEASE, tagRelease.getName());
		assertEquals(line6, tagRelease.getTextContent());
	}

	/*
	 * Scenario: Parsing an undefined line Given an undefined line is available
	 * When the tag processor parses such a line Then no tag objects are
	 * returned
	 */
	@Test
	public void givenAnUndefinedLineIsAvailable_WhenTheTagProcessorProcessesIt_ThenNoTagsAreReturned()
	{
		// Given
		Tag expectedParseResult = null;
		String line = null;

		// When
		TagProcessor tagProcessor = new TagProcessor();
		Tag actualParseResult = tagProcessor.processLine(line);

		// Then
		assertThat("No tags should have been returned.", actualParseResult, is(equalTo(expectedParseResult)));
	}

	/*
	 * Scenario: Parsing a line containing partially completed tag Given a line
	 * containing a partially completed tag is available When the tag processor
	 * parses such a line Then no tag objects are returned
	 */
	@Test
	public void givenAThreeCharatersLineStartingWithOpenBracket_WhenTheTagProcessorActionsIt_ThenNoTagsAreReturned()
	{
		// Given
		Tag expectedParseResult = null;
		String lineWith3LettersStartingWithOpenAngleBracket = JITWatchConstants.C_OPEN_ANGLE + "12";

		// When
		TagProcessor tagProcessor = new TagProcessor();
		Tag actualParseResult = tagProcessor.processLine(lineWith3LettersStartingWithOpenAngleBracket);

		// Then
		assertThat("No tags should have been returned.", actualParseResult, is(equalTo(expectedParseResult)));
	}

	/*
	 * Scenario: Parsing a line containing different open and close tags Given a
	 * line containing an open tag of type 'task' And another closing tag of
	 * type 'tag' When the tag processor parses such a line Then no tags should
	 * be returned
	 */
	@Test
	public void givenALineWithTwoDifferentOpenCloseTags_WhenTheTagProcessorActionsIt_ThenNoTagsAreReturned()
	{
		// Given
		Tag expectedParseResult = null;
		String aLineWithOpeningTag = "<loop idx='1012' inner_loop='1' >";
		String aLineWithClosingTag = "</line>";

		// When
		TagProcessor tagProcessor = new TagProcessor();
		tagProcessor.processLine(aLineWithOpeningTag);
		Tag actualTag = tagProcessor.processLine(aLineWithClosingTag);

		// Then
		assertThat("No tags should have been returned.", actualTag, is(equalTo(expectedParseResult)));
	}

	/*
	 * Scenario: Parsing a line containing an opening tag without a closing
	 * angle bracket (invalid) Given a line containing an open tag And the
	 * closing angle bracket of the tag is missing When the tag processor parses
	 * such a line Then no tags should be returned
	 */
	@Test
	public void givenALineWithAnOpenTagWithNoCloseAngleBracket_WhenTheTagProcessorActionsIt_ThenNoTagsAreReturned()
	{
		// Given
		Tag expectedParseResult = null;
		String aLineWithOpeningTagWithoutClosingAngleBracket = "<loop";

		// When
		TagProcessor tagProcessor = new TagProcessor();
		Tag actualTag = tagProcessor.processLine(aLineWithOpeningTagWithoutClosingAngleBracket);

		// Then
		assertThat("No tags should have been returned.", actualTag, is(equalTo(expectedParseResult)));
	}

	@Test
	public void testRegressionFragmentTag()
	{
		List<String> lines = new ArrayList<>();

		lines.add("<fragment>");
		// lines.add("<![CDATA["); - stripped by log splitter
		lines.add("<z attr0='zzz'/>");
		lines.add("<a attr1='aaa'>");
		lines.add("<b attr2='bbb' attr3='ccc'/>");
		lines.add("<c attr4='ddd'/>");
		lines.add("<d attr5='eee'>");
		// lines.add("]]>"); - stripped by log splitter
		lines.add("</fragment>");

		TagProcessor tp = new TagProcessor();

		Tag tag = null;

		tag = tp.processLine(lines.get(0)); // <fragment>
		tag = tp.processLine(lines.get(1)); // <a attr1='aaa'>

		assertEquals("z", tag.getName());
		assertEquals(1, tag.getAttributes().size());
		assertEquals("zzz", tag.getAttributes().get("attr0"));
		assertEquals(0, tag.getChildren().size());

		tag = tp.processLine(lines.get(2)); // <a attr1='aaa'>
		assertNull(tag);

		tag = tp.processLine(lines.get(3)); // <b attr2='bbb' attr3='ccc'/>
		assertNull(tag);

		tag = tp.processLine(lines.get(4)); // <c attr4='ddd'/>
		assertNull(tag);

		tag = tp.processLine(lines.get(5)); // <d attr5='eee'>
		assertNull(tag);

		tag = tp.processLine(lines.get(6)); // </fragment>
		assertNotNull(tag);

		assertEquals("a", tag.getName());
		assertEquals("aaa", tag.getAttributes().get("attr1"));
		assertEquals(3, tag.getChildren().size());

		List<Tag> childrenB = tag.getNamedChildren("b");
		assertEquals(1, childrenB.size());

		Tag firstChildB = tag.getFirstNamedChild("b");
		assertEquals(2, firstChildB.getAttributes().size());
		assertEquals("bbb", firstChildB.getAttributes().get("attr2"));
		assertEquals("ccc", firstChildB.getAttributes().get("attr3"));

		Tag firstChildC = tag.getFirstNamedChild("c");
		assertEquals(1, firstChildC.getAttributes().size());
		assertEquals("ddd", firstChildC.getAttributes().get("attr4"));

		Tag firstChildD = tag.getFirstNamedChild("d");
		assertEquals(1, firstChildD.getAttributes().size());
		assertEquals("eee", firstChildD.getAttributes().get("attr5"));

		assertTrue(tp.wasFragmentSeen());
	}

	@Test
	public void testRegressionFragmentTagNotBroken()
	{
		List<String> lines = new ArrayList<>();

		lines.add("<fragment>");
		// lines.add("<![CDATA["); - stripped by log splitter
		lines.add("<z attr0='zzz'/>");
		// lines.add("]]>"); - stripped by log splitter
		lines.add("</fragment>");

		TagProcessor tp = new TagProcessor();

		Tag tag = null;

		tag = tp.processLine(lines.get(0)); // <fragment>
		tag = tp.processLine(lines.get(1)); // <z attr0='zzz'>

		assertEquals("z", tag.getName());
		assertEquals(1, tag.getAttributes().size());
		assertEquals("zzz", tag.getAttributes().get("attr0"));
		assertEquals(0, tag.getChildren().size());

		tag = tp.processLine(lines.get(2)); // </fragment>
		assertNull(tag);

		assertTrue(tp.wasFragmentSeen());
	}
}
