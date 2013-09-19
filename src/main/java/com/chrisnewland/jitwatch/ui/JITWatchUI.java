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
import com.chrisnewland.jitwatch.model.MetaClass;
import com.chrisnewland.jitwatch.model.MetaPackage;
import com.chrisnewland.jitwatch.model.PackageManager;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.TimelineBuilder;
import javafx.application.Application;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Duration;

public class JITWatchUI extends Application implements IJITListener
{
    private Stage stage;

    private JITDataModel model;
    private HotSpotLogParser logParser;

    private TreeView<Object> treeView;
    private TreeItem<Object> rootItem;

    private ListView<IMetaMember> memberList;

    private TableView<AttributeTableRow3Col> attributeTableView;
    private ObservableList<AttributeTableRow3Col> memberAttrList;

    private boolean showOnlyCompiled = true;
    private boolean hideInterfaces = true;

    private TreeItem<Object> selectedNode;

    private List<Stage> openPopupStages = new ArrayList<>();

    private TextArea textArea;

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

    private Label lblHeap;

    private ConfigStage configStage;
    private TimeLineStage timeLineStage;
    private StatsStage statsStage;
    private HistoStage histoStage;
    private TopListStage topListStage;

    private IMetaMember selectedMember;

    private Runtime runtime = Runtime.getRuntime();

    // synchronized as buffer is drained async on GUI thread
    private StringBuffer logBuffer = new StringBuffer();

    private StringBuilder errorLog = new StringBuilder();
    private int errorCount = 0;

    private boolean repaintTree = false;

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
        model.reset();

        textArea.clear();

        selectedMember = null;

        errorCount = 0;
        errorLog.delete(0, errorLog.length());

        refreshSelectedTreeNode();

        rootItem.getChildren().clear();

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

        rootItem = new TreeItem<Object>("Packages");

        rootItem.setExpanded(true);

        treeView = new TreeView<Object>(rootItem);

        treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<Object>>()
        {
            @Override
            public void changed(ObservableValue<? extends TreeItem<Object>> observableValue, TreeItem<Object> oldItem,
                    TreeItem<Object> newItem)
            {
                selectedNode = newItem;

                refreshSelectedTreeNode();
            }
        });

        int width = 1024;
        int height = 592;

        BorderPane borderPane = new BorderPane();

        Scene scene = new Scene(borderPane, width, height);

        CheckBox cbOnlyCompiled = new CheckBox("JIT Only");
        cbOnlyCompiled.setTooltip(new Tooltip("Show only compiled methods in the class methods list"));
        cbOnlyCompiled.setSelected(showOnlyCompiled);

        cbOnlyCompiled.selectedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal)
            {
                showOnlyCompiled = newVal;
                refreshSelectedTreeNode();
            }
        });

        CheckBox cbHideInterfaces = new CheckBox("Hide Interfaces");
        cbHideInterfaces.setTooltip(new Tooltip("Hide Interfaces from the Class Tree"));
        cbHideInterfaces.setSelected(hideInterfaces);
        cbHideInterfaces.selectedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal)
            {
                hideInterfaces = newVal;
                clearAndRefresh();
            }
        });

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
                startWatching();
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
                configStage = new ConfigStage(JITWatchUI.this, config);
                configStage.show();

                openPopupStages.add(configStage);

                btnConfigure.setDisable(true);
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
        hboxTop.getChildren().add(btnErrorLog);
        hboxTop.getChildren().add(cbOnlyCompiled);
        hboxTop.getChildren().add(cbHideInterfaces);
        hboxTop.getChildren().add(lblHeap);
        hboxTop.setPrefHeight(topHeight);
        hboxTop.setSpacing(10);

        memberList = new ListView<IMetaMember>();
        memberList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<IMetaMember>()
        {
            @Override
            public void changed(ObservableValue<? extends IMetaMember> arg0, IMetaMember oldVal, IMetaMember newVal)
            {
                showMemberInfo(newVal);
            }
        });

        final ContextMenu contextMenuCompiled = new ContextMenu();
        final ContextMenu contextMenuNotCompiled = new ContextMenu();

        MenuItem menuItemSource = new MenuItem("Show Source");
        MenuItem menuItemBytecode = new MenuItem("Show Bytecode");
        MenuItem menuItemNative = new MenuItem("Show Native Code");

        contextMenuCompiled.getItems().add(menuItemSource);
        contextMenuCompiled.getItems().add(menuItemBytecode);
        contextMenuCompiled.getItems().add(menuItemNative);

        contextMenuNotCompiled.getItems().add(menuItemSource);
        contextMenuNotCompiled.getItems().add(menuItemBytecode);

        memberList.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent e)
            {
                if (e.getButton() == MouseButton.SECONDARY)
                {
                    if (selectedMember.isCompiled())
                    {
                        contextMenuCompiled.show(memberList, e.getScreenX(), e.getScreenY());
                    }
                    else
                    {
                        contextMenuNotCompiled.show(memberList, e.getScreenX(), e.getScreenY());
                    }
                }
            }
        });

        menuItemSource.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {
                openSource(memberList.getSelectionModel().getSelectedItem());
            }
        });

        menuItemBytecode.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {
                openBytecode(memberList.getSelectionModel().getSelectedItem());
            }
        });

        menuItemNative.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {
                openNativeCode(memberList.getSelectionModel().getSelectedItem());
            }
        });

        memberAttrList = FXCollections.observableArrayList();
        attributeTableView = TableUtil.buildTableMemberAttributes(memberAttrList);
        attributeTableView.setPlaceholder(new Text("Select a method to view HotSpot attributes."));

        SplitPane spMethodInfo = new SplitPane();
        spMethodInfo.setOrientation(Orientation.VERTICAL);

        spMethodInfo.getItems().add(memberList);
        spMethodInfo.getItems().add(attributeTableView);

        memberList.prefHeightProperty().bind(scene.heightProperty());
        attributeTableView.prefHeightProperty().bind(scene.heightProperty());

        treeView.prefWidthProperty().bind(scene.widthProperty());

        SplitPane spMain = new SplitPane();
        spMain.setOrientation(Orientation.VERTICAL);

        SplitPane spCentre = new SplitPane();
        spCentre.getItems().add(treeView);
        spCentre.getItems().add(spMethodInfo);
        spCentre.setDividerPositions(0.3, 0.7);

        textArea = new TextArea();
        textArea.setStyle("-fx-font-family:monospace;");
        textArea.setPrefHeight(bottomHeight);
        textArea.setText("Welcome to JITWatch\n");

        if (watchFile == null)
        {
            log("Please choose a HotSpot log file");
        }
        else
        {
            log("Using HotSpot log file: " + watchFile.getAbsolutePath());
        }
        spMain.getItems().add(spCentre);
        spMain.getItems().add(textArea);
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

    public void openTreeAtMember(IMetaMember member)
    {
        List<String> path = member.getTreePath();

        // would be better to identify open nodes and close?
        clearAndRefresh();
        
        TreeItem<Object> curNode = rootItem;

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
                    treeView.getSelectionModel().select(curNode);
                    found = true;
                    break;
                }
            }
        }

        if (found)
        {
            treeView.scrollTo(rowsAbove);
            
            memberList.getSelectionModel().select(member);
            memberList.scrollTo(memberList.getSelectionModel().getSelectedIndex());
        }
        
    }

    private void openSource(IMetaMember member)
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

    private void openBytecode(IMetaMember member)
    {
        String searchMethod = member.getSignatureForBytecode();

        MetaClass methodClass = member.getMetaClass();

        Map<String, String> bytecodeCache = methodClass.getBytecodeCache(config.getClassLocations());

        String bc = bytecodeCache.get(searchMethod);

        TextViewerStage tvs = new TextViewerStage(JITWatchUI.this, "Bytecode for " + member.toString(), bc, false);
        tvs.show();

        openPopupStages.add(tvs);
    }

    private void openNativeCode(IMetaMember member)
    {
        String nativeCode = member.getNativeCode();
        TextViewerStage tvs = new TextViewerStage(JITWatchUI.this, "Native code for " + member.toString(), nativeCode, false);
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

    private void showMemberInfo(IMetaMember member)
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
            memberAttrList.add(new AttributeTableRow3Col("Queued", key, member.getQueuedAttribute(key)));
        }

        List<String> compiledAttrKeys = member.getCompiledAttributes();

        for (String key : compiledAttrKeys)
        {
            memberAttrList.add(new AttributeTableRow3Col("Compiled", key, member.getCompiledAttribute(key)));
        }
    }

    private void refresh()
    {
        if (repaintTree)
        {
            repaintTree = false;
            showTree();
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
        textArea.appendText(logBuffer.toString());
        logBuffer.delete(0, logBuffer.length());
    }

    public IMetaMember getSelectedMember()
    {
        return selectedMember;
    }

    private void clearAndRefresh()
    {
        selectedMember = null;
        rootItem.getChildren().clear();
        showTree();
    }

    public void handleStageClosed(Stage stage)
    {
        openPopupStages.remove(stage);

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
        }
        else if (stage instanceof TopListStage)
        {
            btnTopList.setDisable(false);
            topListStage = null;
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

    private TreeItem<Object> findOrCreateTreeItem(TreeItem<Object> parent, Object value)
    {
        ObservableList<TreeItem<Object>> children = parent.getChildren();

        TreeItem<Object> found = null;

        int placeToInsert = 0;
        boolean foundInsertPos = false;

        for (TreeItem<Object> child : children)
        {
            int stringCompare = child.getValue().toString().compareTo(value.toString());

            if (stringCompare == 0)
            {
                found = child;
                break;
            }
            else if (!foundInsertPos && stringCompare < 0)
            {
                // make sure sub packages listed before classes in this package

                if (child.getValue() instanceof MetaPackage && value instanceof MetaClass)
                {

                }
                else
                {
                    placeToInsert++;
                }
            }
            else
            {
                if (child.getValue() instanceof MetaPackage && value instanceof MetaClass)
                {
                    placeToInsert++;
                }
                else
                {
                    foundInsertPos = true;
                }
            }
        }

        if (found == null)
        {
            found = new TreeItem<Object>(value);
            children.add(placeToInsert, found);

            if (value instanceof MetaClass && ((MetaClass) value).isMissingDef())
            {
                // indicate missing class definition?
            }
        }

        return found;
    }

    private void refreshSelectedTreeNode()
    {
        memberList.getItems().clear();

        showMemberInfo(null);

        if (selectedNode == null)
        {
            // nothing selected
            return;
        }

        Object value = selectedNode.getValue();

        if (value instanceof MetaClass)
        {
            MetaClass metaClass = (MetaClass) value;

            List<IMetaMember> metaMembers = metaClass.getMetaMembers();

            for (IMetaMember member : metaMembers)
            {
                if (member.isCompiled())
                {
                    memberList.getItems().add(member);
                }
                else if (!showOnlyCompiled)
                {
                    memberList.getItems().add(member);
                }
            }

            memberList.setCellFactory(new Callback<ListView<IMetaMember>, ListCell<IMetaMember>>()
            {
                @Override
                public ListCell<IMetaMember> call(ListView<IMetaMember> arg0)
                {
                    return new MetaMethodCell();
                }
            });

        }
    }

    static class MetaMethodCell extends ListCell<IMetaMember>
    {
        @Override
        public void updateItem(IMetaMember item, boolean empty)
        {
            super.updateItem(item, empty);

            if (item != null)
            {
                setText(item.toStringUnqualifiedMethodName());

                if (isSelected())
                {
                    setTextFill(Color.WHITE);
                }
                else if (item.isCompiled())
                {
                    setTextFill(Color.RED);
                }
                else
                {
                    setTextFill(Color.BLACK);
                }
            }
        }
    }

    public PackageManager getPackageManager()
    {
        return model.getPackageManager();
    }

    private void showTree()
    {
        List<MetaPackage> roots = model.getPackageManager().getRootPackages();

        for (MetaPackage mp : roots)
        {
            showTree(rootItem, mp);
        }
    }

    private void showTree(TreeItem<Object> currentNode, MetaPackage mp)
    {
        TreeItem<Object> packageItem = findOrCreateTreeItem(currentNode, mp);

        List<MetaPackage> childPackages = mp.getChildPackages();

        for (MetaPackage childPackage : childPackages)
        {
            showTree(packageItem, childPackage);
        }

        List<MetaClass> packageClasses = mp.getPackageClasses();

        for (MetaClass packageClass : packageClasses)
        {
            if (!hideInterfaces || !packageClass.isInterface())
            {
                findOrCreateTreeItem(packageItem, packageClass);
            }
        }
    }
}