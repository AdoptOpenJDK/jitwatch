/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.report.inlining;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;

import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.report.Report;
import org.adoptopenjdk.jitwatch.report.ReportType;
import org.adoptopenjdk.jitwatch.ui.report.AbstractReportRowBean;

public class InliningRowBean extends AbstractReportRowBean
{
	public InliningRowBean(Report report)
	{
		super(report);
	}

	public String getCompilation()
	{
		return report.getCompileNode().getRootCompilationSignature();
	}

	public String getMember()
	{
		return report.getCompileNode().getParent().getMember().getFullyQualifiedMemberNameWithParamTypes();
	}

	public String getSuccess()
	{
		return (report.getType() == ReportType.INLINE_SUCCESS) ? "Yes" : "No";
	}

	public String getReason()
	{
		return report.getText();
	}
}