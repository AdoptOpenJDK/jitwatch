/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.graphing;

import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.StageStyle;
import org.adoptopenjdk.jitwatch.model.EventType;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.JITEvent;
import org.adoptopenjdk.jitwatch.model.JITStats;
import org.adoptopenjdk.jitwatch.ui.JITWatchUI;
import org.adoptopenjdk.jitwatch.util.ParseUtil;
import org.adoptopenjdk.jitwatch.util.StringUtil;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

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

			double lastCX = graphGapLeft + normaliseX(minX);
			double lastCY = GRAPH_GAP_Y + normaliseY(0);

			drawAxes();

			IMetaMember selectedMember = parent.getSelectedMember();

			double compiledStampTime = getCompiledStampTime(selectedMember);

            Color colourMarker = Color.BLUE;

			gc.setFill(colourMarker);
			gc.setStroke(colourMarker);

            processJITEvents(events, lastCX, lastCY, selectedMember, compiledStampTime);

			showStatsLegend(gc);
		}
		else
		{
			gc.strokeText("No compilation information processed", fix(10), fix(10));
		}
	}

    private double getCompiledStampTime(IMetaMember selectedMember) {
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
        return compiledStampTime;
    }

    private void processJITEvents(List<JITEvent> events, double lastCX, double lastCY, IMetaMember selectedMember, double compiledStampTime) {
        int cumC = 0;
        int markerDiameter = 10;

        for (JITEvent event : events)
        {
            if (event.getEventType() != EventType.QUEUE)
            {
                long stamp = event.getStamp();

                cumC++;

                double x = graphGapLeft + normaliseX(stamp);
                double y = GRAPH_GAP_Y + normaliseY(cumC);

                gc.setLineWidth(2);
                gc.strokeLine(fix(lastCX), fix(lastCY), fix(x), fix(y));
                gc.setLineWidth(1);

                lastCX = x;
                lastCY = y;

                if (compiledStampTime != -1 && stamp > compiledStampTime)
                {
                    double smX = graphGapLeft + normaliseX(compiledStampTime);

                    double blobX = fix(smX - markerDiameter / 2);
                    double blobY = fix(y - markerDiameter / 2);

                    gc.fillOval(blobX, blobY, fix(markerDiameter),
                            fix(markerDiameter));

                    StringBuilder selectedItemBuilder = new StringBuilder();

                    selectedItemBuilder.append(selectedMember.toStringUnqualifiedMethodName(false));

                    String compiler = selectedMember.getCompiledAttribute(ATTR_COMPILER);

                    if (compiler == null)
                    {
                        compiler = selectedMember.getCompiledAttribute(ATTR_COMPILE_KIND);

                        if (compiler == null)
                        {
                            compiler = "unknown!";
                        }
                    }

                    selectedItemBuilder.append(" compiled at ")
                            .append(StringUtil.formatTimestamp((long) compiledStampTime, true)).append(" by ").append(compiler);

                    String compiletime = selectedMember.getCompiledAttribute("compileMillis");

                    if (compiletime != null)
                    {
                        selectedItemBuilder.append(" in ").append(compiletime).append("ms");
                    }

                    double approxWidth = selectedItemBuilder.length() * 5.5;

                    double selectedLabelX;

                    if (blobX + approxWidth > chartWidth)
                    {
                        selectedLabelX = blobX - approxWidth - 16;
                    }
                    else
                    {
                        selectedLabelX = blobX + 32;
                    }

                    double selectedLabelY = Math.min(blobY+8, GRAPH_GAP_Y + chartHeight - 32);

                    gc.setFill(Color.WHITE);
                    gc.setStroke(Color.BLACK);

                    gc.fillRect(fix(selectedLabelX-8), fix(selectedLabelY-12), fix(approxWidth), fix(18));
                    gc.strokeRect(fix(selectedLabelX-8), fix(selectedLabelY-12), fix(approxWidth), fix(18));

                    gc.strokeText(selectedItemBuilder.toString(), fix(selectedLabelX), fix(selectedLabelY));

                    compiledStampTime = -1;

                    gc.setStroke(Color.BLUE);

                }
            }
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
		gc.strokeText(compiledStatsBuilder.toString(), fix(graphGapLeft), fix(12));
	}
}