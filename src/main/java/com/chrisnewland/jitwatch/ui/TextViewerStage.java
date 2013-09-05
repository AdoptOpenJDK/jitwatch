package com.chrisnewland.jitwatch.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class TextViewerStage extends Stage
{
	private TextArea textArea;
	private String[] lines;

	public TextViewerStage(final JITWatchUI parent, String title, String source, boolean showLineNumbers)
	{
		initStyle(StageStyle.DECORATED);

		setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent arg0)
			{
				parent.handleStageClosed(TextViewerStage.this);
			}
		});

		if (source == null)
		{
			source = "Empty";
		}

		source = source.replace("\t", "    "); // 4 spaces

		StringBuilder builder = new StringBuilder();

		lines = source.split("\n");

		int max = 0;

		for (int i = 0; i < lines.length; i++)
		{
			String row = lines[i];

			if (showLineNumbers)
			{
				builder.append(i + 1).append(' ');
			}

			builder.append(row).append("\n");

			int rowLen = row.length();

			if (rowLen > max)
			{
				max = rowLen;
			}
		}

		int x = Math.min(80, max);
		int y = Math.min(30, lines.length);

		x = Math.max(x, 20);
		y = Math.max(y, 20);

		VBox vbox = new VBox();

		textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setText(builder.toString());
		textArea.setStyle("-fx-font-family:monospace;");

		vbox.setPadding(new Insets(4));

		vbox.getChildren().add(textArea);
		VBox.setVgrow(textArea, Priority.ALWAYS);

		setTitle(title);

		Scene scene = new Scene(vbox, x * 12, y * 19);

		setScene(scene);
	}

	//TODO highlight method text
	// http://stackoverflow.com/questions/17456716/how-to-highlight-a-row-if-it-contains-specific-text-in-javafx?rq=1
	public void jumpTo(final String regex)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				int pos = 0;

				for (String line : lines)
				{
					Matcher matcher = Pattern.compile(regex).matcher(line);
					if (matcher.find())
					{
						System.out.println("matched sig on line " + pos);
						break;
					}

					pos++;
				}

				// HORRIBLE HACK
				Text text = new Text("XYZZY");
				text.snapshot(null, null);
				double height = text.getLayoutBounds().getHeight();

				double yPos = pos * height;

				textArea.setScrollTop(yPos);
			}
		});
	}
}