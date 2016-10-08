/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.report.cell;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;

import org.adoptopenjdk.jitwatch.ui.report.IReportRowBean;

import javafx.scene.control.TableCell;
import javafx.scene.control.TextArea;

public class TextWrapTableCell extends TableCell<IReportRowBean, String>
{
	private TextArea textArea;

	public TextWrapTableCell()
	{
		textArea = new TextArea();
		textArea.setWrapText(true);
		textArea.setEditable(false);

		setGraphic(textArea);
	}

	@Override
	protected void updateItem(String text, boolean empty)
	{
		if (text != null)
		{
			int rows = text.split(S_NEWLINE).length;

			textArea.setText(text);
			textArea.setPrefHeight(100 + rows * 10);
			textArea.setVisible(true);
		}
		else
		{
			textArea.setVisible(false);
		}
	}
}