/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.test;

import com.chrisnewland.jitwatch.core.JITWatchConstants;
import com.chrisnewland.jitwatch.core.TagProcessor;
import com.chrisnewland.jitwatch.model.Tag;
import com.chrisnewland.jitwatch.util.StringUtil;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class TestTagProcessor
{
	@Test
	public void testSingleLine()
	{
		TagProcessor tp = new TagProcessor();

		String line = "<klass id='632' name='java/lang/String' flags='17'/>";
		Tag tag = tp.processLine(line);

		assertEquals("klass", tag.getName());

		assertEquals(3, tag.getAttrs().size());

		assertEquals("632", tag.getAttribute("id"));

		assertEquals("java/lang/String", tag.getAttribute("name"));

		assertEquals("17", tag.getAttribute("flags"));
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

		assertEquals(2, tag.getAttrs().size());

		assertEquals("1012", tag.getAttribute("idx"));

		assertEquals("1", tag.getAttribute("inner_loop"));
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
		assertEquals(0, child0.getAttrs().size());
		assertEquals(2, child0.getChildren().size());

		Tag child01 = child0.getChildren().get(0);
		assertEquals("loop", child01.getName());
		assertEquals("1124", child01.getAttribute("idx"));

		Tag child02 = child0.getChildren().get(1);
		assertEquals("loop", child02.getName());
		assertEquals("1012", child02.getAttribute("idx"));
		assertEquals("1", child02.getAttribute("inner_loop"));

		Tag child1 = tag.getChildren().get(1);
		assertEquals("phase_done", child1.getName());
		assertEquals(2, child1.getAttrs().size());
		assertEquals(0, child1.getChildren().size());
		assertEquals("1144", child1.getAttribute("nodes"));
		assertEquals("14.151", child1.getAttribute("stamp"));
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

		assertEquals(7, tag.getAttrs().size());
		assertEquals("java/util/Properties loadConvert ([CII[C)Ljava/lang/String;", tag.getAttribute("method"));
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
		assertEquals("2", firstChild.getAttribute("foo"));
		assertEquals("baz", firstChild.getAttribute("bar"));

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

    /*
        Scenario: Parsing an undefined line
            Given an undefined line is available
            When the tag processor parses such a line
            Then no tag objects are returned
     */
    @Test
    public void givenAnUndefinedLineIsAvailable_WhenTheTagProcessorProcessesIt_ThenNoTagsAreReturned() {
        // Given
        Tag expectedParseResult = null;
        String line = null;

        // When
        TagProcessor tagProcessor = new TagProcessor();
        Tag actualParseResult = tagProcessor.processLine(line);

        // Then
        assertThat("No tags should have been returned.",
                actualParseResult,
                is(equalTo(expectedParseResult)));
    }

    /*
        Scenario: Parsing a line containing partially completed tag
            Given a line containing a partially completed tag is available
            When the tag processor parses such a line
            Then no tag objects are returned
     */
    @Test
    public void givenAThreeCharatersLineStartingWithOpenBracket_WhenTheTagProcessorActionsIt_ThenNoTagsAreReturned() {
        // Given
        Tag expectedParseResult = null;
        String lineWith3LettersStartingWithOpenAngleBracket =
                JITWatchConstants.C_OPEN_ANGLE + "12";

        // When
        TagProcessor tagProcessor = new TagProcessor();
        Tag actualParseResult = tagProcessor.processLine(lineWith3LettersStartingWithOpenAngleBracket);

        // Then
        assertThat("No tags should have been returned.",
                actualParseResult,
                is(equalTo(expectedParseResult)));
    }


    /*
        Scenario: Parsing a line containing different open and close tags
            Given a line containing an open tag of type 'task'
            And another closing tag of type 'tag'
            When the tag processor parses such a line
            Then no tags should be returned
    */
    @Test
    public void givenALineWithTwoDifferentOpenCloseTags_WhenTheTagProcessorActionsIt_ThenNoTagsAreReturned() {
        // Given
        Tag expectedParseResult = null;
        String aLineWithOpeningTag = "<loop idx='1012' inner_loop='1' >";
        String aLineWithClosingTag = "</line>";

        // When
        TagProcessor tagProcessor = new TagProcessor();
        tagProcessor.processLine(aLineWithOpeningTag);
        Tag actualTag = tagProcessor.processLine(aLineWithClosingTag);

        // Then
        assertThat("No tags should have been returned.",
                actualTag,
                is(equalTo(expectedParseResult)));
    }


    /*
        Scenario: Parsing a line containing an opening tag without a closing angle bracket (invalid)
            Given a line containing an open tag
            And the closing angle bracket of the tag is missing
            When the tag processor parses such a line
            Then no tags should be returned
    */
    @Test
    public void givenALineWithAnOpenTagWithNoCloseAngleBracket_WhenTheTagProcessorActionsIt_ThenNoTagsAreReturned() {
        // Given
        Tag expectedParseResult = null;
        String aLineWithOpeningTagWithoutClosingAngleBracket = "<loop";

        // When
        TagProcessor tagProcessor = new TagProcessor();
        Tag actualTag = tagProcessor.processLine(aLineWithOpeningTagWithoutClosingAngleBracket);

        // Then
        assertThat("No tags should have been returned.",
                actualTag,
                is(equalTo(expectedParseResult)));
    }

    /*
        Scenario: Parsing a line containing a tag of type 'Task'
            Given a line containing a tag of type 'Task' is available
            When the tag processor parses such a line
            Then a task object is returned
     */
    @Test
    @Ignore
    public void givenLineContainingATypeTask_WhenTheTagProcessorParsesIt_ThenATaskTagIsReturned() {
        // Given
//        String withTypeTask = "<task compile_id='1' compile_kind='osr' method='com/chrisnewland/jitwatch/demo/MakeHotSpotLog " +
//                "addVariable (I)V' bytes='41' count='10000' backedge_count='5438' iicount='1' osr_bci='5' stamp='0.164'>";
        String withTypeTask = "</task>";
        Map<String, String> attrs = StringUtil.getLineAttributes(withTypeTask);
        boolean selfClosing = (withTypeTask.charAt(withTypeTask.length() - 2) == JITWatchConstants.C_SLASH);
        int indexEndName = withTypeTask.indexOf(C_CLOSE_ANGLE);
        String name = withTypeTask.substring(1, indexEndName);
        Tag expectedParseResult = new Tag(name, attrs, selfClosing);

        // When
        TagProcessor tagProcessor = new TagProcessor();
        Tag actualParseResult = tagProcessor.processLine(withTypeTask);

        // Then
        assertThat("The line should have been parsed correctly, producing a tag..",
                actualParseResult,
                is(equalTo(expectedParseResult)));
    }
}