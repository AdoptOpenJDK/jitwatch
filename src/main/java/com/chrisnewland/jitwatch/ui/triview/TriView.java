/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui.triview;

import com.chrisnewland.jitwatch.core.JITWatchConfig;
import com.chrisnewland.jitwatch.loader.ResourceLoader;
import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.MetaClass;
import com.chrisnewland.jitwatch.model.assembly.AssemblyMethod;
import com.chrisnewland.jitwatch.model.bytecode.BytecodeInstruction;
import com.chrisnewland.jitwatch.model.bytecode.ClassBC;
import com.chrisnewland.jitwatch.model.bytecode.LineTable;
import com.chrisnewland.jitwatch.model.bytecode.LineTableEntry;
import com.chrisnewland.jitwatch.ui.JITWatchUI;
import com.chrisnewland.jitwatch.ui.triview.assembly.ViewerAssembly;
import com.chrisnewland.jitwatch.ui.triview.bytecode.BytecodeLabel;
import com.chrisnewland.jitwatch.ui.triview.bytecode.ViewerBytecode;
import com.chrisnewland.jitwatch.ui.triview.source.ViewerSource;
import com.chrisnewland.jitwatch.util.UserInterfaceUtil;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public class TriView extends Stage implements ILineListener
{
    private static final int TEN_FOR_TOP_RIGHT_BOTTOM_LEFT = 10;
    private static final int TEN_SPACES = 10;
    private static final int TOP = 0;
    private static final int RIGHT = 10;
    private static final int LEFT = 10;
    private static final int BOTTOM = 10;
    private static final double BY_RATIO_OF_4_BY_10 = 0.4;
    private static final double BY_RATIO_OF_5_BY_10 = 0.5;
    private static final double RATIO_OF_ONE_THIRD = 0.333;
    private static final double RATIO_OF_TWO_THIRD = 0.666;
    private static final int RATIO_ZERO = 0;
    private static final int RATIO_ONE = 1;
    private static final int FIRST_COLUMN = 0;
    private static final int SECOND_COLUMN = 1;
    private static final int FOURTH_COLUMN = 3;
    private static final int THIRD_COLUMN = 2;

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

	private static final Logger logger = LoggerFactory.getLogger(TriView.class);

	public TriView(final JITWatchUI parent, final JITWatchConfig config)
	{
		this.config = config;

		setTitle("TriView Source, Bytecode, Assembly Viewer");

		VBox vBox = new VBox();

		HBox hBoxToolBarClass = new HBox();
		hBoxToolBarClass.setSpacing(TEN_SPACES);
		hBoxToolBarClass.setPadding(new Insets(TEN_FOR_TOP_RIGHT_BOTTOM_LEFT));

		HBox hBoxToolBarButtons = new HBox();
		hBoxToolBarButtons.setSpacing(TEN_SPACES);
		hBoxToolBarButtons.setPadding(new Insets(TOP, RIGHT, BOTTOM, LEFT));

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
		classSearch.prefWidthProperty().bind(widthProperty().multiply(BY_RATIO_OF_4_BY_10));

		Label lblMember = new Label("Member:");

		comboMember = new ComboBox<>();
		comboMember.prefWidthProperty().bind(widthProperty().multiply(BY_RATIO_OF_4_BY_10));

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

		comboMember.setCellFactory(new Callback<ListView<IMetaMember>, ListCell<IMetaMember>>()
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
							setText(item.toStringUnqualifiedMethodName(false));

							if (item.isCompiled() && UserInterfaceUtil.getTick() != null)
							{
								setGraphic(new ImageView(UserInterfaceUtil.getTick()));
							}
							else
							{
								setGraphic(null);
							}
						}
					}
				};
			}
		});

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
		case FIRST_COLUMN:
			splitViewer.setDividerPositions(RATIO_ZERO);
			break;
		case SECOND_COLUMN:
			splitViewer.setDividerPositions(RATIO_ONE);
			break;
		case THIRD_COLUMN:
			splitViewer.setDividerPositions(BY_RATIO_OF_5_BY_10);
			break;
		case FOURTH_COLUMN:
			splitViewer.setDividerPositions(RATIO_OF_ONE_THIRD, RATIO_OF_TWO_THIRD);
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

		if (!force)
		{
			if ((previousClass != null) && previousClass.equals(memberClass))
			{
				sameClass = true;
			}
		}

		if (!sameClass)
		{
			comboMember.getSelectionModel().clearSelection();
			comboMember.getItems().clear();
			comboMember.getItems().addAll(memberClass.getMetaMembers());

			String fqName = memberClass.getFullyQualifiedName();
			classSearch.setText(fqName);
		}

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
		}

		List<String> classLocations = config.getClassLocations();

		viewerBytecode.setContent(currentMember, classLocations);

		AssemblyMethod asmMethod = null;

		if (currentMember.isCompiled())
		{			
			asmMethod = currentMember.getAssembly();

			String attrCompiler = currentMember.getCompiledAttribute(ATTR_COMPILER);

			if (attrCompiler != null)
			{
				lblMemberInfo.setText("Compiled with " + attrCompiler);
			}
			else
			{
				String attrCompileKind = currentMember.getCompiledAttribute(ATTR_COMPILE_KIND);

				if (attrCompileKind != null && C2N.equals(attrCompileKind))
				{
					lblMemberInfo.setText("Compiled native wrapper");
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

			LineTableEntry entry = lineTable.get(sourceLine);

			if (entry != null)
			{
				String memberSig = entry.getMemberSignature();

				IMetaMember nextMember = currentMember.getMetaClass().findMemberByBytecodeSignature(memberSig);

				if (nextMember != null)
				{
					if (nextMember != currentMember)
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

		BytecodeLabel bcLabel = (BytecodeLabel)viewerBytecode.getLabelAtIndex(index);
		
		BytecodeInstruction instruction = bcLabel.getInstruction();
		
		int bytecodeOffset = instruction.getOffset();
		
		int sourceHighlight = -1;
		int assemblyHighlight = viewerAssembly.getIndexForBytecodeOffset(currentMember.getMetaClass().getFullyQualifiedName(), bytecodeOffset);
	
		if (classBytecode != null)
		{
			LineTable lineTable = classBytecode.getLineTable();

			sourceHighlight = lineTable.findSourceLine(currentMember, bytecodeOffset);
			
			if (sourceHighlight != -1)
			{
				//starts at 1
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
