/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.optimizedvcall;

import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.optimizedvcall.OptimizedVirtualCall;
import org.adoptopenjdk.jitwatch.optimizedvcall.VirtualCallSite;
import org.adoptopenjdk.jitwatch.util.StringUtil;

public class VCallRow
{
	private IMetaMember callerMember;
	private IMetaMember calleeMember;
	private BytecodeInstruction bytecodeInstruction;
	private VirtualCallSite callSite;

	public VCallRow(OptimizedVirtualCall optimizedVCall)
	{
		this.callerMember = optimizedVCall.getCallerMember();
		this.calleeMember = optimizedVCall.getCalleeMember();
		this.bytecodeInstruction = optimizedVCall.getBytecodeInstruction();
		this.callSite = optimizedVCall.getCallsite();
	}

	public String getInvokeType()
	{
		String invokeType = null;

		if (bytecodeInstruction == null)
		{
			invokeType = "Unknown";
		}
		else
		{
			invokeType = bytecodeInstruction.getOpcode().getMnemonic();
		}

		return invokeType;
	}

	public String getCallerClass()
	{
		return StringUtil.getAbbreviatedFQName(callerMember.getMetaClass().getFullyQualifiedName());
	}

	public String getCallerMember()
	{
		return callerMember.getMemberName();
	}

	public int getCallerBCI()
	{
		return callSite.getBytecodeOffset();
	}

	public int getCallerSourceLine()
	{
		return callSite.getSourceLine();
	}

	public String getCalleeClass()
	{
		return StringUtil.getAbbreviatedFQName(calleeMember.getMetaClass().getFullyQualifiedName());
	}

	public String getCalleeMember()
	{
		return calleeMember.getMemberName();
	}

	public IMetaMember getCaller()
	{
		return callerMember;
	}
}