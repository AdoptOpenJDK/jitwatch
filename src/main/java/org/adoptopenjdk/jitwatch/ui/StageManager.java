/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui;

import java.util.ArrayList;
import java.util.List;
import javafx.stage.Stage;

public class StageManager
{
	private static List<Stage> openStages = new ArrayList<>();

	private StageManager()
	{
	}

	public static void addAndShow(Stage parent, Stage childStage)
	{
		openStages.add(childStage);

		childStage.show();

		double parentX = parent.getX();
		double parentY = parent.getY();
		double parentWidth = parent.getWidth();
		double parentHeight = parent.getHeight();

		double childWidth = childStage.getWidth();
		double childHeight = childStage.getHeight();

		double childX = parentX + (parentWidth - childWidth) / 2;
		double childY = parentY + (parentHeight - childHeight) / 2;

		childStage.setX(childX);
		childStage.setY(childY);
	}

	public static void remove(Stage stage)
	{
		openStages.remove(stage);
	}

	public static void closeAll()
	{
		for (Stage s : openStages)
		{
			if (s != null)
			{
				s.close();
			}
		}
	}
}
