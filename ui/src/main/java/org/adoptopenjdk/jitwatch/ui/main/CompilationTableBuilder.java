/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.main;

import org.adoptopenjdk.jitwatch.ui.main.CompilationTableRow;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public final class CompilationTableBuilder
{
	private CompilationTableBuilder()
	{
	}

	public static TableView<CompilationTableRow> buildTableMemberAttributes(ObservableList<CompilationTableRow> rows)
	{
		TableView<CompilationTableRow> tv = new TableView<>();

		TableColumn<CompilationTableRow, String> columnQueued = new TableColumn<>("Queued");
		columnQueued.setCellValueFactory(new PropertyValueFactory<>("stampQueued"));
		columnQueued.prefWidthProperty().bind(tv.widthProperty().multiply(0.15));

		TableColumn<CompilationTableRow, String> columnCompileStart = new TableColumn<>("Compile Start");
		columnCompileStart.setCellValueFactory(new PropertyValueFactory<>("stampCompilationStart"));
		columnCompileStart.prefWidthProperty().bind(tv.widthProperty().multiply(0.15));
		
		TableColumn<CompilationTableRow, String> columnEmitted = new TableColumn<>("NMethod Emit");
		columnEmitted.setCellValueFactory(new PropertyValueFactory<>("stampNMethodEmitted"));
		columnEmitted.prefWidthProperty().bind(tv.widthProperty().multiply(0.15));

		TableColumn<CompilationTableRow, String> columnSize = new TableColumn<>("Native Size");
		columnSize.setCellValueFactory(new PropertyValueFactory<>("native"));
		columnSize.prefWidthProperty().bind(tv.widthProperty().multiply(0.2));

		TableColumn<CompilationTableRow, String> columnCompiler = new TableColumn<>("Compiler");
		columnCompiler.setCellValueFactory(new PropertyValueFactory<>("compiler"));
		columnCompiler.prefWidthProperty().bind(tv.widthProperty().multiply(0.2));

		TableColumn<CompilationTableRow, String> columnLevel = new TableColumn<>("Level");
		columnLevel.setCellValueFactory(new PropertyValueFactory<>("level"));
		columnLevel.prefWidthProperty().bind(tv.widthProperty().multiply(0.15));
		
		tv.getColumns().add(columnQueued);
		tv.getColumns().add(columnCompileStart);
		tv.getColumns().add(columnEmitted);
		tv.getColumns().add(columnSize);
		tv.getColumns().add(columnCompiler);
		tv.getColumns().add(columnLevel);

		tv.setItems(rows);

		return tv;
	}
}
