/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.suggestion;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextArea;

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
			textArea.setPrefHeight(100 + rows * 10);
			textArea.setVisible(true);
		}
		else
		{
			textArea.setVisible(false);
		}
	}
}