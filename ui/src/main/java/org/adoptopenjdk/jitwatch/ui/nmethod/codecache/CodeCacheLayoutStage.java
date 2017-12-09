/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.nmethod.codecache;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_HEX_PREFIX;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.compilation.codecache.CodeCacheWalkerResult;
import org.adoptopenjdk.jitwatch.model.CodeCacheEvent;
import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.ui.main.JITWatchUI;
import org.adoptopenjdk.jitwatch.ui.nmethod.AbstractNMethodStage;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class CodeCacheLayoutStage extends AbstractNMethodStage
{
	private CodeCacheWalkerResult codeCacheData;

	private long lowAddress;
	private long highAddress;
	private long addressRange;

	private double width;
	private double height;

	private Label lblNMethodCount;
	private Label lblLowAddress;
	private Label lblHighAddress;
	private Label lblAddressRange;

	private Button btnAnimate;

	private CheckBox checkC1;
	private CheckBox checkC2;

	private boolean drawC1 = true;
	private boolean drawC2 = true;

	private TextField txtAnimationSeconds;

	private static final Color NOT_LATEST_COMPILATION = Color.rgb(96, 0, 0);

	private static final Color LATEST_COMPILATION = COLOR_UNSELECTED_COMPILATION;

	public CodeCacheLayoutStage(final JITWatchUI parent)
	{
		super(parent, "Code Cache Layout");
	}

	@Override
	protected VBox buildControls(Scene scene)
	{
		VBox vBoxControls = new VBox();

		vBoxControls.getChildren().addAll(buildControlButtons(scene), buildControlInfo());

		return vBoxControls;
	}

	private HBox buildControlButtons(Scene scene)
	{
		HBox hboxControls = new HBox();

		hboxControls.setSpacing(10);
		hboxControls.setAlignment(Pos.CENTER_LEFT);
		hboxControls.setPadding(new Insets(4, 4, 0, 8));

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

		Region spacerStatus = new Region();
		HBox.setHgrow(spacerStatus, Priority.ALWAYS);

		Button buttonSnapShot = UserInterfaceUtil.getSnapshotButton(scene, "CodeCache");

		hboxControls.getChildren().addAll(checkC1, checkC2, btnZoomIn, btnZoomOut, btnZoomReset, btnAnimate, txtAnimationSeconds,
				spacerStatus, buttonSnapShot);

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
		clear();
		
		boolean ok = false;

		lblNMethodCount.setText(S_EMPTY);
		lblLowAddress.setText(S_EMPTY);
		lblHighAddress.setText(S_EMPTY);
		lblAddressRange.setText(S_EMPTY);

		codeCacheData = parent.getCodeCacheWalkerResult();

		if (codeCacheData != null && !codeCacheData.getEvents().isEmpty())
		{
			lowAddress = codeCacheData.getLowestAddress();
			highAddress = codeCacheData.getHighestAddress();

			addressRange = highAddress - lowAddress;
			addressRange *= 1.01;

			width = scrollPane.getWidth() * zoom;
			height = pane.getHeight();

			pane.setPrefWidth(width);

			int eventCount = codeCacheData.getEvents().size();

			lblNMethodCount.setText(Integer.toString(eventCount));

			lblLowAddress.setText(S_HEX_PREFIX + Long.toHexString(lowAddress));
			lblHighAddress.setText(S_HEX_PREFIX + Long.toHexString(highAddress));
			lblAddressRange.setText(NumberFormat.getNumberInstance().format(highAddress - lowAddress));

			ok = true;
		}

		return ok;
	}

	@Override
	public void redraw()
	{
		if (!preDraw())
		{
			return;
		}

		//TimerUtil.timerStart(getClass().getName() + ".redraw()");

		IMetaMember selectedMember = parent.getSelectedMember();

		if (selectedMember == null)
		{
			return;
		}

		Compilation selectedCompilation = selectedMember == null ? null : selectedMember.getSelectedCompilation();

		List<CodeCacheEvent> eventsOfSelectedMember = new ArrayList<>();

		Color fillColour;
		
		double paneHeight = pane.getHeight();

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

					plotCompilation(x, y, w, paneHeight, fillColour, eventCompilation, true);
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

			final Compilation eventCompilation = event.getCompilation();

			if (event.getCompilation().equals(selectedCompilation))
			{
				fillColour = COLOR_SELECTED_COMPILATION;
			}
			else
			{
				fillColour = COLOR_OTHER_MEMBER_COMPILATIONS;
			}

			plotCompilation(x, y, w, paneHeight, fillColour, eventCompilation, true);

			plotMarker(x, paneHeight, eventCompilation);
		}

		//TimerUtil.timerEnd(getClass().getName() + ".redraw()");
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

	private void plotCompilation(double x, double y, double w, double h, Color fillColour, Compilation compilation,
			boolean clickHandler)
	{
		Rectangle rect = new Rectangle(x, y, w, h);

		rect.setFill(fillColour);

		if (clickHandler)
		{
			attachListener(rect, compilation);
		}

		pane.getChildren().add(rect);
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

		final double eventsPerFrame = eventCount / frameCount;

		final double secondsPerEvent = targetSeconds / eventCount;

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

						plotCompilation(x, y, w, h, fillColour, eventCompilation, false);
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
}
