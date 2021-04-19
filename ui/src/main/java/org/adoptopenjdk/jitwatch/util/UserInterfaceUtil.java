/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.util;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.scene.control.Tooltip;
import org.adoptopenjdk.jitwatch.model.bytecode.BCAnnotationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public final class UserInterfaceUtil
{
	private static final Logger logger = LoggerFactory.getLogger(UserInterfaceUtil.class);

	public static final ResourceBundle LANG = ResourceBundle.getBundle("i18n.lang", Locale.getDefault());
	
	// https://www.iconfinder.com/icons/173960/tick_icon#size=16
	public static final Image IMAGE_TICK;

	public static final Image IMAGE_CAMERA;

	public static final String FONT_MONOSPACE_FAMILY;
	public static final String FONT_MONOSPACE_SIZE;
	public static final boolean ADD_CLOSE_DECORATION; // for fullscreen JavaFX
	// systems

	private UserInterfaceUtil()
	{
	}

	static
	{
		IMAGE_TICK = loadResource("/images/tick.png");
		IMAGE_CAMERA = loadResource("/images/camera.png");

		FONT_MONOSPACE_FAMILY = System.getProperty("monospaceFontFamily", Font.font(java.awt.Font.MONOSPACED, 12).getName());
		FONT_MONOSPACE_SIZE = System.getProperty("monospaceFontSize", "12");
		ADD_CLOSE_DECORATION = Boolean.getBoolean("addCloseDecoration");
	}

	public static Button createButton(String langKey)
	{
		Button button = new Button(LANG.getString(langKey));

		String tooltipKey = langKey + "_tt";

		if (LANG.containsKey(tooltipKey))
		{
			String toolTip = LANG.getString(tooltipKey);
			button.setTooltip(new Tooltip(toolTip));
		}

		return button;
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

	public static Button getSnapshotButton(final Scene scene, final String filenamePrefix)
	{
		Button buttonSnapShot = new Button();

		Image image = UserInterfaceUtil.IMAGE_CAMERA;

		buttonSnapShot.setGraphic(new ImageView(image));

		buttonSnapShot.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override public void handle(ActionEvent e)
			{
				takeSnapShot(scene, filenamePrefix);
			}
		});

		return buttonSnapShot;
	}

	private static void takeSnapShot(Scene scene, String filenamePrefix)
	{
		WritableImage imageSnap = new WritableImage((int) scene.getWidth(), (int) scene.getHeight());

		scene.snapshot(imageSnap);

		SimpleDateFormat sfd = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");

		try
		{
			String snapshotFilename = filenamePrefix + "-snapshot-" + sfd.format(new Date()) + ".png";

			Class<?> classImageIO = Class.forName("javax.imageio.ImageIO");

			Class<?> classSwingFXUtils = Class.forName("javafx.embed.swing.SwingFXUtils");

			Method methodWrite = classImageIO.getMethod("write",
					new Class[] { java.awt.image.RenderedImage.class, String.class, File.class });

			Method methodFromFXImage = classSwingFXUtils.getMethod("fromFXImage",
					new Class[] { javafx.scene.image.Image.class, java.awt.image.BufferedImage.class });

			methodWrite.invoke(null, new Object[] { methodFromFXImage.invoke(null, new Object[] { imageSnap, null }), "png",
					new File(snapshotFilename) });
		}
		catch (Throwable t)
		{
			logger.error("Could not create snapshot", t);
		}
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
				psNameToPathMap.set(null, new HashMap<>());
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
