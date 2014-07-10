/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.sandbox;

import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.core.JITWatchConfig.CompressedOops;
import org.adoptopenjdk.jitwatch.core.JITWatchConfig.TieredCompilation;
import org.adoptopenjdk.jitwatch.ui.FileChooserList;
import org.adoptopenjdk.jitwatch.ui.IStageCloseListener;
import org.adoptopenjdk.jitwatch.util.DisassemblyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final String DEFAULT_DISPLAY_STYLE = "-fx-padding:0px 8px 0px 0px";
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

		vbox.setPadding(new Insets(15));
		vbox.setSpacing(10);

		final FileChooserList chooserClasses = new FileChooserList(this, "Compile and Runtime Classpath",
				config.getSandboxClassLocations());

		HBox hboxButtons = new HBox();
		hboxButtons.setSpacing(20);
		hboxButtons.setPadding(new Insets(10));
		hboxButtons.setAlignment(Pos.CENTER);

		Button btnSave = new Button("Save");
		Button btnCancel = new Button("Cancel");

		btnSave.setOnAction(getEventHandlerForSaveButton(parent, config, chooserClasses));

		btnCancel.setOnAction(getEventHandlerForCancelButton(parent));

		hboxButtons.getChildren().add(btnCancel);
		hboxButtons.getChildren().add(btnSave);

		vbox.getChildren().add(chooserClasses);

		vbox.getChildren().add(buildCheckBoxPrintAssembly(config));
		
		vbox.getChildren().add(buildHBoxAssemblySyntax(config));

		vbox.getChildren().add(buildHBoxTieredCompilation(config));
		
		vbox.getChildren().add(buildHBoxCompressedOops(config));
		
		HBox hboxCompilerSettings = new HBox();

		hboxCompilerSettings.setSpacing(20);

		buildHBoxFreqInline(hboxCompilerSettings, config);
		buildHBoxMaxInline(hboxCompilerSettings, config);
		buildHBoxCompilationThreshold(hboxCompilerSettings, config);
		
		vbox.getChildren().add(hboxCompilerSettings);

		vbox.getChildren().add(hboxButtons);

		setTitle("Sandbox Configuration");

		Scene scene = new Scene(vbox, 740, 480);

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

    private EventHandler<ActionEvent> getEventHandlerForCancelButton(final IStageCloseListener parent) {
        return new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent e)
            {
                parent.handleStageClosed(SandboxConfigStage.this);
                close();
            }
        };
    }

    private EventHandler<ActionEvent> getEventHandlerForSaveButton(final IStageCloseListener parent, final JITWatchConfig config, final FileChooserList chooserClasses) {
        return new EventHandler<ActionEvent>()
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
        };
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
		
		rbATT.setStyle(DEFAULT_DISPLAY_STYLE);

		rbATT.setSelected(!intelMode);
		rbIntel.setSelected(intelMode);

		groupAssemblySyntax.selectedToggleProperty().addListener(
                getChangeListenerForGroupAssemblySyntax(config, rbIntel, groupAssemblySyntax));

		HBox hbox = new HBox();

		Label lblSyntax = new Label("Assembly syntax:");
		lblSyntax.setMinWidth(labelWidth);

		hbox.getChildren().add(lblSyntax);

		hbox.getChildren().add(rbATT);
		hbox.getChildren().add(rbIntel);

		return hbox;
	}

    private ChangeListener<Toggle> getChangeListenerForGroupAssemblySyntax(final JITWatchConfig config, final RadioButton rbIntel, final ToggleGroup groupAssemblySyntax) {
        return new ChangeListener<Toggle>()
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
        };
    }

    private HBox buildHBoxTieredCompilation(final JITWatchConfig config)
	{
		final RadioButton rbVMDefault = new RadioButton("VM Default");
		final RadioButton rbForceTiered = new RadioButton("-XX:+TieredCompilation");
		final RadioButton rbForceNoTiered = new RadioButton("-XX:-TieredCompilation");

		final ToggleGroup groupTiered = new ToggleGroup();

		rbVMDefault.setStyle(DEFAULT_DISPLAY_STYLE);
		rbForceTiered.setStyle(DEFAULT_DISPLAY_STYLE);

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

		groupTiered.selectedToggleProperty().addListener(getChangeListenerForGroupTiered(
                        config, rbVMDefault, rbForceTiered, rbForceNoTiered, groupTiered));

		HBox hbox = new HBox();

		Label lblMode = new Label("Tiered Compilation:");
		lblMode.setMinWidth(labelWidth);

		hbox.getChildren().add(lblMode);

		hbox.getChildren().add(rbVMDefault);
		hbox.getChildren().add(rbForceTiered);
		hbox.getChildren().add(rbForceNoTiered);

		return hbox;
	}

    private ChangeListener<Toggle> getChangeListenerForGroupTiered(final JITWatchConfig config, final RadioButton rbVMDefault, final RadioButton rbForceTiered, final RadioButton rbForceNoTiered, final ToggleGroup groupTiered) {
        return new ChangeListener<Toggle>()
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
        };
    }

    private HBox buildHBoxCompressedOops(final JITWatchConfig config)
	{
		final RadioButton rbVMDefault = new RadioButton("VM Default");
		final RadioButton rbForceCompressed = new RadioButton("-XX:+UseCompressedOops");
		final RadioButton rbForceNoCompressed = new RadioButton("-XX:-UseCompressedOops");

		final ToggleGroup groupOops = new ToggleGroup();

		rbVMDefault.setStyle(DEFAULT_DISPLAY_STYLE);
		rbForceCompressed.setStyle(DEFAULT_DISPLAY_STYLE);

		CompressedOops oopsMode = config.getCompressedOopsMode();

		switch (oopsMode)
		{
		case VM_DEFAULT:
			rbVMDefault.setSelected(true);
			rbForceCompressed.setSelected(false);
			rbForceNoCompressed.setSelected(false);
			break;
		case FORCE_COMPRESSED:
			rbVMDefault.setSelected(false);
			rbForceCompressed.setSelected(true);
			rbForceNoCompressed.setSelected(false);
			break;
		case FORCE_NO_COMPRESSED:
			rbVMDefault.setSelected(false);
			rbForceCompressed.setSelected(false);
			rbForceNoCompressed.setSelected(true);
			break;
		}

		rbVMDefault.setToggleGroup(groupOops);
		rbForceCompressed.setToggleGroup(groupOops);
		rbForceNoCompressed.setToggleGroup(groupOops);

		groupOops.selectedToggleProperty().addListener(getChangeListenerForGroupOops(
                config, rbVMDefault, rbForceCompressed, rbForceNoCompressed, groupOops));

		HBox hbox = new HBox();

		Label lblMode = new Label("Compressed Oops:");
		lblMode.setMinWidth(labelWidth);

		hbox.getChildren().add(lblMode);

		hbox.getChildren().add(rbVMDefault);
		hbox.getChildren().add(rbForceCompressed);
		hbox.getChildren().add(rbForceNoCompressed);

		return hbox;
	}

    private ChangeListener<Toggle> getChangeListenerForGroupOops(final JITWatchConfig config, final RadioButton rbVMDefault, final RadioButton rbForceCompressed, final RadioButton rbForceNoCompressed, final ToggleGroup groupOops) {
        return new ChangeListener<Toggle>()
        {
            @Override
            public void changed(ObservableValue<? extends Toggle> arg0, Toggle arg1, Toggle arg2)
            {
                if (groupOops.getSelectedToggle() != null)
                {
                    if (groupOops.getSelectedToggle().equals(rbVMDefault))
                    {
                        config.setCompressedOopsMode(CompressedOops.VM_DEFAULT);
                    }
                    else if (groupOops.getSelectedToggle().equals(rbForceCompressed))
                    {
                        config.setCompressedOopsMode(CompressedOops.FORCE_COMPRESSED);
                    }
                    else if (groupOops.getSelectedToggle().equals(rbForceNoCompressed))
                    {
                        config.setCompressedOopsMode(CompressedOops.FORCE_NO_COMPRESSED);
                    }
                }
            }
        };
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