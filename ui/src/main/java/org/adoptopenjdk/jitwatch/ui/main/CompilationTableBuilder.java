/*
 * Copyright (c) 2021 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.main;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import static org.adoptopenjdk.jitwatch.util.UserInterfaceUtil.LANG;

public final class CompilationTableBuilder
{
	private CompilationTableBuilder()
	{
	}

	public static TableView<CompilationTableRow> buildTableMemberAttributes(ObservableList<CompilationTableRow> rows)
	{
		TableView<CompilationTableRow> tv = new TableView<>();

		TableColumn<CompilationTableRow, String> columnQueued = new TableColumn<>(LANG.getString("CT_QUEUED"));
		columnQueued.setCellValueFactory(new PropertyValueFactory<>("stampQueued"));
		columnQueued.prefWidthProperty().bind(tv.widthProperty().multiply(0.15));

		TableColumn<CompilationTableRow, String> columnCompileStart = new TableColumn<>(LANG.getString("CT_COMPILE_START"));
		columnCompileStart.setCellValueFactory(new PropertyValueFactory<>("stampCompilationStart"));
		columnCompileStart.prefWidthProperty().bind(tv.widthProperty().multiply(0.15));
		
		TableColumn<CompilationTableRow, String> columnEmitted = new TableColumn<>(LANG.getString("CT_NMETHOD_EMIT"));
		columnEmitted.setCellValueFactory(new PropertyValueFactory<>("stampNMethodEmitted"));
		columnEmitted.prefWidthProperty().bind(tv.widthProperty().multiply(0.15));

		TableColumn<CompilationTableRow, String> columnSize = new TableColumn<>(LANG.getString("CT_NMETHOD_EMIT"));
		columnSize.setCellValueFactory(new PropertyValueFactory<>("native"));
		columnSize.prefWidthProperty().bind(tv.widthProperty().multiply(0.2));

		TableColumn<CompilationTableRow, String> columnCompiler = new TableColumn<>(LANG.getString("CT_COMPILER"));
		columnCompiler.setCellValueFactory(new PropertyValueFactory<>("compiler"));
		columnCompiler.prefWidthProperty().bind(tv.widthProperty().multiply(0.2));

		TableColumn<CompilationTableRow, String> columnLevel = new TableColumn<>(LANG.getString("CT_LEVEL"));
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
