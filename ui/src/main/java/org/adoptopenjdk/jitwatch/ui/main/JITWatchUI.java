/*
 * Copyright (c) 2013-2021 Chris Newland.
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
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_OPEN_PARENTHESES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_SLASH;
import static org.adoptopenjdk.jitwatch.util.UserInterfaceUtil.FONT_MONOSPACE_FAMILY;
import static org.adoptopenjdk.jitwatch.util.UserInterfaceUtil.FONT_MONOSPACE_SIZE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.adoptopenjdk.jitwatch.compilation.codecache.CodeCacheEventWalker;
import org.adoptopenjdk.jitwatch.compilation.codecache.CodeCacheWalkerResult;
import org.adoptopenjdk.jitwatch.core.ErrorLog;
import org.adoptopenjdk.jitwatch.core.IJITListener;
import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.core.JITWatchConstants;
import org.adoptopenjdk.jitwatch.model.*;
import org.adoptopenjdk.jitwatch.parser.ILogParseErrorListener;
import org.adoptopenjdk.jitwatch.parser.ILogParser;
import org.adoptopenjdk.jitwatch.parser.ParserFactory;
import org.adoptopenjdk.jitwatch.parser.ParserType;
import org.adoptopenjdk.jitwatch.parser.hotspot.HotSpotLogParser;
import org.adoptopenjdk.jitwatch.report.Report;
import org.adoptopenjdk.jitwatch.report.comparator.ScoreComparator;
import org.adoptopenjdk.jitwatch.report.escapeanalysis.eliminatedallocation.EliminatedAllocationWalker;
import org.adoptopenjdk.jitwatch.report.inlining.InliningWalker;
import org.adoptopenjdk.jitwatch.report.locks.OptimisedLocksWalker;
import org.adoptopenjdk.jitwatch.report.suggestion.SuggestionWalker;
import org.adoptopenjdk.jitwatch.ui.Dialogs;
import org.adoptopenjdk.jitwatch.ui.browser.BrowserStage;
import org.adoptopenjdk.jitwatch.ui.compilechain.CompileChainStage;
import org.adoptopenjdk.jitwatch.ui.graphing.CodeCacheStage;
import org.adoptopenjdk.jitwatch.ui.graphing.HistoStage;
import org.adoptopenjdk.jitwatch.ui.graphing.TimeLineStage;
import org.adoptopenjdk.jitwatch.ui.nmethod.codecache.CodeCacheLayoutStage;
import org.adoptopenjdk.jitwatch.ui.nmethod.compilerthread.CompilerThreadStage;
import org.adoptopenjdk.jitwatch.ui.parserchooser.IParserSelectedListener;
import org.adoptopenjdk.jitwatch.ui.parserchooser.ParserChooser;
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
import org.adoptopenjdk.jitwatch.util.LocaleCell;
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
import javafx.scene.control.ComboBox;
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
		implements IJITListener, ILogParseErrorListener, IStageClosedListener, IStageAccessProxy, IMemberSelectedListener,
		IParserSelectedListener
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

	private File jitLogFile = null;

	private String lastVmCommand = null;
	private IMetaMember lastSelectedMember = null;
	private MetaClass lastSelectedClass = null;

	private String focusMemberFromProperty = null;

	private static final String PROPERTY_LOGFILE = "jitwatch.logfile";
	private static final String PROPERTY_FOCUS_MEMBER = "jitwatch.focus.member";

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
	private Button btnCompilerThreads;
	private Button btnTriView;
	private Button btnReportSuggestions;
	private Button btnReportEliminatedAllocations;
	private Button btnReportOptimisedLocks;
	private Button btnSandbox;

	private Label lblHeap;

	private MainConfigStage configStage;
	private TimeLineStage timeLineStage;
	private StatsStage statsStage;
	private HistoStage histoStage;
	private TopListStage topListStage;
	private CodeCacheStage codeCacheTimelineStage;
	private CodeCacheLayoutStage codeCacheBlocksStage;
	private CompilerThreadStage compilerThreadStage;
	private TriView triViewStage;
	private BrowserStage browserStage;

	private ReportStage reportStageSuggestions;
	private ReportStage reportStageElminatedAllocations;
	private ReportStage reportStageOptimisedLocks;

	private SandboxStage sandBoxStage;

	private NothingMountedStage nothingMountedStage;

	private IMetaMember selectedMember;
	private MetaClass selectedMetaClass;

	private List<Report> reportListSuggestions = new ArrayList<>();
	private List<Report> reportListEliminatedAllocations = new ArrayList<>();
	private List<Report> reportListOptimisedLocks = new ArrayList<>();

	private CodeCacheWalkerResult codeCacheWalkerResult;

	private Runtime runtime = Runtime.getRuntime();

	// synchronized as buffer is drained async on GUI thread
	private StringBuffer logBuffer = new StringBuffer();

	private ErrorLog errorLog = new ErrorLog();
	private int errorCount = 0;

	private boolean repaintTree = false;
	private boolean startDelayedByConfig = false;

	private ParserChooser parserChooser;

	private StringProperty suggestionsCounterMessage = new SimpleStringProperty(null);
	private StringProperty eliminatedAllocationsCounterMessage = new SimpleStringProperty(null);
	private StringProperty eliminatedLocksCounterMessage = new SimpleStringProperty(null);

	// Called by JFX
	public JITWatchUI()
	{
	}

	public JITWatchUI(String[] args)
	{
		launch(args);
	}

	private void readLogFile()
	{
		Thread jwThread = new Thread(new Runnable()
		{
			@Override public void run()
			{
				try
				{
					logParser.processLogFile(jitLogFile, JITWatchUI.this);
				}
				catch (IOException ioe)
				{
					log("Exception during log processing: " + ioe.toString());
				}
			}
		});

		jwThread.start();
	}

	@Override public void handleReadStart()
	{
		startDelayedByConfig = false;

		isReadingLogFile = true;

		clear();

		Platform.runLater(new Runnable()
		{
			@Override public void run()
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
		codeCacheWalkerResult = null;

		errorCount = 0;
		errorLog.clear();

		reportListSuggestions.clear();
		reportListEliminatedAllocations.clear();
		reportListOptimisedLocks.clear();

		Platform.runLater(new Runnable()
		{
			@Override public void run()
			{
				classMemberList.clear();

				StageManager.clearReportStages();

				if (triViewStage != null)
				{
					triViewStage.clear();
				}

				classTree.handleConfigUpdate(getConfig());

				updateButtons();

				classTree.clear();
				metaClassSelectedFromClassTree(null);

				textAreaLog.clear();

				StageManager.notifyCompilationChanged(null);
			}
		});
	}

	@Override public void handleReadComplete()
	{
		log("Finished reading log file.");

		isReadingLogFile = false;

		buildSuggestions();

		buildEliminatedAllocationReport();

		buildOptimisedLocksReport();

		Platform.runLater(new Runnable()
		{
			@Override public void run()
			{
				updateButtons();

				StageManager.notifyCompilationChanged(selectedMember);
			}
		});

		logParser.discardParsedLogs();

		if (focusMemberFromProperty != null)
		{
			try
			{
				MemberSignatureParts msp = MemberSignatureParts.fromLogCompilationSignature(focusMemberFromProperty);

				final IMetaMember member = getJITDataModel().findMetaMember(msp);

				if (member != null)
				{

					Platform.runLater(new Runnable()
					{
						@Override public void run()
						{
							focusTreeOnMember(member);
						}
					});
				}
			}
			catch (LogParseException lpe)
			{
				log("Could not parse member signature from property " + PROPERTY_FOCUS_MEMBER + ": " + focusMemberFromProperty);
				log("Property must be in LogCompilation signature format: java/lang/String indexOf (II)I");
			}
		}
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

	private void buildOptimisedLocksReport()
	{
		log("Finding optimised locks");

		OptimisedLocksWalker walker = new OptimisedLocksWalker(logParser.getModel());

		reportListOptimisedLocks = walker.getReports(new ScoreComparator());

		log("Found " + reportListOptimisedLocks.size() + " optimised locks.");
	}

	private CodeCacheWalkerResult buildCodeCacheResult()
	{
		CodeCacheEventWalker compilationWalker = new CodeCacheEventWalker(logParser.getModel());

		compilationWalker.walkCompilations();

		return compilationWalker.getResult();
	}

	public CodeCacheWalkerResult getCodeCacheWalkerResult()
	{
		if (codeCacheWalkerResult == null || codeCacheWalkerResult.getEvents().isEmpty())
		{
			codeCacheWalkerResult = buildCodeCacheResult();
		}

		return codeCacheWalkerResult;
	}

	@Override public void handleError(final String title, final String body)
	{
		logger.error(title);

		Platform.runLater(new Runnable()
		{
			@Override public void run()
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

			if (jitLogFile != null)
			{
				log("Stopped parsing " + jitLogFile.getAbsolutePath());
			}
		}
	}

	@Override public JITWatchConfig getConfig()
	{
		return logParser.getConfig();
	}

	@Override public void start(final Stage stage)
	{
		StageManager.registerStageClosedListener(this);

		this.stage = stage;

		stage.setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override public void handle(WindowEvent arg0)
			{
				StageManager.closeStage(stage);

				stopParsing();
			}
		});

		BorderPane borderPane = new BorderPane();

		Scene scene = UserInterfaceUtil.getScene(borderPane, WINDOW_WIDTH, WINDOW_HEIGHT);

		parserChooser = new ParserChooser(this);

		ComboBox<ParserType> comboParser = parserChooser.getCombo();

		String parserProperty = System.getProperty("jitwatch.parser", ParserType.HOTSPOT.toString());

		comboParser.getSelectionModel().select(ParserType.fromString(parserProperty));

		Button btnChooseWatchFile = UserInterfaceUtil.createButton("OPEN_LOG");
		btnChooseWatchFile.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override public void handle(ActionEvent e)
			{
				stopParsing();
				chooseJITLog();
			}
		});

		btnStart = UserInterfaceUtil.createButton("START");

		btnStart.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override public void handle(ActionEvent e)
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

		btnStop = UserInterfaceUtil.createButton("STOP");
		btnStop.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override public void handle(ActionEvent e)
			{
				stopParsing();
			}
		});

		btnConfigure = UserInterfaceUtil.createButton("CONFIG");
		btnConfigure.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override public void handle(ActionEvent e)
			{
				openConfigStage();
			}
		});

		btnTimeLine = UserInterfaceUtil.createButton("TIMELINE");
		btnTimeLine.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override public void handle(ActionEvent e)
			{
				timeLineStage = new TimeLineStage(JITWatchUI.this);

				StageManager.addAndShow(JITWatchUI.this.stage, timeLineStage);

				btnTimeLine.setDisable(true);
			}
		});

		btnHisto = UserInterfaceUtil.createButton("HISTO");
		btnHisto.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override public void handle(ActionEvent e)
			{
				histoStage = new HistoStage(JITWatchUI.this);

				StageManager.addAndShow(JITWatchUI.this.stage, histoStage);

				btnHisto.setDisable(true);
			}
		});

		btnTopList = UserInterfaceUtil.createButton("TOPLIST");
		btnTopList.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override public void handle(ActionEvent e)
			{
				topListStage = new TopListStage(JITWatchUI.this, getJITDataModel());

				StageManager.addAndShow(JITWatchUI.this.stage, topListStage);

				btnTopList.setDisable(true);
			}
		});

		btnCodeCacheTimeline = UserInterfaceUtil.createButton("CACHE");
		btnCodeCacheTimeline.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override public void handle(ActionEvent e)
			{
				codeCacheTimelineStage = new CodeCacheStage(JITWatchUI.this);

				StageManager.addAndShow(JITWatchUI.this.stage, codeCacheTimelineStage);

				btnCodeCacheTimeline.setDisable(true);
			}
		});

		btnNMethods = UserInterfaceUtil.createButton("NMETHODS");
		btnNMethods.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override public void handle(ActionEvent e)
			{
				codeCacheBlocksStage = new CodeCacheLayoutStage(JITWatchUI.this);

				StageManager.addAndShow(JITWatchUI.this.stage, codeCacheBlocksStage);

				btnNMethods.setDisable(true);

				codeCacheBlocksStage.redraw();

			}
		});

		btnCompilerThreads = UserInterfaceUtil.createButton("THREADS");
		btnCompilerThreads.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override public void handle(ActionEvent e)
			{
				compilerThreadStage = new CompilerThreadStage(JITWatchUI.this);

				StageManager.addAndShow(JITWatchUI.this.stage, compilerThreadStage);

				btnCompilerThreads.setDisable(true);

				compilerThreadStage.redraw();

			}
		});

		btnTriView = UserInterfaceUtil.createButton("TRIVIEW");
		btnTriView.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override public void handle(ActionEvent e)
			{
				if (selectedMember == null && selectedMetaClass != null)
				{
					selectedMember = selectedMetaClass.getFirstConstructor();
				}

				openTriView(selectedMember);
			}
		});

		btnReportSuggestions = UserInterfaceUtil.createButton("SUGGEST", suggestionsCounterMessage);
		btnReportSuggestions.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override public void handle(ActionEvent e)
			{
				reportStageSuggestions = new ReportStage(JITWatchUI.this, "JITWatch Code Suggestions", ReportStageType.SUGGESTION,
						reportListSuggestions);

				StageManager.addAndShow(JITWatchUI.this.stage, reportStageSuggestions);

				btnReportSuggestions.setDisable(true);
			}
		});

		btnReportEliminatedAllocations = UserInterfaceUtil.createButton("ELIM_ALLOCS", eliminatedAllocationsCounterMessage);
		btnReportEliminatedAllocations.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override public void handle(ActionEvent e)
			{
				reportStageElminatedAllocations = new ReportStage(JITWatchUI.this, "JITWatch Eliminated Allocation Report",
						ReportStageType.ELIMINATED_ALLOCATION, reportListEliminatedAllocations);

				StageManager.addAndShow(JITWatchUI.this.stage, reportStageElminatedAllocations);

				btnReportEliminatedAllocations.setDisable(true);
			}
		});

		btnReportOptimisedLocks = UserInterfaceUtil.createButton("ELIM_LOCKS", eliminatedLocksCounterMessage);
		btnReportOptimisedLocks.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override public void handle(ActionEvent e)
			{
				reportStageOptimisedLocks = new ReportStage(JITWatchUI.this, "JITWatch Optimised Lock Report",
						ReportStageType.ELIDED_LOCK, reportListOptimisedLocks);

				StageManager.addAndShow(JITWatchUI.this.stage, reportStageOptimisedLocks);

				btnReportOptimisedLocks.setDisable(true);
			}
		});

		btnSandbox = UserInterfaceUtil.createButton("SANDBOX");
		btnSandbox.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override public void handle(ActionEvent e)
			{
				openSandbox();
			}
		});

		btnErrorLog = new Button("Errors (0)");
		btnErrorLog.setStyle("-fx-padding: 2 6;");
		btnErrorLog.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override public void handle(ActionEvent e)
			{
				openTextViewer("Error Log", errorLog.toString(), false, false);
			}
		});

		btnStats =  UserInterfaceUtil.createButton("STATS");
		btnStats.setStyle("-fx-padding: 2 6;");
		btnStats.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override public void handle(ActionEvent e)
			{
				statsStage = new StatsStage(JITWatchUI.this);

				StageManager.addAndShow(JITWatchUI.this.stage, statsStage);

				btnStats.setDisable(true);
			}
		});

		btnReset =  UserInterfaceUtil.createButton("RESET");
		btnReset.setStyle("-fx-padding: 2 6;");
		btnReset.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override public void handle(ActionEvent e)
			{
				logParser.reset();
				clear();
			}
		});

		lblHeap = new Label();

		lblVmVersion = new Label();

		StringBuilder vmBuilder = new StringBuilder();

		vmBuilder.append("VM: ");
		vmBuilder.append(System.getProperty("java.vendor"));
		vmBuilder.append(" JDK");
		vmBuilder.append(System.getProperty("java.version"));
		vmBuilder.append(" build ");
		vmBuilder.append(System.getProperty("java.runtime.version"));

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
		hboxTop.getChildren().add(btnCompilerThreads);
		hboxTop.getChildren().add(btnTriView);
		hboxTop.getChildren().add(btnReportSuggestions);
		hboxTop.getChildren().add(btnReportEliminatedAllocations);
		hboxTop.getChildren().add(btnReportOptimisedLocks);

		compilationRowList = FXCollections.observableArrayList();
		compilationTable = CompilationTableBuilder.buildTableMemberAttributes(compilationRowList);
		compilationTable.setPlaceholder(new Text("Select a JIT-compiled class member to view compilations."));

		compilationTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<CompilationTableRow>()
		{
			@Override public void changed(ObservableValue<? extends CompilationTableRow> arg0, CompilationTableRow oldVal,
					CompilationTableRow newVal)
			{
				if (!selectedProgrammatically)
				{
					if (selectedMember != null && newVal != null)
					{
						selectedMember.setSelectedCompilation(newVal.getIndex());

						setSelectedMetaMemberFromCompilationTable();
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

		String logFileFromProperty = System.getProperty(PROPERTY_LOGFILE);

		if (logFileFromProperty != null)
		{
			File tempLogFile = new File(logFileFromProperty);

			if (tempLogFile.exists() && tempLogFile.isFile())
			{
				jitLogFile = tempLogFile;

				log("Setting JIT log file from property " + PROPERTY_LOGFILE + ": " + logFileFromProperty);

				Platform.runLater(new Runnable()
				{
					@Override public void run()
					{
						readLogFile();

						focusMemberFromProperty = System.getProperty(PROPERTY_FOCUS_MEMBER);

						if (focusMemberFromProperty != null)
						{
							log("Focusing on member from property " + PROPERTY_FOCUS_MEMBER + ": " + focusMemberFromProperty);
						}
					}
				});
			}
		}

		if (jitLogFile == null)
		{
			log("Choose a JIT log file or open the Sandbox");
		}
		else
		{
			log("Using JIT log file: " + jitLogFile.getAbsolutePath());
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

		Label labelParser = new Label("Select Parser");
		labelParser.setStyle(labelStyle);

		Label labelLanguage = new Label("Language");
		labelLanguage.setStyle(labelStyle);

		ComboBox<Locale> comboLanguage = new ComboBox<>();
		comboLanguage.getItems().addAll(Locale.ENGLISH, Locale.GERMAN, Locale.FRENCH, Locale.SIMPLIFIED_CHINESE, Locale.forLanguageTag("pl"), Locale.forLanguageTag("es"));
		comboLanguage.setValue(Locale.ENGLISH);
		comboLanguage.setCellFactory(lv -> new LocaleCell());
		comboLanguage.setButtonCell(new LocaleCell());

		comboLanguage.valueProperty().addListener((obs, oldValue, newValue) -> {
			if (newValue != null) {
				UserInterfaceUtil.configureLocale(newValue);
			}
		});

		Button buttonSnapShot = UserInterfaceUtil.getSnapshotButton(scene, "JITWatch");

		hboxBottom.setPadding(new Insets(4));
		hboxBottom.setPrefHeight(statusBarHeight);
		hboxBottom.setSpacing(4);

		hboxBottom.getChildren().add(labelLanguage);
		hboxBottom.getChildren().add(comboLanguage);

		hboxBottom.getChildren().add(labelParser);
		hboxBottom.getChildren().add(comboParser);

		hboxBottom.getChildren().add(btnErrorLog);
		hboxBottom.getChildren().add(btnStats);
		hboxBottom.getChildren().add(btnReset);
		hboxBottom.getChildren().add(lblHeap);
		hboxBottom.getChildren().add(springLeft);

		hboxBottom.getChildren().add(lblTweakLog);
		hboxBottom.getChildren().add(springRight);
		hboxBottom.getChildren().add(lblVmVersion);
		hboxBottom.getChildren().add(buttonSnapShot);

		borderPane.setTop(hboxTop);
		borderPane.setCenter(spMain);
		borderPane.setBottom(hboxBottom);

		stage.setTitle("JITWatch - Just In Time Compilation Inspector");
		stage.setScene(scene);
		stage.show();

		int refreshMillis = 1000;

		final Duration oneFrameAmt = Duration.millis(refreshMillis);

		final KeyFrame oneFrame = new KeyFrame(oneFrameAmt, new EventHandler<ActionEvent>()
		{
			@Override public void handle(ActionEvent arg0)
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

	@Override public void openTriView(IMetaMember member)
	{
		openTriView(member, 0);
	}

	@Override public void openTriView(IMetaMember member, int highlightBCI)
	{
		openTriView();

		triViewStage.setNextHighlightBCI(highlightBCI);

		selectMember(member, true, false);

		if (member != null)
		{
			triViewStage.setMember(member, true, highlightBCI);
		}
	}

	private void openTriView()
	{
		if (triViewStage == null)
		{
			triViewStage = new TriView(JITWatchUI.this, getConfig());

			StageManager.addAndShow(this.stage, triViewStage);

			btnTriView.setDisable(true);
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

	@Override public void openBrowser(String title, String html, String stylesheet)
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
		if (!(logParser instanceof HotSpotLogParser))
		{
			btnSandbox.setDisable(true);
		}

		btnStart.setDisable(jitLogFile == null || isReadingLogFile);
		btnStop.setDisable(!isReadingLogFile);

		if (reportListSuggestions.size() != 0) {
			suggestionsCounterMessage.setValue(S_OPEN_PARENTHESES + reportListSuggestions.size() + S_CLOSE_PARENTHESES);
		}

		if (reportListEliminatedAllocations.size() != 0) {
			eliminatedAllocationsCounterMessage.setValue(S_OPEN_PARENTHESES + reportListEliminatedAllocations.size() + S_CLOSE_PARENTHESES);
		}

		if (reportListOptimisedLocks.size() != 0) {
			eliminatedLocksCounterMessage.setValue(S_OPEN_PARENTHESES + reportListOptimisedLocks.size() + S_CLOSE_PARENTHESES);
		}
	}

	public boolean focusTreeOnClass(MetaClass metaClass, boolean unsetSelection)
	{
		List<String> path = metaClass.getTreePath();

		clearAndRefreshTreeView(unsetSelection);

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

	public void focusTreeOnMember(IMetaMember member)
	{
		if (member != null)
		{
			MetaClass metaClass = member.getMetaClass();

			boolean found = focusTreeOnClass(metaClass, true);

			if (found)
			{
				classMemberList.selectMember(member);

				selectMember(member, false, true);

				lastSelectedMember = null;
			}
		}
	}

	public void focusTreeInternal(IMetaMember member)
	{
		if (member != null)
		{
			MetaClass metaClass = member.getMetaClass();

			boolean found = focusTreeOnClass(metaClass, false);

			if (found)
			{
				classMemberList.clearClassMembers();

				selectedMetaClass = metaClass;

				classMemberList.setMetaClass(metaClass);

				classMemberList.selectMember(member);

				lastSelectedMember = null;
			}
			else
			{
				log("Could not focus tree on " + member.toStringUnqualifiedMethodName(false, true));

				if (classTree.isHidingClassesWithNoCompiledMethods())
				{
					log("Perhaps this class doesn't contain any compiled methods and 'Hide uncompiled classes' is selected");
				}

				classMemberList.clearClassMembers();

				classMemberList.setMetaClass(null);

				classMemberList.selectMember(null);
			}
		}
	}

	@Override public void openTextViewer(String title, String content, boolean lineNumbers, boolean highlighting)
	{
		TextViewerStage tvs = new TextViewerStage(this, title, content, lineNumbers, highlighting);
		StageManager.addAndShow(this.stage, tvs);
	}

	public void openTextViewer(String title, String content)
	{
		openTextViewer(title, content, false, false);
	}

	@Override public void openCompileChain(IMetaMember member)
	{
		if (member != null && member.isCompiled())
		{
			CompileChainStage compileChainStage = new CompileChainStage(this, this, logParser.getModel());

			compileChainStage.compilationChanged(member);

			StageManager.addAndShow(this.stage, compileChainStage);
		}
	}

	@Override public void openInlinedIntoReport(IMetaMember member)
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

	private void chooseJITLog()
	{
		FileChooser fc = new FileChooser();
		fc.setTitle("Choose JIT log file");

		String osNameProperty = System.getProperty("os.name");

		// don't use ExtensionFilter on OSX due to JavaFX2 missing combo bug
		if (osNameProperty != null && !osNameProperty.toLowerCase().contains("mac"))
		{
			fc.getExtensionFilters()
			  .addAll(new FileChooser.ExtensionFilter("Log Files", "*.log"), new FileChooser.ExtensionFilter("All Files", "*.*"));
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
			setJITLogFile(result);

			JITWatchConfig config = getConfig();

			if (JITWatchConstants.S_PROFILE_SANDBOX.equals(config.getProfileName()))
			{
				logParser.getConfig().switchFromSandbox();
			}
		}
	}

	// Call from UI thread
	private void setJITLogFile(File logFile)
	{
		jitLogFile = logFile;

		getConfig().setLastLogDir(jitLogFile.getParent());
		getConfig().saveConfig();

		clearTextArea();
		log("Selected log file: " + jitLogFile.getAbsolutePath());

		log("\nUsing Config: " + getConfig().getProfileName());

		log("\nClick Start button to process the JIT log");
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

	private void setSelectedMetaMemberFromCompilationTable()
	{
		StageManager.notifyCompilationChanged(selectedMember);
	}

	@Override public synchronized void selectMember(IMetaMember member, boolean updateTree, boolean updateTriView)
	{
		// TimerUtil.timerStart(getClass().getName() + ".selectMember()");

		selectedProgrammatically = true;

		selectedMember = member;

		compilationRowList.clear();

		if (selectedMember != null)
		{
			StageManager.notifyCompilationChanged(selectedMember);

			if (updateTree)
			{
				focusTreeInternal(selectedMember);
			}

			if (updateTriView)
			{
				openTriView();

				triViewStage.setMember(selectedMember, true);
			}

			for (Compilation compilation : selectedMember.getCompilations())
			{
				CompilationTableRow row = new CompilationTableRow(compilation);

				compilationRowList.add(row);
			}

			Compilation selectedCompilation = selectedMember.getSelectedCompilation();

			if (selectedCompilation != null)
			{
				compilationTable.getSelectionModel().clearAndSelect(selectedCompilation.getIndex());
			}
		}

		selectedProgrammatically = false;

		// TimerUtil.timerEnd(getClass().getName() + ".selectMember()");
	}

	@Override public synchronized void selectCompilation(IMetaMember member, int compilationIndex)
	{
		selectedProgrammatically = true;

		selectedMember = member;

		if (selectedMember != null)
		{
			selectedMember.setSelectedCompilation(compilationIndex);

			selectMember(selectedMember, true, true);
		}

		selectedProgrammatically = false;
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
				focusTreeOnMember(lastSelectedMember);
			}
			else if (lastSelectedClass != null)
			{
				focusTreeOnClass(lastSelectedClass, true);
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

	void clearAndRefreshTreeView(boolean unsetSelection)
	{
		if (unsetSelection)
		{
			selectedMember = null;
			selectedMetaClass = null;
		}

		classTree.clear();
		classTree.showTree(sameVmCommand());
	}

	@Override public void handleStageClosed(Stage stage)
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
		else if (stage instanceof CompilerThreadStage)
		{
			btnCompilerThreads.setDisable(false);
			compilerThreadStage = null;
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
				btnReportOptimisedLocks.setDisable(false);
				reportStageOptimisedLocks = null;
				break;
			case INLINING:
				break;
			}
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

	@Override public void handleJITEvent(JITEvent event)
	{
		log(event.toString());
		repaintTree = true;
	}

	@Override public void handleLogEntry(String entry)
	{
		log(entry);
	}

	@Override public void handleErrorEntry(String entry)
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

		selectMember(null, false, true);

		classMemberList.setMetaClass(metaClass);
	}

	public PackageManager getPackageManager()
	{
		return logParser.getModel().getPackageManager();
	}

	@Override public Stage getStageForDialog()
	{
		return stage;
	}

	@Override public void parserSelected(ParserType parserType)
	{
		log("Selected Parser: " + parserType);

		if (logParser != null)
		{
			logParser.reset();
		}

		logParser = ParserFactory.getParser(parserType, this);
	}
}
