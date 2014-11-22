/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui;

import javafx.scene.control.Button;

public class StyleUtil
{
	public static Button buildButton(String title)
	{
		Button button = new Button(title);
		button.setStyle("-fx-font: 13 arial; -fx-base: #eeeeff; -fx-padding: 3 9 4 9");

		return button;
	}
}