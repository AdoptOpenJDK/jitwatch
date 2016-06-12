/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

public class JITStats
{
	// method modifiers
	private long countPrivate = 0;
	private long countProtected = 0;
	private long countPublic = 0;
	private long countStatic = 0;
	private long countFinal = 0;
	private long countSynchronized = 0;
	private long countStrictfp = 0;
	private long countNative = 0;
	private long countAbstract = 0;

	// compilation stats
	private long countOSR = 0;
	private long countC1 = 0;
	private long countC2 = 0;
	private long countC2N = 0;
	private long totalCompileTime = 0;
	private long nativeBytes = 0;
	private long countCompilerThreads = 0;

	private long countClass = 0;
	private long countMethod = 0;
	private long countConstructor = 0;

	public void reset()
	{
		countPrivate = 0;
		countProtected = 0;
		countPublic = 0;
		countStatic = 0;
		countFinal = 0;
		countSynchronized = 0;
		countStrictfp = 0;
		countNative = 0;
		countAbstract = 0;

		countOSR = 0;
		countC1 = 0;
		countC2 = 0;
		countC2N = 0;
		totalCompileTime = 0;
		nativeBytes = 0;
		countCompilerThreads = 0;

		countClass = 0;
		countMethod = 0;
		countConstructor = 0;
	}

	public void recordDelay(long delay)
	{
		totalCompileTime += delay;
	}

	public void incCountPrivate()
	{
		countPrivate++;
	}

	public void incCountProtected()
	{
		countProtected++;
	}

	public void incCountPublic()
	{
		countPublic++;
	}

	public void incCountStatic()
	{
		countStatic++;
	}

	public void incCountFinal()
	{
		countFinal++;
	}

	public void incCountSynchronized()
	{
		countSynchronized++;
	}

	public void incCountStrictfp()
	{
		countStrictfp++;
	}

	public void incCountNative()
	{
		countNative++;
	}
	
	public void incCompilerThreads()
	{
		countCompilerThreads++;
	}

	public void incCountAbstract()
	{
		countAbstract++;
	}

	public void incCountOSR()
	{
		countOSR++;
	}

	public void incCountC1()
	{
		countC1++;
	}

	public void incCountC2()
	{
		countC2++;
	}

	public void incCountC2N()
	{
		countC2N++;
	}

	public void incCountClass()
	{
		countClass++;
	}

	public void incCountMethod()
	{
		countMethod++;
	}

	public void incCountConstructor()
	{
		countConstructor++;
	}

	public void addNativeBytes(long count)
	{
		nativeBytes += count;
	}

	public long getCountPrivate()
	{
		return countPrivate;
	}

	public long getCountProtected()
	{
		return countProtected;
	}

	public long getCountPublic()
	{
		return countPublic;
	}

	public long getCountStatic()
	{
		return countStatic;
	}

	public long getCountFinal()
	{
		return countFinal;
	}

	public long getCountSynchronized()
	{
		return countSynchronized;
	}

	public long getCountStrictfp()
	{
		return countStrictfp;
	}

	public long getCountNative()
	{
		return countNative;
	}
	
	public long getCountCompilerThreads()
	{
		return countCompilerThreads;
	}

	public long getCountAbstract()
	{
		return countAbstract;
	}

	public long getCountOSR()
	{
		return countOSR;
	}

	public long getCountC1()
	{
		return countC1;
	}

	public long getCountC2()
	{
		return countC2;
	}

	public long getCountC2N()
	{
		return countC2N;
	}

	public long getCountClass()
	{
		return countClass;
	}

	public void setCountClass(long countClass)
	{
		this.countClass = countClass;
	}

	public long getCountMethod()
	{
		return countMethod;
	}

	public void setCountMethod(long countMethod)
	{
		this.countMethod = countMethod;
	}

	public long getCountConstructor()
	{
		return countConstructor;
	}

	public void setCountConstructor(long countConstructor)
	{
		this.countConstructor = countConstructor;
	}

	public long getTotalCompileTime()
	{
		return totalCompileTime;
	}

	public long getTotalCompiledMethods()
	{
		return countC1 + countC2 + countC2N;
	}

	public long getNativeBytes()
	{
		return nativeBytes;
	}
}