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
import static com.chrisnewland.jitwatch.util.CompilationUtil.COMPILE_DIR;

public class TestCompilationUtil
{

	@Test
	public void testCompileSimple()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("package com.chrisnewland.compiletest;\n");
		builder.append("public class CompileTest\n");
		builder.append("{\n");
		builder.append("private int foo = 0;\n");
		builder.append("public void setFoo(int foo) {this.foo=foo;}\n");
		builder.append("public int getFoo() {return foo;}\n");
		builder.append("}");

		try
		{
			File f = CompilationUtil.writeToFile(new File(COMPILE_DIR.toFile(), "CompileTest.java"), builder.toString());

			List<File> sources = new ArrayList<>();
			sources.add(f);

			boolean success = CompilationUtil.compile(sources);

			assertTrue(success);
			
			File expectedFile = new File(COMPILE_DIR.toFile(), "com" + File.separator + "chrisnewland" + File.separator + "compiletest"+ File.separator +"CompileTest.class");
			
			assertTrue(expectedFile.exists());
		}
		catch (Exception e)
		{
			e.printStackTrace();

			fail();
		}
	}
}
