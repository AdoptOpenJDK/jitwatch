/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.graphing;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_STAMP;
import static org.adoptopenjdk.jitwatch.util.UserInterfaceUtil.fix;

import java.util.Map;

import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.ui.main.JITWatchUI;
import org.adoptopenjdk.jitwatch.util.ParseUtil;
import org.adoptopenjdk.jitwatch.util.StringUtil;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public abstract class AbstractGraphStage extends Stage
{
	protected Canvas canvas;
	protected GraphicsContext gc;
	protected JITWatchUI mainUI;

	protected double graphGapLeft = 20.5;
	protected final double graphGapRight = 20.5;
	protected final double graphGapTop = 20.5;

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
	
	protected double endOfXAxis;

	private boolean xAxisTime = false;
	
	protected static final Font STANDARD_FONT = new Font(UserInterfaceUtil.FONT_MONOSPACE_FAMILY,
			Double.valueOf(UserInterfaceUtil.FONT_MONOSPACE_SIZE));
	
	protected static final Font MEMBER_FONT = new Font(UserInterfaceUtil.FONT_MONOSPACE_FAMILY,
			Double.valueOf(UserInterfaceUtil.FONT_MONOSPACE_SIZE) * 2.0);

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
		this.mainUI = parent;
		this.width = width;
		this.height = height;
		this.xAxisTime = xAxisTime;

		canvas = new Canvas(width, height);
		gc = canvas.getGraphicsContext2D();

		initStyle(StageStyle.DECORATED);

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
		gc.setFont(STANDARD_FONT);
		
		width = canvas.getWidth();
		height = canvas.getHeight();
		chartWidth = width - graphGapLeft - graphGapRight;
		chartHeight = height - graphGapTop * 2;

		setStrokeForAxis();

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
	
	protected long getStampFromTag(Tag tag)
	{
		Map<String, String> attrs = tag.getAttributes();
				
		return ParseUtil.parseStamp(attrs.get(ATTR_STAMP));
	}
	
	protected void continueLineToEndOfXAxis(double lastX, double lastY, Color color, double lineWidth)
	{
		gc.setStroke(color);
		gc.setLineWidth(lineWidth);
		gc.strokeLine(fix(lastX), fix(lastY), endOfXAxis, fix(lastY));
	}
	
	protected void drawLabel(String text, double xPos, double yPos, Color backgroundColour)
	{
		double boxPad = 4;

		double boxWidth = getApproximateStringWidth(text) + boxPad * 2;
		double boxHeight = getStringHeight() + boxPad * 2;

		gc.setFill(backgroundColour);

		setStrokeForAxis();
		gc.fillRect(fix(xPos), fix(yPos), fix(boxWidth), fix(boxHeight));
		gc.strokeRect(fix(xPos), fix(yPos), fix(boxWidth), fix(boxHeight));

		setStrokeForText();
		gc.fillText(text, fix(xPos + boxPad), fix(yPos));
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
			
			setStrokeForAxis();
			gc.strokeLine(fix(x), fix(graphGapTop), fix(x), fix(graphGapTop + chartHeight));
			
			boolean showMillis = maxX  < 5000;

			setStrokeForText();
			gc.fillText(StringUtil.formatTimestamp(gridX, showMillis), fix(x), fix(graphGapTop + chartHeight + 2));

			gridX += xInc;						
		}
		
		endOfXAxis = fix(graphGapLeft + normaliseX(gridX));
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
			
			setStrokeForAxis();
			gc.strokeLine(fix(x), fix(graphGapTop), fix(x), fix(graphGapTop + chartHeight));
			
			setStrokeForText();
			gc.fillText(StringUtil.formatThousands(Long.toString(gridX)), fix(x), fix(graphGapTop + chartHeight + 2));

			gridX += xInc;
		}
		
		endOfXAxis = fix(graphGapLeft + normaliseX(gridX));
	}

	private void drawYAxis()
	{
		long yInc = findScale(maxY - minY);

		minYQ = (minY / yInc) * yInc;

		maxYQ =  (1 + (maxY / yInc)) * yInc;

		long gridY = minYQ;

		int maxYLabelWidth = StringUtil.formatThousands(Long.toString(maxYQ)).length();

		graphGapLeft = Math.max(40.5, maxYLabelWidth*9);

		double yLabelX = graphGapLeft - (1 + maxYLabelWidth) * 8;

		while (gridY <= maxYQ)
		{
			if (gridY >= minYQ)
			{
				double y = graphGapTop + normaliseY(gridY);
				
				setStrokeForAxis();
				gc.strokeLine(fix(graphGapLeft), fix(y), fix(graphGapLeft + chartWidth), fix(y));
				
				setStrokeForText();
				gc.fillText(StringUtil.formatThousands(Long.toString(gridY)), fix(yLabelX), fix(y - getStringHeight() / 2));
			}

			gridY += yInc;
		}
	}
	
	protected double getApproximateStringWidth(String text)
	{
		return text.length() * gc.getFont().getSize() * 0.6;
	}

	protected double getStringHeight()
	{
		return gc.getFont().getSize();
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
	
	protected void setStrokeForAxis()
	{
		gc.setStroke(Color.BLACK);
		gc.setLineWidth(0.5);
	}
	
	protected void setStrokeForText()
	{
		gc.setStroke(Color.BLACK);
		gc.setFill(Color.BLACK);
		gc.setLineWidth(1.0);
		gc.setTextBaseline(VPos.TOP);
	}
}