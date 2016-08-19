/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model.assembly;

import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.util.StringUtil;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

public class AssemblyMethod
{
	private String header;

	private List<AssemblyBlock> blocks = new ArrayList<>();
	
	private String nativeAddress;
	
	private String assemblyMethodSignature;
	
	public AssemblyMethod(String assemblyMethodSignature)
	{
		this.assemblyMethodSignature = assemblyMethodSignature;
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