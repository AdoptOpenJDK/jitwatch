/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
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
		intrinsicTestMin(iterations);
		tooBigToInline(iterations);
		testSort(iterations);
		testCallChain(iterations);
		testOptimizedVCall(iterations);

		long result = testCallChainReturn(iterations);

		// ensure code not eliminated by using result
		System.out.println("testCallChainReturn: " + result);

		testCallChain3();
		testLeaf(iterations);
		testToUpperCase(iterations);
		testLoopUnrolling(iterations);
		padMethod();
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

		System.out.println("intrinsicTest: " + dstSum);
	}

	// http://openjdk.5641.n7.nabble.com/Intrinsics-for-Math-min-and-max-td183747.html
	private void intrinsicTestMin(int iterations)
	{
		long sum = 0;

		for (int i = 0; i < iterations; i++)
		{
			// x86 has intrinsic for Math.min

			sum = Math.min(i, i + 1);
		}

		System.out.println("intrinsicTest: " + sum);
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

	private void testSort(long iterations)
	{

		long sum = 0;

		// ensure sort is JIT compiled
		for (int i = 0; i < iterations; i++)
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

	/*
	 * Test bimorphic dispatch is inlined
	 */
	private void testOptimizedVCall(long iterations)
	{
		List<Integer> list1 = new ArrayList<>();
		List<Integer> list2 = new LinkedList<>();

		List<Integer> ref;

		for (int i = 0; i < iterations; i++)
		{
			if (i % 2 == 0)
			{
				ref = list1;
			}
			else
			{
				ref = list2;
			}

			ref.add(i);
			// list2.add(i);
		}

		System.out.println("list sizes: " + list1.size() + "/" + list2.size());

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

	public long testCallChainReturn(long iterations)
	{
		long count = 0;

		for (int i = 0; i < iterations; i++)
		{
			count = chainA1(count);
			count = chainB1(count);
		}

		return count;
	}

	private boolean test(int count, int iterations)
	{
		return count < (0.9 * (double) iterations);
	}

	private void testCallChain3()
	{
		long count = 0;

		int iterations = 100_000;

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

		System.out.println("testCallChain3: " + count);
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

	private void testLoopUnrolling(long iterations)
	{
		long result = 0;
		int toAdd = 4;

		for (long l = 0; l < iterations; l++)
		{
			result += timesTen(toAdd);
		}

		for (long l = 0; l < iterations; l++)
		{
			result += timesHundred(toAdd);
		}

		System.out.println("testLoopUnrolling: " + result);
	}

	private int timesTen(int number)
	{
		int result = 0;

		for (int i = 0; i < 10; i++)
		{
			result += number;
		}

		return result;
	}

	private int timesHundred(int number)
	{
		int result = 0;

		for (int i = 0; i < 100; i++)
		{
			result += number;
		}

		return result;
	}

	// sacrificial dummy method
	// in case hotspot truncates the LogCompilation output
	// as it exits and produces a <fragment>
	private void padMethod()
	{
		try
		{
			Thread.sleep(500);
			System.out.println("done");
		}
		catch (InterruptedException ie)
		{
			logger.error("", ie);
		}
	}

	public static void main(String[] args)
	{
		int iterations = 20_000;

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
