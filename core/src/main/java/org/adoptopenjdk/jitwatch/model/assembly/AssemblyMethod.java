/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.assembly;

import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

public class AssemblyMethod
{
	private String header;

	private List<AssemblyBlock> blocks = new ArrayList<>();

	private String nativeAddress;

	private String entryAddress;

	private String assemblyMethodSignature;

	private Architecture architecture;

	private static final Logger logger = LoggerFactory.getLogger(AssemblyMethod.class);

	public AssemblyMethod(Architecture architecture)
	{
		this.architecture = architecture;
	}

	public Architecture getArchitecture()
	{
		return architecture;
	}

	public void setAssemblyMethodSignature(String sig)
	{
		this.assemblyMethodSignature = sig;

		if (DEBUG_LOGGING_ASSEMBLY)
		{
			logger.debug("Received signature: '{}'", sig);
		}
	}

	public String getAssemblyMethodSignature()
	{
		return assemblyMethodSignature;
	}

	public void setHeader(String header)
	{
		this.header = header;
	}

	public String getHeader()
	{
		return header;
	}

	public void addBlock(AssemblyBlock block)
	{
		blocks.add(block);
	}

	public List<AssemblyBlock> getBlocks()
	{
		return blocks;
	}

	public int getMaxAnnotationWidth()
	{
		int width = 0;

		for (AssemblyBlock block : blocks)
		{
			for (AssemblyInstruction instruction : block.getInstructions())
			{
				int annoWidth = instruction.getAnnotation().length();

				width = Math.max(width, annoWidth);
			}
		}

		return width;
	}

	public String getNativeAddress()
	{
		return nativeAddress;
	}

	public void setNativeAddress(String nativeAddress)
	{
		this.nativeAddress = nativeAddress;
	}

	public String getEntryAddress()
	{
		return entryAddress;
	}

	public void setEntryAddress(String entryAddress)
	{
		this.entryAddress = entryAddress;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		int maxAnnoWidth = getMaxAnnotationWidth();

		if (header != null)
		{
			String[] headerLines = header.split(S_NEWLINE);

			for (String headerLine : headerLines)
			{
				builder.append(StringUtil.repeat(C_SPACE, maxAnnoWidth));
				builder.append(headerLine).append(S_NEWLINE);
			}
		}

		for (AssemblyBlock block : blocks)
		{
			builder.append(block.toString(maxAnnoWidth));
		}

		return builder.toString();
	}
}