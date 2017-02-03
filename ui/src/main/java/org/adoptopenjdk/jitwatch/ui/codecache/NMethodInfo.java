/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.codecache;

import org.adoptopenjdk.jitwatch.model.CodeCacheEvent;
import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.util.StringUtil;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;

public class NMethodInfo extends HBox
{
	private Label lblCompileID;
	private Label lblClass;
	private Label lblMember;
	private Label lblCompilationNumber;
	private Label lblCompiler;

	private Label lblNativeSize;
	private Label lblAddress;
	private Label lblQueuedTime;
	private Label lblCompiledTime;
	private Label lblElapsed;

	private CodeCacheLayoutStage parent;

	private static final int DESCRIPTION_WIDTH = 128;

	public NMethodInfo(CodeCacheLayoutStage parent)
	{
		this.parent = parent;

		setPadding(new Insets(24, 4, 0, 4));
		setSpacing(16);

		getChildren().addAll(makeLeftColumn(), makeRightColumn());
	}

	private VBox makeLeftColumn()
	{
		VBox column = new VBox();
		column.setSpacing(8);

		lblCompileID = new Label();
		lblClass = new Label();
		lblMember = new Label();
		lblCompilationNumber = new Label();
		lblCompiler = new Label();

		column.getChildren().add(makeLabel("Compile ID", lblCompileID));
		column.getChildren().add(makeLabel("Class", lblClass));
		column.getChildren().add(makeLabel("Compiled Member", lblMember));
		column.getChildren().add(makeCompilationNavigator());
		column.getChildren().add(makeLabel("Compiler Used", lblCompiler));

		return column;
	}

	private VBox makeRightColumn()
	{
		VBox column = new VBox();
		column.setSpacing(8);

		lblAddress = new Label();
		lblNativeSize = new Label();
		lblQueuedTime = new Label();
		lblCompiledTime = new Label();
		lblElapsed = new Label();

		column.getChildren().add(makeLabel("NMethod Address", lblAddress));
		column.getChildren().add(makeLabel("Native Size", lblNativeSize));
		column.getChildren().add(makeLabel("Queued at", lblQueuedTime));
		column.getChildren().add(makeLabel("Compiled at", lblCompiledTime));
		column.getChildren().add(makeLabel("Elapsed Time", lblElapsed));

		return column;
	}

	private HBox makeCompilationNavigator()
	{
		HBox hbox = new HBox();
		hbox.setSpacing(8);

		Button btnPrev = new Button("Prev");
		Button btnNext = new Button("Next");

		btnPrev.setMinWidth(20);
		btnNext.setMinWidth(20);

		btnPrev.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				parent.selectPrevCompilation();
			}
		});

		btnNext.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				parent.selectNextCompilation();
			}
		});

		HBox hboxLabel = makeLabel("Compilation #", lblCompilationNumber);

		lblCompilationNumber.prefWidthProperty().unbind();
		lblCompilationNumber.prefWidthProperty().bind(widthProperty().multiply(0.25).subtract(DESCRIPTION_WIDTH));

		hbox.getChildren().addAll(hboxLabel, btnPrev, btnNext);

		return hbox;
	}

	private HBox makeLabel(String labelText, Label labelValue)
	{
		HBox hbox = new HBox();

		Label descriptionLabel = new Label(labelText);
		descriptionLabel.setMinWidth(DESCRIPTION_WIDTH);

		labelValue.getStyleClass().add("readonly-label");
		labelValue.prefWidthProperty().bind(widthProperty().multiply(0.5).subtract(DESCRIPTION_WIDTH));

		hbox.getChildren().addAll(descriptionLabel, labelValue);

		return hbox;
	}

	public void setInfo(CodeCacheEvent event, Compilation compilation)
	{
		IMetaMember compilationMember = compilation.getMember();

		int compilationCount = compilationMember.getCompilations().size();

		lblCompileID.setText(compilation.getCompileID());
		lblClass.setText(compilationMember.getMetaClass().getFullyQualifiedName());

		String fqMemberName = compilationMember.toStringUnqualifiedMethodName(true, true);
		lblMember.setText(fqMemberName);
		lblMember.setTooltip(new Tooltip(fqMemberName));
		
		lblCompiler.setText(compilation.getCompiler() + " (Level " + compilation.getLevel() + ")");
		lblCompilationNumber.setText(Integer.toString(1 + compilation.getIndex()) + " of " + compilationCount);

		lblAddress.setText(Long.toHexString(event.getNativeAddress()));
		lblNativeSize.setText(Long.toString(event.getNativeCodeSize()) + " bytes");
		lblQueuedTime.setText(StringUtil.formatTimestamp(compilation.getQueuedStamp(), true));
		lblCompiledTime.setText(StringUtil.formatTimestamp(compilation.getCompiledStamp(), true));
		lblElapsed.setText(Long.toString(compilation.getCompileTime()) + "ms");
	}

	public void clear()
	{
		lblCompileID.setText(S_EMPTY);
		lblClass.setText(S_EMPTY);
		lblMember.setText(S_EMPTY);
		lblMember.setTooltip(null);
		lblCompiler.setText(S_EMPTY);
		lblCompilationNumber.setText(S_EMPTY);

		lblAddress.setText(S_EMPTY);
		lblNativeSize.setText(S_EMPTY);
		lblQueuedTime.setText(S_EMPTY);
		lblCompiledTime.setText(S_EMPTY);
		lblElapsed.setText(S_EMPTY);

	}
}