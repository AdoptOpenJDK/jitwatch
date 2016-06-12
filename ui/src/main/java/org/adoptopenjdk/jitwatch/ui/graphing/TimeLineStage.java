/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.graphing;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILER;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILE_KIND;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILE_MILLIS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_DECOMPILES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_LEVEL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_CLOSE_PARENTHESES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_OPEN_PARENTHESES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_CLOSE_PARENTHESES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_NMETHOD;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_TASK_QUEUED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;

import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.JITEvent;
import org.adoptopenjdk.jitwatch.model.JITStats;
import org.adoptopenjdk.jitwatch.model.Journal;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.ui.JITWatchUI;
import org.adoptopenjdk.jitwatch.util.ParseUtil;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

public class TimeLineStage extends AbstractGraphStage
{
	private IMetaMember selectedMember = null;
	private List<Tag> selectedMemberJournalTags = new ArrayList<>();
	private int selectedTagIndex = 0;
	private static final double MARKET_DIAMETER = 10;
	private boolean labelLeft = true;
	
	public TimeLineStage(final JITWatchUI parent)
	{
		super(parent, JITWatchUI.WINDOW_WIDTH, JITWatchUI.WINDOW_HEIGHT, true);

		initStyle(StageStyle.DECORATED);

		StackPane root = new StackPane();
		Scene scene = UserInterfaceUtil.getScene(root, width, height);

		canvas.widthProperty().bind(root.widthProperty());
		canvas.heightProperty().bind(root.heightProperty());

		root.getChildren().add(canvas);

		setTitle("Compilations Timeline");

		setScene(scene);
		show();

		redraw();
	}

	private void processMemberEvents()
	{
		if (selectedMember != null)
		{
			Journal selectedJournal = selectedMember.getJournal();

			if (selectedJournal != null)
			{
				selectedMemberJournalTags = selectedJournal.getEntryList();

				Iterator<Tag> iter = selectedMemberJournalTags.iterator();

				while (iter.hasNext())
				{
					Tag tag = iter.next();

					if (!TAG_NMETHOD.equals(tag.getName()) && !TAG_TASK_QUEUED.equals(tag.getName()))
					{
						iter.remove();
					}
				}
			}
		}
		else
		{
			selectedMemberJournalTags.clear();
		}
	}

	@Override
	public final void redraw()
	{
		super.baseRedraw();

		gc.setFont(STANDARD_FONT);

		if (selectedMember != parent.getSelectedMember())
		{
			selectedMember = parent.getSelectedMember();
			processMemberEvents();
		}

		List<JITEvent> events = parent.getJITDataModel().getEventListCopy();

		selectedTagIndex = 0;

		if (events.size() > 0)
		{
			Collections.sort(events, new Comparator<JITEvent>()
			{
				@Override
				public int compare(JITEvent e1, JITEvent e2)
				{
					return Long.compare(e1.getStamp(), e2.getStamp());
				}
			});

			JITEvent firstEvent = events.get(0);
			minX = firstEvent.getStamp();

			Tag endOfLogTag = parent.getJITDataModel().getEndOfLogTag();
			
			if (endOfLogTag != null)
			{
				maxX = getStampFromTag(endOfLogTag);

			}
			else
			{
				JITEvent lastEvent = events.get(events.size() - 1);
				maxX = lastEvent.getStamp();
			}	

			minY = 0;

			calculateMaxCompiles(events);

			drawAxes();

			drawEvents(events);

			showSelectedMemberLabel();
		}
		else
		{
			gc.fillText("No compilation information processed", fix(10), fix(10));
		}
	}

	private void calculateMaxCompiles(List<JITEvent> events)
	{
		maxY = events.size();
	}

	private void drawMemberEvents(long stamp, double yPos)
	{
		if (selectedTagIndex >= selectedMemberJournalTags.size())
		{
			return;
		}

		Tag nextJournalEvent = selectedMemberJournalTags.get(selectedTagIndex);

		long journalEventTime = ParseUtil.getStamp(nextJournalEvent.getAttributes());

		if (journalEventTime == stamp)
		{
			selectedTagIndex++;

			gc.setFill(Color.BLUE);

			double smX = graphGapLeft + normaliseX(journalEventTime);

			double blobX = fix(smX - MARKET_DIAMETER / 2);
			double blobY = fix(yPos - MARKET_DIAMETER / 2);

			gc.fillOval(blobX, blobY, fix(MARKET_DIAMETER), fix(MARKET_DIAMETER));

			String label = buildLabel(nextJournalEvent, journalEventTime);

			double labelX;
			double labelY;

			if (labelLeft)
			{
				labelX = blobX - getApproximateStringWidth(label) - 16;
				labelY = Math.min(blobY - getStringHeight(), graphGapTop + chartHeight - 32);

			}
			else
			{
				labelX = blobX + 16;
				labelY = Math.min(blobY, graphGapTop + chartHeight - 32);
			}

			labelLeft = !labelLeft;

			drawLabel(label, labelX, labelY, getLabelColour(nextJournalEvent));
		}
	}
	
