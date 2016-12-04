/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.launch;

import org.adoptopenjdk.jitwatch.ui.JITWatchUI;
import java.lang.reflect.Field;
import java.util.HashMap;

public final class LaunchUI
{
	private LaunchUI()
	{
	}

	public static void main(String[] args)
	{
		if (isMac()) {
			initMacFont();
		}
		new JITWatchUI(args);
	}

	private static boolean isMac()
	{
		String OS = System.getProperty("os.name").toLowerCase();
		if (OS != null && OS.indexOf("mac") >= 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * to see: https://bugs.openjdk.java.net/browse/JDK-8143907
	 */
	private static void initMacFont()
	{
		try {
			final Class<?> macFontFinderClass = Class.forName("com.sun.t2k.MacFontFinder");
			final Field psNameToPathMap = macFontFinderClass.getDeclaredField("psNameToPathMap");
			psNameToPathMap.setAccessible(true);
			if (psNameToPathMap.get(null) == null) {
				psNameToPathMap.set(null, new HashMap<String, String>());
			}
			final Field allAvailableFontFamilies = macFontFinderClass.getDeclaredField("allAvailableFontFamilies");
			allAvailableFontFamilies.setAccessible(true);
			if (allAvailableFontFamilies.get(null) == null) {
				allAvailableFontFamilies.set(null, new String[] {});
			}
		} catch (final Exception e) {
			// ignore
		}
	}
}