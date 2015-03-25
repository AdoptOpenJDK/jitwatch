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
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_DOLLAR;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_DOT;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_OPEN_PARENTHESES;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.C_SPACE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.DEBUG_LOGGING_TRIVIEW;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_NEWLINE;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_STATIC_INIT;

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
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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

	private Button btnCompileChain;
	private Button btnJITJournal;

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

		setTitle("TriView - Source, Bytecode, Assembly Viewer - JITWatch");

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

		btnCompileChain = StyleUtil.buildButton("Chain");
		btnCompileChain.setOnAction(new EventHandler<ActionEvent>()
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
		btnCompileChain.setTooltip(new Tooltip("Show chain of compiled and inlined children"));

		btnJITJournal = StyleUtil.buildButton("Journal");
		btnJITJournal.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				if (currentMember != null)
				{
					parent.openJournalViewer("JIT Journal for " + currentMember.toString(), currentMember);
				}
			}
		});
		btnJITJournal.setTooltip(new Tooltip("Show journal of JIT events for this member"));

		memberInfo = new MemberInfo();

		Region spacerTop = new Region();
		HBox.setHgrow(spacerTop, Priority.ALWAYS);

		Region spacerBottom = new Region();
		HBox.setHgrow(spacerBottom, Priority.ALWAYS);

		hBoxToolBarButtons.getChildren().add(checkSource);
		hBoxToolBarButtons.getChildren().add(checkBytecode);
		hBoxToolBarButtons.getChildren().add(checkAssembly);
		hBoxToolBarButtons.getChildren().add(btnCompileChain);
		hBoxToolBarButtons.getChildren().add(btnJITJournal);
		hBoxToolBarButtons.getChildren().add(getMouseFollowCheckBox());
		hBoxToolBarButtons.getChildren().add(spacerBottom);
		hBoxToolBarButtons.getChildren().add(memberInfo);
		
		Label lblClass = new Label("Class:");
		classSearch = new ClassSearch(this, parent.getPackageManager());
		classSearch.prefWidthProperty().bind(widthProperty().multiply(0.42));

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

		hBoxToolBarClass.getChildren().add(spacerTop);

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

		paneAssembly = new TriViewPane("Assembly", viewerAssembly, getAssemblyTitleComponents());

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

	private HBox getAssemblyTitleComponents()
	{
		HBox hbox = new HBox();
		hbox.setSpacing(16);

		CheckBox cb = new CheckBox("Local labels");

		cb.setSelected(config.isLocalAsmLabels());

		cb.selectedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal)
			{
				config.setLocalAsmLabels(newVal);
				config.saveConfig();
				setAssemblyPaneContent();
			}
		});

		hbox.getChildren().add(cb);

		return hbox;
	}

	private CheckBox getMouseFollowCheckBox()
	{
		CheckBox cb = new CheckBox("Mouse Follow");

		cb.setSelected(config.isTriViewMouseFollow());

		cb.selectedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal)
			{
				config.setTriViewMouseFollow(newVal);
				config.saveConfig();
			}
		});

		return cb;
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

						if (item.isCompiled() && UserInterfaceUtil.IMAGE_TICK != null)
						{
							listCell.setGraphic(new ImageView(UserInterfaceUtil.IMAGE_TICK));
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
			String source = ResourceLoader.getSource(memberClass, config.getSourceLocations());

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
		updateStatusBarIfCompiled(statusBarBuilder);
		applyActionsIfOffsetMismatchDetected(statusBarBuilder);

		viewerBytecode.setContent(currentMember, classBytecode, allClassLocations);

		setAssemblyPaneContent();

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

	private void setAssemblyPaneContent()
	{
		AssemblyMethod asmMethod = null;
		
		if (currentMember.isCompiled())
		{
			asmMethod = currentMember.getAssembly();

			if (asmMethod == null)
			{
				String msg = "Assembly not found. Was -XX:+PrintAssembly option used?";
				viewerAssembly.setContent(msg, false);
			}
			else
			{
				viewerAssembly.setAssemblyMethod(asmMethod, config.isLocalAsmLabels());
			}
		}
		else
		{
			String msg = "Not JIT-compiled";
			viewerAssembly.setContent(msg, false);

			lblMemberInfo.setText(S_EMPTY);
		}
	}	
	
	private void updateStatusBarIfCompiled(StringBuilder statusBarBuilder)
	{
		if (currentMember.isCompiled())
		{
			statusBarBuilder.append(C_SPACE).append(currentMember.toStringUnqualifiedMethodName(false));

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
		}
	}
	
	
	private ClassBC loadBytecodeForCurrentMember(List<String> classLocations)
	{
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
			highlightFromSource(index, MASK_UPDATE_ASSEMBLY | MASK_UPDATE_BYTECODE);
			break;
		case BYTECODE:
			highlightFromBytecode(index);
			break;
		case ASSEMBLY:
			highlightFromAssembly(index);
			break;
		}
	}

	private void highlightFromSource(int index, int updateMask)
	{
		int sourceLine = index + 1;

		MetaClass metaClass = null;

		if (currentMember != null)
		{
			metaClass = currentMember.getMetaClass();
		}

		if (DEBUG_LOGGING_TRIVIEW)
		{
			logger.debug("highlightFromSource: {}", sourceLine);
		}

		if (metaClass != null)
		{
			MemberBytecode memberBytecode = getMemberBytecodeForSourceLine(metaClass, sourceLine);

			if (memberBytecode != null)
			{
				if (DEBUG_LOGGING_TRIVIEW)
				{
					logger.debug("Found MemberBytecode for sourceLine: {}", sourceLine);
				}

				MemberSignatureParts msp = memberBytecode.getMemberSignatureParts();

				if (DEBUG_LOGGING_TRIVIEW)
				{
					logger.debug("MemberSignatureParts:\n{}", msp);
				}

				IMetaMember nextMember = metaClass.getMemberForSignature(msp);

				if (DEBUG_LOGGING_TRIVIEW)
				{
					logger.debug("nextMember: {}", nextMember);
				}

				if (nextMember != null)
				{
					if (!nextMember.equals(currentMember))
					{
						setMember(nextMember, false, false);
					}

					LineTable lineTable = memberBytecode.getLineTable();

					if ((updateMask & MASK_UPDATE_BYTECODE) == MASK_UPDATE_BYTECODE)
					{
						LineTableEntry lineTableEntry = lineTable.getEntryForSourceLine(sourceLine);

						if (DEBUG_LOGGING_TRIVIEW)
						{
							logger.debug("getEntryForSourceLine({}) : {}", sourceLine, lineTableEntry);
						}

						if (lineTableEntry != null)
						{
							int bcOffset = lineTableEntry.getBytecodeOffset();

							int bytecodeHighlight = viewerBytecode.getLineIndexForBytecodeOffset(bcOffset);
							viewerBytecode.highlightLine(bytecodeHighlight);
						}
					}
				}
				else if (msp != null && !msp.getMemberName().equals(S_STATIC_INIT))
				{
					logger.warn("Could not find member for bytecode signature: {}", msp);
				}
			}

			if ((updateMask & MASK_UPDATE_ASSEMBLY) == MASK_UPDATE_ASSEMBLY)
			{
				int assemblyHighlight = -1;
				assemblyHighlight = viewerAssembly.getIndexForSourceLine(metaClass.getFullyQualifiedName(), sourceLine);
				viewerAssembly.highlightLine(assemblyHighlight);
			}
		}
	}

	private MemberBytecode getMemberBytecodeForSourceLine(MetaClass metaClass, int sourceIndex)
	{
		MemberBytecode result = null;

		ClassBC classBytecode = metaClass.getClassBytecode(config.getConfiguredClassLocations());

		if (classBytecode != null)
		{
			result = classBytecode.getMemberBytecodeForSourceLine(sourceIndex);
		}

		if (DEBUG_LOGGING_TRIVIEW)
		{
			logger.debug("source: {} result: {}", sourceIndex, result != null);
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
				int assemblyHighlight = viewerAssembly.getIndexForBytecodeOffset(metaClass.getFullyQualifiedName(), instruction);

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

			if (isClassNameEqualsCurrentMemberClassName(className) || isClassNameAnInnerClassOfCurrentMember(className))
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

				// TODO cache bytecode for inner classes and switch bytecode
				// panel when BCI is for inner class
				if (bytecodeLine != null && !isClassNameAnInnerClassOfCurrentMember(className))
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

	private boolean isClassNameEqualsCurrentMemberClassName(String className)
	{
		boolean result = false;

		if (currentMember != null && className != null)
		{
			String currentFQClassName = currentMember.getMetaClass().getFullyQualifiedName();

			result = className.equals(currentFQClassName);
		}

		return result;
	}

	private boolean isClassNameAnInnerClassOfCurrentMember(String className)
	{
		boolean result = false;

		if (currentMember != null && className != null)
		{
			int dollarPos = className.indexOf(C_DOLLAR);

			if (dollarPos != -1)
			{
				String currentFQClassName = currentMember.getMetaClass().getFullyQualifiedName();

				String baseClassName = className.substring(0, dollarPos);

				if (currentFQClassName.equals(baseClassName))
				{
					result = true;
				}
			}
		}

		return result;
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
	public void highlightBytecodeOffset(int bci, int updateMask)
	{
		if (viewerBytecode != null)
		{
			viewerBytecode.highlightBytecodeOffset(bci);
		}
	}

	@Override
	public void highlightSourceLine(int line, int updateMask)
	{
		highlightFromSource(line - 1, updateMask);
		viewerSource.highlightLine(line - 1);
	}
}