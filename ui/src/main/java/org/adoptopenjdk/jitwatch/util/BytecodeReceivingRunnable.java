package org.adoptopenjdk.jitwatch.util;

import org.adoptopenjdk.jitwatch.model.bytecode.ClassBC;

public abstract class BytecodeReceivingRunnable implements Runnable
{
	private ClassBC classBC;
	
	public void setClassBC(ClassBC classBC)
	{
		this.classBC = classBC;
	}
	
	public ClassBC getClassBC()
	{
		return classBC;
	}
}