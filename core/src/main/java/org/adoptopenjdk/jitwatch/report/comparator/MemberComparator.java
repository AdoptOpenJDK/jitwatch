/*
 * Copyright (c) 2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.report.comparator;

import java.util.Comparator;

import org.adoptopenjdk.jitwatch.report.Report;

public class MemberComparator implements Comparator<Report>
{
	@Override
	public int compare(Report o1, Report o2)
	{
		return o2.getCaller().getFullyQualifiedMemberName().compareTo(o1.getCaller().getFullyQualifiedMemberName());
	}
}