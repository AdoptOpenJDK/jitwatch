package org.adoptopenjdk.jitwatch.ui.sandbox;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.adoptopenjdk.jitwatch.loader.ResourceLoader;
import org.adoptopenjdk.jitwatch.sandbox.Sandbox;
import org.adoptopenjdk.jitwatch.ui.Dialogs;
import org.adoptopenjdk.jitwatch.ui.Dialogs.Response;
import org.adoptopenjdk.jitwatch.util.StringUtil;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

public class EditorPane extends VBox
{
	private Label lblTitle;

	private TextArea taSource;
	private TextArea taLineNumbers;

	private ScrollBar sbSource;
	private ScrollBar sbLineNum;
	private boolean scrollLinked = false;

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
				taSource.setText(S_EMPTY);
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

		taSource = new TextArea();
		String styleSource = "-fx-font-family:monospace; -fx-font-size:12px; -fx-background-color:white;";
		taSource.setStyle(styleSource);

		taSource.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue)
			{
				setModified(true);
			}
		});

		taLineNumbers = new TextArea();
		String styleLineNumber = "-fx-padding:0; -fx-font-family:monospace; -fx-font-size:12px; -fx-background-color:#eeeeee;";
		taLineNumbers.setStyle(styleLineNumber);
		taLineNumbers.setEditable(false);
		taLineNumbers.setWrapText(true);

		HBox hBoxTextAreas = new HBox();

		hBoxTextAreas.getChildren().add(taLineNumbers);
		hBoxTextAreas.getChildren().add(taSource);

		taSource.prefWidthProperty().bind(hBoxTextAreas.widthProperty());

		getChildren().add(hBoxTitle);
		getChildren().add(hBoxTextAreas);

		hBoxTitle.prefWidthProperty().bind(widthProperty());
		hBoxTitle.prefHeightProperty().bind(heightProperty().multiply(0.1));

		hBoxTextAreas.prefWidthProperty().bind(widthProperty());
		hBoxTextAreas.prefHeightProperty().bind(heightProperty().multiply(0.9));
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

				int breakCount = 1 + countChar(taSource.getText(), C_NEWLINE);

				generateLineNumbers(breakCount);
			}
		});
	}

	private void generateLineNumbers(int breakCount)
	{
		StringBuilder builder = new StringBuilder();

		final int maxDigits = Integer.toString(breakCount).length();

		for (int i = 0; i < breakCount; i++)
		{
			String marker = StringUtil.padLineNumber(i + 1, maxDigits);

			builder.append(marker).append(C_NEWLINE);
		}

		taLineNumbers.setText(builder.toString());
		taLineNumbers.setMaxWidth(40 + maxDigits * 25);

		sbSource = (ScrollBar) taSource.lookup(".scroll-bar:vertical");
		sbLineNum = (ScrollBar) taLineNumbers.lookup(".scroll-bar:vertical");

		if (sbLineNum != null)
		{
			sbLineNum.setOpacity(0.0);
		}

		if (sbSource != null && sbLineNum != null && !scrollLinked)
		{
			linkScrollBars();
		}
	}

	private void linkScrollBars()
	{
		sbSource.valueProperty().addListener(new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> value, Number oldValue, Number newValue)
			{
				sbLineNum.setValue(newValue.doubleValue());
			}
		});

		scrollLinked = true;
	}

	private int countChar(String text, char countChar)
	{
		int result = 0;

		for (int i = 0; i < text.length(); i++)
		{
			char c = text.charAt(i);

			if (c == countChar)
			{
				result++;
			}
		}

		return result;
	}

	public String getSource()
	{
		return taSource.getText().trim();
	}

	public void loadSource(File dir, String filename)
	{
		sourceFile = new File(dir, filename);

		String source = ResourceLoader.readFile(sourceFile);

		if (source != null)
		{
			source = source.replace("\t", "    ");

			lblTitle.setText(filename);
			taSource.setText(source.trim());

			setModified(false);
		}
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