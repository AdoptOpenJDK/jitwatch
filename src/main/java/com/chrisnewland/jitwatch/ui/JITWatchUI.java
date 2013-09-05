package com.chrisnewland.jitwatch.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.chrisnewland.jitwatch.core.IJITListener;
import com.chrisnewland.jitwatch.core.JITEvent;
import com.chrisnewland.jitwatch.core.JITStats;
import com.chrisnewland.jitwatch.core.JITWatch;
import com.chrisnewland.jitwatch.loader.ResourceLoader;
import com.chrisnewland.jitwatch.meta.IMetaMember;
import com.chrisnewland.jitwatch.meta.MetaClass;
import com.chrisnewland.jitwatch.meta.MetaPackage;
import com.chrisnewland.jitwatch.meta.PackageManager;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.TimelineBuilder;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Duration;

public class JITWatchUI extends Application implements IJITListener
{
	private Stage stage;

	private JITWatch jw;

	private TreeItem<Object> rootItem;

	private ListView<IMetaMember> memberList;
	private ListView<Label> memberInfoList;

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
	private Button btnErrorLog;
	
	private Label lblHeap;

	private ConfigStage configStage;
	private TimeLineStage timeLineStage;
	private StatsStage statsStage;
	private HistoStage histoStage;

	private IMetaMember selectedMember;
	
	private Runtime runtime = Runtime.getRuntime();

	// needs to be synchronized as buffer drained async on GUI thread
	private StringBuffer logBuffer = new StringBuffer();

	// not synchronized
	private StringBuilder errorLog = new StringBuilder();
	private int errorCount = 0;

	private boolean repaintTree = false;

	public JITWatchUI()
	{
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
					jw.watch(watchFile);
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
			jw.stop();
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

		TreeView<Object> treeView = new TreeView<Object>(rootItem);

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

		CheckBox cbOnlyCompiled = new CheckBox("Compiled Only");
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
				configStage = new ConfigStage(JITWatchUI.this, jw.getConfig());
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

		final ContextMenu contextMenu = new ContextMenu();

		MenuItem menuItemSource = new MenuItem("Show Source");
		contextMenu.getItems().add(menuItemSource);

		MenuItem menuItemBytecode = new MenuItem("Show Bytecode");
		contextMenu.getItems().add(menuItemBytecode);

		MenuItem menuItemNative = new MenuItem("Show Native Code");
		contextMenu.getItems().add(menuItemNative);

		memberList.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent e)
			{
				if (e.getButton() == MouseButton.SECONDARY)
				{
					contextMenu.show(memberList, e.getScreenX(), e.getScreenY());
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

		memberInfoList = new ListView<Label>();

		SplitPane spMethodInfo = new SplitPane();
		spMethodInfo.setOrientation(Orientation.VERTICAL);

		spMethodInfo.getItems().add(memberList);
		spMethodInfo.getItems().add(memberInfoList);

		memberList.prefHeightProperty().bind(scene.heightProperty());
		memberInfoList.prefHeightProperty().bind(scene.heightProperty());

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

		// so that additional classpaths are logged
		jw = new JITWatch(this, true);

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
		return jw.getJITStats();
	}

	private void updateButtons()
	{
		btnStartWatching.setDisable(watchFile == null || isWatching);
		btnStopWatching.setDisable(!isWatching);
	}

	public List<JITEvent> getJITEvents()
	{
		return jw.getEventListCopy();
	}

	private void openSource(IMetaMember member)
	{
		MetaClass methodClass = member.getMetaClass();

		String fqName = methodClass.getFullyQualifiedName();

		fqName = fqName.replace(".", "/") + ".java";

		String source = ResourceLoader.getSource(jw.getConfig().getSourceLocations(), fqName);

		TextViewerStage tvs = new TextViewerStage(JITWatchUI.this, "Source code for " + fqName, source, true);
		tvs.show();

		openPopupStages.add(tvs);
		
		//TODO if source already open then re-use

		tvs.jumpTo(member.getSignatureRegEx());
	}

	private void openBytecode(IMetaMember member)
	{		
		String searchMethod = member.getSignatureForBytecode();

		MetaClass methodClass = member.getMetaClass();

		Map<String, String> bytecodeCache = methodClass.getBytecodeCache(jw.getConfig().getClassLocations());

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
		memberInfoList.getItems().clear();

		if (member == null)
		{
			return;
		}

		selectedMember = member;

		List<String> queuedAttrKeys = member.getQueuedAttributes();

		for (String key : queuedAttrKeys)
		{
			if (member.isQueued() || member.isCompiled())
			{
				Label l = new Label("Queued: " + key + " = " + member.getQueuedAttribute(key));
				memberInfoList.getItems().add(l);
			}
		}

		List<String> compiledAttrKeys = member.getCompiledAttributes();

		for (String key : compiledAttrKeys)
		{
			if (member.isCompiled())
			{
				Label l = new Label("Compiled: " + key + " = " + member.getCompiledAttribute(key));
				memberInfoList.getItems().add(l);
			}
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

		if (logBuffer.length() > 0)
		{
			refreshLog();
		}
		
		long totalMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		long usedMemory = totalMemory - freeMemory;
		
		long megabyte = 1024*1024;
		
		String heapString = "Heap: " + (usedMemory/megabyte) + "/" + (totalMemory/megabyte) + "M";

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

			if (value instanceof MetaClass)
			{
				if (((MetaClass) value).isMissingDef())
				{

				}
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
		return jw.getPackageManager();
	}
	
	private void showTree()
	{
		List<MetaPackage> roots = jw.getPackageManager().getRootPackages();

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