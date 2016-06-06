/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.suggestion;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.adoptopenjdk.jitwatch.core.JITWatchConstants;
import org.adoptopenjdk.jitwatch.suggestion.Suggestion;
import org.adoptopenjdk.jitwatch.ui.JITWatchUI;
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

public class SuggestStage extends Stage
{
	private VBox vbox;
	private TableView<SuggestTableRow> tableView;
	private ObservableList<SuggestTableRow> obList = FXCollections.observableArrayList();

	private Set<String> filterPackageSet = new HashSet<>();

	private List<Suggestion> suggestions;

	public SuggestStage(final JITWatchUI parent, List<Suggestion> suggestions)
	{
		this.suggestions = suggestions;

		MemberTableCell.setTriViewAccessor(parent);

		initStyle(StageStyle.DECORATED);

		BorderPane borderPane = new BorderPane();

		vbox = new VBox();

		SuggestionPackageFilter filter = new SuggestionPackageFilter(this);

		borderPane.setTop(filter);

		borderPane.setCenter(vbox);

		Scene scene = UserInterfaceUtil.getScene(borderPane, JITWatchUI.WINDOW_WIDTH, JITWatchUI.WINDOW_HEIGHT);

		tableView = SuggestionTableUtil.buildTableSuggestion(obList);

		filter.prefWidthProperty().bind(scene.widthProperty());

		vbox.getChildren().add(tableView);

		tableView.prefHeightProperty().bind(scene.heightProperty());

		setTitle("JITWatch Code Suggestions");

		setScene(scene);

		setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				parent.handleStageClosed(SuggestStage.this);
			}
		});

		display();
	}

	private void display()
	{
		obList.clear();

		if (suggestions.size() == 0)
		{
			vbox.getChildren().clear();
			vbox.getChildren().add(new Label("No suggestions"));
		}
		else
		{
			for (Suggestion suggestion : suggestions)
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
						if (suggestion.getCaller() != null
								&& suggestion.getCaller().getFullyQualifiedMemberName().startsWith(allowedPackage.trim()))
						{
							show = true;
						}
					}
				}

				if (show)
				{
					obList.add(new SuggestTableRow(suggestion));
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