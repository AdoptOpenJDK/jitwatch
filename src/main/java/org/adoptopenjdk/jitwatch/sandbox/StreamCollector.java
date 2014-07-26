package org.adoptopenjdk.jitwatch.sandbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.NEW_LINEFEED;

public class StreamCollector
{
	private StringBuilder builder = new StringBuilder();

	private static final Logger logger = LoggerFactory.getLogger(StreamCollector.class);

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
						builder.append(line).append(NEW_LINEFEED);
					}
				}
				catch (Exception e)
				{
					logger.error("Error collecting process output stream", e);
				}
			}
		}).start();
	}

	public String getStreamString()
	{
		return builder.toString();
	}
}