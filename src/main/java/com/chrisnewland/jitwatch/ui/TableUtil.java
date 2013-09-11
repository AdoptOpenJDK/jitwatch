package com.chrisnewland.jitwatch.ui;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.toplist.MemberScore;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class TableUtil
{
	public static TableView<MemberScore> buildTableMemberScore(ObservableList<MemberScore> scores)
	{
		TableView<MemberScore> tv = new TableView<>();

		TableColumn<MemberScore, Long> colScore = new TableColumn<MemberScore, Long>("Value");
		colScore.setCellValueFactory(new PropertyValueFactory<MemberScore, Long>("score"));
		
		TableColumn<MemberScore, IMetaMember> colMember = new TableColumn<MemberScore, IMetaMember>("Method");
		colMember.setCellValueFactory(new PropertyValueFactory<MemberScore, IMetaMember>("member"));

		tv.getColumns().add(colScore);
		tv.getColumns().add(colMember);

		tv.setItems(scores);

		return tv;
	}
	
	public static TableView<AttributeTableRow> buildTableMemberAttributes(ObservableList<AttributeTableRow> rows)
	{
		TableView<AttributeTableRow> tv = new TableView<>();

		TableColumn<AttributeTableRow, String> colType = new TableColumn<AttributeTableRow, String>("Type");
		colType.setCellValueFactory(new PropertyValueFactory<AttributeTableRow, String>("type"));
		
		TableColumn<AttributeTableRow, String> colName = new TableColumn<AttributeTableRow, String>("Name");
		colName.setCellValueFactory(new PropertyValueFactory<AttributeTableRow, String>("name"));
		
		TableColumn<AttributeTableRow, String> colValue = new TableColumn<AttributeTableRow, String>("Value");
		colValue.setCellValueFactory(new PropertyValueFactory<AttributeTableRow, String>("value"));
				
		tv.getColumns().add(colType);
		tv.getColumns().add(colName);
		tv.getColumns().add(colValue);

		tv.setItems(rows);

		return tv;
	}
}