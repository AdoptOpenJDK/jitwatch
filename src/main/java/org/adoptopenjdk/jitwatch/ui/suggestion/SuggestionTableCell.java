/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.suggestion;

import javafx.scene.control.TableCell;
import javafx.scene.control.TextArea;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

class SuggestionTableCell extends TableCell<SuggestTableRow, String>
{
	private TextArea textArea;

	public SuggestionTableCell()
	{
		textArea = new TextArea();
		textArea.setWrapText(true);
		textArea.setEditable(false);

		setGraphic(textArea);
	}

	@Override
	protected void updateItem(String suggestion, boolean empty)
	{
		if (suggestion != null)
		{
			int rows = suggestion.split(S_NEWLINE).length;

			textArea.setText(suggestion);
			textArea.setPrefHeight(70 + rows * 10);
		}
	}
}