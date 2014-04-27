/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui.live;

import com.chrisnewland.jitwatch.core.JITWatchConfig;
import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.ui.JITWatchUI;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class LiveViewStage extends Stage
{
	private IMetaMember currentMember;

	private SplitPane splitViewer;
	
	private TextArea taSource;
	private TextArea taLoad;

	private VBox colSource;
	private VBox colLoad;

	public LiveViewStage(final JITWatchUI parent, final JITWatchConfig config)
	{
		setTitle("LiveView JIT Sandbox");

		VBox vBox = new VBox();

		HBox hBoxToolBarButtons = new HBox();
		hBoxToolBarButtons.setSpacing(10);
		hBoxToolBarButtons.setPadding(new Insets(0,10,10,10));

		Button btnCallChain = new Button("Go!");
		btnCallChain.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				if (currentMember != null)
				{
					runTestLoad();
				}
			}
		});

		hBoxToolBarButtons.getChildren().add(btnCallChain);
	
		splitViewer = new SplitPane();
		splitViewer.setOrientation(Orientation.HORIZONTAL);

		colSource = new VBox();
		colLoad = new VBox();

		Label lblSource = new Label("Source");
		Label lblLoad = new Label("Test Load");

		lblSource.setStyle("-fx-background-color:#dddddd; -fx-padding:4px;");
		lblLoad.setStyle("-fx-background-color:#dddddd; -fx-padding:4px;");

		lblSource.prefWidthProperty().bind(colSource.widthProperty());
		lblLoad.prefWidthProperty().bind(colLoad.widthProperty());

		taSource = new TextArea();
		taLoad = new TextArea();

		colSource.getChildren().add(lblSource);
		colSource.getChildren().add(taSource);

		colLoad.getChildren().add(lblLoad);
		colLoad.getChildren().add(taLoad);

		splitViewer.prefHeightProperty().bind(vBox.heightProperty());

		taSource.prefWidthProperty().bind(colSource.widthProperty());
		taSource.prefHeightProperty().bind(colSource.heightProperty());

		taLoad.prefWidthProperty().bind(colLoad.widthProperty());
		taLoad.prefHeightProperty().bind(colLoad.heightProperty());

		splitViewer.getItems().add(colSource);
		splitViewer.getItems().add(colLoad);
		
		// horizontal VM switches!
		// horizontal status messages
		
		vBox.getChildren().add(hBoxToolBarButtons);
		vBox.getChildren().add(splitViewer);
		
		Scene scene = new Scene(vBox, JITWatchUI.WINDOW_WIDTH, JITWatchUI.WINDOW_HEIGHT);

		setScene(scene);

		setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				parent.handleStageClosed(LiveViewStage.this);
			}
		});
	}
	
	private void runTestLoad()
	{
		
	}
}
