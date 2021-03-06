package com.chatbot.adjustment.web.common.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public class ExcelWriteComponent {

	public static final String FILE_NAME = "fileName";
	public static final String HEAD = "head";
	public static final String BODY = "body";

	public static final String XLS = "xls";
	public static final String XLSX = "xlsx";
	public static final String XLSX_STREAM = "xlsx-stream";

	private Workbook workbook;
	private Map<String, Object> model;
	private HttpServletResponse response;

	public ExcelWriteComponent(Workbook workbook, Map<String, Object> model, HttpServletResponse response) {
		this.workbook = workbook;
		this.model = model;
		this.response = response;
	}

	public void create() {
		setFileName(response, mapToFileName());
		Sheet sheet = workbook.createSheet();
		createHead(sheet, mapToHeadList());
		createBody(sheet, mapToBodyList());
	}

	private String mapToFileName() {
		return (String) model.get(FILE_NAME);
	}

	private List<String> mapToHeadList() {
		return (List<String>) model.get(HEAD);
	}

	private List<List<String>> mapToBodyList() {
		return (List<List<String>>) model.get(BODY);
	}

	private void setFileName(HttpServletResponse response, String fileName) {
		response.setHeader("Content-Disposition",
				"attachment; filename=\"" + setFileExtension(fileName) + "\"");
	}

	private String setFileExtension(String fileName) {
		if ( workbook instanceof XSSFWorkbook) {
			fileName += ".xlsx";
		}
		if ( workbook instanceof SXSSFWorkbook) {
			fileName += ".xlsx";
		}
		if ( workbook instanceof HSSFWorkbook) {
			fileName += ".xls";
		}

		return fileName;
	}

	private void createHead(Sheet sheet, List<String> headList) {
		createRow(sheet, headList, 0);
	}

	private void createBody(Sheet sheet, List<List<String>> bodyList) {
		int rowSize = bodyList.size();
		for (int i = 0; i < rowSize; i++) {
			createRow(sheet, bodyList.get(i), i + 1);
		}
	}

	private void createRow(Sheet sheet, List<String> cellList, int rowNum) {
		int size = cellList.size();
		Row row = sheet.createRow(rowNum);

		for (int i = 0; i < size; i++) {
			row.createCell(i).setCellValue(cellList.get(i));
		}
	}

}
