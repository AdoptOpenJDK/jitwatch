/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.sandbox;

import java.io.File;

import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.ui.stage.IStageClosedListener;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class VMLanguageConfigStage extends Stage
{
	private File lastFolder = null;

	private TextField tfLanguagePath;

	private IStageClosedListener parent;
	private JITWatchConfig config;

	public VMLanguageConfigStage(final IStageClosedListener parent, final JITWatchConfig config, final String language)
	{
		this.parent = parent;
		this.config = config;

		initStyle(StageStyle.UTILITY);

		VBox vbox = new VBox();

		vbox.setPadding(new Insets(15));
		vbox.setSpacing(10);

		vbox.getChildren().add(getVBoxLanguagePath(language));

		if (language != null)
		{
			tfLanguagePath.setText(config.getVMLanguagePath(language));
		}

		vbox.getChildren().add(getHBoxButtons(language));

		setTitle("VM Language Configuration");

		Scene scene = UserInterfaceUtil.getScene(vbox, 480, 120);

		setScene(scene);
	}


	private VBox getVBoxLanguagePath(final String language)
	{
		VBox vbox = new VBox();

		tfLanguagePath = new TextField();

		Button btnChoosePath = new Button("Choose");
		btnChoosePath.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				File languagePath = chooseFile(language + " Home Directory");

				if (languagePath != null)
				{
					tfLanguagePath.setText(languagePath.getAbsolutePath());
				}
			}
		});

		HBox hboxCompiler = new HBox();
		hboxCompiler.setSpacing(10);

		hboxCompiler.getChildren().add(tfLanguagePath);
		hboxCompiler.getChildren().add(btnChoosePath);

		tfLanguagePath.prefWidthProperty().bind(hboxCompiler.widthProperty().multiply(0.8));
		btnChoosePath.prefWidthProperty().bind(hboxCompiler.widthProperty().multiply(0.2));

		vbox.getChildren().add(new Label(language + " Home Directory"));
		vbox.getChildren().add(hboxCompiler);

		return vbox;
	}

	private HBox getHBoxButtons(String language)
	{
		HBox hbox = new HBox();
		hbox.setSpacing(20);
		hbox.setAlignment(Pos.CENTER);

		Button btnSave = new Button("Save");
		Button btnCancel = new Button("Cancel");

		btnSave.setOnAction(getEventHandlerForSaveButton(language, parent, config));

		btnCancel.setOnAction(getEventHandlerForCancelButton(parent));

		hbox.getChildren().add(btnCancel);
		hbox.getChildren().add(btnSave);

		return hbox;
	}

	private File chooseFile(String name)
	{
		DirectoryChooser dc = new DirectoryChooser();
		dc.setTitle("Choose " + name + " folder");

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

		File result = dc.showDialog(null);

		if (result != null)
		{
			lastFolder = result;
		}

		return result;
	}

	private EventHandler<ActionEvent> getEventHandlerForCancelButton(final IStageClosedListener parent)
	{
		return new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				parent.handleStageClosed(VMLanguageConfigStage.this);
				close();
			}
		};
	}

	private EventHandler<ActionEvent> getEventHandlerForSaveButton(final String language, final IStageClosedListener parent, final JITWatchConfig config)
	{
		return new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				String languagePath = tfLanguagePath.getText();

				if (languagePath != null && languagePath.length() > 0)
				{
					config.addOrUpdateVMLanguage(language, languagePath);
					config.saveConfig();
				}

				parent.handleStageClosed(VMLanguageConfigStage.this);
				close();
			}
		};
	}
}
