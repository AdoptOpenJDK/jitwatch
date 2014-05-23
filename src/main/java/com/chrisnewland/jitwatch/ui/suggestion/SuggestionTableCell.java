/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui.suggestion;

import javafx.scene.control.TableCell;
import javafx.scene.control.TextArea;

class SuggestionTableCell extends TableCell<SuggestTableRow, String>
{
    private static final int SEVENTY_ROWS_FROM_THE_TOP = 70;
    private static final int TEN_TIMES = 10;
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
			int rows = suggestion.split("\n").length;

			textArea.setText(suggestion);
			textArea.setPrefHeight(SEVENTY_ROWS_FROM_THE_TOP + rows * TEN_TIMES);
		}
	}
}