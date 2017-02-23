/*
 * Copyright (c) 2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.report;

public enum ReportType
{
	BRANCH, INLINE_SUCCESS, INLINE_FAILURE, CODE_CACHE, HOT_THROW, ELIMINATED_ALLOCATION_DIRECT, ELIMINATED_ALLOCATION_INLINE
}