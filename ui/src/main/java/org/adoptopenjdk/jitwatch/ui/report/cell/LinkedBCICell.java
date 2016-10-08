/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.report.cell;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;

import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.report.Report;
import org.adoptopenjdk.jitwatch.ui.IStageAccessProxy;
import org.adoptopenjdk.jitwatch.ui.report.IReportRowBean;
import org.adoptopenjdk.jitwatch.ui.triview.ITriView;
import org.adoptopenjdk.jitwatch.ui.triview.ILineListener.LineType;

public class LinkedBCICell extends TableCell<IReportRowBean, Report>
{
	private Button btnTriView;

	private static IStageAccessProxy triViewAccessor;

	public static void setTriViewAccessor(IStageAccessProxy triViewAccessor)
	{
		LinkedBCICell.triViewAccessor = triViewAccessor;
	}

	public LinkedBCICell()
	{
		btnTriView = new Button("View");

		setGraphic(btnTriView);
	}

	@Override
	protected void updateItem(final Report report, boolean empty)
	{
		if (report != null && report.getCaller() != null)
		{
			final IMetaMember member = report.getCaller();

			btnTriView.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent e)
				{
					if (report.getCompilationIndex() != -1)
					{
						member.setSelectedCompilation(report.getCompilationIndex());
					}

					ITriView triViewAccesor = triViewAccessor.openTriView(member, false);
					triViewAccesor.lineHighlighted(report.getBytecodeOffset(), LineType.BYTECODE_BCI);
				}
			});

			btnTriView.setText("View BCI " + report.getBytecodeOffset());

			btnTriView.setVisible(true);
		}
		else
		{
			btnTriView.setVisible(false);
		}
	}
}