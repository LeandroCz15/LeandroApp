package com.app.query;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import com.app.connection.Application;

@WebServlet("/Query")
public class Query extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		JSONObject jsonResult = new JSONObject();
		try {
			checkErrors(request);
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

	private void checkErrors(HttpServletRequest request)
			throws ParameterException, UnsupportedEncodingException, SQLException {
		String authHeader = request.getHeader("Authorization");
		if (StringUtils.isBlank(authHeader)) {
			throw new ParameterException("No authorization provided");
		}
		String tableName = request.getParameter("tableName");
		if (StringUtils.isBlank(tableName)) {
			throw new ParameterException("Table name must be valid");
		}
		StringTokenizer st = new StringTokenizer(authHeader);
		if (st.hasMoreTokens()) {
			String basic = st.nextToken();
			if (!StringUtils.equals("Basic", basic)) {
				throw new ParameterException("Please provid basic authorization");
			}
			String credentials = new String(Base64.decodeBase64(st.nextToken()), "UTF-8");
			int separatorIndex = credentials.indexOf(":");
			if (separatorIndex == -1) {
				throw new ParameterException("Error in authorization format");
			}
			checkCredentials(credentials.substring(0, separatorIndex).trim(),
					credentials.substring(separatorIndex + 1).trim(), tableName);
		}
	}

	private void checkCredentials(String user, String password, String tableName)
			throws ParameterException, SQLException {
		PreparedStatement st = Application.getInstance().getConnection()
				.prepareStatement("SELECT * FROM users WHERE users_name = ? AND users_password = ? LIMIT 1");
		st.setString(1, user);
		st.setString(2, password);
		ResultSet rs = st.executeQuery();
		if (!rs.next()) {
			throw new ParameterException("Credentials invalid");
		}
		int userLevel = rs.getInt("users_level");
		st = Application.getInstance().getConnection()
				.prepareStatement("SELECT * FROM table_access WHERE table_name = ? LIMIT 1");
		st.setString(1, tableName);
		rs = st.executeQuery();
		boolean emptyResult = !rs.next();
		if (emptyResult || rs.getInt("level_required") > userLevel) {
			throw new ParameterException("This user dont have access to the specified table");
		}
	}
}
