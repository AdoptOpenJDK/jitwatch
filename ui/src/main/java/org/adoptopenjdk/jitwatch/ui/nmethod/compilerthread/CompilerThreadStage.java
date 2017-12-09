/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.nmethod.compilerthread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.CompilerThread;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.ui.main.JITWatchUI;
import org.adoptopenjdk.jitwatch.ui.nmethod.AbstractNMethodStage;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;

public class CompilerThreadStage extends AbstractNMethodStage
{
	private int maxNativeSize = 0;
	private int maxBytecodeSize = 0;

	private long minTime;
	private long maxTime;
	private long timeRange;
	private long maxQueueLength = 0;

	private double panePlotWidth;
	private double paneLabelWidth = 48;

	private double paneWidth;
	private double paneHeight;

	private Group contextualControls;

	private CheckBox cbOnlyFailures;
	private boolean showOnlyFailedCompiles = false;

	private enum PlotMode
	{
		NATIVE_SIZE, BYTECODE_SIZE, EXPANSIONS, TIMINGS, QUEUE_LENGTH
	}

	private class QueueCounter
	{
		private long timestamp;
		private boolean add;
		private Compilation compilation;

		public QueueCounter(long timestamp, boolean add, Compilation compilation)
		{
			this.timestamp = timestamp;
			this.add = add;
			this.compilation = compilation;
		}

		public long getTimestamp()
		{
			return timestamp;
		}

		public boolean isAdd()
		{
			return add;
		}

		public Compilation getCompilation()
		{
			return compilation;
		}
	}

	private PlotMode plotMode = PlotMode.NATIVE_SIZE;

	public CompilerThreadStage(final JITWatchUI parent)
	{
		super(parent, "Compiler Thread Activity");
	}

	@Override
	protected VBox buildControls(Scene scene)
	{
		VBox vBoxControls = new VBox();

		vBoxControls.getChildren().addAll(buildControlButtons(scene));

		return vBoxControls;
	}

	private void findRanges()
	{
		List<CompilerThread> threads = parent.getJITDataModel().getCompilerThreads();

		int compilerThreadCount = threads.size();

		for (int i = 0; i < compilerThreadCount; i++)
		{
			CompilerThread thread = threads.get(i);

			long earliestQueuedTime = thread.getEarliestQueuedTime();

			long latestEmittedTime = thread.getLatestNMethodEmittedTime();

			if (i == 0)
			{
				minTime = earliestQueuedTime;
				maxTime = latestEmittedTime;
				maxNativeSize = thread.getLargestNativeSize();
				maxBytecodeSize = thread.getLargestBytecodeSize();
			}
			else
			{
				minTime = Math.min(minTime, earliestQueuedTime);
				maxTime = Math.max(maxTime, latestEmittedTime);
				maxNativeSize = Math.max(maxNativeSize, thread.getLargestNativeSize());
				maxBytecodeSize = Math.max(maxBytecodeSize, thread.getLargestBytecodeSize());

			}
		}
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

		btnZoomIn.setMinWidth(60);
		btnZoomOut.setMinWidth(60);
		btnZoomReset.setMinWidth(40);

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

		Region spacerStatus = new Region();
		HBox.setHgrow(spacerStatus, Priority.ALWAYS);

		Button buttonSnapShot = UserInterfaceUtil.getSnapshotButton(scene, "CompilerThreads");

		contextualControls = new Group();

		hboxControls.getChildren().addAll(btnZoomIn, btnZoomOut, btnZoomReset, buildModeHBox(), contextualControls, spacerStatus,
				buttonSnapShot);

		return hboxControls;
	}

	private boolean preDraw()
	{
		clear();
		
		boolean ok = false;

		paneWidth = scrollPane.getWidth() * zoom;
		panePlotWidth = paneWidth - paneLabelWidth;
		paneHeight = pane.getHeight();

		pane.setPrefWidth(paneWidth);

		Rectangle background = new Rectangle(getXOffset(), 0, panePlotWidth, paneHeight);
		pane.getChildren().add(background);

		List<CompilerThread> threads = parent.getJITDataModel().getCompilerThreads();

		if (threads != null && !threads.isEmpty())
		{
			findRanges();

			timeRange = maxTime - minTime;
			timeRange *= 1.01;

			ok = true;
		}

		return ok;
	}

