/*
 * Copyright (c) 2018 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.parser.zing;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_ADDRESS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_BYTES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILER;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILE_ID;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_METHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_NMSIZE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_SIZE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_STAMP;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_QUOTE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ZING;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.FALCON;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_NMETHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TASK_DONE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TASK_QUEUED;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_LEVEL;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.model.Task;
import org.adoptopenjdk.jitwatch.util.StringUtil;

public class ZingLine
{
	private int compileId;
	private int tier;
	private int bytecodeSize;
	private int nativeSize;
	private int score;
	private boolean throwsExceptions;
	private boolean stashedCompile;
	private long timestampMillisQueued;
	private long timestampMillisCompileStart;
	private long timestampMillisNMethodEmitted;

	private String signature;
	private long startAddress;
	private long endAddress;

	private ZingLineType lineType;

	public ZingLineType getLineType()
	{
		return lineType;
	}

	public void setLineType(ZingLineType lineType)
	{
		this.lineType = lineType;
	}

	public int getCompileId()
	{
		return compileId;
	}

	public void setCompileId(int compileId)
	{
		this.compileId = compileId;
	}

	public int getTier()
	{
		return tier;
	}

	public void setTier(int tier)
	{
		this.tier = tier;
	}

	public int getNativeSize()
	{
		return nativeSize;
	}

	public void setNativeSize(int nativeSize)
	{
		this.nativeSize = nativeSize;
	}

	public int getScore()
	{
		return score;
	}

	public void setScore(int score)
	{
		this.score = score;
	}

	public boolean isThrowsExceptions()
	{
		return throwsExceptions;
	}

	public void setThrowsExceptions(boolean throwsExceptions)
	{
		this.throwsExceptions = throwsExceptions;
	}

	public boolean isStashedCompile()
	{
		return stashedCompile;
	}

	public void setStashedCompile(boolean stashedCompile)
	{
		this.stashedCompile = stashedCompile;
	}

	public long getStartAddress()
	{
		return startAddress;
	}

	public void setStartAddress(long startAddress)
	{
		this.startAddress = startAddress;
	}

	public long getEndAddress()
	{
		return endAddress;
	}

	public void setEndAddress(long endAddress)
	{
		this.endAddress = endAddress;
	}

	public void setBytecodeSize(int bytecodeSize)
	{
		this.bytecodeSize = bytecodeSize;
	}

	public String getSignature()
	{
		return signature;
	}

	public void setSignature(String signature)
	{
		this.signature = signature;
	}

	public MemberSignatureParts getMemberSignatureParts() throws LogParseException
	{
		return MemberSignatureParts.fromLogCompilationSignature(signature);
	}

	public int getBytecodeSize()
	{
		return bytecodeSize;
	}

	@Override
	public String toString()
	{
		return "ZingLine [compileId=" + compileId + ", tier=" + tier + ", bytecodeSize=" + bytecodeSize + ", nativeSize="
				+ nativeSize + ", score=" + score + ", throwsExceptions=" + throwsExceptions + ", stashedCompile=" + stashedCompile
				+ ", timestampMillisQueued=" + timestampMillisQueued + ", timestampMillisCompileStart="
				+ timestampMillisCompileStart + ", timestampMillisNMethodEmitted=" + timestampMillisNMethodEmitted + ", signature="
				+ signature + ", startAddress=" + startAddress + ", endAddress=" + endAddress + ", lineType=" + lineType + "]";
	}

	private String millisToSecondsString(long millis)
	{
		return new BigDecimal(Long.toString(millis)).divide(new BigDecimal("1000")).toPlainString();
	}

	public Tag toTagQueued()
	{
		Map<String, String> map = new HashMap<>();

		map.put(ATTR_COMPILE_ID, Integer.toString(compileId));
		map.put(ATTR_STAMP, millisToSecondsString(timestampMillisQueued));
		map.put(ATTR_METHOD, signature);
		map.put(ATTR_BYTES, Integer.toString(getBytecodeSize()));

		Tag tag = new Tag(TAG_TASK_QUEUED, StringUtil.attributeMapToString(map, C_QUOTE), true);

		return tag;
	}

	public Tag toTagNMethod()
	{
		Map<String, String> map = new HashMap<>();
		map.put(ATTR_COMPILE_ID, Integer.toString(compileId));
		map.put(ATTR_STAMP, millisToSecondsString(timestampMillisNMethodEmitted));
		map.put(ATTR_METHOD, signature);
		map.put(ATTR_COMPILER, tier == 3 ? FALCON : ZING);
		map.put(ATTR_ADDRESS, Long.toHexString(startAddress));
		map.put(ATTR_SIZE, Integer.toString(getNativeSize()));
		map.put(ATTR_BYTES, Integer.toString(getBytecodeSize()));
		map.put(ATTR_LEVEL, Integer.toString(tier));

		Tag tag = new Tag(TAG_NMETHOD, StringUtil.attributeMapToString(map, C_QUOTE), true);

		return tag;
	}

	public Task toTagTask()
	{
		Map<String, String> map = new HashMap<>();
		map.put(ATTR_COMPILE_ID, Integer.toString(compileId));
		map.put(ATTR_STAMP, millisToSecondsString(timestampMillisCompileStart));
		map.put(ATTR_METHOD, signature);
		map.put(ATTR_LEVEL, Integer.toString(tier));
		map.put(ATTR_BYTES, Integer.toString(getBytecodeSize()));

		Task task = new Task(StringUtil.attributeMapToString(map, C_QUOTE), true);

		Map<String, String> doneAttrs = new HashMap<>();
		doneAttrs.put(ATTR_NMSIZE, Integer.toString(getNativeSize()));
		doneAttrs.put(ATTR_STAMP, millisToSecondsString(timestampMillisNMethodEmitted));
		doneAttrs.put("success", "1");

		Tag tagTaskDone = new Tag(TAG_TASK_DONE, StringUtil.attributeMapToString(doneAttrs), true);

		task.addChild(tagTaskDone);

		return task;
	}

	public long getTimestampMillisQueued()
	{
		return timestampMillisQueued;
	}

	public void setTimestampMillisQueued(long timestampMillisQueued)
	{
		this.timestampMillisQueued = timestampMillisQueued;
	}

	public long getTimestampMillisCompileStart()
	{
		return timestampMillisCompileStart;
	}

	public void setTimestampMillisCompileStart(long timestampMillisCompileStart)
	{
		this.timestampMillisCompileStart = timestampMillisCompileStart;
	}

	public long getTimestampMillisNMethodEmitted()
	{
		return timestampMillisNMethodEmitted;
	}

	public void setTimestampMillisNMethodEmitted(long timestampMillisNMethodEmitted)
	{
		this.timestampMillisNMethodEmitted = timestampMillisNMethodEmitted;
	}
}