/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui;

import com.chrisnewland.jitwatch.core.JITWatchConfig;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class NothingMountedStage extends Stage
{

    private static final int TWENTY_SPACES = 20;
    private static final int TEN_INSETS = 10;
    private static final int SCENE_WIDTH = 360;
    private static final int SCENE_HEIGHT = 160;

    public NothingMountedStage(final JITWatchUI parent, final JITWatchConfig config)
	{
		initStyle(StageStyle.DECORATED);
			
		VBox vbox = new VBox();
		vbox.setSpacing(TWENTY_SPACES);
		vbox.setPadding(new Insets(TEN_INSETS));
		
		Scene scene = new Scene(vbox, SCENE_WIDTH, SCENE_HEIGHT);
	
		setTitle("No classes or sources mounted");
		
        Label lblMsg1 = new Label("You have not mounted any source or class files.");
        Label lblMsg2 = new Label("JITWatch is much more useful if you do :)");

        CheckBox cbShowWarning = new CheckBox("Don't show this warning again.");
        cbShowWarning.setTooltip(new Tooltip("Don't show warning about no source or class files added."));
        cbShowWarning.setSelected(false);
        cbShowWarning.selectedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal)
            {
                config.setShowNothingMounted(!newVal);
                config.saveConfig();
            }
        });
        
        HBox hboxButtons = new HBox();
        hboxButtons.setSpacing(TWENTY_SPACES);
        hboxButtons.setPadding(new Insets(TEN_INSETS));
        hboxButtons.setAlignment(Pos.CENTER);

        Button btnOpenConfig = new Button("Open Config");
        Button btnDismiss = new Button("Dismiss");
        
        btnOpenConfig.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {   
            	close();
				parent.openConfigStage();
            	
				parent.handleStageClosed(NothingMountedStage.this);                
            }
        });

        btnDismiss.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {
				parent.handleStageClosed(NothingMountedStage.this);
                close();
            }
        });

        hboxButtons.getChildren().add(btnDismiss);
        hboxButtons.getChildren().add(btnOpenConfig);

        vbox.getChildren().add(lblMsg1);       
        vbox.getChildren().add(lblMsg2);       
        vbox.getChildren().add(cbShowWarning);
        vbox.getChildren().add(hboxButtons);       
		
		setScene(scene);
		
		setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				parent.handleStageClosed(NothingMountedStage.this);
			}
		});
	}
}