package com.chrisnewland.jitwatch.ui.browser;

import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

public class Viewer extends VBox
{
	private ScrollPane scrollPane;
	private VBox vBoxRows;

	protected static final String COLOUR_BLACK = "black";
	protected static final String COLOUR_RED = "red";
	protected static final String COLOUR_GREEN = "green";
	protected static final String COLOUR_BLUE = "blue";

	public Viewer()
	{
		vBoxRows = new VBox();

		scrollPane = new ScrollPane();
		scrollPane.setContent(vBoxRows);		
		scrollPane.setStyle("-fx-background-color:white");
		
		scrollPane.prefHeightProperty().bind(heightProperty());
		
		getChildren().add(scrollPane);

		setUpContextMenu();
	}

	public void setContent(String source, boolean showLineNumbers)
	{
		if (source == null)
		{
			source = "Empty";
		}

		source = source.replace("\t", "  "); // 2 spaces

		String[] lines = source.split("\n");

		int maxWidth = Integer.toString(lines.length).length();

		List<Text> textItems = new ArrayList<>();

		for (int i = 0; i < lines.length; i++)
		{
			String row = lines[i];

			if (showLineNumbers)
			{
				lines[i] = padLineNumber(i + 1, maxWidth) + "  " + row;
			}

			Text lineText = new Text(lines[i]);

			String style = "-fx-font-family: monospace; -fx-font-size:12px; -fx-fill: black";

			lineText.setStyle(style);

			textItems.add(lineText);
		}

		setContent(textItems);
	}

	private void setContent(List<Text> items)
	{
		vBoxRows.getChildren().clear();
		vBoxRows.getChildren().addAll(items);
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

				ObservableList<Node> items = vBoxRows.getChildren();

				for (Node text : items)
				{
					String line = ((Text) text).getText();

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

				ObservableList<Node> items = vBoxRows.getChildren();

				Pattern pattern = Pattern.compile(regex);

				for (Node item : items)
				{
					Text text = (Text) item;

					String line = text.getText();

					Matcher matcher = pattern.matcher(line);
					if (matcher.find())
					{
						break;
					}

					pos++;
				}

				final double scrollPos = (double) pos / (double) items.size() * (scrollPane.getVmax() - scrollPane.getVmin());

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