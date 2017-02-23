/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.main;

import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.ui.stage.StageManager;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

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
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class NothingMountedStage extends Stage
{	
	public NothingMountedStage(final JITWatchUI parent, final JITWatchConfig config)
	{
		initStyle(StageStyle.DECORATED);
			
		VBox vbox = new VBox();
		vbox.setSpacing(20);
		vbox.setPadding(new Insets(10));
		
		Scene scene = UserInterfaceUtil.getScene(vbox, 530, 200);
	
		setTitle("No classes or sources mounted");
		
        Label lblMsg1 = new Label("I see you've not configured any source or class locations?");
        Text lblMsg2 = new Text("JITWatch will try and work out the classpath from the HotSpot log but if I miss\nanything then you might need to configure the paths manually!");
        
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
        hboxButtons.setSpacing(20);
        hboxButtons.setPadding(new Insets(10));
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

        btnDismiss.setOnAction(StageManager.getCloseHandler(this));

        hboxButtons.getChildren().add(btnDismiss);
        hboxButtons.getChildren().add(btnOpenConfig);

        vbox.getChildren().add(lblMsg1);       
        vbox.getChildren().add(lblMsg2);      
        //vbox.getChildren().add(lblMsg3);       
        vbox.getChildren().add(cbShowWarning);
        vbox.getChildren().add(hboxButtons);       
		
		setScene(scene);
	}
}