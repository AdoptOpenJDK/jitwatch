/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.triview.source;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_DOUBLE_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE_CR;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_TAB;

import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.ui.main.IStageAccessProxy;
import org.adoptopenjdk.jitwatch.ui.triview.ILineListener;
import org.adoptopenjdk.jitwatch.ui.triview.Viewer;
import org.adoptopenjdk.jitwatch.ui.triview.ILineListener.LineType;
import org.adoptopenjdk.jitwatch.ui.triview.InstructionLabel;
import org.adoptopenjdk.jitwatch.util.StringUtil;

import javafx.scene.control.Label;

public class ViewerSource extends Viewer
{	
	public ViewerSource(IStageAccessProxy stageAccessProxy, ILineListener lineListener, LineType lineType)
	{
		super(stageAccessProxy, lineListener, lineType, true);
	}
	
	@Override
	public void setContent(String inSource, boolean showLineNumbers, boolean canHighlight)
	{
		clear();

		String source = inSource;

		isHighlighting = canHighlight;

		if (source == null)
		{
			source = "Empty";
		}

		originalSource = source;

		source = source.replace(S_TAB, S_DOUBLE_SPACE);

		String[] lines = source.split(S_NEWLINE);

		int maxWidth = Integer.toString(lines.length).length();

		List<Label> labels = new ArrayList<>();

		for (int i = 0; i < lines.length; i++)
		{
			String row = lines[i];

			if (showLineNumbers)
			{
				lines[i] = StringUtil.padLineNumber(i + 1, maxWidth) + S_DOUBLE_SPACE + row;
			}

			lines[i] = lines[i].replace(S_NEWLINE_CR, S_EMPTY);

			InstructionLabel lblInstruction = new InstructionLabel(lines[i]);
			
			labels.add(lblInstruction);
		}

		setContent(labels);
	}
}