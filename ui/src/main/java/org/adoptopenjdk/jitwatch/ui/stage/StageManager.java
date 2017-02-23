/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.adoptopenjdk.jitwatch.ui.report.ReportStage;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class StageManager
{
	private static Map<Stage, List<Stage>> openStages = new HashMap<>();

	private static List<IStageClosedListener> listeners = new ArrayList<>();

	private StageManager()
	{
	}

	public static void registerListener(IStageClosedListener listener)
	{
		listeners.add(listener);
	}

	private static void notifyListeners(Stage stage)
	{
		for (IStageClosedListener listener : listeners)
		{
			listener.handleStageClosed(stage);
		}
	}

	public static void clearReportStages()
	{
		for (Stage rootStage : openStages.keySet())
		{
			clearChildren(rootStage);
		}
	}

	public static void clearChildren(Stage stage)
	{
		if (openStages.containsKey(stage))
		{
			for (Stage childStage : openStages.get(stage))
			{
				clearChildren(childStage);
			}
		}

		if (stage instanceof ReportStage)
		{
			((ReportStage) stage).clear();
		}
	}

	// Adds a close buttong to stages
	// for fullscreen JavaFX systems with no window decorations
	private static void addCloseButton(final Stage stage)
	{
		Scene scene = stage.getScene();

		Parent rootNode = scene.getRoot();

		Button btnClose = new Button("X");

		btnClose.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				closeStage(stage);
			}
		});

		HBox hbox = new HBox();
		hbox.setSpacing(16);
		hbox.getChildren().addAll(btnClose, new Label(stage.getTitle()));

		if (rootNode instanceof BorderPane)
		{
			BorderPane pane = (BorderPane) rootNode;

			Node topNode = pane.getTop();

			if (topNode instanceof VBox)
			{
				VBox vbox = (VBox) topNode;
				vbox.getChildren().add(0, hbox);
			}
			else
			{
				VBox newTopNode = new VBox();
				newTopNode.setPadding(new Insets(0));

				newTopNode.getChildren().addAll(hbox, topNode);
				pane.setTop(newTopNode);
			}
		}
		else
		{
			VBox newTopNode = new VBox();
			newTopNode.setPadding(new Insets(0));

			newTopNode.getChildren().addAll(hbox, rootNode);

			scene.setRoot(newTopNode);
		}
	}

	public static void addAndShow(final Stage parent, final Stage childStage)
	{
		List<Stage> childrenOfParent = openStages.get(parent);

		if (childrenOfParent == null)
		{
			childrenOfParent = new ArrayList<>();
			openStages.put(parent, childrenOfParent);
		}

		childrenOfParent.add(childStage);

		if (UserInterfaceUtil.ADD_CLOSE_DECORATION)
		{
			addCloseButton(childStage);
		}

		childStage.setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				closeStage(childStage);
			}
		});

		childStage.show();

		double parentX = parent.getX();
		double parentY = parent.getY();
		double parentWidth = parent.getWidth();
		double parentHeight = parent.getHeight();

		double childWidth = childStage.getWidth();
		double childHeight = childStage.getHeight();

		double childX = parentX + (parentWidth - childWidth) / 2;
		double childY = parentY + (parentHeight - childHeight) / 2;

		childStage.setX(childX);
		childStage.setY(childY);
	}

	public static EventHandler<ActionEvent> getCloseHandler(final Stage stage)
	{
		return new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				StageManager.closeStage(stage);
			}
		};
	}

	public static void closeStage(Stage stage)
	{
		List<Stage> childrenOfParent = openStages.get(stage);

		if (childrenOfParent != null)
		{
			for (Stage child : childrenOfParent)
			{
				closeStage(child);
			}
		}

		notifyListeners(stage);

		openStages.remove(stage);

		stage.close();
	}
}