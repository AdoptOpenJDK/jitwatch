/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.model.bytecode;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

import java.util.ArrayList;
import java.util.List;

import com.chrisnewland.jitwatch.util.StringUtil;

public class Instruction
{
	private int offset;
	private Opcode opcode;

	private List<IBytecodeParam> parameters = new ArrayList<>();

	private boolean hasComment;
	private String comment;

	public int getOffset()
	{
		return offset;
	}

	public void setOffset(int offset)
	{
		this.offset = offset;
	}

	public Opcode getOpcode()
	{
		return opcode;
	}

	public void setOpcode(Opcode opcode)
	{
		this.opcode = opcode;
	}

	public List<IBytecodeParam> getParameters()
	{
		return parameters;
	}

	public void addParameter(IBytecodeParam parameter)
	{
		this.parameters.add(parameter);
	}

	public boolean hasParameters()
	{
		return parameters.size() > 0;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
		hasComment = true;
	}

	public boolean hasComment()
	{
		return hasComment;
	}
	
	@Override
	public String toString()
	{
		return toString(0);
	}

	public String toString(int maxOffset)
	{
		StringBuilder builder = new StringBuilder();

		int offsetWidth = Integer.toString(maxOffset).length();

		builder.append(StringUtil.padLeft(offset, offsetWidth)).append(C_COLON).append(C_SPACE);
		builder.append(StringUtil.padRight(opcode.getMnemonic(), 16));

		if (hasParameters())
		{
			StringBuilder paramBuilder = new StringBuilder();

			for (IBytecodeParam parameter : parameters)
			{
				paramBuilder.append(parameter.toString()).append(", ");

			}

			int paramLength = paramBuilder.length();
			
			paramBuilder.delete(paramLength - 2, paramLength);

			builder.append(StringUtil.padRight(paramBuilder.toString(), 5));
		}

		if (hasComment)
		{
			builder.append(comment);
		}

		return builder.toString();
	}
}
