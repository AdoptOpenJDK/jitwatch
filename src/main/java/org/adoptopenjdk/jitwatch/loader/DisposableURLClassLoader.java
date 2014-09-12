/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.loader;

import java.net.URL;
import java.net.URLClassLoader;

public class DisposableURLClassLoader extends URLClassLoader
{
	public DisposableURLClassLoader(URL[] urls)
	{
		super(urls);
	}
}