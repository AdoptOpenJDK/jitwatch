/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.triview;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILER;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILE_KIND;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_COMPILE_MILLIS;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_LEVEL;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.ATTR_SIZE;
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
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.TAG_NMETHOD;

import java.util.List;

import org.adoptopenjdk.jitwatch.core.JITWatchConfig;
import org.adoptopenjdk.jitwatch.loader.ResourceLoader;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.model.IReadOnlyJITDataModel;
import org.adoptopenjdk.jitwatch.model.Journal;
import org.adoptopenjdk.jitwatch.model.MemberSignatureParts;
import org.adoptopenjdk.jitwatch.model.MetaClass;
import org.adoptopenjdk.jitwatch.model.Tag;
import org.adoptopenjdk.jitwatch.model.assembly.AssemblyMethod;
import org.adoptopenjdk.jitwatch.model.bytecode.BytecodeInstruction;
import org.adoptopenjdk.jitwatch.model.bytecode.ClassBC;
import org.adoptopenjdk.jitwatch.model.bytecode.LineTable;
import org.adoptopenjdk.jitwatch.model.bytecode.LineTableEntry;
import org.adoptopenjdk.jitwatch.model.bytecode.MemberBytecode;
import org.adoptopenjdk.jitwatch.model.bytecode.SourceMapper;
import org.adoptopenjdk.jitwatch.suggestion.Suggestion;
import org.adoptopenjdk.jitwatch.ui.Dialogs;
import org.adoptopenjdk.jitwatch.ui.JITWatchUI;
import org.adoptopenjdk.jitwatch.ui.triview.assembly.ViewerAssembly;
import org.adoptopenjdk.jitwatch.ui.triview.bytecode.BytecodeLabel;
import org.adoptopenjdk.jitwatch.ui.triview.bytecode.ViewerBytecode;
import org.adoptopenjdk.jitwatch.ui.triview.source.ViewerSource;
import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private CheckBox checkMouseover;
	private CheckBox checkLocalLabels;

	private Button btnCompileChain;
	private Button btnJITJournal;
	private Button btnLineTable;

	private ObservableList<IMetaMember> comboMemberList = FXCollections.observableArrayList();
	private ComboBox<IMetaMember> comboMember;

	private ObservableList<String> comboAssemblyMethodList = FXCollections.observableArrayList();
	private ComboBox<String> comboAssemblyMethod;

	private ClassSearch classSearch;

	private MemberInfo memberInfo;

	private Label lblMemberInfo;

	private boolean ignoreComboChanged = false;

	private boolean classBytecodeMismatch = false;

	private static final Logger logger = LoggerFactory.getLogger(TriView.class);

	private LineType focussedViewer = LineType.SOURCE;

	private TriViewNavigationStack navigationStack;

	private IReadOnlyJITDataModel model;

	public TriView(final JITWatchUI parent, final JITWatchConfig config)
	{
		this.config = config;
		this.model = parent.getJITDataModel();

		setTitle("TriView - Source, Bytecode, Assembly Viewer - JITWatch");

		VBox vBox = new VBox();

		HBox hBoxToolBarClass = new HBox();
		hBoxToolBarClass.setSpacing(10);
		hBoxToolBarClass.setPadding(new Insets(10));

		HBox hBoxToolBarButtons = new HBox();
		hBoxToolBarButtons.setSpacing(10);
		hBoxToolBarButtons.setPadding(new Insets(0, 10, 10, 10));

		setupCheckBoxes();

		btnCompileChain = new Button("Chain");
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

		btnJITJournal = new Button("Journal");
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

		btnLineTable = new Button("LNT");
		btnLineTable.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				if (currentMember != null)
				{
					String lineNumberTable = currentMember.getMemberBytecode().getLineTable().toString();

					parent.openTextViewer("LineNumberTable for " + currentMember.toString(), lineNumberTable, false, false);
				}
			}
		});
		btnLineTable.setTooltip(new Tooltip("Show LineNumberTable for current bytecode"));

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
		hBoxToolBarButtons.getChildren().add(btnLineTable);
		hBoxToolBarButtons.getChildren().add(checkMouseover);
		hBoxToolBarButtons.getChildren().add(spacerBottom);
		hBoxToolBarButtons.getChildren().add(memberInfo);

		Label lblClass = new Label("Class:");
		classSearch = new ClassSearch(this, parent.getPackageManager());
		classSearch.prefWidthProperty().bind(widthProperty().multiply(0.42));

		Label lblMember = new Label("Member:");

		// ================ Set up Member combo box ====================

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

		comboMember.setCellFactory(getCallbackForMemberListCellFactory());

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

		Scene scene = UserInterfaceUtil.getScene(vBox, JITWatchUI.WINDOW_WIDTH, JITWatchUI.WINDOW_HEIGHT);
		navigationStack = new TriViewNavigationStack(this, scene);

		viewerSource = new ViewerSource(parent, this, LineType.SOURCE);
		viewerBytecode = new ViewerBytecode(parent, navigationStack, model, this, LineType.BYTECODE);
		viewerAssembly = new ViewerAssembly(parent, this, LineType.ASSEMBLY);

		paneSource = new TriViewPane(this, "Source", viewerSource);
		paneBytecode = new TriViewPane(this, "Bytecode (double click for JVM spec)", viewerBytecode);
		paneAssembly = new TriViewPane(this, "Assembly", viewerAssembly, getAssemblyTitleComponents());

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

	private void setupCheckBoxes()
	{
		checkSource = new CheckBox("_Source");
		checkBytecode = new CheckBox("_Bytecode");
		checkAssembly = new CheckBox("_Assembly");

		createCheckBoxMouseFollow();

		checkSource.setSelected(true);
		checkBytecode.setSelected(true);
		checkAssembly.setSelected(true);

		addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, new EventHandler<javafx.scene.input.KeyEvent>()
		{
			@Override
			public void handle(javafx.scene.input.KeyEvent event)
			{
				if (event.isAltDown())
				{
					switch (event.getCode())
					{
					case S:
						checkSource.setSelected(!checkSource.isSelected());
						break;
					case B:
						checkBytecode.setSelected(!checkBytecode.isSelected());
						break;
					case A:
						checkAssembly.setSelected(!checkAssembly.isSelected());
						break;
					case M:
						checkMouseover.setSelected(!checkMouseover.isSelected());
						break;
					case L:
						checkLocalLabels.setSelected(!checkLocalLabels.isSelected());
						break;
					default:
						break;
					}
				}
			}
		});

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
	}

	private HBox getAssemblyTitleComponents()
	{
		HBox hbox = new HBox();
		hbox.setSpacing(16);

		checkLocalLabels = new CheckBox("_Labels");
		checkLocalLabels.setTooltip(new Tooltip("Local labels"));

		checkLocalLabels.setSelected(config.isLocalAsmLabels());

		checkLocalLabels.selectedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal)
			{
				config.setLocalAsmLabels(newVal);
				config.saveConfig();
				
				if (currentMember != null)
				{
					setAssemblyPaneContent(0);
				}
			}
		});

		hbox.getChildren().add(checkLocalLabels);

		comboAssemblyMethod = new ComboBox<>(comboAssemblyMethodList);
		comboAssemblyMethod.setStyle("-fx-font-size: 10px");

		comboAssemblyMethod.valueProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal)
			{
				setAssemblyPaneContent(comboAssemblyMethod.getSelectionModel().getSelectedIndex());
			}
		});

		hbox.getChildren().add(comboAssemblyMethod);

		return hbox;
	}

	private void createCheckBoxMouseFollow()
	{
		checkMouseover = new CheckBox("_Mouseover");

		checkMouseover.setSelected(config.isTriViewMouseFollow());

		checkMouseover.selectedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal)
			{
				config.setTriViewMouseFollow(newVal);
				config.saveConfig();
			}
		});
	}

	private Callback<ListView<IMetaMember>, ListCell<IMetaMember>> getCallbackForMemberListCellFactory()
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

		List<String> allClassLocations = config.getAllClassLocations();

		ClassBC classBytecode = currentMember.getMetaClass().getClassBytecode(model, allClassLocations);

		if (!sameClass)
		{
			String source = ResourceLoader.getSourceForClassName(memberClass.getFullyQualifiedName(), config.getSourceLocations());

			if (source == null)
			{
				String sourceFileName = classBytecode.getSourceFile();

				logger.debug("Could not find source for {}. Trying to locate via bytecode source file attribute {}", memberClass,
						sourceFileName);

				if (sourceFileName != null)
				{
					source = ResourceLoader.getSourceForFilename(sourceFileName, config.getSourceLocations());
				}
			}

			viewerSource.setContent(source, true);
		}

		if (jumpToSource)
		{
			viewerSource.jumpTo(currentMember);
			viewerSource.setScrollBar();
		}

		StringBuilder statusBarBuilder = new StringBuilder();

		updateStatusBarWithClassInformation(classBytecode, statusBarBuilder);
		updateStatusBarIfCompiled(statusBarBuilder);
		applyActionsIfOffsetMismatchDetected(statusBarBuilder);

		viewerBytecode.setContent(currentMember);

		comboAssemblyMethod.getSelectionModel().clearSelection();
		comboAssemblyMethodList.clear();

		List<AssemblyMethod> assemblyMethods = currentMember.getAssemblyMethods();
		int numAssemblyMethods = assemblyMethods.size();

		for (int i = 0; i < numAssemblyMethods; i++)
		{
			comboAssemblyMethodList.add("#" + (i+1) + getCompilationDetail(i));
		}

		int selectedAssembly = numAssemblyMethods - 1;
		
		comboAssemblyMethod.getSelectionModel().select(selectedAssembly);
		
		comboAssemblyMethod.setVisible(numAssemblyMethods > 0);

		setAssemblyPaneContent(selectedAssembly);
		
		lblMemberInfo.setText(statusBarBuilder.toString());
	}

	private String getCompilationDetail(int index)
	{
		Journal journal = currentMember.getJournal();

		StringBuilder builder = new StringBuilder();

		int tagIndex = 0;

		for (Tag tag : journal.getEntryList())
		{
			if (TAG_NMETHOD.equals(tag.getName()))
			{
				if (index == tagIndex)
				{
					String level = tag.getAttribute(ATTR_LEVEL);
					String compiler = tag.getAttribute(ATTR_COMPILER);
					String compileKind = tag.getAttribute(ATTR_COMPILE_KIND);
										
					builder.append("  (").append(compiler);
					
					if (compileKind != null)
					{
						builder.append(" / ").append(compileKind.toUpperCase());
					}
					
					if (level != null)
					{
						builder.append(" / Level ").append(level);
					}
					
					builder.append(")");
										
					break;
				}
				else
				{
					tagIndex++;
				}
			}
		}

		return builder.toString();
	}
	
	private void setMemberInfoAssemblyDetails(int compilation)
	{
		Journal journal = currentMember.getJournal();

		int tagIndex = 0;

		for (Tag tag : journal.getEntryList())
		{
			if (TAG_NMETHOD.equals(tag.getName()))
			{
				if (compilation == tagIndex)
				{
					String nativeSize = tag.getAttribute(ATTR_SIZE); // nmethod tag has size attr, task_done has nmsize
					String compileMillis = tag.getAttribute(ATTR_COMPILE_MILLIS);
					
					memberInfo.setAssemblyDetails(nativeSize, compileMillis);
					
					break;
				}
				else
				{
					tagIndex++;
				}
			}
		}
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

	private void setAssemblyPaneContent(int index)
	{
		AssemblyMethod asmMethod = null;

		if (currentMember.isCompiled())
		{
			List<AssemblyMethod> assemblyMethods = currentMember.getAssemblyMethods();

			if (index >= 0 && index < assemblyMethods.size())
			{
				setMemberInfoAssemblyDetails(index);

				asmMethod = assemblyMethods.get(index);
			}

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
		default:
			break;
		}
	}

	private void highlightFromSource(int index, int updateMask)
	{
		int sourceLine = index + 1;
		int bytecodeHighlight = -1;

		MetaClass metaClass = null;

		if (currentMember != null)
		{
			metaClass = currentMember.getMetaClass();
		}

		if (DEBUG_LOGGING_TRIVIEW)
		{
			logger.debug("highlightFromSource: {}", sourceLine);
			logger.debug("metaClass: {}", metaClass.getFullyQualifiedName());
		}

		if (metaClass != null)
		{
			MemberBytecode memberBytecode = SourceMapper.getMemberBytecodeForSourceLine(
					metaClass.getClassBytecode(model, config.getConfiguredClassLocations()), sourceLine);

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

				if (!msp.getFullyQualifiedClassName().equals(metaClass.getFullyQualifiedName()))
				{
					if (DEBUG_LOGGING_TRIVIEW)
					{
						logger.debug("Different class in this source file");
					}

					metaClass = model.getPackageManager().getMetaClass(msp.getFullyQualifiedClassName());
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

					if ((updateMask & MASK_UPDATE_BYTECODE) == MASK_UPDATE_BYTECODE)
					{
						LineTable lineTable = memberBytecode.getLineTable();

						LineTableEntry lineTableEntry = lineTable.getEntryForSourceLine(sourceLine);

						if (DEBUG_LOGGING_TRIVIEW)
						{
							logger.debug("getEntryForSourceLine({}) : {}", sourceLine, lineTableEntry);
						}

						if (lineTableEntry != null)
						{
							int bcOffset = lineTableEntry.getBytecodeOffset();

							bytecodeHighlight = viewerBytecode.getLineIndexForBytecodeOffset(bcOffset);
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

		viewerBytecode.highlightLine(bytecodeHighlight);
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
			BytecodeLabel bcLabel = (BytecodeLabel) viewerBytecode.getLabelAtIndex(index);

			if (bcLabel != null)
			{
				BytecodeInstruction instruction = bcLabel.getInstruction();

				int bytecodeOffset = instruction.getOffset();

				int sourceHighlight = -1;
				int assemblyHighlight = viewerAssembly.getIndexForBytecodeOffset(metaClass.getFullyQualifiedName(), instruction);

				ClassBC classBytecode = metaClass.getClassBytecode(model, config.getConfiguredClassLocations());
				
				if (classBytecode != null)
				{
					MemberBytecode memberBytecode = classBytecode.getMemberBytecode(currentMember);

					if (memberBytecode != null)
					{
						sourceHighlight = SourceMapper.getSourceLineFromBytecode(memberBytecode, bytecodeOffset);
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

		if (label != null)
		{
			String className = viewerAssembly.getClassNameFromLabel(label);

			if (isClassNameEqualsCurrentMemberClassName(className) || isClassNameAnInnerClassOfCurrentMember(className))
			{
				String sourceLine = viewerAssembly.getSourceLineFromLabel(label);
				String bci = viewerAssembly.getBytecodeOffsetFromLabel(label);

				if (sourceLine != null)
				{
					try
					{
						int sourceHighlight = Integer.parseInt(sourceLine) - 1;
						
						viewerSource.highlightLine(sourceHighlight);
						
						if (bci != null)
						{
							try
							{
								int bciIndex = Integer.parseInt(bci);
								viewerBytecode.highlightBytecodeOffset(bciIndex);
							}
							catch (NumberFormatException nfe)
							{
								logger.error("Could not parse bci: {}", bci, nfe);
							}
						}
					}
					catch (NumberFormatException nfe)
					{
						logger.error("Could not parse source line number: {}", sourceLine, nfe);
					}
				}
			}
		}
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
		default:
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
		default:
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
		default:
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