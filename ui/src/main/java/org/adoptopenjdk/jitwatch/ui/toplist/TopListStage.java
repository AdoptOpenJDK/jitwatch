/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.toplist;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_BYTES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILE_ID;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILE_KIND;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_DECOMPILES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.OSR;

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
import org.adoptopenjdk.jitwatch.toplist.AbstractTopListVisitable;
import org.adoptopenjdk.jitwatch.toplist.CompileTimeTopListVisitable;
import org.adoptopenjdk.jitwatch.toplist.CompiledAttributeTopListVisitable;
import org.adoptopenjdk.jitwatch.toplist.HotThrowTopListVisitable;
import org.adoptopenjdk.jitwatch.toplist.ITopListScore;
import org.adoptopenjdk.jitwatch.toplist.InliningFailReasonTopListVisitable;
import org.adoptopenjdk.jitwatch.toplist.MemberScore;
import org.adoptopenjdk.jitwatch.toplist.MostUsedIntrinsicsTopListVisitable;
import org.adoptopenjdk.jitwatch.toplist.NativeMethodSizeTopListVisitable;
import org.adoptopenjdk.jitwatch.toplist.StaleTaskToplistVisitable;
import org.adoptopenjdk.jitwatch.ui.main.JITWatchUI;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

public class TopListStage extends Stage
{
	private static final String MEMBER = "Member";
	private ObservableList<ITopListScore> topList = FXCollections.observableArrayList();

	private TableView<ITopListScore> tableView;

	private TopListWrapper topListWrapper;

	public TopListStage(final JITWatchUI parent)
	{
		initStyle(StageStyle.DECORATED);

		int width = 800;
		int height = 480;

		TopListWrapper tlLargestNative = new TopListWrapper("Largest Native Methods", new NativeMethodSizeTopListVisitable(
				parent.getJITDataModel(), true), new String[] { "Bytes", MEMBER });
		
		TopListWrapper tlInlineFailReasons = new TopListWrapper("Inlining Failure Reasons", new InliningFailReasonTopListVisitable(
				parent.getJITDataModel(), true), new String[] { "Count", "Reason" });
		
		TopListWrapper tlIntrinsics = new TopListWrapper("Most-used Intrinsics", new MostUsedIntrinsicsTopListVisitable(
				parent.getJITDataModel(), true), new String[] { "Count", "Intrinsic" });
		
		TopListWrapper tlHotThrows = new TopListWrapper("Hot throws", new HotThrowTopListVisitable(
				parent.getJITDataModel(), true), new String[] { "Count", "Hot Throw" });
		
		TopListWrapper tlLargestBytecode = new TopListWrapper("Largest Bytecode Methods", new CompiledAttributeTopListVisitable(
				parent.getJITDataModel(), ATTR_BYTES, true), new String[] { "Bytes", MEMBER });
		
		TopListWrapper tlSlowestCompilation = new TopListWrapper("Slowest Compilation Times",
				new CompileTimeTopListVisitable(parent.getJITDataModel(), true), new String[] {
						"Milliseconds",
						MEMBER });
		
		TopListWrapper tlMostDecompiled = new TopListWrapper("Most Decompiled Methods", new CompiledAttributeTopListVisitable(
				parent.getJITDataModel(), ATTR_DECOMPILES, true), new String[] { "Decompiles", MEMBER });
		
		TopListWrapper tlCompilationOrder = new TopListWrapper("Compilation Order", new AbstractTopListVisitable(
				parent.getJITDataModel(), false)
		{
			@Override
			public void visit(IMetaMember mm)
			{
				String compileID = mm.getCompiledAttribute(ATTR_COMPILE_ID);
				String compileKind = mm.getCompiledAttribute(ATTR_COMPILE_KIND);
				if (compileID != null && (compileKind == null || !OSR.equals(compileKind)))
				{
					long value = Long.valueOf(mm.getCompiledAttribute(ATTR_COMPILE_ID));
					topList.add(new MemberScore(mm, value));
				}
			}
		}, new String[] { "Order", MEMBER });

		TopListWrapper tlCompilationOrderOSR = new TopListWrapper("Compilation Order (OSR)", new AbstractTopListVisitable(
				parent.getJITDataModel(), false)
		{
			@Override
			public void visit(IMetaMember mm)
			{
				String compileID = mm.getCompiledAttribute(ATTR_COMPILE_ID);
				String compileKind = mm.getCompiledAttribute(ATTR_COMPILE_KIND);
				if (compileID != null && compileKind != null && OSR.equals(compileKind))
				{
					long value = Long.valueOf(mm.getCompiledAttribute(ATTR_COMPILE_ID));
					topList.add(new MemberScore(mm, value));
				}
			}
		}, new String[] { "Order", MEMBER });
		
		TopListWrapper tlStaleTasks = new TopListWrapper("Most Stale Tasks", new StaleTaskToplistVisitable(
				parent.getJITDataModel(), true), new String[] { "Count", "Member" });

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
			@Override
			public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal)
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
			@Override
			public void changed(ObservableValue<? extends ITopListScore> arg0, ITopListScore oldVal, ITopListScore newVal)
			{
				if (itIsNull(newVal) && isInstanceOfMemberScore(newVal))
				{
					parent.focusTreeOnMember((IMetaMember) newVal.getKey(), true);
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

	private boolean itIsNull(ITopListScore newVal)
	{
		return newVal != null;
	}

	private boolean isInstanceOfMemberScore(ITopListScore newVal)
	{
		return newVal instanceof MemberScore;
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

	public final void redraw()
	{
	}
}