package com.app.utils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public interface ConnectionUtils {

	public static JSONArray convertResultSetToJsonArray(ResultSet rs) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int numColumns = rsmd.getColumnCount();
		List<String> nameColumns = getColumnNames(rsmd);
		JSONArray jsonArray = new JSONArray();
		while (rs.next()) {
			JSONObject json = new JSONObject();
			// for each result row, we must travel trough nameColumns list in order to put
			// key values
			for (int i = 0; i < numColumns; i++) {
				json.put(nameColumns.get(i), rs.getObject(i + 1));
			}
			jsonArray.put(json);
		}
		return jsonArray;
	}

	private static List<String> getColumnNames(ResultSetMetaData rsMetaData) throws SQLException {
		List<String> nameColumns = new ArrayList<String>();
		int numColumns = rsMetaData.getColumnCount();
		for (int i = 0; i < numColumns; i++) {
			nameColumns.add(rsMetaData.getColumnName(i + 1));
		}
		return nameColumns;
	}

}
