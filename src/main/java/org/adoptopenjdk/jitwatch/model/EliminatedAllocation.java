package org.adoptopenjdk.jitwatch.model;

public class EliminatedAllocation
{
	private String type;
	private int bytecodeOffset;

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public int getBytecodeOffset()
	{
		return bytecodeOffset;
	}

	public void setBytecodeOffset(int bytecodeOffset)
	{
		this.bytecodeOffset = bytecodeOffset;
	}

	public EliminatedAllocation(String type, int bytecodeOffset)
	{
		this.type = type;
		this.bytecodeOffset = bytecodeOffset;
	}
}