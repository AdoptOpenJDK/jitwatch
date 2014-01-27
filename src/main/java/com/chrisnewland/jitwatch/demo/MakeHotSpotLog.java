/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.demo;

// Demo class to generate an example hotspot.log
// run with VM arguments
// -XX:+UnlockDiagnosticVMOptions
// -XX:+TraceClassLoading 
// -XX:+LogCompilation 
// -XX:+PrintAssembly
public class MakeHotSpotLog
{
	public MakeHotSpotLog(int iterations)
	{
		addVariable(iterations);
		addConstant(iterations);
		randomBranchTest(iterations);
		intrinsicTest(iterations);
	}

	public void addVariable(int iterations)
	{
		long count = 0;

		for (int i = 0; i < iterations; i++)
		{
			count = add(count, i);
		}

		System.out.println("addVariable: " + count);
	}

	private void addConstant(int iterations)
	{
		long count = 0;

		for (int i = 0; i < iterations; i++)
		{
			count = add(count, 1);
		}

		System.out.println("addConstant: " + count);
	}

	private void randomBranchTest(int iterations)
	{
		long count = 0;
		int adds = 0;
		int subs = 0;

		for (int i = 0; i < iterations; i++)
		{
			if (Math.random() < 0.5)
			{
				count = add(count, 1);
				adds++;
			}
			else
			{
				count = sub(count, 1);
				subs++;
			}
		}

		System.out.println("randomBranchTest: " + count  + " " + adds + " " + subs);
	}

	private void intrinsicTest(int iterations)
	{

		long dstSum = 0;
		int[] src = new int[]{1,2,3,4,5};
		int[] dst = new int[src.length];

		for (int i = 0; i < iterations; i++)
		{
			//x86 has intrinsic for System.arrayCopy
			System.arraycopy(src, 0, dst, 0, src.length);
			
			for(int dstVal : dst)
			{
				dstSum += add(dstSum, dstVal);
			}
		}

		System.out.println("intrinsicTest: " + dstSum);
	}
	
	private long add(long a, long b)
	{
		return a + b;
	}
	
	private long sub(long a, long b)
	{
		return a - b;
	}

	public static void main(String[] args)
	{
		int iterations = 1_000_000;

		if (args.length == 1)
		{
			try
			{
				iterations = Integer.parseInt(args[0]);
			}
			catch (NumberFormatException nfe)
			{
				System.err.println("usage: MakeHotSpotLog [iterations]");
			}
		}

		new MakeHotSpotLog(iterations);
	}
}
