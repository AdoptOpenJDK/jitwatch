/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.filechooser;

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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileChooserList extends VBox
{
	private static final Logger logger = LoggerFactory.getLogger(FileChooserList.class);

	private Stage stage;

	protected ListView<Label> fileList;

	private File lastFolder = null;

	protected VBox vboxButtons;

	public FileChooserList(Stage stage, String title, List<String> items)
	{
		this.stage = stage;

		HBox hbox = new HBox();

		fileList = new ListView<Label>();

		setItems(items);

		Button btnOpenFileDialog = new Button("Add File(s)");
		btnOpenFileDialog.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				chooseFile();
			}
		});

		Button btnOpenFolderDialog = new Button("Add Folder");
		btnOpenFolderDialog.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				chooseFolder();
			}
		});

		Button btnRemove = new Button("Remove");
		btnRemove.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				Label selected = fileList.getSelectionModel().getSelectedItem();

				if (selected != null)
				{
					fileList.getItems().remove(selected);
				}
			}
		});

		vboxButtons = new VBox();
		vboxButtons.setPadding(new Insets(0,10,10,10));
		vboxButtons.setSpacing(10);

		vboxButtons.getChildren().add(btnOpenFileDialog);
		vboxButtons.getChildren().add(btnOpenFolderDialog);
		vboxButtons.getChildren().add(btnRemove);

		hbox.getChildren().add(fileList);
		hbox.getChildren().add(vboxButtons);

		fileList.prefWidthProperty().bind(this.widthProperty().multiply(0.8));
		vboxButtons.prefWidthProperty().bind(this.widthProperty().multiply(0.2));

		Label titleLabel = new Label(title);

		getChildren().add(titleLabel);
		getChildren().add(hbox);

		setSpacing(10);
	}

	public void setItems(List<String> items)
	{
		fileList.getItems().clear();

		for (String item : items)
		{
			fileList.getItems().add(new Label(item));
		}
	}

	private void chooseFile()
	{
		FileChooser fc = new FileChooser();
		fc.setTitle("Choose File(s)");

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

		List<File> result = fc.showOpenMultipleDialog(stage);

		if (result != null)
		{
			for (File f : result)
			{
				try
				{
					fileList.getItems().add(new Label(f.getCanonicalPath()));
				}
				catch (IOException ioe)
				{
					logger.error("", ioe);
				}

				lastFolder = f.getParentFile();
			}
		}
	}

	private void chooseFolder()
	{
		DirectoryChooser dc = new DirectoryChooser();
		dc.setTitle("Choose Folder");

		File dirFile = null;

		if (lastFolder == null)
		{
			dirFile = new File(System.getProperty("user.dir"));
		}
		else
		{
			dirFile = lastFolder;
		}

		dc.setInitialDirectory(dirFile);

		File result = dc.showDialog(stage);

		if (result != null)
		{
			try
			{
				String path = result.getCanonicalPath();

				addPathToList(path);
			}
			catch (IOException ioe)
			{
				logger.error("", ioe);
			}

			lastFolder = result.getParentFile();
		}
	}

	public List<String> getFiles()
	{
		List<String> result = new ArrayList<>();

		for (Label label : fileList.getItems())
		{
			result.add(label.getText());
		}

		return result;
	}

	protected void addPathToList(String path)
	{
		boolean found = false;

		for (Label label : fileList.getItems())
		{
			if (path.equals(label.getText()))
			{
				found = true;
				break;
			}
		}

		if (!found)
		{
			fileList.getItems().add(new Label(path));
		}
	}
}