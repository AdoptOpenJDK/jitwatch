/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/blob/master/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
package com.chrisnewland.jitwatch.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.chrisnewland.jitwatch.core.JITWatchConstants;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class TextViewerStage extends Stage
{
	private String[] lines;

	private ScrollPane scrollPane;
	private VBox vBoxRows;

	private static final String COLOUR_BLACK = "black";
	private static final String COLOUR_RED = "red";
	private static final String COLOUR_GREEN = "green";
	private static final String COLOUR_BLUE = "blue";

	// make this a TextFlow in Java8
	public TextViewerStage(final JITWatchUI parent, String title, String source, boolean showLineNumbers, boolean highlighting)
	{
		initStyle(StageStyle.DECORATED);

		setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				parent.handleStageClosed(TextViewerStage.this);
			}
		});

		if (source == null)
		{
			source = "Empty";
		}

		source = source.replace("\t", "    "); // 4 spaces

		lines = source.split("\n");

		int max = 0;

		int maxWidth = Integer.toString(lines.length).length();

		scrollPane = new ScrollPane();
		scrollPane.setStyle("-fx-background-color:white");
		
		vBoxRows = new VBox();

		scrollPane.setContent(vBoxRows);

		for (int i = 0; i < lines.length; i++)
		{
			String row = lines[i];

			if (showLineNumbers)
			{
				lines[i] = padLineNumber(i + 1, maxWidth) + "  " + row;
			}

			int rowLen = row.length();

			if (rowLen > max)
			{
				max = rowLen;
			}

			Text lineText = new Text(lines[i]);

			String style = "-fx-font-family: monospace; -fx-font-size:12px; -fx-fill:";

			String colour = null;

			if (highlighting)
			{
				if (lines[i].contains("<" + JITWatchConstants.TAG_INLINE_FAIL))
				{
					colour = COLOUR_RED;
				}
				else if (lines[i].contains("<" + JITWatchConstants.TAG_INLINE_SUCCESS))
				{
					colour = COLOUR_GREEN;
				}
				else if (lines[i].contains("<" + JITWatchConstants.TAG_INTRINSIC))
				{
					colour = COLOUR_BLUE;
				}
				else
				{
					colour = COLOUR_BLACK;
				}
			}
			else
			{
				colour = COLOUR_BLACK;
			}

			lineText.setStyle(style + colour);

			vBoxRows.getChildren().add(lineText);
		}

		int x = Math.min(80, max);
		int y = Math.min(30, lines.length);

		x = Math.max(x, 20);
		y = Math.max(y, 20);

		setUpContextMenu();

		setTitle(title);

		Scene scene = new Scene(scrollPane, x * 12, y * 19);

		setScene(scene);
	}

	private String padLineNumber(int number, int maxWidth)
	{
		int len = Integer.toString(number).length();

		StringBuilder builder = new StringBuilder();

		for (int i = len; i < maxWidth; i++)
		{
			builder.append(' ');
		}

		builder.append(number);

		return builder.toString();
	}

	private void setUpContextMenu()
	{

		final ContextMenu contextMenu = new ContextMenu();

		MenuItem menuItemCopyToClipboard = new MenuItem("Copy to Clipboard");

		contextMenu.getItems().add(menuItemCopyToClipboard);

		vBoxRows.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent e)
			{
				if (e.getButton() == MouseButton.SECONDARY)
				{
					contextMenu.show(vBoxRows, e.getScreenX(), e.getScreenY());
				}
			}
		});

		menuItemCopyToClipboard.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				final Clipboard clipboard = Clipboard.getSystemClipboard();
				final ClipboardContent content = new ClipboardContent();

				StringBuilder builder = new StringBuilder();

				for (int i = 0; i < lines.length; i++)
				{
					builder.append(lines[i]).append("\n");
				}

				content.putString(builder.toString());

				clipboard.setContent(content);
			}
		});

	}

	public void jumpTo(final String regex)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				int pos = 0;

				for (String line : lines)
				{
					Matcher matcher = Pattern.compile(regex).matcher(line);
					if (matcher.find())
					{
						break;
					}

					pos++;
				}

				final double scrollPos = (double) pos / (double) lines.length * (scrollPane.getVmax() - scrollPane.getVmin());

				// needed as SelectionModel selected index
				// is not updated instantly on select()
				Platform.runLater(new Runnable()
				{
					@Override
					public void run()
					{
						scrollPane.setVvalue(scrollPos);
					}
				});
			}
		});
	}
}