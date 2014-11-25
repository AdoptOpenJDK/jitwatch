/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.triview;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILER;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILE_KIND;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C2N;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_CLOSE_PARENTHESES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_DOT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_OPEN_PARENTHESES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;

import java.util.List;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
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
import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyMethod;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.model.bytecode.ClassBC;
import org.adoptopenjdk.jitwatch.model.bytecode.LineTable;
import org.adoptopenjdk.jitwatch.model.bytecode.LineTableEntry;
import org.adoptopenjdk.jitwatch.model.bytecode.MemberBytecode;
import org.adoptopenjdk.jitwatch.suggestion.Suggestion;
import org.adoptopenjdk.jitwatch.ui.Dialogs;
import org.adoptopenjdk.jitwatch.ui.JITWatchUI;
import org.adoptopenjdk.jitwatch.ui.StyleUtil;
import org.adoptopenjdk.jitwatch.ui.triview.assembly.ViewerAssembly;
import org.adoptopenjdk.jitwatch.ui.triview.bytecode.BytecodeLabel;
import org.adoptopenjdk.jitwatch.ui.triview.bytecode.ViewerBytecode;
import org.adoptopenjdk.jitwatch.ui.triview.source.ViewerSource;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TriView extends Stage implements ITriView, ILineListener
{
	private IMetaMember currentMember;
	private JITWatchConfig config;

	private ViewerSource viewerSource;
	private ViewerBytecode viewerBytecode;
	private ViewerAssembly viewerAssembly;

	private SplitPane splitViewer;

	private TriViewPane paneSource;
	private TriViewPane paneBytecode;
	private TriViewPane paneAssembly;

	private CheckBox checkSource;
	private CheckBox checkBytecode;
	private CheckBox checkAssembly;

	private ObservableList<IMetaMember> comboMemberList = FXCollections.observableArrayList();

	private ClassSearch classSearch;
	private ComboBox<IMetaMember> comboMember;

	private MemberInfo memberInfo;

	private Label lblMemberInfo;

	private boolean ignoreComboChanged = false;

	private boolean classBytecodeMismatch = false;

	private static final Logger logger = LoggerFactory.getLogger(TriView.class);

	private LineType focussedViewer = LineType.SOURCE;

	private TriViewNavigationStack navigationStack;

	public TriView(final JITWatchUI parent, final JITWatchConfig config)
	{
		this.config = config;

		setTitle("JITWatch TriView: Source, Bytecode, Assembly Viewer");

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

		Button btnCallChain = StyleUtil.buildButton("View Compile Chain");
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

		memberInfo = new MemberInfo();
		memberInfo.setStyle("-fx-padding:0px 0px 0px 110px;");

		hBoxToolBarButtons.getChildren().add(checkSource);
		hBoxToolBarButtons.getChildren().add(checkBytecode);
		hBoxToolBarButtons.getChildren().add(checkAssembly);
		hBoxToolBarButtons.getChildren().add(btnCallChain);
		hBoxToolBarButtons.getChildren().add(memberInfo);

		Label lblClass = new Label("Class:");
		classSearch = new ClassSearch(this, parent.getPackageManager());
		classSearch.prefWidthProperty().bind(widthProperty().multiply(0.4));

		Label lblMember = new Label("Member:");

		comboMember = new ComboBox<>(comboMemberList);
		comboMember.prefWidthProperty().bind(widthProperty().multiply(0.4));

		comboMember.valueProperty().addListener(new ChangeListener<IMetaMember>()
		{
			@Override
			public void changed(ObservableValue<? extends IMetaMember> ov, IMetaMember oldVal, IMetaMember newVal)
			{
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

		Scene scene = new Scene(vBox, JITWatchUI.WINDOW_WIDTH, JITWatchUI.WINDOW_HEIGHT);
		navigationStack = new TriViewNavigationStack(this, scene);

		viewerSource = new ViewerSource(parent, this, LineType.SOURCE);
		viewerBytecode = new ViewerBytecode(parent, navigationStack, parent.getJITDataModel(), this, LineType.BYTECODE);
		viewerAssembly = new ViewerAssembly(parent, this, LineType.ASSEMBLY);

		paneSource = new TriViewPane("Source", viewerSource);
		paneBytecode = new TriViewPane("Bytecode (double click for JVM spec)", viewerBytecode);
		paneAssembly = new TriViewPane("Assembly", viewerAssembly);

		splitViewer.prefHeightProperty().bind(vBox.heightProperty());

		lblMemberInfo = new Label();

		vBox.getChildren().add(hBoxToolBarClass);
		vBox.getChildren().add(hBoxToolBarButtons);
		vBox.getChildren().add(splitViewer);
		vBox.getChildren().add(lblMemberInfo);

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

		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				focusSource();
			}
		});
	}

	private Callback<ListView<IMetaMember>, ListCell<IMetaMember>> getCallbackForCellFactory()
	{
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

					private void performUpdateOfItem(ListCell<IMetaMember> listCell, IMetaMember item)
					{
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
			splitViewer.getItems().add(paneSource);
			colCount++;
		}
		if (checkBytecode.isSelected())
		{
			splitViewer.getItems().add(paneBytecode);
			colCount++;
		}
		if (checkAssembly.isSelected())
		{
			splitViewer.getItems().add(paneAssembly);
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
			comboMemberList.clear();
		}
	}

	public IMetaMember getMetaMember()
	{
		return currentMember;
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

		memberInfo.setMember(member);

		if (!sameClass)
		{
			String sourceFileName = ResourceLoader.getSourceFilename(memberClass, ResourceLoader.SUFFIX_SRC_JAVA);
			String source = ResourceLoader.getSource(config.getSourceLocations(), sourceFileName);

			// TODO Ughhh!
			if (source == null)
			{
				sourceFileName = ResourceLoader.getSourceFilename(memberClass, ResourceLoader.SUFFIX_SRC_SCALA);
				source = ResourceLoader.getSource(config.getSourceLocations(), sourceFileName);
			}

			viewerSource.setContent(source, true);
		}

		if (jumpToSource)
		{
			viewerSource.jumpTo(currentMember);
			viewerSource.setScrollBar();
		}

		StringBuilder statusBarBuilder = new StringBuilder();

		List<String> allClassLocations = config.getAllClassLocations();

		ClassBC classBytecode = loadBytecodeForCurrentMember(allClassLocations);

		updateStatusBarWithClassInformation(classBytecode, statusBarBuilder);

		viewerBytecode.setContent(currentMember, classBytecode, allClassLocations);

		processIfCurrentMemberIsCompiled(statusBarBuilder);

		applyActionsIfOffsetMismatchDetected(statusBarBuilder);

		lblMemberInfo.setText(statusBarBuilder.toString());
	}

	private void applyActionsIfOffsetMismatchDetected(StringBuilder statusBarBuilder)
	{
		if (viewerBytecode.isOffsetMismatchDetected())
		{
			statusBarBuilder.append(C_SPACE).append("WARNING Class bytecode offsets do not match HotSpot log");

			if (!classBytecodeMismatch)
			{
				classBytecodeMismatch = true;

				Platform.runLater(new Runnable()
				{
					@Override
					public void run()
					{
						Dialogs.showOKDialog(TriView.this, "Wrong classes mounted for log file?",
								"Uh-oh, the bytecode for this class does not match the bytecode offsets in your HotSpot log."
										+ S_NEWLINE
										+ "Are the mounted classes the same ones used at runtime when the log was created?");
					}
				});
			}
		}
	}

	private void processIfCurrentMemberIsCompiled(StringBuilder statusBarBuilder)
	{
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

	private ClassBC loadBytecodeForCurrentMember(List<String> classLocations)
	{
		logger.debug("Getting bytecode for {}", currentMember);
		ClassBC classBytecode = currentMember.getMetaClass().getClassBytecode(classLocations);

		return classBytecode;
	}

	private void updateStatusBarWithClassInformation(ClassBC classBytecode, StringBuilder statusBarBuilder)
	{
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
	}

	private void processIfNotSameClass(boolean sameClass, MetaClass memberClass)
	{
		if (!sameClass)
		{
			classBytecodeMismatch = false;

			comboMember.getSelectionModel().clearSelection();
			comboMemberList.clear();
			comboMemberList.addAll(memberClass.getMetaMembers());

			String fqName = memberClass.getFullyQualifiedName();
			classSearch.setText(fqName);
		}
	}

	private boolean evaluateSameClass(boolean force, boolean inSameClass, MetaClass previousClass, MetaClass memberClass)
	{
		boolean result = inSameClass;

		if (!force)
		{
			if ((previousClass != null) && previousClass.equals(memberClass))
			{
				result = true;
			}
		}

		return result;
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

		int bytecodeHighlight = -1;
		int assemblyHighlight = -1;

		MetaClass metaClass = null;

		if (currentMember != null)
		{
			metaClass = currentMember.getMetaClass();
		}

		if (metaClass != null)
		{
			LineTableEntry entry = getLineTableEntryForSourceLine(metaClass, sourceLine);

			if (entry != null)
			{
				MemberSignatureParts msp = entry.getMemberSignatureParts();

				IMetaMember nextMember = metaClass.getMemberFromSignature(msp);

				if (nextMember != null)
				{
					if (!nextMember.equals(currentMember))
					{
						setMember(nextMember, false, false);
					}

					int bcOffset = entry.getBytecodeOffset();

					bytecodeHighlight = viewerBytecode.getLineIndexForBytecodeOffset(bcOffset);
				}
				else
				{
					logger.warn("Could not find member for bytecode signature: {}", msp);
					logger.warn("entry: {}", entry.toString());

				}
			}

			assemblyHighlight = viewerAssembly.getIndexForSourceLine(metaClass.getFullyQualifiedName(), sourceLine);
		}

		viewerBytecode.highlightLine(bytecodeHighlight);
		viewerAssembly.highlightLine(assemblyHighlight);
	}

	private LineTableEntry getLineTableEntryForSourceLine(MetaClass metaClass, int sourceIndex)
	{
		LineTableEntry result = null;

		ClassBC classBytecode = metaClass.getClassBytecode(config.getConfiguredClassLocations());

		if (classBytecode != null)
		{
			result = classBytecode.findLineTableEntryForSourceLine(sourceIndex);
		}

		if (DEBUG_LOGGING)
		{
			logger.debug("source: {} result: {}", sourceIndex, result);
		}

		return result;
	}

	private void highlightFromBytecode(int index)
	{
		MetaClass metaClass = null;

		if (currentMember != null)
		{
			metaClass = currentMember.getMetaClass();
		}

		if (metaClass != null)
		{
			ClassBC classBytecode = metaClass.getClassBytecode(config.getConfiguredClassLocations());

			BytecodeLabel bcLabel = (BytecodeLabel) viewerBytecode.getLabelAtIndex(index);

			if (bcLabel != null)
			{
				BytecodeInstruction instruction = bcLabel.getInstruction();

				int bytecodeOffset = instruction.getOffset();

				int sourceHighlight = -1;
				int assemblyHighlight = viewerAssembly.getIndexForBytecodeOffset(metaClass.getFullyQualifiedName(), bytecodeOffset);

				if (classBytecode != null)
				{
					MemberBytecode memberBytecode = classBytecode.getMemberBytecode(currentMember);

					if (memberBytecode != null)
					{
						LineTable lineTable = memberBytecode.getLineTable();

						sourceHighlight = lineTable.findSourceLineForBytecodeOffset(bytecodeOffset);
					}
					else
					{
						logger.warn("No MemberBytecode found for {}", currentMember);
					}

					if (sourceHighlight != -1)
					{
						// starts at 1
						sourceHighlight--;
					}
				}

				viewerSource.highlightLine(sourceHighlight);
				viewerAssembly.highlightLine(assemblyHighlight);
			}
		}
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

	@Override
	public void handleFocusNext()
	{
		switch (focussedViewer)
		{
		case SOURCE:
			if (checkBytecode.isSelected())
			{
				focusBytecode();
			}
			else if (checkAssembly.isSelected())
			{
				focusAssembly();
			}
			break;
		case BYTECODE:
			if (checkAssembly.isSelected())
			{
				focusAssembly();
			}
			break;
		case ASSEMBLY:
			break;
		}
	}

	@Override
	public void handleFocusPrev()
	{
		switch (focussedViewer)
		{
		case SOURCE:
			break;
		case BYTECODE:
			if (checkSource.isSelected())
			{
				focusSource();
			}
			break;
		case ASSEMBLY:
			if (checkBytecode.isSelected())
			{
				focusBytecode();
			}
			else if (checkSource.isSelected())
			{
				focusSource();
			}
			break;
		}
	}

	@Override
	public void handleFocusSelf(LineType lineType)
	{
		switch (lineType)
		{
		case SOURCE:
			focusSource();
			break;
		case BYTECODE:
			focusBytecode();
			break;
		case ASSEMBLY:
			focusAssembly();
			break;
		}
	}

	private void focusSource()
	{
		paneSource.focus();
		paneBytecode.unFocus();
		paneAssembly.unFocus();

		focussedViewer = LineType.SOURCE;
		viewerSource.requestFocus();
	}

	private void focusBytecode()
	{
		paneSource.unFocus();
		paneBytecode.focus();
		paneAssembly.unFocus();

		focussedViewer = LineType.BYTECODE;
		viewerBytecode.requestFocus();
	}

	private void focusAssembly()
	{
		paneSource.unFocus();
		paneBytecode.unFocus();
		paneAssembly.focus();

		focussedViewer = LineType.ASSEMBLY;
		viewerAssembly.requestFocus();
	}

	@Override
	public void highlightBytecodeForSuggestion(Suggestion suggestion)
	{
		if (viewerBytecode != null)
		{
			viewerBytecode.highlightBytecodeForSuggestion(suggestion);
		}
	}

	@Override
	public void highlightSourceLine(int line)
	{
		highlightFromSource(line-1);
		viewerSource.highlightLine(line-1);
	}
}