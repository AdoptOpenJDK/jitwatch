/*
 * Copyright (c) 2013, 2014 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package com.chrisnewland.jitwatch.ui.triview;

import java.util.List;
import com.chrisnewland.jitwatch.core.JITWatchConfig;
import com.chrisnewland.jitwatch.loader.ResourceLoader;
import com.chrisnewland.jitwatch.model.IMetaMember;
import com.chrisnewland.jitwatch.model.MetaClass;
import com.chrisnewland.jitwatch.ui.JITWatchUI;
import com.chrisnewland.jitwatch.util.UserInterfaceUtil;

import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class TriView extends Stage
{
	private IMetaMember currentMember;
	private JITWatchConfig config;

	private Viewer viewerSource;
	private ViewerBytecode viewerBytecode;
	private Viewer viewerAssembly; // TODO ref.x86asm.net

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

	public TriView(final JITWatchUI parent, final JITWatchConfig config)
	{
		this.config = config;

		setTitle("TriView Source, Bytecode, Assembly Viewer");

		VBox vBox = new VBox();

		HBox hBoxToolBarClass = new HBox();
		hBoxToolBarClass.setSpacing(10);
		hBoxToolBarClass.setPadding(new Insets(10));

		HBox hBoxToolBarButtons = new HBox();
		hBoxToolBarButtons.setSpacing(10);
		hBoxToolBarButtons.setPadding(new Insets(0,10,10,10));

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
						TriView.this.setMember(newVal);
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
							setText(item.toStringUnqualifiedMethodName());

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
				return mm.toStringUnqualifiedMethodName();
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

		viewerSource = new Viewer(parent);
		viewerBytecode = new ViewerBytecode(parent);
		viewerAssembly = new Viewer(parent);

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
			setMember(members.get(0));
		}
		else
		{
			// unlikely but if no members then clear the combo
			comboMember.getItems().clear();
		}
	}

	public void setMember(IMetaMember member)
	{
		boolean sameClass = false;

		MetaClass previousClass = currentMember == null ? null : currentMember.getMetaClass();

		currentMember = member;

		final MetaClass memberClass = currentMember.getMetaClass();

		if ((previousClass != null) && previousClass.equals(memberClass))
		{
			sameClass = true;
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

		viewerSource.jumpTo(currentMember);

		List<String> classLocations = config.getClassLocations();

		viewerBytecode.setContent(currentMember, classLocations);

		String assembly;

		if (currentMember.isCompiled())
		{
			assembly = currentMember.getAssembly();

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
			
			if (assembly == null)
			{
				assembly = "Assembly not found. Was -XX:+PrintAssembly option used?";
			}
		}
		else
		{
			assembly = "Not JIT-compiled";
			lblMemberInfo.setText(S_EMPTY);
		}

		viewerAssembly.setContent(assembly, false);
	}
}
