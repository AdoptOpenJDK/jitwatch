/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.optimizedvcall;

import java.util.List;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import org.adoptopenjdk.jitwatch.optimizedvcall.OptimizedVirtualCall;
import org.adoptopenjdk.jitwatch.ui.JITWatchUI;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

public class OptimizedVirtualCallStage extends Stage
{
	private TableView<VCallRow> tableView;

	public OptimizedVirtualCallStage(final JITWatchUI parent, final List<OptimizedVirtualCall> optimizedVirtualCalls)
	{
		initStyle(StageStyle.DECORATED);

		setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				parent.handleStageClosed(OptimizedVirtualCallStage.this);
			}
		});

		VBox vbox = new VBox();
		vbox.setPadding(new Insets(8));
		vbox.setSpacing(8);

		Scene scene = UserInterfaceUtil.getScene(vbox, JITWatchUI.WINDOW_WIDTH, JITWatchUI.WINDOW_HEIGHT);

		setTitle("Optimized Virtual Calls");

		tableView = VCallTableBuilder.buildTable(parent, optimizedVirtualCalls);

		vbox.getChildren().add(tableView);

		tableView.prefHeightProperty().bind(scene.heightProperty());

		setScene(scene);
		show();
	}

	public final void redraw()
	{
	}
}