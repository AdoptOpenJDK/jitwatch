package org.adoptopenjdk.jitwatch.jarscan.visualiser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class HistoPlotter extends Application
{
	private List<XYChart.Data<Number, Number>> values = new ArrayList<>();

	private ScatterChart<Number, Number> chart;

	private int limit;

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

			String inputPath = unnamedParameters.get(0);

			lines = Files.readAllLines(Paths.get(inputPath));
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

		limit = maxSize;
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
				tooltip.setText(data.getYValue().toString() + " instances of value " + data.getXValue().toString() );
				Tooltip.install(data.getNode(), tooltip);
			}
		}
	}

	public HistoPlotter(String[] args)
	{
		if (args.length != 1)
		{
			System.err.println("HistoPlotter <histo CSV>");
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

		BorderPane borderPane = new BorderPane();

		Label label = new Label("Limit:");

		TextField tfLimit = new TextField();

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

		Scene scene = UserInterfaceUtil.getScene(borderPane, 800, 600);

		borderPane.setTop(hBox);
		borderPane.setCenter(buildChart());

		borderPane.setPadding(new Insets(10, 10, 10, 10));

		updateObservable();

		stage.setTitle("Histo Plotter");
		stage.setScene(scene);
		stage.show();
	}

	private ScatterChart<Number, Number> buildChart()
	{
		Axis<Number> xAxis = new NumberAxis();
		xAxis.setLabel("Method Bytecode Size (bytes)");

		Axis<Number> yAxis = new NumberAxis();
		yAxis.setLabel("Frequency");

		chart = new ScatterChart<>(xAxis, yAxis);
		chart.setAnimated(false);
		chart.setLegendVisible(false);

		return chart;
	}

	public static void main(String[] args)
	{
		new HistoPlotter(args);
	}
}