/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.loader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DisposableURLClassLoader extends URLClassLoader
{
	private List<URL> urlList = new ArrayList<>();
	
	public DisposableURLClassLoader(final List<URL> urls)
	{
		super(urls.toArray(new URL[urls.size()]));
		
		urlList.addAll(urls);
	}
	
	public List<URL> getURLListCopy()
	{
		return Collections.unmodifiableList(urlList);
	}
}