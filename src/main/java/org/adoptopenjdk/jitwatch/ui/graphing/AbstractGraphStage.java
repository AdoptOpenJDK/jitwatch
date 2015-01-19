/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.graphing;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import org.adoptopenjdk.jitwatch.ui.JITWatchUI;
import org.adoptopenjdk.jitwatch.util.StringUtil;

public abstract class AbstractGraphStage extends Stage
{
	protected Canvas canvas;
	protected GraphicsContext gc;
	protected JITWatchUI parent;

	protected static double graphGapLeft = 20.5;
	protected static final double graphGapRight = 20.5;
	protected static final double graphGapTop = 20.5;

	protected static final int[] Y_SCALE = new int[21];

	protected double width;
	protected double height;
	protected double chartWidth;
	protected double chartHeight;

	protected long minX;
	protected long maxX;
	protected long minY;
	protected long maxY;

	protected long minXQ;
	protected long maxXQ;
	protected long minYQ;
	protected long maxYQ;

	private boolean xAxisTime = false;

	static
	{
		int multiplier = 1;

		for (int i = 0; i < Y_SCALE.length; i += 3)
		{
			Y_SCALE[i + 0] = 1 * multiplier;
			Y_SCALE[i + 1] = 2 * multiplier;
			Y_SCALE[i + 2] = 5 * multiplier;

			multiplier *= 10;
		}
	}

	public AbstractGraphStage(final JITWatchUI parent, int width, int height, boolean xAxisTime)
	{
		this.parent = parent;
		this.width = width;
		this.height = height;
		this.xAxisTime = xAxisTime;

		canvas = new Canvas(width, height);
		gc = canvas.getGraphicsContext2D();

		initStyle(StageStyle.DECORATED);

		setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				parent.handleStageClosed(AbstractGraphStage.this);
			}
		});

		class SceneResizeListener implements ChangeListener<Number>
		{
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2)
			{
				redraw();
			}
		}

		SceneResizeListener rl = new SceneResizeListener();

		canvas.widthProperty().addListener(rl);
		canvas.heightProperty().addListener(rl);
	}

	public abstract void redraw();

	protected void baseRedraw()
	{
		width = canvas.getWidth();
		height = canvas.getHeight();
		chartWidth = width - graphGapLeft - graphGapRight;
		chartHeight = height - graphGapTop * 2;

		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, width, height);
		gc.setFill(Color.rgb(210, 255, 255));
		gc.fillRect(graphGapLeft, graphGapTop, chartWidth, chartHeight);
		gc.setStroke(Color.BLACK);
		gc.strokeRect(graphGapLeft, graphGapTop, chartWidth, chartHeight);
	}

	protected void drawAxes()
	{
		if (xAxisTime)
		{
			drawXAxisTime();
		}
		else
		{
			drawXAxis();
		}

		drawYAxis();
	}

	private void drawXAxisTime()
	{
		long xInc = getXStepTime();

		minXQ = (minX / xInc) * xInc;

		maxXQ =  (1 + (maxX / xInc)) * xInc;

		long gridX = minXQ;

		while (gridX <= maxX)
		{
			double x = graphGapLeft + normaliseX(gridX);
			gc.strokeLine(fix(x), fix(graphGapTop), fix(x), fix(graphGapTop + chartHeight));

			boolean showMillis = maxX  < 5000;

			gc.strokeText(StringUtil.formatTimestamp(gridX, showMillis), fix(x), fix(graphGapTop + chartHeight + 12));

			gridX += xInc;
		}
	}

	private void drawXAxis()
	{
		long xInc = findScale(maxX - minX);

		minXQ = (minX / xInc) * xInc;

		maxXQ =  (1 + (maxX / xInc)) * xInc;

		long gridX = minXQ;

		while (gridX <= maxX)
		{
			double x = graphGapLeft + normaliseX(gridX);
			gc.strokeLine(fix(x), fix(graphGapTop), fix(x), fix(graphGapTop + chartHeight));
			gc.strokeText(StringUtil.formatThousands(Long.toString(gridX)), fix(x), fix(graphGapTop + chartHeight + 12));

			gridX += xInc;
		}
	}

	private void drawYAxis()
	{
		long yInc = findScale(maxY - minY);

		minYQ = (minY / yInc) * yInc;

		maxYQ =  (1 + (maxY / yInc)) * yInc;

		long gridY = minYQ;

		int maxYLabelWidth = StringUtil.formatThousands(Long.toString(maxYQ)).length();

		graphGapLeft = Math.max(40.5, maxYLabelWidth*7);

		double yLabelX = graphGapLeft - (1 + maxYLabelWidth) * 6;

		while (gridY <= maxYQ)
		{
			if (gridY >= minYQ)
			{
				double y = graphGapTop + normaliseY(gridY);
				gc.strokeLine(fix(graphGapLeft), fix(y), fix(graphGapLeft + chartWidth), fix(y));
				gc.strokeText(StringUtil.formatThousands(Long.toString(gridY)), fix(yLabelX), fix(y + 2));
			}

			gridY += yInc;
		}
	}

	private long getXStepTime()
	{
		long rangeMillis = maxX - minX;

		int requiredLines = 5;

		long[] gapMillis = new long[] { 30 * 24 * 60 * 60000, 14 * 24 * 60 * 60000, 7 * 24 * 60 * 60000, 4 * 24 * 60 * 60000,
				2 * 24 * 60 * 60000, 24 * 60 * 60000, 16 * 60 * 60000, 12 * 60 * 60000, 8 * 60 * 60000, 6 * 60 * 60000,
				4 * 60 * 60000, 2 * 60 * 60000, 60 * 60000, 30 * 60000, 15 * 60000, 10 * 60000, 5 * 60000, 2 * 60000, 1 * 60000,
				30000, 15000, 10000, 5000, 2000, 1000, 500, 200, 100, 50, 20, 10, 5, 2, 1 };

		long incrementMillis = 120 * 60000;

		for (int i = 0; i < gapMillis.length; i++)
		{
			if (rangeMillis / gapMillis[i] >= requiredLines)
			{
				incrementMillis = gapMillis[i];
				break;
			}
		}

		return incrementMillis;
	}

	protected long findScale(long range)
	{
		long requiredLines = 8;

		for (int i = 0; i < Y_SCALE.length; i++)
		{
			if (range / Y_SCALE[i] < requiredLines)
			{
				return Y_SCALE[i];
			}
		}

		return range / requiredLines;
	}

	protected double normaliseX(double value)
	{
		System.out.println("normaliseX: " + value + " in " + minXQ + " to " + maxXQ);

		return normalise(value, minXQ, maxXQ, chartWidth, false);
	}

	protected double normaliseY(double value)
	{
		return normalise(value, minYQ, maxYQ, chartHeight, true);
	}

	protected double normalise(double value, double min, double max, double size, boolean invert)
	{
		double range = max - min;
		double result = 0;

		if (range == 0)
		{
			result = 0;
		}
		else
		{
			result = (value - min) / range;
		}

		result *= size;

		if (invert)
		{
			result = size - result;
		}

		return result;
	}

	// prevent blurry lines in JavaFX
	protected double fix(double pixel)
	{
		return 0.5 + (int) pixel;
	}
}
