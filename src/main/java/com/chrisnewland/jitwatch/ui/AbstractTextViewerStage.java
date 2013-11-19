/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/blob/master/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
package com.chrisnewland.jitwatch.ui;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
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

public abstract class AbstractTextViewerStage extends Stage
{
	private ScrollPane scrollPane;
	private VBox vBoxRows;

	protected static final String COLOUR_BLACK = "black";
	protected static final String COLOUR_RED = "red";
	protected static final String COLOUR_GREEN = "green";
	protected static final String COLOUR_BLUE = "blue";

	// make this a TextFlow in Java8
	public AbstractTextViewerStage(final JITWatchUI parent, String title)
	{
		initStyle(StageStyle.DECORATED);

		setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				parent.handleStageClosed(AbstractTextViewerStage.this);
			}
		});
		
		vBoxRows = new VBox();

		scrollPane = new ScrollPane();
		scrollPane.setContent(vBoxRows);		
		scrollPane.setStyle("-fx-background-color:white");

		setUpContextMenu();

		setTitle(title);

		Scene scene = new Scene(scrollPane, 640, 480);

		setScene(scene);
	}
	
	protected void setContent(List<Text> textItems, int maxLineLength)
	{
		vBoxRows.getChildren().addAll(textItems);
		
		int x = Math.min(80, maxLineLength);
		int y = Math.min(30, textItems.size());

		x = Math.max(x, 20);
		y = Math.max(y, 20);
		
		setWidth(x*12);
		setHeight(y*19);	
	}

	protected String padLineNumber(int number, int maxWidth)
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

	protected void setUpContextMenu()
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
				
				ObservableList<Node> textItems = vBoxRows.getChildren();

				for (Node text : textItems)
				{
					String line = ((Text)text).getText();
					
					builder.append(line.toString()).append("\n");
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

				ObservableList<Node> textItems = vBoxRows.getChildren();
				
				for (Node text : textItems)
				{
					String line = ((Text)text).getText();
					
					Matcher matcher = Pattern.compile(regex).matcher(line);
					if (matcher.find())
					{
						break;
					}

					pos++;
				}
				
				final double scrollPos = (double) pos / (double) textItems.size() * (scrollPane.getVmax() - scrollPane.getVmin());

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