/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.sandbox;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_NEWLINE;
import static org.adoptopenjdk.jitwatch.util.UserInterfaceUtil.FONT_MONOSPACE_FAMILY;
import static org.adoptopenjdk.jitwatch.util.UserInterfaceUtil.FONT_MONOSPACE_SIZE;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import org.adoptopenjdk.jitwatch.loader.ResourceLoader;
import org.adoptopenjdk.jitwatch.sandbox.Sandbox;
import org.adoptopenjdk.jitwatch.ui.Dialogs;
import org.adoptopenjdk.jitwatch.ui.Dialogs.Response;
import org.adoptopenjdk.jitwatch.util.StringUtil;

public class EditorPane extends VBox
{
	private SplitPane spTextAreas;

	private TextArea taSource;
	private TextArea taLineNumbers;

	private ScrollBar sbSource;
	private ScrollBar sbLineNum;
	private boolean scrollLinked = false;

	private ISandboxStage sandboxStage;

	private boolean isModified = false;

	private File sourceFile = null;

	public EditorPane(ISandboxStage stage)
	{
		this.sandboxStage = stage;

		taSource = new TextArea();

		String styleSource = "-fx-font-family:" + FONT_MONOSPACE_FAMILY + "; -fx-font-size:" + FONT_MONOSPACE_SIZE
				+ "px; -fx-background-color:white;";

		taSource.setStyle(styleSource);

		taSource.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue)
			{
				setModified(true);
			}
		});

		setTextAreaSaveCombo(taSource);

		String styleLineNumber = "-fx-padding:0; -fx-font-family:" + FONT_MONOSPACE_FAMILY + "; -fx-font-size:"
				+ FONT_MONOSPACE_SIZE + "px; -fx-background-color:#eeeeee;";

		taLineNumbers = new TextArea();
		taLineNumbers.setStyle(styleLineNumber);
		taLineNumbers.setEditable(false);
		taLineNumbers.setWrapText(true);

		spTextAreas = new SplitPane();
		spTextAreas.setOrientation(Orientation.HORIZONTAL);
		spTextAreas.getItems().add(taLineNumbers);
		spTextAreas.getItems().add(taSource);
		spTextAreas.setStyle("-fx-background-color: white");

		taSource.prefWidthProperty().bind(spTextAreas.widthProperty());

		getChildren().add(spTextAreas);

		spTextAreas.prefHeightProperty().bind(heightProperty());

		generateLineNumbers(1);
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
				int breakCount = 1 + countChar(taSource.getText(), C_NEWLINE);

				generateLineNumbers(breakCount);
			}
		});

		sandboxStage.setModified(this, isModified);
	}

	public boolean isModified()
	{
		return isModified;
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

		double minWidth = 16 + maxDigits * 12;

		taLineNumbers.setText(builder.toString());
		taLineNumbers.setMinWidth(minWidth);
		taLineNumbers.setMaxWidth(minWidth + 16);
		taLineNumbers.setPrefWidth(minWidth);

		if (sbSource == null)
		{
			sbSource = (ScrollBar) taSource.lookup(".scroll-bar:vertical");
		}

		if (sbLineNum == null)
		{
			sbLineNum = (ScrollBar) taLineNumbers.lookup(".scroll-bar:vertical");
		}

		if (sbLineNum != null)
		{
			sbLineNum.setOpacity(0.0);
			sbLineNum.setStyle("-fx-background-color:white");
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
		
		sbLineNum.valueProperty().addListener(new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> value, Number oldValue, Number newValue)
			{
				sbSource.setValue(newValue.doubleValue());
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

	public String getName()
	{
		String result;

		if (sourceFile == null)
		{
			result = "New";
		}
		else
		{
			result = sourceFile.getName();
		}

		return result;
	}

	public void loadSource(File filename)
	{
		sourceFile = filename;

		if (sourceFile != null)
		{
			// add parent folder so source can be loaded in TriView
			sandboxStage.addSourceFolder(sourceFile.getParentFile());

			String source = ResourceLoader.readFile(sourceFile);

			if (source != null)
			{
				source = source.replace("\t", "    ");

				taSource.setText(source.trim());

				setModified(false);
			}
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

	public void saveFile()
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
			
			sourceFile = saveFile;
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