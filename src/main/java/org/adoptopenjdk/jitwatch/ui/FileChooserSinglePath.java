/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

public class FileChooserSinglePath extends VBox
{
	private static final Logger logger = LoggerFactory.getLogger(FileChooserSinglePath.class);

	private Stage stage;

	protected TextField tfChosenFile;

	private File lastFolder = null;

	protected VBox vboxButtons;

	public FileChooserSinglePath(Stage stage, String title, String currentPath)
	{
		this.stage = stage;

		HBox hbox = new HBox();

		tfChosenFile = new TextField();

		if (currentPath != null)
		{
			tfChosenFile.setText(currentPath);
		}

		Button btnOpenFileDialog = new Button("Choose");
		btnOpenFileDialog.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				chooseFile();
			}
		});

		Button btnRemove = new Button("Remove");
		btnRemove.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				tfChosenFile.setText(S_EMPTY);
			}
		});

		vboxButtons = new VBox();
		vboxButtons.setPadding(new Insets(10));
		vboxButtons.setSpacing(10);

		vboxButtons.getChildren().add(btnOpenFileDialog);
		vboxButtons.getChildren().add(btnRemove);

		hbox.getChildren().add(tfChosenFile);
		hbox.getChildren().add(vboxButtons);

		tfChosenFile.prefWidthProperty().bind(this.widthProperty().multiply(0.8));
		vboxButtons.prefWidthProperty().bind(this.widthProperty().multiply(0.2));

		Label titleLabel = new Label(title);

		getChildren().add(titleLabel);
		getChildren().add(hbox);

		setSpacing(10);
	}

	private void chooseFile()
	{
		FileChooser fc = new FileChooser();
		fc.setTitle("Choose File");

		File dirFile = null;

		if (lastFolder == null)
		{
			dirFile = new File(System.getProperty("user.dir"));
		}
		else
		{
			dirFile = lastFolder;
		}

		fc.setInitialDirectory(dirFile);

		File result = fc.showOpenDialog(stage);

		if (result != null)
		{
			try
			{
				tfChosenFile.setText(result.getCanonicalPath());
			}
			catch (IOException ioe)
			{
				logger.error("", ioe);
			}

			lastFolder = result.getParentFile();

		}
	}
}