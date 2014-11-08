package org.adoptopenjdk.jitwatch.ui.optimizedvcall;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

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

		TableColumn<VCallRow, Integer> colCallerBytecode = new TableColumn<VCallRow, Integer>("Bytecode offset");
		colCallerBytecode.setCellValueFactory(new PropertyValueFactory<VCallRow, Integer>("callerBCI"));

		tableView.getColumns().add(colCallerClass);
		tableView.getColumns().add(colCallerMember);
		tableView.getColumns().add(colCallerLine);
		tableView.getColumns().add(colCallerBytecode);

		TableColumn<VCallRow, String> colCalleeClass = new TableColumn<VCallRow, String>("Callee Class");
		colCalleeClass.setCellValueFactory(new PropertyValueFactory<VCallRow, String>("calleeClass"));

		TableColumn<VCallRow, String> colCalleeMember = new TableColumn<VCallRow, String>("Method");
		colCalleeMember.setCellValueFactory(new PropertyValueFactory<VCallRow, String>("calleeMember"));

		TableColumn<VCallRow, Integer> colCalleeLine = new TableColumn<VCallRow, Integer>("Source Line");
		colCalleeLine.setCellValueFactory(new PropertyValueFactory<VCallRow, Integer>("calleeSourceLine"));

		TableColumn<VCallRow, Integer> colCalleeBytecode = new TableColumn<VCallRow, Integer>("Bytecode offset");
		colCalleeBytecode.setCellValueFactory(new PropertyValueFactory<VCallRow, Integer>("calleeBCI"));

		tableView.getColumns().add(colCalleeClass);
		tableView.getColumns().add(colCalleeMember);
		tableView.getColumns().add(colCalleeLine);
		tableView.getColumns().add(colCalleeBytecode);

		tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<VCallRow>()
		{
			@Override
			public void changed(ObservableValue<? extends VCallRow> arg0, VCallRow oldRow, VCallRow newRow)
			{
				if (newRow != null)
				{
					IMetaMember callingMember = newRow.getCallingMember();

					ITriView triView = proxy.openTriView(callingMember, true);

					triView.highlightSourceLine(newRow.getCallerSourceLine());
				}
			}
		});

		List<VCallRow> rowList = new ArrayList<>();

		for (OptimizedVirtualCall vCall : vCalls)
		{
			VCallRow row = new VCallRow(vCall.getCallingMember(), vCall.getCaller(), vCall.getCallee());
			rowList.add(row);
		}

		tableView.setItems(FXCollections.observableArrayList(rowList));

		return tableView;
	}
}
