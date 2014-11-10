/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.sandbox;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.adoptopenjdk.jitwatch.core.ILogParseErrorListener;
import org.adoptopenjdk.jitwatch.core.ILogParser;
import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.core.JITWatchConstants;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.sandbox.ISandboxLogListener;
import org.adoptopenjdk.jitwatch.sandbox.Sandbox;
import org.adoptopenjdk.jitwatch.ui.Dialogs;
import org.adoptopenjdk.jitwatch.ui.IStageAccessProxy;
import org.adoptopenjdk.jitwatch.ui.IStageCloseListener;
import org.adoptopenjdk.jitwatch.ui.JITWatchUI;
import org.adoptopenjdk.jitwatch.ui.StageManager;
import org.adoptopenjdk.jitwatch.ui.Dialogs.Response;
import org.adoptopenjdk.jitwatch.util.DisassemblyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class SandboxStage extends Stage implements ISandboxStage, IStageCloseListener, ISandboxLogListener, ILogParseErrorListener
{
	private static final Logger logger = LoggerFactory.getLogger(SandboxStage.class);

	private List<EditorPane> editorPanes = new ArrayList<>();

	private TextArea taLog;

	private IStageAccessProxy accessProxy;

	private Sandbox sandbox;

	private SplitPane splitEditorPanes;

	private Button btnSandboxConfig;

	private SandboxConfigStage sandboxConfigStage;

	private ObservableList<String> languageList = FXCollections.observableArrayList();

	private ComboBox<String> comboBoxVMLanguage = new ComboBox<>(languageList);

	private JITWatchConfig config;

	public SandboxStage(final IStageCloseListener closeListener, IStageAccessProxy proxy, final ILogParser parser)
	{
		this.accessProxy = proxy;

		config = parser.getConfig();

		config.switchToSandbox();

		setupVMLanguages();

		sandbox = new Sandbox(parser, this, this);

		setTitle("JITWatch Sandbox");

		splitEditorPanes = new SplitPane();
		splitEditorPanes.setOrientation(Orientation.HORIZONTAL);

		SplitPane splitVertical = new SplitPane();
		splitVertical.setOrientation(Orientation.VERTICAL);

		taLog = new TextArea();

		String style = "-fx-font-family:monospace; -fx-font-size:12px; -fx-background-color:white;";

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

		btnSandboxConfig = new Button("Configure Sandbox");
		btnSandboxConfig.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				sandboxConfigStage = new SandboxConfigStage(SandboxStage.this, parser.getConfig());

				StageManager.addAndShow(sandboxConfigStage);

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

		HBox hBoxTools = new HBox();

		hBoxTools.setSpacing(10);
		hBoxTools.setPadding(new Insets(10));

		hBoxTools.getChildren().add(btnNewEditor);
		hBoxTools.getChildren().add(btnSandboxConfig);
		hBoxTools.getChildren().add(btnResetSandbox);
		hBoxTools.getChildren().add(comboBoxVMLanguage);

		splitVertical.getItems().add(splitEditorPanes);
		splitVertical.getItems().add(taLog);

		splitVertical.setDividerPositions(0.75, 0.25);

		VBox vBoxMain = new VBox();
		vBoxMain.getChildren().add(hBoxTools);
		vBoxMain.getChildren().add(splitVertical);

		BorderPane borderPane = new BorderPane();
		borderPane.setTop(hBoxTools);
		borderPane.setCenter(splitVertical);

		initialiseLog();

		Scene scene = new Scene(borderPane, JITWatchUI.WINDOW_WIDTH, JITWatchUI.WINDOW_HEIGHT);

		setScene(scene);

		setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				saveEditorPaneConfig();

				StageManager.closeAll();
				closeListener.handleStageClosed(SandboxStage.this);
			}
		});

		loadLastEditorPanes();
	}

	public void runFile(final EditorPane pane)
	{
		saveUnsavedEditors();

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				runSandbox(pane.getSourceFile());
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
			editorPanes.clear();
			splitEditorPanes.getItems().clear();

			for (String panePath : panes)
			{
				addEditor(panePath);
			}
		}
	}

	private void loadDefaultEditors()
	{
		editorPanes.clear();
		splitEditorPanes.getItems().clear();

		addEditor("SandboxTest.java");
		addEditor("SandboxTestLoad.java");

		saveEditorPaneConfig();
	}

	private void addEditor(String filename)
	{
		EditorPane editor = new EditorPane(this);

		logger.debug("Add editor: {}", filename);

		if (filename != null)
		{
			editor.loadSource(Sandbox.SANDBOX_SOURCE_DIR.toFile(), filename);
		}

		editorPanes.add(editor);
		splitEditorPanes.getItems().add(editor);
		setEditorDividers();
	}

	private void setEditorDividers()
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				int editorCount = editorPanes.size();

				double widthFraction = 1.0 / editorCount;
				double dividerPos = widthFraction;

				for (int i = 0; i < editorCount - 1; i++)
				{
					splitEditorPanes.setDividerPosition(i, dividerPos);
					dividerPos += widthFraction;
				}
			}
		});
	}

	private void saveEditorPaneConfig()
	{
		List<String> editorPanePaths = new ArrayList<>();

		for (EditorPane pane : editorPanes)
		{
			logger.debug("maybe adding pane: {}", pane);

			if (pane.getSourceFile() != null)
			{
				String editorPanePath = pane.getSourceFile().getAbsolutePath();
				editorPanePaths.add(editorPanePath);

				logger.debug("Added: {}", editorPanePath);
			}
		}

		config.setLastEditorPaneList(editorPanePaths);

		config.saveConfig();
	}

	private void saveUnsavedEditors()
	{
		for (EditorPane editor : editorPanes)
		{
			editor.promptSave();
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

				for (EditorPane editor : editorPanes)
				{
					File sourceFile = editor.getSourceFile();

					if (sourceFile != null)
					{
						if (sourceFile.getName().toLowerCase().endsWith(language.toLowerCase()))
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
					log("All editors are empty");
				}
			}
		}
		catch (Exception e)
		{
			logger.error("Sandbox failure", e);
		}
	}

	public void editorClosed(EditorPane editor)
	{
		editorPanes.remove(editor);
		splitEditorPanes.getItems().remove(editor);
		setEditorDividers();
	}
	
	public void editorGotFocus(EditorPane editor)
	{
		
	}

	public void addSourceFolder(File sourceFolder)
	{
		config.addSourceFolder(sourceFolder);
	}

	public void setVMLanguageFromFileExtension(String vmLanguage)
	{
		if (vmLanguage != null)
		{
			for (String knownLang : languageList)
			{
				if (knownLang.toLowerCase().equals(vmLanguage.toLowerCase()))
				{
					comboBoxVMLanguage.getSelectionModel().select(knownLang);
					break;
				}
			}
		}
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
				accessProxy.openTriView(member, true);
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
		StageManager.remove(stage);

		if (stage instanceof SandboxConfigStage)
		{
			btnSandboxConfig.setDisable(false);
		}
	}

	private void setupVMLanguages()
	{
		List<String> vmLanguageList = config.getVMLanguageList();

		if (!vmLanguageList.contains(JITWatchConstants.VM_LANGUAGE_JAVA))
		{
			String javaCompiler = Paths.get(System.getProperty("java.home"), "..", "bin", "javac").toString();
			String javaRuntime = Paths.get(System.getProperty("java.home"), "bin", "java").toString();

			config.addOrUpdateVMLanguage(VM_LANGUAGE_JAVA, javaCompiler, javaRuntime);
			config.saveConfig();
		}

		if (!vmLanguageList.contains(JITWatchConstants.VM_LANGUAGE_SCALA))
		{
			config.addOrUpdateVMLanguage(VM_LANGUAGE_SCALA, S_EMPTY, S_EMPTY);
			config.saveConfig();
		}

		vmLanguageList = config.getVMLanguageList();

		Collections.sort(vmLanguageList);

		languageList.addAll(vmLanguageList);

		comboBoxVMLanguage.getSelectionModel().select(VM_LANGUAGE_JAVA);
	}

	@Override
	public void handleError(final String title, final String body)
	{
		logger.error(title);

		Platform.runLater(new Runnable()
		{
			public void run()
			{
				Dialogs.showOKDialog(SandboxStage.this, title, body);
			}
		});
	}
	
	public Stage getStageForChooser()
	{
		return this;
	}
}