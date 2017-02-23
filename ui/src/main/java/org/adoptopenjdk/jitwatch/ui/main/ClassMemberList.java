/*
 * Copyright (c) 2013-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.main;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.intrinsic.IntrinsicFinder;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

public class ClassMemberList extends VBox
{
	private CheckBox cbOnlyCompiled;
	private ListView<IMetaMember> memberList;
	private MetaClass metaClass = null;
	private JITWatchConfig config;

	private boolean selectedProgrammatically = false;

	private List<IMemberSelectedListener> listeners = new ArrayList<>();

	public void registerListener(IMemberSelectedListener listener)
	{
		listeners.add(listener);
	}

	public void clear()
	{
		if (memberList != null)
		{
			memberList.getItems().clear();
		}

		metaClass = null;
	}

	private void notifyListeners(IMetaMember member, boolean openTriView)
	{
		for (IMemberSelectedListener listener : listeners)
		{
			listener.setSelectedMetaMember(member, openTriView);
		}
	}

	public ClassMemberList(final IStageAccessProxy parent, final JITWatchConfig config)
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
				if (!selectedProgrammatically)
				{
					notifyListeners(newVal, true);
				}
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

		final ContextMenu menuCompiled = buildContextMenuCompiledMember(parent);
		final ContextMenu menuUncompiled = buildContextMenuUncompiledMember(parent);

		memberList.addEventHandler(MouseEvent.MOUSE_CLICKED, getEventHandlerContextMenu(menuCompiled, menuUncompiled));

		memberList.addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent e)
			{
				menuCompiled.hide();
				menuUncompiled.hide();
			}
		});

		getChildren().add(cbOnlyCompiled);
		getChildren().add(memberList);

		memberList.prefHeightProperty().bind(heightProperty());
	}

	private EventHandler<MouseEvent> getEventHandlerContextMenu(final ContextMenu contextMenuCompiled,
			final ContextMenu contextMenuNotCompiled)
	{
		return new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent e)
			{
				if (e.getButton() == MouseButton.SECONDARY)
				{
					IMetaMember selectedMember = memberList.getSelectionModel().getSelectedItem();

					if (selectedMember.isCompiled())
					{
						contextMenuCompiled.show(memberList, e.getScreenX(), e.getScreenY());
					}
					else
					{
						contextMenuNotCompiled.show(memberList, e.getScreenX(), e.getScreenY());
					}
				}
			}
		};
	}

	private ContextMenu buildContextMenuCompiledMember(IStageAccessProxy parent)
	{
		final ContextMenu menu = new ContextMenu();

		MenuItem menuItemTriView = new MenuItem("Show TriView");
		MenuItem menuItemInlinedInto = new MenuItem("Show inlined into");

		MenuItem menuItemIntrinsics = new MenuItem("Show intrinsics used");
		MenuItem menuItemCallChain = new MenuItem("Show compile chain");
		MenuItem menuItemOptimizedVCalls = new MenuItem("Show optimized virtual calls");

		menu.getItems().add(menuItemTriView);
		menu.getItems().add(menuItemInlinedInto);
		menu.getItems().add(menuItemIntrinsics);
		menu.getItems().add(menuItemCallChain);
		menu.getItems().add(menuItemOptimizedVCalls);

		menuItemTriView.setOnAction(getEventHandlerMenuItemTriView(parent));

		menuItemInlinedInto.setOnAction(getEventHandlerMenuItemInlinedInto(parent));

		menuItemIntrinsics.setOnAction(getEventHandlerMenuItemIntrinsics(parent));

		menuItemCallChain.setOnAction(getEventHandlerMenuItemCallChain(parent));

		menuItemOptimizedVCalls.setOnAction(getEventHandlerMenuItemOptimizedVCall(parent));

		return menu;
	}

	private ContextMenu buildContextMenuUncompiledMember(IStageAccessProxy parent)
	{
		ContextMenu menu = new ContextMenu();

		MenuItem menuItemTriView = new MenuItem("Show TriView");
		MenuItem menuItemInlinedInto = new MenuItem("Show inlined into");

		menu.getItems().add(menuItemTriView);
		menu.getItems().add(menuItemInlinedInto);

		menuItemTriView.setOnAction(getEventHandlerMenuItemTriView(parent));
		menuItemInlinedInto.setOnAction(getEventHandlerMenuItemInlinedInto(parent));

		return menu;
	}

	private EventHandler<ActionEvent> getEventHandlerMenuItemTriView(final IStageAccessProxy parent)
	{
		return new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				parent.openTriView(memberList.getSelectionModel().getSelectedItem(), false);
			}
		};
	}

	private EventHandler<ActionEvent> getEventHandlerMenuItemInlinedInto(final IStageAccessProxy parent)
	{
		return new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				parent.openInlinedIntoReport(memberList.getSelectionModel().getSelectedItem());
			}
		};
	}

	private EventHandler<ActionEvent> getEventHandlerMenuItemIntrinsics(final IStageAccessProxy parent)
	{
		return new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				IMetaMember member = memberList.getSelectionModel().getSelectedItem();

				String intrinsicsUsed = findIntrinsicsUsedByMember(member);

				parent.openTextViewer("Intrinsics used by " + member.toString(), intrinsicsUsed, false, false);
			}
		};
	}

	private EventHandler<ActionEvent> getEventHandlerMenuItemCallChain(final IStageAccessProxy parent)
	{
		return new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				parent.openCompileChain(memberList.getSelectionModel().getSelectedItem());
			}
		};
	}

	private EventHandler<ActionEvent> getEventHandlerMenuItemOptimizedVCall(final IStageAccessProxy parent)
	{
		return new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				parent.openOptmizedVCallReport(memberList.getSelectionModel().getSelectedItem());
			}
		};
	}

	private String findIntrinsicsUsedByMember(IMetaMember member)
	{
		StringBuilder builder = new StringBuilder();

		IntrinsicFinder finder = new IntrinsicFinder();

		Map<String, String> intrinsics = finder.findIntrinsics(member);

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

		List<IMetaMember> members = metaClass.getMetaMembers();

		if (members.size() > 0)
		{
			selectMember(members.get(0));
		}

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
		selectedProgrammatically = true;

		memberList.getSelectionModel().clearSelection();

		for (int i = 0; i < memberList.getItems().size(); i++)
		{
			IMetaMember member = memberList.getItems().get(i);

			if (member.toString().equals(selected.toString()))
			{
				memberList.getSelectionModel().select(i);

				memberList.getFocusModel().focus(i);

				memberList.scrollTo(i);
			}
		}

		selectedProgrammatically = false;
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
				setText(item.toStringUnqualifiedMethodName(false, false));

				if (UserInterfaceUtil.IMAGE_TICK != null && item.isCompiled())
				{
					setGraphic(new ImageView(UserInterfaceUtil.IMAGE_TICK));
				}
				else
				{
					setGraphic(null);
				}
			}
		}
	}
}