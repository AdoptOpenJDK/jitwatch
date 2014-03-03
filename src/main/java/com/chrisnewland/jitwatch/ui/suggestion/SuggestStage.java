/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui.suggestion;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.chrisnewland.jitwatch.suggestion.AttributeSuggestionWalker;
import com.chrisnewland.jitwatch.suggestion.Suggestion;
import com.chrisnewland.jitwatch.ui.JITWatchUI;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class SuggestStage extends Stage
{
	private VBox vbox;
	private TableView<SuggestTableRow> tableView;
	private ObservableList<SuggestTableRow> obList = FXCollections.observableArrayList();

	private JITWatchUI parent;

	public SuggestStage(final JITWatchUI parent)
	{
		this.parent = parent;
		
		MemberTableCell.setTriViewAccessor(parent);

		initStyle(StageStyle.DECORATED);

		vbox = new VBox();

		Scene scene = new Scene(vbox, JITWatchUI.WINDOW_WIDTH, JITWatchUI.WINDOW_HEIGHT);

		tableView = SuggestionTableUtil.buildTableSuggestion(obList);

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

		build();
	}

	private void build()
	{
		obList.clear();

		AttributeSuggestionWalker walker = new AttributeSuggestionWalker(parent.getJITDataModel());

		List<Suggestion> suggestions = walker.getSuggestionList();

		if (suggestions.size() == 0)
		{
			vbox.getChildren().clear();
			vbox.getChildren().add(new Label("No suggestions"));
		}
		else
		{
			Collections.sort(suggestions, new Comparator<Suggestion>()
			{
				@Override
				public int compare(Suggestion s1, Suggestion s2)
				{
					return Integer.compare(s2.getScore(), s1.getScore());
				}	
			});
			
			for (Suggestion suggestion : suggestions)
			{
				obList.add(new SuggestTableRow(suggestion));
			}
		}
	}
}