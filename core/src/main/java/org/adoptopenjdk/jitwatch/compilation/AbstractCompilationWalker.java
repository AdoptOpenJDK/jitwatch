/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.compilation;

import org.adoptopenjdk.jitwatch.model.IParseDictionary;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.treevisitor.ITreeVisitable;
import org.adoptopenjdk.jitwatch.treevisitor.TreeVisitor;

public abstract class AbstractCompilationWalker extends AbstractCompilationVisitable implements ITreeVisitable
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

	@Override
	public void visitTag(Tag toVisit, IParseDictionary parseDictionary) throws LogParseException
	{		
	}
}