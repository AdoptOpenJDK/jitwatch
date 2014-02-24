/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui;

import com.chrisnewland.jitwatch.suggestion.Suggestion;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class SuggestStage extends Stage
{
	private TableView<SuggestTableRow> tableView;
	private ObservableList<SuggestTableRow> obList = FXCollections.observableArrayList();
		
	public SuggestStage(final JITWatchUI parent)
	{
		initStyle(StageStyle.DECORATED);
			
		VBox vbox = new VBox();
		
		Scene scene = new Scene(vbox, 800, 480);
	
		tableView = TableUtil.buildTableSuggestion(obList);
		
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
						
		obList.add(makeRow(null)); //TODO finish
	}
	
	private SuggestTableRow makeRow(Suggestion suggestion)
	{
		return new SuggestTableRow(suggestion);
	}
}