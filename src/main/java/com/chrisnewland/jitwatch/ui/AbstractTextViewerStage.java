/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui;

import java.util.List;
import java.util.Map;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.LineAnnotation;
import com.chrisnewland.jitwatch.ui.triview.Viewer;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public abstract class AbstractTextViewerStage extends Stage
{
	private Viewer viewer;

	// make this a TextFlow in Java8
	public AbstractTextViewerStage(final JITWatchUI parent, String title)
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

		viewer = new Viewer();

		setTitle(title);

		Scene scene = new Scene(viewer, 640, 480);

		setScene(scene);
	}

	public void setLineAnnotations(Map<Integer, LineAnnotation> annotationMap)
	{
		viewer.setLineAnnotations(annotationMap);
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

	protected String padLineNumber(int number, int maxWidth)
	{
		int len = Integer.toString(number).length();

		StringBuilder builder = new StringBuilder();

		for (int i = len; i < maxWidth; i++)
		{
			builder.append(' ');
		}

		builder.append(number);

		return builder.toString();
	}

	public void jumpTo(IMetaMember member)
	{
		viewer.jumpTo(member);
	}
}