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
    public ConfigStage(final JITWatchUI parent, final JITWatchConfig config)
    {
        initStyle(StageStyle.UTILITY);

        VBox vbox = new VBox();
       
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(10);

        final FileChooserList chooserSource = new FileChooserListSrcZip(this, "Source locations", config.getSourceLocations());
        final FileChooserList chooserClasses = new FileChooserList(this, "Class locations", config.getClassLocations());

        HBox hboxButtons = new HBox();
        hboxButtons.setSpacing(20);
        hboxButtons.setPadding(new Insets(10));
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
        
        chooserSource.prefHeightProperty().bind(this.heightProperty().multiply(0.5));
        chooserClasses.prefHeightProperty().bind(this.heightProperty().multiply(0.5));
        hboxButtons.setPrefHeight(30);

        setTitle("JITWatch Configuration");

        Scene scene = new Scene(vbox, 800, 480);

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