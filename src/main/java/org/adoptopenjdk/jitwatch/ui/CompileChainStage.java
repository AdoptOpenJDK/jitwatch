/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import org.adoptopenjdk.jitwatch.chain.CompileNode;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

public class CompileChainStage extends Stage
{
	private ScrollPane scrollPane;
	private Pane pane;
	private JITWatchUI parent;

	private CompileNode rootNode;

	private static final double X_OFFSET = 16;
	private static final double Y_OFFSET = 16;

	private double y = Y_OFFSET;

	private static final double X_GAP = 25;

	private static final int STROKE_WIDTH = 3;
	private static final double RECT_HEIGHT = 25;
	private static final double RECT_Y_GAP = 16;

	class PlotNode
	{
		public Rectangle rect;
		public Text text;
	}

	public CompileChainStage(final JITWatchUI parent, CompileNode root)
	{
		initStyle(StageStyle.DECORATED);
		
		this.parent = parent;

		this.rootNode = root;

		scrollPane = new ScrollPane();
		pane = new Pane();

		scrollPane.setContent(pane);

		Scene scene = UserInterfaceUtil.getScene(scrollPane, JITWatchUI.WINDOW_WIDTH, JITWatchUI.WINDOW_HEIGHT);
			
		setTitle("Compile Chain: " + root.getMemberName());

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
		showKey();

		show(rootNode, X_OFFSET, Y_OFFSET, 0);

		if (rootNode.getChildren().size() == 0)
		{
			
			
			Text text = new Text("No method calls made by " + rootNode.getMemberName() + " were inlined or JIT compiled");
			text.setX(X_OFFSET);
			text.setY(y);

			pane.getChildren().add(text);
		}
	}

	private void showKey()
	{
		double keyX = scrollPane.getWidth() - 220;
		double keyY = 10;

		Rectangle roundedRect = new Rectangle(keyX - 20, keyY, 210, 180);

		roundedRect.setArcHeight(30);
		roundedRect.setArcWidth(30);

		roundedRect.setStroke(Color.BLACK);
		roundedRect.setFill(Color.TRANSPARENT);

		pane.getChildren().add(roundedRect);

		keyY += 20;

		Text text = new Text("Key");
		text.setX(keyX + 75);
		text.setY(keyY);

		pane.getChildren().add(text);

		keyY += 15;

		buildNode("Not Compiled or Inlined", keyX, keyY, false, false);
		keyY += 35;

		buildNode("Compiled Only", keyX, keyY, false, true);
		keyY += 35;

		buildNode("Inlined Only", keyX, keyY, true, false);
		keyY += 35;

		buildNode("Compiled and Inlined", keyX, keyY, true, true);
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
		String memberName = node.getMemberName();

		return memberName == null ? "Unknown" : memberName;
	}

	private double plotNode(final CompileNode node, final double x, final double parentY, final int depth)
	{
		String labelText = getLabelText(node);

		PlotNode plotNode = buildNode(labelText, x, y, node.isInlined(), node.isCompiled());

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

		double nextX = x + plotNode.rect.getWidth() / 2;

		nextX += X_GAP;

		initialiseRectWithOnMouseClickedEventHandler(node, plotNode.rect);
		initialiseRectWithOnMouseClickedEventHandler(node, plotNode.text);

		Tooltip tip = new Tooltip(node.getTooltipText());
		Tooltip.install(plotNode.rect, tip);
		Tooltip.install(plotNode.text, tip);

		return nextX;
	}

	private PlotNode buildNode(String labelText, double x, double y, boolean inlined, boolean compiled)
	{
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

		text.setFill(getColorForInlining(inlined));

		rect.setStroke(Color.BLACK);
		rect.setStrokeWidth(STROKE_WIDTH);
		rect.setFill(getColourForCompilation(compiled));

		pane.getChildren().add(rect);
		pane.getChildren().add(text);

		PlotNode result = new PlotNode();
		result.rect = rect;
		result.text = text;

		return result;
	}

	private Color getColourForCompilation(boolean isCompiled)
	{
		if (isCompiled)
		{
			return Color.GREEN;
		}
		else
		{
			return Color.RED;
		}
	}

	private Color getColorForInlining(boolean isInlined)
	{
		if (isInlined)
		{
			return Color.YELLOW;
		}
		else
		{
			return Color.BLACK;
		}
	}

	private void initialiseRectWithOnMouseClickedEventHandler(final CompileNode node, Shape shape)
	{
		shape.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent arg0)
			{
				parent.openTriView(node.getMember(), true);
			}
		});
	}

}