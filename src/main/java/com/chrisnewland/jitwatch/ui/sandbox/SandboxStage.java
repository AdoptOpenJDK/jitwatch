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
import com.chrisnewland.jitwatch.util.DisassemblyUtil;
import com.chrisnewland.jitwatch.util.ExecutionUtil;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class SandboxStage extends Stage
{
	private static final Logger logger = LoggerFactory.getLogger(SandboxStage.class);

	private TextArea taSource;
	private TextArea taLoad;
	private TextArea taLog;

	private VBox colSource;
	private VBox colLoad;

	private ILogParser logParser;
	private IStageAccessProxy accessProxy;
	
	private boolean intelMode = false;

	private static final boolean hsdisAvailable = DisassemblyUtil.isDisassemblerAvailable();

	public SandboxStage(final IStageCloseListener closeListener, IStageAccessProxy proxy, ILogParser parser)
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

		final RadioButton rbATT = new RadioButton("AT&T");
		final RadioButton rbIntel = new RadioButton("Intel");

		final ToggleGroup group = new ToggleGroup();

		rbATT.setToggleGroup(group);
		rbIntel.setToggleGroup(group);

		rbATT.setSelected(!intelMode);
		rbIntel.setSelected(intelMode);

		group.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
		{
			@Override
			public void changed(ObservableValue<? extends Toggle> arg0, Toggle arg1, Toggle arg2)
			{
				if (group.getSelectedToggle() != null)
				{
					intelMode = group.getSelectedToggle().equals(rbIntel);
				
					if (intelMode)
					{
						log("Intel syntax assembly");
					}
					else
					{
						log("AT&T syntax assembly");
					}				
				}
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
		Label lblSyntax = new Label("Assembly syntax:");


		HBox hBoxLoad = new HBox();
		hBoxLoad.setSpacing(10);
		hBoxLoad.setPadding(new Insets(0, 10, 0, 10));
		hBoxLoad.getChildren().add(lblLoad);
		hBoxLoad.getChildren().add(btnRunTestLoad);
		hBoxLoad.getChildren().add(lblSyntax);
		hBoxLoad.getChildren().add(rbATT);
		hBoxLoad.getChildren().add(rbIntel);

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

		String style = "-fx-font-family:monospace; -fx-font-size:12px; -fx-background-color:white;";

		taSource.setStyle(style);
		taLoad.setStyle(style);
		taLog.setStyle(style);

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

		log("Sandbox Ready");
		log("HotSpot disassembler (hsdis) available: " + hsdisAvailable);

		Scene scene = new Scene(splitVertical, JITWatchUI.WINDOW_WIDTH, JITWatchUI.WINDOW_HEIGHT);

		setScene(scene);

		setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				closeListener.handleStageClosed(SandboxStage.this);
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

		sourceBuilder.append("    public int add(int a, int b)\n");
		sourceBuilder.append("    {\n");
		sourceBuilder.append("        return a + b;\n");
		sourceBuilder.append("    }\n");

		sourceBuilder.append("}\n");

		taSource.setText(sourceBuilder.toString());

		StringBuilder loadBuilder = new StringBuilder();

		loadBuilder.append("package com.chrisnewland.jitwatch.sandbox;\n\n");

		loadBuilder.append("public class TestLoad\n");
		loadBuilder.append("{\n");

		loadBuilder.append("    public static void main(String[] args)\n");
		loadBuilder.append("    {\n");
		loadBuilder.append("        Test test = new Test();\n\n");
		loadBuilder.append("        int sum = 0;\n\n");
		loadBuilder.append("        for (int i = 0 ; i < 1_000_000; i++)\n");
		loadBuilder.append("        {\n");
		loadBuilder.append("            sum = test.add(sum, 1);\n");
		loadBuilder.append("        }\n\n");
		loadBuilder.append("        System.out.println(\"Sum:\" + sum);\n");
		loadBuilder.append("    }\n");

		loadBuilder.append("}\n");

		taLoad.setText(loadBuilder.toString());

	}

	private String getPackageFromSource(String source)
	{
		String result = null;

		String[] lines = source.split(S_NEWLINE);

		for (String line : lines)
		{
			line = line.trim();

			if (line.startsWith(S_PACKAGE) && line.endsWith(S_SEMICOLON))
			{
				result = line.substring(S_PACKAGE.length(), line.length() - 1).trim();
			}
		}

		if (result == null)
		{
			result = S_EMPTY;
		}

		return result;
	}

	private String getClassFromSource(String source)
	{
		String result = null;

		String[] lines = source.split(S_NEWLINE);

		String classToken = S_SPACE + S_CLASS + S_SPACE;

		for (String line : lines)
		{
			line = line.trim();

			int classTokenPos = line.indexOf(classToken);

			if (classTokenPos != -1)
			{
				result = line.substring(classTokenPos + classToken.length());
			}
		}

		if (result == null)
		{
			result = "";
		}

		return result;
	}

	//TODO ffs Chris, refactor!
	private void runTestLoad()
	{				
		try
		{
			Platform.runLater(new Runnable()
			{
				@Override
				public void run()
				{
					taLog.setText(S_EMPTY);
				}
			});

			String source = taSource.getText();
			String load = taLoad.getText();

			String sourcePackage = getPackageFromSource(source);
			String loadPackage = getPackageFromSource(load);

			String sourceClass = getClassFromSource(source);
			String loadClass = getClassFromSource(load);

			StringBuilder fqNameSourceBuilder = new StringBuilder();

			if (sourcePackage.length() > 0)
			{
				fqNameSourceBuilder.append(sourcePackage).append(S_DOT);
			}

			fqNameSourceBuilder.append(sourceClass);

			StringBuilder fqNameLoadBuilder = new StringBuilder();

			if (loadPackage.length() > 0)
			{
				fqNameLoadBuilder.append(loadPackage).append(S_DOT);
			}

			fqNameLoadBuilder.append(loadClass);

			String fqNameSource = fqNameSourceBuilder.toString();
			String fqNameLoad = fqNameLoadBuilder.toString();

			log("Writing source file: " + fqNameSource);
			File sourceFile = CompilationUtil.writeSource(fqNameSource, source);

			log("Writing load file: " + fqNameLoad);
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

			if (hsdisAvailable)
			{
				options.add("-XX:+PrintAssembly");
				
				if (intelMode)
				{
					options.add("-XX:PrintAssemblyOptions=intel");
				}
			}

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
			
			logParser.reset();

			logParser.setConfig(config);

			logParser.readLogFile(logFile);

			log("Parsing complete");

			IReadOnlyJITDataModel model = logParser.getModel();

			log("Looking up class: " + fqNameSource);

			MetaClass metaClass = model.getPackageManager().getMetaClass(fqNameSource);

			IMetaMember firstCompiled = null;

			if (metaClass != null)
			{
				log("Found: " + metaClass.getFullyQualifiedName());

				log("looking for compiled members");

				// select first compiled member if any
				List<IMetaMember> memberList = metaClass.getMetaMembers();

				for (IMetaMember mm : memberList)
				{
					if (mm.isCompiled())
					{
						firstCompiled = mm;
						break;
					}
				}
			}

			final IMetaMember member = firstCompiled;

			log("Launching TriView for " + member);

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

	private void log(final String text)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				taLog.appendText(text + S_NEWLINE);
			}
		});
	}
}
