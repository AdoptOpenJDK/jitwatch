/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */

package org.adoptopenjdk.jitwatch.ui.compilationchooser;

import java.util.List;

import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.ui.main.IMemberSelectedListener;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

public class CompilationChooser
{
	private ObservableList<String> comboCompilationList = FXCollections.observableArrayList();

	private ComboBox<String> comboSelectedCompilation;

	private IMetaMember member;

	private boolean ignoreChange = false;

	public CompilationChooser(final IMemberSelectedListener selectionListener)
	{
		comboSelectedCompilation = new ComboBox<>(comboCompilationList);

		comboSelectedCompilation.setStyle("-fx-font-size: 10px");

		comboSelectedCompilation.valueProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal)
			{
				int index = comboSelectedCompilation.getSelectionModel().getSelectedIndex();

				if (!ignoreChange)
				{
					selectionListener.selectCompilation(member, index);
				}
			}
		});
	}

	public synchronized void clear()
	{
		ignoreChange = true;
		
		comboCompilationList.clear();
		comboSelectedCompilation.getSelectionModel().clearSelection();
		
		ignoreChange = false;
	}

	public void setVisible(boolean visible)
	{
		comboSelectedCompilation.setVisible(visible);
	}

	public synchronized void compilationChanged(IMetaMember member)
	{
		ignoreChange = true;

		comboSelectedCompilation.getSelectionModel().clearSelection();
		comboCompilationList.clear();

		if (member != null)
		{
			this.member = member;

			List<Compilation> compilations = member.getCompilations();

			for (Compilation compilation : compilations)
			{
				comboCompilationList.add(compilation.getSignature());
			}

			Compilation selectedCompilation = member.getSelectedCompilation();

			if (selectedCompilation != null)
			{
				comboSelectedCompilation.getSelectionModel().select(selectedCompilation.getIndex());
			}
		}

		ignoreChange = false;
	}

	public ComboBox<String> getCombo()
	{
		return comboSelectedCompilation;
	}
}