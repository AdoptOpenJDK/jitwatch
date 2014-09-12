/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.TimelineBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import org.adoptopenjdk.jitwatch.chain.CompileChainWalker;
import org.adoptopenjdk.jitwatch.chain.CompileNode;
import org.adoptopenjdk.jitwatch.core.HotSpotLogParser;
import org.adoptopenjdk.jitwatch.core.IJITListener;
import org.adoptopenjdk.jitwatch.core.ILogParser;
import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.JITEvent;
import org.adoptopenjdk.jitwatch.model.Journal;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.model.PackageManager;
import org.adoptopenjdk.jitwatch.ui.graphing.CodeCacheStage;
import org.adoptopenjdk.jitwatch.ui.graphing.HistoStage;
import org.adoptopenjdk.jitwatch.ui.graphing.TimeLineStage;
import org.adoptopenjdk.jitwatch.ui.sandbox.SandboxStage;
import org.adoptopenjdk.jitwatch.ui.suggestion.SuggestStage;
import org.adoptopenjdk.jitwatch.ui.toplist.TopListStage;
import org.adoptopenjdk.jitwatch.ui.triview.TriView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JITWatchUI extends Application implements IJITListener, IStageCloseListener, IStageAccessProxy
{
	private static final Logger logger = LoggerFactory.getLogger(JITWatchUI.class);

	public static final int WINDOW_WIDTH = 1024;
	public static final int WINDOW_HEIGHT;

	private static final String JAVA_VERSION_7 = "1.7";

	static
	{
		String version = System.getProperty("java.version", JAVA_VERSION_7);

		if (version.contains(JAVA_VERSION_7))
		{
			WINDOW_HEIGHT = 592;
		}
		else
		{
			// JavaFX 8 buttons have more padding.
			WINDOW_HEIGHT = 550;
		}
	}

	private Stage stage;

	private ILogParser logParser;

	private ClassTree classTree;
	private ClassMemberList classMemberList;

	private TableView<AttributeTableRow> attributeTableView;
	private ObservableList<AttributeTableRow> memberAttrList;

	private TextArea textAreaLog;

	private File hsLogFile = null;

	private boolean isReadingLogFile = false;

	private Button btnStart;
	private Button btnStop;
	private Button btnConfigure;
	private Button btnTimeLine;
	private Button btnStats;
	private Button btnHisto;
	private Button btnTopList;
	private Button btnErrorLog;
	private Button btnCodeCache;
	private Button btnTriView;
	private Button btnSuggest;
	private Button btnSandbox;

	private Label lblHeap;

	private MainConfigStage configStage;
	private TimeLineStage timeLineStage;
	private StatsStage statsStage;
	private HistoStage histoStage;
	private TopListStage topListStage;
	private CodeCacheStage codeCacheStage;
	private TriView triViewStage;
	private BrowserStage browserStage;
	private SuggestStage suggestStage;
	private SandboxStage sandBoxStage;

	private NothingMountedStage nothingMountedStage;

	private IMetaMember selectedMember;
	private MetaClass selectedMetaClass;

	private Runtime runtime = Runtime.getRuntime();

	// synchronized as buffer is drained async on GUI thread
	private StringBuffer logBuffer = new StringBuffer();

	private StringBuilder errorLog = new StringBuilder();
	private int errorCount = 0;

	private boolean repaintTree = false;
	private boolean startDelayedByConfig = false;

	// Called by JFX
	public JITWatchUI()
	{
		logParser = new HotSpotLogParser(this);

		JITWatchConfig config = new JITWatchConfig();

		logParser.setConfig(config);
	}

	public JITWatchUI(String[] args)
	{
		launch(args);
	}

	private void readLogFile()
	{
		Thread jwThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					logParser.readLogFile(hsLogFile);
				}
				catch (IOException ioe)
				{
					log("Exception during log processing: " + ioe.toString());
				}
			}
		});

		jwThread.start();
	}

	@Override
	public void handleReadStart()
	{
		startDelayedByConfig = false;

		selectedMember = null;

		errorCount = 0;
		errorLog.delete(0, errorLog.length());

		isReadingLogFile = true;

		Platform.runLater(new Runnable()
		{
			public void run()
			{
				updateButtons();

				classTree.clear();
				refreshSelectedTreeNode(null);

				textAreaLog.clear();
			}
		});
	}

	@Override
	public void handleReadComplete()
	{
		log("Finished reading log file.");
		isReadingLogFile = false;

		Platform.runLater(new Runnable()
		{
			public void run()
			{
				updateButtons();
			}
		});

		if (!logParser.hasTraceClassLoading())
		{
			logger.error("Required VM switch -XX:+TraceClassLoading was not enabled");

			Platform.runLater(new Runnable()
			{
				public void run()
				{
					String title = "Missing VM Switch -XX:+TraceClassLoading";
					String msg = "JITWatch requires the -XX:+TraceClassLoading VM switch to be used.\nPlease recreate your log file with this switch enabled.";

					Dialogs.showOKDialog(JITWatchUI.this.stage, title, msg);
				}
			});
		}
	}

	private void stopParsing()
	{
		if (isReadingLogFile)
		{
			logParser.stopParsing();
			isReadingLogFile = false;
			updateButtons();

			log("Stopped parsing " + hsLogFile.getAbsolutePath());
		}
	}

	private JITWatchConfig getConfig()
	{
		return logParser.getConfig();
	}

	@Override
	public void start(final Stage stage)
	{
		this.stage = stage;

		stage.setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				StageManager.closeAll();

				stopParsing();
			}
		});

		BorderPane borderPane = new BorderPane();

		Scene scene = new Scene(borderPane, WINDOW_WIDTH, WINDOW_HEIGHT);

		Button btnChooseWatchFile = new Button("Open Log");
		btnChooseWatchFile.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				stopParsing();
				chooseHotSpotFile();
			}
		});

		btnStart = new Button("Start");
		btnStart.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				if (nothingMountedStage == null)
				{
					int classCount = getConfig().getClassLocations().size();
					int sourceCount = getConfig().getSourceLocations().size();

					if (classCount == 0 && sourceCount == 0)
					{
						if (getConfig().isShowNothingMounted())
						{
							nothingMountedStage = new NothingMountedStage(JITWatchUI.this, getConfig());

							StageManager.addAndShow(nothingMountedStage);

							startDelayedByConfig = true;
						}
					}
				}

				if (!startDelayedByConfig)
				{
					readLogFile();
				}
			}
		});

		btnStop = new Button("Stop");
		btnStop.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				stopParsing();
			}
		});

		btnConfigure = new Button("Config");
		btnConfigure.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				openConfigStage();
			}
		});

		btnTimeLine = new Button("Chart");
		btnTimeLine.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				timeLineStage = new TimeLineStage(JITWatchUI.this);

				StageManager.addAndShow(timeLineStage);

				btnTimeLine.setDisable(true);
			}
		});

		btnStats = new Button("Stats");
		btnStats.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				statsStage = new StatsStage(JITWatchUI.this);

				StageManager.addAndShow(statsStage);

				btnStats.setDisable(true);
			}
		});

		btnHisto = new Button("Histo");
		btnHisto.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				histoStage = new HistoStage(JITWatchUI.this);

				StageManager.addAndShow(histoStage);

				btnHisto.setDisable(true);
			}
		});

		btnTopList = new Button("TopList");
		btnTopList.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				topListStage = new TopListStage(JITWatchUI.this);

				StageManager.addAndShow(topListStage);

				btnTopList.setDisable(true);
			}
		});

		btnCodeCache = new Button("Code Cache");
		btnCodeCache.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				codeCacheStage = new CodeCacheStage(JITWatchUI.this);

				StageManager.addAndShow(codeCacheStage);

				btnCodeCache.setDisable(true);
			}
		});

		btnTriView = new Button("TriView");
		btnTriView.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				if (selectedMember == null && selectedMetaClass != null)
				{
					selectedMember = selectedMetaClass.getFirstConstructor();
				}

				openTriView(selectedMember, false);
			}
		});

		btnSuggest = new Button("Suggest");
		btnSuggest.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				suggestStage = new SuggestStage(JITWatchUI.this);

				StageManager.addAndShow(suggestStage);

				btnSuggest.setDisable(true);
			}
		});

		btnSandbox = new Button("Sandbox");
		btnSandbox.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				openSandbox();
			}
		});

		btnErrorLog = new Button("Errors (0)");
		btnErrorLog.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				openTextViewer("Error Log", errorLog.toString(), false, false);
			}
		});

		btnErrorLog.setStyle("-fx-padding: 2 6;");

		lblHeap = new Label();
		
		Label lblVmVersion = new Label();
		
		StringBuilder vmBuilder = new StringBuilder();
		vmBuilder.append("VM is ");
		vmBuilder.append(Runtime.class.getPackage().getImplementationVendor());
		vmBuilder.append(C_SPACE);
		vmBuilder.append(Runtime.class.getPackage().getImplementationVersion());

		lblVmVersion.setText(vmBuilder.toString());

		int menuBarHeight = 40;
		int textAreaHeight = 100;
		int statusBarHeight = 25;

		HBox hboxTop = new HBox();

		hboxTop.setPadding(new Insets(10));
		hboxTop.setPrefHeight(menuBarHeight);
		hboxTop.setSpacing(10);
		hboxTop.getChildren().add(btnSandbox);
		hboxTop.getChildren().add(btnChooseWatchFile);
		hboxTop.getChildren().add(btnStart);
		hboxTop.getChildren().add(btnStop);
		hboxTop.getChildren().add(btnConfigure);
		hboxTop.getChildren().add(btnTimeLine);
		hboxTop.getChildren().add(btnStats);
		hboxTop.getChildren().add(btnHisto);
		hboxTop.getChildren().add(btnTopList);
		hboxTop.getChildren().add(btnCodeCache);
		hboxTop.getChildren().add(btnTriView);
		hboxTop.getChildren().add(btnSuggest);

		memberAttrList = FXCollections.observableArrayList();
		attributeTableView = TableUtil.buildTableMemberAttributes(memberAttrList);
		attributeTableView.setPlaceholder(new Text("Select a JIT-compiled class member to view compilation attributes."));

		SplitPane spMethodInfo = new SplitPane();
		spMethodInfo.setOrientation(Orientation.VERTICAL);

		classMemberList = new ClassMemberList(this, getConfig());

		spMethodInfo.getItems().add(classMemberList);
		spMethodInfo.getItems().add(attributeTableView);

		classMemberList.prefHeightProperty().bind(scene.heightProperty());
		attributeTableView.prefHeightProperty().bind(scene.heightProperty());

		classTree = new ClassTree(this, getConfig());
		classTree.prefWidthProperty().bind(scene.widthProperty());

		SplitPane spMain = new SplitPane();
		spMain.setOrientation(Orientation.VERTICAL);

		SplitPane spCentre = new SplitPane();
		spCentre.getItems().add(classTree);
		spCentre.getItems().add(spMethodInfo);
		spCentre.setDividerPositions(0.33, 0.67);

		textAreaLog = new TextArea();
		textAreaLog.setStyle("-fx-font-family:monospace;-fx-font-size:12px");
		textAreaLog.setPrefHeight(textAreaHeight);

		log("Welcome to JITWatch by Chris Newland (@chriswhocodes on Twitter) and the AdoptOpenJDK project.\n");

		log("Please send feedback to our mailing list (https://groups.google.com/forum/#!forum/jitwatch) \nor come and find us on GitHub (https://github.com/AdoptOpenJDK/jitwatch).\n");

		log("Includes assembly reference from http://ref.x86asm.net by Karel Lejska. Licenced under http://ref.x86asm.net/index.html#License\n");
		
		if (hsLogFile == null)
		{
			log("Choose a HotSpot log file or open the Sandbox");
		}
		else
		{
			log("Using HotSpot log file: " + hsLogFile.getAbsolutePath());
		}
		spMain.getItems().add(spCentre);
		spMain.getItems().add(textAreaLog);
		spMain.setDividerPositions(0.68, 0.32);

		HBox hboxBottom = new HBox();
		
		Region spring = new Region();
		
		final String labelStyle = "-fx-padding: 3 0 0 0;";

		HBox.setHgrow(spring, Priority.ALWAYS);
		
		lblHeap.setStyle(labelStyle);
		lblVmVersion.setStyle(labelStyle);
		
		hboxBottom.setPadding(new Insets(4));
		hboxBottom.setPrefHeight(statusBarHeight);
		hboxBottom.setSpacing(4);
		hboxBottom.getChildren().add(lblHeap);
		hboxBottom.getChildren().add(btnErrorLog);
		hboxBottom.getChildren().add(spring);
		hboxBottom.getChildren().add(lblVmVersion);

		borderPane.setTop(hboxTop);
		borderPane.setCenter(spMain);
		borderPane.setBottom(hboxBottom);

		stage.setTitle("JITWatch - HotSpot Compilation Inspector");
		stage.setScene(scene);
		stage.show();

		int refresh = 1000; // ms

		final Duration oneFrameAmt = Duration.millis(refresh);

		final KeyFrame oneFrame = new KeyFrame(oneFrameAmt, new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent arg0)
			{
				refresh();
			}
		});

		TimelineBuilder.create().cycleCount(Animation.INDEFINITE).keyFrames(oneFrame).build().play();

		updateButtons();
	}

	void openConfigStage()
	{
		if (configStage == null)
		{
			configStage = new MainConfigStage(this, this, getConfig());

			StageManager.addAndShow(configStage);

			btnConfigure.setDisable(true);
		}
	}

	@Override
	public void openTriView(IMetaMember member, boolean force)
	{
		if (triViewStage == null)
		{
			triViewStage = new TriView(JITWatchUI.this, getConfig());

			StageManager.addAndShow(triViewStage);

			btnTriView.setDisable(true);
		}

		if (member != null)
		{
			triViewStage.setMember(member, force);
		}
	}

	public void openSandbox()
	{
		if (sandBoxStage == null)
		{
			sandBoxStage = new SandboxStage(this, this, logParser);

			StageManager.addAndShow(sandBoxStage);

			btnSandbox.setDisable(true);
		}
	}

	@Override
	public void openBrowser(String title, String html, String stylesheet)
	{
		if (browserStage == null)
		{
			browserStage = new BrowserStage(JITWatchUI.this);

			StageManager.addAndShow(browserStage);
		}

		browserStage.setContent(title, html, stylesheet);
	}

	public IReadOnlyJITDataModel getJITDataModel()
	{
		return (IReadOnlyJITDataModel) logParser.getModel();
	}

	private void updateButtons()
	{
		btnStart.setDisable(hsLogFile == null || isReadingLogFile);
		btnStop.setDisable(!isReadingLogFile);
	}

	public void openTreeAtMember(IMetaMember member)
	{
		List<String> path = member.getTreePath();

		// would be better to identify open nodes and close?
		clearAndRefresh();

		TreeItem<Object> curNode = classTree.getRootItem();

		StringBuilder builtPath = new StringBuilder();

		int pathLength = path.size();
		int pos = 0;

		int rowsAbove = 0;

		boolean found = false;

		for (String part : path)
		{
			builtPath.append(part);

			String matching;

			found = false;

			if (pos++ == pathLength - 1)
			{
				matching = part;
			}
			else
			{
				matching = builtPath.toString();
			}

			for (TreeItem<Object> node : curNode.getChildren())
			{
				rowsAbove++;

				String nodeText = node.getValue().toString();

				if (matching.equals(nodeText))
				{
					builtPath.append('.');
					curNode = node;
					curNode.setExpanded(true);
					classTree.select(curNode);
					found = true;
					break;
				}
			}
		}

		if (found)
		{
			classTree.scrollTo(rowsAbove);
			classMemberList.selectMember(member);
		}
	}

	@Override
	public void openTextViewer(String title, String content, boolean lineNumbers, boolean highlighting)
	{
		TextViewerStage tvs = new TextViewerStage(this, title, content, lineNumbers, highlighting);
		StageManager.addAndShow(tvs);
	}

	public void openTextViewer(String title, String content)
	{
		openTextViewer(title, content, false, false);
	}

	public void openCompileChain(IMetaMember member)
	{
		if (member.isCompiled())
		{
			CompileChainWalker walker = new CompileChainWalker(logParser.getModel());

			CompileNode root = walker.buildCallTree(member);

			if (root != null)
			{
				CompileChainStage ccs = new CompileChainStage(this, root);

				StageManager.addAndShow(ccs);
			}
			else
			{
				logger.error("Could not open CompileChain - root node was null");
			}
		}
		else
		{
			Dialogs.showOKDialog(
					stage,
					"Root method is not compiled",
					"Can only display compile chain where the root method has been JIT-compiled.\n"
							+ member.toStringUnqualifiedMethodName(false) + " is not compiled.");
		}
	}

	void openJournalViewer(String title, Journal journal)
	{
		JournalViewerStage jvs = new JournalViewerStage(this, title, journal);
		StageManager.addAndShow(jvs);
	}

	private void chooseHotSpotFile()
	{
		FileChooser fc = new FileChooser();
		fc.setTitle("Choose HotSpot log file");

		String osNameProperty = System.getProperty("os.name");

		// don't use ExtensionFilter on OSX due to JavaFX2 missing combo bug
		if (osNameProperty != null && !osNameProperty.toLowerCase().contains("mac"))
		{
			fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Log Files", "*.log"),
					new FileChooser.ExtensionFilter("All Files", "*.*"));
		}

		String searchDir = getConfig().getLastLogDir();

		if (searchDir == null)
		{
			searchDir = System.getProperty("user.dir");
		}

		File dirFile = new File(searchDir);

		if (!dirFile.exists() || !dirFile.isDirectory())
		{
			dirFile = new File(System.getProperty("user.dir"));
		}

		fc.setInitialDirectory(dirFile);

		File result = fc.showOpenDialog(stage);

		if (result != null)
		{
			hsLogFile = result;

			getConfig().setLastLogDir(hsLogFile.getParent());
			getConfig().saveConfig();

			clearTextArea();
			log("Selected log file: " + hsLogFile.getAbsolutePath());

			log("\nUsing Config: " + getConfig().getProfileName());

			log("\nClick Start button to process the HotSpot log");
			updateButtons();

			refreshLog();
		}
	}

	void showMemberInfo(IMetaMember member)
	{
		memberAttrList.clear();

		if (member == null)
		{
			return;
		}

		if (triViewStage != null)
		{
			triViewStage.setMember(member, false);
		}

		selectedMember = member;

		List<String> queuedAttrKeys = member.getQueuedAttributes();

		for (String key : queuedAttrKeys)
		{
			memberAttrList.add(new AttributeTableRow("Queued", key, member.getQueuedAttribute(key)));
		}

		List<String> compiledAttrKeys = member.getCompiledAttributes();

		for (String key : compiledAttrKeys)
		{
			memberAttrList.add(new AttributeTableRow("Compiled", key, member.getCompiledAttribute(key)));
		}
	}

	private void refresh()
	{
		if (repaintTree)
		{
			repaintTree = false;
			classTree.showTree();
		}

		if (timeLineStage != null)
		{
			timeLineStage.redraw();
		}

		if (codeCacheStage != null)
		{
			codeCacheStage.redraw();
		}

		if (statsStage != null)
		{
			statsStage.redraw();
		}

		if (histoStage != null)
		{
			histoStage.redraw();
		}

		if (topListStage != null)
		{
			topListStage.redraw();
		}

		if (logBuffer.length() > 0)
		{
			refreshLog();
		}

		long totalMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		long usedMemory = totalMemory - freeMemory;

		long megabyte = 1024 * 1024;

		String heapString = "Heap: " + (usedMemory / megabyte) + S_SLASH + (totalMemory / megabyte) + "M";

		lblHeap.setText(heapString);

		btnErrorLog.setText("Errors (" + errorCount + S_CLOSE_PARENTHESES);
	}

	private void clearTextArea()
	{
		textAreaLog.clear();
	}

	private void refreshLog()
	{
		textAreaLog.appendText(logBuffer.toString());
		logBuffer.delete(0, logBuffer.length());
	}

	public IMetaMember getSelectedMember()
	{
		return selectedMember;
	}

	void clearAndRefresh()
	{
		selectedMember = null;
		classTree.clear();
		classTree.showTree();
	}

	@Override
	public void handleStageClosed(Stage stage)
	{
		StageManager.remove(stage);

		// map?
		if (stage instanceof TimeLineStage)
		{
			btnTimeLine.setDisable(false);
			timeLineStage = null;
		}
		else if (stage instanceof StatsStage)
		{
			btnStats.setDisable(false);
			statsStage = null;
		}
		else if (stage instanceof HistoStage)
		{
			btnHisto.setDisable(false);
			histoStage = null;
		}
		else if (stage instanceof MainConfigStage)
		{
			btnConfigure.setDisable(false);
			configStage = null;

			if (startDelayedByConfig)
			{
				readLogFile();
			}
		}
		else if (stage instanceof TopListStage)
		{
			btnTopList.setDisable(false);
			topListStage = null;
		}
		else if (stage instanceof NothingMountedStage)
		{
			nothingMountedStage = null;

			if (configStage == null && startDelayedByConfig)
			{
				readLogFile();
			}
		}
		else if (stage instanceof CodeCacheStage)
		{
			btnCodeCache.setDisable(false);
			codeCacheStage = null;
		}
		else if (stage instanceof TriView)
		{
			btnTriView.setDisable(false);
			triViewStage = null;
		}
		else if (stage instanceof SuggestStage)
		{
			btnSuggest.setDisable(false);
			suggestStage = null;
		}
		else if (stage instanceof BrowserStage)
		{
			browserStage = null;
		}
		else if (stage instanceof SandboxStage)
		{
			btnSandbox.setDisable(false);
			sandBoxStage = null;
		}
	}

	@Override
	public void handleJITEvent(JITEvent event)
	{
		log(event.toString());
		repaintTree = true;
	}

	@Override
	public void handleLogEntry(String entry)
	{
		log(entry);
	}

	@Override
	public void handleErrorEntry(String entry)
	{
		errorLog.append(entry).append(S_NEWLINE);
		errorCount++;
	}

	private void log(final String entry)
	{
		logBuffer.append(entry);
		logBuffer.append(S_NEWLINE);
	}

	void refreshSelectedTreeNode(MetaClass metaClass)
	{
		classMemberList.clearClassMembers();
		selectedMetaClass = metaClass;

		showMemberInfo(null);

		if (metaClass == null)
		{
			// nothing selected
			return;
		}

		classMemberList.setMetaClass(metaClass);
	}

	public PackageManager getPackageManager()
	{
		return logParser.getModel().getPackageManager();
	}

	@Override
	public Stage getStageForDialog()
	{
		return stage;
	}
}