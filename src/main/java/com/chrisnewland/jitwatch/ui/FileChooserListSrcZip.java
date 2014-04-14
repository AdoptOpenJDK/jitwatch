/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.chrisnewland.jitwatch.core.JITWatchConfig;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileChooserListSrcZip extends FileChooserList
{
    private static final Logger logger = LoggerFactory.getLogger(FileChooserList.class);

	public FileChooserListSrcZip(Stage stage, String title, List<String> items)
	{
		super(stage, title, items);

		Button btnAddSrcZip = new Button("Add JDK src");
		btnAddSrcZip.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				File srcZipFile = JITWatchConfig.getJDKSourceZip();

				if (srcZipFile != null)
				{
					try
					{
						fileList.getItems().add(new Label(srcZipFile.getCanonicalPath()));
					}
					catch (IOException ioe)
					{
						logger.error("{}", ioe);
					}
				}
			}
		});
		
		vboxButtons.getChildren().add(btnAddSrcZip);
	}
}