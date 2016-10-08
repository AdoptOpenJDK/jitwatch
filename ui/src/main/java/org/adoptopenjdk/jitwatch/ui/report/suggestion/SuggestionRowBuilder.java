/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.report.suggestion;

import org.adoptopenjdk.jitwatch.report.Report;
import org.adoptopenjdk.jitwatch.ui.report.IReportRowBean;
import org.adoptopenjdk.jitwatch.ui.report.cell.IntegerTableCell;
import org.adoptopenjdk.jitwatch.ui.report.cell.MemberTableCell;
import org.adoptopenjdk.jitwatch.ui.report.cell.TextWrapTableCell;
import org.adoptopenjdk.jitwatch.ui.report.cell.TextTableCell;

import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public final class SuggestionRowBuilder
{
	private SuggestionRowBuilder()
	{
	}

	public static TableView<IReportRowBean> buildTableSuggestion(ObservableList<IReportRowBean> rows)
	{
		TableView<IReportRowBean> tv = new TableView<>();

		TableColumn<IReportRowBean, Integer> score = new TableColumn<IReportRowBean, Integer>("Score");
		score.setCellValueFactory(new PropertyValueFactory<IReportRowBean, Integer>("score"));
		score.setCellFactory(new Callback<TableColumn<IReportRowBean, Integer>, TableCell<IReportRowBean, Integer>>()
		{
			@Override
			public TableCell<IReportRowBean, Integer> call(TableColumn<IReportRowBean, Integer> col)
			{
				return new IntegerTableCell();
			}
		});

		TableColumn<IReportRowBean, String> type = new TableColumn<IReportRowBean, String>("Type");
		type.setCellValueFactory(new PropertyValueFactory<IReportRowBean, String>("type"));
		type.setCellFactory(new Callback<TableColumn<IReportRowBean, String>, TableCell<IReportRowBean, String>>()
		{
			@Override
			public TableCell<IReportRowBean, String> call(TableColumn<IReportRowBean, String> col)
			{
				return new TextTableCell();
			}
		});

		TableColumn<IReportRowBean, Report> caller = new TableColumn<IReportRowBean, Report>("Caller");
		caller.setCellValueFactory(new PropertyValueFactory<IReportRowBean, Report>("report"));
		caller.setCellFactory(new Callback<TableColumn<IReportRowBean, Report>, TableCell<IReportRowBean, Report>>()
		{
			@Override
			public TableCell<IReportRowBean, Report> call(TableColumn<IReportRowBean, Report> col)
			{
				return new MemberTableCell();
			}
		});

		TableColumn<IReportRowBean, String> suggestion = new TableColumn<IReportRowBean, String>("Suggestion");
		suggestion.setCellValueFactory(new PropertyValueFactory<IReportRowBean, String>("text"));
		suggestion.setCellFactory(new Callback<TableColumn<IReportRowBean, String>, TableCell<IReportRowBean, String>>()
		{
			@Override
			public TableCell<IReportRowBean, String> call(TableColumn<IReportRowBean, String> col)
			{
				return new TextWrapTableCell();
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