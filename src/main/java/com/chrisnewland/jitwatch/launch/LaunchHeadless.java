/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.launch;

import java.io.File;
import java.io.IOException;

import com.chrisnewland.jitwatch.core.IJITListener;
import com.chrisnewland.jitwatch.core.HotSpotLogParser;
import com.chrisnewland.jitwatch.core.JITWatchConfig;
import com.chrisnewland.jitwatch.model.JITDataModel;
import com.chrisnewland.jitwatch.model.JITEvent;

public class LaunchHeadless implements IJITListener
{
	private boolean showErrors;

	public LaunchHeadless(String filename, boolean showErrors) throws IOException
	{
		this.showErrors = showErrors;

		JITDataModel model = new JITDataModel();

		JITWatchConfig config = new JITWatchConfig(this);

		HotSpotLogParser parser = new HotSpotLogParser(model, config, this);

		parser.watch(new File(filename));
	}

	@Override
	public void handleLogEntry(String entry)
	{
		System.out.println(entry);
	}

	@Override
	public void handleErrorEntry(String entry)
	{
		if (showErrors)
		{
			System.err.println(entry);
		}
	}

	@Override
	public void handleJITEvent(JITEvent event)
	{
		System.out.println(event.toString());
	}

	public static void main(String[] args) throws IOException
	{
		if (args.length < 1)
		{
			System.err.println("Usage: LaunchHeadless <hotspot log file> [logErrors (true|false)]");
			System.exit(-1);
		}

		final boolean showErrors = args.length == 2 && Boolean.valueOf(args[1]) == true;

		new LaunchHeadless(args[0], showErrors);
	}
}