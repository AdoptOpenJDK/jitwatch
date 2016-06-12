/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.logger.NullLogListener;
import org.adoptopenjdk.jitwatch.process.compiler.CompilerJava;
import org.adoptopenjdk.jitwatch.sandbox.Sandbox;
import org.adoptopenjdk.jitwatch.util.FileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestCompilationUtil
{
	private static final File TEST_SOURCE_FILE = new File(Sandbox.SANDBOX_SOURCE_DIR.toFile(), "org" + File.separator + "adoptopenjdk"
			+ File.separator + "jitwatch" + File.separator + "compiletest" + File.separator + "CompileTest.java");

	private static final File TEST_CLASS_FILE = new File(Sandbox.SANDBOX_CLASS_DIR.toFile(), "org" + File.separator + "adoptopenjdk" + File.separator
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
		builder.append("package org.adoptopenjdk.jitwatch.compiletest;\n");
		builder.append("public class CompileTest\n");
		builder.append("{\n");
		builder.append("private int foo = 0;\n");
		builder.append("public void setFoo(int foo) {this.foo=foo;}\n");
		builder.append("public int getFoo() {return foo;}\n");
		builder.append("}");

		try
		{
			File f = FileUtil.writeSource(Sandbox.SANDBOX_SOURCE_DIR.toFile(), "org.adoptopenjdk.jitwatch.compiletest.CompileTest", builder.toString());

			assertTrue(TEST_SOURCE_FILE.exists());

			List<File> sources = new ArrayList<>();
			sources.add(f);

			CompilerJava compiler = new CompilerJava(System.getProperty("java.home"));

			List<String> compileClasspath = new ArrayList<>();

			boolean success = compiler.compile(sources, compileClasspath, Sandbox.SANDBOX_CLASS_DIR.toFile(),  new NullLogListener());

			if (!success)
			{
				System.err.println(compiler.getErrorStream());
				fail();
			}

			assertTrue(TEST_CLASS_FILE.exists());
		}
		catch (Exception e)
		{
			e.printStackTrace();

			fail();
		}
	}
}