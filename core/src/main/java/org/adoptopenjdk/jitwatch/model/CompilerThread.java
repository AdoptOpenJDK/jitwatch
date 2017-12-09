/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */

package org.adoptopenjdk.jitwatch.model;

import java.util.ArrayList;
import java.util.List;

public class CompilerThread
{
	private String threadId;

	private String threadName;

	private List<Compilation> compilations;

	public String getThreadId()
	{
		return threadId;
	}

	public CompilerThread(String threadId, String threadName)
	{
		this.threadId = threadId;
		this.threadName = threadName;

		compilations = new ArrayList<>();
	}

	public String getThreadName()
	{
		return threadName;
	}

	public void addCompilation(Compilation compilation)
	{
		compilations.add(compilation);
	}

	public List<Compilation> getCompilations()
	{
		return compilations;
	}

	public void clear()
	{
		compilations.clear();
	}
	
	public int getLargestNativeSize()
	{
		int result = 0;

		if (!compilations.isEmpty())
		{
			Compilation first = compilations.get(0);

			result = first.getNativeSize();
		}

		for (Compilation compilation : compilations)
		{
			if (compilation.getNativeSize() > result)
			{
				result = compilation.getNativeSize();
			}
		}

		return result;
	}
	
	public int getLargestBytecodeSize()
	{
		int result = 0;

		if (!compilations.isEmpty())
		{
			Compilation first = compilations.get(0);

			result = first.getBytecodeSize();
		}

		for (Compilation compilation : compilations)
		{
			if (compilation.getBytecodeSize() > result)
			{
				result = compilation.getBytecodeSize();
			}
		}

		return result;
	}

	public long getEarliestQueuedTime()
	{
		long result = 0;

		if (!compilations.isEmpty())
		{
			Compilation first = compilations.get(0);

			result = first.getStampTaskQueued();
		}

		for (Compilation compilation : compilations)
		{
			if (compilation.getStampTaskQueued() < result)
			{
				result = compilation.getStampTaskQueued();
			}
		}

		return result;
	}

	public long getLatestNMethodEmittedTime()
	{
		long result = 0;

		if (!compilations.isEmpty())
		{
			Compilation first = compilations.get(0);

			result = first.getStampNMethodEmitted();
		}

		for (Compilation compilation : compilations)
		{
			long emitted = compilation.getStampNMethodEmitted();
			
			if (emitted > result)
			{
				result = emitted;
			}
		}

		return result;
	}
}