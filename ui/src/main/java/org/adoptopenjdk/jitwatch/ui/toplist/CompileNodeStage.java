/*
 * Copyright (c) 2013-2019 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.toplist;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.adoptopenjdk.jitwatch.chain.CompileNode;
import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.toplist.*;
import org.adoptopenjdk.jitwatch.ui.main.IMemberSelectedListener;
import org.adoptopenjdk.jitwatch.ui.main.IStageAccessProxy;
import org.adoptopenjdk.jitwatch.ui.stage.IClearableStage;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

public class CompileNodeStage extends Stage implements IClearableStage
{
	private ObservableList<CompileNode> compilationList = FXCollections.observableArrayList();

	private TableView<CompileNode> tableView;

	public CompileNodeStage(final IMemberSelectedListener selectionListener, final IStageAccessProxy triViewAccessor)
	{
		initStyle(StageStyle.DECORATED);

		int width = 800;
		int height = 480;

		VBox vbox = new VBox();
		vbox.setPadding(new Insets(8));
		vbox.setSpacing(8);

		Scene scene = UserInterfaceUtil.getScene(vbox, width, height);

		tableView = CompileNodeRowBuilder.buildTable(compilationList);

		tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<CompileNode>()
		{
			@Override public void changed(ObservableValue<? extends CompileNode> arg0, CompileNode oldCompileNode, CompileNode newCompileNode)
			{
				if (newCompileNode != null)
				{
					CompileNode rootNode = newCompileNode.getRoot();

					Compilation compilationOfRoot = rootNode.getCompilation();

					if (compilationOfRoot != null)
					{
						System.out.println("Selecting root compilation " + compilationOfRoot.getMember());
						selectionListener.selectCompilation(compilationOfRoot.getMember(), compilationOfRoot.getIndex());
					}

					CompileNode caller = newCompileNode.getParent();

					if (caller != null)
					{
//						IMetaMember memberForCaller = caller.getMember();

						triViewAccessor.openTriView(newCompileNode);
					}
					
					selectionListener.selectCompileNode(newCompileNode);
				}
			}
		});

		vbox.getChildren().add(tableView);

		tableView.prefHeightProperty().bind(scene.heightProperty());

		setScene(scene);

		show();
	}

	public void setCompilations(String title, CompilationListScore score)
	{
		setTitle(title);

		compilationList.clear();

		compilationList.addAll(score.getCompilations());

		tableView.setItems(compilationList);
	}

	@Override public void clear()
	{
		compilationList.clear();
	}
}