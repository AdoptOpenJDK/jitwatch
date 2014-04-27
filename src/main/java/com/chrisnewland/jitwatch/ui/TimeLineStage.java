/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

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

        defineEventsSortingByUsingStamp(events);

        if (events.size() > 0)
		{
			minX = 0;
			maxX = events.get(events.size() - 1).getStamp();

			minY = 0;
			maxY = 0;

            countNumberOfNonQueueEventTypes(events);

            double lastCX = GRAPH_GAP_LEFT + normaliseX(minX);
			double lastCY = GRAPH_GAP_Y + normaliseY(0);

			drawAxes();

			IMetaMember selectedMember = parent.getSelectedMember();

			double compiledStampTime = -1;

            compiledStampTime = parseCompiledStampTimeUsingSelectedMember(selectedMember, compiledStampTime);

            Color colourMarker = Color.BLUE;
			
			gc.setFill(colourMarker);
			gc.setStroke(colourMarker);
			
			int cumC = 0;
			int markerDiameter = 8;

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

                    compiledStampTime = updateVisualsUsingCompiledStampTime(
                            selectedMember, compiledStampTime, markerDiameter, stamp, y);
                }
			}

			showStatsLegend(gc);
		}
	}

    private double updateVisualsUsingCompiledStampTime(IMetaMember selectedMember, double inCompiledStampTime, int markerDiameter, long stamp, double y) {
        double compiledStampTime = inCompiledStampTime;

        if ((compiledStampTime != -1) && (stamp > compiledStampTime))
        {
            double smX = GRAPH_GAP_LEFT + normaliseX(compiledStampTime);

            gc.fillOval(fix(smX - markerDiameter / 2), fix(y - markerDiameter / 2), fix(markerDiameter),
                    fix(markerDiameter));

            String line1 = selectedMember.toString();

            String compiler = parseCompilerAttributeUsing(selectedMember);

            String line2 = convertCompileTimeIntoString(selectedMember, compiledStampTime, compiler);

            double legendY = computerLegendYAxis(y);

            gc.strokeText(line1, fix(smX + 10), fix(legendY));
            gc.strokeText(line2, fix(smX + 10), fix(legendY + 16));

            compiledStampTime = -1;
        }
        return compiledStampTime;
    }

    private String convertCompileTimeIntoString(IMetaMember selectedMember, double compiledStampTime, String compiler) {
        String line2 = "Compiled at " + StringUtil.formatTimestamp((long) compiledStampTime, true) + " using "
                + compiler;

        String compiletime = selectedMember.getCompiledAttribute("compileMillis");

        if (compiletime != null)
        {
            line2 += " took " + compiletime + "ms";
        }
        return line2;
    }

    private double computerLegendYAxis(double y) {
        double legendY = y;

        if (legendY > GRAPH_GAP_Y + chartHeight - 32)
        {
            legendY = GRAPH_GAP_Y + chartHeight - 32;
        }
        return legendY;
    }

    private String parseCompilerAttributeUsing(IMetaMember selectedMember) {
        String compiler = selectedMember.getCompiledAttribute(ATTR_COMPILER);

        if (compiler == null)
        {
            compiler = selectedMember.getCompiledAttribute(ATTR_COMPILE_KIND);

            if (compiler == null)
            {
                compiler = "unknown!";
            }
        }
        return compiler;
    }

    private void defineEventsSortingByUsingStamp(List<JITEvent> events) {
        Collections.sort(events, new Comparator<JITEvent>() {
            @Override
            public int compare(JITEvent e1, JITEvent e2) {
                return Long.compare(e1.getStamp(), e2.getStamp());
            }
        });
    }

    private void countNumberOfNonQueueEventTypes(List<JITEvent> events) {
        for (JITEvent event : events)
        {
            if (event.getEventType() != EventType.QUEUE)
            {
                maxY++;
            }
        }
    }

    private double parseCompiledStampTimeUsingSelectedMember(IMetaMember selectedMember, double inCompiledStampTime) {
        double compiledStampTime = inCompiledStampTime;
        if (selectedMember != null)
        {
            // last compile stamp write wins - plot all?
            String cStamp = selectedMember.getCompiledAttribute("stamp");

            compiledStampTime = parseCompiledStampTime(compiledStampTime, cStamp);
        }
        return compiledStampTime;
    }

    private double parseCompiledStampTime(double inCompiledStampTime, String cStamp) {
        double compiledStampTime = inCompiledStampTime;
        if (cStamp != null)
        {
            compiledStampTime = ParseUtil.parseStamp(cStamp);
        }
        return compiledStampTime;
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
		gc.strokeText(compiledStatsBuilder.toString(), fix(GRAPH_GAP_LEFT), fix(12));
	}
}