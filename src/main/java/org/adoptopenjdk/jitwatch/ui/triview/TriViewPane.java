/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.triview;

import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class TriViewPane extends VBox
{
	private Label lblTitle;
	private boolean isFocussed = false;

	private static final String STYLE_UNFOCUSSED = "-fx-background-color:#dddddd; -fx-padding:4px;";
	private static final String STYLE_FOCUSSED = "-fx-background-color:#ffffaa; -fx-padding:4px;";

	public TriViewPane(String title, Viewer viewer)
	{
		lblTitle = new Label(title);

		lblTitle.setStyle(STYLE_UNFOCUSSED);
		lblTitle.prefWidthProperty().bind(widthProperty());

		viewer.prefWidthProperty().bind(widthProperty());
		viewer.prefHeightProperty().bind(heightProperty());

		getChildren().add(lblTitle);
		getChildren().add(viewer);

		lblTitle.setOnMouseEntered(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent arg0)
			{
				focus();
			}
		});

		lblTitle.setOnMouseExited(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent arg0)
			{
				unFocus();
			}
		});

	}

	public void focus()
	{
		if (!isFocussed)
		{
			lblTitle.setStyle(STYLE_FOCUSSED);
			isFocussed = true;
		}
	}

	public void unFocus()
	{
		if (isFocussed)
		{
			lblTitle.setStyle(STYLE_UNFOCUSSED);
			isFocussed = false;
		}
	}
}
