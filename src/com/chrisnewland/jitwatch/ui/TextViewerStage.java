package com.chrisnewland.jitwatch.ui;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class TextViewerStage extends Stage
{
    private TextArea textArea;
    
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
        
        source = source.replace("\t",  "    "); // 4 spaces
        
        StringBuilder builder = new StringBuilder();
       
        String[] rows = source.split("\n");
        
        int max = 0;
        
        for (int i = 0; i < rows.length; i++)
        {
        	String row = rows[i];
        	
        	if (showLineNumbers)
        	{
        		builder.append(i+1).append(' ');
        	}
        	
        	builder.append(row).append("\n");
        	
            int rowLen = row.length();
            
            if (rowLen > max)
            {
                max = rowLen;
            }
        }
        
        int x = Math.min(80, max);
        int y = Math.min(30, rows.length);
        
        x = Math.max(x, 20);
        y = Math.max(y, 20);
        
        VBox vbox = new VBox();

        textArea = new TextArea();
        textArea.setText(builder.toString());
        textArea.setStyle("-fx-font-family:monospace;");

        vbox.setPadding(new Insets(4));
          
        vbox.getChildren().add(textArea);
        VBox.setVgrow(textArea, Priority.ALWAYS);

        setTitle(title);
        
        Scene scene = new Scene(vbox, x * 12, y * 19);

        setScene(scene);
    }
    
    public void jumpTo(String regex)
    {        
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