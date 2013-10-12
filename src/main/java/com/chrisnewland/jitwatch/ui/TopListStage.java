/*
 * Copyright (c) 2013 Chris Newland. All rights reserved.
 * Licensed under https://github.com/chriswhocodes/jitwatch/blob/master/LICENSE-BSD
 * http://www.chrisnewland.com/jitwatch
 */
package com.chrisnewland.jitwatch.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chrisnewland.jitwatch.core.JITWatchConstants;
import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.PackageManager;
import com.chrisnewland.jitwatch.toplist.CompiledAttributeFilterAdapter;
import com.chrisnewland.jitwatch.toplist.ITopListFilter;
import com.chrisnewland.jitwatch.toplist.MemberScore;
import com.chrisnewland.jitwatch.toplist.TopListTreeWalker;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class TopListStage extends Stage
{
    private ObservableList<MemberScore> topList = FXCollections.observableArrayList();

    private TableView<MemberScore> tableView;

    private ITopListFilter toplistFilter;

    private PackageManager pm;

    public TopListStage(final JITWatchUI parent)
    {
        initStyle(StageStyle.DECORATED);

        pm = parent.getPackageManager();

        setOnCloseRequest(new EventHandler<WindowEvent>()
        {
            @Override
            public void handle(WindowEvent arg0)
            {
                parent.handleStageClosed(TopListStage.this);
            }
        });

        int width = 800;
        int height = 480;

        final Map<String, ITopListFilter> attrMap = new HashMap<>();

        // Hurry up lambdas !!!

        attrMap.put("Largest Native Methods", new CompiledAttributeFilterAdapter(JITWatchConstants.ATTR_NMSIZE));
        attrMap.put("Largest Bytecode Methods", new CompiledAttributeFilterAdapter(JITWatchConstants.ATTR_BYTES));
        attrMap.put("Slowest Compilation Times", new CompiledAttributeFilterAdapter(JITWatchConstants.ATTR_COMPILE_MILLIS));
        attrMap.put("Most Decompiled Methods", new CompiledAttributeFilterAdapter(JITWatchConstants.ATTR_DECOMPILES));
        attrMap.put("Compilation Order", new ITopListFilter()
        {
            // OSR compile_id values overlap non-OSR compile_id values so filter
            // out
            @Override
            public MemberScore getScore(IMetaMember mm)
            {
                long value = Long.valueOf(mm.getCompiledAttribute(JITWatchConstants.ATTR_COMPILE_ID));
                return new MemberScore(mm, value);
            }

            @Override
            public boolean acceptMember(IMetaMember mm)
            {
                String compileID = mm.getCompiledAttribute(JITWatchConstants.ATTR_COMPILE_ID);
                String compileKind = mm.getCompiledAttribute(JITWatchConstants.ATTR_COMPILE_KIND);
                return compileID != null && (compileKind == null || !JITWatchConstants.OSR.equals(compileKind));
            }
        });

        attrMap.put("Compilation Order (OSR)", new ITopListFilter()
        {
            // OSR compile_id values overlap non-OSR compile_id values so filter
            // out
            @Override
            public MemberScore getScore(IMetaMember mm)
            {
                long value = Long.valueOf(mm.getCompiledAttribute(JITWatchConstants.ATTR_COMPILE_ID));
                return new MemberScore(mm, value);
            }

            @Override
            public boolean acceptMember(IMetaMember mm)
            {
                String compileID = mm.getCompiledAttribute(JITWatchConstants.ATTR_COMPILE_ID);
                String compileKind = mm.getCompiledAttribute(JITWatchConstants.ATTR_COMPILE_KIND);
                return compileID != null && compileKind != null && JITWatchConstants.OSR.equals(compileKind);
            }
        });

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(8));
        vbox.setSpacing(8);

        List<String> keyList = new ArrayList<>(attrMap.keySet());
        Collections.sort(keyList);

        ObservableList<String> options = FXCollections.observableArrayList(keyList);

        toplistFilter = attrMap.get(options.get(0));

        final ComboBox<String> comboBox = new ComboBox<>(options);
        comboBox.setValue(options.get(0));

        comboBox.valueProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal)
            {
                toplistFilter = attrMap.get(newVal);
                buildTableView(toplistFilter);
            }
        });

        Scene scene = new Scene(vbox, width, height);

        setTitle("JITWatch TopLists");

        buildTableView(toplistFilter);
        tableView = TableUtil.buildTableMemberScore(topList);

        tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<MemberScore>()
        {
            @Override
            public void changed(ObservableValue<? extends MemberScore> arg0, MemberScore oldVal, MemberScore newVal)
            {
                if (newVal != null)
                {
                    parent.openTreeAtMember(newVal.getMember());
                }
            }
        });

        vbox.getChildren().add(comboBox);
        vbox.getChildren().add(tableView);

        tableView.prefHeightProperty().bind(scene.heightProperty());

        setScene(scene);
        show();

        redraw();
    }

    private void buildTableView(ITopListFilter filter)
    {
        topList.clear();
        topList.addAll(TopListTreeWalker.buildTopListForAttribute(pm, filter));
    }

    public void redraw()
    {

    }
}