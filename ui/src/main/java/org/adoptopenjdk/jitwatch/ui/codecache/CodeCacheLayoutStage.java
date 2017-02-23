/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.codecache;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.compilation.codecache.CodeCacheWalkerResult;
import org.adoptopenjdk.jitwatch.model.CodeCacheEvent;
import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.ui.main.JITWatchUI;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class CodeCacheLayoutStage extends Stage
{
	protected JITWatchUI mainUI;

	private CodeCacheWalkerResult codeCacheData;

	private long lowAddress;
	private long highAddress;
	private long addressRange;

	private double width;
	private double height;

	private BorderPane borderPane;

	private Pane pane;
	private ScrollPane scrollPane;

	private NMethodInfo nMethodInfo;

	private double zoom;

	private Label lblNMethodCount;
	private Label lblLowAddress;
	private Label lblHighAddress;
	private Label lblAddressRange;

	private Button btnZoomIn;
	private Button btnZoomOut;
	private Button btnZoomReset;
	private Button btnAnimate;

	private CheckBox checkC1;
	private CheckBox checkC2;

	private boolean drawC1 = true;
	private boolean drawC2 = true;

	private TextField txtAnimationSeconds;

	private static final Color NOT_LATEST_COMPILATION = Color.rgb(96, 0, 0);

	private static final Color LATEST_COMPILATION = Color.rgb(0, 96, 0);

	private static final Color SELECTED_COMPILATION = Color.rgb(0, 255, 255);

	private static final Color OTHER_MEMBER_COMPILATIONS = Color.rgb(0, 0, 128);

	private long lastResizeRedraw = 0;
	private boolean redrawRequired = false;

	public CodeCacheLayoutStage(final JITWatchUI parent)
	{
		this.mainUI = parent;

		this.zoom = 1;

		initStyle(StageStyle.DECORATED);

		borderPane = new BorderPane();

		scrollPane = new ScrollPane();

		pane = new Pane();
		pane.setStyle("-fx-background-color: #000000");

		scrollPane.setContent(pane);

		nMethodInfo = new NMethodInfo(this);

		borderPane.setTop(scrollPane);

		VBox vBoxControls = buildControls();

		borderPane.setCenter(vBoxControls);

		borderPane.setBottom(nMethodInfo);

		Scene scene = UserInterfaceUtil.getScene(borderPane, JITWatchUI.WINDOW_WIDTH, JITWatchUI.WINDOW_HEIGHT);

		scrollPane.prefWidthProperty().bind(scene.widthProperty());
		scrollPane.prefHeightProperty().bind(scene.heightProperty().multiply(0.5));

		scrollPane.setFitToHeight(true);

		pane.prefHeightProperty().bind(scrollPane.heightProperty());

		nMethodInfo.prefHeightProperty().bind(scrollPane.heightProperty());

		vBoxControls.prefWidthProperty().bind(scene.widthProperty());

		class SceneResizeListener implements ChangeListener<Number>
		{
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2)
			{
				long now = System.currentTimeMillis();

				if (now - lastResizeRedraw > 200)
				{
					redraw();

					redrawRequired = true;

					lastResizeRedraw = now;

					new Thread(new Runnable()
					{
						@Override
						public void run() // off UI thread
						{
							try
							{
								Thread.sleep(500); // wait is off UI thread

								Platform.runLater(new Runnable()
								{
									@Override
									public void run() // on UI thread
									{
										if (redrawRequired)
										{
											redraw();
										}
									}
								});
							}
							catch (InterruptedException e)
							{
							}
						}
					}).start();
				}
				else
				{
					redrawRequired = true; // we skipped a redraw
				}
			}
		}

		SceneResizeListener rl = new SceneResizeListener();

		scene.widthProperty().addListener(rl);
		scene.heightProperty().addListener(rl);

		setTitle("Code Cache Layout");

		setScene(scene);
	}

	private VBox buildControls()
	{
		VBox vBoxControls = new VBox();

		vBoxControls.getChildren().addAll(buildControlButtons(), buildControlInfo());

		return vBoxControls;
	}

	private HBox buildControlButtons()
	{
		HBox hboxControls = new HBox();

		hboxControls.setSpacing(10);
		hboxControls.setAlignment(Pos.CENTER_LEFT);
		hboxControls.setPadding(new Insets(4, 0, 0, 8));

		btnZoomIn = new Button("Zoom In");
		btnZoomOut = new Button("Zoom Out");
		btnZoomReset = new Button("Reset");
		btnAnimate = new Button("Animate");

		checkC1 = new CheckBox("Show C1");
		checkC1.setSelected(drawC1);
		checkC1.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				drawC1 = checkC1.isSelected();
				redraw();
			}
		});

		checkC2 = new CheckBox("Show C2");
		checkC2.setSelected(drawC2);
		checkC2.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				drawC2 = checkC2.isSelected();
				redraw();
			}
		});

		txtAnimationSeconds = new TextField("5");
		txtAnimationSeconds.getStyleClass().add("readonly-label");
		txtAnimationSeconds.setMaxWidth(40);

		btnZoomIn.setMinWidth(60);
		btnZoomOut.setMinWidth(60);
		btnZoomReset.setMinWidth(40);
		btnAnimate.setMinWidth(60);

		btnZoomIn.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				zoom += 0.2;
				redraw();
			}
		});

		btnZoomOut.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				zoom -= 0.2;
				zoom = Math.max(zoom, 1);
				redraw();
			}
		});

		btnZoomReset.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				zoom = 1;
				redraw();
			}
		});

		btnAnimate.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				try
				{
					double animateOverSeconds = Double.parseDouble(txtAnimationSeconds.getText());
					animate(animateOverSeconds);
				}
				catch (NumberFormatException nfe)
				{
				}
			}
		});

		hboxControls.getChildren().addAll(checkC1, checkC2, btnZoomIn, btnZoomOut, btnZoomReset, btnAnimate, txtAnimationSeconds);

		return hboxControls;
	}

	private HBox buildControlInfo()
	{
		HBox hboxControls = new HBox();

		hboxControls.setSpacing(10);
		hboxControls.setAlignment(Pos.CENTER_LEFT);
		hboxControls.setPadding(new Insets(12, 0, 0, 8));

		int addressLabelWidth = 128;

		lblNMethodCount = new Label();
		lblNMethodCount.setMinWidth(addressLabelWidth);
		lblNMethodCount.getStyleClass().add("readonly-label");
		
		lblLowAddress = new Label();
		lblLowAddress.setMinWidth(addressLabelWidth);
		lblLowAddress.getStyleClass().add("readonly-label");

		lblHighAddress = new Label();
		lblHighAddress.setMinWidth(addressLabelWidth);
		lblHighAddress.getStyleClass().add("readonly-label");

		lblAddressRange = new Label();
		lblAddressRange.setMinWidth(addressLabelWidth);
		lblAddressRange.getStyleClass().add("readonly-label");

		hboxControls.getChildren().addAll(new Label("NMethods"), lblNMethodCount, new Label("Lowest Address"), lblLowAddress,
				new Label("Highest Address"), lblHighAddress, new Label("Address Range Size"), lblAddressRange);

		return hboxControls;
	}

	private boolean preDraw()
	{
		boolean ok = false;

		pane.getChildren().clear();
		
		lblNMethodCount.setText(S_EMPTY);
		lblLowAddress.setText(S_EMPTY);
		lblHighAddress.setText(S_EMPTY);
		lblAddressRange.setText(S_EMPTY);

		nMethodInfo.clear();

		codeCacheData = mainUI.getCodeCacheWalkerResult();

		if (codeCacheData != null)
		{
			lowAddress = codeCacheData.getLowestAddress();
			highAddress = codeCacheData.getHighestAddress();

			addressRange = highAddress - lowAddress;
			addressRange *= 1.005;

			width = scrollPane.getWidth() * zoom;
			height = pane.getHeight();

			pane.setPrefWidth(width);

			int eventCount = codeCacheData.getEvents().size();

			lblNMethodCount.setText(Integer.toString(eventCount));

			lblLowAddress.setText(Long.toHexString(lowAddress));
			lblHighAddress.setText(Long.toHexString(highAddress));
			lblAddressRange.setText(NumberFormat.getNumberInstance().format(addressRange));

			ok = true;
		}

		return ok;
	}

	public void redraw()
	{
		if (!preDraw())
		{
			return;
		}

		// long start = System.currentTimeMillis();

		IMetaMember selectedMember = mainUI.getSelectedMember();

		Compilation selectedCompilation = selectedMember == null ? null : selectedMember.getSelectedCompilation();

		List<CodeCacheEvent> eventsOfSelectedMember = new ArrayList<>();

		Color fillColour;

		for (CodeCacheEvent event : codeCacheData.getEvents())
		{
			if (!showEvent(event))
			{
				continue;
			}
			
			final Compilation eventCompilation = event.getCompilation();

			final IMetaMember compilationMember = eventCompilation.getMember();

			if (eventCompilation != null)
			{			
				if (selectedMember != null && selectedMember.equals(compilationMember))
				{
					eventsOfSelectedMember.add(event);
				}
				else
				{
					long addressOffset = event.getNativeAddress() - lowAddress;

					double scaledAddress = (double) addressOffset / (double) addressRange;

					double scaledSize = (double) event.getNativeCodeSize() / (double) addressRange;

					int latestCompilationIndex = compilationMember.getCompilations().size() - 1;

					if (eventCompilation.getIndex() == latestCompilationIndex)
					{
						fillColour = LATEST_COMPILATION;
					}
					else
					{
						fillColour = NOT_LATEST_COMPILATION;
					}

					double x = scaledAddress * width;
					double y = 0;
					double w = scaledSize * width;
					double h = height;

					plotCompilation(x, y, w, h, fillColour, compilationMember, eventCompilation.getIndex(), true);
				}
			}
		}

		for (CodeCacheEvent event : eventsOfSelectedMember)
		{
			long addressOffset = event.getNativeAddress() - lowAddress;

			double scaledAddress = (double) addressOffset / (double) addressRange;

			double scaledSize = (double) event.getNativeCodeSize() / (double) addressRange;

			double x = scaledAddress * width;
			double y = 0;
			double w = scaledSize * width;
			double h = height;

			final Compilation eventCompilation = event.getCompilation();

			if (event.getCompilation().equals(selectedCompilation))
			{
				fillColour = SELECTED_COMPILATION;

				nMethodInfo.setInfo(event, eventCompilation);
			}
			else
			{
				fillColour = OTHER_MEMBER_COMPILATIONS;
			}

			plotCompilation(x, y, w, h, fillColour, selectedMember, eventCompilation.getIndex(), true);

			plotMarker(x, h, w, selectedMember, eventCompilation.getIndex());
		}

		// long stop = System.currentTimeMillis();

		// System.out.println("redraw " + (stop - start));
	}

	private boolean showEvent(CodeCacheEvent event)
	{
		boolean result = true;
	
		int level = event.getCompilationLevel();
		
		if (!drawC1 && level >= 1 && level <= 3)
		{
			result = false;
		}
		
		if (!drawC2 && level == 4)
		{
			result = false;
		}
		
		return result;
	}
	
	private void plotMarker(double x, double h, double w, IMetaMember compilationMember, int compilationIndex)
	{
		double side = h * 0.04;
		double centre = x + w / 2;

		double top = h - side;
		double left = centre - side / 2;
		double right = centre + side / 2;
		double bottom = h;

		Polygon triangle = new Polygon();
		triangle.getPoints().addAll(new Double[] {
				left,
				bottom,
				centre,
				top,
				right,
				bottom });

		triangle.setFill(Color.WHITE);
		triangle.setStroke(Color.BLACK);

		attachListener(triangle, compilationMember, compilationIndex);

		pane.getChildren().add(triangle);
	}

	private void plotCompilation(double x, double y, double w, double h, Color fillColour, IMetaMember compilationMember,
			int compilationIndex, boolean clickHandler)
	{
		Rectangle rect = new Rectangle(x, y, w, h);

		rect.setFill(fillColour);

		if (clickHandler)
		{
			attachListener(rect, compilationMember, compilationIndex);
		}

		pane.getChildren().add(rect);
	}

	private void attachListener(Shape shape, final IMetaMember compilationMember, final int compilationIndex)
	{
		shape.setOnMouseClicked(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent arg0)
			{
				mainUI.setCompilationOnSelectedMember(compilationMember, compilationIndex);
			}
		});
	}

	private void animate(double targetSeconds)
	{
		if (!preDraw())
		{
			return;
		}

		final List<CodeCacheEvent> events = codeCacheData.getEvents();

		final int eventCount = events.size();

		double framesPerSecond = 60;

		double frameCount = targetSeconds * framesPerSecond;

		final double eventsPerFrame = (double) eventCount / frameCount;

		final double secondsPerEvent = targetSeconds / (double) eventCount;

		final double nanoSecondsPerEvent = 1_000_000_000 * secondsPerEvent;

		AnimationTimer timer = new AnimationTimer()
		{
			private int currentEvent;

			private long lastHandledAt = 0;

			@Override
			public void handle(long now)
			{
				double realEventsPerFrame = eventsPerFrame;

				if (eventsPerFrame < 1.0)
				{
					realEventsPerFrame = 1;

					if (now - lastHandledAt < nanoSecondsPerEvent)
					{
						return;
					}

					lastHandledAt = now;
				}

				for (int i = 0; i < realEventsPerFrame; i++)
				{
					if (currentEvent >= eventCount)
					{
						stop();
						break;
					}

					CodeCacheEvent event = events.get(currentEvent++);
					
					if (!showEvent(event))
					{
						continue;
					}
					
					final Compilation eventCompilation = event.getCompilation();

					final IMetaMember compilationMember = eventCompilation.getMember();

					if (eventCompilation != null)
					{						
						long addressOffset = event.getNativeAddress() - lowAddress;

						double scaledAddress = (double) addressOffset / (double) addressRange;

						double scaledSize = (double) event.getNativeCodeSize() / (double) addressRange;

						int latestCompilationIndex = compilationMember.getCompilations().size() - 1;

						Color fillColour;

						if (eventCompilation.getIndex() == latestCompilationIndex)
						{
							fillColour = LATEST_COMPILATION;
						}
						else
						{
							fillColour = NOT_LATEST_COMPILATION;
						}

						double x = scaledAddress * width;
						double y = 0;
						double w = scaledSize * width;
						double h = height;

						plotCompilation(x, y, w, h, fillColour, compilationMember, eventCompilation.getIndex(), false);
					}
				}
			}

			@Override
			public void start()
			{
				super.start();
			}

			@Override
			public void stop()
			{
				super.stop();
				btnAnimate.setDisable(false);

				redraw();
			}
		};

		btnAnimate.setDisable(true);

		timer.start();
	}

	void selectPrevCompilation()
	{
		IMetaMember selectedMember = mainUI.getSelectedMember();

		if (selectedMember != null && selectedMember.getSelectedCompilation() != null)
		{
			int prevIndex = selectedMember.getSelectedCompilation().getIndex() - 1;

			mainUI.setCompilationOnSelectedMember(selectedMember, prevIndex);
		}
	}

	void selectNextCompilation()
	{
		IMetaMember selectedMember = mainUI.getSelectedMember();

		if (selectedMember != null && selectedMember.getSelectedCompilation() != null)
		{
			int nextIndex = selectedMember.getSelectedCompilation().getIndex() + 1;

			mainUI.setCompilationOnSelectedMember(selectedMember, nextIndex);
		}
	}
}