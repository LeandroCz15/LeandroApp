package com.app.query;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.app.connection.Application;
import com.app.utils.Credentials;
import com.app.utils.CredentialsUtils;

@WebServlet("/Query")
public class Query extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		JSONObject jsonResult = new JSONObject();
		try {
			// Validations of credentials (not at database level)
			Credentials credentials = CredentialsUtils.checkAuthHttpErrors(request);
			String tableName = request.getParameter("tableName");
			// Validation of table name parameter
			if (StringUtils.isBlank(tableName)) {
				throw new ParameterException("Table name must be valid");
			}
			// Validations of credentials (at database level)
			CredentialsUtils.checkAccessAndCredentials(credentials.getEmail(), credentials.getPassword(), tableName);
			PreparedStatement st = Application.getInstance().getConnection()
					.prepareStatement("SELECT * FROM " + request.getParameter("tableName") + " LIMIT 1000");
			ResultSet rs = st.executeQuery();
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
			st.close();
			jsonResult.put("status", "success");
			jsonResult.put("result", jsonArray);
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

	private List<String> getColumnNames(ResultSetMetaData rsMetaData) throws SQLException {
		List<String> nameColumns = new ArrayList<String>();
		int numColumns = rsMetaData.getColumnCount();
		for (int i = 0; i < numColumns; i++) {
			nameColumns.add(rsMetaData.getColumnName(i + 1));
		}
		return nameColumns;
	}
}
