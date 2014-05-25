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

		int x = Math.min(80, maxLineLength);
		int y = Math.min(30, items.size());

		x = Math.max(x, 20);
		y = Math.max(y, 20);

		setWidth(x * 12);
		setHeight(y * 19);
	}

	public void jumpTo(IMetaMember member)
	{
		viewer.jumpTo(member);
	}
}