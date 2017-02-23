/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.graphing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.adoptopenjdk.jitwatch.histo.AttributeNameHistoWalker;
import org.adoptopenjdk.jitwatch.histo.CompileTimeHistoWalker;
import org.adoptopenjdk.jitwatch.histo.Histo;
import org.adoptopenjdk.jitwatch.histo.IHistoVisitable;
import org.adoptopenjdk.jitwatch.histo.InlineSizeHistoVisitable;
import org.adoptopenjdk.jitwatch.histo.NativeSizeHistoWalker;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.ui.main.JITWatchUI;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;
import static org.adoptopenjdk.jitwatch.util.UserInterfaceUtil.fix;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;
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
        super(parent, JITWatchUI.WINDOW_WIDTH, JITWatchUI.WINDOW_HEIGHT, false);
        
        IReadOnlyJITDataModel model = parent.getJITDataModel();

		final Map<String, IHistoVisitable> attrMap = new HashMap<>();

		attrMap.put("JIT Compilation Times", new CompileTimeHistoWalker(model, 1));
        attrMap.put("Bytes per Compiled Method", new AttributeNameHistoWalker(model, true, ATTR_BYTES, 1));
        attrMap.put("Native Bytes per Compiled Method", new NativeSizeHistoWalker(model, 1));
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
        hbox.setPadding(new Insets(4, 0, 0, graphGapLeft));
        hbox.getChildren().add(comboBox);
        hbox.setMinHeight(30); // prevent combo clipping when highlighted

        vbox.getChildren().add(hbox);
        vbox.getChildren().add(canvas);

        Scene scene = UserInterfaceUtil.getScene(vbox, width, height);

        canvas.widthProperty().bind(scene.widthProperty());
        canvas.heightProperty().bind(scene.heightProperty().subtract(30));

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
            gc.setFont(new Font("monospace", 10));

            drawAxes();

            Color colourLine = Color.RED;

            for (Map.Entry<Long, Integer> entry : result)
            {
                long key = entry.getKey();
                int value = entry.getValue();

                double x = graphGapLeft + normaliseX(key);

                gc.setLineWidth(2.0);
                gc.setStroke(colourLine);

                double y = graphGapTop + normaliseY(value);
                gc.strokeLine(fix(x), fix(graphGapTop + chartHeight), fix(x), fix(y));

            }

            double legendWidth = 100;
            double legendHeight = 220;
            double xPos = canvas.getWidth() - graphGapRight - legendWidth - 5;
            double yPos = graphGapTop + 5;

            gc.setFill(Color.WHITE);
            gc.setStroke(Color.BLACK);

            gc.fillRect(fix(xPos), fix(yPos), fix(legendWidth), fix(legendHeight));
            gc.strokeRect(fix(xPos), fix(yPos), fix(legendWidth), fix(legendHeight));

            xPos += 5;
            yPos += 5;

            for (double percent : new double[] { 50, 90, 95, 99, 99.9, 99.99, 99.999, 100 })
            {
        		setStrokeForText();
                gc.fillText(percent + "% : " + histo.getPercentile(percent), fix(xPos), fix(yPos));
                yPos += 20;
            }
        }
        else
        {
			setStrokeForText();
            gc.fillText("No data for histogram.", fix(graphGapLeft + 8), fix(graphGapTop + 16));
        }
    }
}