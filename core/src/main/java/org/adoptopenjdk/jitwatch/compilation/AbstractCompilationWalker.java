/*
 * Copyright (c) 2017-2019 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.compilation;

import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.treevisitor.ITreeVisitable;
import org.adoptopenjdk.jitwatch.treevisitor.TreeVisitor;

public abstract class AbstractCompilationWalker implements ITreeVisitable
{
    protected IReadOnlyJITDataModel model;

	public AbstractCompilationWalker(IReadOnlyJITDataModel model)
	{
		this.model = model;
	}

	public void walkCompilations()
	{
		TreeVisitor.walkTree(model, this);
	}
}