/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.chrisnewland.jitwatch.core.JITWatchConstants;
import com.chrisnewland.jitwatch.model.Tag;
import com.chrisnewland.jitwatch.util.ParseUtil;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.StageStyle;

public class CodeCacheStage extends AbstractGraphStage
{
	public CodeCacheStage(JITWatchUI parent)
	{
		super(parent, 640, 480, true);

		initStyle(StageStyle.DECORATED);

		StackPane root = new StackPane();
		Scene scene = new Scene(root, width, height);

		canvas.widthProperty().bind(root.widthProperty());
		canvas.heightProperty().bind(root.heightProperty());

		root.getChildren().add(canvas);

		setTitle("JITWatch Free Code Cache");

		setScene(scene);
		show();

		redraw();
	}

	@Override
	public void redraw()
	{
		super.baseRedraw();

		List<Tag> codeCacheTags = parent.getJITDataModel().getCodeCacheTags();

		Collections.sort(codeCacheTags, new Comparator<Tag>()
		{
			@Override
			public int compare(Tag t1, Tag t2)
			{
				return Long.compare(getStampFromTag(t1), getStampFromTag(t2));
			}
		});

		if (codeCacheTags.size() > 0)
		{
			Tag firstTag = codeCacheTags.get(0);
			Tag lastTag = codeCacheTags.get(codeCacheTags.size() - 1);

			minX = getStampFromTag(firstTag);
			maxX = getStampFromTag(lastTag);

			minY = getFreeCodeCacheFromTag(firstTag);
			maxY = getFreeCodeCacheFromTag(firstTag);

			// find ranges
			for (Tag ccTag : codeCacheTags)
			{
				long freeCodeCache = getFreeCodeCacheFromTag(ccTag);

				if (freeCodeCache > maxY)
				{
					maxY = freeCodeCache;
				}
				else if (freeCodeCache < minY)
				{
					minY = freeCodeCache;
				}
			}

			drawAxes();

			double lastCX = GRAPH_GAP_LEFT + normaliseX(minX);
			double lastCY = GRAPH_GAP_Y + normaliseY(getFreeCodeCacheFromTag(firstTag));

			gc.setStroke(Color.BLACK);
			gc.setFont(new Font("monospace", 10));

			Color colourLine = Color.BLUE;

			gc.setFill(colourLine);
			gc.setStroke(colourLine);
			gc.setLineWidth(2.0);

			for (Tag ccTag : codeCacheTags)
			{
				long stamp = getStampFromTag(ccTag);
				long freeCodeCache = getFreeCodeCacheFromTag(ccTag);

				double x = GRAPH_GAP_LEFT + normaliseX(stamp);
				double y = GRAPH_GAP_Y + normaliseY(freeCodeCache);

				gc.strokeLine(fix(lastCX), fix(lastCY), fix(x), fix(y));

				lastCX = x;
				lastCY = y;
			}

			gc.setLineWidth(1.0);
		}
		else
		{
			gc.strokeText("No code cache information in log", fix(10), fix(10));

		}
	}

	private long getStampFromTag(Tag tag)
	{
		Map<String, String> attrs = tag.getAttrs();
		long stamp = ParseUtil.parseStamp(attrs.get(JITWatchConstants.ATTR_STAMP));
		return stamp;
	}

	private long getFreeCodeCacheFromTag(Tag tag)
	{
		Map<String, String> attrs = tag.getAttrs();
		long freeCodeCache = Long.parseLong(attrs.get(JITWatchConstants.ATTR_FREE_CODE_CACHE));
		return freeCodeCache;
	}
}