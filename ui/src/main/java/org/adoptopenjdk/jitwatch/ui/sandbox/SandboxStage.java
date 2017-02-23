/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.sandbox;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_ASTERISK;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.VM_LANGUAGE_CLOJURE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.VM_LANGUAGE_GROOVY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.VM_LANGUAGE_JAVA;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.VM_LANGUAGE_JAVASCRIPT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.VM_LANGUAGE_JRUBY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.VM_LANGUAGE_KOTLIN;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.VM_LANGUAGE_SCALA;
import static org.adoptopenjdk.jitwatch.util.UserInterfaceUtil.FONT_MONOSPACE_FAMILY;
import static org.adoptopenjdk.jitwatch.util.UserInterfaceUtil.FONT_MONOSPACE_SIZE;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.adoptopenjdk.jitwatch.core.ILogParseErrorListener;
import org.adoptopenjdk.jitwatch.core.ILogParser;
import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.core.JITWatchConstants;
import org.adoptopenjdk.jitwatch.jvmlang.LanguageManager;
import org.adoptopenjdk.jitwatch.logger.ILogListener;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.process.IExternalProcess;
import org.adoptopenjdk.jitwatch.sandbox.Sandbox;
import org.adoptopenjdk.jitwatch.ui.Dialogs;
import org.adoptopenjdk.jitwatch.ui.Dialogs.Response;
import org.adoptopenjdk.jitwatch.ui.main.IStageAccessProxy;
import org.adoptopenjdk.jitwatch.ui.main.JITWatchUI;
import org.adoptopenjdk.jitwatch.ui.stage.IStageClosedListener;
import org.adoptopenjdk.jitwatch.ui.stage.StageManager;
import org.adoptopenjdk.jitwatch.util.DisassemblyUtil;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SandboxStage extends Stage implements ISandboxStage, IStageClosedListener, ILogListener, ILogParseErrorListener
{
	private static final Logger logger = LoggerFactory.getLogger(SandboxStage.class);

	private TextArea taLog;

	private IStageAccessProxy accessProxy;

	private Sandbox sandbox;

	private TabPane tabPane;

	private Button btnSandboxConfig;
	
	private Button btnRun;

	private SandboxConfigStage sandboxConfigStage;

	private ObservableList<String> languageList = FXCollections.observableArrayList();

	private ComboBox<String> comboBoxVMLanguage = new ComboBox<>(languageList);

	private JITWatchConfig config;

	public SandboxStage(final IStageClosedListener closeListener, IStageAccessProxy proxy, final ILogParser parser)
	{
		this.accessProxy = proxy;

		config = parser.getConfig();

		config.switchToSandbox();

		setupVMLanguages();

		sandbox = new Sandbox(parser, this, this);

		setTitle("Sandbox - Code, Compile, Execute, and Analyse JIT logs");

		tabPane = new TabPane();

		tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>()
		{
			@Override
			public void changed(ObservableValue<? extends Tab> ov, Tab t, Tab t1)
			{
				Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();

				if (selectedTab != null)
				{
					EditorPane pane = (EditorPane) selectedTab.getContent();

					setVMLanguage(pane);
				}
			}
		});

		SplitPane splitVertical = new SplitPane();
		splitVertical.setOrientation(Orientation.VERTICAL);

		taLog = new TextArea();

		String style = "-fx-font-family:" + FONT_MONOSPACE_FAMILY + "; -fx-font-size:" + FONT_MONOSPACE_SIZE
				+ "px; -fx-background-color:white;";

		taLog.setStyle(style);

		Button btnNewEditor = new Button("New Editor");
		btnNewEditor.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				addEditor(null);
			}
		});

		Button btnOpen = new Button("Open");
		btnOpen.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				if (tabPane.getSelectionModel().getSelectedItem() == null)
				{
					addEditor(null);
				}

				Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();

				EditorPane pane = (EditorPane) selectedTab.getContent();

				if (pane.isModified())
				{
					pane.promptSave();
				}

				FileChooser fc = new FileChooser();

				fc.setTitle("Choose source file");

				fc.setInitialDirectory(Sandbox.SANDBOX_SOURCE_DIR.toFile());

				File result = fc.showOpenDialog(getStageForChooser());

				if (result != null)
				{
					pane.loadSource(result);
					selectedTab.setText(pane.getName());

					setVMLanguage(pane);

					saveEditorPaneConfig();
				}
			}
		});

		Button btnSave = new Button("Save");
		btnSave.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();

				if (selectedTab != null)
				{
					EditorPane pane = (EditorPane) selectedTab.getContent();

					pane.saveFile();

					selectedTab.setText(pane.getName());

					setVMLanguage(pane);
				}
			}
		});

		btnSandboxConfig = new Button("Configure Sandbox");
		btnSandboxConfig.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				sandboxConfigStage = new SandboxConfigStage(SandboxStage.this, parser.getConfig());

				StageManager.addAndShow(SandboxStage.this, sandboxConfigStage);

				btnSandboxConfig.setDisable(true);
			}
		});

		Button btnResetSandbox = new Button("Reset Sandbox");
		btnResetSandbox.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				Response resp = Dialogs.showYesNoDialog(SandboxStage.this, "Reset Sandbox?",
						"Delete all modified Sandbox sources and classes?");

				if (resp == Response.YES)
				{
					initialiseLog();
					sandbox.reset();
					loadDefaultEditors();
				}
			}
		});

		comboBoxVMLanguage.valueProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal)
			{
				if (newVal != null)
				{
					log("Changed language to " + newVal);
				}
			}
		});

		btnRun = new Button("Run");
		btnRun.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				btnRun.setDisable(true);
				
				Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();

				if (selectedTab != null)
				{
					EditorPane pane = (EditorPane) selectedTab.getContent();

					if (pane.isModified())
					{
						pane.promptSave();
					}

					setVMLanguage(pane);

					runFile(pane);
				}
			}
		});

		Button btnOutput = new Button("View Output");
		btnOutput.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				IExternalProcess lastProcess = sandbox.getLastProcess();
				
				String outputString;
				
				if (lastProcess != null)
				{
					outputString = lastProcess.getOutputStream();
				}
				else
				{
					outputString = "No output";
				}
				
				showOutput(outputString);
			}
		});

		HBox hBoxTools = new HBox();

		hBoxTools.setSpacing(10);
		hBoxTools.setPadding(new Insets(10));

		hBoxTools.getChildren().add(btnNewEditor);
		hBoxTools.getChildren().add(btnOpen);
		hBoxTools.getChildren().add(btnSave);
		hBoxTools.getChildren().add(btnSandboxConfig);
		hBoxTools.getChildren().add(btnResetSandbox);
		hBoxTools.getChildren().add(comboBoxVMLanguage);
		hBoxTools.getChildren().add(btnRun);
		hBoxTools.getChildren().add(btnOutput);

		splitVertical.getItems().add(tabPane);
		splitVertical.getItems().add(taLog);

		splitVertical.setDividerPositions(0.75, 0.25);

		VBox vBoxMain = new VBox();
		vBoxMain.getChildren().add(hBoxTools);
		vBoxMain.getChildren().add(splitVertical);

		BorderPane borderPane = new BorderPane();
		borderPane.setTop(hBoxTools);
		borderPane.setCenter(splitVertical);

		initialiseLog();

		Scene scene = UserInterfaceUtil.getScene(borderPane, JITWatchUI.WINDOW_WIDTH, JITWatchUI.WINDOW_HEIGHT);

		setScene(scene);

		setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				saveEditorPaneConfig();

				closeListener.handleStageClosed(SandboxStage.this);
			}
		});

		loadLastEditorPanes();
	}

	@Override
	public void setModified(EditorPane pane, boolean isModified)
	{
		for (Tab tab : tabPane.getTabs())
		{
			EditorPane currentPane = (EditorPane) tab.getContent();

			if (currentPane == pane)
			{
				String tabText = tab.getText();

				if (isModified)
				{
					if (!tabText.endsWith(S_ASTERISK))
					{
						tab.setText(tabText + S_ASTERISK);
					}
				}
				else
				{
					tab.setText(pane.getName());
				}
			}
		}
	}

	private void setVMLanguage(EditorPane pane)
	{
		if (pane != null)
		{
			setVMLanguageFromFile(pane.getSourceFile());
		}
	}

	@Override
	public void runFile(final EditorPane pane)
	{
		saveUnsavedEditors();

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				runSandbox(pane.getSourceFile());
				btnRun.setDisable(false);
			}
		}).start();
	}

	private void initialiseLog()
	{
		taLog.setText(S_EMPTY);
		log("Sandbox ready");
		log("HotSpot disassembler (hsdis) available: " + DisassemblyUtil.isDisassemblerAvailable());
	}

	private void loadLastEditorPanes()
	{
		List<String> panes = config.getLastEditorPaneList();

		if (panes.size() == 0)
		{
			loadDefaultEditors();
		}
		else
		{
			tabPane.getTabs().clear();

			for (String panePath : panes)
			{
				addEditor(new File(panePath));
			}
		}
	}

	private void loadDefaultEditors()
	{
		tabPane.getTabs().clear();

		addEditor(new File(Sandbox.SANDBOX_SOURCE_DIR.toFile(), "SimpleInliningTest.java"));

		saveEditorPaneConfig();
	}

	private void addEditor(File filename)
	{
		final EditorPane pane = new EditorPane(this);

		if (filename != null)
		{
			pane.loadSource(filename);
		}

		final Tab tab = new Tab();
		tab.setContent(pane);
		tab.setText(pane.getName());

		EventHandler<Event> closeHandler = new EventHandler<Event>()
		{
			@Override
			public void handle(Event e)
			{
				if (pane.isModified())
				{
					pane.promptSave();
				}

				tabPane.getTabs().remove(tab);
			}
		};

		// JavaFX 2.2 (from Java 7) has no onCloseRequestProperty
		if (JITWatchUI.IS_JAVA_FX2)
		{
			tab.setOnClosed(closeHandler);
		}
		else
		{
			// Use reflection to call setOnCloseRequestProperty for Java 8
			try
			{
				MethodType mt = MethodType.methodType(void.class, EventHandler.class);

				MethodHandle mh = MethodHandles.lookup().findVirtual(Tab.class, "setOnCloseRequest", mt);

				// fails with invokeExact due to generic type erasure?
				mh.invoke(tab, closeHandler);

			}
			catch (Throwable t)
			{
				logger.error("Exception: {}", t.getMessage(), t);
			}
		}

		tabPane.getTabs().add(tab);

		pane.requestFocus();

		setVMLanguage(pane);
		saveEditorPaneConfig();
	}

	private void saveEditorPaneConfig()
	{
		List<String> editorPanePaths = new ArrayList<>();

		for (Tab tab : tabPane.getTabs())
		{
			EditorPane pane = (EditorPane) tab.getContent();

			if (pane != null && pane.getSourceFile() != null)
			{
				String editorPanePath = pane.getSourceFile().getAbsolutePath();
				editorPanePaths.add(editorPanePath);
			}
		}

		config.setLastEditorPaneList(editorPanePaths);
		config.saveConfig();
	}

	private void saveUnsavedEditors()
	{
		for (Tab tab : tabPane.getTabs())
		{
			EditorPane pane = (EditorPane) tab.getContent();

			pane.promptSave();
		}
	}

	private void runSandbox(File fileToRun)
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

			String language = comboBoxVMLanguage.getValue();

			if (language != null)
			{
				List<File> compileList = new ArrayList<>();

				for (Tab tab : tabPane.getTabs())
				{
					EditorPane pane = (EditorPane) tab.getContent();

					File sourceFile = pane.getSourceFile();

					if (sourceFile != null)
					{
						if (LanguageManager.isCompilable(language, sourceFile))
						{
							compileList.add(sourceFile);
						}
					}
				}

				if (compileList.size() > 0)
				{
					sandbox.runSandbox(language, compileList, fileToRun);
				}
				else
				{
					log("Nothing to compile?");
				}
			}
		}
		catch (Exception e)
		{
			logger.error("Sandbox failure", e);
		}
	}

	@Override
	public void addSourceFolder(File sourceFolder)
	{
		config.addSourceFolder(sourceFolder);
	}

	private void setVMLanguageFromFile(File sourceFile)
	{
		String language = LanguageManager.getLanguageFromFile(sourceFile);

		if (language != null)
		{
			comboBoxVMLanguage.getSelectionModel().select(language);
		}
	}

	@Override
	public void handleLogEntry(String text)
	{
		log(text);
	}
	
	@Override
	public void handleErrorEntry(String text)
	{
		log(text);
	}
	
	@Override
	public void log(final String text)
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

	@Override
	public void openTriView(final IMetaMember member)
	{
		log("Launching TriView for " + member);

		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				accessProxy.openTriView(member, true, 0);
			}
		});
	}

	@Override
	public void showOutput(final String output)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				// TODO perhaps filter out classloading statements?
				accessProxy.openTextViewer("Sandbox Output", output, false, false);
			}
		});
	}

	@Override
	public void showError(final String error)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				accessProxy.openTextViewer("Error", error, false, false);
			}
		});
	}

	@Override
	public void handleStageClosed(Stage stage)
	{
		StageManager.closeStage(stage);

		if (stage instanceof SandboxConfigStage)
		{
			btnSandboxConfig.setDisable(false);
		}
	}

	private void setupVMLanguages()
	{
		List<String> vmLanguageList = config.getVMLanguageList();

		addVMLanguages(config);

		vmLanguageList = config.getVMLanguageList();

		Collections.sort(vmLanguageList);

		languageList.addAll(vmLanguageList);

		comboBoxVMLanguage.getSelectionModel().select(VM_LANGUAGE_JAVA);
	}

	private void addVMLanguages(JITWatchConfig config)
	{
		List<String> vmLanguageList = config.getVMLanguageList();

		if (!vmLanguageList.contains(JITWatchConstants.VM_LANGUAGE_JAVA))
		{
			config.addOrUpdateVMLanguage(VM_LANGUAGE_JAVA, System.getProperty("java.home"));
		}
		if (!vmLanguageList.contains(JITWatchConstants.VM_LANGUAGE_SCALA))
		{
			config.addOrUpdateVMLanguage(VM_LANGUAGE_SCALA, S_EMPTY);
		}
		if (!vmLanguageList.contains(JITWatchConstants.VM_LANGUAGE_JRUBY))
		{
			config.addOrUpdateVMLanguage(VM_LANGUAGE_JRUBY, S_EMPTY);
		}
		if (!vmLanguageList.contains(JITWatchConstants.VM_LANGUAGE_GROOVY))
		{
			config.addOrUpdateVMLanguage(VM_LANGUAGE_GROOVY, S_EMPTY);
		}
		if (!vmLanguageList.contains(JITWatchConstants.VM_LANGUAGE_KOTLIN))
		{
			config.addOrUpdateVMLanguage(VM_LANGUAGE_KOTLIN, S_EMPTY);
		}
		if (!vmLanguageList.contains(JITWatchConstants.VM_LANGUAGE_JAVASCRIPT))
		{
			config.addOrUpdateVMLanguage(VM_LANGUAGE_JAVASCRIPT, System.getProperty("java.home"));
		}
		if (!vmLanguageList.contains(JITWatchConstants.VM_LANGUAGE_CLOJURE))
		{
			config.addOrUpdateVMLanguage(VM_LANGUAGE_CLOJURE, S_EMPTY);
		}

		config.saveConfig();
	}

	@Override
	public void handleError(final String title, final String body)
	{
		logger.error(title);

		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				Dialogs.showOKDialog(SandboxStage.this, title, body);
			}
		});
	}

	@Override
	public Stage getStageForChooser()
	{
		return this;
	}
}
