package com.app.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;

@WebServlet("/GenerateTableQuery")
@MultipartConfig(location = "E:\\", // my disk
		fileSizeThreshold = 1024 * 1024 * 1, // 1 MB, The file size in bytes after which the file will be temporarily
												// stored on disk. The default size is 0 bytes.
		maxFileSize = 1024 * 1024 * 2, // 2 MB, The maximum size allowed for uploaded files, in bytes. If the size of
										// any uploaded file is greater than this size, the web container will throw an
										// exception (IllegalStateException). The default size is unlimited.
		maxRequestSize = 1024 * 1024 * 2 * 3 // 6 MB, The maximum size allowed for a multipart/form-data request, in
												// bytes. The web container will throw an exception if the overall size
												// of all uploaded files exceeds this threshold. The default size is
												// unlimited.
)
public class GenerateTableQuery extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		JSONObject jsonResult = new JSONObject();
		try {
			Part filePart = request.getPart("file");
			String fileName = filePart.getSubmittedFileName();
			String pathPrefixHeader = request.getHeader("pathPrefix");
			InputStream fileContent = filePart.getInputStream();
			Files.copy(fileContent, Paths.get(pathPrefixHeader + fileName));
			fileContent.close();
			String resultStatement = processSheet(fileName, pathPrefixHeader);
			jsonResult.put("status", "success");
			jsonResult.put("result", resultStatement);
		} catch (Exception e) {
			jsonResult.put("status", "error");
			jsonResult.put("message", e.getMessage());
		} finally {
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().print(jsonResult);
			response.getWriter().flush();
		}
	}

	private String processSheet(String fileName, String pathPrefix) throws InvalidFormatException, IOException {
		File excelFile = new File(pathPrefix + fileName);
		XSSFWorkbook workBook = new XSSFWorkbook(excelFile);
		XSSFSheet sheet = workBook.getSheetAt(0);
		HashMap<String, Object> columnNames = getColumnNames(sheet);
		workBook.close(); // workBook is not longer needed
		String fileNameWithoutExtension = fileName.substring(0, fileName.indexOf('.'));
		if (excelFile.exists()) {
			excelFile.delete();
		}
		return getCreateTableStatement(columnNames, fileNameWithoutExtension);
	}

	private String getCreateTableStatement(HashMap<String, Object> hashMap, String tableName) {
		final String CHARACTERCOLUMN = " CHARACTER VARYING(30) NOT NULL DEFAULT ''";
		final String NUMERICCOLUMN = " NUMERIC(10, 2) NOT NULL DEFAULT 0";
		final String BOOLEANCOLUMN = " CHARACTER(1) NOT NULL DEFAULT 'Y'";
		StringBuilder SQLCREATETABLE = new StringBuilder(
				"CREATE TABLE IF NOT EXISTS " + tableName + "(" + tableName + "_id SERIAL PRIMARY KEY, ");
		for (Map.Entry<String, Object> set : hashMap.entrySet()) {
			String setKeyName = set.getKey();
			Object setValueType = set.getValue();
			if (setValueType.equals(CellType.STRING)) {
				SQLCREATETABLE.append(setKeyName + CHARACTERCOLUMN + ", ");
			} else if (setValueType.equals(CellType.NUMERIC)) {
				SQLCREATETABLE.append(setKeyName + NUMERICCOLUMN + ", ");
			} else if (setValueType.equals(CellType.BOOLEAN)) {
				SQLCREATETABLE.append(setKeyName + BOOLEANCOLUMN + ", ");
			} else {
				System.out.println("Invalid value of HashMap: " + setValueType + " with key: " + setKeyName);
			}
		}
		int statementLength = SQLCREATETABLE.length();
		SQLCREATETABLE.delete(statementLength - 2, statementLength); // to remove the final subsequence ', '
		SQLCREATETABLE.append(")");
		return SQLCREATETABLE.toString();
	}

	private HashMap<String, Object> getColumnNames(XSSFSheet sheet) {
		HashMap<String, Object> hashMapReturn = new HashMap<String, Object>();
		Row firstRow = sheet.getRow(0);
		Row secondRow = sheet.getRow(1);
		Iterator<Cell> firstRowCellItr = firstRow.cellIterator();
		Iterator<Cell> secondRowCellItr = secondRow.cellIterator();
		while (firstRowCellItr.hasNext() && secondRowCellItr.hasNext()) {
			Cell firstRowCells = firstRowCellItr.next();
			Cell secondRowCells = secondRowCellItr.next();
			CellType type = secondRowCells.getCellType();
			if (type.equals(CellType.STRING) || type.equals(CellType.BOOLEAN) || type.equals(CellType.NUMERIC)) {
				hashMapReturn.put(firstRowCells.getStringCellValue().replace(' ', '_'), type);
			} else {
				System.out.println("Skipped column mapping at column: " + firstRowCells.getStringCellValue());
			}
		}
		return hashMapReturn;
	}
}