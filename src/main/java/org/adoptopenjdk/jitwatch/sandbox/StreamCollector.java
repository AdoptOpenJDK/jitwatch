package org.adoptopenjdk.jitwatch.sandbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

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
						builder.append(line).append(S_NEWLINE);
					}
				}
				catch (Exception e)
				{
					logger.error("Error collecting process output stream", e);
				}
				finally
				{
					try
					{
						reader.close();
					}
					catch(IOException ioe)
					{
					}
				}
			}
		}).start();
	}

	public String getStreamString()
	{
		return builder.toString();
	}
}