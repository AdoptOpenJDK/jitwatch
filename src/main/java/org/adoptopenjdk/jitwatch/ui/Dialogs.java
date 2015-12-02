/*
 * Copyright (c) 2013-2015 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

import org.adoptopenjdk.jitwatch.util.UserInterfaceUtil;

public final class Dialogs
{
	/*
	 * Hide Utility Class Constructor Utility classes should not have a public
	 * or default constructor.
	 */
	private Dialogs()
	{
	}

	public enum Response
	{
		NO, YES
	};

	private static Response response = Response.NO;

	private static String textInput;

	public static String getTextInput()
	{
		return textInput;
	}

	static class Dialog extends Stage
	{

		public Dialog(String title, Stage owner, Scene scene)
		{
			setTitle(title);

			initStyle(StageStyle.UTILITY);

			initModality(Modality.APPLICATION_MODAL);

			initOwner(owner);

			setResizable(false);

			setScene(scene);
		}

		public void showDialog()
		{
			sizeToScene();

			centerOnScreen();

			showAndWait();
		}
	}

	public static Response showYesNoDialog(Stage owner, String title, String message)
	{
		VBox vBox = new VBox();
		vBox.setAlignment(Pos.CENTER);
		vBox.setSpacing(10);
		vBox.setPadding(new Insets(10));

		int width = Math.max(320, message == null ? 0 : message.length() * 10);

		Scene scene = UserInterfaceUtil.getScene(vBox, width, 80);

		final Dialog dialog = new Dialog(title, owner, scene);

		Button btnYes = new Button("Yes");

		btnYes.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				dialog.close();
				response = Response.YES;
			}
		});

		Button btnNo = new Button("No");

		btnNo.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				dialog.close();
				response = Response.NO;
			}
		});

		BorderPane bp = new BorderPane();

		HBox hBox = new HBox();
		hBox.setAlignment(Pos.CENTER);
		hBox.setSpacing(10);
		hBox.setPadding(new Insets(10));

		hBox.getChildren().addAll(btnYes, btnNo);

		bp.setCenter(hBox);

		vBox.getChildren().addAll(new Label(message), bp);

		dialog.showDialog();

		return response;
	}

	public static Response showTextInputDialog(Stage owner, String title, String message)
	{
		VBox vBox = new VBox();
		vBox.setAlignment(Pos.CENTER);
		vBox.setSpacing(10);
		vBox.setPadding(new Insets(10));

		Scene scene = UserInterfaceUtil.getScene(vBox, 320, 100);

		final Dialog dialog = new Dialog(title, owner, scene);

		final TextField textInput = new TextField();

		textInput.setOnKeyPressed(new EventHandler<KeyEvent>()
		{

			@Override
			public void handle(javafx.scene.input.KeyEvent event)
			{
				if (KeyCode.ENTER.equals(event.getCode()))
				{
					Dialogs.textInput = textInput.getText();
					dialog.close();
					response = Response.YES;
				}
			}

		});

		Button btnOK = new Button("OK");

		btnOK.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				Dialogs.textInput = textInput.getText();
				dialog.close();
				response = Response.YES;
			}
		});

		Button btnCancel = new Button("Cancel");

		btnCancel.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				dialog.close();
				response = Response.NO;
			}
		});

		HBox hBox = new HBox();
		hBox.setAlignment(Pos.CENTER);
		hBox.setSpacing(10);
		hBox.setPadding(new Insets(10));

		hBox.getChildren().addAll(btnOK, btnCancel);

		if (message != null && message.length() > 0)
		{
			vBox.getChildren().add(new Label(message));
		}

		vBox.getChildren().add(textInput);
		vBox.getChildren().add(hBox);

		dialog.showDialog();

		textInput.requestFocus();

		return response;
	}

	public static void showOKDialog(Stage owner, String title, String message)
	{
		VBox vBox = new VBox();
		vBox.setSpacing(10);
		vBox.setAlignment(Pos.CENTER);
		vBox.setPadding(new Insets(10));

		String[] lines = message.split(S_NEWLINE);

		for (String line : lines)
		{
			Label label = new Label(line);
			vBox.getChildren().add(label);
		}

		int width = 640;
		
		if (lines.length == 1)
		{
			width = Math.max(320, message == null ? 0 : message.length() * 10);
		}
		
		Scene scene = UserInterfaceUtil.getScene(vBox, width, 60 + 20 * lines.length);

		final Dialog dialog = new Dialog(title, owner, scene);

		Button btnOK = new Button("OK");

		btnOK.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent e)
			{
				dialog.close();
			}
		});

		vBox.getChildren().add(btnOK);

		dialog.showDialog();
	}
}