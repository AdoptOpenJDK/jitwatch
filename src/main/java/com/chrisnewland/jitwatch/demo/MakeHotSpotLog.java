/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

// If you prefer Intel assembly syntax to AT&T
// -XX:PrintAssemblyOptions=intel

// Disable TieredCompilation on Java 8 (optional)
// -XX:-TieredCompilation

public class MakeHotSpotLog
{
    private static final Logger logger = LoggerFactory.getLogger(MakeHotSpotLog.class);

	public MakeHotSpotLog(int iterations)
	{
		addVariable(iterations);
		addConstant(iterations);
		randomBranchTest(iterations);
		changingBranchTest(iterations);
		intrinsicTest(iterations);
		tooBigToInline(iterations);
		testSort();
		testCallChain(iterations);
		testCallChain2(iterations);
		testLeaf(iterations);
	}

	private void addVariable(int iterations)
	{
		long count = 0;

		for (int i = 0; i < iterations; i++)
		{
			count = add(count, i);
		}

        logger.info(String.format("addVariable: %d", count));
	}

	private void addConstant(int iterations)
	{
		long count = 0;

		for (int i = 0; i < iterations; i++)
		{
			count = add(count, 1);
		}

        logger.info(String.format("addConstant: %d", count));
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

        logger.info(String.format("randomBranchTest: %d %d %d", count, adds, subs));
	}

	private void changingBranchTest(int iterations)
	{
		long count = 0;
		int adds = 0;
		int subs = 0;

		for (int i = 0; i < iterations * 2; i++)
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

        logger.info(String.format("changingBranchTest: %d %d %d", count, adds, subs));
	}

	private void intrinsicTest(int iterations)
	{

		long dstSum = 0;
		int[] src = new int[] { 1, 2, 3, 4, 5 };
		int[] dst = new int[src.length];

		for (int i = 0; i < iterations; i++)
		{
			// x86 has intrinsic for System.arrayCopy
			System.arraycopy(src, 0, dst, 0, src.length);

			for (int dstVal : dst)
			{
				dstSum += add(dstSum, dstVal);
			}
		}

        logger.info(String.format("intrinsicTest: %d", dstSum));
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

        logger.warn(String.format("tooBigToInline: %d", count));
	}

	private long bigMethod(long count, int i)
	{
		long a, b, c, d, e, f, g;

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

		long result = a + b + c + d + e + f + g;

		return result;
	}

	private void testSort()
	{

		long sum = 0;

		// ensure sort is JIT compiled
		for (int i = 0; i < 20000; i++)
		{
			Random random = new Random();

			int count = 1000;
			
			List<Integer> list = new ArrayList<>();

			for (int j = 0; j < count; j++)
			{
				list.add(random.nextInt());
			}

			Collections.sort(list);

			for (int j = 0; j < count; j++)
			{
				sum += list.get(j);
			}
		}

        logger.info(String.format("list sum: %d", sum));
	}

	private void testCallChain(long iterations)
	{
		long count = 0;

		for (int i = 0; i < iterations; i++)
		{
			count = chainA1(count);
			count = chainB1(count);
		}

        logger.info(String.format("testCallChain: %d", count));
	}

	private long chainA1(long count)
	{
		return 1 + chainA2(count);
	}

	private long chainA2(long count)
	{
		return 2 + chainA3(count);
	}

	private long chainA3(long count)
	{
		return 3 + chainA4(count);
	}

	private long chainA4(long count)
	{
		// last link will not be inlined
		return bigMethod(count, 4);
	}

	private long chainB1(long count)
	{
		return chainB2(count) - 1;
	}

	private long chainB2(long count)
	{
		return chainB3(count) - 2;
	}

	private long chainB3(long count)
	{
		return count - 3;
	}

	private void testCallChain2(long iterations)
	{
		long count = 0;

		for (int i = 0; i < iterations; i++)
		{
			count = chainC1(count);
			count = chainC2(count);

		}

        logger.warn(String.format("testCallChain2: %d", count));
	}

	private long chainC1(long count)
	{
		count += chainC2(count);
		return chainC3(count);
	}

	private long chainC2(long count)
	{
		return 2 + count;
	}

	private long chainC3(long count)
	{
		return 3 + count;
	}
	
	private void testLeaf(long iterations)
	{
		long count = 0;

		for (int i = 0; i < iterations; i++)
		{
			count = leaf1(count);
			count = leaf2(count);
			count = leaf3(count);
			count = leaf4(count);
		}

		logger.info("testLeaf: " + count);
	}
	
	private long leaf1(long count)
	{
		return count + 1;
	}
	
	private long leaf2(long count)
	{
		return count + 2;
	}
	
	private long leaf3(long count)
	{
		return count + 3;
	}
	
	private long leaf4(long count)
	{
		return count + 4;
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
                logger.error(String.format("usage: MakeHotSpotLog [iterations]"), nfe);
			}
		}

		new MakeHotSpotLog(iterations);
	}
}
