package com.chrisnewland.jitwatch.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class NetUtil
{

    /*
        Hide Utility Class Constructor
        Utility classes should not have a public or default constructor.
    */
    private NetUtil() {
    }
    
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

		}
		catch (Exception e)
		{
			//e.printStackTrace();
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
