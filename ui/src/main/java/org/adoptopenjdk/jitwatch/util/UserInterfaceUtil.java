/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.util;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.bytecode.BCAnnotationType;
import org.adoptopenjdk.jitwatch.model.bytecode.ClassBC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public final class UserInterfaceUtil
{
	private static final Logger logger = LoggerFactory.getLogger(UserInterfaceUtil.class);

	// https://www.iconfinder.com/icons/173960/tick_icon#size=16
	public static final Image IMAGE_TICK;

	public static final String FONT_MONOSPACE_FAMILY;
	public static final String FONT_MONOSPACE_SIZE;
	public static final boolean ADD_CLOSE_DECORATION; // for fullscreen JavaFX systems

	private UserInterfaceUtil()
	{
	}

	static
	{
		IMAGE_TICK = loadResource("/images/tick.png");

		FONT_MONOSPACE_FAMILY = System.getProperty("monospaceFontFamily", Font.font(java.awt.Font.MONOSPACED, 12).getName());
		FONT_MONOSPACE_SIZE = System.getProperty("monospaceFontSize", "12");
		ADD_CLOSE_DECORATION = Boolean.getBoolean("addCloseDecoration");
	}

	public static void getBytecodeAndUpdateUI(final IMetaMember member, final IReadOnlyJITDataModel model,
			final List<String> classLocations, final BytecodeReceivingRunnable bcRunnable)
	{
		new Thread(new Runnable()
		{
			public void run()
			{
				ClassBC classBC = member.getMetaClass().getClassBytecode(model, classLocations);

				bcRunnable.setClassBC(classBC);

				Platform.runLater(bcRunnable);
			}
		}).start();

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
			logger.error(
					"Could not load resource {}. If running in an IDE please add [ui,core]/src/main/resources to your classpath",
					path);
		}

		return result;
	}

	public static Scene getScene(Parent rootNode, double width, double height)
	{
		Scene scene = new Scene(rootNode, width, height);

		String styleSheet = UserInterfaceUtil.class.getResource("/style.css").toExternalForm();

		scene.getStylesheets().add(styleSheet);

		return scene;
	}

	public static Color getColourForBytecodeAnnotation(BCAnnotationType type)
	{
		Color colourSuccess = Color.GREEN;
		Color colourFailure = Color.RED;
		Color colourInformation = Color.BLUE;

		switch (type)
		{
		case ELIMINATED_ALLOCATION:
		case INLINE_SUCCESS:
		case HOT_THROW_PREALLOCATED:
			return colourSuccess;
		case INLINE_FAIL:
		case HOT_THROW_NOT_PREALLOCATED:
		case VIRTUAL_CALL:
			return colourFailure;
		case BRANCH:
		case UNCOMMON_TRAP:
		case INTRINSIC_USED:
			return colourInformation;
		default:
			return Color.BLACK;
		}
	}

	// prevent blurry lines in JavaFX
	public static double fix(double pixel)
	{
		return 0.5 + (int) pixel;
	}

	public static void initMacFonts()
	{
		try
		{
			final Class<?> macFontFinderClass = Class.forName("com.sun.t2k.MacFontFinder");

			final Field psNameToPathMap = macFontFinderClass.getDeclaredField("psNameToPathMap");

			psNameToPathMap.setAccessible(true);

			if (psNameToPathMap.get(null) == null)
			{
				psNameToPathMap.set(null, new HashMap<String, String>());
			}

			final Field allAvailableFontFamilies = macFontFinderClass.getDeclaredField("allAvailableFontFamilies");

			allAvailableFontFamilies.setAccessible(true);

			if (allAvailableFontFamilies.get(null) == null)
			{
				allAvailableFontFamilies.set(null, new String[] {});
			}
		}
		catch (Exception e)
		{
			logger.error("Could not initialise Mac fonts", e);
		}
	}
}