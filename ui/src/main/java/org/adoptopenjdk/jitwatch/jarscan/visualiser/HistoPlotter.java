/*
 * Copyright (c) 2016-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.jarscan.visualiser;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class HistoPlotter extends Application
{
	private List<XYChart.Data<Number, Number>> values = new ArrayList<>();

	private Scene scene;

	private ScatterChart<Number, Number> chart;

	private int limit = -1;

	private static final String PARAM_SCREENSHOT = "screenshot";

	private static final String PARAM_LIMIT = "limit";

	private String screenshotFilename;

	private String filename;

	public HistoPlotter()
	{
	}

	@Override
	public void init() throws Exception
	{
		super.init();

		List<String> lines = new ArrayList<>();

		try
		{
			Parameters parameters = getParameters();

			List<String> unnamedParameters = parameters.getUnnamed();

			filename = unnamedParameters.get(0);

			lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);

			Map<String, String> namedParameters = parameters.getNamed();

			if (namedParameters.containsKey(PARAM_SCREENSHOT))
			{
				screenshotFilename = namedParameters.get(PARAM_SCREENSHOT);
			}

			if (namedParameters.containsKey(PARAM_LIMIT))
			{
				limit = Integer.parseInt(namedParameters.get(PARAM_LIMIT));
			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		int maxSize = 0;

		for (String line : lines)
		{
			String[] parts = line.split(",");

			if (parts.length == 2)
			{
				Number xValue = Integer.parseInt(parts[0]);
				Number yValue = Integer.parseInt(parts[1]);

				maxSize = Math.max(maxSize, xValue.intValue());

				XYChart.Data<Number, Number> point = new XYChart.Data<Number, Number>(xValue, yValue);

				values.add(point);
			}
		}

		if (limit == -1)
		{
			limit = maxSize;
		}
	}

	private void updateObservable()
	{
		chart.getData().clear();

		ObservableList<XYChart.Data<Number, Number>> observableValues = FXCollections.observableArrayList();

		for (XYChart.Data<Number, Number> value : values)
		{
			if (value.getXValue().intValue() <= limit)
			{
				observableValues.add(value);
			}
		}

		XYChart.Series<Number, Number> series = new XYChart.Series<>(observableValues);

		series.setName("Method Sizes");

		chart.getData().add(series);

		for (Series<Number, Number> chartSeries : chart.getData())
		{
			for (final XYChart.Data<Number, Number> data : chartSeries.getData())
			{
				Tooltip tooltip = new Tooltip();
				tooltip.setText(data.getYValue().toString() + " instances of value " + data.getXValue().toString());
				Tooltip.install(data.getNode(), tooltip);
			}
		}

		chart.setTitle(getTitle());
	}

	private void doScreenshot()
	{
		if (screenshotFilename != null)
		{
			WritableImage imageSnap = new WritableImage((int) chart.getWidth(), (int) chart.getHeight());

			chart.snapshot(new SnapshotParameters(), imageSnap);

			try
			{

				Class<?> swingFXUtils = Class.forName("javafx.embed.swing.SwingFXUtils");

				Method methodFromFXImage = swingFXUtils.getMethod("fromFXImage", new Class[] {
						javafx.scene.image.Image.class,
						java.awt.image.BufferedImage.class });

				ImageIO.write((RenderedImage) methodFromFXImage.invoke(null, new Object[] {
						imageSnap,
						null }), "png", new File(screenshotFilename));
				Platform.exit();
			}
			catch (Throwable t)
			{
				System.err.println("Couldn't write screenshot");
				t.printStackTrace();
			}
		}
	}

	public HistoPlotter(String[] args)
	{
		if (args.length < 1)
		{
			System.err
					.println("HistoPlotter <histo CSV> [--" + PARAM_LIMIT + "=limit] [--" + PARAM_SCREENSHOT + "=outputFilename]");
			System.exit(-1);
		}

		launch(args);
	}

	@Override
	public void start(final Stage stage)
	{
		stage.setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				stage.close();
			}
		});

		stage.addEventHandler(WindowEvent.WINDOW_SHOWN, new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent window)
			{
				doScreenshot();
			}
		});

		BorderPane borderPane = new BorderPane();

		Label label = new Label("Limit:");

		final TextField tfLimit = new TextField();

		tfLimit.setText(Integer.toString(limit));

		tfLimit.setOnKeyReleased(new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent keyEvent)
			{
				try
				{
					limit = Integer.parseInt(tfLimit.getText());
					updateObservable();
				}
				catch (NumberFormatException nfe)
				{
				}
			}
		});

		HBox hBox = new HBox();
		hBox.getChildren().addAll(label, tfLimit);
		hBox.setSpacing(10);

		scene = UserInterfaceUtil.getScene(borderPane, 800, 480);

		borderPane.setTop(hBox);
		borderPane.setCenter(buildChart());

		updateObservable();

		stage.setTitle("JarScan Histo Plotter by @chriswhocodes");
		stage.setScene(scene);
		stage.show();
	}

	private String getTitle()
	{
		String chartTitle = null;

		int lastSlash = filename.lastIndexOf(File.separator);

		if (lastSlash != -1)
		{
			chartTitle = filename.substring(lastSlash + 1);
		}
		else
		{
			chartTitle = filename;
		}

		return "Input file: " + chartTitle + "    limit: " + limit;
	}

	private ScatterChart<Number, Number> buildChart()
	{
		Axis<Number> xAxis = new NumberAxis();
		xAxis.setLabel("Method bytecode size (bytes)");

		Axis<Number> yAxis = new NumberAxis();
		yAxis.setLabel("Frequency");

		chart = new ScatterChart<>(xAxis, yAxis);
		chart.setAnimated(false);
		chart.setLegendVisible(false);
		chart.setPadding(new Insets(10, 20, 10, 10));
		chart.setTitle(getTitle());

		return chart;
	}

	public static void main(String[] args)
	{
		new HistoPlotter(args);
	}
}