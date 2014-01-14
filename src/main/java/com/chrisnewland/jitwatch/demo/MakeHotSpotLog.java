/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.demo;

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
			count = add(count, i);
		}

		System.out.println("addConstant: " + count);
	}

	private long add(long a, long b)
	{
		return a + b;
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
