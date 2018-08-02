/*
 * Copyright (c) 2018 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */

package org.adoptopenjdk.jitwatch.ui.parserchooser;

import org.adoptopenjdk.jitwatch.parser.ParserType;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

public class ParserChooser
{
	private ObservableList<ParserType> parserTypeList = FXCollections.observableArrayList();

	private ComboBox<ParserType> comboParser;

	public ParserChooser(final IParserSelectedListener selectionListener)
	{
		comboParser = new ComboBox<>(parserTypeList);

		clear();
		
		comboParser.setStyle("-fx-font-size: 10px");

		comboParser.valueProperty().addListener(new ChangeListener<ParserType>()
		{
			@Override
			public void changed(ObservableValue<? extends ParserType> ov, ParserType oldVal, ParserType newVal)
			{
				selectionListener.parserSelected(newVal);
			}
		});
	}

	public synchronized void clear()
	{
		parserTypeList.clear();

		parserTypeList.addAll(ParserType.values());

		comboParser.getSelectionModel().clearSelection();
	}

	public ComboBox<ParserType> getCombo()
	{
		return comboParser;
	}
}