/*
 * Copyright (c) 2016-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_ADDRESS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_BYTES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILER;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILE_ID;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILE_KIND;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_ENTRY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_LEVEL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_NMSIZE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_OSR_BCI;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C2;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C2N;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.OSR;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_FAILURE;

import java.util.HashMap;
import java.util.Map;

import org.adoptopenjdk.jitwatch.model.assembly.AssemblyMethod;
import org.adoptopenjdk.jitwatch.util.ParseUtil;

public class Compilation
{
	private Tag tagTaskQueued;

	private Tag tagNMethod;

	private Task tagTask;

	private Tag tagTaskDone;

	private AssemblyMethod assembly;

	private String compileID;

	private long stampTaskQueued;

	private long stampTaskCompilationStart;

	private long stampNMethodEmitted;

	private boolean isC2N;

	private boolean isOSR;

	private int osrBCI;

	private String nativeAddress;

	private String entryAddress;

	private int index;

	private IMetaMember member;

	private boolean failedTask = false;

	private CompilerThread compilerThread = null;

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

	public String getEntryAddress()
	{
		return entryAddress;
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

		Map<String, String> attrs = tagTaskQueued.getAttributes();

		this.compileID = attrs.get(ATTR_COMPILE_ID);

		stampTaskQueued = ParseUtil.getStamp(attrs);

		String compileKind = attrs.get(ATTR_COMPILE_KIND);
		String osrBCIString = attrs.get(ATTR_OSR_BCI);

		if (OSR.equalsIgnoreCase(compileKind))
		{
			isOSR = true;
			osrBCI = -1;

			try
			{
				osrBCI = Integer.parseInt(osrBCIString);
			}
			catch (NumberFormatException nfe)
			{
				// logger.error("Could not parse {} '{}'", ATTR_OSR_BCI,
				// osrBCIString);
			}
		}
	}

	public void setTagNMethod(Tag tagNMethod)
	{
		this.tagNMethod = tagNMethod;

		Map<String, String> attrs = tagNMethod.getAttributes();

		this.nativeAddress = attrs.get(ATTR_ADDRESS);

		this.entryAddress = attrs.get(ATTR_ENTRY);

		String compileKind = attrs.get(ATTR_COMPILE_KIND);

		stampNMethodEmitted = ParseUtil.getStamp(attrs);

		if (C2N.equalsIgnoreCase(compileKind))
		{
			isC2N = true;
			this.compileID = tagNMethod.getAttributes().get(ATTR_COMPILE_ID);
		}
	}

	public void setTagTask(Task tagTask)
	{
		this.tagTask = tagTask;

		Map<String, String> attrs = tagTask.getAttributes();

		stampTaskCompilationStart = ParseUtil.getStamp(attrs);

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

	public int getBytecodeSize()
	{
		int result = 0;

		if (tagTask != null)
		{
			result = Integer.parseInt(tagTask.getAttributes().get(ATTR_BYTES));
		}

		return result;
	}

	public boolean isC2N()
	{
		return isC2N;
	}

	public long getStampTaskQueued()
	{
		return stampTaskQueued;
	}

	public long getStampTaskCompilationStart()
	{
		return stampTaskCompilationStart;
	}

	public long getStampNMethodEmitted()
	{
		return stampNMethodEmitted;
	}

	public long getCompilationDuration()
	{
		long duration = 0;

		if (stampTaskCompilationStart != 0 && stampNMethodEmitted != 0)
		{
			duration = stampNMethodEmitted - stampTaskCompilationStart;
		}

		return duration;
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
				builder.append(compiler.toUpperCase());
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
				builder.append(compiler.toUpperCase());
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
			else if (C2.equalsIgnoreCase(tagAttributes.get(ATTR_COMPILER)))
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

	public boolean isFailed()
	{
		return failedTask;
	}

	public boolean isOSR()
	{
		return isOSR;
	}

	public int getOSRBCI()
	{
		return osrBCI;
	}

	public CompilerThread getCompilerThread()
	{
		return compilerThread;
	}

	public void setCompilerThread(CompilerThread compilerThread)
	{
		this.compilerThread = compilerThread;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + index;
		result = prime * result + ((member == null) ? 0 : member.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Compilation other = (Compilation) obj;
		if (index != other.index)
			return false;
		if (member == null)
		{
			if (other.member != null)
				return false;
		}
		else if (!member.equals(other.member))
			return false;
		return true;
	}
}