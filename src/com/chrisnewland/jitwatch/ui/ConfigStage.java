package com.chrisnewland.jitwatch.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.chrisnewland.jitwatch.core.JITWatch;

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
    public ConfigStage(final JITWatchUI parent, final Properties props)
    {
        initStyle(StageStyle.UTILITY);

        String confPackages = (String) props.get(JITWatch.CONF_PACKAGE_FILTER);
        String confSources = (String) props.get(JITWatch.CONF_SOURCES);
        String confClasses = (String) props.get(JITWatch.CONF_CLASSES);

        VBox vbox = new VBox();

        Label labelFilter = new Label("Package filter (leave empty to show all packages)");
        final TextArea taFilter = new TextArea();
        taFilter.setText(JITWatch.unpack(confPackages));
        taFilter.setDisable(false);
        
        vbox.setPadding(new Insets(10));
        vbox.setSpacing(10);

        String[] sources = JITWatch.unpack(confSources).split("\n");
        List<String> sourceList = new ArrayList<>(Arrays.asList(sources));
        final FileChooserList chooserSource = new FileChooserList(this, "Source locations", sourceList);

        String[] classes = JITWatch.unpack(confClasses).split("\n");
        List<String> classList = new ArrayList<>(Arrays.asList(classes));
        final FileChooserList chooserClasses = new FileChooserList(this, "Class locations", classList);

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
                props.setProperty(JITWatch.CONF_PACKAGE_FILTER, JITWatch.pack(taFilter.getText()));
                props.setProperty(JITWatch.CONF_SOURCES, JITWatch.pack(chooserSource.getFiles()));
                props.setProperty(JITWatch.CONF_CLASSES, JITWatch.pack(chooserClasses.getFiles()));

                parent.updateConfig(props);
                close();
            }
        });

        btnCancel.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {
                parent.updateConfig(null);
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
                parent.updateConfig(null);
            }
        });
    }

}