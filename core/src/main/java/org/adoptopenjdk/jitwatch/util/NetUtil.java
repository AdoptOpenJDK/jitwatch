/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

public final class NetUtil
{
	private NetUtil()
	{
	}

	private static final Logger logger = LoggerFactory.getLogger(NetUtil.class);

	public static String fetchURL(String toFetch)
	{
		StringBuilder builder = new StringBuilder();

		BufferedReader in = null;

		try
		{
			URL url = new URL(toFetch);

			in = new BufferedReader(new InputStreamReader(url.openStream()));

			String inputLine;

			while ((inputLine = in.readLine()) != null)
			{
				builder.append(inputLine).append(S_NEWLINE);
			}

		}
		catch (MalformedURLException e)
		{
			logger.error("", e);
		}
		catch (IOException e)
		{
			logger.error("", e);
		}
		finally
		{
			if (in != null)
			{
				try
				{
					in.close();
				}
				catch (IOException ioe)
				{
				}
			}
		}

		return builder.toString();
	}
}