	private double getXOffset()
	{
		return paneLabelWidth;
	}

	@Override
	public void redraw()
	{		
		if (!preDraw())
		{
			return;
		}

		//TimerUtil.timerStart(getClass().getName() + ".redraw()");

		List<CompilerThread> threads = parent.getJITDataModel().getCompilerThreads();

		double rowHeight = paneHeight / threads.size();

		maxQueueLength = 0;

		double usableHeight = rowHeight * 0.9;

		double y = rowHeight / 2;

		for (CompilerThread thread : threads)
		{
			Rectangle threadBackground = new Rectangle(getXOffset(), y - usableHeight / 2, panePlotWidth, usableHeight);
			threadBackground.setFill(Color.rgb(32, 32, 32));
			pane.getChildren().add(threadBackground);

			if (plotMode == PlotMode.QUEUE_LENGTH)
			{
				plotQueueLengths(thread, y, usableHeight);
			}
			else
			{
				plotThread(thread, y, usableHeight);
			}

			y += rowHeight;
		}

		//TimerUtil.timerEnd(getClass().getName() + ".redraw()");
	}

	private void plotThread(CompilerThread thread, double y, double rowHeight)
	{
		IMetaMember selectedMember = parent.getSelectedMember();

		Compilation selectedCompilation = (selectedMember == null) ? null : selectedMember.getSelectedCompilation();

		List<Compilation> compilations = thread.getCompilations();

		plotThreadHeader(thread, y, rowHeight);

		Color fillColour;

		boolean isCompilationOfSelectedMember;

		for (Compilation compilation : compilations)
		{
			if (selectedMember != null && selectedMember.equals(compilation.getMember()))
			{
				if (compilation.equals(selectedCompilation))
				{
					fillColour = COLOR_SELECTED_COMPILATION;
				}
				else
				{
					fillColour = COLOR_OTHER_MEMBER_COMPILATIONS;
				}

				isCompilationOfSelectedMember = true;
			}
			else
			{
				fillColour = COLOR_UNSELECTED_COMPILATION;

				isCompilationOfSelectedMember = false;
			}

			if (plotMode == PlotMode.NATIVE_SIZE)
			{
				if (!compilation.isFailed())
				{
					plotNativeSize(compilation, y, rowHeight, fillColour, isCompilationOfSelectedMember);
				}
			}
			else if (plotMode == PlotMode.BYTECODE_SIZE)
			{
				if (!compilation.isFailed())
				{
					plotBytecodeSize(compilation, y, rowHeight, fillColour, isCompilationOfSelectedMember);
				}
			}
			else if (plotMode == PlotMode.EXPANSIONS)
			{
				if (!compilation.isFailed())
				{
					plotExpansions(compilation, y, rowHeight, fillColour, isCompilationOfSelectedMember);
				}
			}
			else if (plotMode == PlotMode.TIMINGS)
			{
				if (!showOnlyFailedCompiles || (showOnlyFailedCompiles == compilation.isFailed()))
				{
					plotQueuedCompileTimes(compilation, y, rowHeight, fillColour, isCompilationOfSelectedMember);
				}
			}
		}
	}

