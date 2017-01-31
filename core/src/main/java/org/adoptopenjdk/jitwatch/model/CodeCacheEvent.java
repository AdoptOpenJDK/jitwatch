/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

public class CodeCacheEvent
{
	public enum CodeCacheEventType
	{
		COMPILATION, SWEEPER, CACHE_FULL
	};

	private CodeCacheEventType eventType;

	private long stamp;
	private long nativeCodeSize;
	private long freeCodeCache;
	private long nativeAddress;

	private Compilation compilation;
	
	private int compilationLevel;

	public CodeCacheEvent(CodeCacheEventType eventType, long stamp, long nativeCodeSize, long freeCodeCache)
	{
		super();
		this.eventType = eventType;
		this.stamp = stamp;
		this.nativeCodeSize = nativeCodeSize;
		this.freeCodeCache = freeCodeCache;
	}

	public void setNativeAddress(long address)
	{
		this.nativeAddress = address;
	}

	public long getNativeAddress()
	{
		return nativeAddress;
	}

	public long getStamp()
	{
		return stamp;
	}

	public long getNativeCodeSize()
	{
		return nativeCodeSize;
	}

	public Compilation getCompilation()
	{
		return compilation;
	}

	public void setCompilation(Compilation compilation)
	{
		this.compilation = compilation;
		
		compilationLevel = compilation.getLevel();
	}

	public int getCompilationLevel()
	{
		return compilationLevel;
	}

	public long getFreeCodeCache()
	{
		return freeCodeCache;
	}

	public CodeCacheEventType getEventType()
	{
		return eventType;
	}
}