/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.compilechain;

import static org.adoptopenjdk.jitwatch.util.UserInterfaceUtil.fix;

import org.adoptopenjdk.jitwatch.chain.CompileNode;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.ui.main.IStageAccessProxy;
import org.adoptopenjdk.jitwatch.ui.main.JITWatchUI;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

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

public class CompileChainStage extends Stage
{
	private ScrollPane scrollPane;
	private Pane pane;
	private IStageAccessProxy stageAccess;

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

	public CompileChainStage(final IStageAccessProxy stageAccess, CompileNode root)
	{
		initStyle(StageStyle.DECORATED);

		this.stageAccess = stageAccess;

		this.rootNode = root;

		scrollPane = new ScrollPane();
		pane = new Pane();

		scrollPane.setContent(pane);

		Scene scene = UserInterfaceUtil.getScene(scrollPane, JITWatchUI.WINDOW_WIDTH, JITWatchUI.WINDOW_HEIGHT);

		setTitle("Compile Chain: " + root.getMemberName() + " " + root.getCompilation().getSignature());

		setScene(scene);

		redraw();
	}

	private void redraw()
	{
		showKey();

		show(rootNode, X_OFFSET, Y_OFFSET, 0);

		if (rootNode.getChildren().isEmpty())
		{
			Text text = new Text("No method calls made by " + rootNode.getMemberName() + " were inlined or JIT compiled");
			text.setX(fix(X_OFFSET));
			text.setY(fix(y));
			text.setStrokeWidth(1.0);

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
		text.setX(fix(keyX + 75));
		text.setY(fix(keyY));

		pane.getChildren().add(text);

		keyY += 15;

		buildNode("Inlined", keyX, keyY, true, false, false);
		keyY += 35;

		buildNode("Compiled", keyX, keyY, false, true, false);
		keyY += 35;

		buildNode("Virtual Call", keyX, keyY, false, false, true);
		keyY += 35;

		buildNode("Not Compiled", keyX, keyY, false, false, false);
		keyY += 35;
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

		String result = null;

		if (member == null)
		{
			result = "Unknown";
		}
		else if (member.isConstructor())
		{
			result = member.getMetaClass().getAbbreviatedFullyQualifiedName() + "()";
		}
		else
		{
			result = member.getAbbreviatedFullyQualifiedMemberName() + "()";
		}

		return result;
	}

	private double plotNode(final CompileNode node, final double x, final double parentY, final int depth)
	{
		String labelText = getLabelText(node);

		PlotNode plotNode = buildNode(labelText, x, y, node.isInlined(), node.isCompiled(), node.isVirtualCall());

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

		Tooltip tip = new Tooltip(node == this.rootNode ? "Root node" : node.getTooltipText());
		Tooltip.install(plotNode.rect, tip);
		Tooltip.install(plotNode.text, tip);

		return nextX;
	}

	private PlotNode buildNode(String labelText, double x, double y, boolean inlined, boolean compiled, boolean virtualCall)
	{
		Text text = new Text(labelText);

		text.snapshot(null, null);

		double textWidth = text.getLayoutBounds().getWidth();
		double textHeight = text.getLayoutBounds().getHeight();

		double rectWidth = textWidth + 20;

		Rectangle rect = new Rectangle(x, y, rectWidth, RECT_HEIGHT);
		rect.setArcWidth(16);
		rect.setArcHeight(16);

		text.setX(fix(x + (rectWidth / 2 - textWidth / 2)));
		text.setY(fix(y + RECT_HEIGHT - STROKE_WIDTH - (RECT_HEIGHT - textHeight) / 2));

		text.setStrokeWidth(0.5);
		text.setFill(Color.WHITE);
		text.setStroke(Color.WHITE);

		rect.setStroke(Color.BLACK);
		rect.setStrokeWidth(STROKE_WIDTH);
		rect.setFill(getColourForCompilation(compiled, inlined, virtualCall));

		pane.getChildren().add(rect);
		pane.getChildren().add(text);

		PlotNode result = new PlotNode();
		result.rect = rect;
		result.text = text;

		return result;
	}

	private Color getColourForCompilation(boolean isCompiled, boolean isInlined, boolean isVirtual)
	{
		if (isInlined)
		{
			return Color.GREEN;
		}
		else if (isVirtual)
		{
			return Color.PURPLE;
		}
		else if (isCompiled)
		{
			return Color.RED;
		}
		else
		{
			return Color.GRAY;
		}
	}

	private void initialiseRectWithOnMouseClickedEventHandler(final CompileNode node, Shape shape)
	{
		shape.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent arg0)
			{
				stageAccess.openTriView(node.getMember(), true);
			}
		});
	}
}