	private void plotQueueLengths(CompilerThread thread, double y, double rowHeight)
	{
		List<QueueCounter> counters = getQueueCounters(thread);
	
		long lastTimestamp = -1;

		plotThreadHeader(thread, y, rowHeight);

		//long timeBlocked = 0;

		List<Compilation> liveQueue = new LinkedList<>();

		double oneHeight = (1.0 / maxQueueLength) * rowHeight;
		
		oneHeight = Math.min(oneHeight, rowHeight / 20);
				
		IMetaMember selectedMember = parent.getSelectedMember();

		Compilation selectedCompilation = (selectedMember == null) ? null : selectedMember.getSelectedCompilation();

		for (QueueCounter counter : counters)
		{
			long timestamp = counter.getTimestamp();
			
			int queueLength = liveQueue.size();

			if (lastTimestamp != -1)
			{
				//long deltaTime = timestamp - lastTimestamp;

				//timeBlocked += deltaTime;

				double x1 = getScaledTimestampX(lastTimestamp);
				double x2 = getScaledTimestampX(timestamp);

				double baseLine = y + rowHeight / 2;
				
				boolean compilationMemberInQueue = false;

				for (int i = 0; i < queueLength; i++)
				{
					Compilation compilation = liveQueue.get(i);
					
					double startY = baseLine - (i+1) * oneHeight;
					
					double rectWidth = x2 -x1;
					
					Rectangle rect = new Rectangle(x1, startY, rectWidth, oneHeight);
				
					Color fillColor = Color.WHITE;
					
					if (compilation.getMember().equals(selectedMember))
					{
						fillColor = COLOR_SELECTED_COMPILATION;
						
						compilationMemberInQueue = true;
					}					
					
					rect.setFill(fillColor);					
					pane.getChildren().add(rect);

					if (rectWidth > 4)
					{
						rect.setStroke(Color.BLACK);
					}
					
					attachListener(rect, compilation);
				}
				
				if (compilationMemberInQueue)
				{
					plotMarker(x1, y - rowHeight / 2, selectedCompilation, true);
				}
			}
			
			Compilation compilation = counter.getCompilation();

			if (counter.isAdd())
			{
				liveQueue.add(compilation);
			}
			else
			{
				liveQueue.remove(compilation);
			}
			
			lastTimestamp = timestamp;

		} // end for

		//System.out.println("Thread " + thread.getThreadName() + " blocked for " + timeBlocked);
	}

	private List<QueueCounter> getQueueCounters(CompilerThread thread)
	{
		List<QueueCounter> result = new ArrayList<>();

		for (Compilation compilation : thread.getCompilations())
		{
			result.add(new QueueCounter(compilation.getStampTaskQueued(), true, compilation));
			result.add(new QueueCounter(compilation.getStampTaskCompilationStart(), false, compilation));
		}

		Collections.sort(result, new Comparator<QueueCounter>()
		{
			@Override
			public int compare(QueueCounter q1, QueueCounter q2)
			{
				return Long.compare(q1.getTimestamp(), q2.getTimestamp());
			}
		});

		int tempQueueLength = 0;

		for (QueueCounter counter : result)
		{
			if (counter.isAdd())
			{
				tempQueueLength++;
			}
			else
			{
				tempQueueLength--;
			}

			maxQueueLength = Math.max(tempQueueLength, maxQueueLength);
		}

		return result;
	}

	private void plotThreadHeader(CompilerThread thread, double y, double rowHeight)
	{
		switch (plotMode)
		{
		case NATIVE_SIZE:
		case BYTECODE_SIZE:
		case EXPANSIONS:
			plotHorizontalLine(y);
			plotThreadName(getCompilerThreadName(thread), y);
			break;
		case TIMINGS:
			double yQueued = y - rowHeight * 0.25;
			double yCompiled = y + rowHeight * 0.25;

			plotHorizontalLine(yQueued);
			plotHorizontalLine(yCompiled);

			plotThreadName(getCompilerThreadName(thread) + "-Q", yQueued);
			plotThreadName(getCompilerThreadName(thread) + "-C", yCompiled);

			break;
		case QUEUE_LENGTH:
			plotHorizontalLine(y + rowHeight / 2);
			plotThreadName(getCompilerThreadName(thread), y + rowHeight / 2);
			break;
		}
	}

	private double getScaledTimestampX(long timestamp)
	{
		return getXOffset() + ((timestamp - minTime) / (double) timeRange) * panePlotWidth;
	}

	private String getCompilerThreadName(CompilerThread thread)
	{
		String result;

		String threadName = thread.getThreadName();

		if (threadName != null && threadName.length() >= 3)
		{
			result = threadName.substring(0, 3);
		}
		else
		{
			result = "??";
		}

		return result;
	}

	private void plotHorizontalLine(double y)
	{
		Line line = new Line(getXOffset(), y, paneWidth, y);
		line.setStroke(Color.DARKGREY);
		pane.getChildren().add(line);
	}

	private void plotThreadName(String name, double y)
	{
		Text textThreadName = new Text(4, y, name);
		textThreadName.setFill(Color.WHITE);
		textThreadName.setFontSmoothingType(FontSmoothingType.LCD);

		pane.getChildren().add(textThreadName);
	}

