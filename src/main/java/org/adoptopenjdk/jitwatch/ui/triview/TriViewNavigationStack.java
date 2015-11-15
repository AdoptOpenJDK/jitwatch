/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.triview;

import java.util.Deque;
import java.util.LinkedList;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import org.adoptopenjdk.jitwatch.model.IMetaMember;

public class TriViewNavigationStack
{
	private boolean isCtrlPressed = false;
	private Deque<IMetaMember> navigationStack = new LinkedList<>();
	private TriView triView;

	public TriViewNavigationStack(final TriView triView, final Scene scene)
	{
		this.triView = triView;

		scene.setOnKeyPressed(new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent event)
			{
				KeyCode code = event.getCode();

				if (code == KeyCode.CONTROL)
				{
					isCtrlPressed = true;
				}
			}
		});

		scene.setOnKeyReleased(new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent event)
			{
				KeyCode code = event.getCode();

				if (code == KeyCode.CONTROL)
				{
					isCtrlPressed = false;
				}
				else if (code == KeyCode.BACK_SPACE)
				{
					if (!navigationStack.isEmpty())
					{
						IMetaMember previous = navigationStack.pop();

						triView.setMember(previous, false);
					}
				}
			}
		});
	}

	public void navigateTo(IMetaMember nextMember)
	{
		if (nextMember != null)
		{
			IMetaMember currentMember = triView.getMetaMember();

			if (currentMember != null)
			{
				navigationStack.push(currentMember);
			}

			triView.setMember(nextMember, false);
		}
	}

	public boolean isCtrlPressed()
	{
		return isCtrlPressed;
	}
}