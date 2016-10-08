/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.report;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class ReportPackageFilter extends HBox
{
	private TextField tfFilter;

	public ReportPackageFilter(final ReportStage parent)
	{
		setPadding(new Insets(4));
		setSpacing(4);

		Label label = new Label("Filter on package prefixes (comma separated)");
		tfFilter = new TextField();
		
		getChildren().add(label);
		getChildren().add(tfFilter);
		
		tfFilter.prefWidthProperty().bind(widthProperty().multiply(0.35));
		tfFilter.prefWidthProperty().bind(widthProperty().multiply(0.65));

		tfFilter.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				if (newValue != null)
				{
					parent.setFilter(newValue);
				}
			}
		});
	}
}