/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.model.assembly;

import java.util.ArrayList;
import java.util.List;

public class AssemblyMethod
{
	private String header;

	private List<AssemblyBlock> blocks = new ArrayList<>();

	public AssemblyMethod()
	{
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

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		builder.append(header);

		for (AssemblyBlock block : blocks)
		{
			builder.append(block.toString());
		}

		return builder.toString();
	}

}