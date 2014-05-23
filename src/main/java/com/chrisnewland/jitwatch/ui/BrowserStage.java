/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class BrowserStage extends Stage
{
    private static final int MAX_WIDTH = 800;
    private static final int MAX_HEIGHT = 480;

    private WebView web;

	private WebEngine webEngine;

	public BrowserStage(final JITWatchUI parent)
	{
		initStyle(StageStyle.DECORATED);

		web = new WebView();

		Scene scene = new Scene(web, MAX_WIDTH, MAX_HEIGHT);

		webEngine = web.getEngine();

		setScene(scene);

		setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				parent.handleStageClosed(BrowserStage.this);
			}
		});
	}

	public void setContent(final String title, final String html, String stylesheet)
	{
		setTitle(title);
		
		webEngine.loadContent(html);

		if (stylesheet != null)
		{
			webEngine.setUserStyleSheetLocation(stylesheet);
		}

		toFront();
	}
}