	private void plotNativeSize(Compilation compilation, double y, double rowHeight, Color fillColour,
			boolean isCompilationOfSelectedMember)
	{
		long stampCompilationStart = compilation.getStampTaskCompilationStart();
		long stampNMethodEmitted = compilation.getStampNMethodEmitted();

		int nativeSize = compilation.getNativeSize();

		double x1 = getScaledTimestampX(stampCompilationStart);

		double x2 = getScaledTimestampX(stampNMethodEmitted);

		Rectangle rect;

		double nativeSizeHeight = (double) nativeSize / (double) maxNativeSize * rowHeight;

		rect = new Rectangle(x1, y - nativeSizeHeight / 2, x2 - x1, nativeSizeHeight);

		rect.setFill(fillColour);
		attachListener(rect, compilation);
		pane.getChildren().add(rect);

		if (isCompilationOfSelectedMember)
		{
			plotMarker(x1, y + rowHeight / 2, compilation);
		}
	}

	private void plotExpansions(Compilation compilation, double y, double rowHeight, Color fillColour,
			boolean isCompilationOfSelectedMember)
	{
		long stampCompilationStart = compilation.getStampTaskCompilationStart();
		long stampNMethodEmitted = compilation.getStampNMethodEmitted();

		int nativeSize = compilation.getNativeSize();

		double x1 = getScaledTimestampX(stampCompilationStart);

		double x2 = getScaledTimestampX(stampNMethodEmitted);

		Polygon polygon = new Polygon();
		double maxBytes = Math.max(maxBytecodeSize, maxNativeSize);

		int bytecodeSize = compilation.getBytecodeSize();

		double bytecodeSizeHeight = bytecodeSize / maxBytes * rowHeight;
		double nativeSizeHeight = nativeSize / maxBytes * rowHeight;

		polygon.getPoints().addAll(x1, y - bytecodeSizeHeight / 2, x1, y + bytecodeSizeHeight / 2, x2, y + nativeSizeHeight / 2, x2,
				y - nativeSizeHeight / 2);

		polygon.setFill(fillColour);
		attachListener(polygon, compilation);
		pane.getChildren().add(polygon);

		if (isCompilationOfSelectedMember)
		{
			plotMarker(x1, y + rowHeight / 2, compilation);
		}
	}

	private void plotBytecodeSize(Compilation compilation, double y, double rowHeight, Color fillColour,
			boolean isCompilationOfSelectedMember)
	{
		long stampCompilationStart = compilation.getStampTaskCompilationStart();
		long stampNMethodEmitted = compilation.getStampNMethodEmitted();

		int bytecodeSize = compilation.getBytecodeSize();

		double x = getScaledTimestampX(stampCompilationStart);

		double w = getScaledTimestampX(stampNMethodEmitted) - x;

		double bytecodeSizeHeight = (double) bytecodeSize / (double) maxBytecodeSize * rowHeight;

		Rectangle rect = new Rectangle(x, y - bytecodeSizeHeight / 2, w, bytecodeSizeHeight);
		rect.setFill(fillColour);
		attachListener(rect, compilation);

		pane.getChildren().add(rect);

		if (isCompilationOfSelectedMember)
		{
			plotMarker(x, y + rowHeight / 2, compilation);
		}
	}

