package com.chrisnewland.jitwatch.core;

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
	
	private Histo histo = new Histo(10);
	
	
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
		histo.clear();
	}
	
	public void recordDelay(long delay)
	{
		totalCompileTime += delay;
		histo.recordTime(delay);
	}

	public Histo getHisto()
	{
		return histo;
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
	
	public long getTotalCompileTime()
	{
		return totalCompileTime;
	}
	
	public long getNativeBytes()
	{
		return nativeBytes;
	}
}