package com.chrisnewland.jitwatch.test;

import java.util.List;

import org.junit.Test;

import com.chrisnewland.jitwatch.core.TagProcessor;
import com.chrisnewland.jitwatch.model.Tag;

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

		assertEquals("632", tag.getAttrs().get("id"));

		assertEquals("java/lang/String", tag.getAttrs().get("name"));

		assertEquals("17", tag.getAttrs().get("flags"));
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

		assertEquals("1012", tag.getAttrs().get("idx"));

		assertEquals("1", tag.getAttrs().get("inner_loop"));
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
		assertEquals("1124", child01.getAttrs().get("idx"));

		Tag child02 = child0.getChildren().get(1);
		assertEquals("loop", child02.getName());
		assertEquals("1012", child02.getAttrs().get("idx"));
		assertEquals("1", child02.getAttrs().get("inner_loop"));

		Tag child1 = tag.getChildren().get(1);
		assertEquals("phase_done", child1.getName());
		assertEquals(2, child1.getAttrs().size());
		assertEquals(0, child1.getChildren().size());
		assertEquals("1144", child1.getAttrs().get("nodes"));
		assertEquals("14.151", child1.getAttrs().get("stamp"));
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
		assertEquals("java/util/Properties loadConvert ([CII[C)Ljava/lang/String;", tag.getAttrs().get("method"));
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
		assertEquals("2", firstChild.getAttrs().get("foo"));
		assertEquals("baz", firstChild.getAttrs().get("bar"));

		List<Tag> childrenWithAttr = tag.getNamedChildrenWithAttribute("b", "bar", "baz");
		assertEquals(1, childrenWithAttr.size());		
	}
}
