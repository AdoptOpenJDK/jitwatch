package com.chrisnewland.jitwatch.ui;

import java.util.List;

import com.chrisnewland.jitwatch.core.JITWatchConfig;
import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.Journal;
import com.chrisnewland.jitwatch.model.MetaClass;

import javafx.application.Platform;
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

        MenuItem menuItemSource = new MenuItem("Show Source");
        MenuItem menuItemBytecode = new MenuItem("Show Bytecode");
        MenuItem menuItemNative = new MenuItem("Show Native Code");
        MenuItem menuItemJournal = new MenuItem("Show JIT Journal");

        contextMenuCompiled.getItems().add(menuItemSource);
        contextMenuCompiled.getItems().add(menuItemBytecode);
        contextMenuCompiled.getItems().add(menuItemNative);
        contextMenuCompiled.getItems().add(menuItemJournal);

        contextMenuNotCompiled.getItems().add(menuItemSource);
        contextMenuNotCompiled.getItems().add(menuItemBytecode);
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

        menuItemSource.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {
                parent.openSource(memberList.getSelectionModel().getSelectedItem());
            }
        });

        menuItemBytecode.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {
                parent.openBytecode(memberList.getSelectionModel().getSelectedItem());
            }
        });

        menuItemNative.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {
                parent.openNativeCode(memberList.getSelectionModel().getSelectedItem());
            }
        });

        menuItemJournal.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {
                IMetaMember member = memberList.getSelectionModel().getSelectedItem();

                String compileID = member.getQueuedAttribute("compile_id");

                Journal journal = parent.getJournal(compileID);

                StringBuilder builder = new StringBuilder();

                for (String entry : journal.getEntryList())
                {
                    builder.append(entry).append("\n");
                }

                parent.openTextViewer("JIT Journal for " + member.toString(), builder.toString());
            }
        });

        getChildren().add(cbOnlyCompiled);
        getChildren().add(memberList);

        memberList.prefHeightProperty().bind(heightProperty());
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

        // needed as SelectionModel selected index
        // is not updated instantly on select()
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                memberList.scrollTo(memberList.getSelectionModel().getSelectedIndex());
            }
        });
    }

    static class MetaMethodCell extends ListCell<IMetaMember>
    {
        @Override
        public void updateItem(IMetaMember item, boolean empty)
        {
            super.updateItem(item, empty);

            if (item != null)
            {
                setText(item.toStringUnqualifiedMethodName());

                if (isSelected())
                {
                    setStyle("-fx-background-color: blue; -fx-text-fill: white;");
                }
                else if (item.isCompiled())
                {
                    setStyle("-fx-text-fill:red;");
                }
                else
                {
                    setStyle("-fx-text-fill:black;");
                }
            }
        }
    }

}
