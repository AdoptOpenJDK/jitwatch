/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui;

import java.util.List;

import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.ui.triview.Viewer;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public abstract class AbstractTextViewerStage extends Stage
{
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

		Scene scene = UserInterfaceUtil.getScene(viewer, 640, 480);

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