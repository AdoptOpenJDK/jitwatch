/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import org.adoptopenjdk.jitwatch.chain.CompileNode;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompileChainStage extends Stage
{
	private static final Logger logger = LoggerFactory.getLogger(CompileChainStage.class);

	private ScrollPane scrollPane;
	private Pane pane;

	private CompileNode rootNode;

	private static final double X_OFFSET = 16;
	private static final double Y_OFFSET = 16;

	private double y = Y_OFFSET;

	private static final double X_GAP = 25;

	private static final int STROKE_WIDTH = 3;
	private static final double RECT_HEIGHT = 25;
	private static final double RECT_Y_GAP = 16;

	public CompileChainStage(final JITWatchUI parent, CompileNode root)
	{
		initStyle(StageStyle.DECORATED);

		this.rootNode = root;

		scrollPane = new ScrollPane();
		pane = new Pane();

		scrollPane.setContent(pane);

		Scene scene = new Scene(scrollPane, JITWatchUI.WINDOW_WIDTH, JITWatchUI.WINDOW_HEIGHT);

		setTitle("Compile Chain: " + root.getMember().toString());

		setScene(scene);

		redraw();

		setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				parent.handleStageClosed(CompileChainStage.this);
			}
		});
	}

	public void redraw()
	{
		show(rootNode, X_OFFSET, Y_OFFSET, 0);

		if (rootNode.getChildren().size() == 0)
		{
			Text text = new Text(rootNode.getMember().toStringUnqualifiedMethodName(false) + " makes no calls");
			text.setX(X_OFFSET);
			text.setY(y);

			pane.getChildren().add(text);
		}
	}

	private void show(CompileNode node, double x, double parentY, int depth)
	{
		double lastX = x;

		lastX = plotNode(node, x, parentY, depth);

		y += RECT_HEIGHT + STROKE_WIDTH + RECT_Y_GAP;

		parentY = y - RECT_Y_GAP;

		for (CompileNode child : node.getChildren())
		{
			show(child, lastX, parentY, depth + 1);
		}
	}

	private String getLabelText(CompileNode node)
	{
		IMetaMember member = node.getMember();

		return member == null ? "Unknown" : member.getMemberName();
	}

	private double plotNode(final CompileNode node, double x, double parentY, int depth)
	{
		String labelText = getLabelText(node);

		StringBuilder tipBuilder = new StringBuilder();
		tipBuilder.append(node.getMember().toString()).append(C_NEWLINE);

		Text text = new Text(labelText);

		text.snapshot(null, null);
		double textWidth = text.getLayoutBounds().getWidth();
		double textHeight = text.getLayoutBounds().getHeight();

		double rectWidth = textWidth + 20;

		Rectangle rect = new Rectangle(x, y, rectWidth, RECT_HEIGHT);
		rect.setArcWidth(16);
		rect.setArcHeight(16);

		text.setX(x + (rectWidth / 2 - textWidth / 2));

		// text plot from bottom left
		text.setY(y + RECT_HEIGHT - STROKE_WIDTH - (RECT_HEIGHT - textHeight) / 2);

		rect.setStroke(Color.BLACK);
		rect.setStrokeWidth(STROKE_WIDTH);

		tipBuilder.append("JIT Compiled: ");

		highlightCompilation(node, tipBuilder, rect);

		highlightInlining(node, text);

		String inlineReason = node.getInlineReason();

		if (inlineReason != null)
		{
			tipBuilder.append(inlineReason);
		}

		tipBuilder.append(C_NEWLINE);

		if (depth > 0)
		{
			double connectX = x - X_GAP;
			double connectY = y + RECT_HEIGHT / 2;
			double upLineY = y + RECT_HEIGHT / 2;

			Line lineUp = new Line(connectX, upLineY, connectX, parentY);
			lineUp.setStrokeWidth(STROKE_WIDTH);
			pane.getChildren().add(lineUp);

			Line lineLeft = new Line(connectX, connectY, x, connectY);
			lineLeft.setStrokeWidth(STROKE_WIDTH);
			pane.getChildren().add(lineLeft);
		}

		x += rectWidth / 2;

		x += X_GAP;

		initialiseRectWithOnMouseClickedEventHandler(node, rect);

		Tooltip tip = new Tooltip(tipBuilder.toString());
		Tooltip.install(rect, tip);
		Tooltip.install(text, tip);

		pane.getChildren().add(rect);
		pane.getChildren().add(text);

		return x;
	}

	private void highlightCompilation(CompileNode node, StringBuilder tipBuilder, Rectangle rect)
	{
		if (node.getMember().isCompiled())
		{
			tipBuilder.append("Yes\n");
			rect.setFill(Color.GREEN);
		}
		else
		{
			tipBuilder.append("No\n");
			rect.setFill(Color.RED);
		}
	}

	private void highlightInlining(CompileNode node, Text text)
	{
		if (node.isInlined())
		{
			text.setFill(Color.YELLOW);
		}
	}

	private void initialiseRectWithOnMouseClickedEventHandler(final CompileNode node, Rectangle rect)
	{
		rect.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent arg0)
			{
				logger.info("{}", node.getMember());
				// TODO use for navigation in TriView?
			}
		});
	}

}