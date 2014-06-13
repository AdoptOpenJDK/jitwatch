/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui.triview;

import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.LineAnnotation;
import com.chrisnewland.jitwatch.ui.IStageAccessProxy;
import com.chrisnewland.jitwatch.ui.triview.ILineListener.LineType;
import com.chrisnewland.jitwatch.ui.triview.bytecode.BytecodeLabel;
import com.chrisnewland.jitwatch.util.ParseUtil;
import com.chrisnewland.jitwatch.util.StringUtil;

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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class Viewer extends VBox
{
	private ScrollPane scrollPane;
	protected VBox vBoxRows;

	public static final String COLOUR_BLACK = "black";
	public static final String COLOUR_RED = "red";
	public static final String COLOUR_GREEN = "green";
	public static final String COLOUR_BLUE = "blue";

	private int scrollIndex = 0;
	protected int lastScrollIndex = -1;
	protected String originalSource;

	protected static final String STYLE_UNHIGHLIGHTED = "-fx-font-family:monospace; -fx-font-size:12px; -fx-background-color:white;";
	protected static final String STYLE_HIGHLIGHTED = "-fx-font-family:monospace; -fx-font-size:12px; -fx-background-color:red;";

	protected Map<Integer, LineAnnotation> lineAnnotations = new HashMap<>();

	protected static final Logger logger = LoggerFactory.getLogger(Viewer.class);

	protected IStageAccessProxy stageAccessProxy;

	protected ILineListener lineListener;
	protected LineType lineType;

	private boolean isHighlighting;

	public Viewer(IStageAccessProxy stageAccessProxy, boolean highlighting)
	{
		this.stageAccessProxy = stageAccessProxy;
		this.isHighlighting = highlighting;

		setup();
	}

	public Viewer(IStageAccessProxy stageAccessProxy, ILineListener lineListener, LineType lineType, boolean highlighting)
	{
		this.stageAccessProxy = stageAccessProxy;
		this.lineListener = lineListener;
		this.lineType = lineType;
		this.isHighlighting = highlighting;

		setup();
	}

	private void setup()
	{
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
		scrollPane.setStyle("-fx-background:white");

		scrollPane.setFitToHeight(true);

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

		source = source.replace(S_TAB, S_DOUBLE_SPACE);

		String[] lines = source.split(S_NEWLINE);

		int maxWidth = Integer.toString(lines.length).length();

		List<Label> labels = new ArrayList<>();

		for (int i = 0; i < lines.length; i++)
		{
			String row = lines[i];

			if (showLineNumbers)
			{
				lines[i] = StringUtil.padLineNumber(i + 1, maxWidth) + S_DOUBLE_SPACE + row;
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

		int pos = 0;

		for (final Label label : items)
		{
			final int finalPos = pos;

			unhighlightLabel(label);

			if (isHighlighting)
			{
				label.setOnMouseEntered(new EventHandler<MouseEvent>()
				{
					@Override
					public void handle(MouseEvent arg0)
					{
						unhighlightPrevious();
						
						label.setStyle(STYLE_HIGHLIGHTED);
						lineListener.lineHighlighted(finalPos, lineType);
					}
				});

				label.setOnMouseExited(new EventHandler<MouseEvent>()
				{
					@Override
					public void handle(MouseEvent arg0)
					{
						unhighlightLabel(label);
					}
				});
			}

			label.minWidthProperty().bind(scrollPane.widthProperty());
			pos++;
		}
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
		scrollIndex = -1;

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
	}

	private void unhighlightLabel(Label label)
	{
		if (label instanceof BytecodeLabel)
		{
			label.setStyle(((BytecodeLabel) label).getUnhighlightedStyle());
		}
		else
		{
			label.setStyle(STYLE_UNHIGHLIGHTED);
		}
	}
	
	private void unhighlightPrevious()
	{
		if (lastScrollIndex != -1)
		{
			Label label = (Label) vBoxRows.getChildren().get(lastScrollIndex);

			unhighlightLabel(label);
		}
	}

	public void highlightLine(int index)
	{
		unhighlightPrevious();

		if (index != -1)
		{
			Label label = (Label) vBoxRows.getChildren().get(index);
			label.setStyle(STYLE_HIGHLIGHTED);

			lastScrollIndex = index;

			scrollIndex = index;

			setScrollBar();
		}
	}

	public Label getLabelAtIndex(int index)
	{
		ObservableList<Node> items = vBoxRows.getChildren();

		Label result = null;

		if (index >= 0 && index < items.size())
		{
			result = (Label) items.get(index);
		}

		return result;
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

	public void setScrollBar()
	{
		if (vBoxRows.getChildren().size() > 0)
		{
			double scrollMin = scrollPane.getVmin();
			double scrollMax = scrollPane.getVmax();

			double scrollPercent = (double) scrollIndex / (double) vBoxRows.getChildren().size();

			double scrollPos = scrollPercent * (scrollMax - scrollMin);

			scrollPane.setVvalue(scrollPos);
		}
	}
}