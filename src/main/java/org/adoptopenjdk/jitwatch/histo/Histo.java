/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.histo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Histo
{
	private Map<Long, Integer> bucketMap = new HashMap<>();

	// lots of writes, not COWAL
	private List<Long> values = new ArrayList<>();

	private long lastTime = 0;
	private int maxCount = 0;
	private long resolution = 1;

	public Histo()
	{

	}

	public Histo(long resolution)
	{
		this.resolution = resolution;
	}

	public void addValue(long inValue)
	{
        long value = inValue;
		synchronized (values)
		{
			values.add(value);
		}

		if (resolution > 1)
		{
			value = (value / resolution) * resolution;
		}

		int newCount = 0;

		if (bucketMap.containsKey(value))
		{
			newCount = bucketMap.get(value) + 1;
		}
		else
		{
			newCount = 1;
		}

		bucketMap.put(value, newCount);

		if (newCount > maxCount)
		{
			maxCount = newCount;
		}

		if (value > lastTime)
		{
			lastTime = value;
		}

	}

	public void clear()
	{
		bucketMap.clear();
		lastTime = 0;
		maxCount = 0;
	}

	public List<Map.Entry<Long, Integer>> getSortedData()
	{
		List<Map.Entry<Long, Integer>> result = new ArrayList<>(bucketMap.entrySet());

		Collections.sort(result, new Comparator<Map.Entry<Long, Integer>>()
		{
			@Override
			public int compare(Entry<Long, Integer> o1, Entry<Long, Integer> o2)
			{
				return o1.getValue().compareTo(o2.getValue());
			}
		});

		return result;
	}

	/*
	 * Nearest rank percentile calculation from
	 * http://en.wikipedia.org/wiki/Percentile
	 */
	public long getPercentile(double percentile)
	{
		long result = 0;

		List<Long> valuesCopy = null;

		synchronized (values)
		{
			valuesCopy = new ArrayList<>(values);
		}

		Collections.sort(valuesCopy);

		int count = valuesCopy.size();

		if (percentile >= 100)
		{
			result = valuesCopy.get(count - 1);
		}
		else if (percentile <= 0)
		{
			result = 0;
		}
		else
		{
			double position = 0.5 + (percentile) / 100.0 * count;
			int index = (int) Math.round(position) - 1;

			result = valuesCopy.get(index);
		}

		return result;
	}

	public long getLastTime()
	{
		return lastTime;
	}

	public int getMaxCount()
	{
		return maxCount;
	}
}
