/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui;

import com.chrisnewland.jitwatch.util.StringUtil;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public abstract class AbstractGraphStage extends Stage
{
    private static final int TOP = 0;
    private static final int LEFT = 0;
    private static final int RED = 210;
    private static final int GREEN = 255;
    private static final int BLUE = 255;
    private static final int X_POSITION_MARGIN = 12;
    private static final int FIVE_THOUSAND_PIXELS = 5000;
    private static final int Y_POSITION_MARGIN = 12;
    private static final int HUNDRED_AND_TWO_SECONDS = 120;
    private static final int SECONDS_TO_MILLISECONDS_MULTIPLIER = 60000;
    private static final int DEFAULT_SCALE_REQUIRED_LINES = 8;
    private static final int ONE_HUNDRED = 100;
    private static final double HALF = 0.5;
    private static final int SIXTY_SECONDS = 60;
    private static final int TWENTY_FOUR_HOURS = 24;
    private static final int ONE_SECOND = 1;
    private static final int TWO_SECONDS = 2;
    private static final int ONE_MILLISECOND = 1;
    private static final int TWO_MILLISECONDS = 2;
    private static final int FIVE_SECONDS = 5;
    private static final int TEN_SECONDS = 10;
    private static final int FIFTEEN_SECONDS = 15;
    private static final int THIRTY_SECONDS = 30;
    private static final int ONE_MINUTE = 1;
    private static final int TWO_MINUTES = 2;
    private static final int FOUR_MINUTES = 4;
    private static final int THIRTY_DAYS = 30;
    private static final int FOURTEEN_DAYS = 14;
    private static final int SEVEN_DAYS = 7;
    private static final int FOUR_DAYS = 4;
    private static final int TWO_DAYS = 2;
    private static final int ONE_DAY = 1;
    private static final int SIXTEEN_MINUTES = 16;
    private static final int TWELVE_MINUTES = 12;
    private static final int EIGHT_MINUTES = 8;
    private static final int SIX_MINUTES = 6;
    private static final int FIVE_MILLISECONDS = 5;
    private static final int TEN_MILLISECONDS = 10;
    private static final int TWENTY_MILLISECONDS = 20;
    private static final int FIFTY_MILLISECONDS = 50;
    private static final int ONE_HUNDRED_MILLISECONDS = 100;
    private static final int TWO_HUNDRED_MILLISECONDS = 200;
    private static final int FIVE_HUNDRED_MILLISECONDS = 500;
    private static final int ONE_THOUSAND_MILLISECONDS = 1000;
    private static final int TWO_THOUSAND_MILLISECONDS = 2000;
    private static final int FIVE_THOUSAND_MILLISECONDS = 5000;
    private static final int TEN_THOUSAND_MILLISECONDS = 10000;
    private static final int FIFTEEN_THOUSAND_MILLISECONDS = 15000;
    private static final int THIRTY_THOUSAND_MILLISECONDS = 30000;
    public static final int DEFAULT_STEP_TIME_REQUIRED_LINES = 6;

    protected Canvas canvas;
    protected GraphicsContext gc;
    protected JITWatchUI parent;

	protected static final double GRAPH_GAP_LEFT = 60.5;
	protected static final double GRAPH_GAP_RIGHT = 20.5;
	protected static final double GRAPH_GAP_Y = 20.5;

    private static final int DIVISONS = 21;
    static final int[] Y_SCALE = new int[DIVISONS];

    protected double width;
    protected double height;
    protected double chartWidth;
    protected double chartHeight;

    protected long minX;
    protected long maxX;
    protected long minY;
    protected long maxY;

	private boolean xAxisTime = false;

    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int FIVE_TIMES = 5;
    private static final int BY_TEN = 10;
    private static final int ONE_TIME = 1;
    private static final int TWO_TIMES = 2;

    private static final int INCREMENT_BY_THREE = 3;

    static
	{
		int multiplier = 1;

		for (int i = 0; i < Y_SCALE.length; i += INCREMENT_BY_THREE)
		{
			Y_SCALE[i + ZERO] = ONE_TIME * multiplier;
			Y_SCALE[i + ONE] = TWO_TIMES * multiplier;
			Y_SCALE[i + TWO] = FIVE_TIMES * multiplier;

			multiplier *= BY_TEN;
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
		chartWidth = width - GRAPH_GAP_LEFT - GRAPH_GAP_RIGHT;
		chartHeight = height - GRAPH_GAP_Y * 2;

		gc.setFill(Color.WHITE);
		gc.fillRect(TOP, LEFT, width, height);
		gc.setFill(Color.rgb(RED, GREEN, BLUE));
		gc.fillRect(GRAPH_GAP_LEFT, GRAPH_GAP_Y, chartWidth, chartHeight);
		gc.setStroke(Color.BLACK);
		gc.strokeRect(GRAPH_GAP_LEFT, GRAPH_GAP_Y, chartWidth, chartHeight);
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

		long gridX = minX;

		while (gridX < maxX)
		{
			double x = GRAPH_GAP_LEFT + normaliseX(gridX);
			gc.strokeLine(fix(x), fix(GRAPH_GAP_Y), fix(x), fix(GRAPH_GAP_Y + chartHeight));

			boolean showMillis = gridX >= 0 && gridX < FIVE_THOUSAND_PIXELS && xInc < FIVE_THOUSAND_PIXELS;

			gc.strokeText(StringUtil.formatTimestamp(gridX, showMillis), fix(x),
                    fix(GRAPH_GAP_Y + chartHeight + X_POSITION_MARGIN));

			gridX += xInc;
		}
	}

	private void drawXAxis()
	{
		long xInc = findScale(maxX - minX);

		long gridX = minX;

		while (gridX < maxX)
		{
			double x = GRAPH_GAP_LEFT + normaliseX(gridX);
			gc.strokeLine(fix(x), fix(GRAPH_GAP_Y), fix(x), fix(GRAPH_GAP_Y + chartHeight));
			gc.strokeText(StringUtil.formatThousands(Long.toString(gridX)), fix(x),
                    fix(GRAPH_GAP_Y + chartHeight + Y_POSITION_MARGIN));

			gridX += xInc;
		}
	}

	private void drawYAxis()
	{
		long yInc = findScale(maxY - minY);

		long minYCopy = (minY / yInc) * yInc; // quantise start value

		long gridY = minYCopy;

		while (gridY < maxY)
		{
			if (gridY >= minY)
			{
				double y = GRAPH_GAP_Y + normaliseY(gridY);
				gc.strokeLine(fix(GRAPH_GAP_LEFT), fix(y), fix(GRAPH_GAP_LEFT + chartWidth), fix(y));
				gc.strokeText(StringUtil.formatThousands(Long.toString(gridY)), fix(2), fix(y + 2));
			}

			gridY += yInc;
		}
	}

	private long getXStepTime()
	{
		long rangeMillis = maxX - minX;

		int requiredLines = DEFAULT_STEP_TIME_REQUIRED_LINES;

		long[] gapMillis = new long[] {
                THIRTY_DAYS * TWENTY_FOUR_HOURS * SIXTY_SECONDS * SECONDS_TO_MILLISECONDS_MULTIPLIER,
                FOURTEEN_DAYS * TWENTY_FOUR_HOURS * SIXTY_SECONDS * SECONDS_TO_MILLISECONDS_MULTIPLIER,
                SEVEN_DAYS * TWENTY_FOUR_HOURS * SIXTY_SECONDS * SECONDS_TO_MILLISECONDS_MULTIPLIER,
                FOUR_DAYS * TWENTY_FOUR_HOURS * SIXTY_SECONDS * SECONDS_TO_MILLISECONDS_MULTIPLIER,
                TWO_DAYS * TWENTY_FOUR_HOURS * SIXTY_SECONDS * SECONDS_TO_MILLISECONDS_MULTIPLIER,
                ONE_DAY * TWENTY_FOUR_HOURS * SIXTY_SECONDS * SECONDS_TO_MILLISECONDS_MULTIPLIER,
                SIXTEEN_MINUTES * SIXTY_SECONDS * SECONDS_TO_MILLISECONDS_MULTIPLIER,
                TWELVE_MINUTES * SIXTY_SECONDS * SECONDS_TO_MILLISECONDS_MULTIPLIER,
                EIGHT_MINUTES * SIXTY_SECONDS * SECONDS_TO_MILLISECONDS_MULTIPLIER,
                SIX_MINUTES * SIXTY_SECONDS * SECONDS_TO_MILLISECONDS_MULTIPLIER,
                FOUR_MINUTES * SIXTY_SECONDS * SECONDS_TO_MILLISECONDS_MULTIPLIER,
                TWO_MINUTES * SIXTY_SECONDS * SECONDS_TO_MILLISECONDS_MULTIPLIER,
                ONE_MINUTE * SIXTY_SECONDS * SECONDS_TO_MILLISECONDS_MULTIPLIER,
                THIRTY_SECONDS * SECONDS_TO_MILLISECONDS_MULTIPLIER,
                FIFTEEN_SECONDS * SECONDS_TO_MILLISECONDS_MULTIPLIER,
                TEN_SECONDS * SECONDS_TO_MILLISECONDS_MULTIPLIER,
                FIVE_SECONDS * SECONDS_TO_MILLISECONDS_MULTIPLIER,
                TWO_SECONDS * SECONDS_TO_MILLISECONDS_MULTIPLIER,
                ONE_SECOND * SECONDS_TO_MILLISECONDS_MULTIPLIER,
                THIRTY_THOUSAND_MILLISECONDS,
                FIFTEEN_THOUSAND_MILLISECONDS,
                TEN_THOUSAND_MILLISECONDS,
                FIVE_THOUSAND_MILLISECONDS,
                TWO_THOUSAND_MILLISECONDS,
                ONE_THOUSAND_MILLISECONDS,
                FIVE_HUNDRED_MILLISECONDS,
                TWO_HUNDRED_MILLISECONDS,
                ONE_HUNDRED_MILLISECONDS,
                FIFTY_MILLISECONDS,
                TWENTY_MILLISECONDS,
                TEN_MILLISECONDS,
                FIVE_MILLISECONDS,
                TWO_MILLISECONDS,
                ONE_MILLISECOND
        };

		long incrementMillis = HUNDRED_AND_TWO_SECONDS * SECONDS_TO_MILLISECONDS_MULTIPLIER;

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
		long requiredLines = DEFAULT_SCALE_REQUIRED_LINES;

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
		return normalise(value, minX, maxX, chartWidth, false);
	}

	protected double normaliseY(double value)
	{
		return normalise(value, minY, maxY, chartHeight, true);
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

	protected void padY(double percentOfRange)
	{
		double rangePercent = ((double) maxY - (double) minY) / (double) maxY * ONE_HUNDRED;

		double spacePercent = rangePercent * percentOfRange / ONE_HUNDRED;

		minY *= (ONE - spacePercent / ONE_HUNDRED);
		maxY *= (ONE + spacePercent / ONE_HUNDRED);
	}

	// prevent blurry lines in JavaFX
	protected double fix(double pixel)
	{
		return HALF + (int) pixel;
	}
}
