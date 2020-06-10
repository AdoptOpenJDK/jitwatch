/*
 * Copyright (c) 2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.ui.report.locks;

import org.adoptopenjdk.jitwatch.report.Report;
import org.adoptopenjdk.jitwatch.ui.report.IReportRowBean;
import org.adoptopenjdk.jitwatch.ui.report.cell.LinkedBCICell;
import org.adoptopenjdk.jitwatch.ui.report.cell.TextTableCell;

import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public final class OptimisedLockRowBuilder
{
	private OptimisedLockRowBuilder()
	{
	}

	public static TableView<IReportRowBean> buildTable(ObservableList<IReportRowBean> rows)
	{
		TableView<IReportRowBean> tv = new TableView<>();

		TableColumn<IReportRowBean, String> metaClass = new TableColumn<>("Class");
		metaClass.setCellValueFactory(new PropertyValueFactory<>("metaClass"));
		metaClass.setCellFactory(new Callback<TableColumn<IReportRowBean, String>, TableCell<IReportRowBean, String>>()
		{
			@Override
			public TableCell<IReportRowBean, String> call(TableColumn<IReportRowBean, String> col)
			{
				return new TextTableCell();
			}
		});

		TableColumn<IReportRowBean, String> member = new TableColumn<>("Member");
		member.setCellValueFactory(new PropertyValueFactory<>("member"));
		member.setCellFactory(new Callback<TableColumn<IReportRowBean, String>, TableCell<IReportRowBean, String>>()
		{
			@Override
			public TableCell<IReportRowBean, String> call(TableColumn<IReportRowBean, String> col)
			{
				return new TextTableCell();
			}
		});

		TableColumn<IReportRowBean, String> compilation = new TableColumn<>("Compilation");
		compilation.setCellValueFactory(new PropertyValueFactory<>("compilation"));
		compilation.setCellFactory(new Callback<TableColumn<IReportRowBean, String>, TableCell<IReportRowBean, String>>()
		{
			@Override
			public TableCell<IReportRowBean, String> call(TableColumn<IReportRowBean, String> col)
			{
				return new TextTableCell();
			}
		});

		TableColumn<IReportRowBean, Report> viewBCI = new TableColumn<>("BCI");
		viewBCI.setCellValueFactory(new PropertyValueFactory<>("report"));
		viewBCI.setCellFactory(new Callback<TableColumn<IReportRowBean, Report>, TableCell<IReportRowBean, Report>>()
		{
			@Override
			public TableCell<IReportRowBean, Report> call(TableColumn<IReportRowBean, Report> col)
			{
				return new LinkedBCICell();
			}
		});
		
		TableColumn<IReportRowBean, String> directOrInline = new TableColumn<>("How");
		directOrInline.setCellValueFactory(new PropertyValueFactory<>("kind"));
		directOrInline.setCellFactory(new Callback<TableColumn<IReportRowBean, String>, TableCell<IReportRowBean, String>>()
		{
			@Override
			public TableCell<IReportRowBean, String> call(TableColumn<IReportRowBean, String> col)
			{
				return new TextTableCell();
			}
		});

		TableColumn<IReportRowBean, String> optimisationKind = new TableColumn<>("Optimisation Kind");
		optimisationKind.setCellValueFactory(new PropertyValueFactory<>("optimisationKind"));
		optimisationKind.setCellFactory(new Callback<TableColumn<IReportRowBean, String>, TableCell<IReportRowBean, String>>()
		{
			@Override
			public TableCell<IReportRowBean, String> call(TableColumn<IReportRowBean, String> col)
			{
				return new TextTableCell();
			}
		});

		metaClass.prefWidthProperty().bind(tv.widthProperty().multiply(0.2));
		member.prefWidthProperty().bind(tv.widthProperty().multiply(0.2));
		compilation.prefWidthProperty().bind(tv.widthProperty().multiply(0.2));
		viewBCI.prefWidthProperty().bind(tv.widthProperty().multiply(0.12));
		directOrInline.prefWidthProperty().bind(tv.widthProperty().multiply(0.1));
		optimisationKind.prefWidthProperty().bind(tv.widthProperty().multiply(0.18));

		tv.getColumns().add(metaClass);
		tv.getColumns().add(member);
		tv.getColumns().add(compilation);
		tv.getColumns().add(viewBCI);
		tv.getColumns().add(directOrInline);
		tv.getColumns().add(optimisationKind);

		tv.setItems(rows);

		return tv;
	}
}
