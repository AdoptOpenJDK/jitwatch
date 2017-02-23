/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.logger.ILogListener;
import org.adoptopenjdk.jitwatch.ui.Dialogs;
import org.adoptopenjdk.jitwatch.ui.Dialogs.Response;
import org.adoptopenjdk.jitwatch.ui.filechooser.FileChooserList;
import org.adoptopenjdk.jitwatch.ui.filechooser.FileChooserListSrcZip;
import org.adoptopenjdk.jitwatch.ui.stage.IStageClosedListener;
import org.adoptopenjdk.jitwatch.ui.stage.StageManager;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainConfigStage extends Stage
{
	private ObservableList<String> profileList = FXCollections.observableArrayList();

	private FileChooserList chooserSource;
	private FileChooserList chooserClasses;

	private static final Logger logger = LoggerFactory.getLogger(MainConfigStage.class);

	public MainConfigStage(final IStageClosedListener parent2, final ILogListener logListener, final JITWatchConfig config)
	{
		initStyle(StageStyle.UTILITY);

		VBox vbox = new VBox();

		vbox.setPadding(new Insets(10));
		vbox.setSpacing(10);

		chooserSource = new FileChooserListSrcZip(this, "Source locations", config.getSourceLocations());
		chooserClasses = new FileChooserList(this, "Class locations", config.getConfiguredClassLocations());

		final ComboBox<String> comboBox = new ComboBox<>(profileList);

		createComboOptions(config);

		String profileName = config.getProfileName();

		comboBox.setValue(profileName);

		comboBox.valueProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal)
			{
				if (newVal != null)
				{
					logger.debug("changed({})", newVal);
					logListener.handleLogEntry("Using Config: " + newVal);
					config.setProfileName(newVal);

					chooserSource.setItems(config.getSourceLocations());
					chooserClasses.setItems(config.getConfiguredClassLocations());
				}
			}
		});

		Button btnNewProfile = new Button("New");

		btnNewProfile.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				Response resp = Dialogs.showTextInputDialog(MainConfigStage.this, "Enter Profile Name", S_EMPTY);

				if (resp == Response.YES)
				{
					String name = Dialogs.getTextInput();

					if (name != null)
					{
						if (DEBUG_LOGGING)
						{
							logger.debug("setting new name: {}", name);
						}

						config.setProfileName(name);

						chooserSource.setItems(config.getSourceLocations());
						chooserClasses.setItems(config.getConfiguredClassLocations());

						createComboOptions(config);

						if (DEBUG_LOGGING)
						{
							logger.debug("setting combo name: {}", name);
						}

						comboBox.setValue(name);
					}
				}
			}
		});

		Button btnDelete = new Button("Delete");

		btnDelete.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				String profileName = comboBox.getValue();

				if (profileName != null && !config.isBuiltInProfile(profileName))
				{
					Response resp = Dialogs.showYesNoDialog(MainConfigStage.this, "Really Delete Profile?", "Delete profile '"
							+ profileName + C_QUOTE);

					if (resp == Response.YES)
					{
						if (DEBUG_LOGGING)
						{
							logger.debug("deleting: {}", profileName);
						}
						config.deleteProfile(profileName);
						config.setProfileName(S_PROFILE_DEFAULT);

						chooserSource.setItems(config.getSourceLocations());
						chooserClasses.setItems(config.getConfiguredClassLocations());

						createComboOptions(config);

						comboBox.setValue(S_PROFILE_DEFAULT);
					}
				}
				else
				{
					Dialogs.showOKDialog(MainConfigStage.this, "Cannot delete profile", "Cannot delete built-in profile '"
							+ profileName + "'");
				}
			}
		});

		Button btnSave = new Button("Save");

		btnSave.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				config.setSourceLocations(chooserSource.getFiles());
				config.setClassLocations(chooserClasses.getFiles());

				config.saveConfig();

				StageManager.closeStage(MainConfigStage.this);
				
			}
		});

		Button btnCancel = new Button("Cancel");

		btnCancel.setOnAction(StageManager.getCloseHandler(MainConfigStage.this));

		HBox hboxButtons = new HBox();
		hboxButtons.setSpacing(20);
		hboxButtons.setPadding(new Insets(10));
		hboxButtons.setAlignment(Pos.BASELINE_LEFT);

		hboxButtons.getChildren().add(new Label("Profile:"));
		hboxButtons.getChildren().add(comboBox);
		hboxButtons.getChildren().add(btnNewProfile);
		hboxButtons.getChildren().add(btnDelete);
		hboxButtons.getChildren().add(btnCancel);
		hboxButtons.getChildren().add(btnSave);

		vbox.getChildren().add(chooserSource);
		vbox.getChildren().add(chooserClasses);

		vbox.getChildren().add(hboxButtons);

		chooserSource.prefHeightProperty().bind(this.heightProperty().multiply(0.5));
		chooserClasses.prefHeightProperty().bind(this.heightProperty().multiply(0.5));
		hboxButtons.setPrefHeight(30);

		setTitle("JITWatch Configuration");

		Scene scene = UserInterfaceUtil.getScene(vbox, 640, 400);

		setScene(scene);
	}

	private void createComboOptions(JITWatchConfig config)
	{
		if (DEBUG_LOGGING)
		{
			logger.debug("createComboOptions");
		}

		profileList.clear();

		List<String> configNameList = new ArrayList<String>(config.getProfileNames());

		Collections.sort(configNameList);

		if (DEBUG_LOGGING)
		{
			for (String name : configNameList)
			{
				logger.debug("option: {}", name);
			}
		}

		profileList.addAll(configNameList);
	}
}