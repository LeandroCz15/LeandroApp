package com.app.endpoints;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.app.connection.Application;
import com.app.query.ParameterException;
import com.app.utils.ConvertBodyToJson;
import com.app.utils.Credentials;
import com.app.utils.CredentialsUtils;
import com.app.utils.ImageKitUtils;

import io.imagekit.sdk.ImageKit;
import io.imagekit.sdk.config.Configuration;
import io.imagekit.sdk.exceptions.BadRequestException;
import io.imagekit.sdk.exceptions.ForbiddenException;
import io.imagekit.sdk.exceptions.InternalServerException;
import io.imagekit.sdk.exceptions.TooManyRequestsException;
import io.imagekit.sdk.exceptions.UnauthorizedException;
import io.imagekit.sdk.exceptions.UnknownException;
import io.imagekit.sdk.models.FileCreateRequest;
import io.imagekit.sdk.models.results.Result;

@WebServlet("/Product")
public class Product extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String PRODUCT_TABLE_NAME = "products";
	private static final String JPG_EXTENSION = ".jpg";

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		JSONObject jsonResult = new JSONObject();
		try {
			Application.getInstance().getConnection().setAutoCommit(false);
			Credentials credentials = CredentialsUtils.checkAuthHttpErrors(request);
			accesForPostingProducts(credentials.getEmail(), credentials.getPassword());
			JSONObject body = ConvertBodyToJson.convertBodyToJson(request.getReader());
			insertProduct(body);
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

	private void insertProduct(JSONObject json) throws Exception {
		Result imageUploadResult = uploadImage(json.getString("imgBase64"), json.getString("sku"));
		PreparedStatement insertStmt = Application.getInstance().getConnection()
				.prepareStatement("INSERT INTO " + PRODUCT_TABLE_NAME
						+ " (products_name, products_price, products_sku, products_description, products_image_url)"
						+ " VALUES (?, ?, ?, ?, ?)");
		insertStmt.setString(1, json.getString("name"));
		insertStmt.setBigDecimal(2, json.getBigDecimal("price"));
		insertStmt.setString(3, json.getString("sku"));
		insertStmt.setString(4, json.getString("description"));
		insertStmt.setString(5, imageUploadResult.getUrl() + "?tr=w-450,h-300,px-pad_resize");
		insertStmt.executeUpdate();
	}

	private Result uploadImage(String imgBase64, String prodSku) throws InternalServerException, BadRequestException,
			UnknownException, ForbiddenException, TooManyRequestsException, UnauthorizedException {
		ImageKit imageKit = ImageKit.getInstance();
		Configuration config = new Configuration(ImageKitUtils.IMAGE_KIT_PUBLIC_KEY,
				ImageKitUtils.IMAGE_KIT_PRIVATE_KEY, ImageKitUtils.IMAGE_KIT_URL_PREFIX);
		imageKit.setConfig(config);
		FileCreateRequest fileCreateRequest = new FileCreateRequest(imgBase64, prodSku + JPG_EXTENSION);
		return ImageKit.getInstance().upload(fileCreateRequest);
	}

	private void accesForPostingProducts(String email, String password) throws SQLException, ParameterException {
		PreparedStatement st = Application.getInstance().getConnection()
				.prepareStatement("SELECT * FROM users WHERE users_email = ? AND users_password = ? LIMIT 1");
		st.setString(1, email);
		st.setString(2, password);
		ResultSet rs = st.executeQuery();
		if (!rs.next()) {
			throw new ParameterException("Invalid credentials");
		}
		if (rs.getInt("users_level") != 3) {
			throw new ParameterException("This user cant post products");
		}
	}

}