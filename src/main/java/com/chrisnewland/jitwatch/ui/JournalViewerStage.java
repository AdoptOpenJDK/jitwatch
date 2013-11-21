/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/blob/master/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
package com.chrisnewland.jitwatch.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.chrisnewland.jitwatch.core.JITWatchConstants;
import com.chrisnewland.jitwatch.model.Journal;
import com.chrisnewland.jitwatch.model.Tag;
import com.chrisnewland.jitwatch.model.Task;
import com.chrisnewland.jitwatch.util.StringUtil;

import javafx.event.EventHandler;
import javafx.scene.text.Text;
import javafx.stage.WindowEvent;

public class JournalViewerStage extends AbstractTextViewerStage
{
	// make this a TextFlow in Java8
	public JournalViewerStage(final JITWatchUI parent, String title, Journal journal)
	{
		super(parent, title);
		
		setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				parent.handleStageClosed(JournalViewerStage.this);
			}
		});

		int maxLineLength = 0;

		List<Text> textItems = new ArrayList<>();

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

				String style = "-fx-font-family: monospace; -fx-font-size:12px; -fx-fill:";

				String colour = COLOUR_BLACK;

				if (tagLines[i].contains("<" + JITWatchConstants.TAG_INLINE_FAIL))
				{
					colour = COLOUR_RED;
				}
				else if (tagLines[i].contains("<" + JITWatchConstants.TAG_INLINE_SUCCESS))
				{
					colour = COLOUR_GREEN;
				}
				else if (tagLines[i].contains("<" + JITWatchConstants.TAG_INTRINSIC))
				{
					colour = COLOUR_BLUE;
				}
				else if (tagLines[i].contains("<" + JITWatchConstants.TAG_PARSE + " "))
				{
					Map<String, String> attrs = StringUtil.getLineAttributesDoubleQuote(tagLines[i]
							.substring(1 + JITWatchConstants.TAG_PARSE.length()));

					String method = attrs.get(JITWatchConstants.ATTR_METHOD);

					if (tag instanceof Task)
					{
						tagLines[i] += ((Task)tag).decodeParseMethod(method);
					}
				}

				Text lineText = new Text(tagLines[i]);

				lineText.setStyle(style + colour + ";");

				textItems.add(lineText);
			}
		}

		setContent(textItems, maxLineLength);
	}
}