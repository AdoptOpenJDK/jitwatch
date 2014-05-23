package com.chrisnewland.jitwatch.ui.sandbox;

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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.S_EMPTY;

public class EditorPane extends VBox
{
    private static final int TEN_SPACES = 10;
    private static final int TOP_POSITION = 0;
    private static final int RIGHT_POSITION = 10;
    private static final int BOTTOM_POSITION = 0;
    private static final int LEFT_POSITION = 10;
    private static final double TITLE_HEIGHT_MULTIPLIER = 0.1;
    private static final double TEXTAREA_HEIGHT_MULTIPLIER = 0.9;

    private Label lblTitle;
	private TextArea textArea;
	private HBox hBoxTitle;

	private SandboxStage sandboxStage;

	public EditorPane(SandboxStage stage)
	{
		this.sandboxStage = stage;

		lblTitle = new Label("New File");

		Button btnOpen = new Button("Open");
		btnOpen.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				chooseFile();
			}
		});

		Button btnSave = new Button("Save");
		btnSave.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				saveFile();
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

		Button btnClose = new Button("Close");
		btnClose.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				sandboxStage.editorClosed(EditorPane.this);
			}
		});

		hBoxTitle = new HBox();
		hBoxTitle.setSpacing(TEN_SPACES);
		hBoxTitle.setPadding(new Insets(
                TOP_POSITION,
                RIGHT_POSITION,
                BOTTOM_POSITION,
                LEFT_POSITION));
		hBoxTitle.getChildren().add(lblTitle);
		hBoxTitle.getChildren().add(btnOpen);
		hBoxTitle.getChildren().add(btnSave);
		hBoxTitle.getChildren().add(btnClear);
		hBoxTitle.getChildren().add(btnClose);

		hBoxTitle.setStyle("-fx-background-color:#dddddd; -fx-padding:4px;");

		textArea = new TextArea();
		String style = "-fx-font-family:monospace; -fx-font-size:12px; -fx-background-color:white;";
		textArea.setStyle(style);

		getChildren().add(hBoxTitle);
		getChildren().add(textArea);

		hBoxTitle.prefWidthProperty().bind(widthProperty());
		hBoxTitle.prefHeightProperty().bind(heightProperty().multiply(TITLE_HEIGHT_MULTIPLIER));

		textArea.prefWidthProperty().bind(widthProperty());
		textArea.prefHeightProperty().bind(heightProperty().multiply(TEXTAREA_HEIGHT_MULTIPLIER));
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

	private void chooseFile()
	{
		FileChooser fc = new FileChooser();
		fc.setTitle("Choose source file");

		fc.setInitialDirectory(SandboxStage.SANDBOX_EXAMPLE_DIR);

		File result = fc.showOpenDialog(sandboxStage);

		if (result != null)
		{
			loadSource(result.getParentFile(), result.getName());
		}
	}

	private void saveFile()
	{
		FileChooser fc = new FileChooser();
		fc.setTitle("Save file as");

		fc.setInitialDirectory(SandboxStage.SANDBOX_EXAMPLE_DIR);

		File result = fc.showSaveDialog(sandboxStage);

		if (result != null)
		{
			saveFile(result);
		}
	}

	private void saveFile(File saveFile)
	{
		FileWriter writer = null;

		try
		{
			writer = new FileWriter(saveFile);
			writer.write(getSource());
			sandboxStage.log("Saved " + saveFile.getCanonicalPath());

		}
		catch (IOException ioe)
		{
			sandboxStage.log("Could not save file");
		}
		finally
		{
			if (writer != null)
			{
				try
				{
					writer.close();
				}
				catch (IOException ioe)
				{
				}
			}
		}
	}
}