/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.treevisitor;

import com.chrisnewland.jitwatch.model.IMetaMember;

public interface ITreeVisitable
{
    void visit(IMetaMember mm);
    void reset();
}