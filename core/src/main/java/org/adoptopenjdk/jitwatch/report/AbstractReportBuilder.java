/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.adoptopenjdk.jitwatch.compilation.AbstractCompilationVisitable;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.treevisitor.ITreeVisitable;
import org.adoptopenjdk.jitwatch.treevisitor.TreeVisitor;

public abstract class AbstractReportBuilder extends AbstractCompilationVisitable implements ITreeVisitable
{
    protected IReadOnlyJITDataModel model;
    protected List<Report> reportList;

	public AbstractReportBuilder(IReadOnlyJITDataModel model)
	{
		this.model = model;
		reportList = new ArrayList<>();
	}

	public List<Report> getReports(Comparator<Report> comparator)
	{
		TreeVisitor.walkTree(model, this);
		
		findNonMemberReports();

		Collections.sort(reportList, comparator);

		return reportList;
	}
	
	protected abstract void findNonMemberReports();

	@Override
	public void reset()
	{
		reportList.clear();
	}
}