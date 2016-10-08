/*
 * Copyright (c) 2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.report;

import org.adoptopenjdk.jitwatch.report.Report;

public abstract class AbstractReportRowBean implements IReportRowBean
{
	protected final Report report;

	public AbstractReportRowBean(Report report)
	{
		this.report = report;
	}
	
	public Report getReport()
	{
		return report;
	}

	public String getText()
	{
		return report.getText();
	}
	
	public int getBytecodeOffset()
	{
		return report.getBytecodeOffset();
	}
}