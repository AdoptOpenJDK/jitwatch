/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

import java.util.List;
import java.util.Map;

import org.adoptopenjdk.jitwatch.core.IntrinsicFinder;
import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.Journal;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class ClassMemberList extends VBox
{
	private CheckBox cbOnlyCompiled;
	private ListView<IMetaMember> memberList;
	private MetaClass metaClass = null;
	private JITWatchConfig config;

	public ClassMemberList(final JITWatchUI parent, final JITWatchConfig config)
	{
		this.config = config;

		cbOnlyCompiled = new CheckBox("Hide non JIT-compiled class members");
		cbOnlyCompiled.setTooltip(new Tooltip("Hide class members (methods and constructors) that were not JIT-compiled."));

		cbOnlyCompiled.setSelected(config.isShowOnlyCompiledMembers());

		cbOnlyCompiled.selectedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal)
			{
				config.setShowOnlyCompiledMembers(newVal);
				config.saveConfig();
				refresh();
			}
		});

		cbOnlyCompiled.setStyle("-fx-background-color:#dddddd; -fx-padding:4px");
		cbOnlyCompiled.prefWidthProperty().bind(widthProperty());

		memberList = new ListView<IMetaMember>();
		memberList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<IMetaMember>()
		{
			@Override
			public void changed(ObservableValue<? extends IMetaMember> arg0, IMetaMember oldVal, IMetaMember newVal)
			{
				parent.showMemberInfo(newVal);
			}
		});

		memberList.getItems().addListener(new ListChangeListener<IMetaMember>()
		{

			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends IMetaMember> arg0)
			{
				setScrollBar();
			}
		});

		memberList.setCellFactory(new Callback<ListView<IMetaMember>, ListCell<IMetaMember>>()
		{
			@Override
			public ListCell<IMetaMember> call(ListView<IMetaMember> arg0)
			{
				return new MetaMethodCell();
			}
		});

		final ContextMenu contextMenuCompiled = new ContextMenu();
		final ContextMenu contextMenuNotCompiled = new ContextMenu();

		MenuItem menuItemTriView = new MenuItem("Show TriView");
		MenuItem menuItemJournal = new MenuItem("Show JIT journal");
		MenuItem menuItemIntrinsics = new MenuItem("Show intrinsics used");
		MenuItem menuItemCallChain = new MenuItem("Show compile chain");

		contextMenuCompiled.getItems().add(menuItemTriView);
		contextMenuCompiled.getItems().add(menuItemJournal);
		contextMenuCompiled.getItems().add(menuItemIntrinsics);
		contextMenuCompiled.getItems().add(menuItemCallChain);

		contextMenuNotCompiled.getItems().add(menuItemTriView);
		contextMenuNotCompiled.getItems().add(menuItemJournal);

		memberList.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent e)
			{
				if (e.getButton() == MouseButton.SECONDARY)
				{
					if (memberList.getSelectionModel().getSelectedItem().isCompiled())
					{
						contextMenuCompiled.show(memberList, e.getScreenX(), e.getScreenY());
					}
					else
					{
						contextMenuNotCompiled.show(memberList, e.getScreenX(), e.getScreenY());
					}
				}
			}
		});

		menuItemTriView.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				parent.openTriView(memberList.getSelectionModel().getSelectedItem(), false);
			}
		});

		menuItemJournal.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				IMetaMember member = memberList.getSelectionModel().getSelectedItem();

				Journal journal = member.getJournal();

				parent.openJournalViewer("JIT Journal for " + member.toString(), journal);
			}
		});

		menuItemIntrinsics.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				IMetaMember member = memberList.getSelectionModel().getSelectedItem();

				Journal journal = member.getJournal();

				String intrinsicsUsed = processIntrinsicsUsing(journal);

				parent.openTextViewer("Intrinsics used by " + member.toString(), intrinsicsUsed);
			}
		});

		menuItemCallChain.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				parent.openCompileChain(memberList.getSelectionModel().getSelectedItem());
			}
		});

		getChildren().add(cbOnlyCompiled);
		getChildren().add(memberList);

		memberList.prefHeightProperty().bind(heightProperty());
	}

	private String processIntrinsicsUsing(Journal journal)
	{
		StringBuilder builder = new StringBuilder();
		
		Map<String, String> intrinsics = IntrinsicFinder.findIntrinsics(journal);

		if (intrinsics.size() > 0)
		{
			addArrowWithNewLineToEachIntrinsicEntry(builder, intrinsics);
		}
		else
		{
			builder.append("No intrinsics used in this method");
		}
		
		return builder.toString();
	}

	private void addArrowWithNewLineToEachIntrinsicEntry(StringBuilder builder, Map<String, String> intrinsics)
	{
		for (Map.Entry<String, String> entry : intrinsics.entrySet())
		{
			builder.append(entry.getKey()).append(" -> ").append(entry.getValue()).append(C_NEWLINE);
		}
	}

	public void setMetaClass(MetaClass metaClass)
	{
		this.metaClass = metaClass;
		refresh();
	}

	private void refresh()
	{
		clearClassMembers();

		if (metaClass != null)
		{
			List<IMetaMember> metaMembers = metaClass.getMetaMembers();

			for (IMetaMember member : metaMembers)
			{
				if (member.isCompiled() || !config.isShowOnlyCompiledMembers())
				{
					addMember(member);
				}
			}
		}
	}

	private void addMember(IMetaMember member)
	{
		memberList.getItems().add(member);
	}

	public void clearClassMembers()
	{
		memberList.getItems().clear();
	}

	public void selectMember(IMetaMember selected)
	{
		memberList.getSelectionModel().select(selected);

		setScrollBar();
	}

	private void setScrollBar()
	{
		int index = memberList.getSelectionModel().getSelectedIndex();

		memberList.scrollTo(index);
	}

	static class MetaMethodCell extends ListCell<IMetaMember>
	{
		@Override
		public void updateItem(IMetaMember item, boolean empty)
		{
			super.updateItem(item, empty);

			if (item == null)
			{
				setText(S_EMPTY);
				setGraphic(null);
			}
			else
			{
				setText(item.toStringUnqualifiedMethodName(false));

				if (item.isCompiled())
				{
					setGraphic(new ImageView(UserInterfaceUtil.getTick()));
				}
				else
				{
					setGraphic(null);
				}
			}
		}
	}
}