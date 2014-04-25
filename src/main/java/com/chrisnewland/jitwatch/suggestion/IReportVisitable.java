/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.suggestion;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.treevisitor.ITreeVisitable;

import java.util.Map;

public interface IReportVisitable extends ITreeVisitable
{
	Map<IMetaMember, String> getReportMap();
}
