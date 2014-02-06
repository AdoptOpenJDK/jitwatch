/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.toplist.CompiledAttributeTopListVisitable;
import com.chrisnewland.jitwatch.toplist.ITopListScore;
import com.chrisnewland.jitwatch.toplist.ITopListVisitable;
import com.chrisnewland.jitwatch.toplist.InliningFailReasonTopListVisitable;
import com.chrisnewland.jitwatch.toplist.MemberScore;
import com.chrisnewland.jitwatch.toplist.AbstractTopListVisitable;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class TopListStage extends Stage
{
	private ObservableList<ITopListScore> topList = FXCollections.observableArrayList();

	private TableView<ITopListScore> tableView;

	private ITopListVisitable topListVisitable;

	public TopListStage(final JITWatchUI parent)
	{
		initStyle(StageStyle.DECORATED);

		setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				parent.handleStageClosed(TopListStage.this);
			}
		});

		int width = 800;
		int height = 480;

		final Map<String, ITopListVisitable> attrMap = new HashMap<>();

		// Hurry up lambdas !!!

		String largestNativeMethods = "Largest Native Methods";

		attrMap.put(largestNativeMethods, new CompiledAttributeTopListVisitable(parent.getJITDataModel(), ATTR_NMSIZE, true));
		attrMap.put("Inlining Failure Reasons", new InliningFailReasonTopListVisitable(parent.getJITDataModel(), true));

		attrMap.put("Largest Bytecode Methods", new CompiledAttributeTopListVisitable(parent.getJITDataModel(), ATTR_BYTES, true));
		attrMap.put("Slowest Compilation Times", new CompiledAttributeTopListVisitable(parent.getJITDataModel(),
				ATTR_COMPILE_MILLIS, true));
		attrMap.put("Most Decompiled Methods", new CompiledAttributeTopListVisitable(parent.getJITDataModel(), ATTR_DECOMPILES,
				true));

		attrMap.put("Compilation Order", new AbstractTopListVisitable(parent.getJITDataModel(), false)
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
		});

		attrMap.put("Compilation Order (OSR)", new AbstractTopListVisitable(parent.getJITDataModel(), false)
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
		});

		VBox vbox = new VBox();
		vbox.setPadding(new Insets(8));
		vbox.setSpacing(8);

		List<String> keyList = new ArrayList<>(attrMap.keySet());
		Collections.sort(keyList);

		ObservableList<String> options = FXCollections.observableArrayList(keyList);

		final ComboBox<String> comboBox = new ComboBox<>(options);
		comboBox.setValue(largestNativeMethods);

		topListVisitable = attrMap.get(largestNativeMethods);

		comboBox.valueProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal)
			{
				topListVisitable = attrMap.get(newVal);
				buildTableView(topListVisitable);
			}
		});

		Scene scene = new Scene(vbox, width, height);

		setTitle("JITWatch TopLists");

		buildTableView(topListVisitable);
		tableView = TableUtil.buildTableTopListScore(topList);

		tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ITopListScore>()
		{
			@Override
			public void changed(ObservableValue<? extends ITopListScore> arg0, ITopListScore oldVal, ITopListScore newVal)
			{
				if (newVal != null && newVal instanceof MemberScore)
				{
					parent.openTreeAtMember((IMetaMember)newVal.getKey());
				}
			}
		});

		vbox.getChildren().add(comboBox);
		vbox.getChildren().add(tableView);

		tableView.prefHeightProperty().bind(scene.heightProperty());

		setScene(scene);
		show();

		redraw();
	}

	private void buildTableView(ITopListVisitable visitable)
	{
		topList.clear();
		topList.addAll(visitable.buildTopList());
	}

	public void redraw()
	{
	}
}