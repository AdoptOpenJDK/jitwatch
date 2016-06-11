/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.suggestion;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;

class TypeTableCell extends TableCell<SuggestTableRow, String>
{
	private Label lblType;

	public TypeTableCell()
	{
		lblType = new Label();
		setAlignment(Pos.TOP_CENTER);

		setGraphic(lblType);
	}

	@Override
	protected void updateItem(String type, boolean empty)
	{		
		if (type != null)
		{
			lblType.setText(type);
		}
	}
}