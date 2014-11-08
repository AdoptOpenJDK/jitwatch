package org.adoptopenjdk.jitwatch.ui.optimizedvcall;

import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.optimizedvcall.VirtualCallSite;

public class VCallRow
{
	private IMetaMember callingMember;
	private VirtualCallSite caller;
	private VirtualCallSite callee;

	public VCallRow(IMetaMember callingMember, VirtualCallSite caller, VirtualCallSite callee)
	{
		this.callingMember = callingMember;
		this.caller = caller;
		this.callee = callee;
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