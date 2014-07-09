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