/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui.triview;

import com.chrisnewland.jitwatch.model.IMetaMember;

public interface ITriViewAccessor
{
	public void openTriView(IMetaMember member);
}
