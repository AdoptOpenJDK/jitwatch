package com.chrisnewland.jitwatch.sandbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamCollector
{
    private static final Logger logger = LoggerFactory.getLogger(StreamCollector.class);
    private StringBuilder builder = new StringBuilder();

	public StreamCollector(InputStream stream)
	{
		final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					String line = reader.readLine();

					while (line != null)
					{
						line = reader.readLine();
						builder.append(line).append("\n");
					}
				}
				catch (Exception e)
				{
					logger.error("", e);
				}
			}
		}).start();
	}

	public String getStreamString()
	{
		return builder.toString();
	}
}