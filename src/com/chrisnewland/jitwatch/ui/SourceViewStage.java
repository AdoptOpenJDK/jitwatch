package com.chrisnewland.jitwatch.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SourceViewStage extends Stage
{
    private TextArea textArea;
    
    public SourceViewStage(String title, String source)
    {
        initStyle(StageStyle.DECORATED);
        
        if (source == null)
        {
        	source = "Empty";
        }
        
        source = source.replace("\t",  "    "); // 4 spaces

        VBox vbox = new VBox();

        textArea = new TextArea();
        textArea.setText(source);
        textArea.setStyle("-fx-font-family:monospace;");

        vbox.setPadding(new Insets(4));
          
        vbox.getChildren().add(textArea);
        VBox.setVgrow(textArea, Priority.ALWAYS);

        setTitle(title);
        
        String[] rows = source.split("\n");
        
        int max = 0;
        
        for (int i = 0; i < rows.length; i++)
        {
            int rowLen = rows[i].length();
            
            if (rowLen > max)
            {
                max = rowLen;
            }
        }
        
        int x = Math.min(80, max);
        int y = Math.min(30, rows.length);
        
        x = Math.max(x, 20);
        y = Math.max(y, 20);
        
        Scene scene = new Scene(vbox, x * 12, y * 19);

        setScene(scene);
    }
    
    public void jumpTo(String regex)
    {
        System.out.println("Looking for " + regex);
        
    	Platform.runLater(new Runnable()
        {
            
            @Override
            public void run()
            {
                textArea.setScrollTop(0);
            }
        });
    }
}