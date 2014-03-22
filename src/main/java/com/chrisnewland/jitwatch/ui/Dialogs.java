package com.chrisnewland.jitwatch.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

public class Dialogs
{
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

	public static Response showYesNoDialog(Stage owner, String message, String title)
	{
		VBox vBox = new VBox();
		
		Scene scene = new Scene(vBox, 320, 200);
		
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
}