package com.chrisnewland.jitwatch.ui.sandbox;

import java.io.File;

import com.chrisnewland.jitwatch.loader.ResourceLoader;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public class EditorPane extends VBox
{
	private Label lblTitle;
	private TextArea textArea;
	private HBox hBoxTitle;
	
	public EditorPane(final Stage stage)
	{
		lblTitle = new Label();
		
		Button btnOpen = new Button("Open");
		btnOpen.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				chooseFile(stage);
			}
		});
		
		Button btnClear = new Button("Clear");
		btnClear.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				lblTitle.setText("New File");
				textArea.setText(S_EMPTY);
			}
		});
		
		hBoxTitle = new HBox();
		hBoxTitle.setSpacing(10);
		hBoxTitle.setPadding(new Insets(0, 10, 0, 10));
		hBoxTitle.getChildren().add(lblTitle);
		hBoxTitle.getChildren().add(btnOpen);
		hBoxTitle.getChildren().add(btnClear);
		
		hBoxTitle.setStyle("-fx-background-color:#dddddd; -fx-padding:4px;");

		textArea = new TextArea();
		String style = "-fx-font-family:monospace; -fx-font-size:12px; -fx-background-color:white;";
		textArea.setStyle(style);
		
		getChildren().add(hBoxTitle);
		getChildren().add(textArea);
		
		hBoxTitle.prefWidthProperty().bind(widthProperty());
		hBoxTitle.prefHeightProperty().bind(heightProperty().multiply(0.1));

		textArea.prefWidthProperty().bind(widthProperty());
		textArea.prefHeightProperty().bind(heightProperty().multiply(0.9));
	}
	
	public String getSource()
	{
		return textArea.getText().trim();
	}
	
	public void loadSource(File dir, String filename)
	{
		String source = ResourceLoader.readFileInDirectory(dir, filename);
	
		source = source.replace("\t", "    ");
		
		lblTitle.setText(filename);
		textArea.setText(source);
	}
	
	private void chooseFile(Stage stage)
	{
		FileChooser fc = new FileChooser();
		fc.setTitle("Choose source file");
		
		fc.setInitialDirectory(SandboxStage.SANDBOX_EXAMPLE_DIR);

		File result = fc.showOpenDialog(stage);
		
		if (result != null)
		{
			loadSource(result.getParentFile(), result.getName());
		}
	}
}