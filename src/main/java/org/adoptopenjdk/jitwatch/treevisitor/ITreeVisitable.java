/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.treevisitor;

import org.adoptopenjdk.jitwatch.model.IMetaMember;

public interface ITreeVisitable
{
    void visit(IMetaMember mm);
    void reset();
}