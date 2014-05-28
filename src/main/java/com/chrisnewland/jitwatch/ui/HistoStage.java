/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui;

import com.chrisnewland.jitwatch.histo.AttributeNameHistoWalker;
import com.chrisnewland.jitwatch.histo.Histo;
import com.chrisnewland.jitwatch.histo.IHistoVisitable;
import com.chrisnewland.jitwatch.histo.InlineSizeHistoVisitable;
import com.chrisnewland.jitwatch.model.IReadOnlyJITDataModel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public class HistoStage extends AbstractGraphStage
{
    private static final int VIEW_WIDTH = 640;
    private static final int VIEW_HEIGHT = 480;

    private static final String JIT_COMPILATION_TIMES = "JIT Compilation Times";
    private static final String BYTES_PER_COMPILED_METHOD = "Bytes per Compiled Method";
    private static final String NATIVE_BYTES_PER_COMPILED_METHOD = "Native Bytes per Compiled Method";
    private static final String INLINED_METHOD_SIZES = "Inlined Method Sizes";

    private static final int TEN_MILLISECONDS = 10;
    private static final int TEN_BYTES = 10;
    private static final int ONE_UNIT = 1;
    public static final int GRAPH_TOP = 4;
    public static final int GRAPH_RIGHT = 0;
    public static final int GRAPH_BOTTOM = 0;
    public static final int GRAPH_HEIGHT = 30;
    public static final int THIRTY_PIXELS_FROM_BOTTOM = 30;
    public static final int DEFAULT_MONOSPACE_FONT_SIZE = 10;
    public static final int DEFAULT_LEGEND_WIDTH = 100;
    public static final int DEFAULT_LEGEND_HEIGHT = 220;
    public static final int X_AXIS_MARGIN = 5;
    public static final int Y_AXIS_MARGIN = 5;
    public static final int INCREMENT_X_POS_BY = 5;
    public static final int INCREMENT_Y_POS_BY = 15;
    public static final int FIFTY_PERCENT = 50;
    public static final int SEVENTY_FIVE_PERCENT = 75;
    public static final int EIGHTY_PERCENT = 80;
    public static final int EIGHTY_FIVE_PERCENT = 85;
    public static final int NINETY_PERCENT = 90;
    public static final int NINETY_FIVE_PERCENT = 95;
    public static final int NINETY_EIGHT_PERCENT = 98;
    public static final int NINETY_NINE_PERCENT = 99;
    public static final double NINETY_NINE_POINT_FIVE_PERCENT = 99.5;
    public static final int HUNDRED_PERCENT = 100;
    public static final double NINETY_NINE_POINT_NINE_PERCENT = 99.9;
    public static final int INCREMENT_TEXT_Y_POS_BY = 20;
    public static final int TEXT_X_POS_MARGIN = 8;
    public static final int TEXT_Y_POS_MARGIN = 16;

    private Histo histo;

    private IHistoVisitable histoVisitable;

    public HistoStage(final JITWatchUI parent)
    {
        super(parent, VIEW_WIDTH, VIEW_HEIGHT, false);
        
        IReadOnlyJITDataModel model = parent.getJITDataModel();

		final Map<String, IHistoVisitable> attrMap = new HashMap<>();

		attrMap.put(JIT_COMPILATION_TIMES, new AttributeNameHistoWalker(model, true, ATTR_COMPILE_MILLIS, TEN_MILLISECONDS));
        attrMap.put(BYTES_PER_COMPILED_METHOD, new AttributeNameHistoWalker(model, true, ATTR_BYTES, TEN_BYTES));
        attrMap.put(NATIVE_BYTES_PER_COMPILED_METHOD, new AttributeNameHistoWalker(model, true, ATTR_NMSIZE, TEN_BYTES));
        attrMap.put(INLINED_METHOD_SIZES, new InlineSizeHistoVisitable(model, ONE_UNIT));

        VBox vbox = new VBox();

        ObservableList<String> options = FXCollections.observableArrayList(attrMap.keySet());

        histoVisitable = attrMap.get(options.get(0));

        final ComboBox<String> comboBox = new ComboBox<>(options);
        comboBox.setValue(options.get(0));

        comboBox.valueProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal)
            {
            	histoVisitable = attrMap.get(newVal);
                                
                histo = histoVisitable.buildHistogram();
                redraw();
            }
        });
        
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(GRAPH_TOP, GRAPH_RIGHT, GRAPH_BOTTOM, GRAPH_GAP_LEFT));
        hbox.getChildren().add(comboBox);
        hbox.setMinHeight(GRAPH_HEIGHT); // prevent combo clipping when highlighted

        vbox.getChildren().add(hbox);
        vbox.getChildren().add(canvas);

        Scene scene = new Scene(vbox, width, height);

        canvas.widthProperty().bind(scene.widthProperty());
        canvas.heightProperty().bind(scene.heightProperty().subtract(THIRTY_PIXELS_FROM_BOTTOM));

        setTitle("JITWatch Histogram");
        
        histo = histoVisitable.buildHistogram();

        setScene(scene);
        show();

        redraw();
    }

    @Override
    public final void redraw()
    {
        super.baseRedraw();
        
        if (histo == null)
        {
            return;
        }

        List<Map.Entry<Long, Integer>> result = histo.getSortedData();

        if (result.size() > 0)
        {
            minX = 0;
            maxX = histo.getLastTime();

            minY = 0;
            maxY = histo.getMaxCount();

            gc.setStroke(Color.BLACK);
            gc.setFont(new Font("monospace", DEFAULT_MONOSPACE_FONT_SIZE));

            drawAxes();

            Color colourLine = Color.RED;

            for (Map.Entry<Long, Integer> entry : result)
            {
                long key = entry.getKey();
                int value = entry.getValue();

                double x = GRAPH_GAP_LEFT + normaliseX(key);

                gc.setStroke(colourLine);

                double y = GRAPH_GAP_Y + normaliseY(value);
                gc.strokeLine(fix(x), fix(GRAPH_GAP_Y + chartHeight), fix(x), fix(y));

            }

            double legendWidth = DEFAULT_LEGEND_WIDTH;
            double legendHeight = DEFAULT_LEGEND_HEIGHT;
            double xPos = canvas.getWidth() - GRAPH_GAP_RIGHT - legendWidth - X_AXIS_MARGIN;
            double yPos = GRAPH_GAP_Y + Y_AXIS_MARGIN;

            gc.setFill(Color.WHITE);
            gc.setStroke(Color.BLACK);

            gc.fillRect(fix(xPos), fix(yPos), fix(legendWidth), fix(legendHeight));
            gc.strokeRect(fix(xPos), fix(yPos), fix(legendWidth), fix(legendHeight));

            xPos += INCREMENT_X_POS_BY;
            yPos += INCREMENT_Y_POS_BY;

            for (double percent : new double[] {
                    FIFTY_PERCENT,
                    SEVENTY_FIVE_PERCENT,
                    EIGHTY_PERCENT,
                    EIGHTY_FIVE_PERCENT,
                    NINETY_PERCENT,
                    NINETY_FIVE_PERCENT,
                    NINETY_EIGHT_PERCENT,
                    NINETY_NINE_PERCENT,
                    NINETY_NINE_POINT_FIVE_PERCENT,
                    NINETY_NINE_POINT_NINE_PERCENT,
                    HUNDRED_PERCENT
            })
            {
                gc.strokeText(percent + "% : " + histo.getPercentile(percent), fix(xPos), fix(yPos));
                yPos += INCREMENT_TEXT_Y_POS_BY;
            }
        }
        else
        {
            gc.strokeText("No data for histogram.", fix(GRAPH_GAP_LEFT + TEXT_X_POS_MARGIN), fix(GRAPH_GAP_Y + TEXT_Y_POS_MARGIN));
        }
    }
}