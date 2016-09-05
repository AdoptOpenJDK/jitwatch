/*
 * Copyright (c) 2013-2016 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.suggestion;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.layout.VBox;

import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.S_EMPTY;
import org.adoptopenjdk.jitwatch.model.Compilation;
import org.adoptopenjdk.jitwatch.model.IMetaMember;
import org.adoptopenjdk.jitwatch.suggestion.Suggestion;
import org.adoptopenjdk.jitwatch.ui.IStageAccessProxy;
import org.adoptopenjdk.jitwatch.ui.triview.ITriView;

public class MemberTableCell extends TableCell<SuggestTableRow, Suggestion>
{
	private VBox vb;
	private Label lblMetaClass;
	private Label lblMetaMember;
	private Label lblCompilation;

	private Button btnTriView;

	private static IStageAccessProxy triViewAccessor;

	public static void setTriViewAccessor(IStageAccessProxy triViewAccessor)
	{
		MemberTableCell.triViewAccessor = triViewAccessor;
	}

	public MemberTableCell()
	{
		vb = new VBox();

		lblMetaClass = new Label();
		lblMetaMember = new Label();
		lblCompilation = new Label();
		
		btnTriView = new Button("View");

		vb.getChildren().add(lblMetaClass);
		vb.getChildren().add(lblMetaMember);
		vb.getChildren().add(lblCompilation);

		vb.getChildren().add(btnTriView);

		vb.setSpacing(5);

		setGraphic(vb);
	}

	@Override
	protected void updateItem(final Suggestion suggestion, boolean empty)
	{
		if (suggestion != null && suggestion.getCaller() != null)
		{
			final IMetaMember member = suggestion.getCaller();

			btnTriView.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent e)
				{
					if (suggestion.getCompilationIndex() != -1)
					{
						member.setSelectedCompilation(suggestion.getCompilationIndex());
					}
					
					ITriView triViewAccesor = triViewAccessor.openTriView(member, false);
					triViewAccesor.highlightBytecodeForSuggestion(suggestion);
				}
			});

			lblMetaClass.setText(member.getMetaClass().getFullyQualifiedName());
			lblMetaMember.setText(member.toStringUnqualifiedMethodName(false));
			
			Compilation compilation = member.getCompilation(suggestion.getCompilationIndex());
			
			String compilationText = compilation != null ? "Compilation: " +compilation.getSignature() : S_EMPTY;
			
			lblCompilation.setText(compilationText);


			btnTriView.setVisible(true);
		}
		else
		{
			btnTriView.setVisible(false);
		}
	}
}