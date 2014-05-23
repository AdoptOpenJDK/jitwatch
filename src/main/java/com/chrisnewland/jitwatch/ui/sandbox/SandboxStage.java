/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui.sandbox;

import com.chrisnewland.jitwatch.core.ILogParser;
import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.sandbox.Sandbox;
import com.chrisnewland.jitwatch.ui.IStageAccessProxy;
import com.chrisnewland.jitwatch.ui.IStageCloseListener;
import com.chrisnewland.jitwatch.ui.JITWatchUI;
import com.chrisnewland.jitwatch.util.DisassemblyUtil;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.S_EMPTY;
import static com.chrisnewland.jitwatch.core.JITWatchConstants.S_NEWLINE;

public class SandboxStage extends Stage implements ISandboxStage
{
    private static final Logger logger = LoggerFactory.getLogger(SandboxStage.class);
    public static final int TEN_SPACES = 10;
    public static final int TEN_INSETS = 10;
    public static final double DIVIDER_POSITION_ONE = 0.1;
    public static final double DIVIDER_POSITION_TWO = 0.7;
    public static final double DIVIDER_POSITION_THREE = 0.2;

    private List<EditorPane> editorPanes = new ArrayList<>();

	private TextArea taLog;

	private IStageAccessProxy accessProxy;

	private boolean intelMode = false;

	private Sandbox sandbox;

	private SplitPane splitEditorPanes;

	static final File SANDBOX_EXAMPLE_DIR = new File("src/main/java/com/chrisnewland/jitwatch/demo");

	public SandboxStage(final IStageCloseListener closeListener, IStageAccessProxy proxy, ILogParser parser)
	{
		this.accessProxy = proxy;

		sandbox = new Sandbox(parser, this);

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

		rbIntel.setDisable(true); // TODO support Intel format

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

		splitEditorPanes = new SplitPane();
		splitEditorPanes.setOrientation(Orientation.HORIZONTAL);

		SplitPane splitVertical = new SplitPane();
		splitVertical.setOrientation(Orientation.VERTICAL);

		Label lblSyntax = new Label("Assembly syntax:");

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

		HBox hBoxTools = new HBox();

		hBoxTools.setSpacing(TEN_SPACES);
		hBoxTools.setPadding(new Insets(TEN_INSETS));

		hBoxTools.getChildren().add(lblSyntax);
		hBoxTools.getChildren().add(rbATT);
		hBoxTools.getChildren().add(rbIntel);
		hBoxTools.getChildren().add(btnRunTestLoad);
		hBoxTools.getChildren().add(btnNewEditor);

		splitVertical.getItems().add(hBoxTools);
		splitVertical.getItems().add(splitEditorPanes);
		splitVertical.getItems().add(taLog);

		splitVertical.setDividerPositions(
                DIVIDER_POSITION_ONE,
                DIVIDER_POSITION_TWO,
                DIVIDER_POSITION_THREE);

		log("Sandbox ready");
		log("HotSpot disassembler (hsdis) available: " + DisassemblyUtil.isDisassemblerAvailable());

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
		addEditor("SandboxTest.java");
		addEditor("SandboxTestLoad.java");
	}

	private void addEditor(String filename)
	{
		EditorPane editor = new EditorPane(this);

		if (filename != null)
		{
			editor.loadSource(SANDBOX_EXAMPLE_DIR, filename);
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
				sandbox.runSandbox(sources, intelMode);
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
}
