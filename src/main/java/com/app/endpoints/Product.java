package com.app.endpoints;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.app.connection.Application;
import com.app.utils.Credentials;
import com.app.utils.CredentialsUtils;

@WebServlet("/Product")
public class Product extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String PRODUCT_TABLE_NAME = "products";
	private static final String IMAGES_TABLE_NAME = "images";

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		JSONObject jsonResult = new JSONObject();
		try {
			Application.getInstance().getConnection().setAutoCommit(false);
			Credentials credentials = CredentialsUtils.checkErrors(request);
			CredentialsUtils.checkAccessAndCredentials(credentials.getUser(), credentials.getPassword(),
					PRODUCT_TABLE_NAME);
			JSONObject body = convertBodyToJson(request.getReader());
			String productCreatedId = insertProduct(body);
			JSONArray imagesArray = body.getJSONArray("imagesArray");
			for (int i = 0; i < imagesArray.length(); i++) {
				JSONObject image = imagesArray.getJSONObject(i);
				insertImage(image.getString("imageBytes"), productCreatedId);
			}
			Application.getInstance().getConnection().commit();
			jsonResult.put("status", "success");
		} catch (Exception e) {
			String errorMsg = e.getMessage();
			try {
				Application.getInstance().getConnection().rollback();
			} catch (SQLException f) {
				errorMsg += f.getMessage();
			}
			jsonResult.put("status", "error");
			jsonResult.put("message", errorMsg);
		} finally {
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().print(jsonResult);
			response.getWriter().flush();
		}
	}

	private String insertProduct(JSONObject json) throws SQLException {
		PreparedStatement st = Application.getInstance().getConnection().prepareStatement("INSERT INTO "
				+ PRODUCT_TABLE_NAME + " (products_name, products_price) VALUES (?, ?) RETURNING products_id");
		st.setString(1, json.getString("name"));
		st.setBigDecimal(2, json.getBigDecimal("price"));
		st.execute(); // optionaly we could check the form of the result set
		ResultSet rs = st.getResultSet();
		rs.next();
		return rs.getString("products_id");
	}

	private void insertImage(String imageBytes, String productId) throws SQLException {
		PreparedStatement st = Application.getInstance().getConnection()
				.prepareStatement("INSERT INTO " + IMAGES_TABLE_NAME + " (products_id, image_bytea) VALUES (?, ?)");
		st.setString(1, productId);
		st.setBytes(2, imageBytes.getBytes());
		st.executeUpdate();
	}

	private JSONObject convertBodyToJson(BufferedReader br) throws IOException {
		StringBuilder builder = new StringBuilder();
		String line = br.readLine();
		while (line != null) {
			builder.append(line);
			line = br.readLine();
		}
		String data = builder.toString();
		return new JSONObject(data);
	}

}