/*
 * Copyright (c) 2019 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.toplist;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.adoptopenjdk.jitwatch.chain.CompileNode;

public final class CompileNodeRowBuilder
{
	private CompileNodeRowBuilder()
	{
	}

	public static TableView<CompileNode> buildTable(ObservableList<CompileNode> rows)
	{
		TableView<CompileNode> tv = new TableView<>();

		TableColumn<CompileNode, String> compilationRoot = new TableColumn<>("Compilation Root");
		compilationRoot.setCellValueFactory(new PropertyValueFactory<>("compilationRoot"));

		TableColumn<CompileNode, String> rootCompilation = new TableColumn<>("Compilation");
		rootCompilation.setCellValueFactory(new PropertyValueFactory<>("rootCompilationSignature"));

		TableColumn<CompileNode, String> callerMember = new TableColumn<>("Caller Method");
		callerMember.setCellValueFactory(new PropertyValueFactory<>("callerMember"));

		TableColumn<CompileNode, Integer> callerBCI = new TableColumn<>("BCI");
		callerBCI.setCellValueFactory(new PropertyValueFactory<>("callerBCI"));

		//LinkedBCICell

		TableColumn<CompileNode, String> calleeMember = new TableColumn<>("Callee Method");
		calleeMember.setCellValueFactory(new PropertyValueFactory<>("calleeMember"));

		compilationRoot.prefWidthProperty().bind(tv.widthProperty().multiply(0.25));
		rootCompilation.prefWidthProperty().bind(tv.widthProperty().multiply(0.20));
		callerMember.prefWidthProperty().bind(tv.widthProperty().multiply(0.25));
		callerBCI.prefWidthProperty().bind(tv.widthProperty().multiply(0.05));
		calleeMember.prefWidthProperty().bind(tv.widthProperty().multiply(0.25));

		tv.getColumns().add(compilationRoot);
		tv.getColumns().add(rootCompilation);
		tv.getColumns().add(callerMember);
		tv.getColumns().add(callerBCI);
		tv.getColumns().add(calleeMember);

		tv.setItems(rows);

		return tv;
	}
}