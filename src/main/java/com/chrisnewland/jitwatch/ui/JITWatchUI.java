/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/blob/master/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
package com.chrisnewland.jitwatch.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.chrisnewland.jitwatch.core.IJITListener;
import com.chrisnewland.jitwatch.core.JITEvent;
import com.chrisnewland.jitwatch.core.JITStats;
import com.chrisnewland.jitwatch.core.HotSpotLogParser;
import com.chrisnewland.jitwatch.core.JITWatchConfig;
import com.chrisnewland.jitwatch.loader.ResourceLoader;
import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.JITDataModel;
import com.chrisnewland.jitwatch.model.Journal;
import com.chrisnewland.jitwatch.model.MetaClass;
import com.chrisnewland.jitwatch.model.PackageManager;
import com.chrisnewland.jitwatch.model.Tag;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.TimelineBuilder;
import javafx.application.Application;
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
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class JITWatchUI extends Application implements IJITListener
{
    private Stage stage;

    private JITDataModel model;
    private HotSpotLogParser logParser;

    private ClassTree classTree;
    private ClassMemberList classMemberList;

    private TableView<AttributeTableRow> attributeTableView;
    private ObservableList<AttributeTableRow> memberAttrList;

    private List<Stage> openPopupStages = new ArrayList<>();

    private TextArea textAreaLog;

    private File watchFile = null;
    private boolean isWatching = false;

    private Button btnStartWatching;
    private Button btnStopWatching;
    private Button btnConfigure;
    private Button btnTimeLine;
    private Button btnStats;
    private Button btnHisto;
    private Button btnTopList;
    private Button btnErrorLog;
    private Button btnCodeCache;

    private Label lblHeap;

    private ConfigStage configStage;
    private TimeLineStage timeLineStage;
    private StatsStage statsStage;
    private HistoStage histoStage;
    private TopListStage topListStage;
    private CodeCacheStage codeCacheStage;

    private NothingMountedStage nothingMountedStage;

    private IMetaMember selectedMember;

    private Runtime runtime = Runtime.getRuntime();

    // synchronized as buffer is drained async on GUI thread
    private StringBuffer logBuffer = new StringBuffer();

    private StringBuilder errorLog = new StringBuilder();
    private int errorCount = 0;

    private boolean repaintTree = false;
    private boolean startDelayedByConfig = false;

    private JITWatchConfig config;

    // Called by JFX
    public JITWatchUI()
    {
        model = new JITDataModel();
        config = new JITWatchConfig(this);
        logParser = new HotSpotLogParser(model, config, this);
    }

    public JITWatchUI(String[] args)
    {
        launch(args);
    }

    public void setHotSpotLogFile(File file)
    {
        watchFile = file;
        updateButtons();
    }

    private void startWatching()
    {
        startDelayedByConfig = false;

        model.reset();

        textAreaLog.clear();

        log("Processing file: " + watchFile);

        selectedMember = null;

        errorCount = 0;
        errorLog.delete(0, errorLog.length());

        classTree.clear();
        refreshSelectedTreeNode(null);

        isWatching = true;

        updateButtons();

        Thread jwThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    logParser.watch(watchFile);
                }
                catch (IOException ioe)
                {
                    log("Exception during log processing: " + ioe.toString());
                }
            }
        });

        jwThread.start();
    }

    private void stopWatching()
    {
        if (isWatching)
        {
            logParser.stop();
            isWatching = false;
            updateButtons();

            log("Stopped watching " + watchFile.getAbsolutePath());
        }
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
                for (Stage s : openPopupStages)
                {
                    if (s != null)
                    {
                        s.close();
                    }
                }

                stopWatching();
            }
        });

        int width = 1024;
        int height = 592;

        BorderPane borderPane = new BorderPane();

        Scene scene = new Scene(borderPane, width, height);

        Button btnChooseWatchFile = new Button("Open Log");
        btnChooseWatchFile.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {
                chooseHotSpotFile();
            }
        });

        btnStartWatching = new Button("Start");
        btnStartWatching.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {
                if (nothingMountedStage == null)
                {
                    int classCount = config.getClassLocations().size();
                    int sourceCount = config.getSourceLocations().size();

                    if (classCount == 0 && sourceCount == 0)
                    {
                        if (config.isShowNothingMounted())
                        {
                            nothingMountedStage = new NothingMountedStage(JITWatchUI.this, config);
                            nothingMountedStage.show();

                            openPopupStages.add(nothingMountedStage);

                            startDelayedByConfig = true;
                        }
                    }
                }

                if (!startDelayedByConfig)
                {
                    startWatching();
                }
            }
        });

        btnStopWatching = new Button("Stop");
        btnStopWatching.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {
                stopWatching();
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
                timeLineStage.show();

                openPopupStages.add(timeLineStage);

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
                statsStage.show();

                openPopupStages.add(statsStage);

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
                histoStage.show();

                openPopupStages.add(histoStage);

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
                topListStage.show();

                openPopupStages.add(topListStage);

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
                codeCacheStage.show();

                openPopupStages.add(codeCacheStage);

                btnCodeCache.setDisable(true);
            }
        });

        btnErrorLog = new Button("Errors (0)");
        btnErrorLog.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {
                TextViewerStage viewer = new TextViewerStage(JITWatchUI.this, "Error Log", errorLog.toString(), false);
                viewer.show();

                openPopupStages.add(viewer);
            }
        });

        lblHeap = new Label();

        int topHeight = 50;
        int bottomHeight = 100;

        HBox hboxTop = new HBox();

        hboxTop.setPadding(new Insets(10));
        hboxTop.getChildren().add(btnChooseWatchFile);
        hboxTop.getChildren().add(btnStartWatching);
        hboxTop.getChildren().add(btnStopWatching);
        hboxTop.getChildren().add(btnConfigure);
        hboxTop.getChildren().add(btnTimeLine);
        hboxTop.getChildren().add(btnStats);
        hboxTop.getChildren().add(btnHisto);
        hboxTop.getChildren().add(btnTopList);
        hboxTop.getChildren().add(btnCodeCache);
        hboxTop.getChildren().add(btnErrorLog);
        hboxTop.getChildren().add(lblHeap);
        hboxTop.setPrefHeight(topHeight);
        hboxTop.setSpacing(10);

        memberAttrList = FXCollections.observableArrayList();
        attributeTableView = TableUtil.buildTableMemberAttributes(memberAttrList);
        attributeTableView.setPlaceholder(new Text("Select a JIT-compiled class member to view compilation attributes."));

        SplitPane spMethodInfo = new SplitPane();
        spMethodInfo.setOrientation(Orientation.VERTICAL);

        classMemberList = new ClassMemberList(this, config);

        spMethodInfo.getItems().add(classMemberList);
        spMethodInfo.getItems().add(attributeTableView);

        classMemberList.prefHeightProperty().bind(scene.heightProperty());
        attributeTableView.prefHeightProperty().bind(scene.heightProperty());

        classTree = new ClassTree(this, config);
        classTree.prefWidthProperty().bind(scene.widthProperty());

        SplitPane spMain = new SplitPane();
        spMain.setOrientation(Orientation.VERTICAL);

        SplitPane spCentre = new SplitPane();
        spCentre.getItems().add(classTree);
        spCentre.getItems().add(spMethodInfo);
        spCentre.setDividerPositions(0.33, 0.67);

        textAreaLog = new TextArea();
        textAreaLog.setStyle("-fx-font-family:monospace;");
        textAreaLog.setPrefHeight(bottomHeight);
        textAreaLog.setText("Welcome to JITWatch\n");

        if (watchFile == null)
        {
            log("Please choose a HotSpot log file");
        }
        else
        {
            log("Using HotSpot log file: " + watchFile.getAbsolutePath());
        }
        spMain.getItems().add(spCentre);
        spMain.getItems().add(textAreaLog);
        spMain.setDividerPositions(0.7, 0.3);

        borderPane.setTop(hboxTop);
        borderPane.setCenter(spMain);

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
            configStage = new ConfigStage(JITWatchUI.this, config);
            configStage.show();

            openPopupStages.add(configStage);

            btnConfigure.setDisable(true);
        }
    }

    public JITStats getJITStats()
    {
        return model.getJITStats();
    }

    private void updateButtons()
    {
        btnStartWatching.setDisable(watchFile == null || isWatching);
        btnStopWatching.setDisable(!isWatching);
    }

    public List<JITEvent> getJITEvents()
    {
        return model.getEventListCopy();
    }

    public List<Tag> getCodeCacheTags()
    {
        return model.getCodeCacheTags();
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

    void openSource(IMetaMember member)
    {
        MetaClass methodClass = member.getMetaClass();

        String fqName = methodClass.getFullyQualifiedName();

        fqName = fqName.replace(".", "/") + ".java";

        String source = ResourceLoader.getSource(config.getSourceLocations(), fqName);

        TextViewerStage tvs = null;
        String title = "Source code for " + fqName;

        for (Stage s : openPopupStages)
        {
            if (s instanceof TextViewerStage && title.equals(s.getTitle()))
            {
                tvs = (TextViewerStage) s;
                break;
            }
        }

        if (tvs == null)
        {
            tvs = new TextViewerStage(JITWatchUI.this, title, source, true);
            tvs.show();
            openPopupStages.add(tvs);
        }

        tvs.requestFocus();

        tvs.jumpTo(member.getSignatureRegEx());
    }

    void openBytecode(IMetaMember member)
    {
        String searchMethod = member.getSignatureForBytecode();

        MetaClass methodClass = member.getMetaClass();

        Map<String, String> bytecodeCache = methodClass.getBytecodeCache(config.getClassLocations());

        String bc = bytecodeCache.get(searchMethod);

        TextViewerStage tvs = new TextViewerStage(JITWatchUI.this, "Bytecode for " + member.toString(), bc, false);
        tvs.show();

        openPopupStages.add(tvs);
    }

    void openNativeCode(IMetaMember member)
    {
        String nativeCode = member.getNativeCode();
        TextViewerStage tvs = new TextViewerStage(JITWatchUI.this, "Native code for " + member.toString(), nativeCode, false);
        tvs.show();

        openPopupStages.add(tvs);
    }

    void openTextViewer(String title, String content)
    {
        TextViewerStage tvs = new TextViewerStage(JITWatchUI.this, title, content, false);
        tvs.show();
        openPopupStages.add(tvs);
    }

    private void chooseHotSpotFile()
    {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose HotSpot log file");
        String curDir = System.getProperty("user.dir");
        File dirFile = new File(curDir);
        fc.setInitialDirectory(dirFile);

        File result = fc.showOpenDialog(stage);

        if (result != null)
        {
            watchFile = result;
            log("Selected file: " + watchFile.getAbsolutePath());
            log("Click Start button to process or tail the file");
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

        String heapString = "Heap: " + (usedMemory / megabyte) + "/" + (totalMemory / megabyte) + "M";

        lblHeap.setText(heapString);

        btnErrorLog.setText("Errors (" + errorCount + ")");
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

    public void handleStageClosed(Stage stage)
    {
        openPopupStages.remove(stage);

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
        else if (stage instanceof ConfigStage)
        {
            btnConfigure.setDisable(false);
            configStage = null;

            if (startDelayedByConfig)
            {
                startWatching();
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
                startWatching();
            }
        }
        else if (stage instanceof CodeCacheStage)
        {
            btnCodeCache.setDisable(false);
            codeCacheStage = null;
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
        errorLog.append(entry).append("\n");
        errorCount++;
    }

    private void log(final String entry)
    {
        logBuffer.append(entry + "\n");
    }

    void refreshSelectedTreeNode(MetaClass metaClass)
    {
        classMemberList.clearClassMembers();

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
        return model.getPackageManager();
    }

    public Journal getJournal(String id)
    {
        return model.getJournal(id);
    }
}