/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.model.bytecode;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

import com.chrisnewland.jitwatch.util.StringUtil;

public class Instruction
{
	private int offset;
	private String mnemonic;
	
	private boolean hasParameters;
	private boolean isParamConstant;
	private int[] parameters = null;
	
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

	public String getMnemonic()
	{
		return mnemonic;
	}

	public void setMnemonic(String mnemonic)
	{
		this.mnemonic = mnemonic;
	}

	public int[] getParameters()
	{
		if (parameters == null)
		{
			parameters = new int[0];
		}
		
		return parameters;
	}

	public void setParameters(int[] parameters)
	{
		this.parameters = parameters;
		hasParameters = true;
	}

	public boolean isParamConstant()
	{
		return isParamConstant;
	}
	
	public boolean hasParameters()
	{
		return hasParameters;
	}

	public void setParamConstant(boolean isParamConstant)
	{
		this.isParamConstant = isParamConstant;
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
	
	public String toString(int maxOffset)
	{
		StringBuilder builder = new StringBuilder();
		
		int offsetWidth = Integer.toString(maxOffset).length();
		
		builder.append(StringUtil.padLeft(offset, offsetWidth)).append(C_COLON).append(C_SPACE);
		builder.append(StringUtil.padRight(mnemonic, 16));
		
		if (hasParameters)
		{
			if (isParamConstant)
			{
				builder.append(C_HASH);
			}
			
			StringBuilder paramBuilder = new StringBuilder();
			
			for (int param : parameters)
			{
				paramBuilder.append(param).append(", ");
			}
			
			int paramLength = paramBuilder.length();
			
			if (paramLength > 0)
			{
				paramBuilder.delete(paramLength-2, paramLength);
			}

			builder.append(StringUtil.padRight(paramBuilder.toString(), 5));
		}
		
		if (hasComment)
		{
			builder.append(comment);
		}		
		
		return builder.toString();
	}
}
