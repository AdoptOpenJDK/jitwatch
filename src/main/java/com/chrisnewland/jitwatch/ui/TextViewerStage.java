/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui;

import java.util.ArrayList;
import java.util.List;

import com.chrisnewland.jitwatch.ui.triview.Viewer;

import javafx.event.EventHandler;
import javafx.scene.text.Text;
import javafx.stage.WindowEvent;

public class TextViewerStage extends AbstractTextViewerStage
{
	// make this a TextFlow in Java8
	public TextViewerStage(final JITWatchUI parent, String title, String source, boolean showLineNumbers)
	{
		super(parent, title);
		
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

		List<Text> textItems = new ArrayList<>();
		
		String style = "-fx-font-family: monospace; -fx-font-size:12px; -fx-fill: " + Viewer.COLOUR_BLACK + ";";
		
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

			Text lineText = new Text(lines[i]);

			lineText.setStyle(style);

			textItems.add(lineText);
		}
		
		setContent(textItems, maxLineLength);
	}
}