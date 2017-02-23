/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.stats;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public final class StatsTableBuilder
{
	private StatsTableBuilder()
	{
	}

	public static TableView<StatsTableRow> buildTableStats(ObservableList<StatsTableRow> rows)
	{
		TableView<StatsTableRow> tv = new TableView<>();

		TableColumn<StatsTableRow, String> colName = new TableColumn<StatsTableRow, String>("Name");
		colName.setCellValueFactory(new PropertyValueFactory<StatsTableRow, String>("name"));
		colName.prefWidthProperty().bind(tv.widthProperty().multiply(0.33));

		TableColumn<StatsTableRow, Long> colValue = new TableColumn<StatsTableRow, Long>("Value");
		colValue.setCellValueFactory(new PropertyValueFactory<StatsTableRow, Long>("value"));
		colValue.prefWidthProperty().bind(tv.widthProperty().multiply(0.66));

		tv.getColumns().add(colName);
		tv.getColumns().add(colValue);

		tv.setItems(rows);

		return tv;
	}
}