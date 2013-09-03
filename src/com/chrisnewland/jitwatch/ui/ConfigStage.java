package com.chrisnewland.jitwatch.ui;

import com.chrisnewland.jitwatch.core.JITWatchConfig;
import com.chrisnewland.jitwatch.core.StringUtil;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
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

        Label labelFilter = new Label("Package filter (leave empty to show all packages)");
        final TextArea taFilter = new TextArea();
        taFilter.setText(StringUtil.listToText(config.getAllowedPackages(), "\n"));
        taFilter.setDisable(false);
        
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(10);

        final FileChooserList chooserSource = new FileChooserList(this, "Source locations", config.getSourceLocations());
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
                config.setAllowedPackages(StringUtil.textToList(taFilter.getText(), "\n"));
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

        vbox.getChildren().add(labelFilter);
        vbox.getChildren().add(taFilter);

        vbox.getChildren().add(chooserSource);
        vbox.getChildren().add(chooserClasses);

        vbox.getChildren().add(hboxButtons);
        
        taFilter.prefHeightProperty().bind(this.heightProperty().multiply(0.33));
        chooserSource.prefHeightProperty().bind(this.heightProperty().multiply(0.33));
        chooserClasses.prefHeightProperty().bind(this.heightProperty().multiply(0.33));
        hboxButtons.setPrefHeight(30);


        Scene scene = new Scene(vbox, 512, 480);

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