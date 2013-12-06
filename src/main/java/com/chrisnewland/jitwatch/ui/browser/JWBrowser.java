/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/blob/master/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
package com.chrisnewland.jitwatch.ui.browser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;

import com.chrisnewland.jitwatch.ui.JITWatchUI;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class JWBrowser extends HBox implements org.w3c.dom.events.EventListener
{
	private WebView webView;
	private WebEngine engine;

	public static final String EVENT_TYPE_CLICK = "click";

	// Called by JFX
	public JWBrowser()
	{
		webView = new WebView();

		// webView.prefHeightProperty().bind(scene.heightProperty());

		engine = webView.getEngine();

		engine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>()
		{
			@Override
			public void changed(ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState)
			{
				if (newState == Worker.State.SUCCEEDED)
				{
					Document doc = engine.getDocument();

					NodeList nodeList = doc.getElementsByTagName("a");

					for (int i = 0; i < nodeList.getLength(); i++)
					{
						((EventTarget) nodeList.item(i)).addEventListener(EVENT_TYPE_CLICK, JWBrowser.this, false);
					}
				}
			}

		});

		getChildren().add(webView);

	}

	public void setContent(String html)
	{
		StringBuilder htmlBuilder = new StringBuilder();

		if (html != null)
		{
			htmlBuilder.append("<html><head><style>");
			htmlBuilder.append("body {font-family:monospaced;font-size:8pt}");
			htmlBuilder.append("</style></head>");
			htmlBuilder.append("<body>");

			String[] lines = html.split("\n");

			for (String line : lines)
			{
				line = line.replace("  ", "&#160;&#160;");

				htmlBuilder.append("<div>").append(line).append("</div>");
			}

			htmlBuilder.append("</body></html>");
		}
		
		engine.loadContent(htmlBuilder.toString());
	}

	@Override
	public void handleEvent(Event evt)
	{

		if (EVENT_TYPE_CLICK.equals(evt.getType()))
		{
			String href = ((Element) evt.getTarget()).getAttribute("href");

			System.out.println(href);
		}
	}

}