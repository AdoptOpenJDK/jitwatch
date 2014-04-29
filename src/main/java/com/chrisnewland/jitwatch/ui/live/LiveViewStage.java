/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui.live;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class LiveViewStage extends Stage
{
	private static final Logger logger = LoggerFactory.getLogger(LiveViewStage.class);

	private SplitPane splitViewer;

	private TextArea taSource;
	private TextArea taLoad;

	private VBox colSource;
	private VBox colLoad;
	
	private ILogParser logParser;
	private IStageAccessProxy accessProxy;

	public LiveViewStage(final IStageCloseListener closeListener, IStageAccessProxy proxy, ILogParser parser)
	{
		this.logParser = parser;
		this.accessProxy = proxy;
		
		setTitle("LiveView JIT Sandbox");

		VBox vBox = new VBox();

		HBox hBoxToolBarButtons = new HBox();
		hBoxToolBarButtons.setSpacing(10);
		hBoxToolBarButtons.setPadding(new Insets(0, 10, 10, 10));

		Button btnCallChain = new Button("Go!");
		btnCallChain.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				runTestLoad();
			}
		});

		hBoxToolBarButtons.getChildren().add(btnCallChain);

		splitViewer = new SplitPane();
		splitViewer.setOrientation(Orientation.HORIZONTAL);

		colSource = new VBox();
		colLoad = new VBox();

		Label lblSource = new Label("Source");
		Label lblLoad = new Label("Test Load");

		lblSource.setStyle("-fx-background-color:#dddddd; -fx-padding:4px;");
		lblLoad.setStyle("-fx-background-color:#dddddd; -fx-padding:4px;");

		lblSource.prefWidthProperty().bind(colSource.widthProperty());
		lblLoad.prefWidthProperty().bind(colLoad.widthProperty());

		taSource = new TextArea();
		taLoad = new TextArea();

		colSource.getChildren().add(lblSource);
		colSource.getChildren().add(taSource);

		colLoad.getChildren().add(lblLoad);
		colLoad.getChildren().add(taLoad);

		splitViewer.prefHeightProperty().bind(vBox.heightProperty());

		taSource.prefWidthProperty().bind(colSource.widthProperty());
		taSource.prefHeightProperty().bind(colSource.heightProperty());

		taLoad.prefWidthProperty().bind(colLoad.widthProperty());
		taLoad.prefHeightProperty().bind(colLoad.heightProperty());

		splitViewer.getItems().add(colSource);
		splitViewer.getItems().add(colLoad);

		// horizontal VM switches!
		// horizontal status messages

		vBox.getChildren().add(hBoxToolBarButtons);
		vBox.getChildren().add(splitViewer);

		Scene scene = new Scene(vBox, JITWatchUI.WINDOW_WIDTH, JITWatchUI.WINDOW_HEIGHT);

		setScene(scene);

		setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				closeListener.handleStageClosed(LiveViewStage.this);
			}
		});

		setup();
	}

	private void setup()
	{
		StringBuilder sourceBuilder = new StringBuilder();

		sourceBuilder.append("package com.chrisnewland.jitwatch.liveview;\n\n");

		sourceBuilder.append("public class Test\n");
		sourceBuilder.append("{\n");

		sourceBuilder.append("\tpublic int add(int a, int b)\n");
		sourceBuilder.append("\t{\n");
		sourceBuilder.append("\t\treturn a + b;\n");
		sourceBuilder.append("\t}\n");

		sourceBuilder.append("}\n");

		taSource.setText(sourceBuilder.toString());

		StringBuilder loadBuilder = new StringBuilder();

		loadBuilder.append("package com.chrisnewland.jitwatch.liveview;\n\n");

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
			logger.info("Writing source file");
			String source = taSource.getText();
			File sourceFile = CompilationUtil.writeSource("com.chrisnewland.jitwatch.liveview.Test", source);

			logger.info("Writing load file");
			String load = taLoad.getText();
			File loadFile = CompilationUtil.writeSource("com.chrisnewland.jitwatch.liveview.TestLoad", load);

			List<File> toCompile = new ArrayList<>();
			toCompile.add(sourceFile);
			toCompile.add(loadFile);

			logger.info("Compiling");
			CompilationUtil.compile(toCompile);
			
			List<String> cp = new ArrayList<>();

			cp.add(CompilationUtil.SANDBOX_CLASS_DIR.toString());

			List<String> options = new ArrayList<>();
			options.add("-XX:+UnlockDiagnosticVMOptions");
			options.add("-XX:+TraceClassLoading");
			options.add("-XX:+LogCompilation");
			options.add("-XX:LogFile=live.log");
			options.add("-XX:+PrintAssembly");

			logger.info("Executing");
			boolean success = ExecutionUtil.execute("com.chrisnewland.jitwatch.liveview.TestLoad", cp, options);

			logger.info("Success: {}", success);
			
			File logFile = new File("live.log");
			
			List<String> sourceLocations = new ArrayList<>();
			List<String> classLocations = new ArrayList<>();
			
			sourceLocations.add(CompilationUtil.SANDBOX_SOURCE_DIR.toString());
			classLocations.add(CompilationUtil.SANDBOX_CLASS_DIR.toString());

			JITWatchConfig config = new JITWatchConfig();
			config.setSourceLocations(sourceLocations);
			config.setClassLocations(classLocations);
			
			// TODO: unload classes?
			
			logParser.reset();
			
			logParser.setConfig(config);
			
			logParser.readLogFile(logFile);
			
			IReadOnlyJITDataModel model = logParser.getModel();
			
			MetaClass metaClass = model.getPackageManager().getMetaClass("com.chrisnewland.jitwatch.liveview.Test");
			
			IMetaMember member = metaClass.getMemberFromSignature("add", "int",  new String[]{"int", "int"});
			
			accessProxy.openTriView(member, true);

		}
		catch (IOException ioe)
		{
			logger.error("Compile failure", ioe);
		}
	}
}