	private Color getLabelColour(Tag tag)
	{
		Color result = Color.WHITE;
		
		String tagName = tag.getName();
		
		if (TAG_NMETHOD.equals(tagName))
		{
			if (tag.containsAttribute(ATTR_DECOMPILES))
			{
				result = Color.ORANGERED;
			}
			else
			{
				result = Color.LIMEGREEN;
			}				 
		}
		else if (TAG_TASK_QUEUED.equals(tagName))
		{
			result = Color.YELLOW;
		}
		
		return result;
	}

	private void showSelectedMemberLabel()
	{
		if (selectedMember != null)
		{
			gc.setFont(MEMBER_FONT);
			drawLabel(selectedMember.toString(), 56, 40);
		}
	}

	private void drawLabel(String text, double xPos, double yPos)
	{
		drawLabel(text, xPos, yPos, Color.WHITE);
	}
	
	private String buildLabel(Tag nextJournalEvent, long journalEventTime)
	{
		StringBuilder selectedItemBuilder = new StringBuilder();

		String tagName = nextJournalEvent.getName();

		if (TAG_TASK_QUEUED.equals(tagName))
		{
			selectedItemBuilder.append("Queued");
		}
		else
		{
			if (nextJournalEvent.getAttribute(ATTR_DECOMPILES) != null)
			{
				selectedItemBuilder.append("Recompiled");
			}
			else
			{
				selectedItemBuilder.append("Compiled");
			}

			String compiler = nextJournalEvent.getAttribute(ATTR_COMPILER);

			if (compiler == null)
			{
				compiler = "unknown!";
			}

			selectedItemBuilder.append(" by ").append(compiler);

			String compileKind = nextJournalEvent.getAttribute(ATTR_COMPILE_KIND);

			if (compileKind != null)
			{
				selectedItemBuilder.append(C_SPACE).append(C_OPEN_PARENTHESES).append(compileKind.toUpperCase()).append(C_CLOSE_PARENTHESES);
			}

			String level = nextJournalEvent.getAttribute(ATTR_LEVEL);

			if (level != null)
			{
				selectedItemBuilder.append(" (Level ").append(level).append(C_CLOSE_PARENTHESES);
			}

			String compiletime = selectedMember.getCompiledAttribute(ATTR_COMPILE_MILLIS);

			if (compiletime != null)
			{
				selectedItemBuilder.append(" in ").append(compiletime).append("ms");
			}
		}

		return selectedItemBuilder.toString();
	}

	private void drawEvents(List<JITEvent> events)
	{
		Color colourMarker = Color.BLUE;
		double lineWidth = 2.0;

		int cumC = 0;

		double lastCX = graphGapLeft + normaliseX(minX);
		double lastCY = graphGapTop + normaliseY(0);

		for (JITEvent event : events)
		{
			long stamp = event.getStamp();

			cumC++;

			double x = graphGapLeft + normaliseX(stamp);

			double y = graphGapTop + normaliseY(cumC);

			if (selectedMemberJournalTags.size() > 0)
			{
				drawMemberEvents(stamp, y);
			}

			gc.setStroke(colourMarker);
			gc.setLineWidth(lineWidth);
			gc.strokeLine(fix(lastCX), fix(lastCY), fix(x), fix(y));

			lastCX = x;
			lastCY = y;
		}
		
		continueLineToEndOfXAxis(lastCX, lastCY, colourMarker, lineWidth);

		showStatsLegend(gc);
	}

	private void showStatsLegend(GraphicsContext gc)
	{
		JITStats stats = parent.getJITDataModel().getJITStats();

		StringBuilder compiledStatsBuilder = new StringBuilder();
		compiledStatsBuilder.append("Total Compilations: ").append(stats.getTotalCompiledMethods());
		compiledStatsBuilder.append(" (C1: ").append(stats.getCountC1()).append(S_CLOSE_PARENTHESES);
		compiledStatsBuilder.append(" (C2: ").append(stats.getCountC2()).append(S_CLOSE_PARENTHESES);
		compiledStatsBuilder.append(" (C2N: ").append(stats.getCountC2N()).append(S_CLOSE_PARENTHESES);
		compiledStatsBuilder.append(" (OSR: ").append(stats.getCountOSR()).append(S_CLOSE_PARENTHESES);

		setStrokeForText();
		gc.fillText(compiledStatsBuilder.toString(), fix(graphGapLeft), fix(2));
	}
}