/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ParsedClasspath
{
	private Set<String> classLocationSet = new HashSet<>();
	
	public void clear()
	{
		classLocationSet.clear();
	}
	
	public void addClassLocation(String location)
	{
		classLocationSet.add(location);
	}
	
	public List<String> getClassLocations()
	{
		List<String> asList = new ArrayList<String>(classLocationSet);
		
		Collections.sort(asList);
		
		return Collections.unmodifiableList(asList);
	}
}
