/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.sandbox.ClassExecutor;
import org.junit.Test;

public class TestExecutionUtil
{

	@Test
	public void testExecuteDemo()
	{
		List<String> cp = new ArrayList<>();

		cp.add("target/classes");

		File libDir = new File("lib");

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
			cp.add("lib/" + jar);
		}

		List<String> options = new ArrayList<>();

		ClassExecutor executor = new ClassExecutor();
		
		boolean success = executor.execute("org.adoptopenjdk.jitwatch.demo.MakeHotSpotLog", cp, options);

		System.out.println(executor.getErrorStream());
		//System.out.println(executor.getOutputStream());

		
		assertTrue(success);
	}
}