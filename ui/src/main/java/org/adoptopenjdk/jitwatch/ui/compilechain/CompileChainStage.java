/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.compilechain;

import static org.adoptopenjdk.jitwatch.util.UserInterfaceUtil.fix;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;
import org.adoptopenjdk.jitwatch.chain.CompileChainWalker;
import org.adoptopenjdk.jitwatch.chain.CompileNode;
import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.ui.compilationchooser.CompilationChooser;
import org.adoptopenjdk.jitwatch.ui.main.ICompilationChangeListener;
import org.adoptopenjdk.jitwatch.ui.main.IMemberSelectedListener;
import org.adoptopenjdk.jitwatch.ui.main.IStageAccessProxy;
import org.adoptopenjdk.jitwatch.ui.main.JITWatchUI;
import org.adoptopenjdk.jitwatch.ui.resize.IRedrawable;
import org.adoptopenjdk.jitwatch.ui.resize.RateLimitedResizeListener;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class CompileChainStage extends Stage implements ICompilationChangeListener, IRedrawable
{
	private ScrollPane scrollPane;
	private Pane pane;
	private IStageAccessProxy stageAccess;
	
	private Label labelRootNodeMember;

	private CompilationChooser compilationChooser;

	private CompileNode rootNode;

	private static final double X_OFFSET = 16;
	private static final double Y_OFFSET = 16;

	private double y;

	private static final double X_GAP = 25;

	private static final int STROKE_WIDTH = 3;
	private static final double RECT_HEIGHT = 25;
	private static final double RECT_Y_GAP = 16;

	class PlotNode
	{
		public Rectangle rect;
		public Text text;
	}

	private IReadOnlyJITDataModel model;

	public CompileChainStage(IMemberSelectedListener selectionListener, final IStageAccessProxy stageAccess,
			IReadOnlyJITDataModel model)
	{
		initStyle(StageStyle.DECORATED);

		this.stageAccess = stageAccess;

		this.model = model;

		scrollPane = new ScrollPane();

		pane = new Pane();

		scrollPane.setContent(pane);

		compilationChooser = new CompilationChooser(selectionListener);
		
		VBox verticalLayout = new VBox();
		
		Scene scene = UserInterfaceUtil.getScene(verticalLayout, JITWatchUI.WINDOW_WIDTH, JITWatchUI.WINDOW_HEIGHT);
		
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
				
		Button buttonSnapShot = UserInterfaceUtil.getSnapshotButton(scene, "CompileChain");
		
		HBox hBox = new HBox();
		
		labelRootNodeMember = new Label();
		
		hBox.getChildren().add(labelRootNodeMember);
		hBox.getChildren().add(compilationChooser.getCombo());
		hBox.getChildren().add(spacer);
		hBox.getChildren().add(buttonSnapShot);
		
		hBox.setSpacing(16.0);
		hBox.setPadding(new Insets(4, 4, 4, 4));
		
		verticalLayout.getChildren().addAll(hBox, scrollPane);

		RateLimitedResizeListener resizeListener = new RateLimitedResizeListener(this, 200);

		pane.widthProperty().addListener(resizeListener);
		pane.heightProperty().addListener(resizeListener);

		setScene(scene);
	}

	@Override
	public void compilationChanged(IMetaMember member)
	{
		compilationChooser.compilationChanged(member);

		if (member != null)
		{
			buildTree(member);
		}
		else
		{
			rootNode = null;
			redraw();
		}
	}

	private void clear()
	{
		y = Y_OFFSET;

		pane.getChildren().clear();

		showKey();
	}

	@Override
	public void redraw()
	{
		if (rootNode != null)
		{
			clear();			
			
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
		String result = null;

		IMetaMember member = null;

		if (node == null)
		{
			result = "Unknown";
		}
		else
		{
			member = node.getMember();
		}

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
				stageAccess.openTriView(node.getMember());
			}
		});
	}

	private void buildTree(IMetaMember member)
	{
		Compilation selectedCompilation = member.getSelectedCompilation();
		
		String title = "Compile Chain: ";

		if (selectedCompilation != null)
		{
			CompileChainWalker walker = new CompileChainWalker(model);

			CompileNode root = walker.buildCallTree(selectedCompilation);

			this.rootNode = root;

			String rootMemberName = getLabelText(root);
			
			title += rootMemberName + " " + root.getCompilation().getSignature();

			setTitle(title);
			
			labelRootNodeMember.setText(rootMemberName);
		}
		else
		{
			rootNode = null;
			
			labelRootNodeMember.setText(S_EMPTY);
			
			clear();
			
			Text text = new Text(member.toString() + " was not JIT compiled");
			text.setX(fix(X_OFFSET));
			text.setY(fix(y));
			text.setStrokeWidth(1.0);

			pane.getChildren().add(text);
		}

		redraw();
	}
}