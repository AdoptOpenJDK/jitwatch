/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.toplist;

import com.chrisnewland.jitwatch.treevisitor.ITreeVisitable;

import java.util.List;

public interface ITopListVisitable extends ITreeVisitable
{
	List<ITopListScore> buildTopList();
}