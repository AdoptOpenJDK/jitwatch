/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui;

import java.util.List;

import com.chrisnewland.jitwatch.core.JITWatchConfig;
import com.chrisnewland.jitwatch.model.MetaClass;
import com.chrisnewland.jitwatch.model.MetaPackage;
import com.chrisnewland.jitwatch.util.UserInterfaceUtil;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ClassTree extends VBox
{
    private TreeView<Object> treeView;
    private TreeItem<Object> rootItem;

    private JITWatchUI parent;
    private JITWatchConfig config;

    public ClassTree(final JITWatchUI parent, final JITWatchConfig config)
    {
        this.parent = parent;
        this.config = config;

        CheckBox cbHideInterfaces = new CheckBox("Hide interfaces");
        cbHideInterfaces.setMaxWidth(280);
        cbHideInterfaces.setTooltip(new Tooltip("Hide interfaces from the class tree."));
        cbHideInterfaces.setSelected(config.isHideInterfaces());
        cbHideInterfaces.selectedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal)
            {
                config.setHideInterfaces(newVal);
                config.saveConfig();

                parent.clearAndRefresh();
            }
        });

        cbHideInterfaces.setStyle("-fx-background-color:#dddddd; -fx-padding:4px");
        cbHideInterfaces.prefWidthProperty().bind(widthProperty());

        CheckBox cbHideUncompiled = new CheckBox("Hide uncompiled classes");
        cbHideUncompiled.setTooltip(new Tooltip("Hide classes with no JIT-compiled members from the class tree."));
        cbHideUncompiled.setSelected(config.isShowOnlyCompiledClasses());
        cbHideUncompiled.selectedProperty().addListener(new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal)
            {
                config.setShowOnlyCompiledClasses(newVal);
                config.saveConfig();

                parent.clearAndRefresh();
            }
        });

        cbHideUncompiled.setStyle("-fx-background-color:#dddddd; -fx-padding:4px");
        cbHideUncompiled.prefWidthProperty().bind(widthProperty());

        rootItem = new TreeItem<Object>("Packages");

        rootItem.setExpanded(true);

        treeView = new TreeView<Object>(rootItem);

        treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<Object>>()
        {
            @Override
            public void changed(ObservableValue<? extends TreeItem<Object>> observableValue, TreeItem<Object> oldItem,
                    TreeItem<Object> newItem)
            {
                if (newItem != null)
                {
                    Object value = newItem.getValue();

                    if (value instanceof MetaClass)
                    {
                        parent.refreshSelectedTreeNode((MetaClass) value);
                    }
                }
            }
        });
        
        HBox hboxOptions = new HBox();
        
        hboxOptions.getChildren().add(cbHideInterfaces);
        hboxOptions.getChildren().add(cbHideUncompiled);

        getChildren().add(hboxOptions);
        getChildren().add(treeView);

        treeView.prefHeightProperty().bind(heightProperty());
    }

    private TreeItem<Object> findOrCreateTreeItem(TreeItem<Object> parent, Object value)
    {
        ObservableList<TreeItem<Object>> children = parent.getChildren();

        TreeItem<Object> found = null;

        int placeToInsert = 0;
        boolean foundInsertPos = false;

        for (TreeItem<Object> child : children)
        {
            int stringCompare = child.getValue().toString().compareTo(value.toString());

            if (stringCompare == 0)
            {
                found = child;
                break;
            }
            else if (!foundInsertPos && stringCompare < 0)
            {
                // make sure sub packages listed before classes in this package

                if (not(child.getValue() instanceof MetaPackage && value instanceof MetaClass))
                {
                    placeToInsert++;
                }
            }
            else
            {
                if (child.getValue() instanceof MetaPackage && value instanceof MetaClass)
                {
                    placeToInsert++;
                }
                else
                {
                    foundInsertPos = true;
                }
            }
        }

        if (found == null)
        {
            found = new TreeItem<Object>(value);
            children.add(placeToInsert, found);

            //TODO indicate missing class definition?
        }

        boolean hasCompiledChildren = false;

        if (value instanceof MetaPackage && ((MetaPackage) value).hasCompiledClasses())
        {
            hasCompiledChildren = true;
        }
        else if (value instanceof MetaClass && ((MetaClass) value).hasCompiledMethods())
        {
            hasCompiledChildren = true;
        }

        if (UserInterfaceUtil.getTick() != null && hasCompiledChildren)
        {
            found.setGraphic(new ImageView(UserInterfaceUtil.getTick()));
        }

        return found;
    }

    private boolean not(boolean someCondition) {
        return !someCondition;
    }

    public void showTree()
    {
        List<MetaPackage> roots = parent.getPackageManager().getRootPackages();

        for (MetaPackage mp : roots)
        {
            boolean allowed = true;

            if (!mp.hasCompiledClasses() && config.isShowOnlyCompiledClasses())
            {
                allowed = false;
            }

            if (allowed)
            {
                showTree(rootItem, mp);
            }
        }
    }

    private void showTree(TreeItem<Object> currentNode, MetaPackage mp)
    {
        TreeItem<Object> packageItem = findOrCreateTreeItem(currentNode, mp);

        List<MetaPackage> childPackages = mp.getChildPackages();

        for (MetaPackage childPackage : childPackages)
        {
            boolean allowed = true;

            if (!childPackage.hasCompiledClasses() && config.isShowOnlyCompiledClasses())
            {
                allowed = false;
            }

            if (allowed)
            {
                showTree(packageItem, childPackage);
            }
        }

        List<MetaClass> packageClasses = mp.getPackageClasses();

        for (MetaClass packageClass : packageClasses)
        {
            boolean allowed = true;

            if (packageClass.isInterface() && config.isHideInterfaces())
            {
                allowed = false;
            }

            if (allowed &&
                    (!packageClass.hasCompiledMethods()
                            && config.isShowOnlyCompiledClasses()))
            {
                allowed = false;
            }

            if (allowed)
            {
                findOrCreateTreeItem(packageItem, packageClass);
            }
        }
    }

    public void select(TreeItem<Object> node)
    {
        treeView.getSelectionModel().select(node);
    }

    public void scrollTo(int rowsAbove)
    {
        treeView.scrollTo(rowsAbove);
    }

    public TreeItem<Object> getRootItem()
    {
        return rootItem;
    }

    public void clear()
    {
        rootItem.getChildren().clear();
    }
}
