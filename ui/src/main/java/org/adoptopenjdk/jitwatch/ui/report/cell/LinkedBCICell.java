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
import org.adoptopenjdk.jitwatch.ui.main.IStageAccessProxy;
import org.adoptopenjdk.jitwatch.ui.report.IReportRowBean;

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

					triViewAccessor.openTriView(member, false, report.getBytecodeOffset());
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