/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.main;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_DOT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEFAULT_PACKAGE_NAME;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_CLOSE_PARENTHESES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_SLASH;
import static org.adoptopenjdk.jitwatch.util.UserInterfaceUtil.FONT_MONOSPACE_FAMILY;
import static org.adoptopenjdk.jitwatch.util.UserInterfaceUtil.FONT_MONOSPACE_SIZE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.chain.CompileChainWalker;
import org.adoptopenjdk.jitwatch.chain.CompileNode;
import org.adoptopenjdk.jitwatch.compilation.codecache.CodeCacheEventWalker;
import org.adoptopenjdk.jitwatch.compilation.codecache.CodeCacheWalkerResult;
import org.adoptopenjdk.jitwatch.core.ErrorLog;
import org.adoptopenjdk.jitwatch.core.HotSpotLogParser;
import org.adoptopenjdk.jitwatch.core.IJITListener;
import org.adoptopenjdk.jitwatch.core.ILogParseErrorListener;
import org.adoptopenjdk.jitwatch.core.ILogParser;
import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.core.JITWatchConstants;
import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.JITEvent;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.model.PackageManager;
import org.adoptopenjdk.jitwatch.optimizedvcall.OptimizedVirtualCall;
import org.adoptopenjdk.jitwatch.optimizedvcall.OptimizedVirtualCallFinder;
import org.adoptopenjdk.jitwatch.optimizedvcall.OptimizedVirtualCallVisitable;
import org.adoptopenjdk.jitwatch.report.Report;
import org.adoptopenjdk.jitwatch.report.comparator.ScoreComparator;
import org.adoptopenjdk.jitwatch.report.escapeanalysis.eliminatedallocation.EliminatedAllocationWalker;
import org.adoptopenjdk.jitwatch.report.escapeanalysis.lockelision.ElidedLocksWalker;
import org.adoptopenjdk.jitwatch.report.inlining.InliningWalker;
import org.adoptopenjdk.jitwatch.report.suggestion.SuggestionWalker;
import org.adoptopenjdk.jitwatch.ui.Dialogs;
import org.adoptopenjdk.jitwatch.ui.browser.BrowserStage;
import org.adoptopenjdk.jitwatch.ui.codecache.CodeCacheLayoutStage;
import org.adoptopenjdk.jitwatch.ui.compilechain.CompileChainStage;
import org.adoptopenjdk.jitwatch.ui.graphing.CodeCacheStage;
import org.adoptopenjdk.jitwatch.ui.graphing.HistoStage;
import org.adoptopenjdk.jitwatch.ui.graphing.TimeLineStage;
import org.adoptopenjdk.jitwatch.ui.optimizedvcall.OptimizedVirtualCallStage;
import org.adoptopenjdk.jitwatch.ui.report.ReportStage;
import org.adoptopenjdk.jitwatch.ui.report.ReportStageType;
import org.adoptopenjdk.jitwatch.ui.sandbox.SandboxStage;
import org.adoptopenjdk.jitwatch.ui.stage.IStageClosedListener;
import org.adoptopenjdk.jitwatch.ui.stage.StageManager;
import org.adoptopenjdk.jitwatch.ui.stats.StatsStage;
import org.adoptopenjdk.jitwatch.ui.toplist.TopListStage;
import org.adoptopenjdk.jitwatch.ui.triview.TriView;
import org.adoptopenjdk.jitwatch.ui.viewer.JournalViewerStage;
import org.adoptopenjdk.jitwatch.ui.viewer.TextViewerStage;
import org.adoptopenjdk.jitwatch.util.OSUtil;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
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

