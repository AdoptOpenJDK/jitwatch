/*
 * Copyright (c) 2013-2017 Chris Newland.
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
import org.adoptopenjdk.jitwatch.ui.main.IStageAccessProxy;
import org.adoptopenjdk.jitwatch.ui.main.JITWatchUI;
import org.adoptopenjdk.jitwatch.ui.report.cell.LinkedBCICell;
import org.adoptopenjdk.jitwatch.ui.report.cell.MemberTableCell;
import org.adoptopenjdk.jitwatch.ui.report.elidedlock.ElidedLockRowBean;
import org.adoptopenjdk.jitwatch.ui.report.elidedlock.ElidedLockRowBuilder;
import org.adoptopenjdk.jitwatch.ui.report.eliminatedallocation.EliminatedAllocationRowBean;
import org.adoptopenjdk.jitwatch.ui.report.eliminatedallocation.EliminatedAllocationRowBuilder;
import org.adoptopenjdk.jitwatch.ui.report.inlining.InliningRowBean;
import org.adoptopenjdk.jitwatch.ui.report.inlining.InliningRowBuilder;
import org.adoptopenjdk.jitwatch.ui.report.suggestion.SuggestionRowBean;
import org.adoptopenjdk.jitwatch.ui.report.suggestion.SuggestionRowBuilder;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ReportStage extends Stage
{
	private VBox vbox;
	private TableView<IReportRowBean> tableView;
	private ObservableList<IReportRowBean> observableList = FXCollections.observableArrayList();

	private Set<String> filterPackageSet = new HashSet<>();

	private List<Report> reportList;

	private ReportStageType type;

	public ReportStage(final IStageAccessProxy proxy, String title, ReportStageType type, List<Report> reportList)
	{
		this.reportList = reportList;
		this.type = type;

		MemberTableCell.setTriViewAccessor(proxy);
		LinkedBCICell.setTriViewAccessor(proxy);

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
			setTitle(title);
			tableView = SuggestionRowBuilder.buildTableSuggestion(observableList);
			break;
		case ELIMINATED_ALLOCATION:
			setTitle(title);
			tableView = EliminatedAllocationRowBuilder.buildTableSuggestion(observableList);
			break;
		case ELIDED_LOCK:
			setTitle(title);
			tableView = ElidedLockRowBuilder.buildTableSuggestion(observableList);
			break;
		case INLINING:
			setTitle(title);
			tableView = InliningRowBuilder.buildTableSuggestion(observableList);
			break;
		}

		filter.prefWidthProperty().bind(scene.widthProperty());

		vbox.getChildren().add(tableView);

		tableView.prefHeightProperty().bind(scene.heightProperty());

		setScene(scene);

		display();
	}

	public void clear()
	{
		observableList.clear();
	}
	
	public ReportStageType getType()
	{
		return type;
	}

	private void display()
	{
		clear();

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
					case ELIDED_LOCK:
						observableList.add(new ElidedLockRowBean(report));
						break;
					case INLINING:
						observableList.add(new InliningRowBean(report));
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