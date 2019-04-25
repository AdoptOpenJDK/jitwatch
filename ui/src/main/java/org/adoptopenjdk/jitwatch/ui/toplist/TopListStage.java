/*
 * Copyright (c) 2013-2019 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.toplist;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_BYTES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_DECOMPILES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.toplist.*;
import org.adoptopenjdk.jitwatch.ui.main.IMemberSelectedListener;
import org.adoptopenjdk.jitwatch.ui.main.IStageAccessProxy;
import org.adoptopenjdk.jitwatch.ui.main.JITWatchUI;
import org.adoptopenjdk.jitwatch.ui.stage.IClearableStage;
import org.adoptopenjdk.jitwatch.ui.stage.IStageClosedListener;
import org.adoptopenjdk.jitwatch.ui.stage.StageManager;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

public class TopListStage extends Stage implements IStageClosedListener, IClearableStage
{
	private static final String MEMBER = "Member";

	private ObservableList<ITopListScore> topList = FXCollections.observableArrayList();

	private TableView<ITopListScore> tableView;

	private TopListWrapper topListWrapper;

	private CompileNodeStage compilationListStage;

	public TopListStage(JITWatchUI parent, IReadOnlyJITDataModel model)
	{
		StageManager.registerStageClosedListener(this);

		initStyle(StageStyle.DECORATED);

		IMemberSelectedListener selectionListener = parent;

		IStageAccessProxy triViewAccessor = parent;

		int width = 800;
		int height = 480;

		TopListWrapper tlLargestNative = new TopListWrapper("Largest Native Methods",
				new NativeMethodSizeTopListVisitable(model, true), new String[] { "Bytes", MEMBER });

		TopListWrapper tlInlineFailReasons = new TopListWrapper("Inlining Failure Reasons",
				new InliningFailReasonTopListVisitable(model, true), new String[] { "Count", "Reason" });

		TopListWrapper tlIntrinsics = new TopListWrapper("Most-used Intrinsics",
				new MostUsedIntrinsicsTopListVisitable(model, true), new String[] { "Count", "Intrinsic" });

		TopListWrapper tlHotThrows = new TopListWrapper("Hot throws", new HotThrowTopListVisitable(model, true),
				new String[] { "Count", "Hot Throw" });

		TopListWrapper tlLargestBytecode = new TopListWrapper("Largest Bytecode Methods",
				new CompiledAttributeTopListVisitable(model, ATTR_BYTES, true), new String[] { "Bytes", MEMBER });

		TopListWrapper tlSlowestCompilation = new TopListWrapper("Slowest Compilation Times",
				new CompileTimeTopListVisitable(model, true), new String[] { "Milliseconds", MEMBER });

		TopListWrapper tlMostDecompiled = new TopListWrapper("Most Decompiled Methods",
				new CompiledAttributeTopListVisitable(model, ATTR_DECOMPILES, true), new String[] { "Decompiles", MEMBER });

		TopListWrapper tlCompilationOrder = new TopListWrapper("Compilation Order",
				new CompilationOrderTopListVisitable(model, false), new String[] { "Order", MEMBER });

		TopListWrapper tlCompilationOrderOSR = new TopListWrapper("Compilation Order (OSR)",
				new CompilationOrderOSRTopListVisitable(model, false), new String[] { "Order", MEMBER });

		TopListWrapper tlStaleTasks = new TopListWrapper("Most Stale Tasks", new StaleTaskToplistVisitable(model, true),
				new String[] { "Count", "Member" });

		final Map<String, TopListWrapper> attrMap = new HashMap<>();

		attrMap.put(tlLargestNative.getTitle(), tlLargestNative);
		attrMap.put(tlInlineFailReasons.getTitle(), tlInlineFailReasons);
		attrMap.put(tlIntrinsics.getTitle(), tlIntrinsics);
		attrMap.put(tlHotThrows.getTitle(), tlHotThrows);
		attrMap.put(tlLargestBytecode.getTitle(), tlLargestBytecode);
		attrMap.put(tlSlowestCompilation.getTitle(), tlSlowestCompilation);
		attrMap.put(tlMostDecompiled.getTitle(), tlMostDecompiled);
		attrMap.put(tlCompilationOrder.getTitle(), tlCompilationOrder);
		attrMap.put(tlCompilationOrderOSR.getTitle(), tlCompilationOrderOSR);
		attrMap.put(tlStaleTasks.getTitle(), tlStaleTasks);

		VBox vbox = new VBox();
		vbox.setPadding(new Insets(8));
		vbox.setSpacing(8);

		List<String> keyList = new ArrayList<>(attrMap.keySet());
		Collections.sort(keyList);

		ObservableList<String> options = FXCollections.observableArrayList(keyList);

		final ComboBox<String> comboBox = new ComboBox<>(options);
		comboBox.setValue(tlLargestNative.getTitle());
		topListWrapper = tlLargestNative;

		comboBox.valueProperty().addListener(new ChangeListener<String>()
		{
			@Override public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal)
			{
				topListWrapper = attrMap.get(newVal);
				buildTableView(topListWrapper);
			}
		});

		Scene scene = UserInterfaceUtil.getScene(vbox, width, height);

		setTitle("JITWatch TopLists");

		tableView = new TableView<>();

		TableColumn<ITopListScore, Long> colScore = new TableColumn<ITopListScore, Long>("");
		colScore.setCellValueFactory(new PropertyValueFactory<ITopListScore, Long>("score"));
		colScore.prefWidthProperty().bind(tableView.widthProperty().divide(8));

		TableColumn<ITopListScore, Object> colKey = new TableColumn<ITopListScore, Object>("");
		colKey.setCellValueFactory(new PropertyValueFactory<ITopListScore, Object>("key"));
		colKey.prefWidthProperty().bind(tableView.widthProperty().divide(8).multiply(7));

		tableView.getColumns().add(colScore);
		tableView.getColumns().add(colKey);

		tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ITopListScore>()
		{
			@Override public void changed(ObservableValue<? extends ITopListScore> arg0, ITopListScore oldVal, ITopListScore newVal)
			{
				if (newVal != null)
				{
					if (newVal instanceof MemberScore)
					{
						selectionListener.selectMember((IMetaMember) newVal.getKey(), true, true);
					}
					else if (newVal instanceof CompilationListScore)
					{
						CompilationListScore score = (CompilationListScore) newVal;

						if (compilationListStage == null)
						{
							compilationListStage = new CompileNodeStage(selectionListener, triViewAccessor);
							StageManager.addAndShow(TopListStage.this, compilationListStage);
						}

						String title = "Compilations for TopList '" + topListWrapper.getTitle() + " : " + score.getKey() + "'";

						compilationListStage.setCompilations(title, score);

						compilationListStage.requestFocus();
					}
				}
			}
		});

		buildTableView(topListWrapper);

		vbox.getChildren().add(comboBox);
		vbox.getChildren().add(tableView);

		tableView.prefHeightProperty().bind(scene.heightProperty());

		setScene(scene);
		show();
	}

	private void buildTableView(TopListWrapper topListWrapper)
	{
		topList.clear();

		topList.addAll(topListWrapper.getVisitable().buildTopList());

		int pos = 0;

		for (TableColumn<ITopListScore, ? extends Object> col : tableView.getColumns())
		{
			col.setText(topListWrapper.getColumns()[pos++]);
		}

		tableView.setItems(topList);
	}

	@Override public void handleStageClosed(Stage stage)
	{
		if (stage instanceof CompileNodeStage)
		{
			compilationListStage = null;
		}
	}

	@Override public void clear()
	{
		topList.clear();
	}
}