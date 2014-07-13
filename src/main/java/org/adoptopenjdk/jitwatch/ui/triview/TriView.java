/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.triview;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.loader.ResourceLoader;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyMethod;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.model.bytecode.ClassBC;
import org.adoptopenjdk.jitwatch.model.bytecode.LineTable;
import org.adoptopenjdk.jitwatch.model.bytecode.LineTableEntry;
import org.adoptopenjdk.jitwatch.ui.Dialogs;
import org.adoptopenjdk.jitwatch.ui.JITWatchUI;
import org.adoptopenjdk.jitwatch.ui.triview.assembly.ViewerAssembly;
import org.adoptopenjdk.jitwatch.ui.triview.bytecode.BytecodeLabel;
import org.adoptopenjdk.jitwatch.ui.triview.bytecode.ViewerBytecode;
import org.adoptopenjdk.jitwatch.ui.triview.source.ViewerSource;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

public class TriView extends Stage implements ILineListener
{
	private IMetaMember currentMember;
	private JITWatchConfig config;

	private ViewerSource viewerSource;
	private ViewerBytecode viewerBytecode;
	private ViewerAssembly viewerAssembly;

	private SplitPane splitViewer;

	private VBox colSource;
	private VBox colBytecode;
	private VBox colAssembly;

	private CheckBox checkSource;
	private CheckBox checkBytecode;
	private CheckBox checkAssembly;

	private ClassSearch classSearch;
	private ComboBox<IMetaMember> comboMember;

	private Label lblMemberInfo;

	private boolean ignoreComboChanged = false;
	
	private boolean classBytecodeMismatch = false;

	private static final Logger logger = LoggerFactory.getLogger(TriView.class);

