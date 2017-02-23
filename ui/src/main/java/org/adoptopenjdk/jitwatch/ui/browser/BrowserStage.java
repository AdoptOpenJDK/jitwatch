/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.browser;

import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class BrowserStage extends Stage
{
	private WebView web;

	private WebEngine webEngine;

	public BrowserStage()
	{
		initStyle(StageStyle.DECORATED);

		web = new WebView();

		Scene scene = UserInterfaceUtil.getScene(web, 800, 480);

		webEngine = web.getEngine();

		setScene(scene);
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