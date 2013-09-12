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
		
		Scene scene = new Scene(vbox, 360, 450);
	
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
		
		obList.add(makeRow("Public", stats.getCountPublic()));
		obList.add(makeRow("Private", stats.getCountPrivate()));
		obList.add(makeRow("Protected", stats.getCountProtected()));
		obList.add(makeRow("Static", stats.getCountStatic()));
		obList.add(makeRow("Final", stats.getCountFinal()));
		obList.add(makeRow("Synchronized", stats.getCountSynchronized()));
		obList.add(makeRow("Strictfp", stats.getCountStrictfp()));
		obList.add(makeRow("Native", stats.getCountNative()));
		//obList.add(makeRow("Abstract", stats.getCountAbstract()));
		
		obList.add(makeRow("C1 Compiled", stats.getCountC1()));
		obList.add(makeRow("C2 Compiled", stats.getCountC2()));
		obList.add(makeRow("OSR", stats.getCountOSR()));
		obList.add(makeRow("C2N Compiled", stats.getCountC2N()));	
		obList.add(makeRow("Total JIT Time", stats.getTotalCompileTime()));
		obList.add(makeRow("Native bytes", stats.getNativeBytes()));		

		obList.add(makeRow("Loaded Classes", stats.getCountClass()));		
		obList.add(makeRow("Total Methods", stats.getCountMethod()));		
		obList.add(makeRow("Total Constructors", stats.getCountConstructor()));		

	}
	
	private AttributeTableRow2Col makeRow(String name, long value)
	{
		return new AttributeTableRow2Col(name, value);
	}
}