	public TriView(final JITWatchUI parent, final JITWatchConfig config)
	{
		this.config = config;

		setTitle("TriView: Source, Bytecode, Assembly Viewer");

		VBox vBox = new VBox();

		HBox hBoxToolBarClass = new HBox();
		hBoxToolBarClass.setSpacing(10);
		hBoxToolBarClass.setPadding(new Insets(10));

		HBox hBoxToolBarButtons = new HBox();
		hBoxToolBarButtons.setSpacing(10);
		hBoxToolBarButtons.setPadding(new Insets(0, 10, 10, 10));

		checkSource = new CheckBox("Source");
		checkBytecode = new CheckBox("Bytecode");
		checkAssembly = new CheckBox("Assembly");

		checkSource.setSelected(true);
		checkBytecode.setSelected(true);
		checkAssembly.setSelected(true);

		ChangeListener<Boolean> checkListener = new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal)
			{
				checkColumns();
			}
		};

		checkSource.selectedProperty().addListener(checkListener);
		checkBytecode.selectedProperty().addListener(checkListener);
		checkAssembly.selectedProperty().addListener(checkListener);

		Button btnCallChain = new Button("View Compile Chain");
		btnCallChain.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				if (currentMember != null)
				{
					parent.openCompileChain(currentMember);
				}
			}
		});

		hBoxToolBarButtons.getChildren().add(checkSource);
		hBoxToolBarButtons.getChildren().add(checkBytecode);
		hBoxToolBarButtons.getChildren().add(checkAssembly);
		hBoxToolBarButtons.getChildren().add(btnCallChain);

		Label lblClass = new Label("Class:");
		classSearch = new ClassSearch(this, parent.getPackageManager());
		classSearch.prefWidthProperty().bind(widthProperty().multiply(0.4));

		Label lblMember = new Label("Member:");

		comboMember = new ComboBox<>();
		comboMember.prefWidthProperty().bind(widthProperty().multiply(0.4));

		comboMember.valueProperty().addListener(new ChangeListener<IMetaMember>()
		{
			@Override
			public void changed(ObservableValue<? extends IMetaMember> ov, IMetaMember oldVal, IMetaMember newVal)
			{
				// TODO Looks like a bug in JavaFX 2.2 here
				// sometimes combo contains only selected member
				if (!ignoreComboChanged)
				{
					if (newVal != null)
					{
						TriView.this.setMember(newVal, false);
					}
				}
			}
		});

		comboMember.setCellFactory(getCallbackForCellFactory());

		comboMember.setConverter(new StringConverter<IMetaMember>()
		{
			@Override
			public String toString(IMetaMember mm)
			{
				return mm.toStringUnqualifiedMethodName(false);
			}

			@Override
			public IMetaMember fromString(String arg0)
			{
				return null;
			}
		});

		hBoxToolBarClass.getChildren().add(lblClass);
		hBoxToolBarClass.getChildren().add(classSearch);

		hBoxToolBarClass.getChildren().add(lblMember);
		hBoxToolBarClass.getChildren().add(comboMember);

		splitViewer = new SplitPane();
		splitViewer.setOrientation(Orientation.HORIZONTAL);

		colSource = new VBox();
		colBytecode = new VBox();
		colAssembly = new VBox();

		Label lblSource = new Label("Source");
		Label lblBytecode = new Label("Bytecode (double click for JVMS)");
		Label lblAssembly = new Label("Assembly");

		lblSource.setStyle("-fx-background-color:#dddddd; -fx-padding:4px;");
		lblBytecode.setStyle("-fx-background-color:#dddddd; -fx-padding:4px;");
		lblAssembly.setStyle("-fx-background-color:#dddddd; -fx-padding:4px;");

		lblSource.prefWidthProperty().bind(colSource.widthProperty());
		lblBytecode.prefWidthProperty().bind(colBytecode.widthProperty());
		lblAssembly.prefWidthProperty().bind(colAssembly.widthProperty());

		viewerSource = new ViewerSource(parent, this, LineType.SOURCE);
		viewerBytecode = new ViewerBytecode(parent, this, LineType.BYTECODE);
		viewerAssembly = new ViewerAssembly(parent, this, LineType.ASSEMBLY);

		colSource.getChildren().add(lblSource);
		colSource.getChildren().add(viewerSource);

		colBytecode.getChildren().add(lblBytecode);
		colBytecode.getChildren().add(viewerBytecode);

		colAssembly.getChildren().add(lblAssembly);
		colAssembly.getChildren().add(viewerAssembly);

		splitViewer.prefHeightProperty().bind(vBox.heightProperty());

		viewerSource.prefWidthProperty().bind(colSource.widthProperty());
		viewerSource.prefHeightProperty().bind(colSource.heightProperty());

		viewerBytecode.prefWidthProperty().bind(colBytecode.widthProperty());
		viewerBytecode.prefHeightProperty().bind(colBytecode.heightProperty());

		viewerAssembly.prefWidthProperty().bind(colAssembly.widthProperty());
		viewerAssembly.prefHeightProperty().bind(colAssembly.heightProperty());

		lblMemberInfo = new Label();

		vBox.getChildren().add(hBoxToolBarClass);
		vBox.getChildren().add(hBoxToolBarButtons);
		vBox.getChildren().add(splitViewer);
		vBox.getChildren().add(lblMemberInfo);

		Scene scene = new Scene(vBox, JITWatchUI.WINDOW_WIDTH, JITWatchUI.WINDOW_HEIGHT);

		setScene(scene);

		setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				parent.handleStageClosed(TriView.this);
			}
		});

		checkColumns();
	}

    private Callback<ListView<IMetaMember>, ListCell<IMetaMember>> getCallbackForCellFactory() {
        return new Callback<ListView<IMetaMember>, ListCell<IMetaMember>>()
        {
            @Override
            public ListCell<IMetaMember> call(ListView<IMetaMember> arg0)
            {
                return new ListCell<IMetaMember>()
                {
                    @Override
                    protected void updateItem(IMetaMember item, boolean empty)
                    {
                        super.updateItem(item, empty);

                        if (item == null || empty)
                        {
                            setText(S_EMPTY);
                            setGraphic(null);
                        }
                        else
                        {
                            performUpdateOfItem(this, item);
                        }
                    }

                    private void performUpdateOfItem(ListCell<IMetaMember> listCell, IMetaMember item) {
                        listCell.setText(item.toStringUnqualifiedMethodName(false));

                        if (item.isCompiled() && UserInterfaceUtil.getTick() != null)
                        {
                            listCell.setGraphic(new ImageView(UserInterfaceUtil.getTick()));
                        }
                        else
                        {
                            listCell.setGraphic(null);
                        }
                    }
                };
            }
        };
    }

    private void checkColumns()
	{
		splitViewer.getItems().clear();

		int colCount = 0;

		if (checkSource.isSelected())
		{
			splitViewer.getItems().add(colSource);
			colCount++;
		}
		if (checkBytecode.isSelected())
		{
			splitViewer.getItems().add(colBytecode);
			colCount++;
		}
		if (checkAssembly.isSelected())
		{
			splitViewer.getItems().add(colAssembly);
			colCount++;
		}

		switch (colCount)
		{
		case 0:
			splitViewer.setDividerPositions(0);
			break;
		case 1:
			splitViewer.setDividerPositions(1);
			break;
		case 2:
			splitViewer.setDividerPositions(0.5);
			break;
		case 3:
			splitViewer.setDividerPositions(0.333, 0.666);
			break;
		default:
			break;
		}
	}

	public void setMetaClass(MetaClass metaClass)
	{
		String fqName = metaClass.getFullyQualifiedName();

		classSearch.setText(fqName);

		List<IMetaMember> members = metaClass.getMetaMembers();

		if (members.size() > 0)
		{
			setMember(members.get(0), false);
		}
		else
		{
			// unlikely but if no members then clear the combo
			comboMember.getItems().clear();
		}
	}

	public void setMember(IMetaMember member, boolean force)
	{
		setMember(member, force, true);
	}

	public void setMember(IMetaMember member, boolean force, boolean jumpToSource)
	{
		boolean sameClass = false;

		MetaClass previousClass = currentMember == null ? null : currentMember.getMetaClass();

		currentMember = member;

		final MetaClass memberClass = currentMember.getMetaClass();

        sameClass = evaluateSameClass(force, sameClass, previousClass, memberClass);

        processIfNotSameClass(sameClass, memberClass);

        ignoreComboChanged = true;
		comboMember.setValue(currentMember);
		ignoreComboChanged = false;

		if (!sameClass)
		{
			String sourceFileName = ResourceLoader.getSourceFilename(memberClass);
			String source = ResourceLoader.getSource(config.getSourceLocations(), sourceFileName);
			viewerSource.setContent(source, true);
		}

		if (jumpToSource)
		{
			viewerSource.jumpTo(currentMember);
			viewerSource.setScrollBar();
		}

		StringBuilder statusBarBuilder = new StringBuilder();

		List<String> classLocations = config.getClassLocations();

        ClassBC classBytecode = processStatusBarIfClassBytecodeIsValid(statusBarBuilder, classLocations);

		viewerBytecode.setContent(currentMember, classBytecode, classLocations);

        processIfCurrentMemberIsCompiled(statusBarBuilder);

        applyActionsIfOffsetMismatchDetected(statusBarBuilder);

        lblMemberInfo.setText(statusBarBuilder.toString());
	}

    private void applyActionsIfOffsetMismatchDetected(StringBuilder statusBarBuilder) {
        if (viewerBytecode.isOffsetMismatchDetected())
		{
			statusBarBuilder.append(C_SPACE).append("WARNING Class bytecode offsets do not match HotSpot log");

			if (!classBytecodeMismatch)
			{
				classBytecodeMismatch = true;

				Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Dialogs.showOKDialog(
                                TriView.this,
                                "Wrong classes mounted for log file?",
                                "Uh-oh, the bytecode for this class does not match the bytecode offsets in your HotSpot log.\nAre the mounted classes the same ones used at runtime when the log was created?");
                    }
                });
			}
		}
    }

    private void processIfCurrentMemberIsCompiled(StringBuilder statusBarBuilder) {
        AssemblyMethod asmMethod = null;
        if (currentMember.isCompiled())
        {
            statusBarBuilder.append(C_SPACE).append(currentMember.toStringUnqualifiedMethodName(false));

            asmMethod = currentMember.getAssembly();

            String attrCompiler = currentMember.getCompiledAttribute(ATTR_COMPILER);

            if (attrCompiler != null)
            {
                statusBarBuilder.append(" compiled with ").append(attrCompiler);
            }
            else
            {
                String attrCompileKind = currentMember.getCompiledAttribute(ATTR_COMPILE_KIND);

                if (attrCompileKind != null && C2N.equals(attrCompileKind))
                {
                    statusBarBuilder.append(" compiled native wrapper");
                }
            }

            if (asmMethod == null)
            {
                String msg = "Assembly not found. Was -XX:+PrintAssembly option used?";
                viewerAssembly.setContent(msg, false);
            }
            else
            {
                viewerAssembly.setAssemblyMethod(asmMethod);
            }
        }
        else
        {
            String msg = "Not JIT-compiled";
            viewerAssembly.setContent(msg, false);

            lblMemberInfo.setText(S_EMPTY);
        }
    }

    private ClassBC processStatusBarIfClassBytecodeIsValid(StringBuilder statusBarBuilder, List<String> classLocations) {
        ClassBC classBytecode = currentMember.getMetaClass().getClassBytecode(classLocations);

        if (classBytecode != null)
        {
            int majorVersion = classBytecode.getMajorVersion();
            int minorVersion = classBytecode.getMinorVersion();
            String javaVersion = classBytecode.getJavaVersion();

            statusBarBuilder.append("Mounted class version: ");
            statusBarBuilder.append(majorVersion).append(C_DOT).append(minorVersion);
            statusBarBuilder.append(C_SPACE).append(C_OPEN_PARENTHESES);
            statusBarBuilder.append(javaVersion).append(C_CLOSE_PARENTHESES);
        }
        return classBytecode;
    }

    private void processIfNotSameClass(boolean sameClass, MetaClass memberClass) {
        if (!sameClass)
		{
			classBytecodeMismatch = false;

			comboMember.getSelectionModel().clearSelection();
			comboMember.getItems().clear();
			comboMember.getItems().addAll(memberClass.getMetaMembers());

			String fqName = memberClass.getFullyQualifiedName();
			classSearch.setText(fqName);
		}
    }

    private boolean evaluateSameClass(boolean force, boolean inSameClass, MetaClass previousClass,
                                      MetaClass memberClass) {
        boolean sameClass = inSameClass;
        if (!force)
        {
            if ((previousClass != null) && previousClass.equals(memberClass))
            {
                sameClass = true;
            }
        }
        return sameClass;
    }

    @Override
	public void lineHighlighted(int index, LineType lineType)
	{		
		switch (lineType)
		{
		case SOURCE:
			highlightFromSource(index);
			break;
		case BYTECODE:
			highlightFromBytecode(index);
			break;
		case ASSEMBLY:
			highlightFromAssembly(index);
			break;
		}
	}

	private void highlightFromSource(int index)
	{
		int sourceLine = index + 1;

		ClassBC classBytecode = currentMember.getMetaClass().getClassBytecode(config.getClassLocations());

		int bytecodeHighlight = -1;
		int assemblyHighlight = -1;
		
		if (classBytecode != null)
		{
			LineTable lineTable = classBytecode.getLineTable();
			
			if (lineTable.size() == 0)
			{
				logger.warn("LineNumberTable not found in class file. TriView highlight linking will not be available.");
			}
			
			LineTableEntry entry = lineTable.get(sourceLine);

			if (entry != null)
			{
				String memberSig = entry.getMemberSignature();

				IMetaMember nextMember = currentMember.getMetaClass().findMemberByBytecodeSignature(memberSig);

				if (nextMember != null)
				{
					if (nextMember.equals(currentMember))
					{
						setMember(nextMember, false, false);
					}

					int bcOffset = entry.getBytecodeOffset();

					bytecodeHighlight = viewerBytecode.getLineIndexForBytecodeOffset(bcOffset);
				}
				else
				{
					logger.warn("Could not find member for bc sig: {}", memberSig);
				}
			}
		}

		assemblyHighlight = viewerAssembly.getIndexForSourceLine(currentMember.getMetaClass().getFullyQualifiedName(), sourceLine);
		
		viewerBytecode.highlightLine(bytecodeHighlight);
		viewerAssembly.highlightLine(assemblyHighlight);
	}

	private void highlightFromBytecode(int index)
	{
		// each source line can map to multiple bytecodes?
		// but bytecode only maps to 1 source line

		ClassBC classBytecode = currentMember.getMetaClass().getClassBytecode(config.getClassLocations());

		BytecodeLabel bcLabel = (BytecodeLabel) viewerBytecode.getLabelAtIndex(index);

		BytecodeInstruction instruction = bcLabel.getInstruction();

		int bytecodeOffset = instruction.getOffset();

		int sourceHighlight = -1;
		int assemblyHighlight = viewerAssembly.getIndexForBytecodeOffset(currentMember.getMetaClass().getFullyQualifiedName(),
				bytecodeOffset);
		
		if (classBytecode != null)
		{
			LineTable lineTable = classBytecode.getLineTable();

			sourceHighlight = lineTable.findSourceLine(currentMember, bytecodeOffset);

			if (sourceHighlight != -1)
			{
				// starts at 1
				sourceHighlight--;
			}
		}

		viewerSource.highlightLine(sourceHighlight);
		viewerAssembly.highlightLine(assemblyHighlight);
	}

	private void highlightFromAssembly(int index)
	{
		Label label = viewerAssembly.getLabelAtIndex(index);

		int sourceHighlight = -1;
		int bytecodeHighlight = -1;

		if (label != null)
		{
			String className = viewerAssembly.getClassNameFromLabel(label);

			if (className != null && className.equals(currentMember.getMetaClass().getFullyQualifiedName()))
			{
				String sourceLine = viewerAssembly.getSourceLineFromLabel(label);

				String bytecodeLine = viewerAssembly.getBytecodeOffsetFromLabel(label);

				if (sourceLine != null)
				{
					try
					{
						sourceHighlight = Integer.parseInt(sourceLine) - 1;
					}
					catch (NumberFormatException nfe)
					{
						logger.error("Could not parse line number: {}", sourceLine, nfe);
					}
				}

				if (bytecodeLine != null)
				{
					try
					{
						int offset = Integer.parseInt(bytecodeLine);
						bytecodeHighlight = viewerBytecode.getLineIndexForBytecodeOffset(offset);
					}
					catch (NumberFormatException nfe)
					{
						logger.error("Could not parse line number: {}", bytecodeHighlight, nfe);
					}
				}
			}
		}

		viewerSource.highlightLine(sourceHighlight);
		viewerBytecode.highlightLine(bytecodeHighlight);
	}

}
