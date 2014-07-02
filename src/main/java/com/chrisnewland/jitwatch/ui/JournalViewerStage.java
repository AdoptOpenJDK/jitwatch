/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;
import com.chrisnewland.jitwatch.model.Journal;
import com.chrisnewland.jitwatch.model.Tag;
import com.chrisnewland.jitwatch.model.Task;
import com.chrisnewland.jitwatch.ui.triview.Viewer;
import com.chrisnewland.jitwatch.util.StringUtil;

import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.WindowEvent;

public class JournalViewerStage extends AbstractTextViewerStage
{
	public JournalViewerStage(final JITWatchUI parent, String title, Journal journal)
	{
		super(parent, title, false);

		setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				parent.handleStageClosed(JournalViewerStage.this);
			}
		});

		int maxLineLength = 0;

		List<Label> labels = new ArrayList<>();

		for (Tag tag : journal.getEntryList())
		{
			String[] tagLines = tag.toString().split("\n");

			for (int i = 0; i < tagLines.length; i++)
			{
				String row = tagLines[i];

				int rowLen = row.length();

				if (rowLen > maxLineLength)
				{
					maxLineLength = rowLen;
				}

				String style = "-fx-font-family: monospace; -fx-font-size:12px;";

				String colour = Viewer.COLOUR_BLACK;

				if (tagLines[i].contains(S_OPEN_ANGLE + TAG_INLINE_FAIL))
				{
					colour = Viewer.COLOUR_RED;
				}
				else if (tagLines[i].contains(S_OPEN_ANGLE + TAG_INLINE_SUCCESS))
				{
					colour = Viewer.COLOUR_GREEN;
				}
				else if (tagLines[i].contains(S_OPEN_ANGLE + TAG_INTRINSIC))
				{
					colour = Viewer.COLOUR_BLUE;
				}
				else if (tagLines[i].contains(S_OPEN_ANGLE + TAG_PARSE + C_SPACE))
				{
					Map<String, String> attrs = StringUtil.getLineAttributesDoubleQuote(tagLines[i].substring(1 + TAG_PARSE
							.length()));

					String method = attrs.get(ATTR_METHOD);

					if (tag instanceof Task)
					{
						tagLines[i] += ((Task) tag).decodeParseMethod(method);
					}
				}

				Label lblLine = new Label(tagLines[i]);

				lblLine.setStyle(style);
				lblLine.setTextFill(Color.web(colour));

				labels.add(lblLine);
			}
		}

		setContent(labels, maxLineLength);
	}
}