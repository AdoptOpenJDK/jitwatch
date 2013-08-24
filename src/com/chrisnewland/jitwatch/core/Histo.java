package com.chrisnewland.jitwatch.core;

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

	public void recordTime(long time)
	{
		values.add(time);

		if (resolution > 1)
		{
			time = (time / resolution) * resolution;
		}

		int newCount = 0;

		if (bucketMap.containsKey(time))
		{
			newCount = bucketMap.get(time) + 1;
		}
		else
		{
			newCount = 1;
		}

		bucketMap.put(time, newCount);

		if (newCount > maxCount)
		{
			maxCount = newCount;
		}

		if (time > lastTime)
		{
			lastTime = time;
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
	public long getPercentile(int percentile)
	{
		long result = 0;

		Collections.sort(values);

		int count = values.size();

		if (percentile >= 100)
		{
			result = values.get(count - 1);
		}
		else if (percentile <= 0)
		{
			result = 0;
		}
		else
		{
			double position = 0.5 + ((double) percentile) / 100.0 * (double) count;	
			int index = (int)Math.round(position) - 1;
		
			result = values.get(index);
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
