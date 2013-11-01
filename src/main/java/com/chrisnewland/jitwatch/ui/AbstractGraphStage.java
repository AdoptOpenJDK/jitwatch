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
    protected Canvas canvas;
    protected GraphicsContext gc;
    protected JITWatchUI parent;

    protected static final double GRAPH_GAP_LEFT = 60.5;
    protected static final double GRAPH_GAP_RIGHT = 20.5;
    protected static final double GRAPH_GAP_Y = 20.5;

    protected static final int[] Y_SCALE = new int[21];

    protected double width;
    protected double height;
    protected double chartWidth;
    protected double chartHeight;

    protected long minX;
    protected long maxX;
    protected long minY;
    protected long maxY;

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
        chartWidth = width - GRAPH_GAP_LEFT - GRAPH_GAP_RIGHT;
        chartHeight = height - GRAPH_GAP_Y * 2;

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, width, height);
        gc.setFill(Color.rgb(235, 255, 255));
        gc.fillRect(GRAPH_GAP_LEFT, GRAPH_GAP_Y, chartWidth, chartHeight);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(GRAPH_GAP_LEFT, GRAPH_GAP_Y, chartWidth, chartHeight);
    }

    protected void drawAxes()
    {
        //padY(10);
        
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

            boolean showMillis = gridX >= 0 && gridX < 5000;

            gc.strokeText(StringUtil.formatTimestamp(gridX, showMillis), fix(x), fix(GRAPH_GAP_Y + chartHeight + 12));

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
            gc.strokeText(StringUtil.formatThousands(Long.toString(gridX)), fix(x), fix(GRAPH_GAP_Y + chartHeight + 12));

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
            double y = GRAPH_GAP_Y + normaliseY(gridY);
            gc.strokeLine(fix(GRAPH_GAP_LEFT), fix(y), fix(GRAPH_GAP_LEFT + chartWidth), fix(y));
            gc.strokeText(StringUtil.formatThousands(Long.toString(gridY)), fix(2), fix(y + 2));

            gridY += yInc;
        }
    }

    private long getXStepTime()
    {
        long rangeMillis = maxX - minX;

        int requiredLines = 6;

        long[] gapMillis = new long[] { 120 * 60000, 60 * 60000, 30 * 60000, 15 * 60000, 10 * 60000, 5 * 60000, 2 * 60000,
                1 * 60000, 30000, 15000, 10000, 5000, 2000, 1000, 500, 200, 100, 50, 20, 10, 5, 2, 1 };

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
        long requiredLines = 10;

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
        double rangePercent = ((double) maxY - (double) minY) / (double) maxY * 100;

        double spacePercent = rangePercent * percentOfRange/100;

        minY *= (1.0 - spacePercent / 100.0);
        maxY *= (1.0 + spacePercent / 100.0);
    }
    
    // prevent blurry lines in JavaFX
    protected double fix(double pixel)
    {
        return 0.5 + (int)pixel;
    }
}
