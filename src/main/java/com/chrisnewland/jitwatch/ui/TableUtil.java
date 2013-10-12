/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/blob/master/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
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
        colScore.prefWidthProperty().bind(tv.widthProperty().divide(8));

        TableColumn<MemberScore, IMetaMember> colMember = new TableColumn<MemberScore, IMetaMember>("Method");
        colMember.setCellValueFactory(new PropertyValueFactory<MemberScore, IMetaMember>("member"));
        colMember.prefWidthProperty().bind(tv.widthProperty().divide(8).multiply(7));

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
        colType.prefWidthProperty().bind(tv.widthProperty().divide(3));

        TableColumn<AttributeTableRow, String> colName = new TableColumn<AttributeTableRow, String>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<AttributeTableRow, String>("name"));
        colName.prefWidthProperty().bind(tv.widthProperty().divide(3));

        TableColumn<AttributeTableRow, String> colValue = new TableColumn<AttributeTableRow, String>("Value");
        colValue.setCellValueFactory(new PropertyValueFactory<AttributeTableRow, String>("value"));
        colValue.prefWidthProperty().bind(tv.widthProperty().divide(3));

        tv.getColumns().add(colType);
        tv.getColumns().add(colName);
        tv.getColumns().add(colValue);

        tv.setItems(rows);

        return tv;
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