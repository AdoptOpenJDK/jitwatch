/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/blob/master/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
package com.chrisnewland.jitwatch.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

public class TextViewerStage extends Stage
{
    private String[] lines;
    private ListView<String> listView;

    public TextViewerStage(final JITWatchUI parent, String title, String source, boolean showLineNumbers)
    {
        initStyle(StageStyle.DECORATED);

        setOnCloseRequest(new EventHandler<WindowEvent>()
        {
            @Override
            public void handle(WindowEvent arg0)
            {
                parent.handleStageClosed(TextViewerStage.this);
            }
        });

        if (source == null)
        {
            source = "Empty";
        }

        source = source.replace("\t", "    "); // 4 spaces

        lines = source.split("\n");

        int max = 0;

        for (int i = 0; i < lines.length; i++)
        {
            String row = lines[i];

            if (showLineNumbers)
            {
                lines[i] = (i + 1) + " " + row;
            }

            int rowLen = row.length();

            if (rowLen > max)
            {
                max = rowLen;
            }
        }

        listView = new ListView<>(FXCollections.observableArrayList(lines));

        listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>()
        {
            @Override
            public ListCell<String> call(ListView<String> arg0)
            {
                return new TextCell();
            }
        });

        int x = Math.min(80, max);
        int y = Math.min(30, lines.length);

        x = Math.max(x, 20);
        y = Math.max(y, 20);

        VBox vbox = new VBox();

        vbox.setPadding(new Insets(4));

        vbox.getChildren().add(listView);
        VBox.setVgrow(listView, Priority.ALWAYS);

        setTitle(title);

        Scene scene = new Scene(vbox, x * 12, y * 19);

        setScene(scene);
    }

    public void jumpTo(final String regex)
    {
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                int pos = 0;

                for (String line : lines)
                {
                    Matcher matcher = Pattern.compile(regex).matcher(line);
                    if (matcher.find())
                    {
                        break;
                    }

                    pos++;
                }

                final int posCopy = pos;

                // needed as SelectionModel selected index
                // is not updated instantly on select()
                Platform.runLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        listView.scrollTo(posCopy);
                    }
                });
            }
        });
    }

    static class TextCell extends ListCell<String>
    {
        @Override
        public void updateItem(String item, boolean empty)
        {
            super.updateItem(item, empty);

            if (item != null)
            {
                setText(item);

                if (isSelected())
                {
                    setStyle("-fx-font-family: monospace; -fx-font-size:12pt; -fx-cell-size:22; -fx-background-color: blue; -fx-text-fill:white;");
                }
                else
                {
                    setStyle("-fx-font-family: monospace; -fx-font-size:12pt; -fx-cell-size:22; -fx-background-color: white; -fx-text-fill:black;");
                }
            }
        }
    }
}