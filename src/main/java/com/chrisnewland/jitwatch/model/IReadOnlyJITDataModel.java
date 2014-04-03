/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.model;

import java.util.List;

import com.chrisnewland.jitwatch.core.JITEvent;
import com.chrisnewland.jitwatch.core.JITStats;

public interface IReadOnlyJITDataModel
{
	public PackageManager getPackageManager();

	public JITStats getJITStats();

	public List<JITEvent> getEventListCopy();

	public List<Tag> getCodeCacheTags();
	
	public String getVmVersionRelease();
}