/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chrisnewland.jitwatch.histo.AttributeNameHistoWalker;
import com.chrisnewland.jitwatch.histo.Histo;
import com.chrisnewland.jitwatch.histo.IHistoVisitable;
import com.chrisnewland.jitwatch.histo.InlineSizeHistoVisitable;
import com.chrisnewland.jitwatch.model.IReadOnlyJITDataModel;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;
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

public class HistoStage extends AbstractGraphStage
{
    private Histo histo;

    private IHistoVisitable histoVisitable;

    public HistoStage(final JITWatchUI parent)
    {
        super(parent, 640, 480, false);
        
        IReadOnlyJITDataModel model = parent.getJITDataModel();

		final Map<String, IHistoVisitable> attrMap = new HashMap<>();

		attrMap.put("Method JIT-Compilation Times", new AttributeNameHistoWalker(model, true, ATTR_COMPILE_MILLIS, 10));
        attrMap.put("Bytecodes per Compiled Method", new AttributeNameHistoWalker(model, true, ATTR_BYTES, 10));
        attrMap.put("Native Bytes per Compiled Method", new AttributeNameHistoWalker(model, true, ATTR_NMSIZE, 10));
        attrMap.put("Inlined Method Sizes", new InlineSizeHistoVisitable(model, 1));

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
        hbox.setPadding(new Insets(4, 0, 0, GRAPH_GAP_LEFT));
        hbox.getChildren().add(comboBox);
        hbox.setMinHeight(30); // prevent combo clipping when highlighted

        vbox.getChildren().add(hbox);
        vbox.getChildren().add(canvas);

        Scene scene = new Scene(vbox, width, height);

        canvas.widthProperty().bind(scene.widthProperty());
        canvas.heightProperty().bind(scene.heightProperty().subtract(30));

        setTitle("JITWatch Histogram");
        
        histo = histoVisitable.buildHistogram();

        setScene(scene);
        show();

        redraw();
    }

    @Override
    public void redraw()
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
            gc.setFont(new Font("monospace", 10));

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

            double legendWidth = 100;
            double legendHeight = 220;
            double xPos = canvas.getWidth() - GRAPH_GAP_RIGHT - legendWidth - 5;
            double yPos = GRAPH_GAP_Y + 5;

            gc.setFill(Color.WHITE);
            gc.setStroke(Color.BLACK);

            gc.fillRect(fix(xPos), fix(yPos), fix(legendWidth), fix(legendHeight));
            gc.strokeRect(fix(xPos), fix(yPos), fix(legendWidth), fix(legendHeight));

            xPos += 5;
            yPos += 15;

            for (double percent : new double[] { 50, 75, 80, 85, 90, 95, 98, 99, 99.5, 99.9, 100 })
            {
                gc.strokeText(percent + "% : " + histo.getPercentile(percent), fix(xPos), fix(yPos));
                yPos += 20;
            }
        }
        else
        {
            gc.strokeText("No data for histogram.", fix(GRAPH_GAP_LEFT + 8), fix(GRAPH_GAP_Y + 16));
        }
    }
}