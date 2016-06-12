/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.util;

import java.io.InputStream;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import org.adoptopenjdk.jitwatch.model.bytecode.BCAnnotationType;
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

		FONT_MONOSPACE_FAMILY = System.getProperty("monospaceFontFamily", Font.font(java.awt.Font.MONOSPACED, 12).getName());
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

	public static Color getColourForBytecodeAnnotation(BCAnnotationType type)
	{
		switch (type)
		{
		case BRANCH:
			return Color.BLUE;
		case ELIMINATED_ALLOCATION:
			return Color.GRAY;
		case INLINE_FAIL:
			return Color.RED;
		case INLINE_SUCCESS:
			return Color.GREEN;
		case UNCOMMON_TRAP:
			return Color.PURPLE;
		default:
			return Color.BLACK;
		}
	}
}