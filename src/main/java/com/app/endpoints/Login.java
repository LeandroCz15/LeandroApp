package com.app.endpoints;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.app.connection.Application;
import com.app.utils.Credentials;
import com.app.utils.CredentialsUtils;

@WebServlet("/Login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		JSONObject jsonResult = new JSONObject();
		try {
			// Validations of credentials (not at database level)
			Credentials credentials = CredentialsUtils.checkAuthHttpErrors(request);
			// Validations of credentials (at database level)
			PreparedStatement st = Application.getInstance().getConnection().prepareStatement(
					"SELECT users_level FROM users WHERE users_email = ? AND users_password = ? LIMIT 1");
			st.setString(1, credentials.getEmail());
			st.setString(2, credentials.getPassword());
			ResultSet rs = st.executeQuery();
			if (!rs.next()) {
				jsonResult.put("level", -1);
			} else {
				jsonResult.put("level", rs.getInt("users_level"));
			}
			st.close();
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

}