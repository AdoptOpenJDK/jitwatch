/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui;

import com.chrisnewland.jitwatch.core.JITWatchConfig;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class ConfigStage extends Stage
{

    private static final int TEN_FOR_TOP_RIGHT_BOTTOM_LEFT = 10;
    private static final int TEN_SPACES = 10;
    private static final int TWENTY_SPACES = 20;
    private static final double BY_RATIO_OF_HALF = 0.5;
    private static final int OF_THIRTY_UNITS = 30;
    private static final int SCENE_HEIGHT = 800;
    private static final int SCENE_WIDTH = 480;

    public ConfigStage(final JITWatchUI parent, final JITWatchConfig config)
    {
        initStyle(StageStyle.UTILITY);

        VBox vbox = new VBox();
       
        vbox.setPadding(new Insets(TEN_FOR_TOP_RIGHT_BOTTOM_LEFT));
        vbox.setSpacing(TEN_SPACES);

        final FileChooserList chooserSource = new FileChooserListSrcZip(this, "Source locations", config.getSourceLocations());
        final FileChooserList chooserClasses = new FileChooserList(this, "Class locations", config.getClassLocations());

        HBox hboxButtons = new HBox();
        hboxButtons.setSpacing(TWENTY_SPACES);
        hboxButtons.setPadding(new Insets(TEN_FOR_TOP_RIGHT_BOTTOM_LEFT));
        hboxButtons.setAlignment(Pos.CENTER);

        Button btnSave = new Button("Save");
        Button btnCancel = new Button("Cancel");
        
        btnSave.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {               
                config.setSourceLocations(chooserSource.getFiles());
                config.setClassLocations(chooserClasses.getFiles());

                config.saveConfig();
                
				parent.handleStageClosed(ConfigStage.this);
                close();
            }
        });

        btnCancel.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {
				parent.handleStageClosed(ConfigStage.this);
                close();
            }
        });

        hboxButtons.getChildren().add(btnCancel);
        hboxButtons.getChildren().add(btnSave);

        vbox.getChildren().add(chooserSource);
        vbox.getChildren().add(chooserClasses);

        vbox.getChildren().add(hboxButtons);
        
        chooserSource.prefHeightProperty().bind(this.heightProperty().multiply(BY_RATIO_OF_HALF));
        chooserClasses.prefHeightProperty().bind(this.heightProperty().multiply(BY_RATIO_OF_HALF));
        hboxButtons.setPrefHeight(OF_THIRTY_UNITS);

        setTitle("JITWatch Configuration");

        Scene scene = new Scene(vbox, SCENE_HEIGHT, SCENE_WIDTH);

        setScene(scene);

        setOnCloseRequest(new EventHandler<WindowEvent>()
        {
            @Override
            public void handle(WindowEvent arg0)
            {
				parent.handleStageClosed(ConfigStage.this);
            }
        });
    }
}