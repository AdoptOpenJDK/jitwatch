/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/blob/master/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
package com.chrisnewland.jitwatch.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.chrisnewland.jitwatch.core.JITEvent;
import com.chrisnewland.jitwatch.core.JITStats;
import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.util.StringUtil;

import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.StageStyle;

public class TimeLineStage extends AbstractGraphStage
{
	public TimeLineStage(final JITWatchUI parent)
	{
		super(parent, 640, 480, true);

		initStyle(StageStyle.DECORATED);

		StackPane root = new StackPane();
		Scene scene = new Scene(root, width, height);

		canvas.widthProperty().bind(root.widthProperty());
		canvas.heightProperty().bind(root.heightProperty());

		gc.setFont(new Font("monospace", 10));

		root.getChildren().add(canvas);

		setTitle("JITWatch Compilations Timeline");

		setScene(scene);
		show();

		redraw();
	}

	@Override
	public void redraw()
	{
		super.baseRedraw();

		List<JITEvent> events = parent.getJITEvents();

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
				if (event.isCompile())
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
				String cStamp = selectedMember.getCompiledAttribute("stamp");

				if (cStamp != null)
				{
					compiledStampTime = 1000 * Double.parseDouble(cStamp);
				}
			}

			Color colourMarker = Color.BLUE;
			
			gc.setFill(colourMarker);
			gc.setStroke(colourMarker);
			
			int cumC = 0;
			int markerDiameter = 8;

			for (JITEvent event : events)
			{
				if (event.isCompile())
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

						String compiler = selectedMember.getCompiledAttribute("compiler");

						if (compiler == null)
						{
							compiler = selectedMember.getCompiledAttribute("compile_kind");

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

						if (legendY > GRAPH_GAP_Y + chartHeight - 32)
						{
							legendY = GRAPH_GAP_Y + chartHeight - 32;
						}

						gc.strokeText(line1, fix(smX + 10), fix(legendY));
						gc.strokeText(line2, fix(smX + 10), fix(legendY + 16));

						compiledStampTime = -1;
					}
				}
			}

			showStatsLegend(gc);
		}
	}

	private void showStatsLegend(GraphicsContext gc)
	{
		JITStats stats = parent.getJITStats();

		StringBuilder compiledStatsBuilder = new StringBuilder();
		compiledStatsBuilder.append("Compiled: ").append(stats.getTotalCompiledMethods());
		compiledStatsBuilder.append(" (C1: ").append(stats.getCountC1()).append(")");
		compiledStatsBuilder.append(" (C2: ").append(stats.getCountC2()).append(")");
		compiledStatsBuilder.append(" (C2N: ").append(stats.getCountC2N()).append(")");
		compiledStatsBuilder.append(" (OSR: ").append(stats.getCountOSR()).append(")");
		gc.setStroke(Color.BLACK);
		gc.strokeText(compiledStatsBuilder.toString(), fix(GRAPH_GAP_LEFT), fix(12));
	}
}