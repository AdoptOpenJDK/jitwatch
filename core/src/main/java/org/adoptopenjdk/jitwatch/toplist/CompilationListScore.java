/*
 * Copyright (c) 2019 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.toplist;

import org.adoptopenjdk.jitwatch.chain.CompileNode;

import java.util.List;

public class CompilationListScore implements ITopListScore
{
	private final List<CompileNode> compileNodes;
	private String reason;

	public CompilationListScore(String reason, List<CompileNode> compileNodes)
	{
		this.reason = reason;
		this.compileNodes = compileNodes;
	}

	@Override public String getKey()
	{
		return reason;
	}

	@Override public long getScore()
	{
		return compileNodes.size();
	}

	public List<CompileNode> getCompilations()
	{
		return compileNodes;
	}
}