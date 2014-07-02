/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui;

import java.util.ArrayList;
import java.util.List;

import javafx.stage.Stage;

public class StageManager
{
	private List<Stage> openStages = new ArrayList<>();
	
	public void add(Stage stage)
	{
		openStages.add(stage);
	}
	
	public void remove(Stage stage)
	{
		openStages.remove(stage);
	}

	public void closeAll()
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
