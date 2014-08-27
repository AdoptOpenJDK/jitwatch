package org.adoptopenjdk.jitwatch.ui.sandbox;

import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.model.VMLanguageConfig;
import org.adoptopenjdk.jitwatch.ui.FileChooserList;
import org.adoptopenjdk.jitwatch.ui.IStageCloseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class VMLanguageConfigStage extends Stage
{
	private static final String DEFAULT_DISPLAY_STYLE = "-fx-padding:0px 8px 0px 0px";

	private static final int labelWidth = 150;
	
	private TextField tfLanguage;
	private TextField tfCompilerPath;
	private TextField tfExecutorPath;

	private static final Logger logger = LoggerFactory.getLogger(VMLanguageConfigStage.class);

	public VMLanguageConfigStage(final IStageCloseListener parent, final JITWatchConfig config, final String language)
	{
		initStyle(StageStyle.UTILITY);

		VBox vbox = new VBox();

		vbox.setPadding(new Insets(15));
		vbox.setSpacing(15);
		
		tfLanguage = new TextField();
		tfCompilerPath = new TextField();
		tfExecutorPath = new TextField();
		
		vbox.getChildren().add(tfLanguage);
		vbox.getChildren().add(tfCompilerPath);
		vbox.getChildren().add(tfExecutorPath);
		
		if (language != null)
		{

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

		hboxCompilerSettings.setSpacing(20);
		
		vbox.getChildren().add(hboxCompilerSettings);

		vbox.getChildren().add(hboxButtons);

		setTitle("VM Language Configuration");

		Scene scene = new Scene(vbox, 720, 460);

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
				//save stuff

				parent.handleStageClosed(VMLanguageConfigStage.this);
				close();
			}
		};
	}
}
