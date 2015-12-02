/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.stage.Stage;

public class StageManager
{
	private static Map<Stage, List<Stage>> openStages = new HashMap<>();

	private StageManager()
	{
	}

	public static void addAndShow(Stage parent, Stage childStage)
	{
		List<Stage> childrenOfParent = openStages.get(parent);
		
		if (childrenOfParent == null)
		{
			childrenOfParent = new ArrayList<>();
			openStages.put(parent, childrenOfParent);
		}
		
		childrenOfParent.add(childStage);
		
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

	public static void closeStageAndChildren(Stage stage)
	{		
		List<Stage> childrenOfParent = openStages.get(stage);
		
		if (childrenOfParent != null)
		{
			for (Stage child : childrenOfParent)
			{
				closeStageAndChildren(child);
			}
		}
		
		stage.close();
		
		openStages.remove(stage);
	}
}
