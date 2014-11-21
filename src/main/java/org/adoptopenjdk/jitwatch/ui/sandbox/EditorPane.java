/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.sandbox;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_DOT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import org.adoptopenjdk.jitwatch.loader.ResourceLoader;
import org.adoptopenjdk.jitwatch.sandbox.Sandbox;
import org.adoptopenjdk.jitwatch.ui.Dialogs;
import org.adoptopenjdk.jitwatch.ui.Dialogs.Response;
import org.adoptopenjdk.jitwatch.ui.StyleUtil;
import org.adoptopenjdk.jitwatch.util.StringUtil;

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

	private ISandboxStage sandboxStage;

	private boolean isModified = false;

	private File sourceFile = null;

	public EditorPane(ISandboxStage stage)
	{
		this.sandboxStage = stage;

		lblTitle = new Label("New File");

		Button btnOpen = StyleUtil.buildButton("Open");
		btnOpen.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				promptSave();

				chooseFile();
			}
		});

		btnSave = StyleUtil.buildButton("Save");
		btnSave.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				saveFile();
			}
		});

		btnSave.setDisable(!isModified);

		Button btnClear = StyleUtil.buildButton("Clear");
		btnClear.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				lblTitle.setText("New File");
				taSource.setText(S_EMPTY);
			}
		});

		Button btnClose = StyleUtil.buildButton("Close");
		btnClose.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				promptSave();

				sandboxStage.editorClosed(EditorPane.this);
			}
		});

		Button btnRun = StyleUtil.buildButton("Run");
		btnRun.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				if (isModified)
				{
					promptSave();
				}

				setVMLanguage();

				sandboxStage.runFile(EditorPane.this);
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
		hBoxTitle.getChildren().add(btnRun);

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

		taSource.focusedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldValue, Boolean newValue)
			{
				if (Boolean.TRUE.equals(newValue))
				{
					setVMLanguage();
				}
			}
		});

		setTextAreaSaveCombo(taSource);

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

	private void setTextAreaSaveCombo(TextArea textArea)
	{
		textArea.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>()
		{
			final KeyCombination combo = new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN);

			@Override
			public void handle(KeyEvent event)
			{
				// check for only tab key
				if (combo.match(event))
				{
					saveFile();
					event.consume();
				}
			}
		});
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

	public File getSourceFile()
	{
		return sourceFile;
	}

	public void loadSource(File filename)
	{
		sourceFile = filename;

		if (sourceFile != null)
		{
			lblTitle.setText(sourceFile.getName());

			// add parent folder so source can be loaded in TriView
			sandboxStage.addSourceFolder(sourceFile.getParentFile());

			String source = ResourceLoader.readFile(sourceFile);

			if (source != null)
			{
				source = source.replace("\t", "    ");

				taSource.setText(source.trim());

				setModified(false);

				setVMLanguage();
			}
		}
	}

	private void setVMLanguage()
	{
		if (sourceFile != null)
		{
			int lastDotPos = sourceFile.getName().lastIndexOf(C_DOT);

			if (lastDotPos != -1)
			{
				String fileExtension = sourceFile.getName().substring(lastDotPos + 1);

				sandboxStage.setVMLanguageFromFileExtension(fileExtension);
			}
		}
	}

	private void chooseFile()
	{
		FileChooser fc = new FileChooser();
		fc.setTitle("Choose source file");

		fc.setInitialDirectory(Sandbox.SANDBOX_SOURCE_DIR.toFile());

		File result = fc.showOpenDialog(sandboxStage.getStageForChooser());

		if (result != null)
		{
			loadSource(result);
		}
	}

	public void promptSave()
	{
		if (isModified)
		{
			Response resp = Dialogs.showYesNoDialog(sandboxStage.getStageForChooser(), "Save modified file?", "Save changes?");

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

			sourceFile = fc.showSaveDialog(sandboxStage.getStageForChooser());
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