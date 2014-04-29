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

import org.junit.Test;

import com.chrisnewland.jitwatch.util.CompilationUtil;
import static com.chrisnewland.jitwatch.util.CompilationUtil.*;

public class TestCompilationUtil
{

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
			File f = CompilationUtil.writeSource("com.chrisnewland.jitwatch.compiletest.CompileTest", builder.toString());

			File expectedSourceFile = new File(SANDBOX_SOURCE_DIR.toFile(), "com" + File.separator + "chrisnewland"
					+ File.separator + "jitwatch" + File.separator + "compiletest" + File.separator + "CompileTest.java");

			assertTrue(expectedSourceFile.exists());

			List<File> sources = new ArrayList<>();
			sources.add(f);

			boolean success = CompilationUtil.compile(sources);

			assertTrue(success);

			File expectedClassFile = new File(SANDBOX_CLASS_DIR.toFile(), "com" + File.separator + "chrisnewland" + File.separator
					+ "jitwatch" + File.separator + "compiletest" + File.separator + "CompileTest.class");

			assertTrue(expectedClassFile.exists());
		}
		catch (Exception e)
		{
			e.printStackTrace();

			fail();
		}
	}
}
