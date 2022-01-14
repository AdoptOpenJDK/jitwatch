/*
 * Copyright (c) 2013-2022 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.util;

import com.chrisnewland.freelogj.Logger;
import com.chrisnewland.freelogj.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;
import java.nio.file.Path;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

public final class NetUtil
{
	private NetUtil()
	{
	}

	private static final Logger logger = LoggerFactory.getLogger(NetUtil.class);

	public static String fetchURL(String url)
	{
		StringBuilder builder = new StringBuilder();

		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(url).openStream())))
		{
			String inputLine;

			while ((inputLine = bufferedReader.readLine()) != null)
			{
				builder.append(inputLine).append(S_NEWLINE);
			}
		}
		catch (MalformedURLException e)
		{
			logger.error("Bad URL: {}", url, e);
		}
		catch (IOException e)
		{
			logger.error("Could not download {}", url, e);
		}

		return builder.toString();
	}

	public static boolean fetchBinary(String url, Path targetPath)
	{
		try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new URL(url).openStream());
				BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(targetPath.toFile())))
		{
			byte[] data = new byte[1024];

			int read;

			while ((read = bufferedInputStream.read(data)) != -1)
			{
				bufferedOutputStream.write(data, 0, read);
			}
		}
		catch (MalformedURLException e)
		{
			logger.error("Bad URL: {}", url, e);
			return false;
		}
		catch (IOException e)
		{
			logger.error("Could not download {}", url, e);
			return false;
		}

		return true;
	}
}