public class JITWatchUI extends Application
		implements IJITListener, ILogParseErrorListener, IStageClosedListener, IStageAccessProxy, IMemberSelectedListener
{
	private static final Logger logger = LoggerFactory.getLogger(JITWatchUI.class);

	public static final int WINDOW_WIDTH = 1024;
	public static final int WINDOW_HEIGHT = 550;

	private static final String JAVA_VERSION_7 = "1.7";
	public static final boolean IS_JAVA_FX2;

	private boolean selectedProgrammatically = false;

	static
	{
		String version = System.getProperty("java.version", JAVA_VERSION_7);

		if (version.contains(JAVA_VERSION_7))
		{
			IS_JAVA_FX2 = true;

			if (OSUtil.getOperatingSystem() == OSUtil.OperatingSystem.MAC)
			{
				UserInterfaceUtil.initMacFonts();
			}
		}
		else
		{
			IS_JAVA_FX2 = false;
		}
	}

	private Stage stage;

	private ILogParser logParser;

	private ClassTree classTree;
	private ClassMemberList classMemberList;

	private TableView<CompilationTableRow> compilationTable;
	private ObservableList<CompilationTableRow> compilationRowList;

	private TextArea textAreaLog;

	private File hsLogFile = null;

	private String lastVmCommand = null;
	private IMetaMember lastSelectedMember = null;
	private MetaClass lastSelectedClass = null;

	private boolean isReadingLogFile = false;

	private Label lblVmVersion;
	private Label lblTweakLog;

	private Button btnStart;
	private Button btnStop;
	private Button btnConfigure;
	private Button btnTimeLine;
	private Button btnStats;
	private Button btnReset;
	private Button btnHisto;
	private Button btnTopList;
	private Button btnErrorLog;
	private Button btnCodeCacheTimeline;
	private Button btnNMethods;
	private Button btnTriView;
	private Button btnReportSuggestions;
	private Button btnReportEliminatedAllocations;
	private Button btnReportElidedLocks;
	private Button btnOptimizedVirtualCalls;
	private Button btnSandbox;

	private Label lblHeap;

	private MainConfigStage configStage;
	private TimeLineStage timeLineStage;
	private StatsStage statsStage;
	private HistoStage histoStage;
	private TopListStage topListStage;
	private CodeCacheStage codeCacheTimelineStage;
	private CodeCacheLayoutStage codeCacheBlocksStage;
	private TriView triViewStage;
	private BrowserStage browserStage;
		
	private ReportStage reportStageSuggestions;
	private ReportStage reportStageElminatedAllocations;
	private ReportStage reportStageElidedLocks;

	private OptimizedVirtualCallStage ovcStage;
	private SandboxStage sandBoxStage;

	private NothingMountedStage nothingMountedStage;

	private IMetaMember selectedMember;
	private MetaClass selectedMetaClass;

	private List<Report> reportListSuggestions = new ArrayList<>();
	private List<Report> reportListEliminatedAllocations = new ArrayList<>();
	private List<Report> reportListElidedLocks = new ArrayList<>();

	private CodeCacheWalkerResult codeCacheWalkerResult;

	private Runtime runtime = Runtime.getRuntime();

	// synchronized as buffer is drained async on GUI thread
	private StringBuffer logBuffer = new StringBuffer();

	private ErrorLog errorLog = new ErrorLog();
	private int errorCount = 0;

	private boolean repaintTree = false;
	private boolean startDelayedByConfig = false;

	// Called by JFX
	public JITWatchUI()
	{
		logParser = new HotSpotLogParser(this);
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
					logParser.processLogFile(hsLogFile, JITWatchUI.this);
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

		isReadingLogFile = true;
		
		clear();
		
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				updateButtons();
			}
		});
	}
	
	private void clear()
	{
		lastVmCommand = logParser.getVMCommand();
		lastSelectedMember = selectedMember;
		lastSelectedClass = selectedMetaClass;

		selectedMember = null;

		errorCount = 0;
		errorLog.clear();
		
		reportListSuggestions.clear();
		reportListEliminatedAllocations.clear();
		reportListElidedLocks.clear();
						
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				classMemberList.clear();
				
				StageManager.clearReportStages();
				
				codeCacheWalkerResult = null;
				
				if (triViewStage != null)
				{
					triViewStage.clear();
				}
				
				classTree.handleConfigUpdate(getConfig());

				updateButtons();

				classTree.clear();
				metaClassSelectedFromClassTree(null);

				textAreaLog.clear();
				
				refreshOnce();
			}
		});
	}

	@Override
	public void handleReadComplete()
	{
		log("Finished reading log file.");

		isReadingLogFile = false;

		buildSuggestions();

		buildEliminatedAllocationReport();

		buildElidedLocksReport();

		buildCodeCacheResult();

		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				updateButtons();

				refreshOnce();
			}
		});

		logParser.discardParsedLogs();
	}

	private void buildSuggestions()
	{
		log("Finding code suggestions.");

		SuggestionWalker walker = new SuggestionWalker(logParser.getModel());

		reportListSuggestions = walker.getReports(new ScoreComparator());

		log("Found " + reportListSuggestions.size() + " code suggestions.");
	}

	private void buildEliminatedAllocationReport()
	{
		log("Finding eliminated allocations");

		EliminatedAllocationWalker walker = new EliminatedAllocationWalker(logParser.getModel());

		reportListEliminatedAllocations = walker.getReports(new ScoreComparator());

		log("Found " + reportListEliminatedAllocations.size() + "  eliminated allocations.");
	}

	private void buildElidedLocksReport()
	{
		log("Finding elided locks");

		ElidedLocksWalker walker = new ElidedLocksWalker(logParser.getModel());

		reportListElidedLocks = walker.getReports(new ScoreComparator());

		log("Found " + reportListElidedLocks.size() + " elided locks.");
	}

	private void buildCodeCacheResult()
	{
		CodeCacheEventWalker compilationWalker = new CodeCacheEventWalker(logParser.getModel());

		compilationWalker.walkCompilations();

		codeCacheWalkerResult = compilationWalker.getResult();
	}

	public CodeCacheWalkerResult getCodeCacheWalkerResult()
	{
		return codeCacheWalkerResult;
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
				Dialogs.showOKDialog(JITWatchUI.this.stage, title, body);
			}
		});
	}

	private void stopParsing()
	{
		if (isReadingLogFile)
		{
			logParser.stopParsing();
			isReadingLogFile = false;
			updateButtons();

			if (hsLogFile != null)
			{
				log("Stopped parsing " + hsLogFile.getAbsolutePath());
			}
		}
	}

	@Override
	public JITWatchConfig getConfig()
	{
		return logParser.getConfig();
	}

	@Override
	public void start(final Stage stage)
	{
		StageManager.registerListener(this);

		this.stage = stage;

		stage.setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				StageManager.closeStage(stage);

				stopParsing();
			}
		});

		BorderPane borderPane = new BorderPane();

		Scene scene = UserInterfaceUtil.getScene(borderPane, WINDOW_WIDTH, WINDOW_HEIGHT);

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
					int classCount = getConfig().getConfiguredClassLocations().size();
					int sourceCount = getConfig().getSourceLocations().size();

					if (classCount == 0 && sourceCount == 0)
					{
						if (getConfig().isShowNothingMounted())
						{
							nothingMountedStage = new NothingMountedStage(JITWatchUI.this, getConfig());

							StageManager.addAndShow(JITWatchUI.this.stage, nothingMountedStage);

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

		btnTimeLine = new Button("Timeline");
		btnTimeLine.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				timeLineStage = new TimeLineStage(JITWatchUI.this);

				StageManager.addAndShow(JITWatchUI.this.stage, timeLineStage);

				btnTimeLine.setDisable(true);
			}
		});

		btnHisto = new Button("Histo");
		btnHisto.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				histoStage = new HistoStage(JITWatchUI.this);

				StageManager.addAndShow(JITWatchUI.this.stage, histoStage);

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

				StageManager.addAndShow(JITWatchUI.this.stage, topListStage);

				btnTopList.setDisable(true);
			}
		});

		btnCodeCacheTimeline = new Button("Cache");
		btnCodeCacheTimeline.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				codeCacheTimelineStage = new CodeCacheStage(JITWatchUI.this);

				StageManager.addAndShow(JITWatchUI.this.stage, codeCacheTimelineStage);

				btnCodeCacheTimeline.setDisable(true);
			}
		});

		btnNMethods = new Button("NMethods");
		btnNMethods.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				codeCacheBlocksStage = new CodeCacheLayoutStage(JITWatchUI.this);

				StageManager.addAndShow(JITWatchUI.this.stage, codeCacheBlocksStage);

				btnNMethods.setDisable(true);

				codeCacheBlocksStage.redraw();

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

		btnReportSuggestions = new Button("Suggest");
		btnReportSuggestions.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				reportStageSuggestions = new ReportStage(JITWatchUI.this, "JITWatch Code Suggestions", ReportStageType.SUGGESTION,
						reportListSuggestions);

				StageManager.addAndShow(JITWatchUI.this.stage, reportStageSuggestions);

				btnReportSuggestions.setDisable(true);
			}
		});

		btnReportEliminatedAllocations = new Button("-Allocs");
		btnReportEliminatedAllocations.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				reportStageElminatedAllocations = new ReportStage(JITWatchUI.this, "JITWatch Eliminated Allocation Report",
						ReportStageType.ELIMINATED_ALLOCATION, reportListEliminatedAllocations);

				StageManager.addAndShow(JITWatchUI.this.stage, reportStageElminatedAllocations);

				btnReportEliminatedAllocations.setDisable(true);
			}
		});

		btnReportElidedLocks = new Button("-Locks");
		btnReportElidedLocks.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				reportStageElidedLocks = new ReportStage(JITWatchUI.this, "JITWatch Elided Lock Report",
						ReportStageType.ELIDED_LOCK, reportListElidedLocks);

				StageManager.addAndShow(JITWatchUI.this.stage, reportStageElidedLocks);

				btnReportElidedLocks.setDisable(true);
			}
		});

		btnOptimizedVirtualCalls = new Button("OVCs");
		btnOptimizedVirtualCalls.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				OptimizedVirtualCallVisitable optimizedVCallVisitable = new OptimizedVirtualCallVisitable();

				List<OptimizedVirtualCall> optimizedVirtualCalls = optimizedVCallVisitable
						.buildOptimizedCalleeReport(logParser.getModel(), getConfig().getAllClassLocations());

				ovcStage = new OptimizedVirtualCallStage(JITWatchUI.this, optimizedVirtualCalls);

				StageManager.addAndShow(JITWatchUI.this.stage, ovcStage);

				btnOptimizedVirtualCalls.setDisable(true);
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
		btnErrorLog.setStyle("-fx-padding: 2 6;");
		btnErrorLog.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				openTextViewer("Error Log", errorLog.toString(), false, false);
			}
		});

		btnStats = new Button("Stats");
		btnStats.setStyle("-fx-padding: 2 6;");
		btnStats.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				statsStage = new StatsStage(JITWatchUI.this);

				StageManager.addAndShow(JITWatchUI.this.stage, statsStage);

				btnStats.setDisable(true);
			}
		});
		
		btnReset = new Button("Reset");
		btnReset.setStyle("-fx-padding: 2 6;");
		btnReset.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				logParser.reset();
				clear();
			}
		});

		lblHeap = new Label();

		lblVmVersion = new Label();

		StringBuilder vmBuilder = new StringBuilder();

		vmBuilder.append("VM is ");
		vmBuilder.append(Runtime.class.getPackage().getImplementationVendor());
		vmBuilder.append(C_SPACE);
		vmBuilder.append(Runtime.class.getPackage().getImplementationVersion());

		lblVmVersion.setText(vmBuilder.toString());

		lblTweakLog = new Label();

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
		hboxTop.getChildren().add(btnHisto);
		hboxTop.getChildren().add(btnTopList);
		hboxTop.getChildren().add(btnCodeCacheTimeline);
		hboxTop.getChildren().add(btnNMethods);
		hboxTop.getChildren().add(btnTriView);
		hboxTop.getChildren().add(btnReportSuggestions);
		hboxTop.getChildren().add(btnReportEliminatedAllocations);
		hboxTop.getChildren().add(btnReportElidedLocks);
		hboxTop.getChildren().add(btnOptimizedVirtualCalls);

		compilationRowList = FXCollections.observableArrayList();
		compilationTable = CompilationTableBuilder.buildTableMemberAttributes(compilationRowList);
		compilationTable.setPlaceholder(new Text("Select a JIT-compiled class member to view compilations."));

		compilationTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<CompilationTableRow>()
		{
			@Override
			public void changed(ObservableValue<? extends CompilationTableRow> arg0, CompilationTableRow oldVal,
					CompilationTableRow newVal)
			{
				if (!selectedProgrammatically)
				{
					if (selectedMember != null && newVal != null)
					{
						selectedMember.setSelectedCompilation(newVal.getIndex());

						openTriView(selectedMember, true);

						refreshOnce();
					}
				}
			}
		});

		SplitPane spMethodInfo = new SplitPane();
		spMethodInfo.setOrientation(Orientation.VERTICAL);

		classMemberList = new ClassMemberList(this, getConfig());
		classMemberList.registerListener(this);

		spMethodInfo.getItems().add(classMemberList);
		spMethodInfo.getItems().add(compilationTable);

		classMemberList.prefHeightProperty().bind(scene.heightProperty());
		compilationTable.prefHeightProperty().bind(scene.heightProperty());

		classTree = new ClassTree(this, getConfig());
		classTree.prefWidthProperty().bind(scene.widthProperty());

		SplitPane spMain = new SplitPane();
		spMain.setOrientation(Orientation.VERTICAL);

		SplitPane spCentre = new SplitPane();
		spCentre.getItems().add(classTree);
		spCentre.getItems().add(spMethodInfo);
		spCentre.setDividerPositions(0.33, 0.67);

		textAreaLog = new TextArea();
		textAreaLog.setStyle("-fx-font-family:" + FONT_MONOSPACE_FAMILY + ";-fx-font-size:" + FONT_MONOSPACE_SIZE + "px");
		textAreaLog.setPrefHeight(textAreaHeight);

		log("Welcome to JITWatch by Chris Newland (@chriswhocodes on Twitter) and the AdoptOpenJDK project.\n");

		log("Please send feedback to our mailing list (https://groups.google.com/forum/#!forum/jitwatch) \nor come and find us on GitHub (https://github.com/AdoptOpenJDK/jitwatch).\n");

		log("Includes assembly reference from x86asm.net licenced under http://ref.x86asm.net/index.html#License\n");

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

		Region springLeft = new Region();
		Region springRight = new Region();

		final String labelStyle = "-fx-padding: 3 0 0 0;";

		HBox.setHgrow(springLeft, Priority.ALWAYS);
		HBox.setHgrow(springRight, Priority.ALWAYS);

		lblHeap.setStyle(labelStyle);
		lblVmVersion.setStyle(labelStyle);

		hboxBottom.setPadding(new Insets(4));
		hboxBottom.setPrefHeight(statusBarHeight);
		hboxBottom.setSpacing(4);
		hboxBottom.getChildren().add(lblHeap);
		hboxBottom.getChildren().add(btnErrorLog);
		hboxBottom.getChildren().add(btnStats);
		hboxBottom.getChildren().add(btnReset);
		hboxBottom.getChildren().add(springLeft);
		hboxBottom.getChildren().add(lblTweakLog);
		hboxBottom.getChildren().add(springRight);
		hboxBottom.getChildren().add(lblVmVersion);

		borderPane.setTop(hboxTop);
		borderPane.setCenter(spMain);
		borderPane.setBottom(hboxBottom);

		stage.setTitle("JITWatch - HotSpot Compilation Inspector");
		stage.setScene(scene);
		stage.show();

		int refreshMillis = 1000;

		final Duration oneFrameAmt = Duration.millis(refreshMillis);

		final KeyFrame oneFrame = new KeyFrame(oneFrameAmt, new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent arg0)
			{
				refresh();
			}
		});

		Timeline timeline = new Timeline();
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.getKeyFrames().add(oneFrame);
		timeline.play();

		updateButtons();
	}

	void openConfigStage()
	{
		if (configStage == null)
		{
			configStage = new MainConfigStage(this, this, getConfig());

			StageManager.addAndShow(this.stage, configStage);

			btnConfigure.setDisable(true);
		}
	}

	@Override
	public void openTriView(IMetaMember member, boolean force)
	{
		openTriView(member, force, 0);
	}
	
	@Override
	public void openTriView(IMetaMember member, boolean force, int highlightBCI)
	{
		if (triViewStage == null)
		{
			triViewStage = new TriView(JITWatchUI.this, getConfig());

			StageManager.addAndShow(this.stage, triViewStage);

			btnTriView.setDisable(true);
		}

		if (member != null)
		{
			triViewStage.setMember(member, force, highlightBCI);
		}
	}

	public void openSandbox()
	{
		if (sandBoxStage == null)
		{
			sandBoxStage = new SandboxStage(this, this, logParser);

			StageManager.addAndShow(this.stage, sandBoxStage);

			btnSandbox.setDisable(true);
		}
	}

	@Override
	public void openBrowser(String title, String html, String stylesheet)
	{
		if (browserStage == null)
		{
			browserStage = new BrowserStage();

			StageManager.addAndShow(this.stage, browserStage);
		}

		browserStage.setContent(title, html, stylesheet);
	}

	public IReadOnlyJITDataModel getJITDataModel()
	{
		return logParser.getModel();
	}

	private void updateButtons()
	{
		btnStart.setDisable(hsLogFile == null || isReadingLogFile);
		btnStop.setDisable(!isReadingLogFile);

		btnReportSuggestions.setText("Suggestions (" + reportListSuggestions.size() + S_CLOSE_PARENTHESES);
		btnReportEliminatedAllocations.setText("-Allocs (" + reportListEliminatedAllocations.size() + S_CLOSE_PARENTHESES);
		btnReportElidedLocks.setText("-Locks (" + reportListElidedLocks.size() + S_CLOSE_PARENTHESES);
	}

	public boolean focusTreeOnClass(MetaClass metaClass)
	{
		List<String> path = metaClass.getTreePath();

		clearAndRefreshTreeView();

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

				if (matching.equals(nodeText) || (S_EMPTY.equals(matching) && DEFAULT_PACKAGE_NAME.equals(nodeText)))
				{
					builtPath.append(C_DOT);
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
			lastSelectedClass = null;
		}

		return found;
	}

	public void focusTreeOnMember(IMetaMember member, boolean openTriView)
	{
		if (member != null)
		{
			MetaClass metaClass = member.getMetaClass();

			boolean found = focusTreeOnClass(metaClass);

			if (found)
			{
				classMemberList.selectMember(member);

				setSelectedMetaMember(member, openTriView);

				lastSelectedMember = null;
			}
		}
	}

	@Override
	public void openTextViewer(String title, String content, boolean lineNumbers, boolean highlighting)
	{
		TextViewerStage tvs = new TextViewerStage(this, title, content, lineNumbers, highlighting);
		StageManager.addAndShow(this.stage, tvs);
	}

	public void openTextViewer(String title, String content)
	{
		openTextViewer(title, content, false, false);
	}

	@Override
	public void openCompileChain(IMetaMember member)
	{
		if (member != null && member.isCompiled())
		{
			CompileChainWalker walker = new CompileChainWalker(logParser.getModel());

			CompileNode root = walker.buildCallTree(member.getSelectedCompilation());

			if (root != null)
			{
				CompileChainStage ccs = new CompileChainStage(this, root);

				StageManager.addAndShow(this.stage, ccs);
			}
			else
			{
				logger.error("Could not open CompileChain - root node was null");
			}
		}
	}

	@Override
	public void openOptmizedVCallReport(IMetaMember member)
	{
		if (member.isCompiled())
		{
			OptimizedVirtualCallFinder finder = new OptimizedVirtualCallFinder(logParser.getModel(),
					getConfig().getAllClassLocations());

			List<OptimizedVirtualCall> optimizedVirtualCalls = finder.findOptimizedCalls(member);

			OptimizedVirtualCallStage ovcs = new OptimizedVirtualCallStage(this, optimizedVirtualCalls);

			StageManager.addAndShow(this.stage, ovcs);

		}
	}

	@Override
	public void openInlinedIntoReport(IMetaMember member)
	{
		if (member != null)
		{
			log("Finding inlined into reports for " + member.toStringUnqualifiedMethodName(true, true));

			InliningWalker walker = new InliningWalker(logParser.getModel(), member);

			List<Report> inlinedIntoMemberList = walker.getReports(new ScoreComparator());

			log("Found " + inlinedIntoMemberList.size() + " locations.");

			ReportStage inlinedIntoStage = new ReportStage(JITWatchUI.this,
					"Inlining report for callee " + member.toStringUnqualifiedMethodName(true, true), ReportStageType.INLINING,
					inlinedIntoMemberList);
			
			StageManager.addAndShow(JITWatchUI.this.stage, inlinedIntoStage);
		}
	}

	public void openJournalViewer(String title, IMetaMember member)
	{
		if (member.isCompiled())
		{
			JournalViewerStage jvs = new JournalViewerStage(this, title, member.getSelectedCompilation());
			StageManager.addAndShow(this.stage, jvs);
		}
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
			setHotSpotLogFile(result);

			JITWatchConfig config = getConfig();

			if (JITWatchConstants.S_PROFILE_SANDBOX.equals(config.getProfileName()))
			{
				logParser.getConfig().switchFromSandbox();
			}
		}
	}

	// Call from UI thread
	private void setHotSpotLogFile(File logFile)
	{
		hsLogFile = logFile;

		getConfig().setLastLogDir(hsLogFile.getParent());
		getConfig().saveConfig();

		clearTextArea();
		log("Selected log file: " + hsLogFile.getAbsolutePath());

		log("\nUsing Config: " + getConfig().getProfileName());

		log("\nClick Start button to process the HotSpot log");
		updateButtons();

		refreshLog();
	}

	private boolean sameVmCommand()
	{
		boolean same = false;

		if (lastVmCommand != null && logParser.getVMCommand() != null)
		{
			same = lastVmCommand.equals(logParser.getVMCommand());

			if (!same)
			{
				// vm command known and not same so flush open node history
				classTree.clearOpenPackageHistory();
				lastVmCommand = null;
			}
		}

		return same;
	}

	@Override
	public void setSelectedMetaMember(IMetaMember member, boolean openTriView)
	{
		compilationRowList.clear();

		if (member == null)
		{
			return;
		}

		if (openTriView && triViewStage != null)
		{
			triViewStage.setMember(member, false);
		}

		selectedMember = member;

		for (Compilation compilation : member.getCompilations())
		{
			CompilationTableRow row = new CompilationTableRow(compilation);

			compilationRowList.add(row);
		}

		Compilation selectedCompilation = selectedMember.getSelectedCompilation();

		if (selectedCompilation != null)
		{
			compilationTable.getSelectionModel().clearAndSelect(selectedCompilation.getIndex());
		}

		refreshOnce();
	}

	public void setCompilationOnSelectedMember(IMetaMember member, int compilationIndex)
	{
		selectedProgrammatically = true;

		selectedMember = member;

		if (selectedMember != null)
		{
			selectedMember.setSelectedCompilation(compilationIndex);

			if (selectedMember.getSelectedCompilation() != null)
			{
				compilationTable.getSelectionModel().clearAndSelect(selectedMember.getSelectedCompilation().getIndex());
			}

			focusTreeOnMember(selectedMember, true);
		}

		selectedProgrammatically = false;
	}

	private void refreshOnce()
	{
		if (codeCacheBlocksStage != null)
		{
			codeCacheBlocksStage.redraw();
		}
	}

	private void refresh()
	{
		boolean sameVmCommandAsLastRun = sameVmCommand();

		if (repaintTree)
		{
			repaintTree = false;
			classTree.showTree(sameVmCommandAsLastRun);
		}

		if (sameVmCommandAsLastRun)
		{
			if (lastSelectedMember != null)
			{
				focusTreeOnMember(lastSelectedMember, true);
			}
			else if (lastSelectedClass != null)
			{
				focusTreeOnClass(lastSelectedClass);
			}
		}

		if (timeLineStage != null)
		{
			timeLineStage.redraw();
		}

		if (codeCacheTimelineStage != null)
		{
			codeCacheTimelineStage.redraw();
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

		checkIfTweakLog();
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

	void clearAndRefreshTreeView()
	{
		selectedMember = null;
		selectedMetaClass = null;

		classTree.clear();
		classTree.showTree(sameVmCommand());
	}

	@Override
	public void handleStageClosed(Stage stage)
	{
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
			btnCodeCacheTimeline.setDisable(false);
			codeCacheTimelineStage = null;
		}
		else if (stage instanceof CodeCacheLayoutStage)
		{
			btnNMethods.setDisable(false);
			codeCacheBlocksStage = null;
		}
		else if (stage instanceof TriView)
		{
			btnTriView.setDisable(false);
			triViewStage = null;
		}
		else if (stage instanceof ReportStage)
		{
			switch (((ReportStage) stage).getType())
			{
			case SUGGESTION:
				btnReportSuggestions.setDisable(false);
				reportStageSuggestions = null;
				break;
			case ELIMINATED_ALLOCATION:
				btnReportEliminatedAllocations.setDisable(false);
				reportStageElminatedAllocations = null;
				break;
			case ELIDED_LOCK:
				btnReportElidedLocks.setDisable(false);
				reportStageElidedLocks = null;
				break;
			case INLINING:
				break;
			}
		}
		else if (stage instanceof OptimizedVirtualCallStage)
		{
			btnOptimizedVirtualCalls.setDisable(false);
			ovcStage = null;
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
		errorLog.addEntry(entry);
		errorCount++;
	}

	private void log(final String entry)
	{
		logBuffer.append(entry);
		logBuffer.append(S_NEWLINE);
	}

	void metaClassSelectedFromClassTree(MetaClass metaClass)
	{
		classMemberList.clearClassMembers();
		selectedMetaClass = metaClass;

		setSelectedMetaMember(null, true);

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

	// TOOD remove
	private void checkIfTweakLog()
	{
		if (logParser != null && logParser.isTweakVMLog())
		{
			lblTweakLog.setText("TweakVM log detected! Enabling extra features.");
		}
		else
		{
			lblTweakLog.setText(S_EMPTY);
		}
	}
}