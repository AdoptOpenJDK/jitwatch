/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.optimizedvcall;

import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;

public class OptimizedVirtualCall
{
	private IMetaMember callerMember;
	private IMetaMember calleeMember;
	private VirtualCallSite callsite;
	private BytecodeInstruction bytecodeInstruction;

	public OptimizedVirtualCall(IMetaMember callerMember, IMetaMember calleeMember, VirtualCallSite callsite,
			BytecodeInstruction bytecodeInstruction)
	{
		super();
		this.callerMember = callerMember;
		this.calleeMember = calleeMember;
		this.callsite = callsite;
		this.bytecodeInstruction = bytecodeInstruction;
	}

	public IMetaMember getCallerMember()
	{
		return callerMember;
	}

	public IMetaMember getCalleeMember()
	{
		return calleeMember;
	}

	public VirtualCallSite getCallsite()
	{
		return callsite;
	}

	public BytecodeInstruction getBytecodeInstruction()
	{
		return bytecodeInstruction;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bytecodeInstruction == null) ? 0 : bytecodeInstruction.hashCode());
		result = prime * result + ((calleeMember == null) ? 0 : calleeMember.hashCode());
		result = prime * result + ((callerMember == null) ? 0 : callerMember.hashCode());
		result = prime * result + ((callsite == null) ? 0 : callsite.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (obj == null)
		{
			return false;
		}

		if (getClass() != obj.getClass())
		{
			return false;
		}

		OptimizedVirtualCall other = (OptimizedVirtualCall) obj;

		if (bytecodeInstruction == null)
		{
			if (other.bytecodeInstruction != null)
			{
				return false;
			}
		}
		else if (!bytecodeInstruction.equals(other.bytecodeInstruction))
		{
			return false;
		}

		if (calleeMember == null)
		{
			if (other.calleeMember != null)
			{
				return false;
			}
		}
		else if (!calleeMember.equals(other.calleeMember))
		{
			return false;
		}

		if (callerMember == null)
		{
			if (other.callerMember != null)
			{
				return false;
			}
		}
		else if (!callerMember.equals(other.callerMember))
		{
			return false;
		}

		if (callsite == null)
		{
			if (other.callsite != null)
			{
				return false;
			}
		}
		else if (!callsite.equals(other.callsite))
		{
			return false;
		}

		return true;
	}

	@Override
	public String toString()
	{
		return "OptimizedVirtualCall [callerMember=" + callerMember + ", calleeMember=" + calleeMember + ", callsite=" + callsite
				+ ", bytecodeInstruction=" + bytecodeInstruction + "]";
	}

}