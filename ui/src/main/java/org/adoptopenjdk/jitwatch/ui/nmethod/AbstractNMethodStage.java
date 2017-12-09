/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.nmethod;

import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.ui.main.ICompilationChangeListener;
import org.adoptopenjdk.jitwatch.ui.main.IPrevNextCompilationListener;
import org.adoptopenjdk.jitwatch.ui.main.JITWatchUI;
import org.adoptopenjdk.jitwatch.ui.resize.IRedrawable;
import org.adoptopenjdk.jitwatch.ui.resize.RateLimitedResizeListener;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public abstract class AbstractNMethodStage extends Stage
		implements IRedrawable, ICompilationChangeListener, IPrevNextCompilationListener
{
	protected double width;
	protected double height;

	protected Scene scene;

	protected VBox vBoxStack;

	protected Pane pane;
	protected ScrollPane scrollPane;

	protected VBox vBoxControls;

	private NMethodInfo nMethodInfo;

	protected JITWatchUI parent;

	protected Button btnZoomIn;
	protected Button btnZoomOut;
	protected Button btnZoomReset;

	protected double zoom = 1;
	
	protected static final Color COLOR_UNSELECTED_COMPILATION = Color.rgb(0, 196, 0);

	protected static final Color COLOR_SELECTED_COMPILATION = Color.rgb(0, 220, 255);

	protected static final Color COLOR_OTHER_MEMBER_COMPILATIONS = Color.rgb(0, 0, 160);

	public AbstractNMethodStage(final JITWatchUI parent, String title)
	{
		this.parent = parent;

		initStyle(StageStyle.DECORATED);

		vBoxStack = new VBox();
		vBoxStack.setSpacing(0);

		scrollPane = new ScrollPane();

		pane = new Pane();
		
		scrollPane.setContent(pane);
		scrollPane.setFitToHeight(true);

		nMethodInfo = new NMethodInfo(this);

		scene = UserInterfaceUtil.getScene(vBoxStack, JITWatchUI.WINDOW_WIDTH, JITWatchUI.WINDOW_HEIGHT);

		vBoxControls = buildControls(scene);

		vBoxStack.getChildren().addAll(scrollPane, vBoxControls, nMethodInfo);

		scrollPane.prefWidthProperty().bind(vBoxStack.widthProperty());
		vBoxControls.prefWidthProperty().bind(vBoxStack.widthProperty());
		nMethodInfo.prefWidthProperty().bind(vBoxStack.widthProperty());

		pane.prefHeightProperty().bind(vBoxStack.heightProperty().multiply(0.975).subtract(vBoxControls.heightProperty())
				.subtract(nMethodInfo.heightProperty()));

		RateLimitedResizeListener resizeListener = new RateLimitedResizeListener(this, 100);

		scene.widthProperty().addListener(resizeListener);
		scene.heightProperty().addListener(resizeListener);

		setTitle(title);

		setScene(scene);
		
		pane.setStyle("-fx-background-color: #000000");
	}

	protected abstract VBox buildControls(Scene scene);

	protected void plotMarker(double x, double y, Compilation compilation)
	{
		plotMarker(x, y, compilation, false);
	}
	
	protected void plotMarker(double x, double y, Compilation compilation, boolean invert)
	{
		double side = 12;
		double centre = x;

		double left = x - side / 2;
		double right = x + side / 2;

		double top;
		double bottom;

		if (invert)
		{
			top = y+side;
			bottom = y;
		}
		else
		{
			top = y - side;
			bottom = y;	
		}
		
		Polygon triangle = new Polygon();
		triangle.getPoints().addAll(new Double[] { left, bottom, centre, top, right, bottom });

		triangle.setFill(Color.WHITE);
		triangle.setStroke(Color.BLACK);

		attachListener(triangle, compilation);

		pane.getChildren().add(triangle);
	}

	protected void attachListener(Shape shape, final Compilation compilation)
	{
		shape.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent arg0)
			{
				selectCompilation(compilation.getMember(), compilation.getIndex());
			}
		});
	}

	@Override
	public void selectPrevCompilation()
	{
		IMetaMember selectedMember = parent.getSelectedMember();

		if (selectedMember != null && selectedMember.getSelectedCompilation() != null)
		{
			int prevIndex = selectedMember.getSelectedCompilation().getIndex() - 1;

			selectCompilation(selectedMember, prevIndex);
		}
	}

	@Override
	public void selectNextCompilation()
	{
		IMetaMember selectedMember = parent.getSelectedMember();

		if (selectedMember != null && selectedMember.getSelectedCompilation() != null)
		{
			int nextIndex = selectedMember.getSelectedCompilation().getIndex() + 1;

			selectCompilation(selectedMember, nextIndex);
		}
	}

	private void selectCompilation(final IMetaMember member, final int index)
	{
		parent.selectCompilation(member, index);
	}

	@Override
	public void compilationChanged(IMetaMember member)
	{		
		redraw();
	}
	
	protected void clear()
	{

		pane.getChildren().clear();

		nMethodInfo.clear();
		
		IMetaMember member = parent.getSelectedMember();
		
		Compilation selectedCompilation = (member == null) ? null :member.getSelectedCompilation();

		if (selectedCompilation != null)
		{
			nMethodInfo.setInfo(selectedCompilation);
		}
	}
}