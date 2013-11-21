/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/blob/master/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
package com.chrisnewland.jitwatch.ui;

import java.util.ArrayList;
import java.util.List;
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

			String style = "-fx-font-family: monospace; -fx-font-size:12px; -fx-fill:";

			String colour = COLOUR_BLACK;

			lineText.setStyle(style + colour + ";");

			textItems.add(lineText);
		}
		
		setContent(textItems, maxLineLength);
	}
}