/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.viewer;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_OPEN_ANGLE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INLINE_FAIL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INLINE_SUCCESS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_INTRINSIC;
import static org.adoptopenjdk.jitwatch.util.UserInterfaceUtil.FONT_MONOSPACE_FAMILY;
import static org.adoptopenjdk.jitwatch.util.UserInterfaceUtil.FONT_MONOSPACE_SIZE;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.ui.main.JITWatchUI;
import org.adoptopenjdk.jitwatch.ui.triview.Viewer;

public class JournalViewerStage extends AbstractTextViewerStage
{
	public JournalViewerStage(final JITWatchUI parent, String title, Compilation compilation)
	{
		super(parent, title, false);

		int maxLineLength = 0;

		List<Label> labels = new ArrayList<>();
		
			String[] tagLines = compilation.toStringVerbose().split(S_NEWLINE);

			for (int i = 0; i < tagLines.length; i++)
			{
				String row = tagLines[i];

				int rowLen = row.length();

				if (rowLen > maxLineLength)
				{
					maxLineLength = rowLen;
				}

				String style = "-fx-font-family:" + FONT_MONOSPACE_FAMILY + "; -fx-font-size:" + FONT_MONOSPACE_SIZE + "px;";

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
//				else if (tagLines[i].contains(S_OPEN_ANGLE + TAG_PARSE + C_SPACE))
//				{
//					Map<String, String> attrs = StringUtil.getLineAttributesDoubleQuote(tagLines[i].substring(1 + TAG_PARSE
//							.length()));
//
//					String method = attrs.get(ATTR_METHOD);
//
//					if (tag instanceof Task)
//					{
//						tagLines[i] += ((Task) tag).decodeParseMethod(method);
//					}
//				}

				Label lblLine = new Label(tagLines[i]);

				lblLine.setStyle(style);
				lblLine.setTextFill(Color.web(colour));

				labels.add(lblLine);
			}

			Label lblLine = new Label(S_EMPTY);
			labels.add(lblLine);
		
		setContent(labels, maxLineLength);
	}
}