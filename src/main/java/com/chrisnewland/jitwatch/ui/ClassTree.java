package com.chrisnewland.jitwatch.ui;

import java.util.List;

import com.chrisnewland.jitwatch.core.JITWatchConfig;
import com.chrisnewland.jitwatch.model.MetaClass;
import com.chrisnewland.jitwatch.model.MetaPackage;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
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

        CheckBox cbHideInterfaces = new CheckBox("Hide Interfaces");
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

        getChildren().add(cbHideInterfaces);
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

                if (child.getValue() instanceof MetaPackage && value instanceof MetaClass)
                {

                }
                else
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

            if (value instanceof MetaClass && ((MetaClass) value).isMissingDef())
            {
                // indicate missing class definition?
            }
        }

        return found;
    }

    public void showTree()
    {
        List<MetaPackage> roots = parent.getPackageManager().getRootPackages();

        for (MetaPackage mp : roots)
        {
            showTree(rootItem, mp);
        }
    }

    private void showTree(TreeItem<Object> currentNode, MetaPackage mp)
    {
        TreeItem<Object> packageItem = findOrCreateTreeItem(currentNode, mp);

        List<MetaPackage> childPackages = mp.getChildPackages();

        for (MetaPackage childPackage : childPackages)
        {
            showTree(packageItem, childPackage);
        }

        List<MetaClass> packageClasses = mp.getPackageClasses();

        for (MetaClass packageClass : packageClasses)
        {
            if (!config.isHideInterfaces() || !packageClass.isInterface())
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
