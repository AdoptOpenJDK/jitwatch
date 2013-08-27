package com.chrisnewland.jitwatch.ui;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.chrisnewland.jitwatch.core.IJITListener;
import com.chrisnewland.jitwatch.core.JITEvent;
import com.chrisnewland.jitwatch.core.JITStats;
import com.chrisnewland.jitwatch.core.JITWatch;
import com.chrisnewland.jitwatch.core.ResourceLoader;
import com.chrisnewland.jitwatch.meta.MetaClass;
import com.chrisnewland.jitwatch.meta.MetaMethod;
import com.chrisnewland.jitwatch.meta.MetaPackage;

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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class JITWatchUI extends Application implements IJITListener
{
	private Stage stage;

	private JITWatch jw;

	private TreeItem<Object> rootItem;

	private ListView<MetaMethod> methodList;
	private ListView<Label> methodInfoList;

	private boolean showOnlyCompiled = true;
	private boolean hideInterfaces = true;

	private TreeItem<Object> selectedNode;

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

	private ConfigStage configStage;
	private TimeLineStage timeLineStage;
	private StatsStage statsStage;
	private HistoStage histoStage;

	private MetaMethod selectedMethod;

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

		selectedMethod = null;
		
		errorCount = 0;

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
				if (configStage != null)
				{
					configStage.close();
				}

				if (timeLineStage != null)
				{
					timeLineStage.close();
				}

				if (statsStage != null)
				{
					statsStage.close();
				}
				
				if (histoStage != null)
				{
					histoStage.close();
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

		int width = 900;
		int height = 480;

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

		btnConfigure = new Button("Configure");
		btnConfigure.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				configStage = new ConfigStage(JITWatchUI.this, jw.getProperties());
				configStage.show();
				btnConfigure.setDisable(true);
			}
		});

		btnTimeLine = new Button("TimeLine");
		btnTimeLine.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				timeLineStage = new TimeLineStage(JITWatchUI.this);
				timeLineStage.show();
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
				btnHisto.setDisable(true);
			}
		});

		btnErrorLog = new Button("Errors (0)");
		btnErrorLog.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				TextViewerStage viwer = new TextViewerStage("Error Log", errorLog.toString(), false);
				viwer.show();
			}
		});

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
		hboxTop.setPrefHeight(topHeight);
		hboxTop.setSpacing(10);

		methodList = new ListView<MetaMethod>();
		methodList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<MetaMethod>()
		{
			@Override
			public void changed(ObservableValue<? extends MetaMethod> arg0, MetaMethod oldVal, MetaMethod newVal)
			{
				showMethodInfo(newVal);
			}
		});

		final ContextMenu contextMenu = new ContextMenu();

		MenuItem menuItemSource = new MenuItem("Show Source");
		contextMenu.getItems().add(menuItemSource);

		MenuItem menuItemBytecode = new MenuItem("Show Bytecode");
		contextMenu.getItems().add(menuItemBytecode);

		MenuItem menuItemNative = new MenuItem("Show Native Code");
		contextMenu.getItems().add(menuItemNative);

		methodList.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent e)
			{
				if (e.getButton() == MouseButton.SECONDARY)
				{
					contextMenu.show(methodList, e.getScreenX(), e.getScreenY());
				}
			}
		});

		menuItemSource.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				openSource(methodList.getSelectionModel().getSelectedItem());
			}
		});

		menuItemBytecode.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				openBytecode(methodList.getSelectionModel().getSelectedItem());
			}
		});

		menuItemNative.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				openNativeCode(methodList.getSelectionModel().getSelectedItem());
			}
		});

		methodInfoList = new ListView<Label>();

		SplitPane spMethodInfo = new SplitPane();
		spMethodInfo.setOrientation(Orientation.VERTICAL);

		spMethodInfo.getItems().add(methodList);
		spMethodInfo.getItems().add(methodInfoList);

		methodList.prefHeightProperty().bind(scene.heightProperty());
		methodInfoList.prefHeightProperty().bind(scene.heightProperty());

		treeView.prefWidthProperty().bind(scene.widthProperty());
		// vboxMethods.prefWidthProperty().bind(scene.widthProperty());

		SplitPane spMain = new SplitPane();
		spMain.setOrientation(Orientation.VERTICAL);

		SplitPane spCentre = new SplitPane();
		spCentre.getItems().add(treeView);
		spCentre.getItems().add(spMethodInfo);

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

		borderPane.setTop(hboxTop);
		borderPane.setCenter(spMain);
		// borderPane.setBottom(textArea);

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

	private void openSource(MetaMethod method)
	{
		MetaClass methodClass = method.getMetaClass();

		String fqName = methodClass.getFullyQualifiedName();

		fqName = fqName.replace(".", "/") + ".java";

		String source = ResourceLoader.getSource(jw.getSourceLocations(), fqName);

		TextViewerStage tvs = new TextViewerStage("Source code for " + fqName, source, true);
		tvs.show();
		tvs.jumpTo(method.getSignatureRegEx());
	}

	private void openBytecode(MetaMethod method)
	{
		String searchMethod = method.getSignatureForBytecode();

		MetaClass methodClass = method.getMetaClass();

		Map<String, String> bytecodeCache = methodClass.getBytecodeCache(jw.getClassLocations());

		String bc = bytecodeCache.get(searchMethod);

		TextViewerStage tvs = new TextViewerStage("Bytecode for " + method.toString(), bc, false);
		tvs.show();
	}

	private void openNativeCode(MetaMethod method)
	{
		String nativeCode = method.getNativeCode();
		TextViewerStage tvs = new TextViewerStage("Native code for " + method.toString(), nativeCode, false);
		tvs.show();
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

	private void showMethodInfo(MetaMethod metaMethod)
	{
		methodInfoList.getItems().clear();

		if (metaMethod == null)
		{
			return;
		}

		selectedMethod = metaMethod;

		List<String> queuedAttrKeys = metaMethod.getQueuedAttributes();

		for (String key : queuedAttrKeys)
		{
			if (metaMethod.isQueued() || metaMethod.isCompiled())
			{
				Label l = new Label("Queued: " + key + " = " + metaMethod.getQueuedAttribute(key));
				methodInfoList.getItems().add(l);
			}
		}

		List<String> compiledAttrKeys = metaMethod.getCompiledAttributes();

		for (String key : compiledAttrKeys)
		{
			if (metaMethod.isCompiled())
			{
				Label l = new Label("Compiled: " + key + " = " + metaMethod.getCompiledAttribute(key));
				methodInfoList.getItems().add(l);
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

		if (logBuffer.length() > 0)
		{
			refreshLog();
		}

		btnErrorLog.setText("Errors (" + errorCount + ")");
	}

	private void refreshLog()
	{
		textArea.appendText(logBuffer.toString());
		logBuffer.delete(0, logBuffer.length());
	}

	public MetaMethod getSelectedMethod()
	{
		return selectedMethod;
	}

	private void clearAndRefresh()
	{
		selectedMethod = null;
		rootItem.getChildren().clear();
		showTree();
	}

	public void updateConfig(Properties updatedProps)
	{
		if (updatedProps != null)
		{
			jw.setProperties(updatedProps);
		}

		btnConfigure.setDisable(false);
	}

	public void timeLineClosed()
	{
		btnTimeLine.setDisable(false);
		timeLineStage = null;
	}

	public void statsClosed()
	{
		btnStats.setDisable(false);
		statsStage = null;
	}

	public void histoStageClosed()
	{
		btnHisto.setDisable(false);
		histoStage = null;
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
		methodList.getItems().clear();

		showMethodInfo(null);

		if (selectedNode == null)
		{
			// nothing selected
			return;
		}

		Object value = selectedNode.getValue();

		if (value instanceof MetaClass)
		{
			MetaClass metaClass = (MetaClass) value;

			List<MetaMethod> metaMethods = metaClass.getMetaMethods();

			for (MetaMethod metaMethod : metaMethods)
			{
				if (metaMethod.isCompiled())
				{
					methodList.getItems().add(metaMethod);
				}
				else if (!showOnlyCompiled)
				{
					methodList.getItems().add(metaMethod);
				}
			}
		}
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