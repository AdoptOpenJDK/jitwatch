/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.resize;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class RateLimitedResizeListener implements ChangeListener<Number>
{
	private long lastResizeRedraw;

	private boolean delayedRedrawQueued = false;
	private boolean delayedRedrawRequired = false;

	private IRedrawable redrawable;

	private long minIntervalMillis;

	public RateLimitedResizeListener(IRedrawable redrawable, long minIntervalMillis)
	{
		this.redrawable = redrawable;
		this.minIntervalMillis = minIntervalMillis;
	}

	@Override
	public void changed(ObservableValue<? extends Number> property, Number from, Number to)
	{
		if (to.doubleValue() <= 1.0)
		{
			return;
		}

		long now = System.currentTimeMillis();

		if (now - lastResizeRedraw > minIntervalMillis)
		{
			redrawable.redraw();

			delayedRedrawRequired = false;

			lastResizeRedraw = now;
		}
		else
		{
			// we skipped a redraw
			
			if (!delayedRedrawQueued)
			{
				delayedRedrawQueued = true;
				delayedRedrawRequired = true;
				
				new Thread(new Runnable()
				{
					@Override
					public void run() // off UI thread
					{						
						try
						{
							Thread.sleep(1000); // wait is off UI thread

							Platform.runLater(new Runnable()
							{
								@Override
								public void run() // on UI thread
								{									
									if (delayedRedrawRequired)
									{
										redrawable.redraw();

										delayedRedrawRequired = false;
									}

									delayedRedrawQueued = false;

								}
							});
						}
						catch (InterruptedException e)
						{
						}
					}
				}).start();
			}
		}
	}
}