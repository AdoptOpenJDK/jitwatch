/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.suggestion;

import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import org.adoptopenjdk.jitwatch.suggestion.Suggestion;

public final class SuggestionTableUtil
{
	private SuggestionTableUtil()
	{
	}

	public static TableView<SuggestTableRow> buildTableSuggestion(ObservableList<SuggestTableRow> rows)
	{
		TableView<SuggestTableRow> tv = new TableView<>();

		TableColumn<SuggestTableRow, Integer> score = new TableColumn<SuggestTableRow, Integer>("Score");
		score.setCellValueFactory(new PropertyValueFactory<SuggestTableRow, Integer>("score"));
		score.setCellFactory(new Callback<TableColumn<SuggestTableRow, Integer>, TableCell<SuggestTableRow, Integer>>()
		{
			@Override
			public TableCell<SuggestTableRow, Integer> call(TableColumn<SuggestTableRow, Integer> col)
			{
				return new ScoreTableCell();
			}
		});

		TableColumn<SuggestTableRow, String> type = new TableColumn<SuggestTableRow, String>("Type");
		type.setCellValueFactory(new PropertyValueFactory<SuggestTableRow, String>("type"));
		type.setCellFactory(new Callback<TableColumn<SuggestTableRow, String>, TableCell<SuggestTableRow, String>>()
		{
			@Override
			public TableCell<SuggestTableRow, String> call(TableColumn<SuggestTableRow, String> col)
			{
				return new TypeTableCell();
			}
		});

		// such boilerplate, very inefficiency, wow!
		TableColumn<SuggestTableRow, Suggestion> caller = new TableColumn<SuggestTableRow, Suggestion>("Caller");
		caller.setCellValueFactory(new PropertyValueFactory<SuggestTableRow, Suggestion>("suggestion"));
		caller.setCellFactory(new Callback<TableColumn<SuggestTableRow, Suggestion>, TableCell<SuggestTableRow, Suggestion>>()
		{
			@Override
			public TableCell<SuggestTableRow, Suggestion> call(TableColumn<SuggestTableRow, Suggestion> col)
			{
				return new MemberTableCell();
			}
		});

		TableColumn<SuggestTableRow, String> suggestion = new TableColumn<SuggestTableRow, String>("Suggestion");
		suggestion.setCellValueFactory(new PropertyValueFactory<SuggestTableRow, String>("text"));
		suggestion.setCellFactory(new Callback<TableColumn<SuggestTableRow, String>, TableCell<SuggestTableRow, String>>()
		{
			@Override
			public TableCell<SuggestTableRow, String> call(TableColumn<SuggestTableRow, String> col)
			{
				return new SuggestionTableCell();
			}
		});

		score.prefWidthProperty().bind(tv.widthProperty().multiply(0.05));
		type.prefWidthProperty().bind(tv.widthProperty().multiply(0.1));
		caller.prefWidthProperty().bind(tv.widthProperty().multiply(0.35));
		suggestion.prefWidthProperty().bind(tv.widthProperty().multiply(0.50));

		tv.getColumns().add(score);
		tv.getColumns().add(type);
		tv.getColumns().add(caller);
		tv.getColumns().add(suggestion);

		tv.setItems(rows);

		return tv;
	}
}