/*
 * Copyright (c) 2013-2022 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.adoptopenjdk.jitwatch.logger.StdLogListener;
import org.adoptopenjdk.jitwatch.process.runtime.RuntimeJava;
import org.junit.Test;

public class TestExecutionUtil
{
	@Test public void testExecuteDemo()
	{
		List<String> cp = new ArrayList<>();

		String userDir = System.getProperty("user.dir");

		//path for maven build
		Path path = FileSystems.getDefault().getPath(userDir, "target", "test-classes");

		if (Files.exists(path))
		{
			cp.add(path.toString());
		}

		// path for gradle build
		path = FileSystems.getDefault().getPath(userDir, "build", "classes", "java", "test");

		if (Files.exists(path))
		{
			cp.add(path.toString());
		}

		List<String> options = new ArrayList<>();
		options.add("-XX:+UnlockDiagnosticVMOptions");
		options.add("-XX:+LogCompilation");

		try
		{
			RuntimeJava executor = new RuntimeJava(System.getProperty("java.home"));

			boolean success = executor.execute(DummyClassWithMain.class.getCanonicalName(), cp, options, Collections.emptyMap(),
					new StdLogListener());

			System.out.println(executor.getOutputStream());

			System.out.println(executor.getErrorStream());

			assertTrue(success);
		}
		catch (Exception e)
		{
			e.printStackTrace();

			fail();
		}
	}
}
