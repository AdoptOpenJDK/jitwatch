/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.triview;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_DOUBLE_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE_CR;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_TAB;
import static org.adoptopenjdk.jitwatch.util.UserInterfaceUtil.FONT_MONOSPACE_FAMILY;
import static org.adoptopenjdk.jitwatch.util.UserInterfaceUtil.FONT_MONOSPACE_SIZE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.bytecode.LineAnnotation;
import org.adoptopenjdk.jitwatch.ui.main.IStageAccessProxy;
import org.adoptopenjdk.jitwatch.ui.triview.ILineListener.LineType;
import org.adoptopenjdk.jitwatch.ui.triview.assembly.AssemblyLabel;
import org.adoptopenjdk.jitwatch.ui.triview.bytecode.BytecodeLabel;
import org.adoptopenjdk.jitwatch.util.ParseUtil;
import org.adoptopenjdk.jitwatch.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private double lastKnownGoodLineHeight = 15;

	private static final String FONT_STYLE = "-fx-font-family:" + FONT_MONOSPACE_FAMILY + "; -fx-font-size:" + FONT_MONOSPACE_SIZE
			+ "px;";

	public static final String STYLE_UNHIGHLIGHTED = FONT_STYLE + "-fx-background-color:white;";
	public static final String STYLE_HIGHLIGHTED = FONT_STYLE + "-fx-background-color:red;";
	public static final String STYLE_UNHIGHLIGHTED_SUGGESTION = FONT_STYLE + "-fx-background-color:yellow;";
	public static final String STYLE_SAFEPOINT = FONT_STYLE + "-fx-background-color:yellow;";

	protected Map<Integer, LineAnnotation> lineAnnotations = new HashMap<>();

	protected static final Logger logger = LoggerFactory.getLogger(Viewer.class);

	protected IStageAccessProxy stageAccessProxy;

	protected ILineListener lineListener;
	protected LineType lineType = LineType.PLAIN;

	private boolean isHighlighting;

	public Viewer(IStageAccessProxy stageAccessProxy, boolean highlighting)
	{
		this.stageAccessProxy = stageAccessProxy;
		this.isHighlighting = highlighting;

		lineListener = new NoOpLineListener();

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
	
	public void clear()
	{
		lineAnnotations.clear();
		
		vBoxRows.getChildren().clear();
		
		lastScrollIndex = -1;
	}
	
	public LineType getLineType()
	{
		return lineType;
	}

	public JITWatchConfig getConfig()
	{
		return stageAccessProxy.getConfig();
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

		EventHandler<KeyEvent> keyHandler = new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent event)
			{
				KeyCode code = event.getCode();

				clearAllHighlighting();

				switch (code)
				{
				case UP:
					handleKeyUp();
					break;
				case DOWN:
					handleKeyDown();
					break;
				case LEFT:
					handleKeyLeft();
					break;
				case RIGHT:
					handleKeyRight();
					break;
				case PAGE_UP:
					handleKeyPageUp();
					break;
				case PAGE_DOWN:
					handleKeyPageDown();
					break;
				default:
					return;
				}

				event.consume();
			}
		};

		focusedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean hadFocus, Boolean hasFocus)
			{
				if (hasFocus && !hadFocus)
				{
					scrollPane.requestFocus();
				}
			}
		});

		scrollPane.focusedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean hadFocus, Boolean hasFocus)
			{
				if (hasFocus && !hadFocus)
				{
					lineListener.lineHighlighted(scrollIndex, lineType);
					highlightLine(scrollIndex, false);
				}
			}
		});

		scrollPane.setOnMouseEntered(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent arg0)
			{

				if (getConfig().isTriViewMouseFollow())
				{
					lineListener.handleFocusSelf(lineType);
				}
			}
		});

		scrollPane.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent arg0)
			{
				lineListener.handleFocusSelf(lineType);
			}
		});

		scrollPane.setOnKeyPressed(keyHandler);

		getChildren().add(scrollPane);

		setUpContextMenu();
	}

	public void setContent(String inSource, boolean showLineNumbers, boolean canHighlight)
	{
		clear();
		
		String source = inSource;
		
		isHighlighting = canHighlight;
		
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

			lines[i] = lines[i].replace(S_NEWLINE_CR, S_EMPTY);

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
		
		if (!isHighlighting)
		{
			clearAllHighlighting();
		}

		for (final Label label : items)
		{
			final int finalPos = pos;

			unhighlightLabel(label);

			if (isHighlighting)
			{
				label.setOnMouseEntered(new EventHandler<MouseEvent>()
				{
					@Override
					public void handle(MouseEvent mouseEvent)
					{
						if (getConfig().isTriViewMouseFollow())
						{
							handleLabelClicked(mouseEvent, finalPos);
						}
					}
				});

				if (label.getOnMouseClicked() == null)
				{
					label.setOnMouseClicked(new EventHandler<MouseEvent>()
					{
						@Override
						public void handle(MouseEvent mouseEvent)
						{
							handleLabelClicked(mouseEvent, finalPos);
						}
					});
				}

				label.setOnMouseExited(new EventHandler<MouseEvent>()
				{
					@Override
					public void handle(MouseEvent arg0)
					{
						if (getConfig().isTriViewMouseFollow())
						{
							unhighlightLabel(label);
						}
					}
				});
			}

			label.minWidthProperty().bind(scrollPane.widthProperty());
			pos++;
		}
	}

	protected void handleLabelClicked(MouseEvent mouseEvent, int index)
	{
		clearAllHighlighting();

		lineListener.lineHighlighted(index, lineType);
		highlightLine(index, false);
	}

	private int checkBounds(int scrollIndex)
	{
		int min = 0;
		int max = vBoxRows.getChildren().size() - 1;

		return Math.min(Math.max(scrollIndex, min), max);
	}

	private void handleKeyUp()
	{
		scrollIndex--;

		scrollIndex = checkBounds(scrollIndex);

		lineListener.lineHighlighted(scrollIndex, lineType);
		highlightLine(scrollIndex);
	}

	private void handleKeyDown()
	{
		scrollIndex++;

		scrollIndex = checkBounds(scrollIndex);

		lineListener.lineHighlighted(scrollIndex, lineType);
		highlightLine(scrollIndex);
	}

	private void handleKeyLeft()
	{
		lineListener.handleFocusPrev();
	}

	private void handleKeyRight()
	{
		lineListener.handleFocusNext();
	}

	private void handleKeyPageUp()
	{
		scrollIndex -= linesPerPane();

		scrollIndex = checkBounds(scrollIndex);

		lineListener.lineHighlighted(scrollIndex, lineType);
		highlightLine(scrollIndex);
	}

	private void handleKeyPageDown()
	{
		scrollIndex += linesPerPane();

		scrollIndex = checkBounds(scrollIndex);

		lineListener.lineHighlighted(scrollIndex, lineType);
		highlightLine(scrollIndex);
	}

	private int linesPerPane()
	{
		return (int) (scrollPane.getHeight() / 10);
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

				ObservableList<Node> items = vBoxRows.getChildren();

				content.putString(transformNodeItemsToTextUsing(items));

				clipboard.setContent(content);
			}
		});
	}

	private String transformNodeItemsToTextUsing(ObservableList<Node> items)
	{
		StringBuilder builder = new StringBuilder();

		for (Node item : items)
		{
			String line = ((Label) item).getText();

			builder.append(line).append(S_NEWLINE);
		}

		return builder.toString();
	}

	public void jumpToMemberSource(IMetaMember member)
	{
		scrollIndex = -1;

		int regexPos = findPosForRegex(member.getSourceMethodSignatureRegEx());

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

	public void clearAllHighlighting()
	{
		for (Node item : vBoxRows.getChildren())
		{
			unhighlightLabel(item);
		}
	}

	private void unhighlightLabel(Node node)
	{
		if (node instanceof BytecodeLabel)
		{
			node.setStyle(((BytecodeLabel) node).getUnhighlightedStyle());
		}
		else if (node instanceof AssemblyLabel)
		{
			node.setStyle(((AssemblyLabel) node).getUnhighlightedStyle());
		}
		else
		{
			node.setStyle(STYLE_UNHIGHLIGHTED);
		}
	}

	public void unhighlightPrevious()
	{
		if (lastScrollIndex >= 0 && lastScrollIndex < vBoxRows.getChildren().size())
		{
			Label label = (Label) vBoxRows.getChildren().get(lastScrollIndex);

			unhighlightLabel(label);
		}
	}

	protected void highlightLine(int index)
	{
		highlightLine(index, true);
	}

	public void highlightLine(int index, boolean setScrollbar)
	{	
		unhighlightPrevious();

		if (index >= vBoxRows.getChildren().size())
		{
			index = vBoxRows.getChildren().size() - 1;
		}

		if (index >= 0)
		{
			// leave source position unchanged if not a known source line
			Label label = (Label) vBoxRows.getChildren().get(index);
			label.setStyle(STYLE_HIGHLIGHTED);

			lastScrollIndex = index;

			scrollIndex = index;
			
			if (setScrollbar)
			{
				setScrollBar();
			}
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

		if (DEBUG_LOGGING)
		{
			if (result == null)
			{
				logger.debug("No label at index {}", index);
			}
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
			double scrollPaneHeight = scrollPane.getHeight();
			double lineHeight = vBoxRows.getChildren().get(0).getBoundsInParent().getHeight();
			
			if (lineHeight == 0.0)
			{
				lineHeight = lastKnownGoodLineHeight;
			}
			else
			{
				lastKnownGoodLineHeight = lineHeight;
			}
						
			double visibleLines = scrollPaneHeight / lineHeight;

			double count = vBoxRows.getChildren().size() - visibleLines;

			double scrollPercent = 0;
			
			if (count > 0)
			{
				scrollPercent = Math.max(scrollIndex - (visibleLines / 2), 0) / count;
			}
			
			double scrollPos = scrollPercent * (scrollMax - scrollMin);
			
			scrollPane.setVvalue(scrollPos);
		}
	}
}