/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/blob/master/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
package com.chrisnewland.jitwatch.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chrisnewland.jitwatch.core.JITWatchConstants;
import com.chrisnewland.jitwatch.histo.Histo;
import com.chrisnewland.jitwatch.histo.HistoTreeWalker;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class HistoStage extends Stage
{
    private Canvas canvas;
    private GraphicsContext gc;

    private Histo histo;

    private String selectedAttribute;

    private static final int GRAPH_GAP_LEFT = 60;
    private static final int GRAPH_GAP_RIGHT = 20;

    private static final int GRAPH_GAP_Y = 20;

    private static final long[] Y_SCALE = new long[21];

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

    public HistoStage(final JITWatchUI parent)
    {
        initStyle(StageStyle.DECORATED);

        setOnCloseRequest(new EventHandler<WindowEvent>()
        {
            @Override
            public void handle(WindowEvent arg0)
            {
                parent.handleStageClosed(HistoStage.this);
            }
        });

        int width = 640;
        int height = 480;

        canvas = new Canvas(width, height);
        gc = canvas.getGraphicsContext2D();

        final Map<String, String> attrMap = new HashMap<>();
        attrMap.put("Method JIT-Compilation Times", JITWatchConstants.ATTR_COMPILE_MILLIS);
        attrMap.put("Bytecodes per Compiled Method", JITWatchConstants.ATTR_BYTES);
        attrMap.put("Native Bytes per Compiled Method", JITWatchConstants.ATTR_NMSIZE);

        VBox vbox = new VBox();

        ObservableList<String> options = FXCollections.observableArrayList(attrMap.keySet());

        selectedAttribute = attrMap.get(options.get(0));

        final ComboBox<String> comboBox = new ComboBox<>(options);
        comboBox.setValue(options.get(0));

        comboBox.valueProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal)
            {
                selectedAttribute = attrMap.get(newVal);
                histo = HistoTreeWalker.buildHistoForAttribute(parent.getPackageManager(), true, selectedAttribute, 10);
                redraw();
            }
        });

        HBox hbox = new HBox();
        hbox.setPadding(new Insets(4, 0, 0, GRAPH_GAP_LEFT));
        hbox.getChildren().add(comboBox);

        vbox.getChildren().add(hbox);
        vbox.getChildren().add(canvas);

        Scene scene = new Scene(vbox, width, height);

        canvas.widthProperty().bind(scene.widthProperty());
        canvas.heightProperty().bind(scene.heightProperty().subtract(30));

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

        setTitle("JITWatch Histogram");

        histo = HistoTreeWalker.buildHistoForAttribute(parent.getPackageManager(), true, selectedAttribute, 10);

        setScene(scene);
        show();

        redraw();
    }

    public void redraw()
    {
        List<Map.Entry<Long, Integer>> result = histo.getSortedData();

        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double chartWidth = width - GRAPH_GAP_LEFT - GRAPH_GAP_RIGHT;
        double chartHeight = height - GRAPH_GAP_Y * 2;

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, width, height);
        gc.setFill(Color.BEIGE);
        gc.fillRect(GRAPH_GAP_LEFT, GRAPH_GAP_Y, chartWidth, chartHeight);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(GRAPH_GAP_LEFT, GRAPH_GAP_Y, chartWidth, chartHeight);

        if (result.size() > 0)
        {
            double minStamp = 0;
            double maxStamp = histo.getLastTime();

            int maxEvents = histo.getMaxCount();

            gc.setStroke(Color.BLACK);
            gc.setFont(new Font("monospace", 10));

            // ============
            // Draw X axis
            // ============
            double xInc = findScale((long) maxStamp);

            int gridX = 0;

            while (gridX < maxStamp)
            {

                double x = GRAPH_GAP_LEFT + normalise(gridX, 0, maxStamp, chartWidth, false);
                gc.strokeLine(x, GRAPH_GAP_Y, x, GRAPH_GAP_Y + chartHeight);
                gc.strokeText(Integer.toString(gridX), x, GRAPH_GAP_Y + chartHeight + 12);

                gridX += xInc;
            }

            // ============
            // Draw Y axis
            // ============
            long yInc = findScale(maxEvents);

            int gridY = 0;

            while (gridY < maxEvents)
            {

                double y = GRAPH_GAP_Y + normalise(gridY, 0, maxEvents, chartHeight, true);
                gc.strokeLine(GRAPH_GAP_LEFT, y, GRAPH_GAP_LEFT + chartWidth, y);
                gc.strokeText(Integer.toString(gridY), 2, y + 2);

                gridY += yInc;
            }

            Color colourLine = Color.RED;

            for (Map.Entry<Long, Integer> entry : result)
            {
                long key = entry.getKey();
                int value = entry.getValue();

                double x = GRAPH_GAP_LEFT + normalise(key, minStamp, maxStamp, chartWidth, false);

                gc.setStroke(colourLine);

                double y = GRAPH_GAP_Y + normalise(value, 0, maxEvents, chartHeight, true);
                gc.strokeLine(x, GRAPH_GAP_Y + chartHeight, x, y);

            }

            double legendWidth = 100;
            double legendHeight = 220;
            double xPos = canvas.getWidth() - GRAPH_GAP_RIGHT - legendWidth - 5;
            double yPos = GRAPH_GAP_Y + 5;

            gc.setFill(Color.WHITE);
            gc.setStroke(Color.BLACK);

            gc.fillRect(xPos, yPos, legendWidth, legendHeight);
            gc.strokeRect(xPos, yPos, legendWidth, legendHeight);

            xPos += 5;
            yPos += 15;

            for (double percent : new double[] { 50, 75, 80, 85, 90, 95, 98, 99, 99.5, 99.9, 100 })
            {
                gc.strokeText(percent + "% : " + histo.getPercentile(percent), xPos, yPos);
                yPos += 20;
            }
        }
    }

    private long findScale(long max)
    {
        long requiredLines = 10;

        for (int i = 0; i < Y_SCALE.length; i++)
        {
            if (max / Y_SCALE[i] < requiredLines)
            {
                return Y_SCALE[i];
            }
        }

        return max / requiredLines;
    }

    private double normalise(double value, double min, double max, double size, boolean invert)
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

}