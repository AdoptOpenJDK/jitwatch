/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/blob/master/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
package com.chrisnewland.jitwatch.ui;

import com.chrisnewland.jitwatch.core.JITStats;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class StatsStage extends Stage
{
	private TableView<AttributeTableRow2Col> tableView;
	private ObservableList<AttributeTableRow2Col> obList = FXCollections.observableArrayList();
	
	private JITWatchUI parent;
	
	public StatsStage(final JITWatchUI parent)
	{
		initStyle(StageStyle.DECORATED);
	
		this.parent = parent;
		
		VBox vbox = new VBox();
		
		Scene scene = new Scene(vbox, 512, 450);
	
		tableView = TableUtil.buildTableStats(obList);
		
		vbox.getChildren().add(tableView);
		
		tableView.prefHeightProperty().bind(scene.heightProperty());

		setTitle("JITWatch Method Statistics");
		
		setScene(scene);
		
		redraw();

		setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				parent.handleStageClosed(StatsStage.this);
			}
		});
	}
	
	public void redraw()
	{
		obList.clear();
		
		JITStats stats = parent.getJITStats();
		
		obList.add(makeRow("Public methods compiled", stats.getCountPublic()));
		obList.add(makeRow("Private methods compiled", stats.getCountPrivate()));
		obList.add(makeRow("Protected methods compiled", stats.getCountProtected()));
		obList.add(makeRow("Static methods compiled", stats.getCountStatic()));
		obList.add(makeRow("Final methods compiled", stats.getCountFinal()));
		obList.add(makeRow("Synchronized methods compiled", stats.getCountSynchronized()));
		obList.add(makeRow("Strictfp methods compiled", stats.getCountStrictfp()));
		obList.add(makeRow("Native methods compiled", stats.getCountNative()));
		//obList.add(makeRow("Abstract", stats.getCountAbstract()));
		
		obList.add(makeRow("C1 Compiled", stats.getCountC1()));
		obList.add(makeRow("C2 Compiled", stats.getCountC2()));
		obList.add(makeRow("OSR Compiled", stats.getCountOSR()));
		obList.add(makeRow("C2N Compiled", stats.getCountC2N()));	
		
		
		// * = Only have the queued timestamp and compiled timestamp
		// JIT Time assumes the entire interval was spent compiling
		obList.add(makeRow("Total JIT Time*", stats.getTotalCompileTime()));
		
		
		obList.add(makeRow("Native bytes generated", stats.getNativeBytes()));		

		obList.add(makeRow("Loaded Classes", stats.getCountClass()));		
		obList.add(makeRow("Total Methods Loaded", stats.getCountMethod()));		
		obList.add(makeRow("Total Constructors Loaded", stats.getCountConstructor()));		
	}
	
	private AttributeTableRow2Col makeRow(String name, long value)
	{
		return new AttributeTableRow2Col(name, value);
	}
}