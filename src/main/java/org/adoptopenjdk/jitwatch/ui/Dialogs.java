package org.adoptopenjdk.jitwatch.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import static org.adoptopenjdk.jitwatch.core.JITWatchConstants.*;

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

		Scene scene = new Scene(vBox, 640, 80);

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

		Scene scene = new Scene(vBox, 640, 60 + 20 * lines.length);

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