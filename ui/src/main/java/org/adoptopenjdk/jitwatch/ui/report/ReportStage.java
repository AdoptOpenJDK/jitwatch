/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.report;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.adoptopenjdk.jitwatch.core.JITWatchConstants;
import org.adoptopenjdk.jitwatch.report.Report;
import org.adoptopenjdk.jitwatch.ui.JITWatchUI;
import org.adoptopenjdk.jitwatch.ui.report.cell.LinkedBCICell;
import org.adoptopenjdk.jitwatch.ui.report.cell.MemberTableCell;
import org.adoptopenjdk.jitwatch.ui.report.eliminatedallocation.EliminatedAllocationRowBean;
import org.adoptopenjdk.jitwatch.ui.report.eliminatedallocation.EliminatedAllocationRowBuilder;
import org.adoptopenjdk.jitwatch.ui.report.suggestion.SuggestionRowBean;
import org.adoptopenjdk.jitwatch.ui.report.suggestion.SuggestionRowBuilder;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class ReportStage extends Stage
{
	private VBox vbox;
	private TableView<IReportRowBean> tableView;
	private ObservableList<IReportRowBean> observableList = FXCollections.observableArrayList();

	private Set<String> filterPackageSet = new HashSet<>();

	private List<Report> reportList;

	private ReportStageType type;

	public ReportStage(final JITWatchUI parent, ReportStageType type, List<Report> reportList)
	{
		this.reportList = reportList;
		this.type = type;

		MemberTableCell.setTriViewAccessor(parent);
		LinkedBCICell.setTriViewAccessor(parent);

		initStyle(StageStyle.DECORATED);

		BorderPane borderPane = new BorderPane();

		vbox = new VBox();

		ReportPackageFilter filter = new ReportPackageFilter(this);

		borderPane.setTop(filter);

		borderPane.setCenter(vbox);

		Scene scene = UserInterfaceUtil.getScene(borderPane, JITWatchUI.WINDOW_WIDTH, JITWatchUI.WINDOW_HEIGHT);

		switch (type)
		{
		case SUGGESTION:
			setTitle("JITWatch Code Suggestions");
			tableView = SuggestionRowBuilder.buildTableSuggestion(observableList);
			break;
		case ELIMINATED_ALLOCATION:
			setTitle("JITWatch Eliminated Allocation Report");
			tableView = EliminatedAllocationRowBuilder.buildTableSuggestion(observableList);
			break;
		}
		
		filter.prefWidthProperty().bind(scene.widthProperty());

		vbox.getChildren().add(tableView);

		tableView.prefHeightProperty().bind(scene.heightProperty());

		setScene(scene);

		setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				parent.handleStageClosed(ReportStage.this);
			}
		});

		display();
	}

	public ReportStageType getType()
	{
		return type;
	}

	private void display()
	{
		observableList.clear();

		if (reportList.size() == 0)
		{
			vbox.getChildren().clear();
			vbox.getChildren().add(new Label("No results"));
		}
		else
		{
			for (Report report : reportList)
			{
				boolean show = false;

				if (filterPackageSet.size() == 0)
				{
					show = true;
				}
				else
				{
					for (String allowedPackage : filterPackageSet)
					{
						if (report.getCaller() != null
								&& report.getCaller().getFullyQualifiedMemberName().startsWith(allowedPackage.trim()))
						{
							show = true;
						}
					}
				}

				if (show)
				{					
					switch (type)
					{
					case SUGGESTION:
						observableList.add(new SuggestionRowBean(report));
						break;
					case ELIMINATED_ALLOCATION:
						observableList.add(new EliminatedAllocationRowBean(report));
						break;
					}
				}
			}
		}
	}

	public void setFilter(String packageFilter)
	{
		String[] packages = packageFilter.split(JITWatchConstants.S_COMMA);

		filterPackageSet.clear();
		filterPackageSet.addAll(Arrays.asList(packages));
		display();
	}
}