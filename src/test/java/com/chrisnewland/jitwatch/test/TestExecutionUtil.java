/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.chrisnewland.jitwatch.util.ExecutionUtil;

public class TestExecutionUtil
{

	@Test
	public void testExecuteDemo()
	{
		List<String> cp = new ArrayList<>();

		String workingDir = System.getProperty("user.dir");
		System.out.println(workingDir);

		cp.add("target/classes");
		cp.add("lib/logback-classic-1.0.1.jar");
		cp.add("lib/slf4j-api-1.7.7.jar");
		cp.add("lib/logback-core-1.0.1.jar");

		List<String> options = new ArrayList<>();
		// options.add("-XX:+UnlockDiagnosticVMOptions");
		// options.add("-XX:+TraceClassLoading");
		// options.add("-XX:+LogCompilation ");
		// options.add("-XX:+PrintAssembly");

		ExecutionUtil.execute("com.chrisnewland.jitwatch.demo.MakeHotSpotLog", cp, options);

	}
}
