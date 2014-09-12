/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.sandbox;

import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.ui.IStageCloseListener;
import org.adoptopenjdk.jitwatch.ui.StageManager;

public class VMLanguageList extends VBox implements IStageCloseListener
{
	private ListView<Label> languageList;

	private VBox vboxButtons;

	private JITWatchConfig config;

	private VMLanguageConfigStage openVMLCStage = null;

	public VMLanguageList(String title, final JITWatchConfig config)
	{
		this.config = config;

		HBox hbox = new HBox();

		languageList = new ListView<Label>();

		updateList();

		Button btnOpenFileDialog = new Button("Add");
		btnOpenFileDialog.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				addLanguage();
			}
		});

		Button btnOpenFolderDialog = new Button("Edit");
		btnOpenFolderDialog.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				editLanguage();
			}
		});

		Button btnRemove = new Button("Remove");
		btnRemove.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				Label selected = languageList.getSelectionModel().getSelectedItem();

				if (selected != null)
				{
					String language = selected.getText();
					config.removeVMLanguage(language);
					updateList();
				}
			}
		});

		vboxButtons = new VBox();
		vboxButtons.setPadding(new Insets(10));
		vboxButtons.setSpacing(10);

		vboxButtons.getChildren().add(btnOpenFileDialog);
		vboxButtons.getChildren().add(btnOpenFolderDialog);
		vboxButtons.getChildren().add(btnRemove);

		hbox.getChildren().add(languageList);
		hbox.getChildren().add(vboxButtons);

		languageList.prefWidthProperty().bind(this.widthProperty().multiply(0.8));
		vboxButtons.prefWidthProperty().bind(this.widthProperty().multiply(0.2));

		Label titleLabel = new Label(title);

		getChildren().add(titleLabel);
		getChildren().add(hbox);

		setSpacing(10);
	}

	private void addLanguage()
	{
		openVMLCStage(null);
	}

	private void editLanguage()
	{
		Label selected = languageList.getSelectionModel().getSelectedItem();

		if (selected != null)
		{
			String language = selected.getText();

			openVMLCStage(language);
		}
	}

	private void openVMLCStage(String language)
	{
		if (openVMLCStage == null)
		{
			openVMLCStage = new VMLanguageConfigStage(this, config, language);
			StageManager.addAndShow(openVMLCStage);
		}
	}

	@Override
	public void handleStageClosed(Stage stage)
	{
		openVMLCStage = null;
		updateList();
	}

	private void updateList()
	{
		List<String> vmLanguageList = config.getVMLanguageList();

		languageList.getItems().clear();

		for (String lang : vmLanguageList)
		{
			languageList.getItems().add(new Label(lang));
		}
	}
}