/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.sandbox;

import java.io.File;

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

import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.ui.IStageCloseListener;
import org.adoptopenjdk.jitwatch.ui.StyleUtil;

public class VMLanguageConfigStage extends Stage
{
	private File lastFolder = null;

	private TextField tfLanguage;
	private TextField tfCompilerPath;
	private TextField tfRuntimePath;

	private IStageCloseListener parent;
	private JITWatchConfig config;

	public VMLanguageConfigStage(final IStageCloseListener parent, final JITWatchConfig config, final String language)
	{
		this.parent = parent;
		this.config = config;

		initStyle(StageStyle.UTILITY);

		VBox vbox = new VBox();

		vbox.setPadding(new Insets(15));
		vbox.setSpacing(20);

		vbox.getChildren().add(getVBoxLanguage());
		vbox.getChildren().add(getVBoxCompilerPath());
		vbox.getChildren().add(getVBoxRuntimePath());

		if (language != null)
		{
			tfLanguage.setText(language);
			tfLanguage.setEditable(false);

			tfCompilerPath.setText(config.getVMLanguageCompilerPath(language));
			tfRuntimePath.setText(config.getVMLanguageRuntimePath(language));
		}

		vbox.getChildren().add(getHBoxButtons());

		setTitle("VM Language Configuration");

		Scene scene = new Scene(vbox, 480, 250);

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

	private VBox getVBoxLanguage()
	{
		VBox vbox = new VBox();

		tfLanguage = new TextField();

		vbox.getChildren().add(new Label("Language Name"));
		vbox.getChildren().add(tfLanguage);

		return vbox;
	}

	private VBox getVBoxCompilerPath()
	{
		VBox vbox = new VBox();

		tfCompilerPath = new TextField();

		Button btnChooseCompilerPath = StyleUtil.buildButton("Choose");
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

		HBox hboxCompiler = new HBox();
		hboxCompiler.setSpacing(10);

		hboxCompiler.getChildren().add(tfCompilerPath);
		hboxCompiler.getChildren().add(btnChooseCompilerPath);

		tfCompilerPath.prefWidthProperty().bind(hboxCompiler.widthProperty().multiply(0.8));
		btnChooseCompilerPath.prefWidthProperty().bind(hboxCompiler.widthProperty().multiply(0.2));

		vbox.getChildren().add(new Label("Compiler Path"));
		vbox.getChildren().add(hboxCompiler);

		return vbox;
	}

	private VBox getVBoxRuntimePath()
	{
		VBox vbox = new VBox();

		tfRuntimePath = new TextField();

		Button btnChooseRuntimePath = StyleUtil.buildButton("Choose");
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

		HBox hboxRuntime = new HBox();
		hboxRuntime.setSpacing(10);
		hboxRuntime.getChildren().add(tfRuntimePath);
		hboxRuntime.getChildren().add(btnChooseRuntimePath);

		tfRuntimePath.prefWidthProperty().bind(hboxRuntime.widthProperty().multiply(0.8));
		btnChooseRuntimePath.prefWidthProperty().bind(hboxRuntime.widthProperty().multiply(0.2));

		vbox.getChildren().add(new Label("Runtime Path"));
		vbox.getChildren().add(hboxRuntime);

		return vbox;
	}

	private HBox getHBoxButtons()
	{
		HBox hbox = new HBox();
		hbox.setSpacing(20);
		hbox.setPadding(new Insets(10));
		hbox.setAlignment(Pos.CENTER);

		Button btnSave = StyleUtil.buildButton("Save");
		Button btnCancel = StyleUtil.buildButton("Cancel");

		btnSave.setOnAction(getEventHandlerForSaveButton(parent, config));

		btnCancel.setOnAction(getEventHandlerForCancelButton(parent));

		hbox.getChildren().add(btnCancel);
		hbox.getChildren().add(btnSave);

		return hbox;
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
