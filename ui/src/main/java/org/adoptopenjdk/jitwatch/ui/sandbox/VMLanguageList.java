/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.sandbox;

import java.util.Collections;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.jvmlang.LanguageManager;
import org.adoptopenjdk.jitwatch.ui.stage.IStageClosedListener;
import org.adoptopenjdk.jitwatch.ui.stage.StageManager;

public class VMLanguageList extends FlowPane implements IStageClosedListener
{
	private JITWatchConfig config;

	private VMLanguageConfigStage openVMLCStage = null;
	
	private Stage parent;

	public VMLanguageList(Stage parent, final JITWatchConfig config)
	{
		this.config = config;
		this.parent = parent;

		setVgap(10);
		setHgap(20);

		setMaxWidth(600);

		setPadding(new Insets(0,0,5,0));

		updateList();
	}

	@Override
	public void handleStageClosed(Stage stage)
	{
		openVMLCStage = null;
		updateList();
	}

	private void updateList()
	{
		List<String> vmLanguageList = config.getVMLanguageList();

		Collections.sort(vmLanguageList);

		getChildren().clear();

		for (final String lang : vmLanguageList)
		{
			Button button = new Button(lang);
			button.setPrefWidth(120);

			if (!LanguageManager.isLanguageEnabled(lang))
			{
				button.setDisable(true);
			}

			button.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent e)
				{
					if (openVMLCStage == null)
					{
						openVMLCStage = new VMLanguageConfigStage(VMLanguageList.this, config, lang);
						StageManager.addAndShow(parent, openVMLCStage);
					}
				}
			});

			getChildren().add(button);
		}
	}
}