/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.report.suggestion;

import org.adoptopenjdk.jitwatch.report.Report;
import org.adoptopenjdk.jitwatch.ui.report.AbstractReportRowBean;

public class SuggestionRowBean extends AbstractReportRowBean
{
	public SuggestionRowBean(Report report)
	{
		super(report);
	}

	public int getScore()
	{
		return report.getScore();
	}

	public String getType()
	{
		switch (report.getType())
		{
		case BRANCH:
			return "Branch";
		case INLINING:
			return "Inlinling";
		case CODE_CACHE:
			return "Code Cache";
		case ELIMINATED_ALLOCATION:
			return "Eliminated Allocation";
		default:
			return "Unknown";
		}
	}
}