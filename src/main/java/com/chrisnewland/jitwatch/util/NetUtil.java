package com.chrisnewland.jitwatch.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public final class NetUtil
{
    /*
        Hide Utility Class Constructor
        Utility classes should not have a public or default constructor.
    */
    private NetUtil() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(NetUtil.class);

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
				builder.append(inputLine).append("\n");
			}

		} catch (MalformedURLException e) {
            LOGGER.error("", e);
        } catch (IOException e) {
            LOGGER.error("", e);
        } finally
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
