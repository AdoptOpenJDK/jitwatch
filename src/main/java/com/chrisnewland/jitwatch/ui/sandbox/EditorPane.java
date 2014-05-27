package com.chrisnewland.jitwatch.ui.sandbox;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.chrisnewland.jitwatch.loader.ResourceLoader;
import com.chrisnewland.jitwatch.sandbox.Sandbox;
import com.chrisnewland.jitwatch.ui.Dialogs;
import com.chrisnewland.jitwatch.ui.Dialogs.Response;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public class EditorPane extends VBox
{
	private Label lblTitle;
	private TextArea textArea;
	private HBox hBoxTitle;

	private Button btnSave;

	private SandboxStage sandboxStage;

	private boolean isModified = false;

	private File sourceFile = null;

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
				promptSave();
				
				chooseFile();
			}
		});

		btnSave = new Button("Save");
		btnSave.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				saveFile();
			}
		});

		btnSave.setDisable(!isModified);

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
				promptSave();

				sandboxStage.editorClosed(EditorPane.this);
			}
		});

		hBoxTitle = new HBox();
		hBoxTitle.setSpacing(10);
		hBoxTitle.setPadding(new Insets(0, 10, 0, 10));
		hBoxTitle.getChildren().add(lblTitle);
		hBoxTitle.getChildren().add(btnOpen);
		hBoxTitle.getChildren().add(btnSave);
		hBoxTitle.getChildren().add(btnClear);
		hBoxTitle.getChildren().add(btnClose);

		hBoxTitle.setStyle("-fx-background-color:#dddddd; -fx-padding:4px;");

		textArea = new TextArea();
		String style = "-fx-font-family:monospace; -fx-font-size:12px; -fx-background-color:white;";
		textArea.setStyle(style);

		textArea.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue)
			{
				setModified(true);
			}
		});

		getChildren().add(hBoxTitle);
		getChildren().add(textArea);

		hBoxTitle.prefWidthProperty().bind(widthProperty());
		hBoxTitle.prefHeightProperty().bind(heightProperty().multiply(0.1));

		textArea.prefWidthProperty().bind(widthProperty());
		textArea.prefHeightProperty().bind(heightProperty().multiply(0.9));
	}

	private void setModified(final boolean modified)
	{
		this.isModified = modified;
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				btnSave.setDisable(!modified);
			}
		});
	}

	public String getSource()
	{
		return textArea.getText().trim();
	}

	public void loadSource(File dir, String filename)
	{
		sourceFile = new File(dir, filename);

		String source = ResourceLoader.readFile(sourceFile);

		source = source.replace("\t", "    ");

		lblTitle.setText(filename);
		textArea.setText(source);

		setModified(false);
	}

	private void chooseFile()
	{
		FileChooser fc = new FileChooser();
		fc.setTitle("Choose source file");

		fc.setInitialDirectory(Sandbox.SANDBOX_SOURCE_DIR.toFile());

		File result = fc.showOpenDialog(sandboxStage);

		if (result != null)
		{
			loadSource(result.getParentFile(), result.getName());
		}
	}

	public void promptSave()
	{
		if (isModified)
		{
			Response resp = Dialogs.showYesNoDialog(sandboxStage, "Save modified file?", "Save changes?");

			if (resp == Response.YES)
			{
				saveFile();
			}
		}
	}

	private void saveFile()
	{
		if (sourceFile == null)
		{
			FileChooser fc = new FileChooser();
			fc.setTitle("Save file as");

			fc.setInitialDirectory(Sandbox.SANDBOX_SOURCE_DIR.toFile());

			sourceFile = fc.showSaveDialog(sandboxStage);
		}

		if (sourceFile != null)
		{
			saveFile(sourceFile);
			setModified(false);
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

	public boolean isModified()
	{
		return isModified;
	}
}