package com.chrisnewland.jitwatch.ui;

import java.util.HashMap;
import java.util.Map;

import com.chrisnewland.jitwatch.model.PackageManager;
import com.chrisnewland.jitwatch.toplist.MemberScore;
import com.chrisnewland.jitwatch.toplist.ToplistTreeWalker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
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

		Button btnGo = new Button("Go");

		btnGo.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				selectedAttribute = attrMap.get(comboBox.getValue());
				buildTableView(selectedAttribute);
			}
		});

		HBox hbox = new HBox();
		hbox.getChildren().add(comboBox);
		hbox.getChildren().add(btnGo);

		Scene scene = new Scene(vbox, width, height);

		setTitle("JITWatch TopLists");

		buildTableView(selectedAttribute);
		tableView = TableUtil.buildTableMemberScore(topList);

		vbox.getChildren().add(hbox);
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