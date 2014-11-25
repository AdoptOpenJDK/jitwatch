/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.optimizedvcall;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_NEWLINE;

import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;

public class OptimizedVirtualCall
{
	private IMetaMember callingMember;
	private VirtualCallSite caller;
	private VirtualCallSite callee;
	private BytecodeInstruction bytecodeInstruction;

	public OptimizedVirtualCall(IMetaMember callingMember, BytecodeInstruction bytecodeInstruction, VirtualCallSite caller, VirtualCallSite callee)
	{
		super();
		this.callingMember = callingMember;
		this.bytecodeInstruction = bytecodeInstruction;
		this.caller = caller;
		this.callee = callee;
	}

	public IMetaMember getCallingMember()
	{
		return callingMember;
	}

	public BytecodeInstruction getBytecodeInstruction()
	{
		return bytecodeInstruction;
	}

	public VirtualCallSite getCaller()
	{
		return caller;
	}

	public VirtualCallSite getCallee()
	{
		return callee;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		builder.append("Member: ").append(callingMember).append(C_NEWLINE);
		builder.append("Caller: ").append(caller).append(C_NEWLINE);
		builder.append("Callee: ").append(callee).append(C_NEWLINE);
		builder.append("Instr : ").append(bytecodeInstruction);

		return builder.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((callee == null) ? 0 : callee.hashCode());
		result = prime * result + ((caller == null) ? 0 : caller.hashCode());
		result = prime * result + ((callingMember == null) ? 0 : callingMember.hashCode());
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

		if (callee == null)
		{
			if (other.callee != null)
			{
				return false;
			}
		}
		else if (!callee.equals(other.callee))
		{
			return false;
		}

		if (caller == null)
		{
			if (other.caller != null)
			{
				return false;
			}
		}
		else if (!caller.equals(other.caller))
		{
			return false;
		}

		if (callingMember == null)
		{
			if (other.callingMember != null)
			{
				return false;
			}
		}

		else if (!callingMember.equals(other.callingMember))
		{
			return false;
		}

		return true;
	}
}