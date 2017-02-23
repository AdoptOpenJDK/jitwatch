/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.triview;

import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.model.MetaPackage;
import org.adoptopenjdk.jitwatch.model.PackageManager;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;

public class ClassSearch extends Region
{
	private TextField tfSearch;
	private ContextMenu dropMenu = new ContextMenu();
	private PackageManager pm;
	private TriView triView;

	private static final int MAX_SEARCH_RESULTS = 20;

	private boolean ignoreChanges = false;

	public ClassSearch(TriView triView, PackageManager pm)
	{
		this.triView = triView;
		this.pm = pm;

		tfSearch = new TextField();
		tfSearch.setPromptText("Enter class name");
		tfSearch.prefWidthProperty().bind(widthProperty());

		getChildren().add(tfSearch);

		tfSearch.setOnKeyReleased(new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent keyEvent)
			{
				if (keyEvent.getCode() == KeyCode.DOWN)
				{
					dropMenu.requestFocus();
				}
			}
		});

		tfSearch.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				if (!ignoreChanges)
				{
					if (tfSearch.getText().length() == 0)
					{
						if (dropMenu != null)
						{
							dropMenu.hide();
						}
					}
					else
					{
						performActionUsingSearchResults();
					}
				}
			}
		});
	}
	
	public void clear()
	{
		tfSearch.clear();
	}

	private void performActionUsingSearchResults()
	{
		List<String> results = search(tfSearch.getText());

		if (results.size() > 0)
		{
			buildResultsMenu(results);

			if (!dropMenu.isShowing())
			{
				dropMenu.show(this, Side.BOTTOM, 10, -5);
			}
		}
		else
		{
			dropMenu.hide();
		}
	}

	public void setText(String text)
	{
		// yuck
		ignoreChanges = true;

		tfSearch.setText(text);

		ignoreChanges = false;
	}

	private List<String> search(String term)
	{
		List<String> results = new ArrayList<>();

		List<MetaPackage> roots = pm.getRootPackages();

		for (MetaPackage mp : roots)
		{
			walkTree(mp, results, term.toLowerCase());

			if (results.size() >= MAX_SEARCH_RESULTS)
			{
				break;
			}
		}

		return results;
	}

	private void walkTree(MetaPackage mp, List<String> results, String term)
	{
		if (results.size() >= MAX_SEARCH_RESULTS)
		{
			return;
		}

		List<MetaPackage> childPackages = mp.getChildPackages();

		for (MetaPackage childPackage : childPackages)
		{
			walkTree(childPackage, results, term);
		}

		List<MetaClass> packageClasses = mp.getPackageClasses();

		for (MetaClass mc : packageClasses)
		{
			if (mc.getFullyQualifiedName().toLowerCase().contains(term))
			{
				results.add(mc.getFullyQualifiedName());

				if (results.size() >= MAX_SEARCH_RESULTS)
				{
					break;
				}
			}
		}
	}

	private void buildResultsMenu(List<String> items)
	{
		dropMenu.getItems().clear();

		for (String item : items)
		{
			final MenuItem mi = new MenuItem(item);
			mi.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent e)
				{
					dropMenu.hide();

					MetaClass metaClass = pm.getMetaClass(mi.getText());
					triView.setMetaClass(metaClass);
				}
			});

			dropMenu.getItems().add(mi);
		}
	}
}
