/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui.suggestion;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.layout.VBox;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.ui.IStageAccessProxy;

public class MemberTableCell extends TableCell<SuggestTableRow, IMetaMember>
{
	private VBox vb;
	private Label lblMetaClass;
	private Label lblMetaMember;
	private Button btnTriView;

	private static IStageAccessProxy triViewAccessor;

	public static void setTriViewAccessor(IStageAccessProxy triViewAccessor)
	{
		MemberTableCell.triViewAccessor = triViewAccessor;
	}

	public MemberTableCell()
	{
		vb = new VBox();

		lblMetaClass = new Label();
		lblMetaMember = new Label();
		btnTriView = new Button();
		btnTriView.setText("View");

		vb.getChildren().add(lblMetaClass);
		vb.getChildren().add(lblMetaMember);
		vb.getChildren().add(btnTriView);
		
		vb.setSpacing(5);

		setGraphic(vb);
	}

	@Override
	protected void updateItem(final IMetaMember member, boolean empty)
	{
		if (member != null)
		{
			btnTriView.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent e)
				{
					triViewAccessor.openTriView(member, false);
				}
			});

			lblMetaClass.setText(member.getMetaClass().getFullyQualifiedName());
			lblMetaMember.setText(member.toStringUnqualifiedMethodName());
		}
	}
}