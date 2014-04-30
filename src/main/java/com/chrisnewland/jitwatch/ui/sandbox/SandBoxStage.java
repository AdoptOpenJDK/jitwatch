/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

import com.chrisnewland.jitwatch.core.ILogParser;
import com.chrisnewland.jitwatch.core.JITWatchConfig;
import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.IReadOnlyJITDataModel;
import com.chrisnewland.jitwatch.model.MetaClass;
import com.chrisnewland.jitwatch.ui.IStageAccessProxy;
import com.chrisnewland.jitwatch.ui.IStageCloseListener;
import com.chrisnewland.jitwatch.ui.JITWatchUI;
import com.chrisnewland.jitwatch.util.CompilationUtil;
import com.chrisnewland.jitwatch.util.ExecutionUtil;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class SandBoxStage extends Stage
{
	private static final Logger logger = LoggerFactory.getLogger(SandBoxStage.class);

	private TextArea taSource;
	private TextArea taLoad;
	private TextArea taLog;

	private VBox colSource;
	private VBox colLoad;

	private ILogParser logParser;
	private IStageAccessProxy accessProxy;

	private StringBuilder logBuilder = new StringBuilder();

	public SandBoxStage(final IStageCloseListener closeListener, IStageAccessProxy proxy, ILogParser parser)
	{
		this.logParser = parser;
		this.accessProxy = proxy;

		setTitle("JIT Sandbox");

		Button btnRunTestLoad = new Button("Run");
		btnRunTestLoad.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						runTestLoad();
					}
				}).start();
			}
		});

		SplitPane splitHorizontal = new SplitPane();
		splitHorizontal.setOrientation(Orientation.HORIZONTAL);

		SplitPane splitVertical = new SplitPane();
		splitVertical.setOrientation(Orientation.VERTICAL);

		colSource = new VBox();
		colLoad = new VBox();

		Label lblSource = new Label("Source");
		Label lblLoad = new Label("Test Load");

		HBox hBoxLoad = new HBox();
		hBoxLoad.setSpacing(10);
		hBoxLoad.setPadding(new Insets(0, 10, 0, 10));
		hBoxLoad.getChildren().add(lblLoad);
		hBoxLoad.getChildren().add(btnRunTestLoad);

		HBox hBoxSource = new HBox();
		hBoxSource.setSpacing(10);
		hBoxSource.setPadding(new Insets(0, 10, 0, 10));
		hBoxSource.getChildren().add(lblSource);

		hBoxSource.setStyle("-fx-background-color:#dddddd; -fx-padding:4px;");
		hBoxLoad.setStyle("-fx-background-color:#dddddd; -fx-padding:4px;");

		hBoxSource.prefWidthProperty().bind(colSource.widthProperty());
		hBoxLoad.prefWidthProperty().bind(colLoad.widthProperty());

		double headerFraction = 0.1;

		hBoxSource.prefHeightProperty().bind(colSource.heightProperty().multiply(headerFraction));
		hBoxLoad.prefHeightProperty().bind(colLoad.heightProperty().multiply(headerFraction));

		taSource = new TextArea();
		taLoad = new TextArea();
		taLog = new TextArea();

		colSource.getChildren().add(hBoxSource);
		colSource.getChildren().add(taSource);

		colLoad.getChildren().add(hBoxLoad);
		colLoad.getChildren().add(taLoad);

		taSource.prefWidthProperty().bind(colSource.widthProperty());
		taSource.prefHeightProperty().bind(colSource.heightProperty().multiply(1 - headerFraction));

		taLoad.prefWidthProperty().bind(colLoad.widthProperty());
		taLoad.prefHeightProperty().bind(colLoad.heightProperty().multiply(1 - headerFraction));

		splitHorizontal.getItems().add(colSource);
		splitHorizontal.getItems().add(colLoad);

		splitVertical.getItems().add(splitHorizontal);
		splitVertical.getItems().add(taLog);

		splitVertical.setDividerPositions(0.7, 0.3);

		log("Ready");

		Scene scene = new Scene(splitVertical, JITWatchUI.WINDOW_WIDTH, JITWatchUI.WINDOW_HEIGHT);

		setScene(scene);

		setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				closeListener.handleStageClosed(SandBoxStage.this);
			}
		});

		setup();
	}

	private void setup()
	{
		StringBuilder sourceBuilder = new StringBuilder();

		sourceBuilder.append("package com.chrisnewland.jitwatch.sandbox;\n\n");

		sourceBuilder.append("public class Test\n");
		sourceBuilder.append("{\n");

		sourceBuilder.append("\tpublic int add(int a, int b)\n");
		sourceBuilder.append("\t{\n");
		sourceBuilder.append("\t\treturn a + b;\n");
		sourceBuilder.append("\t}\n");

		sourceBuilder.append("}\n");

		taSource.setText(sourceBuilder.toString());

		StringBuilder loadBuilder = new StringBuilder();

		loadBuilder.append("package com.chrisnewland.jitwatch.sandbox;\n\n");

		loadBuilder.append("public class TestLoad\n");
		loadBuilder.append("{\n");

		loadBuilder.append("\tpublic static void main(String[] args)\n");
		loadBuilder.append("\t{\n");
		loadBuilder.append("\t\tTest test = new Test();\n\n");
		loadBuilder.append("\t\tint sum = 0;\n\n");
		loadBuilder.append("\t\tfor (int i = 0 ; i < 1_000_000; i++)\n");
		loadBuilder.append("\t\t{\n");
		loadBuilder.append("\t\t\tsum = test.add(sum, 1);\n");
		loadBuilder.append("\t\t}\n\n");
		loadBuilder.append("\t\tSystem.out.println(\"Sum:\" + sum);\n");
		loadBuilder.append("\t}\n");

		loadBuilder.append("}\n");

		taLoad.setText(loadBuilder.toString());

	}

	private void runTestLoad()
	{
		try
		{
			String fqNameSource = "com.chrisnewland.jitwatch.sandbox.Test";
			String fqNameLoad = "com.chrisnewland.jitwatch.sandbox.TestLoad";

			log("Writing source file: " + fqNameSource);
			String source = taSource.getText();
			File sourceFile = CompilationUtil.writeSource(fqNameSource, source);

			log("Writing load file: " + fqNameLoad);
			String load = taLoad.getText();
			File loadFile = CompilationUtil.writeSource(fqNameLoad, load);

			List<File> toCompile = new ArrayList<>();
			toCompile.add(sourceFile);
			toCompile.add(loadFile);
			
			log("Compiling: " + listToString(toCompile));
			CompilationUtil.compile(toCompile);

			List<String> cp = new ArrayList<>();

			cp.add(CompilationUtil.SANDBOX_CLASS_DIR.toString());

			List<String> options = new ArrayList<>();
			options.add("-XX:+UnlockDiagnosticVMOptions");
			options.add("-XX:+TraceClassLoading");
			options.add("-XX:+LogCompilation");
			options.add("-XX:LogFile=live.log");
			options.add("-XX:+PrintAssembly");

			log("Executing: " + fqNameLoad);
			log("VM options: " + listToString(options));
			
			boolean success = ExecutionUtil.execute(fqNameLoad, cp, options);

			log("Success: " + success);

			File logFile = new File("live.log");

			List<String> sourceLocations = new ArrayList<>();
			List<String> classLocations = new ArrayList<>();

			sourceLocations.add(CompilationUtil.SANDBOX_SOURCE_DIR.toString());
			classLocations.add(CompilationUtil.SANDBOX_CLASS_DIR.toString());

			JITWatchConfig config = new JITWatchConfig();
			config.setSourceLocations(sourceLocations);
			config.setClassLocations(classLocations);

			// TODO: unload classes? CUSTOM CLASSLOADER

			logParser.reset();

			logParser.setConfig(config);

			logParser.readLogFile(logFile);

			IReadOnlyJITDataModel model = logParser.getModel();

			MetaClass metaClass = model.getPackageManager().getMetaClass(fqNameSource);

			final IMetaMember member = metaClass.getMemberFromSignature("add", "int", new String[] { "int", "int" });

			log("Launching TriView");
			
			Platform.runLater(new Runnable()
			{
				@Override
				public void run()
				{
					accessProxy.openTriView(member, true);
				}
			});

		}
		catch (IOException ioe)
		{
			logger.error("Compile failure", ioe);
		}
	}
	
	private String listToString(List<?> list)
	{
		StringBuilder builder = new StringBuilder();
		
		for (Object item : list)
		{
			builder.append(item.toString()).append(C_SPACE);
		}
		
		return builder.toString().trim();
	}

	private void log(String text)
	{
		logBuilder.append(text).append(C_NEWLINE);

		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				taLog.setText(logBuilder.toString());
			}
		});
	}
}
