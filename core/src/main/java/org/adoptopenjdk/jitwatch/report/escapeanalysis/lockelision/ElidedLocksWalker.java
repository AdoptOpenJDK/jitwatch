/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.report.escapeanalysis.lockelision;

import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.bytecode.BCAnnotationType;
import org.adoptopenjdk.jitwatch.model.bytecode.LineAnnotation;
import org.adoptopenjdk.jitwatch.report.escapeanalysis.AbstractEscapeAnalysisWalker;

public class ElidedLocksWalker extends AbstractEscapeAnalysisWalker
{
	public ElidedLocksWalker(IReadOnlyJITDataModel model)
	{
		super(model);
	}

	@Override
	protected boolean filterLineAnnotation(LineAnnotation la)
	{
		return la.getType() == BCAnnotationType.LOCK_ELISION;
	}
}