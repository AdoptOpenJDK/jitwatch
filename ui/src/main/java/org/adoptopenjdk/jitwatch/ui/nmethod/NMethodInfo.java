/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.nmethod;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;

import java.util.ArrayList;
import java.util.List;

import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.util.StringUtil;

import org.adoptopenjdk.jitwatch.ui.main.IPrevNextCompilationListener;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class NMethodInfo extends HBox
{
	private Label lblCompileID;
	private Label lblClass;
	private Label lblMember;
	private Label lblCompilationNumber;
	private Label lblCompiler;
	
	private Label lblAddress;
		
	private Label lblBytecodeSize;
	private Label lblNativeSize;
	
	private Label lblQueuedTime;
	private Label lblCompilationStartTime;
	private Label lblNMethodEmittedTime;
	private Label lblCompileDuration;
	
	private List<Label> clearable = new ArrayList<>();

	private IPrevNextCompilationListener listener;

	private static final int DESCRIPTION_WIDTH = 128;

	public NMethodInfo(IPrevNextCompilationListener listener)
	{
		this.listener = listener;

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
		lblBytecodeSize = new Label();
		lblNativeSize = new Label();
		lblQueuedTime = new Label();
		lblCompilationStartTime = new Label();
		lblNMethodEmittedTime = new Label();
		lblCompileDuration = new Label();

		column.getChildren().add(makeLabel("NMethod Address", lblAddress));
		column.getChildren().add(makeSizeInfo());
		column.getChildren().add(makeLabel("Queued at", lblQueuedTime));
		column.getChildren().add(makeTimingInfo());
		column.getChildren().add(makeLabel("Compile Duration", lblCompileDuration));

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
				listener.selectPrevCompilation();
			}
		});

		btnNext.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				listener.selectNextCompilation();
			}
		});

		HBox hboxLabel = makeLabel("Compilation #", lblCompilationNumber);

		lblCompilationNumber.prefWidthProperty().unbind();
		lblCompilationNumber.prefWidthProperty().bind(widthProperty().multiply(0.25).subtract(DESCRIPTION_WIDTH));

		hbox.getChildren().addAll(hboxLabel, btnPrev, btnNext);

		return hbox;
	}
	
	private HBox makeSizeInfo()
	{
		
		HBox hbox = new HBox();
		hbox.setSpacing(8);

		HBox hboxBytecode = makeLabel("Bytecode Size", lblBytecodeSize);

		lblBytecodeSize.prefWidthProperty().unbind();
		lblBytecodeSize.prefWidthProperty().bind(widthProperty().multiply(0.25).subtract(DESCRIPTION_WIDTH));
		
		HBox hboxNative = makeLabel("Native Size", lblNativeSize, Pos.BASELINE_RIGHT);
		
		lblNativeSize.prefWidthProperty().unbind();
		lblNativeSize.prefWidthProperty().bind(widthProperty().multiply(0.25).subtract(DESCRIPTION_WIDTH));
	
		hbox.getChildren().addAll(hboxBytecode, hboxNative);

		return hbox;
	}
	
	private HBox makeTimingInfo()
	{		
		HBox hbox = new HBox();
		hbox.setSpacing(8);

		HBox hboxTiming1 = makeLabel("Compile Start", lblCompilationStartTime);
		
		lblCompilationStartTime.prefWidthProperty().unbind();
		lblCompilationStartTime.prefWidthProperty().bind(widthProperty().multiply(0.25).subtract(DESCRIPTION_WIDTH));
		
		HBox hboxTiming2 = makeLabel("NMethod Emitted", lblNMethodEmittedTime, Pos.BASELINE_RIGHT);

		lblNMethodEmittedTime.prefWidthProperty().unbind();
		lblNMethodEmittedTime.prefWidthProperty().bind(widthProperty().multiply(0.25).subtract(DESCRIPTION_WIDTH));

		hbox.getChildren().addAll(hboxTiming1, hboxTiming2);

		return hbox;
	}
		
	private HBox makeLabel(String labelText, Label labelValue)
	{
		return makeLabel(labelText, labelValue, Pos.BASELINE_LEFT);
	}

	private HBox makeLabel(String labelText, Label labelValue, Pos labelAlignment)
	{
		HBox hbox = new HBox();
		hbox.setSpacing(16);

		Label descriptionLabel = new Label(labelText);
		descriptionLabel.setMinWidth(DESCRIPTION_WIDTH);
		descriptionLabel.setAlignment(labelAlignment);

		labelValue.getStyleClass().add("readonly-label");
		labelValue.prefWidthProperty().bind(widthProperty().multiply(0.5).subtract(DESCRIPTION_WIDTH));

		hbox.getChildren().addAll(descriptionLabel, labelValue);
		
		clearable.add(labelValue);

		return hbox;
	}

	public void setInfo(Compilation compilation)
	{
		IMetaMember compilationMember = compilation.getMember();

		int compilationCount = compilationMember.getCompilations().size();

		lblCompileID.setText(compilation.getCompileID());
		lblClass.setText(compilationMember.getMetaClass().getFullyQualifiedName());

		String fqMemberName = compilationMember.toStringUnqualifiedMethodName(true, true);
		lblMember.setText(fqMemberName);
		lblMember.setTooltip(new Tooltip(fqMemberName));

		String compilerString = compilation.getCompiler();

		if (compilation.getLevel() != -1)
		{
			compilerString += " (Level " + compilation.getLevel() + ")";
		}

		lblCompiler.setText(compilerString);
		lblCompilationNumber.setText(Integer.toString(1 + compilation.getIndex()) + " of " + compilationCount);

		lblBytecodeSize.setText(Long.toString(compilation.getBytecodeSize()) + " bytes");
		
		lblQueuedTime.setText(StringUtil.formatTimestamp(compilation.getStampTaskQueued(), true));
		lblCompilationStartTime.setText(StringUtil.formatTimestamp(compilation.getStampTaskCompilationStart(), true));
			
		if (compilation.isFailed())
		{
			lblAddress.setText("Compilation failed, no nmethod emitted");
			lblNativeSize.setText("NA");
			lblCompileDuration.setText("NA");
			lblNMethodEmittedTime.setText("NA");
		}
		else
		{
			lblAddress.setText(compilation.getNativeAddress());
			lblNativeSize.setText(Long.toString(compilation.getNativeSize()) + " bytes");
			lblCompileDuration.setText(Long.toString(compilation.getCompilationDuration()) + "ms");
			lblNMethodEmittedTime.setText(StringUtil.formatTimestamp(compilation.getStampNMethodEmitted(), true));
		}
	}

	public void clear()
	{
		for (Label label : clearable)
		{
			label.setText(S_EMPTY);
		}
	}
}