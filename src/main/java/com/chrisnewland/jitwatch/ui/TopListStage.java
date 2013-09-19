package com.chrisnewland.jitwatch.ui;

import java.util.HashMap;
import java.util.Map;

import com.chrisnewland.jitwatch.model.PackageManager;
import com.chrisnewland.jitwatch.toplist.MemberScore;
import com.chrisnewland.jitwatch.toplist.ToplistTreeWalker;

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

    private String selectedAttribute;

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

        int width = 640;
        int height = 480;

        final Map<String, String> attrMap = new HashMap<>();
        attrMap.put("Largest Native Methods", "nmsize");
        attrMap.put("Largest Bytecode Methods", "bytes");
        attrMap.put("Slowest Compilation Times", "compileMillis");

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(8));
        vbox.setSpacing(8);

        ObservableList<String> options = FXCollections.observableArrayList(attrMap.keySet());

        selectedAttribute = attrMap.get(options.get(0));

        final ComboBox<String> comboBox = new ComboBox<>(options);
        comboBox.setValue(options.get(0));

        comboBox.valueProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal)
            {
                selectedAttribute = attrMap.get(newVal);
                buildTableView(selectedAttribute);
            }
        });

        Scene scene = new Scene(vbox, width, height);

        setTitle("JITWatch TopLists");

        buildTableView(selectedAttribute);
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

    private void buildTableView(String attribute)
    {
        topList.clear();
        topList.addAll(ToplistTreeWalker.buildTopListForAttribute(pm, true, selectedAttribute));
    }

    public void redraw()
    {

    }
}