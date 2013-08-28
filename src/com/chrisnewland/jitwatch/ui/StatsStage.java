package com.chrisnewland.jitwatch.ui;

import com.chrisnewland.jitwatch.core.JITStats;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class StatsStage extends Stage
{
	private ListView<Label> listView;
	private JITWatchUI parent;
	
	public StatsStage(final JITWatchUI parent)
	{
		initStyle(StageStyle.DECORATED);
	
		this.parent = parent;
		
		VBox vbox = new VBox();

		Scene scene = new Scene(vbox, 360, 340);
		
		listView = new ListView<Label>();
		
		vbox.getChildren().add(listView);

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
		listView.getItems().clear();
		
		JITStats stats = parent.getJITStats();
		
		listView.getItems().add(makeLabel("Public", stats.getCountPublic()));
		listView.getItems().add(makeLabel("Private", stats.getCountPrivate()));
		listView.getItems().add(makeLabel("Protected", stats.getCountProtected()));
		listView.getItems().add(makeLabel("Static", stats.getCountStatic()));
		listView.getItems().add(makeLabel("Final", stats.getCountFinal()));
		listView.getItems().add(makeLabel("Synchronized", stats.getCountSynchronized()));
		listView.getItems().add(makeLabel("Strictfp", stats.getCountStrictfp()));
		listView.getItems().add(makeLabel("Native", stats.getCountNative()));
		//listView.getItems().add(makeLabel("Abstract", stats.getCountAbstract()));
		
		listView.getItems().add(makeLabel("C1 Compiled", stats.getCountC1()));
		listView.getItems().add(makeLabel("C2 Compiled", stats.getCountC2()));
		listView.getItems().add(makeLabel("OSR", stats.getCountOSR()));
		listView.getItems().add(makeLabel("C2N Compiled", stats.getCountC2N()));	
		listView.getItems().add(makeLabel("Total JIT Time", stats.getTotalCompileTime()));
		listView.getItems().add(makeLabel("Native bytes", stats.getNativeBytes()));		
	}
	
	private Label makeLabel(String title, long value)
	{
		return new Label(title + ": " + value);
	}

}