/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.sandbox;

import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.core.ILogParser;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class SandboxStage extends Stage implements ISandboxStage, IStageCloseListener
{
	private static final Logger logger = LoggerFactory.getLogger(SandboxStage.class);

	private List<EditorPane> editorPanes = new ArrayList<>();

	private TextArea taLog;

	private IStageAccessProxy accessProxy;

	private Sandbox sandbox;

	private SplitPane splitEditorPanes;
	
	private Button btnSandboxConfig;
	
	private SandboxConfigStage sandboxConfigStage;
	
	private StageManager stageManager = new StageManager();

	public SandboxStage(final IStageCloseListener closeListener, IStageAccessProxy proxy, final ILogParser parser)
	{
		this.accessProxy = proxy;
		
		parser.getConfig().switchToSandbox();

		sandbox = new Sandbox(parser, this);

		setTitle("JIT Sandbox");

		Button btnRun = new Button("Run");
		btnRun.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				saveUnsavedEditors();

				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						runSandbox();
					}
				}).start();
			}
		});

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
				
				stageManager.add(sandboxConfigStage);
				
				sandboxConfigStage.show();

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
					sandbox.reset();
					loadDefaultEditors();
				}
			}
		});

		HBox hBoxTools = new HBox();

		hBoxTools.setSpacing(10);
		hBoxTools.setPadding(new Insets(10));

		hBoxTools.getChildren().add(btnRun);
		hBoxTools.getChildren().add(btnNewEditor);
		hBoxTools.getChildren().add(btnSandboxConfig);
		hBoxTools.getChildren().add(btnResetSandbox);

		splitVertical.getItems().add(hBoxTools);
		splitVertical.getItems().add(splitEditorPanes);
		splitVertical.getItems().add(taLog);

		splitVertical.setDividerPositions(0.1, 0.7, 0.2);

		log("Sandbox ready");
		log("HotSpot disassembler (hsdis) available: " + DisassemblyUtil.isDisassemblerAvailable());

		Scene scene = new Scene(splitVertical, JITWatchUI.WINDOW_WIDTH, JITWatchUI.WINDOW_HEIGHT);

		setScene(scene);

		setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				parser.getConfig().switchFromSandbox();

				stageManager.closeAll();
				closeListener.handleStageClosed(SandboxStage.this);
			}
		});

		loadDefaultEditors();
	}

	private void loadDefaultEditors()
	{
		editorPanes.clear();
		splitEditorPanes.getItems().clear();
		
		addEditor("SandboxTest.java");
		addEditor("SandboxTestLoad.java");
	}

	private void addEditor(String filename)
	{
		EditorPane editor = new EditorPane(this);

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

	private void saveUnsavedEditors()
	{
		for (EditorPane editor : editorPanes)
		{
			editor.promptSave();
		}
	}

	private void runSandbox()
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

			List<String> sources = new ArrayList<>();

			for (EditorPane editor : editorPanes)
			{
				if (editor.getSource().length() > 0)
				{
					sources.add(editor.getSource());
				}
			}

			if (sources.size() > 0)
			{
				sandbox.runSandbox(sources);
			}
			else
			{
				log("All editors are empty");
			}
		}
		catch (Exception e)
		{
			logger.error("Sandbox failure", e);
		}
	}

	void editorClosed(EditorPane editor)
	{
		editorPanes.remove(editor);
		splitEditorPanes.getItems().remove(editor);
		setEditorDividers();
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
		stageManager.remove(stage);
		
		if (stage instanceof SandboxConfigStage)
		{
			btnSandboxConfig.setDisable(false);
		}
	}
}
