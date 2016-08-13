/*
 * Copyright (c) 2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

import org.adoptopenjdk.jitwatch.model.assembly.AssemblyMethod;

public class Compilation
{
	private AssemblyMethod assembly;
	
	private String queuedAttributes;
	
	private String compiledAttributes;

	public AssemblyMethod getAssembly()
	{
		return assembly;
	}

	public void setAssembly(AssemblyMethod assembly)
	{
		this.assembly = assembly;
	}

	public String getQueuedAttributes()
	{
		return queuedAttributes;
	}

	public void setQueuedAttributes(String queuedAttributes)
	{
		this.queuedAttributes = queuedAttributes;
	}

	public String getCompiledAttributes()
	{
		return compiledAttributes;
	}

	public void setCompiledAttributes(String compiledAttributes)
	{
		this.compiledAttributes = compiledAttributes;
	}
}