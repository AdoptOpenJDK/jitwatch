/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.parser.j9;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_ADDRESS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_BYTES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILER;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILE_ID;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_METHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_NMSIZE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_SIZE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_STAMP;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_QUOTE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.J9;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_NMETHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TASK_DONE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TASK_QUEUED;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.model.Task;
import org.adoptopenjdk.jitwatch.util.StringUtil;

public class J9Line
{
	public static final String TEMPERATURE_COLD = "cold";
	public static final String TEMPERATURE_WARM = "warm";
	public static final String TEMPERATURE_PROFILED_VERY_HOT = "profiled very-hot";

	private String temperature;
	private String signature;
	private String rangeStart;
	private String rangeEnd;

	private Map<String, String> attributes = new HashMap<>();

	private Set<String> features = new HashSet<>();

	public String getTemperature()
	{
		return temperature;
	}

	public void setTemperature(String temperature)
	{
		this.temperature = temperature;
	}

	public String getSignature()
	{
		return signature;
	}

	public void setSignature(String signature)
	{
		this.signature = signature;
	}

	public String getRangeStart()
	{
		return rangeStart;
	}

	public String getRangeEnd()
	{
		return rangeEnd;
	}

	public void setRange(String range)
	{
		if (range != null)
		{
			String[] parts = range.split("-");

			if (parts.length == 2)
			{
				this.rangeStart = parts[0];
				this.rangeEnd = parts[1];
			}
		}
	}

	public Map<String, String> getAttributes()
	{
		return attributes;
	}

	public void addAttribute(String key, String value)
	{
		this.attributes.put(key, value);
	}

	public Set<String> getFeatures()
	{
		return features;
	}

	public void addFeatures(String feature)
	{
		this.features.add(feature);
	}

	public boolean hasFeature(String feature)
	{
		return features.contains(feature);
	}

	public MemberSignatureParts getMemberSignatureParts() throws LogParseException
	{
		String logCompilationSignature = J9Util.convertJ9SigToLogCompilationSignature(signature);

		return MemberSignatureParts.fromLogCompilationSignature(logCompilationSignature);
	}

	public int getBytecodeSize()
	{
		int result = 0;

		String bcszAttr = attributes.get("bcsz");

		if (bcszAttr != null)
		{
			try
			{
				result = Integer.parseInt(bcszAttr);
			}
			catch (NumberFormatException nfe)
			{
				nfe.printStackTrace(); // TODO log
			}
		}

		return result;
	}

	public int getNativeSize()
	{

		long highAddress = 0;
		long lowAddress = 0;

		try
		{
			highAddress = Long.parseLong(rangeEnd, 16);
			lowAddress = Long.parseLong(rangeStart, 16);

		}
		catch (NumberFormatException nfe)
		{
			nfe.printStackTrace(); // TODO log
		}

		return (int) (highAddress - lowAddress);
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("J9Line [getTemperature()=");
		builder.append(getTemperature());
		builder.append(", getSignature()=");
		builder.append(getSignature());
		builder.append(", getRangeStart()=");
		builder.append(getRangeStart());
		builder.append(", getRangeEnd()=");
		builder.append(getRangeEnd());
		builder.append(", getAttributes()=");
		builder.append(getAttributes());
		builder.append(", getFeatures()=");
		builder.append(getFeatures());
		builder.append(", getMemberSignatureParts()=");
		try
		{
			builder.append(getMemberSignatureParts()).append("\n");
		}
		catch (LogParseException e)
		{
			e.printStackTrace();
		}
		builder.append(", getBytecodeSize()=");
		builder.append(getBytecodeSize());
		builder.append("]");
		return builder.toString();
	}

	
	public Tag toTagQueued(int compiledID, long timestampMillis)
	{
		Map<String, String> map = new HashMap<>();
		
		map.put(ATTR_COMPILE_ID, Integer.toString(compiledID));
		map.put(ATTR_STAMP, Long.toString(timestampMillis));
		map.put(ATTR_METHOD, J9Util.convertJ9SigToLogCompilationSignature(signature));
		map.put(ATTR_BYTES, Integer.toString(getBytecodeSize()));
		
		Tag tag = new Tag(TAG_TASK_QUEUED, StringUtil.attributeMapToString(map, C_QUOTE), true);

		return tag;
	}

	public Tag toTagNMethod(int compiledID, long timestampMillis)
	{
		Map<String, String> map = new HashMap<>();
		map.put(ATTR_COMPILE_ID, Integer.toString(compiledID));
		map.put(ATTR_STAMP, Long.toString(timestampMillis));
		map.put(ATTR_METHOD, J9Util.convertJ9SigToLogCompilationSignature(signature));
		map.put(ATTR_COMPILER, J9);
		map.put(ATTR_ADDRESS, rangeStart);
		map.put(ATTR_SIZE, Integer.toString(getNativeSize()));
		map.put(ATTR_BYTES, Integer.toString(getBytecodeSize()));

		Tag tag = new Tag(TAG_NMETHOD, StringUtil.attributeMapToString(map, C_QUOTE), true);

		return tag;
	}

	public Task toTagTask(int compiledID, long timestampMillis)
	{
		Map<String, String> map = new HashMap<>();
		map.put(ATTR_COMPILE_ID, Integer.toString(compiledID));
		map.put(ATTR_STAMP, Long.toString(timestampMillis));
		map.put(ATTR_METHOD, J9Util.convertJ9SigToLogCompilationSignature(signature));

		Task task = new Task(StringUtil.attributeMapToString(map, C_QUOTE), true);

		Map<String, String> doneAttrs = new HashMap<>();
		doneAttrs.put(ATTR_NMSIZE, Integer.toString(getNativeSize()));
		doneAttrs.put(ATTR_STAMP, Long.toString(timestampMillis));
		doneAttrs.put("success", "1");

		Tag tagTaskDone = new Tag(TAG_TASK_DONE, StringUtil.attributeMapToString(doneAttrs), true);
		
		task.addChild(tagTaskDone);
				
		return task;
	}
}