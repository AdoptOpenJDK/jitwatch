/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.graphing;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_FREE_CODE_CACHE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_CODE_CACHE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_CODE_CACHE_FULL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_SWEEPER;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.adoptopenjdk.jitwatch.model.CodeCacheEvent;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.ui.JITWatchUI;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;

public class CodeCacheStage extends AbstractGraphStage
{
	private boolean labelLeft = true;

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

		labelLeft = true;

		List<CodeCacheEvent> codeCacheEvents = parent.getJITDataModel().getCodeCacheEvents();

		Collections.sort(codeCacheEvents, new Comparator<CodeCacheEvent>()
		{
			@Override
			public int compare(CodeCacheEvent e1, CodeCacheEvent e2)
			{
				return Long.compare(e1.getStamp(), e2.getStamp());
			}
		});

		if (codeCacheEvents.size() > 0)
		{
			CodeCacheEvent firstEvent = codeCacheEvents.get(0);
			minX = firstEvent.getStamp();

			Tag endOfLogTag = parent.getJITDataModel().getEndOfLogTag();

			if (endOfLogTag != null)
			{
				maxX = getStampFromTag(endOfLogTag);

			}
			else
			{
				CodeCacheEvent lastEvent = codeCacheEvents.get(codeCacheEvents.size() - 1);
				maxX = lastEvent.getStamp();
			}

			minY = getFreeCodeCacheFromEvent(firstEvent);
			maxY = getFreeCodeCacheFromEvent(firstEvent);

			// find ranges
			for (CodeCacheEvent event : codeCacheEvents)
			{
				long freeCodeCache = getFreeCodeCacheFromEvent(event);

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
			double lastCY = graphGapTop + normaliseY(getFreeCodeCacheFromEvent(firstEvent));

			Color colourLine = Color.BLUE;
			double lineWidth = 2.0;

			for (CodeCacheEvent event : codeCacheEvents)
			{
				long stamp = event.getStamp();

				double x = graphGapLeft + normaliseX(stamp);
				double y = lastCY;

				switch (event.getTag().getName())
				{
				case TAG_CODE_CACHE:
					y = addToGraph(lastCX, lastCY, colourLine, lineWidth, event, x);
					break;

				case TAG_SWEEPER:
					y = addToGraph(lastCX, lastCY, colourLine, lineWidth, event, x);
					showLabel("Sweep", Color.WHITE, x, y);
					break;

				case TAG_CODE_CACHE_FULL:
					showLabel("Code Cache Full", Color.RED, x, y);
					break;
				}

				lastCY = y;
				lastCX = x;
			}

			continueLineToEndOfXAxis(lastCX, lastCY, colourLine, lineWidth);
		}
		else
		{
			gc.fillText("No code cache information in log", fix(10), fix(10));
		}
	}

	private double addToGraph(double lastCX, double lastCY, Color colourLine, double lineWidth, CodeCacheEvent event, double x)
	{
		long freeCodeCache = getFreeCodeCacheFromEvent(event);

		double y = graphGapTop + normaliseY(freeCodeCache);

		gc.setFill(colourLine);
		gc.setStroke(colourLine);
		gc.setLineWidth(lineWidth);

		gc.strokeLine(fix(lastCX), fix(lastCY), fix(x), fix(y));
		return y;
	}

	private void showLabel(String text, Color background, double x, double y)
	{
		double labelX;
		double labelY;

		if (labelLeft)
		{
			labelX = x - getApproximateStringWidth(text) - 16;
			labelY = Math.min(y - getStringHeight(), graphGapTop + chartHeight - 32);

		}
		else
		{
			labelX = x + 8;
			labelY = Math.min(y, graphGapTop + chartHeight - 32);
		}

		drawLabel(text, labelX, labelY, background);

		labelLeft = !labelLeft;
	}

	private long getFreeCodeCacheFromEvent(CodeCacheEvent event)
	{
		Map<String, String> attrs = event.getTag().getAttributes();
		return Long.parseLong(attrs.get(ATTR_FREE_CODE_CACHE));
	}
}