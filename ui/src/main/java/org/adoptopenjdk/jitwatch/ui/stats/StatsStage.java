/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.stats;

import org.adoptopenjdk.jitwatch.model.JITStats;
import org.adoptopenjdk.jitwatch.ui.main.JITWatchUI;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class StatsStage extends Stage
{
	private TableView<StatsTableRow> tableView;
	private ObservableList<StatsTableRow> obList = FXCollections.observableArrayList();
	
	private JITWatchUI parent;
	
	public StatsStage(final JITWatchUI parent)
	{
		initStyle(StageStyle.DECORATED);
	
		this.parent = parent;
		
		VBox vbox = new VBox();
		
		Scene scene = UserInterfaceUtil.getScene(vbox, 800, 480);
	
		tableView = StatsTableBuilder.buildTableStats(obList);
		
		vbox.getChildren().add(tableView);
		
		tableView.prefHeightProperty().bind(scene.heightProperty());

		setTitle("JITWatch Compilation Statistics");
		
		setScene(scene);
		
		redraw();
	}
	
	public final void redraw()
	{
		obList.clear();
		
		JITStats stats = parent.getJITDataModel().getJITStats();
		
		obList.add(makeRow("Public methods compiled", stats.getCountPublic()));
		obList.add(makeRow("Private methods compiled", stats.getCountPrivate()));
		obList.add(makeRow("Protected methods compiled", stats.getCountProtected()));
		obList.add(makeRow("Static methods compiled", stats.getCountStatic()));
		obList.add(makeRow("Final methods compiled", stats.getCountFinal()));
		obList.add(makeRow("Synchronized methods compiled", stats.getCountSynchronized()));
		obList.add(makeRow("Strictfp methods compiled", stats.getCountStrictfp()));
		obList.add(makeRow("Native methods compiled", stats.getCountNative()));

		obList.add(makeRow("C1 Compiled", stats.getCountC1()));
		obList.add(makeRow("C2 Compiled", stats.getCountC2()));
		obList.add(makeRow("OSR Compiled", stats.getCountOSR()));
		obList.add(makeRow("C2N Compiled", stats.getCountC2N()));
		obList.add(makeRow("Compiler Threads", stats.getCountCompilerThreads()));
		
		
		// * = Only have the queued timestamp and compiled timestamp
		// JIT Time assumes the entire interval was spent compiling
		obList.add(makeRow("Total JIT Time*", stats.getTotalCompileTime()));
		
		
		obList.add(makeRow("Native bytes generated", stats.getNativeBytes()));		

		obList.add(makeRow("Loaded Classes", stats.getCountClass()));		
		obList.add(makeRow("Total Methods Loaded", stats.getCountMethod()));		
		obList.add(makeRow("Total Constructors Loaded", stats.getCountConstructor()));		
	}
	
	private StatsTableRow makeRow(String name, long value)
	{
		return new StatsTableRow(name, value);
	}
}