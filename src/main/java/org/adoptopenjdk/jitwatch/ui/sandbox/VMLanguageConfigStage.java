/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.sandbox;

import java.io.File;
import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.ui.IStageCloseListener;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class VMLanguageConfigStage extends Stage
{
	private File lastFolder = null;

	private TextField tfLanguage;
	private TextField tfCompilerPath;
	private TextField tfRuntimePath;

	public VMLanguageConfigStage(final IStageCloseListener parent, final JITWatchConfig config, final String language)
	{
		initStyle(StageStyle.UTILITY);

		VBox vbox = new VBox();

		vbox.setPadding(new Insets(15));
		vbox.setSpacing(15);

		tfLanguage = new TextField();
		tfCompilerPath = new TextField();
		tfRuntimePath = new TextField();

		vbox.getChildren().add(new Label("Language Name"));
		vbox.getChildren().add(tfLanguage);

		Button btnChooseCompilerPath = new Button("Choose");
		btnChooseCompilerPath.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				File compilerPath = chooseFile("Compiler");

				if (compilerPath != null)
				{
					tfCompilerPath.setText(compilerPath.getAbsolutePath());
				}
			}
		});

		vbox.getChildren().add(new Label("Compiler Path"));
		HBox hboxCompiler = new HBox();
		hboxCompiler.getChildren().add(tfCompilerPath);
		hboxCompiler.getChildren().add(btnChooseCompilerPath);
		
		tfCompilerPath.prefWidthProperty().bind(hboxCompiler.widthProperty().multiply(0.8));
		btnChooseCompilerPath.prefWidthProperty().bind(hboxCompiler.widthProperty().multiply(0.2));
		
		vbox.getChildren().add(hboxCompiler);

		Button btnChooseRuntimePath = new Button("Choose");
		btnChooseRuntimePath.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				File runtimePath = chooseFile("Runtime");

				if (runtimePath != null)
				{
					tfRuntimePath.setText(runtimePath.getAbsolutePath());
				}
			}
		});

		vbox.getChildren().add(new Label("Runtime Path"));
		HBox hboxRuntime = new HBox();
		hboxRuntime.getChildren().add(tfRuntimePath);
		hboxRuntime.getChildren().add(btnChooseRuntimePath);
		
		tfRuntimePath.prefWidthProperty().bind(hboxRuntime.widthProperty().multiply(0.8));
		btnChooseRuntimePath.prefWidthProperty().bind(hboxRuntime.widthProperty().multiply(0.2));

		vbox.getChildren().add(hboxRuntime);

		if (language != null)
		{
			tfLanguage.setText(language);
			tfLanguage.setDisable(true);

			tfCompilerPath.setText(config.getVMLanguageCompilerPath(language));
			tfRuntimePath.setText(config.getVMLanguageRuntimePath(language));
		}

		HBox hboxButtons = new HBox();
		hboxButtons.setSpacing(20);
		hboxButtons.setPadding(new Insets(10));
		hboxButtons.setAlignment(Pos.CENTER);

		Button btnSave = new Button("Save");
		Button btnCancel = new Button("Cancel");

		btnSave.setOnAction(getEventHandlerForSaveButton(parent, config));

		btnCancel.setOnAction(getEventHandlerForCancelButton(parent));

		hboxButtons.getChildren().add(btnCancel);
		hboxButtons.getChildren().add(btnSave);

		HBox hboxCompilerSettings = new HBox();

		hboxCompilerSettings.setSpacing(10);

		vbox.getChildren().add(hboxCompilerSettings);

		vbox.getChildren().add(hboxButtons);

		setTitle("VM Language Configuration");

		Scene scene = new Scene(vbox, 480, 240);

		setScene(scene);

		setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				parent.handleStageClosed(VMLanguageConfigStage.this);
			}
		});
	}

	private File chooseFile(String name)
	{
		FileChooser fc = new FileChooser();
		fc.setTitle("Choose " + name);

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

		File result = fc.showOpenDialog(null);

		if (result != null)
		{
			lastFolder = result.getParentFile();
		}

		return result;
	}

	private EventHandler<ActionEvent> getEventHandlerForCancelButton(final IStageCloseListener parent)
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

	private EventHandler<ActionEvent> getEventHandlerForSaveButton(final IStageCloseListener parent, final JITWatchConfig config)
	{
		return new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				String language = tfLanguage.getText();
				String compilerPath = tfCompilerPath.getText();
				String runtimePath = tfRuntimePath.getText();
				
				if (language != null && language.length() > 0)
				{
					config.addOrUpdateVMLanguage(language, compilerPath, runtimePath);
					config.saveConfig();
				}
				
				parent.handleStageClosed(VMLanguageConfigStage.this);
				close();
			}
		};
	}
}
