package com.chrisnewland.jitwatch.ui;

import java.util.List;

import com.chrisnewland.jitwatch.core.JITEvent;
import com.chrisnewland.jitwatch.core.JITStats;
import com.chrisnewland.jitwatch.core.StringUtil;
import com.chrisnewland.jitwatch.meta.IMetaMember;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class TimeLineStage extends Stage
{
	private Canvas canvas;
	private GraphicsContext gc;
	private JITWatchUI parent;

	private static final int GRAPH_GAP_LEFT = 60;
	private static final int GRAPH_GAP_RIGHT = 20;

	private static final int GRAPH_GAP_Y = 20;

	private static final int[] Y_SCALE = new int[21];

	static
	{
		int multiplier = 1;

		for (int i = 0; i < Y_SCALE.length; i += 3)
		{
			Y_SCALE[i + 0] = 1 * multiplier;
			Y_SCALE[i + 1] = 2 * multiplier;
			Y_SCALE[i + 2] = 5 * multiplier;

			multiplier *= 10;
		}
	}

	public TimeLineStage(final JITWatchUI parent)
	{
		this.parent = parent;

		initStyle(StageStyle.DECORATED);

		setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				parent.handleStageClosed(TimeLineStage.this);
			}
		});

		int width = 640;
		int height = 480;

		canvas = new Canvas(width, height);
		gc = canvas.getGraphicsContext2D();

		StackPane root = new StackPane();
		Scene scene = new Scene(root, width, height);

		canvas.widthProperty().bind(root.widthProperty());
		canvas.heightProperty().bind(root.heightProperty());

		class SceneResizeListener implements ChangeListener<Number>
		{
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2)
			{
				redraw();
			}
		}

		SceneResizeListener rl = new SceneResizeListener();

		canvas.widthProperty().addListener(rl);
		canvas.heightProperty().addListener(rl);

		root.getChildren().add(canvas);

		setTitle("JITWatch Compilations Timeline");

		setScene(scene);
		show();

		redraw();
	}

	public void redraw()
	{
		List<JITEvent> events = parent.getJITEvents();

		if (events.size() > 0)
		{
			double width = canvas.getWidth();
			double height = canvas.getHeight();
			double chartWidth = width - GRAPH_GAP_LEFT - GRAPH_GAP_RIGHT;
			double chartHeight = height - GRAPH_GAP_Y * 2;

			gc.setFill(Color.WHITE);
			gc.fillRect(0, 0, width, height);
			gc.setFill(Color.BEIGE);
			gc.fillRect(GRAPH_GAP_LEFT, GRAPH_GAP_Y, chartWidth, chartHeight);
			gc.setStroke(Color.BLACK);
			gc.strokeRect(GRAPH_GAP_LEFT, GRAPH_GAP_Y, chartWidth, chartHeight);

			long minStamp = 0;
			long maxStamp = events.get(events.size() - 1).getStamp();

			// assume compiled is no more than 20% bigger than queued
			int maxEvents = (int) (events.size() / 2.0 * 1.2);

			double lastCX = GRAPH_GAP_LEFT + normalise(0, minStamp, maxStamp, chartWidth, false);
			double lastCY = GRAPH_GAP_Y + normalise(0, 0, maxEvents, chartHeight, true);

			gc.setStroke(Color.BLACK);
			gc.setFont(new Font("monospace", 10));

			drawXAxis(gc, minStamp, maxStamp, chartWidth, chartHeight);
			drawYAxis(gc, maxEvents, chartWidth, chartHeight);

			IMetaMember selectedMember = parent.getSelectedMember();

			double compiledStampTime = -1;

			if (selectedMember != null)
			{
				String cStamp = selectedMember.getCompiledAttribute("stamp");

				if (cStamp != null)
				{
					compiledStampTime = 1000 * Double.parseDouble(cStamp);
				}
			}

			Color colourLine = Color.RED;
			Color colourMarker = Color.BLUE;

			int cumC = 0;
			int markerDiameter = 8;

			for (JITEvent event : events)
			{
				long stamp = event.getStamp();

				double x = GRAPH_GAP_LEFT + normalise(stamp, minStamp, maxStamp, chartWidth, false);

				if (event.isCompile())
				{
					gc.setFill(colourLine);
					gc.setStroke(colourLine);

					cumC++;

					double y = GRAPH_GAP_Y + normalise(cumC, 0, maxEvents, chartHeight, true);
					gc.strokeLine(lastCX, lastCY, x, y);

					lastCX = x;
					lastCY = y;

					if (compiledStampTime != -1 && stamp > compiledStampTime)
					{
						double smX = GRAPH_GAP_LEFT + normalise(compiledStampTime, minStamp, maxStamp, chartWidth, false);

						gc.setFill(colourMarker);
						gc.setStroke(colourMarker);
						gc.fillOval(smX - markerDiameter / 2, y - markerDiameter / 2, markerDiameter, markerDiameter);

						String line1 = selectedMember.toStringUnqualifiedMethodName();

						String compiler = selectedMember.getCompiledAttribute("compiler");

						if (compiler == null)
						{
							compiler = selectedMember.getCompiledAttribute("compile_kind");

							if (compiler == null)
							{
								compiler = "unknown!";
							}
						}

						String line2 = "Compiled at " + StringUtil.formatTimestamp((long) compiledStampTime, true) + " using "
								+ compiler;

						String compiletime = selectedMember.getCompiledAttribute("compileMillis");

						if (compiletime != null)
						{
							line2 += " took " + compiletime + "ms";
						}
						
						double legendY = y;
						
						if (legendY > GRAPH_GAP_Y + chartHeight - 32)
						{
							legendY = GRAPH_GAP_Y + chartHeight - 32;
						}

						gc.strokeText(line1, smX + 10, legendY);
						gc.strokeText(line2, smX + 10, legendY + 16);

						compiledStampTime = -1;
					}
				}
			}

			showStatsLegend(gc);
		}
	}

	private void showStatsLegend(GraphicsContext gc)
	{
		JITStats stats = parent.getJITStats();

		StringBuilder compiledStatsBuilder = new StringBuilder();
		compiledStatsBuilder.append("Compiled: ").append(stats.getTotalCompiledMethods());
		compiledStatsBuilder.append(" (C1: ").append(stats.getCountC1()).append(")");
		compiledStatsBuilder.append(" (C2: ").append(stats.getCountC2()).append(")");
		compiledStatsBuilder.append(" (C2N: ").append(stats.getCountC2N()).append(")");
		compiledStatsBuilder.append(" (OSR: ").append(stats.getCountOSR()).append(")");
		gc.setStroke(Color.BLACK);
		gc.strokeText(compiledStatsBuilder.toString(), GRAPH_GAP_LEFT, 12);
	}

	private void drawXAxis(GraphicsContext gc, long minStamp, long maxStamp, double chartWidth, double chartHeight)
	{
		long xInc = getXStep(minStamp, maxStamp);

		int gridX = 0;

		while (gridX < maxStamp)
		{
			double x = GRAPH_GAP_LEFT + normalise(gridX, 0, maxStamp, chartWidth, false);
			gc.strokeLine(x, GRAPH_GAP_Y, x, GRAPH_GAP_Y + chartHeight);

			boolean showMillis = gridX > 0 && gridX < 5000;

			gc.strokeText(StringUtil.formatTimestamp(gridX, showMillis), x, GRAPH_GAP_Y + chartHeight + 12);

			gridX += xInc;
		}
	}

	private void drawYAxis(GraphicsContext gc, int maxEvents, double chartWidth, double chartHeight)
	{
		int yInc = getYStep(maxEvents);

		int gridY = 0;

		while (gridY < maxEvents)
		{

			double y = GRAPH_GAP_Y + normalise(gridY, 0, maxEvents, chartHeight, true);
			gc.strokeLine(GRAPH_GAP_LEFT, y, GRAPH_GAP_LEFT + chartWidth, y);
			gc.strokeText(Integer.toString(gridY), 2, y + 2);

			gridY += yInc;
		}
	}

	private long getXStep(long min, long max)
	{
		long rangeMillis = max - min;

		int requiredLines = 6;

		long[] gapMillis = new long[] { 120 * 60000, 60 * 60000, 30 * 60000, 15 * 60000, 10 * 60000, 5 * 60000, 2 * 60000,
				1 * 60000, 30000, 15000, 10000, 5000, 2000, 1000, 500, 200, 100, 50, 20, 10, 5, 2, 1 };

		long incrementMillis = 120 * 60000;

		for (int i = 0; i < gapMillis.length; i++)
		{
			if (rangeMillis / gapMillis[i] >= requiredLines)
			{
				incrementMillis = gapMillis[i];
				break;
			}
		}

		return incrementMillis;
	}

	private int getYStep(int max)
	{
		int requiredLines = 10;

		for (int i = 0; i < Y_SCALE.length; i++)
		{
			if (max / Y_SCALE[i] < requiredLines)
			{
				return Y_SCALE[i];
			}
		}

		return max / requiredLines;
	}

	private double normalise(double value, double min, double max, double size, boolean invert)
	{
		double range = max - min;
		double result = 0;

		if (range == 0)
		{
			result = 0;
		}
		else
		{
			result = (value - min) / range;
		}

		result *= size;

		if (invert)
		{
			result = size - result;
		}

		return result;
	}

}