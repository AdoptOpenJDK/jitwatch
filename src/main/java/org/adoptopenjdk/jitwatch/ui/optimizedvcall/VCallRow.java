/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.optimizedvcall;

import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.optimizedvcall.OptimizedVirtualCall;
import org.adoptopenjdk.jitwatch.optimizedvcall.VirtualCallSite;

public class VCallRow
{
	private IMetaMember callingMember;
	private BytecodeInstruction bytecodeInstruction;
	private VirtualCallSite caller;
	private VirtualCallSite callee;

	public VCallRow(OptimizedVirtualCall vCall)
	{
		this.callingMember = vCall.getCallingMember();
		this.bytecodeInstruction = vCall.getBytecodeInstruction();
		this.caller = vCall.getCaller();
		this.callee = vCall.getCallee();
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
		return caller.getClassName();
	}

	public String getCallerMember()
	{
		return caller.getMemberName();
	}

	public int getCallerBCI()
	{
		return caller.getBytecodeOffset();
	}

	public int getCallerSourceLine()
	{
		return caller.getSourceLine();
	}

	public String getCalleeClass()
	{
		return callee.getClassName();
	}

	public String getCalleeMember()
	{
		return callee.getMemberName();
	}

	public int getCalleeBCI()
	{
		return callee.getBytecodeOffset();
	}

	public int getCalleeSourceLine()
	{
		return callee.getSourceLine();
	}

	public IMetaMember getCallingMember()
	{
		return callingMember;
	}
}