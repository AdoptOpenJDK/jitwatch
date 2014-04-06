/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui;

import java.util.ArrayList;
import java.util.List;

import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.stage.WindowEvent;

public class TextViewerStage extends AbstractTextViewerStage
{
	// make this a TextFlow in Java8
	public TextViewerStage(final JITWatchUI parent, String title, String inSource, boolean showLineNumbers)
	{
		super(parent, title);

        String source = inSource;
		setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				parent.handleStageClosed(TextViewerStage.this);
			}
		});

		if (source == null)
		{
			source = "Empty";
		}

		source = source.replace("\t", "    "); // 4 spaces

		String[] lines = source.split("\n");

		int maxLineLength = 0;

		int maxWidth = Integer.toString(lines.length).length();

		List<Label> labels = new ArrayList<>();
				
		for (int i = 0; i < lines.length; i++)
		{
			String row = lines[i];

			if (showLineNumbers)
			{
				lines[i] = padLineNumber(i + 1, maxWidth) + "  " + row;
			}

			int rowLen = row.length();

			if (rowLen > maxLineLength)
			{
				maxLineLength = rowLen;
			}

			Label lblLine = new Label(lines[i]);

			labels.add(lblLine);
		}
		
		setContent(labels, maxLineLength);
	}
}