/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui;

import com.chrisnewland.jitwatch.model.EventType;
import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.JITEvent;
import com.chrisnewland.jitwatch.model.JITStats;
import com.chrisnewland.jitwatch.util.ParseUtil;
import com.chrisnewland.jitwatch.util.StringUtil;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.StageStyle;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public class TimeLineStage extends AbstractGraphStage
{

    private static final int VIEW_WIDTH = 640;
    private static final int VIEW_HEIGHT = 480;
    private static final int DEFAULT_FONT_SIZE = 10;
    private static final int DEFAULT_MARKER_DIAMETER = 8;
    private static final int Y_AXIS_MARGIN = 32;
    private static final int X_AXIS_MARGIN = 10;
    private static final int TWELVE_PIXELS = 12;

    public TimeLineStage(final JITWatchUI parent)
	{
		super(parent, VIEW_WIDTH, VIEW_HEIGHT, true);

		initStyle(StageStyle.DECORATED);

		StackPane root = new StackPane();
		Scene scene = new Scene(root, width, height);

		canvas.widthProperty().bind(root.widthProperty());
		canvas.heightProperty().bind(root.heightProperty());

		gc.setFont(new Font("monospace", DEFAULT_FONT_SIZE));

		root.getChildren().add(canvas);

		setTitle("JITWatch Compilations Timeline");

		setScene(scene);
		show();

		redraw();
	}

	@Override
	public final void redraw()
	{
		super.baseRedraw();

		List<JITEvent> events = parent.getJITDataModel().getEventListCopy();

		Collections.sort(events, new Comparator<JITEvent>()
		{
			@Override
			public int compare(JITEvent e1, JITEvent e2)
			{
				return Long.compare(e1.getStamp(), e2.getStamp());
			}
		});

		if (events.size() > 0)
		{
			minX = 0;
			maxX = events.get(events.size() - 1).getStamp();

			minY = 0;
			maxY = 0;

			for (JITEvent event : events)
			{
				if (event.getEventType() != EventType.QUEUE)
				{
					maxY++;
				}
			}

			double lastCX = GRAPH_GAP_LEFT + normaliseX(minX);
			double lastCY = GRAPH_GAP_Y + normaliseY(0);

			drawAxes();

			IMetaMember selectedMember = parent.getSelectedMember();

			double compiledStampTime = -1;

			if (selectedMember != null)
			{
				// last compile stamp write wins - plot all?
				String cStamp = selectedMember.getCompiledAttribute("stamp");

				if (cStamp != null)
				{
					compiledStampTime = ParseUtil.parseStamp(cStamp);
				}
			}

			Color colourMarker = Color.BLUE;
			
			gc.setFill(colourMarker);
			gc.setStroke(colourMarker);
			
			int cumC = 0;
			int markerDiameter = DEFAULT_MARKER_DIAMETER;

			for (JITEvent event : events)
			{
				if (event.getEventType() != EventType.QUEUE)
				{
					long stamp = event.getStamp();

					cumC++;

					double x = GRAPH_GAP_LEFT + normaliseX(stamp);
					double y = GRAPH_GAP_Y + normaliseY(cumC);

					gc.setLineWidth(2);
					gc.strokeLine(fix(lastCX), fix(lastCY), fix(x), fix(y));
					gc.setLineWidth(1);

					lastCX = x;
					lastCY = y;

					if (compiledStampTime != -1 && stamp > compiledStampTime)
					{
						double smX = GRAPH_GAP_LEFT + normaliseX(compiledStampTime);

						gc.fillOval(fix(smX - markerDiameter / 2), fix(y - markerDiameter / 2), fix(markerDiameter),
								fix(markerDiameter));

						String line1 = selectedMember.toString();

						String compiler = selectedMember.getCompiledAttribute(ATTR_COMPILER);

						if (compiler == null)
						{
							compiler = selectedMember.getCompiledAttribute(ATTR_COMPILE_KIND);

							if (compiler == null)
							{
								compiler = "unknown!";
							}
						}

						String line2 = "Compiled at " + StringUtil.formatTimestamp((long) compiledStampTime, true) + " using "
								+ compiler;

						String compiletime = selectedMember.getCompiledAttribute("compileMillis");

						if (compiletime != null)
						{
							line2 += " took " + compiletime + "ms";
						}

						double legendY = y;

						if (legendY > GRAPH_GAP_Y + chartHeight - Y_AXIS_MARGIN)
						{
							legendY = GRAPH_GAP_Y + chartHeight - Y_AXIS_MARGIN;
						}

						gc.strokeText(line1, fix(smX + X_AXIS_MARGIN), fix(legendY));
						gc.strokeText(line2, fix(smX + X_AXIS_MARGIN),
                                fix(legendY + (Y_AXIS_MARGIN / 2)));

						compiledStampTime = -1;
					}
				}
			}

			showStatsLegend(gc);
		}
	}

	private void showStatsLegend(GraphicsContext gc)
	{
		JITStats stats = parent.getJITDataModel().getJITStats();

		StringBuilder compiledStatsBuilder = new StringBuilder();
		compiledStatsBuilder.append("Compiled: ").append(stats.getTotalCompiledMethods());
		compiledStatsBuilder.append(" (C1: ").append(stats.getCountC1()).append(S_CLOSE_PARENTHESES);
		compiledStatsBuilder.append(" (C2: ").append(stats.getCountC2()).append(S_CLOSE_PARENTHESES);
		compiledStatsBuilder.append(" (C2N: ").append(stats.getCountC2N()).append(S_CLOSE_PARENTHESES);
		compiledStatsBuilder.append(" (OSR: ").append(stats.getCountOSR()).append(S_CLOSE_PARENTHESES);
		gc.setStroke(Color.BLACK);
		gc.strokeText(compiledStatsBuilder.toString(), fix(GRAPH_GAP_LEFT), fix(TWELVE_PIXELS));
	}
}