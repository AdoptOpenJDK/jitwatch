/*
 * Copyright (c) 2013-2021 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.filechooser;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import org.adoptopenjdk.jitwatch.util.FileUtil;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;
import com.chrisnewland.freelogj.Logger;
import com.chrisnewland.freelogj.LoggerFactory;

public class FileChooserListSrcZip extends FileChooserList
{
    private static final Logger logger = LoggerFactory.getLogger(FileChooserList.class);

	public FileChooserListSrcZip(Stage stage, String title, List<String> items)
	{
		super(stage, title, items);

		Button btnAddSrcZip = UserInterfaceUtil.createButton("CONFIG_ADD_JDK_SRC");
		
		btnAddSrcZip.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				File srcZipFile = FileUtil.getJDKSourceZip();
				
				if (srcZipFile != null)
				{
					try
					{
						String srcZipPath = srcZipFile.getCanonicalPath();
						
						addPathToList(srcZipPath);
					}
					catch (IOException ioe)
					{
						logger.error("", ioe);
					}
				}
			}
		});
		
		vboxButtons.getChildren().add(btnAddSrcZip);
	}
}
