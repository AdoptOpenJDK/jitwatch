/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.ui.triview.Viewer;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.util.List;

public abstract class AbstractTextViewerStage extends Stage
{
    private static final int SCENE_WIDTH = 640;
    private static final int SCENE_HEIGHT = 480;

    private static final int DEFAULT_LINE_LENGTH = 80;
    private static final int DEFAULT_NUMBER_OF_ITEMS = 30;
    private static final int DEFAULT_X_POSITION = 20;
    private static final int DEFAULT_Y_POSITION = 20;
    private static final int BY_TWELVE = 12;
    private static final int BY_NINETEEN = 19;

    private Viewer viewer;

	public AbstractTextViewerStage(final JITWatchUI parent, String title, boolean highlighting)
	{
		initStyle(StageStyle.DECORATED);

		setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				parent.handleStageClosed(AbstractTextViewerStage.this);
			}
		});

		viewer = new Viewer(parent, highlighting);

		setTitle(title);

		Scene scene = new Scene(viewer, SCENE_WIDTH, SCENE_HEIGHT);

		setScene(scene);
	}

	protected void setContent(List<Label> items, int maxLineLength)
	{
		viewer.setContent(items);

		int x = Math.min(DEFAULT_LINE_LENGTH, maxLineLength);
		int y = Math.min(DEFAULT_NUMBER_OF_ITEMS, items.size());

		x = Math.max(x, DEFAULT_X_POSITION);
		y = Math.max(y, DEFAULT_Y_POSITION);

		setWidth(x * BY_TWELVE);
		setHeight(y * BY_NINETEEN);
	}

	public void jumpTo(IMetaMember member)
	{
		viewer.jumpTo(member);
	}
}