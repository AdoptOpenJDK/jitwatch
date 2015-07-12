/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.graphing;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.adoptopenjdk.jitwatch.core.JITWatchConstants;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.ui.JITWatchUI;
import org.adoptopenjdk.jitwatch.util.ParseUtil;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.StageStyle;

public class CodeCacheStage extends AbstractGraphStage
{
	public CodeCacheStage(JITWatchUI parent)
	{
		super(parent, JITWatchUI.WINDOW_WIDTH, JITWatchUI.WINDOW_HEIGHT, true);

		initStyle(StageStyle.DECORATED);

		StackPane root = new StackPane();
		Scene scene = UserInterfaceUtil.getScene(root, width, height);

		canvas.widthProperty().bind(root.widthProperty());
		canvas.heightProperty().bind(root.heightProperty());

		root.getChildren().add(canvas);

		setTitle("JITWatch Free Code Cache");

		setScene(scene);
		show();

		redraw();
	}

	@Override
	public final void redraw()
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

			double lastCX = graphGapLeft + normaliseX(minX);
			double lastCY = graphGapTop + normaliseY(getFreeCodeCacheFromTag(firstTag));

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

				double x = graphGapLeft + normaliseX(stamp);
				double y = graphGapTop + normaliseY(freeCodeCache);

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
		return ParseUtil.parseStamp(attrs.get(JITWatchConstants.ATTR_STAMP));

	}

	private long getFreeCodeCacheFromTag(Tag tag)
	{
		Map<String, String> attrs = tag.getAttrs();
		return Long.parseLong(attrs.get(JITWatchConstants.ATTR_FREE_CODE_CACHE));
	}
}