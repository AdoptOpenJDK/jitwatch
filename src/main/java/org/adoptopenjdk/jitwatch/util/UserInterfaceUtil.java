/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.util;

import java.io.InputStream;

import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UserInterfaceUtil
{
	private static final Logger logger = LoggerFactory.getLogger(UserInterfaceUtil.class);

	// https://www.iconfinder.com/icons/173960/tick_icon#size=16
	public static final Image IMAGE_TICK;

	private UserInterfaceUtil()
	{
	}

	static
	{
		IMAGE_TICK = loadResource("/images/tick.png");
	}

	private static Image loadResource(String path)
	{
		InputStream inputStream = UserInterfaceUtil.class.getResourceAsStream(path);

		Image result = null;

		if (inputStream != null)
		{
			result = new Image(inputStream);
		}
		else
		{
			logger.error("Could not load resource {}. If running in an IDE please add src/main/resources to your classpath", path);
		}

		return result;
	}
}
