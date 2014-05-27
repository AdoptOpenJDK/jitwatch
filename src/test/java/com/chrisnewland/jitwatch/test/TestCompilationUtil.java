/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.test;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.chrisnewland.jitwatch.sandbox.ClassCompiler;
import com.chrisnewland.jitwatch.sandbox.Sandbox;
import com.chrisnewland.jitwatch.util.FileUtil;

public class TestCompilationUtil
{
	private static final File TEST_SOURCE_FILE = new File(Sandbox.SANDBOX_SOURCE_DIR.toFile(), "com" + File.separator + "chrisnewland"
			+ File.separator + "jitwatch" + File.separator + "compiletest" + File.separator + "CompileTest.java");

	private static final File TEST_CLASS_FILE = new File(Sandbox.SANDBOX_CLASS_DIR.toFile(), "com" + File.separator + "chrisnewland" + File.separator
			+ "jitwatch" + File.separator + "compiletest" + File.separator + "CompileTest.class");

	@Before
	public void setUp()
	{
		deleteFile(TEST_SOURCE_FILE);
		deleteFile(TEST_CLASS_FILE);
	}
	
	@After
	public void tearDown()
	{
		deleteFile(TEST_SOURCE_FILE);
		deleteFile(TEST_CLASS_FILE);
	}
	
	private void deleteFile(File file)
	{
		if (file.exists() && file.isFile())
		{
			file.delete();
		}
	}
	
	@Test
	public void testCompileSimple()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("package com.chrisnewland.jitwatch.compiletest;\n");
		builder.append("public class CompileTest\n");
		builder.append("{\n");
		builder.append("private int foo = 0;\n");
		builder.append("public void setFoo(int foo) {this.foo=foo;}\n");
		builder.append("public int getFoo() {return foo;}\n");
		builder.append("}");

		try
		{
			File f = FileUtil.writeSource(Sandbox.SANDBOX_SOURCE_DIR.toFile(), "com.chrisnewland.jitwatch.compiletest.CompileTest", builder.toString());

			assertTrue(TEST_SOURCE_FILE.exists());

			List<File> sources = new ArrayList<>();
			sources.add(f);

			ClassCompiler compiler = new ClassCompiler();
			
			boolean success = compiler.compile(sources, Sandbox.SANDBOX_CLASS_DIR.toFile());

			assertTrue(success);

			assertTrue(TEST_CLASS_FILE.exists());
		}
		catch (Exception e)
		{
			e.printStackTrace();

			fail();
		}
	}
}
