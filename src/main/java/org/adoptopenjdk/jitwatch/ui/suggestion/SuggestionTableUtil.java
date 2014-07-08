/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.suggestion;

import org.adoptopenjdk.jitwatch.model.IMetaMember;

import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public final class SuggestionTableUtil
{
    /*
        Hide Utility Class Constructor
        Utility classes should not have a public or default constructor.
    */
    private SuggestionTableUtil() {
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
		
		TableColumn<SuggestTableRow, IMetaMember> caller = new TableColumn<SuggestTableRow, IMetaMember>("Caller");
		caller.setCellValueFactory(new PropertyValueFactory<SuggestTableRow, IMetaMember>("caller"));
		caller.setCellFactory(new Callback<TableColumn<SuggestTableRow, IMetaMember>, TableCell<SuggestTableRow, IMetaMember>>()
		{
			@Override
			public TableCell<SuggestTableRow, IMetaMember> call(TableColumn<SuggestTableRow, IMetaMember> col)
			{
				return new MemberTableCell();
			}
		});
		
		TableColumn<SuggestTableRow, String> suggestion = new TableColumn<SuggestTableRow, String>("Suggestion");
		suggestion.setCellValueFactory(new PropertyValueFactory<SuggestTableRow, String>("suggestion"));
		suggestion.setCellFactory(new Callback<TableColumn<SuggestTableRow, String>, TableCell<SuggestTableRow, String>>()
		{
			@Override
			public TableCell<SuggestTableRow, String> call(TableColumn<SuggestTableRow, String> col)
			{
				return new SuggestionTableCell();
			}
		});
		
		score.prefWidthProperty().bind(tv.widthProperty().multiply(0.07));
		type.prefWidthProperty().bind(tv.widthProperty().multiply(0.08));
		caller.prefWidthProperty().bind(tv.widthProperty().multiply(0.35));
		suggestion.prefWidthProperty().bind(tv.widthProperty().multiply(0.48));
		
		tv.getColumns().add(score);
		tv.getColumns().add(type);
		tv.getColumns().add(caller);
		tv.getColumns().add(suggestion);

		tv.setItems(rows);

		return tv;
	}
}