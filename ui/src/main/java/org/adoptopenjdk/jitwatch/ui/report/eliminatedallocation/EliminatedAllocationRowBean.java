/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.report.eliminatedallocation;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;

import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.report.Report;
import org.adoptopenjdk.jitwatch.report.ReportType;
import org.adoptopenjdk.jitwatch.ui.report.AbstractReportRowBean;

public class EliminatedAllocationRowBean extends AbstractReportRowBean
{
	public EliminatedAllocationRowBean(Report report)
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
	
	public String getEliminatedType()
	{
		String type = null;
		
		Object metaData = report.getMetaData();
		
		if (metaData instanceof MetaClass)
		{
			type = ((MetaClass)metaData).getFullyQualifiedName();
		}
		else if (metaData instanceof String)
		{
			type = (String)metaData;
		}		
		else
		{
			type = "Unknown";
		}
		
		return type;
	}
}