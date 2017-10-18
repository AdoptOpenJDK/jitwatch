/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.report.locks;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;

import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.report.Report;
import org.adoptopenjdk.jitwatch.report.ReportType;
import org.adoptopenjdk.jitwatch.ui.report.AbstractReportRowBean;

public class OptimisedLockRowBean extends AbstractReportRowBean
{
	public OptimisedLockRowBean(Report report)
	{
		super(report);
	}

	public String getCompilation()
	{
		Compilation compilation = report.getCaller().getCompilation(report.getCompilationIndex());

		return compilation != null ? compilation.getSignature() : S_EMPTY;
	}

	public String getMetaClass()
	{
		return report.getCaller().getMetaClass().getFullyQualifiedName();
	}

	public String getMember()
	{
		return report.getCaller().toStringUnqualifiedMethodName(false, false);
	}

	public String getKind()
	{
		return (report.getType() == ReportType.ELIMINATED_ALLOCATION_DIRECT) ? "Direct" : "Inline";
	}

	public String getOptimisationKind()
	{
		String kind = null;

		Object metaData = report.getMetaData();

		if (metaData instanceof String && metaData != null)
		{
			kind = explain((String) metaData);
		}
		else
		{
			kind = "Unknown";
		}

		return kind;
	}

	private String explain(String kind)
	{
		switch (kind)
		{
		case "non_escaping":
			return "Elided lock on non-escaping object";
		case "coarsened":
			return "Lock was coarsened";
		default:
			return kind;
		}
	}
}