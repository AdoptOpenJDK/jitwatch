/*
 * Copyright (c) 2016-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.report.escapeanalysis.eliminatedallocation;

import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.bytecode.BCAnnotationType;
import org.adoptopenjdk.jitwatch.model.bytecode.LineAnnotation;
import org.adoptopenjdk.jitwatch.report.escapeanalysis.AbstractEscapeAnalysisWalker;

public class EliminatedAllocationWalker extends AbstractEscapeAnalysisWalker
{
	public EliminatedAllocationWalker(IReadOnlyJITDataModel model)
	{
		super(model);
	}

	@Override
	protected boolean filterLineAnnotation(LineAnnotation la)
	{
		return la.getType() == BCAnnotationType.ELIMINATED_ALLOCATION;
	}
}