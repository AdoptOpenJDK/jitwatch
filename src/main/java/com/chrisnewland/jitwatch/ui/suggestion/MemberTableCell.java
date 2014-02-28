/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui.suggestion;

import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.layout.VBox;

import com.chrisnewland.jitwatch.model.IMetaMember;

class MemberTableCell extends TableCell<SuggestTableRow, IMetaMember>
{
	private VBox vb;
	private Label lblMetaClass;
	private Label lblMetaMember;

	public MemberTableCell()
	{
		vb = new VBox();

		lblMetaClass = new Label();
		lblMetaMember = new Label();

		vb.getChildren().add(lblMetaClass);
		vb.getChildren().add(lblMetaMember);

		setGraphic(vb);
	}

	@Override
	protected void updateItem(IMetaMember member, boolean empty)
	{
		if (member != null)
		{
			lblMetaClass.setText(member.getMetaClass().getFullyQualifiedName());
			lblMetaMember.setText(member.toStringUnqualifiedMethodName());
		}
	}
}