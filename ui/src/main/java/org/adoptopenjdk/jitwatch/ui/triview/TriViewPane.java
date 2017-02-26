/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.triview;

import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TriViewPane extends VBox
{
	private boolean isFocussed = false;
	private HBox titleComponents;

	private static final String STYLE_UNFOCUSSED = "-fx-background-color:#dddddd; -fx-padding:4px;";
	private static final String STYLE_FOCUSSED = "-fx-background-color:#ffffaa; -fx-padding:4px;";
	
	private Viewer viewer;
	
	public TriViewPane(TriView parent, String title, Viewer viewer)
	{
		construct(parent, title, viewer, new HBox());
	}

	public TriViewPane(TriView parent, String title, Viewer viewer, HBox titleComponents)
	{
		construct(parent, title, viewer, titleComponents);
	}
	
	public void clear()
	{
		viewer.clear();
	}

	private void construct(final TriView parent, String title, final Viewer viewer, HBox titleComponents)
	{
		this.titleComponents = titleComponents;
		this.viewer = viewer;

		titleComponents.setStyle(STYLE_UNFOCUSSED);
		titleComponents.prefWidthProperty().bind(widthProperty());
		titleComponents.setPrefHeight(60);

		Label lblTitle = new Label(title);
		titleComponents.getChildren().add(0, lblTitle);

		viewer.prefWidthProperty().bind(widthProperty());
		viewer.prefHeightProperty().bind(heightProperty());

		getChildren().add(titleComponents);
		getChildren().add(viewer);

		titleComponents.setOnMouseEntered(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent arg0)
			{
				if (viewer.getConfig().isTriViewMouseFollow())
				{
					parent.handleFocusSelf(viewer.getLineType());
				}
			}
		});

		titleComponents.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent arg0)
			{
				parent.handleFocusSelf(viewer.getLineType());
			}
		});
	}

	public void focus()
	{
		if (!isFocussed)
		{
			titleComponents.setStyle(STYLE_FOCUSSED);
			isFocussed = true;
		}
	}

	public void unFocus()
	{
		if (isFocussed)
		{
			titleComponents.setStyle(STYLE_UNFOCUSSED);
			isFocussed = false;
		}
	}
}
