/*
 * Copyright (c) 2016-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

import java.util.HashMap;
import java.util.Map;

import org.adoptopenjdk.jitwatch.model.assembly.AssemblyMethod;
import org.adoptopenjdk.jitwatch.util.ParseUtil;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILE_ID;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_ADDRESS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILER;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_NMSIZE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C2;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILE_KIND;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_LEVEL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C2N;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_FAILURE;

public class Compilation
{
	private Tag tagTaskQueued;

	private Tag tagNMethod;

	private Task tagTask;

	private Tag tagTaskDone;

	private AssemblyMethod assembly;

	private long compileTime;

	private String compileID;

	private long queuedStamp;

	private long compiledStamp;

	private boolean isC2N;

	private String nativeAddress;

	private int index;

	private IMetaMember member;

	private boolean failedTask = false;

	public Compilation(IMetaMember member, int index)
	{
		this.member = member;
		this.index = index;
	}

	public IMetaMember getMember()
	{
		return member;
	}

	public String getCompileID()
	{
		return compileID;
	}

	public String getNativeAddress()
	{
		return nativeAddress;
	}

	public AssemblyMethod getAssembly()
	{
		return assembly;
	}

	public void setAssembly(AssemblyMethod assembly)
	{
		this.assembly = assembly;
	}

	public Map<String, String> getQueuedAttributes()
	{
		Map<String, String> result = null;

		if (tagTaskQueued != null)
		{
			result = tagTaskQueued.getAttributes();
		}
		else
		{
			result = new HashMap<>();
		}

		return result;
	}

	public String getQueuedAttribute(String key)
	{
		return getQueuedAttributes().get(key);
	}

	public Map<String, String> getCompiledAttributes()
	{
		Map<String, String> result = null;

		if (tagNMethod != null)
		{
			result = tagNMethod.getAttributes();
		}
		else
		{
			result = new HashMap<>();
		}

		return result;
	}

	public String getCompiledAttribute(String key)
	{
		return getCompiledAttributes().get(key);
	}

	public void setTagTaskQueued(Tag tagTaskQueued)
	{
		this.tagTaskQueued = tagTaskQueued;

		this.compileID = tagTaskQueued.getAttributes().get(ATTR_COMPILE_ID);

		queuedStamp = ParseUtil.getStamp(tagTaskQueued.getAttributes());
	}

	public void setTagNMethod(Tag tagNMethod)
	{
		this.tagNMethod = tagNMethod;

		Map<String, String> attrs = tagNMethod.getAttributes();

		this.nativeAddress = attrs.get(ATTR_ADDRESS);

		String compileKind = attrs.get(ATTR_COMPILE_KIND);

		compiledStamp = ParseUtil.getStamp(attrs);

		if (C2N.equals(compileKind))
		{
			isC2N = true;
			this.compileID = tagNMethod.getAttributes().get(ATTR_COMPILE_ID);
		}
		else
		{
			compileTime = compiledStamp - queuedStamp;
		}
	}

	public void setTagTask(Task tagTask)
	{
		this.tagTask = tagTask;

		if (tagTask.getFirstNamedChild(TAG_FAILURE) != null)
		{
			failedTask = true;
		}
	}

	public Tag getTagTaskQueued()
	{
		return tagTaskQueued;
	}

	public Tag getTagNMethod()
	{
		return tagNMethod;
	}

	public Task getTagTask()
	{
		return tagTask;
	}

	public Tag getTagTaskDone()
	{
		return tagTaskDone;
	}

	public void setTagTaskDone(Tag tagTaskDone)
	{
		this.tagTaskDone = tagTaskDone;
	}

	public long getCompileTime()
	{
		return compileTime;
	}

	public int getIndex()
	{
		return index;
	}

	public int getNativeSize()
	{
		int result = 0;

		if (tagTaskDone != null)
		{
			result = Integer.parseInt(tagTaskDone.getAttributes().get(ATTR_NMSIZE));
		}

		return result;
	}

	public boolean isC2N()
	{
		return isC2N;
	}

	public long getQueuedStamp()
	{
		return queuedStamp;
	}

	public long getCompiledStamp()
	{
		return compiledStamp;
	}

	public String getSignature()
	{
		StringBuilder builder = new StringBuilder();

		builder.append("#").append(index + 1);

		if (tagNMethod != null)
		{
			Map<String, String> tagAttributes = tagNMethod.getAttributes();

			String level = tagAttributes.get(ATTR_LEVEL);
			String compiler = tagAttributes.get(ATTR_COMPILER);
			String compileKind = tagAttributes.get(ATTR_COMPILE_KIND);

			builder.append("  (");

			if (compiler != null)
			{
				builder.append(compiler);
			}

			if (compileKind != null)
			{
				if (compiler != null)
				{
					builder.append(" / ");
				}

				builder.append(compileKind.toUpperCase());
			}

			if (level != null)
			{
				builder.append(" / Level ").append(level);
			}

			builder.append(")");
		}

		return builder.toString();
	}

	public String getCompiler()
	{
		String result = null;

		if (tagNMethod != null)
		{
			StringBuilder builder = new StringBuilder();

			Map<String, String> tagAttributes = tagNMethod.getAttributes();

			String compiler = tagAttributes.get(ATTR_COMPILER);
			String compileKind = tagAttributes.get(ATTR_COMPILE_KIND);

			if (compiler != null)
			{
				builder.append(compiler);
			}

			if (compileKind != null)
			{
				if (compiler != null)
				{
					builder.append(" ");
				}

				builder.append(compileKind.toUpperCase());
			}

			result = builder.toString();
		}

		return result;
	}

	public int getLevel()
	{
		int result = -1;

		Tag tag;

		if (tagNMethod != null)
		{
			tag = tagNMethod;
		}
		else
		{
			tag = tagTaskQueued;
		}

		if (tag != null)
		{
			Map<String, String> tagAttributes = tag.getAttributes();

			String level = tagAttributes.get(ATTR_LEVEL);

			if (level != null)
			{
				try
				{
					result = Integer.parseInt(level);
				}
				catch (NumberFormatException nfe)
				{
				}
			}
			else if (C2.equals(tagAttributes.get(ATTR_COMPILER)))
			{
				result = 4;
			}
		}

		return result;
	}

	public String toStringVerbose()
	{
		StringBuilder builder = new StringBuilder();

		if (tagTaskQueued != null)
		{
			builder.append(tagTaskQueued).append("\n");
		}

		if (tagNMethod != null)
		{
			builder.append(tagNMethod).append("\n");
		}

		if (tagTask != null)
		{
			builder.append(tagTask).append("\n");
		}

		return builder.toString();
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		if (tagNMethod != null)
		{
			builder.append("Compilation for ").append(tagNMethod).append("\n");
		}

		return builder.toString();
	}

	public boolean isFailedTask()
	{
		return failedTask;
	}
}