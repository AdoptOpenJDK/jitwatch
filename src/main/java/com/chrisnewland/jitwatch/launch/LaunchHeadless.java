/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.launch;

import com.chrisnewland.jitwatch.core.HotSpotLogParser;
import com.chrisnewland.jitwatch.core.IJITListener;
import com.chrisnewland.jitwatch.core.JITWatchConfig;
import com.chrisnewland.jitwatch.model.JITDataModel;
import com.chrisnewland.jitwatch.model.JITEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class LaunchHeadless implements IJITListener
{
    private boolean showErrors;
    private static final Logger logger = LoggerFactory.getLogger(LaunchHeadless.class);

    public LaunchHeadless(String filename, boolean showErrors) throws IOException
    {
        this.showErrors = showErrors;

        JITDataModel model = new JITDataModel();

        JITWatchConfig config = new JITWatchConfig(this);

        HotSpotLogParser parser = new HotSpotLogParser(model, config, this);

        parser.readLogFile(new File(filename));
    }

    @Override
    public void handleLogEntry(String entry)
    {
        logger.error(entry);
    }

    @Override
    public void handleErrorEntry(String entry)
    {
        if (showErrors)
        {
            logger.error(entry);
        }
    }

    @Override
    public void handleJITEvent(JITEvent event)
    {
        logger.info(event.toString());
    }

    public static void main(String[] args) throws IOException
    {
        if (args.length < 1)
        {
            logger.error("Usage: LaunchHeadless <hotspot log file> [logErrors (true|false)]");
            System.exit(-1);
        }

		final boolean showErrors = twoParametersArePassedIn(args) && firstParameterIsABooleanExpression(args[1]);

		new LaunchHeadless(args[0], showErrors);
	}

    private static boolean firstParameterIsABooleanExpression(String arg) {
        return Boolean.valueOf(arg) == true;
    }

    private static boolean twoParametersArePassedIn(String[] args) {
        return args.length == 2;
    }

	@Override
	public void handleReadComplete()
	{
		logger.info("Finished reading log file.");		
	}
}