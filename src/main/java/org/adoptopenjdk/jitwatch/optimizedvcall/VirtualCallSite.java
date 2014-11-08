package org.adoptopenjdk.jitwatch.optimizedvcall;

public class VirtualCallSite
{
	private String className;
	private String memberName;
	private int bytecodeOffset;
	private int sourceLine;

	public String getClassName()
	{
		return className;
	}

	public String getMemberName()
	{
		return memberName;
	}

	public int getBytecodeOffset()
	{
		return bytecodeOffset;
	}

	public int getSourceLine()
	{
		return sourceLine;
	}

	public VirtualCallSite(String className, String memberName, int bytecodeOffset, int sourceLine)
	{
		super();
		this.className = className;
		this.memberName = memberName;
		this.bytecodeOffset = bytecodeOffset;
		this.sourceLine = sourceLine;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + bytecodeOffset;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((memberName == null) ? 0 : memberName.hashCode());
		result = prime * result + sourceLine;
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
		VirtualCallSite other = (VirtualCallSite) obj;
		if (bytecodeOffset != other.bytecodeOffset)
		{
			return false;
		}
		if (className == null)
		{
			if (other.className != null)
			{
				return false;
			}
		}
		else if (!className.equals(other.className))
		{
			return false;
		}
		if (memberName == null)
		{
			if (other.memberName != null)
			{
				return false;
			}
		}
		else if (!memberName.equals(other.memberName))
		{
			return false;
		}
		if (sourceLine != other.sourceLine)
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("CallSite [className=");
		builder.append(className);
		builder.append(", memberName=");
		builder.append(memberName);
		builder.append(", bytecodeOffset=");
		builder.append(bytecodeOffset);
		builder.append(", sourceLine=");
		builder.append(sourceLine);
		builder.append("]");
		return builder.toString();
	}
}
