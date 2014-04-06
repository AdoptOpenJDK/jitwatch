/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui.triview;

import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.LineAnnotation;
import com.chrisnewland.jitwatch.ui.IStageAccessProxy;
import com.chrisnewland.jitwatch.util.ParseUtil;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class Viewer extends VBox
{
	private ScrollPane scrollPane;
	private VBox vBoxRows;

	public static final String COLOUR_BLACK = "black";
	public static final String COLOUR_RED = "red";
	public static final String COLOUR_GREEN = "green";
	public static final String COLOUR_BLUE = "blue";

	protected int scrollIndex = 0;
	protected int lastScrollIndex = -1;
	protected String originalSource;

	protected static final String STYLE_UNHIGHLIGHTED = "-fx-font-family:monospace; -fx-font-size:12px; -fx-background-color:white;";
	protected static final String STYLE_HIGHLIGHTED = "-fx-font-family:monospace; -fx-font-size:12px; -fx-background-color:red;";

	protected Map<Integer, LineAnnotation> lineAnnotations = new HashMap<>();

	protected IStageAccessProxy stageAccessProxy;
	
	public Viewer(IStageAccessProxy stageAccessProxy)
	{
		this.stageAccessProxy = stageAccessProxy;
		
		vBoxRows = new VBox();

		vBoxRows.heightProperty().addListener(new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number oldValue, Number newValue)
			{
				setScrollBar();
			}
		});

		scrollPane = new ScrollPane();
		scrollPane.setContent(vBoxRows);
		scrollPane.setStyle("-fx-background-color:white");

		scrollPane.prefHeightProperty().bind(heightProperty());

		getChildren().add(scrollPane);

		setUpContextMenu();
	}

	public void setContent(String inSource, boolean showLineNumbers)
	{
        String source = inSource;
		lineAnnotations.clear();
		lastScrollIndex = -1;

		if (source == null)
		{
			source = "Empty";
		}

		originalSource = source;

		source = source.replace(S_TAB, S_DOUBLE_SPACE); // 2 spaces

		String[] lines = source.split(S_NEWLINE);

		int maxWidth = Integer.toString(lines.length).length();

		List<Label> labels = new ArrayList<>();

		for (int i = 0; i < lines.length; i++)
		{
			String row = lines[i];

			if (showLineNumbers)
			{
				lines[i] = padLineNumber(i + 1, maxWidth) + S_DOUBLE_SPACE + row;
			}

			Label lblLine = new Label(lines[i]);

			lblLine.setStyle(STYLE_UNHIGHLIGHTED);

			labels.add(lblLine);
		}

		setContent(labels);
	}

	public void setContent(List<Label> items)
	{
		lineAnnotations.clear();
		lastScrollIndex = -1;

		vBoxRows.getChildren().clear();
		vBoxRows.getChildren().addAll(items);
	}

	public void setLineAnnotations(Map<Integer, LineAnnotation> annotations)
	{
		this.lineAnnotations = annotations;

		for (Map.Entry<Integer, LineAnnotation> entry : annotations.entrySet())
		{
			int lineReference = entry.getKey();
			LineAnnotation la = entry.getValue();
			Color colour = la.getColour();

			Label lblLine = (Label) vBoxRows.getChildren().get(lineReference);
			lblLine.setTooltip(new Tooltip(la.getAnnotation()));
			lblLine.setTextFill(colour);
		}
	}



	private String padLineNumber(int number, int maxWidth)
	{
		int len = Integer.toString(number).length();

		StringBuilder builder = new StringBuilder();

		for (int i = len; i < maxWidth; i++)
		{
			builder.append(S_SPACE);
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

				for (Node item : items)
				{
					String line = ((Label) item).getText();
					builder.append(line).append(S_NEWLINE);
				}

				content.putString(builder.toString());

				clipboard.setContent(content);
			}
		});
	}

	public void jumpTo(IMetaMember member)
	{
		scrollIndex = 0;

		int regexPos = findPosForRegex(member.getSignatureRegEx());

		if (regexPos == -1)
		{
			List<String> lines = Arrays.asList(originalSource.split(S_NEWLINE));

			scrollIndex = ParseUtil.findBestLineMatchForMemberSignature(member, lines);
		}
		else
		{
			scrollIndex = regexPos;
		}

		highlightLine(scrollIndex);
	}

	// ugh! dirty hack for highlighting
	private void highlightLine(int pos)
	{
		if (pos != 0)
		{
			if (lastScrollIndex != -1)
			{
				// revert to black Label
				Label label = (Label) vBoxRows.getChildren().get(lastScrollIndex);
				label.setStyle(STYLE_UNHIGHLIGHTED);
			}

			// replace new selected Label with background coloured label
			Label label = (Label) vBoxRows.getChildren().get(pos);
			label.prefWidthProperty().bind(vBoxRows.widthProperty());
			label.setStyle(STYLE_HIGHLIGHTED);

			lastScrollIndex = pos;

			setScrollBar();
		}
	}

	private int findPosForRegex(String regex)
	{
		int result = -1;

		ObservableList<Node> items = vBoxRows.getChildren();

		Pattern pattern = Pattern.compile(regex);

		int index = 0;

		for (Node item : items)
		{
			String line = ((Label) item).getText();

			Matcher matcher = pattern.matcher(line);
			if (matcher.find())
			{
				result = index;
				break;
			}

			index++;
		}

		return result;
	}

	private void setScrollBar()
	{
		double scrollPos = (double) scrollIndex / (double) vBoxRows.getChildren().size()
				* (scrollPane.getVmax() - scrollPane.getVmin());
		scrollPane.setVvalue(scrollPos);
	}
}