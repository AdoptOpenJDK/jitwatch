/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.suggestion;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;

public class ScoreTableCell extends TableCell<SuggestTableRow, Integer>
{
	private Label lblScore;

	public ScoreTableCell()
	{
		lblScore = new Label();
		setAlignment(Pos.TOP_CENTER);

		setGraphic(lblScore);
	}

	@Override
	protected void updateItem(Integer score, boolean empty)
	{
		if (score != null)
		{
			lblScore.setText(score.toString());
		}
	}
}