package org.adoptopenjdk.jitwatch.model.assembly;

public enum Architecture
{
	X86_32, X86_64, ARM_32, ARM_64;
	
	public static Architecture parseFromLogLine(String line)
	{
		return X86_64;
	}
}
