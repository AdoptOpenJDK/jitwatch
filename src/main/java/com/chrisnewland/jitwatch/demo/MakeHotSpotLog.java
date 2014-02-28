/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
		changingBranchTest(iterations);
		intrinsicTest(iterations);
		tooBigToInline(iterations);
		testSort();
	}

	private void addVariable(int iterations)
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

	private void changingBranchTest(int iterations)
	{
		long count = 0;
		int adds = 0;
		int subs = 0;

		for (int i = 0; i < iterations*2; i++)
		{
			if (i < iterations)
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

		System.out.println("changingBranchTest: " + count  + " " + adds + " " + subs);
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
	
	public void tooBigToInline(int iterations)
	{
		long count = 0;

		for (int i = 0; i < iterations; i++)
		{
			count = bigMethod(count, i);
		}

		System.out.println("tooBigToInline: " + count);
	}
	
	private long bigMethod(long count, int i)
	{
		long a,b,c,d,e,f,g;
		
		a = count;
		b = count;
		c = count;
		d = count;
		e = count;
		f = count;
		g = count;
		
		a += i;
		b += i;
		c += i;
		d += i;
		e += i;
		f += i;
		g += i;
		
		a += 1;
		b += 2;
		c += 3;
		d += 4;
		e += 5;
		f += 6;
		g += 7;
		
		a += i;
		b += i;
		c += i;
		d += i;
		e += i;
		f += i;
		g += i;
		
		a -= 7;
		b -= 6;
		c -= 5;
		d -= 4;
		e -= 3;
		f -= 2;
		g -= 1;
		
		a++;
		b++;
		c++;
		d++;
		e++;
		f++;
		g++;
		
		a /= 2;
		b /= 2;
		c /= 2;
		d /= 2;
		e /= 2;
		f /= 2;
		g /= 2;
		
		long result = a+b+c+d+e+f+g;
		
		return result;
	}
	
	private void testSort()
	{
		List<Integer> list = new ArrayList<>();
		
		int count = 1_000_000;
		
		Random seededRandom = new Random(12345678);
		
		for (int i = 0; i < count; i++)
		{
			list.add(seededRandom.nextInt());
		}
		
		Collections.sort(list);
		
		System.out.println("list size: " + list.size());
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
