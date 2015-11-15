/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.toplist;

import java.util.List;

import org.adoptopenjdk.jitwatch.treevisitor.ITreeVisitable;

public interface ITopListVisitable extends ITreeVisitable
{
	List<ITopListScore> buildTopList();
}