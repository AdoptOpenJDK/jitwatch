package com.chrisnewland.jitwatch.ui;

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
import static com.chrisnewland.jitwatch.core.JITWatchConstants.*;

public final class Dialogs
{
    private static final int SCENE_WIDTH = 640;
    private static final int SCENE_HEIGHT = 320;
    private static final int TEN_FOR_TOP_RIGHT_BOTTOM_LEFT = 10;
    private static final int SIXTY_FROM_TOP = 60;
    private static final int LINE_HEIGHT = 20;
    private static final int TEN_SPACES = 10;

    /*
       Hide Utility Class Constructor Utility classes should not have a public
       or default constructor.
    */
	private Dialogs()
	{
	}

	public enum Response
	{
		NO, YES
	}

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

	public static Response showYesNoDialog(Stage owner, String message, String title)
	{
		VBox vBox = new VBox();

		Scene scene = new Scene(vBox, SCENE_WIDTH, SCENE_HEIGHT);

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

		HBox buttons = new HBox();

		buttons.setAlignment(Pos.CENTER);

		buttons.getChildren().addAll(btnYes, btnNo);

		bp.setCenter(buttons);

		vBox.getChildren().addAll(new Label(message), bp);

		dialog.showDialog();

		return response;
	}

	public static void showOKDialog(Stage owner, String title, String message)
	{
		VBox vBox = new VBox();
		vBox.setSpacing(TEN_SPACES);
		vBox.setAlignment(Pos.CENTER);
		vBox.setPadding(new Insets(TEN_FOR_TOP_RIGHT_BOTTOM_LEFT));

		String[] lines = message.split(S_NEWLINE);

		for (String line : lines)
		{
			Label label = new Label(line);
			vBox.getChildren().add(label);
		}

		Scene scene = new Scene(vBox, SCENE_WIDTH, SIXTY_FROM_TOP + LINE_HEIGHT * lines.length);

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