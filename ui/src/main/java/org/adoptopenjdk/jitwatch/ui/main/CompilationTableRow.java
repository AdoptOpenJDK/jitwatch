/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.main;

import org.adoptopenjdk.jitwatch.compilation.CompilationUtil;
import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.util.StringUtil;

public class CompilationTableRow
{
	private final Compilation compilation;

	public CompilationTableRow(Compilation compilation)
	{
		this.compilation = compilation;
	}

	public int getIndex()
	{
		return compilation.getIndex();
	}

	public String getQueuedStamp()
	{
		long stamp = compilation.getQueuedStamp();

		String result;

		if (stamp == 0)
		{
			result = "NA";
		}
		else
		{
			result = StringUtil.formatTimestamp(stamp, true);
		}

		return result;
	}

	public String getCompiledStamp()
	{
		long stamp = compilation.getCompiledStamp();

		String result;

		if (stamp == 0)
		{
			result = "NA";
		}
		else
		{
			result = StringUtil.formatTimestamp(stamp, true);
		}

		return result;
	}

	public String getNative()
	{
		int nativeSize = compilation.getNativeSize();

		String result;

		if (nativeSize == 0)
		{
			result = "NA";
		}
		else
		{
			result = Integer.toString(nativeSize);
		}

		return result;
	}

	public String getCompiler()
	{
		String result = compilation.getCompiler();

		if (result == null)
		{
			if (CompilationUtil.isStaleTask(compilation.getTagTask()))
			{
				result = "Stale task";
			}
			else
			{
				result = "NA";
			}
		}

		return result;
	}

	public String getLevel()
	{
		String result = null;
		
		int level = compilation.getLevel();

		if (level == -1)
		{
			result = "NA";
		}
		else
		{
			result = "Level " + level;
		}

		return result;
	}

	public String getLevelDescription(String level)
	{
		switch (level)
		{
		case "0":
			return "Interpreter";
		case "1":
			return "C1 full optimization (no profiling)";
		case "2":
			return "C1 invocation and backedge counters";
		case "3":
			return "C1 full profiling (invocation and backedge counters + MDO)";
		case "4":
			return "Server compiler";
		default:
			return "Unknown compiler level";
		}
	}
}