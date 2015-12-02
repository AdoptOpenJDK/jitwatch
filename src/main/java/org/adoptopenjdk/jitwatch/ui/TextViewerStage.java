/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui;

import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.util.StringUtil;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.stage.WindowEvent;

public class TextViewerStage extends AbstractTextViewerStage
{
	public TextViewerStage(final JITWatchUI parent, String title, String inSource, boolean showLineNumbers, boolean highlighting)
	{
		super(parent, title, highlighting);

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

		String[] lines = source.split(S_NEWLINE);

		int maxLineLength = 0;

		int maxWidth = Integer.toString(lines.length).length();

		List<Label> labels = new ArrayList<>();
				
		for (int i = 0; i < lines.length; i++)
		{
			String row = lines[i];

			if (showLineNumbers)
			{
				lines[i] = StringUtil.alignRight(i + 1, maxWidth) + S_DOUBLE_SPACE + row;
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