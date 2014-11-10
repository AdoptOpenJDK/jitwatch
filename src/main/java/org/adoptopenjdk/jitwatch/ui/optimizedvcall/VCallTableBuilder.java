/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.optimizedvcall;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.optimizedvcall.OptimizedVirtualCall;
import org.adoptopenjdk.jitwatch.ui.IStageAccessProxy;
import org.adoptopenjdk.jitwatch.ui.triview.ITriView;

public class VCallTableBuilder
{
	public static TableView<VCallRow> buildTable(final IStageAccessProxy proxy, List<OptimizedVirtualCall> vCalls)
	{
		TableView<VCallRow> tableView = new TableView<>();

		TableColumn<VCallRow, String> colCallerClass = new TableColumn<VCallRow, String>("Caller Class");
		colCallerClass.setCellValueFactory(new PropertyValueFactory<VCallRow, String>("callerClass"));

		TableColumn<VCallRow, String> colCallerMember = new TableColumn<VCallRow, String>("Method");
		colCallerMember.setCellValueFactory(new PropertyValueFactory<VCallRow, String>("callerMember"));

		TableColumn<VCallRow, Integer> colCallerLine = new TableColumn<VCallRow, Integer>("Source Line");
		colCallerLine.setCellValueFactory(new PropertyValueFactory<VCallRow, Integer>("callerSourceLine"));

		TableColumn<VCallRow, Integer> colCallerBytecode = new TableColumn<VCallRow, Integer>("BCI");
		colCallerBytecode.setCellValueFactory(new PropertyValueFactory<VCallRow, Integer>("callerBCI"));

		TableColumn<VCallRow, String> colInvokeType = new TableColumn<VCallRow, String>("Invoke");
		colInvokeType.setCellValueFactory(new PropertyValueFactory<VCallRow, String>("invokeType"));

		tableView.getColumns().add(colCallerClass);
		tableView.getColumns().add(colCallerMember);
		tableView.getColumns().add(colCallerLine);
		tableView.getColumns().add(colCallerBytecode);
		tableView.getColumns().add(colInvokeType);

		TableColumn<VCallRow, String> colCalleeClass = new TableColumn<VCallRow, String>("Callee Class");
		colCalleeClass.setCellValueFactory(new PropertyValueFactory<VCallRow, String>("calleeClass"));

		TableColumn<VCallRow, String> colCalleeMember = new TableColumn<VCallRow, String>("Method");
		colCalleeMember.setCellValueFactory(new PropertyValueFactory<VCallRow, String>("calleeMember"));

		TableColumn<VCallRow, Integer> colCalleeLine = new TableColumn<VCallRow, Integer>("Source Line");
		colCalleeLine.setCellValueFactory(new PropertyValueFactory<VCallRow, Integer>("calleeSourceLine"));

		TableColumn<VCallRow, Integer> colCalleeBytecode = new TableColumn<VCallRow, Integer>("BCI");
		colCalleeBytecode.setCellValueFactory(new PropertyValueFactory<VCallRow, Integer>("calleeBCI"));

		tableView.getColumns().add(colCalleeClass);
		tableView.getColumns().add(colCalleeMember);
		tableView.getColumns().add(colCalleeLine);
		tableView.getColumns().add(colCalleeBytecode);

		tableView.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				VCallRow selected = tableView.getSelectionModel().getSelectedItem();

				if (selected != null)
				{
					IMetaMember callingMember = selected.getCallingMember();

					ITriView triView = proxy.openTriView(callingMember, true);

					triView.highlightSourceLine(selected.getCallerSourceLine());
				}
			}
		});

		List<VCallRow> rowList = new ArrayList<>();

		for (OptimizedVirtualCall vCall : vCalls)
		{
			VCallRow row = new VCallRow(vCall);
			rowList.add(row);
		}

		tableView.setItems(FXCollections.observableArrayList(rowList));

		return tableView;
	}
}
