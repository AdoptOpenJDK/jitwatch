/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.histo;

import org.adoptopenjdk.jitwatch.compilation.AbstractCompilationVisitable;
import org.adoptopenjdk.jitwatch.model.IParseDictionary;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.LogParseException;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.treevisitor.TreeVisitor;

public abstract class AbstractHistoVisitable extends AbstractCompilationVisitable implements IHistoVisitable
{
	protected Histo histo;
	protected IReadOnlyJITDataModel model;
	protected long resolution;

	public AbstractHistoVisitable(IReadOnlyJITDataModel model, long resolution)
	{
		this.model = model;
		this.resolution = resolution;
	}

	@Override
	public Histo buildHistogram()
	{
		histo = new Histo(resolution);

		TreeVisitor.walkTree(model, this);

		return histo;
	}

	@Override
	public void reset()
	{
	}

	@Override
	public void visitTag(Tag toVisit, IParseDictionary parseDictionary) throws LogParseException
	{
	}	
}