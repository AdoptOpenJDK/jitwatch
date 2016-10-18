/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.report;

import org.adoptopenjdk.jitwatch.model.IMetaMember;

public class Report
{
	private final IMetaMember caller;
	private final int compilationIndex;
	private final int bci;
	private final String text;
	private final ReportType type;
	private final int score;
	private Object metaData;
	

	public Report(IMetaMember caller, int compilationIndex, int bci, String text, ReportType type, int score)
	{
		this(caller, compilationIndex, bci, text, type, score, null);
	}

	public Report(IMetaMember caller, int compilationIndex, int bci, String text, ReportType type, int score,
			Object metaData)
	{
		this.caller = caller;
		this.compilationIndex = compilationIndex;
		this.bci = bci;
		this.text = text;
		this.score = score;
		this.type = type;
		this.metaData = metaData;
	}

	public IMetaMember getCaller()
	{
		return caller;
	}

	public Object getMetaData()
	{
		return metaData;
	}

	public int getCompilationIndex()
	{
		return compilationIndex;
	}

	public int getBytecodeOffset()
	{
		return bci;
	}

	public String getText()
	{
		return text;
	}

	public ReportType getType()
	{
		return type;
	}

	public int getScore()
	{
		return score;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + bci;
		result = prime * result + ((caller == null) ? 0 : caller.hashCode());
		result = prime * result + compilationIndex;
		result = prime * result + ((metaData == null) ? 0 : metaData.hashCode());
		result = prime * result + score;
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		Report other = (Report) obj;
		if (bci != other.bci)
			return false;
		if (caller == null)
		{
			if (other.caller != null)
				return false;
		}
		else if (!caller.equals(other.caller))
			return false;
		if (compilationIndex != other.compilationIndex)
			return false;
		if (metaData == null)
		{
			if (other.metaData != null)
				return false;
		}
		else if (!metaData.equals(other.metaData))
			return false;
		if (score != other.score)
			return false;
		if (text == null)
		{
			if (other.text != null)
				return false;
		}
		else if (!text.equals(other.text))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "Report [caller=" + caller + ", compilationIndex=" + compilationIndex + ", bytecodeOffset=" + bci
				+ ", text=" + text + ", type=" + type + ", score=" + score + ", metaData=" + metaData + "]";
	}
}