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
	private Map<Long, Integer> histoMap = new HashMap<>();
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
		if (resolution > 1)
		{
			time = (time / resolution) * resolution;
		}
		
		int newCount = 0;

		if (histoMap.containsKey(time))
		{
			newCount = histoMap.get(time) + 1;
		}
		else
		{
			newCount = 1;
		}

		histoMap.put(time, newCount);

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
		histoMap.clear();
		lastTime = 0;
		maxCount = 0;
	}

	public List<Map.Entry<Long, Integer>> getSortedData()
	{
		List<Map.Entry<Long, Integer>> result = new ArrayList<>(histoMap.entrySet());

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

	public long getLastTime()
	{
		return lastTime;
	}

	public int getMaxCount()
	{
		return maxCount;
	}
}
