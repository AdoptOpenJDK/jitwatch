/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog;
import org.adoptopenjdk.jitwatch.logger.NullLogListener;
import org.adoptopenjdk.jitwatch.process.runtime.RuntimeJava;
import org.junit.Test;

public class TestExecutionUtil
{
	@Test
	public void testExecuteDemo()
	{
		List<String> cp = new ArrayList<>();
		
		String userDir = System.getProperty("user.dir");
		
		//path for maven build
		Path path = FileSystems.getDefault().getPath(userDir, "target", "classes");
		
		if (Files.exists(path)){
			cp.add(path.toString());
		}
		
		// path for gradle build
		path = FileSystems.getDefault().getPath(userDir, "build", "classes", "main");
		
		if (Files.exists(path)){
			cp.add(path.toString());
		}

		File libDir = Paths.get(userDir, "../lib").toFile();
		
		assertTrue(libDir.exists());
		assertTrue(libDir.isDirectory());

		String[] jarNames = libDir.list(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String name)
			{
				return name.endsWith(".jar");
			}
		});

		for (String jar : jarNames)
		{
			cp.add(libDir.getAbsolutePath() + File.separatorChar + jar);
		}

		List<String> options = new ArrayList<>();
		options.add("-XX:+UnlockDiagnosticVMOptions");
		options.add("-XX:+TraceClassLoading");
		options.add("-XX:+LogCompilation");

		try
		{
			RuntimeJava executor = new RuntimeJava(System.getProperty("java.home"));

			boolean success = executor.execute(MakeHotSpotLog.class.getCanonicalName(), cp, options, new NullLogListener());

			assertTrue(success);
		}
		catch (Exception e)
		{
			e.printStackTrace();

			fail();
		}
	}
}