	private void plotQueuedCompileTimes(Compilation compilation, double y, double rowHeight, Color fillColour,
			boolean isCompilationOfSelectedMember)
	{
		int nativeSize = compilation.getNativeSize();

		double xQueued = getScaledTimestampX(compilation.getStampTaskQueued());
		double xCompileStart = getScaledTimestampX(compilation.getStampTaskCompilationStart());
		double xNMethodEmitted = getScaledTimestampX(compilation.getStampNMethodEmitted());

		double yQueued = y - rowHeight * 0.25;
		double yCompiled = y + rowHeight * 0.25;

		double halfRowHeight = rowHeight / 2;

		double nativeSizeHeight = (double) nativeSize / (double) maxNativeSize * halfRowHeight;

		Color joinColor;
		Color queuedColor;

		if (compilation.isFailed())
		{
			joinColor = Color.RED;
			queuedColor = Color.RED;
		}
		else
		{
			joinColor = fillColour;
			queuedColor = fillColour;
		}

		Line join = new Line(xQueued, yQueued, xCompileStart, yCompiled + nativeSizeHeight / 2);
		join.setStroke(joinColor);
		pane.getChildren().add(join);
		attachListener(join, compilation);

		double radius = 4;

		Circle queuedCircle = new Circle(xQueued, yQueued, radius);
		queuedCircle.setFill(queuedColor);
		pane.getChildren().add(queuedCircle);
		attachListener(queuedCircle, compilation);

		if (!compilation.isFailed())
		{
			Rectangle compliedRect = new Rectangle(xCompileStart, yCompiled - nativeSizeHeight / 2, xNMethodEmitted - xCompileStart,
					nativeSizeHeight);

			compliedRect.setFill(fillColour);

			pane.getChildren().add(compliedRect);

			attachListener(compliedRect, compilation);
		}

		if (isCompilationOfSelectedMember)
		{
			plotMarker(xCompileStart, y + rowHeight / 2, compilation);
		}
	}

	private HBox getContextualControlsTimings()
	{
		HBox hBox = new HBox();

		cbOnlyFailures = new CheckBox("Only failed compilations");
		cbOnlyFailures.setSelected(showOnlyFailedCompiles);
		cbOnlyFailures.selectedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal)
			{
				showOnlyFailedCompiles = newVal;
				redraw();
			}
		});

		hBox.getChildren().add(cbOnlyFailures);

		return hBox;
	}

	private HBox getContextualControlsQueueLength()
	{
		return new HBox();
	}

	private HBox buildModeHBox()
	{
		final RadioButton rbNativeSize = new RadioButton("Native Sizes");
		final RadioButton rbBytecodeSize = new RadioButton("Bytecode Sizes");
		final RadioButton rbExpansions = new RadioButton("Expansions");
		final RadioButton rbTimings = new RadioButton("Timings");
		final RadioButton rbQueueLength = new RadioButton("Compiler Queues");

		final ToggleGroup groupMode = new ToggleGroup();

		rbNativeSize.setToggleGroup(groupMode);
		rbBytecodeSize.setToggleGroup(groupMode);
		rbExpansions.setToggleGroup(groupMode);
		rbTimings.setToggleGroup(groupMode);
		rbQueueLength.setToggleGroup(groupMode);

		rbNativeSize.setSelected(true);
		rbBytecodeSize.setSelected(false);
		rbExpansions.setSelected(false);
		rbTimings.setSelected(false);
		rbQueueLength.setSelected(false);

		groupMode.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
		{
			@Override
			public void changed(ObservableValue<? extends Toggle> arg0, Toggle arg1, Toggle arg2)
			{
				Toggle selected = groupMode.getSelectedToggle();

				if (selected != null)
				{
					if (selected.equals(rbNativeSize))
					{
						plotMode = PlotMode.NATIVE_SIZE;

						contextualControls.getChildren().clear();
					}
					else if (selected.equals(rbBytecodeSize))
					{
						plotMode = PlotMode.BYTECODE_SIZE;

						contextualControls.getChildren().clear();
					}
					else if (selected.equals(rbExpansions))
					{
						plotMode = PlotMode.EXPANSIONS;

						contextualControls.getChildren().clear();
					}
					else if (selected.equals(rbTimings))
					{
						plotMode = PlotMode.TIMINGS;

						contextualControls.getChildren().clear();
						contextualControls.getChildren().addAll(getContextualControlsTimings().getChildren());

					}
					else
					{
						plotMode = PlotMode.QUEUE_LENGTH;

						contextualControls.getChildren().clear();
						contextualControls.getChildren().addAll(getContextualControlsQueueLength().getChildren());
					}

					redraw();
				}
			}
		});

		HBox hBox = new HBox();
		hBox.setSpacing(16);

		hBox.getChildren().add(rbNativeSize);
		hBox.getChildren().add(rbBytecodeSize);
		hBox.getChildren().add(rbExpansions);
		hBox.getChildren().add(rbTimings);
		hBox.getChildren().add(rbQueueLength);

		return hBox;
	}
}
