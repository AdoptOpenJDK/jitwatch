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

// If you prefer Intel assembly syntax to AT&T
// -XX:PrintAssemblyOptions=intel

// Disable TieredCompilation on Java 8 (optional)
// -XX:-TieredCompilation

public class MakeHotSpotLog
{

    private static final double HALF = 0.5;
    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int THREE = 3;
    private static final int FOUR = 4;
    private static final int FIVE = 5;
    private static final int SIX = 6;
    private static final int SEVEN = 7;
    private static final int ONE_THOUSAND_TIMES = 1000;
    private static final double NINETY_PERCENT_OF = 0.9;
    private static final int ONE_HUNDRED_THOUSAND = 100_000;
    private static final int TWENTY_THOUSAND_TIMES = 20_000;

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
		testCallChain3();
		testLeaf(iterations);
		testToUpperCase(iterations);
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
			if (Math.random() < HALF)
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

		System.out.println("randomBranchTest: " + count + " " + adds + " " + subs);
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

		System.out.println("changingBranchTest: " + count + " " + adds + " " + subs);
	}

	private void intrinsicTest(int iterations)
	{

		long dstSum = 0;
		int[] src = new int[] { ONE, TWO, THREE, FOUR, FIVE };
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

		a += ONE;
		b += TWO;
		c += THREE;
		d += FOUR;
		e += FIVE;
		f += SIX;
		g += SEVEN;

		a += i;
		b += i;
		c += i;
		d += i;
		e += i;
		f += i;
		g += i;

		a -= SEVEN;
		b -= SIX;
		c -= FIVE;
		d -= FOUR;
		e -= THREE;
		f -= TWO;
		g -= ONE;

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
		for (int i = 0; i < TWENTY_THOUSAND_TIMES; i++)
		{
			Random random = new Random();

			int count = ONE_THOUSAND_TIMES;

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

		System.out.println("list sum: " + sum);
	}

	private void testCallChain(long iterations)
	{
		long count = 0;

		for (int i = 0; i < iterations; i++)
		{
			count = chainA1(count);
			count = chainB1(count);
		}

		System.out.println("testCallChain: " + count);
	}

	private long chainA1(long count)
	{
		return ONE + chainA2(count);
	}

	private long chainA2(long count)
	{
		return TWO + chainA3(count);
	}

	private long chainA3(long count)
	{
		return THREE + chainA4(count);
	}

	private long chainA4(long count)
	{
		// last link will not be inlined
		return bigMethod(count, FOUR);
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

		System.out.println("testCallChain2: " + count);
	}

	private boolean test(int count, int iterations)
	{
		return count < (NINETY_PERCENT_OF * (double) iterations);
	}

	private void testCallChain3()
	{
		long count = 0;

		int iterations = ONE_HUNDRED_THOUSAND;

		for (int i = 0; i < iterations; i++)
		{
			if (test(i, iterations))
			{
				count = chainC1(count);
			}
			else
			{
				count = chainC2(count);
			}
		}

		System.out.println("testCallChain2: " + count);
	}

	private long chainC1(long inCount)
	{
		long count = inCount;
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

		System.out.println("testLeaf: " + count);
	}

	private long leaf1(long count)
	{
		return count + ONE;
	}

	private long leaf2(long count)
	{
		return count + TWO;
	}

	private long leaf3(long count)
	{
		return count + THREE;
	}

	private long leaf4(long count)
	{
		return count + FOUR;
	}

	private void testToUpperCase(long iterations)
	{
		String sentence = "The quick brown fox jumps over the lazy dog\n";

		String[] lcWords = sentence.split(" ");

		int wordCount = lcWords.length;

		String[] ucWords = new String[wordCount];

		for (long l = 0; l < iterations; l++)
		{
			toUpper(lcWords, ucWords, wordCount);
		}
	}

	private void toUpper(String[] lcWords, String[] ucWords, int wordCount)
	{
		for (int w = 0; w < wordCount; w++)
		{
			ucWords[w] = lcWords[w].toUpperCase();
		}
	}

	public static void main(String[] args)
	{
		int iterations = TWENTY_THOUSAND_TIMES;

		if (args.length == 1)
		{
			try
			{
				iterations = Integer.parseInt(args[0]);
			}
			catch (NumberFormatException nfe)
			{
				System.out.println("usage: MakeHotSpotLog [iterations]");
			}
		}

		new MakeHotSpotLog(iterations);
	}
}
