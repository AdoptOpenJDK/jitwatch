/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui.sandbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chrisnewland.jitwatch.core.JITWatchConfig;
import com.chrisnewland.jitwatch.core.JITWatchConfig.TieredCompilation;
import com.chrisnewland.jitwatch.ui.FileChooserList;
import com.chrisnewland.jitwatch.ui.IStageCloseListener;
import com.chrisnewland.jitwatch.util.DisassemblyUtil;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class SandboxConfigStage extends Stage
{
	private TextField txtFreqInline;
	private TextField txtMaxInline;
	private TextField txtCompilerThreshold;

	private CheckBox checkBoxPrintAssembly;
	private static final int labelWidth = 150;

	private static final Logger logger = LoggerFactory.getLogger(SandboxConfigStage.class);

	public SandboxConfigStage(final IStageCloseListener parent, final JITWatchConfig config)
	{
		initStyle(StageStyle.UTILITY);

		VBox vbox = new VBox();

		vbox.setPadding(new Insets(10));
		vbox.setSpacing(10);

		final FileChooserList chooserClasses = new FileChooserList(this, "Compile and Runtime Classpath",
				config.getSandboxClassLocations());

		HBox hboxButtons = new HBox();
		hboxButtons.setSpacing(20);
		hboxButtons.setPadding(new Insets(10));
		hboxButtons.setAlignment(Pos.CENTER);

		Button btnSave = new Button("Save");
		Button btnCancel = new Button("Cancel");

		btnSave.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				config.setSandboxClassLocations(chooserClasses.getFiles());
				
				try
				{
					config.setFreqInlineSize(Integer.parseInt(txtFreqInline.getText()));
				}
				catch (NumberFormatException nfe)
				{
					logger.error("Bad FreqInlineSize value", nfe);
				}

				try
				{
					config.setMaxInlineSize(Integer.parseInt(txtMaxInline.getText()));
				}
				catch (NumberFormatException nfe)
				{
					logger.error("Bad MaxInlineSize value", nfe);
				}
				
				try
				{
					config.setCompilerThreshold(Integer.parseInt(txtCompilerThreshold.getText()));
				}
				catch (NumberFormatException nfe)
				{
					logger.error("Bad CompilerThreshold value", nfe);
				}
				
				config.setPrintAssembly(checkBoxPrintAssembly.isSelected());

				config.saveConfig();

				parent.handleStageClosed(SandboxConfigStage.this);
				close();
			}
		});

		btnCancel.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				parent.handleStageClosed(SandboxConfigStage.this);
				close();
			}
		});

		hboxButtons.getChildren().add(btnCancel);
		hboxButtons.getChildren().add(btnSave);

		vbox.getChildren().add(chooserClasses);

		vbox.getChildren().add(buildCheckBoxPrintAssembly(config));
		
		vbox.getChildren().add(buildHBoxAssemblySyntax(config));

		vbox.getChildren().add(buildHBoxTieredCompilation(config));
		
		HBox hboxCompilerSettings = new HBox();
		//hboxCompilerSettings.setPadding(new Insets(0, 20, 0, 0));
		hboxCompilerSettings.setSpacing(20);

		buildHBoxFreqInline(hboxCompilerSettings, config);
		buildHBoxMaxInline(hboxCompilerSettings, config);
		buildHBoxCompilationThreshold(hboxCompilerSettings, config);
		
		vbox.getChildren().add(hboxCompilerSettings);

		vbox.getChildren().add(hboxButtons);

		setTitle("Sandbox Configuration");

		Scene scene = new Scene(vbox, 700, 400);

		setScene(scene);

		setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				parent.handleStageClosed(SandboxConfigStage.this);
			}
		});
	}

	private HBox buildHBoxAssemblySyntax(final JITWatchConfig config)
	{
		final RadioButton rbATT = new RadioButton("AT&T");
		final RadioButton rbIntel = new RadioButton("Intel");

		rbIntel.setDisable(true);

		final ToggleGroup groupAssemblySyntax = new ToggleGroup();

		boolean intelMode = config.isSandboxIntelMode();

		rbATT.setToggleGroup(groupAssemblySyntax);
		rbIntel.setToggleGroup(groupAssemblySyntax);
		
		rbATT.setStyle("-fx-padding:0px 8px 0px 0px");

		rbATT.setSelected(!intelMode);
		rbIntel.setSelected(intelMode);

		groupAssemblySyntax.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
		{
			@Override
			public void changed(ObservableValue<? extends Toggle> arg0, Toggle arg1, Toggle arg2)
			{
				if (groupAssemblySyntax.getSelectedToggle() != null)
				{
					boolean nextIntelMode = groupAssemblySyntax.getSelectedToggle().equals(rbIntel);

					config.setSandboxIntelMode(nextIntelMode);
				}
			}
		});

		HBox hbox = new HBox();
		// hbox.setPadding(new Insets(0,10,0,10));

		Label lblSyntax = new Label("Assembly syntax:");
		lblSyntax.setMinWidth(labelWidth);

		hbox.getChildren().add(lblSyntax);

		hbox.getChildren().add(rbATT);
		hbox.getChildren().add(rbIntel);

		return hbox;
	}

	private HBox buildHBoxTieredCompilation(final JITWatchConfig config)
	{
		final RadioButton rbVMDefault = new RadioButton("VM Default");
		final RadioButton rbForceTiered = new RadioButton("-XX:+TieredCompilation");
		final RadioButton rbForceNoTiered = new RadioButton("-XX:-TieredCompilation");

		final ToggleGroup groupTiered = new ToggleGroup();

		rbVMDefault.setStyle("-fx-padding:0px 8px 0px 0px");
		rbForceTiered.setStyle("-fx-padding:0px 8px 0px 0px");

		TieredCompilation tieredMode = config.getTieredCompilationMode();

		switch (tieredMode)
		{
		case VM_DEFAULT:
			rbVMDefault.setSelected(true);
			rbForceTiered.setSelected(false);
			rbForceNoTiered.setSelected(false);
			break;
		case FORCE_TIERED:
			rbVMDefault.setSelected(false);
			rbForceTiered.setSelected(true);
			rbForceNoTiered.setSelected(false);
			break;
		case FORCE_NO_TIERED:
			rbVMDefault.setSelected(false);
			rbForceTiered.setSelected(false);
			rbForceNoTiered.setSelected(true);
			break;
		}

		rbVMDefault.setToggleGroup(groupTiered);
		rbForceTiered.setToggleGroup(groupTiered);
		rbForceNoTiered.setToggleGroup(groupTiered);

		groupTiered.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
		{
			@Override
			public void changed(ObservableValue<? extends Toggle> arg0, Toggle arg1, Toggle arg2)
			{
				if (groupTiered.getSelectedToggle() != null)
				{
					if (groupTiered.getSelectedToggle().equals(rbVMDefault))
					{
						config.setTieredCompilationMode(TieredCompilation.VM_DEFAULT);
					}
					else if (groupTiered.getSelectedToggle().equals(rbForceTiered))
					{
						config.setTieredCompilationMode(TieredCompilation.FORCE_TIERED);
					}
					else if (groupTiered.getSelectedToggle().equals(rbForceNoTiered))
					{
						config.setTieredCompilationMode(TieredCompilation.FORCE_NO_TIERED);
					}
				}
			}
		});

		HBox hbox = new HBox();

		Label lblMode = new Label("Tiered Compilation:");
		lblMode.setMinWidth(labelWidth);

		hbox.getChildren().add(lblMode);

		hbox.getChildren().add(rbVMDefault);
		hbox.getChildren().add(rbForceTiered);
		hbox.getChildren().add(rbForceNoTiered);

		return hbox;
	}

	private void buildHBoxFreqInline(HBox hbCompilerSettings, final JITWatchConfig config)
	{
		txtFreqInline = new TextField(Integer.toString(config.getFreqInlineSize()));
		txtFreqInline.setMaxWidth(50);
		txtFreqInline.setAlignment(Pos.BASELINE_RIGHT);

		Label label = new Label("-XX:FreqInlineSize:");

		hbCompilerSettings.getChildren().add(label);
		hbCompilerSettings.getChildren().add(txtFreqInline);
	}

	private void buildHBoxMaxInline(HBox hbCompilerSettings, final JITWatchConfig config)
	{
		txtMaxInline = new TextField(Integer.toString(config.getMaxInlineSize()));
		txtMaxInline.setMaxWidth(50);
		txtMaxInline.setAlignment(Pos.BASELINE_RIGHT);

		Label label = new Label("-XX:MaxInlineSize:");

		hbCompilerSettings.getChildren().add(label);
		hbCompilerSettings.getChildren().add(txtMaxInline);
	}
	
	private void buildHBoxCompilationThreshold(HBox hbCompilerSettings, final JITWatchConfig config)
	{
		txtCompilerThreshold = new TextField(Integer.toString(config.getCompilerThreshold()));
		txtCompilerThreshold.setMaxWidth(70);
		txtCompilerThreshold.setAlignment(Pos.BASELINE_RIGHT);

		Label label = new Label("-XX:CompilationThreshold:");

		hbCompilerSettings.getChildren().add(label);
		hbCompilerSettings.getChildren().add(txtCompilerThreshold);
	}

	private CheckBox buildCheckBoxPrintAssembly(final JITWatchConfig config)
	{
		checkBoxPrintAssembly = new CheckBox("Disassemble native code");
		
		boolean checked = false;

		if (DisassemblyUtil.isDisassemblerAvailable())
		{
			if (config.isPrintAssembly())
			{
				checked = true;
			}
		}
		else
		{
			checkBoxPrintAssembly.setDisable(true);
		}

		checkBoxPrintAssembly.setSelected(checked);

		return checkBoxPrintAssembly;
	}
}