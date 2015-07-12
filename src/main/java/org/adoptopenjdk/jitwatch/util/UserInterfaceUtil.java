/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.util;

import java.io.InputStream;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UserInterfaceUtil
{
	private static final Logger logger = LoggerFactory.getLogger(UserInterfaceUtil.class);

	// https://www.iconfinder.com/icons/173960/tick_icon#size=16
	public static final Image IMAGE_TICK;

	public static final String FONT_MONOSPACE_FAMILY;
	public static final String FONT_MONOSPACE_SIZE;

	private UserInterfaceUtil()
	{
	}

	static
	{
		IMAGE_TICK = loadResource("/images/tick.png");
		
		FONT_MONOSPACE_FAMILY = System.getProperty("monospaceFontFamily", "monospace");
		FONT_MONOSPACE_SIZE = System.getProperty("monospaceFontSize", "12");
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
	
	public static Scene getScene(Parent parent, double width, double height)
	{
		Scene scene = new Scene(parent, width, height);
		
		String styleSheet = UserInterfaceUtil.class.getResource("/style.css").toExternalForm();
		
		scene.getStylesheets().add(styleSheet);
		
		return scene;
	}
}