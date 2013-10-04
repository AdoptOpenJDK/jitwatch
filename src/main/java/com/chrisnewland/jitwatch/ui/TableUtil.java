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

    public static TableView<AttributeTableRow3Col> buildTableMemberAttributes(ObservableList<AttributeTableRow3Col> rows)
    {
        TableView<AttributeTableRow3Col> tv = new TableView<>();

        TableColumn<AttributeTableRow3Col, String> colType = new TableColumn<AttributeTableRow3Col, String>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<AttributeTableRow3Col, String>("type"));
        colType.prefWidthProperty().bind(tv.widthProperty().divide(3));

        TableColumn<AttributeTableRow3Col, String> colName = new TableColumn<AttributeTableRow3Col, String>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<AttributeTableRow3Col, String>("name"));
        colName.prefWidthProperty().bind(tv.widthProperty().divide(3));

        TableColumn<AttributeTableRow3Col, String> colValue = new TableColumn<AttributeTableRow3Col, String>("Value");
        colValue.setCellValueFactory(new PropertyValueFactory<AttributeTableRow3Col, String>("value"));
        colValue.prefWidthProperty().bind(tv.widthProperty().divide(3));

        tv.getColumns().add(colType);
        tv.getColumns().add(colName);
        tv.getColumns().add(colValue);

        tv.setItems(rows);

        return tv;
    }

    public static TableView<AttributeTableRow2Col> buildTableStats(ObservableList<AttributeTableRow2Col> rows)
    {
        TableView<AttributeTableRow2Col> tv = new TableView<>();

        TableColumn<AttributeTableRow2Col, String> colName = new TableColumn<AttributeTableRow2Col, String>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<AttributeTableRow2Col, String>("name"));
        colName.prefWidthProperty().bind(tv.widthProperty().multiply(0.33));

        TableColumn<AttributeTableRow2Col, Long> colValue = new TableColumn<AttributeTableRow2Col, Long>("Value");
        colValue.setCellValueFactory(new PropertyValueFactory<AttributeTableRow2Col, Long>("value"));
        colValue.prefWidthProperty().bind(tv.widthProperty().multiply(0.66));

        tv.getColumns().add(colName);
        tv.getColumns().add(colValue);

        tv.setItems(rows);

        return tv;
    }
}