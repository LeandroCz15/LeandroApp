package com.app.utils;

import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;

import com.app.connection.Application;
import com.app.query.ParameterException;

public interface CredentialsUtils {
	public static Credentials checkErrors(HttpServletRequest request)
			throws ParameterException, UnsupportedEncodingException, SQLException {
		String authHeader = request.getHeader("Authorization");
		if (StringUtils.isBlank(authHeader)) {
			throw new ParameterException("No authorization provided");
		}
		StringTokenizer st = new StringTokenizer(authHeader);
		if (st.hasMoreTokens()) {
			String basic = st.nextToken();
			if (!StringUtils.equals("Basic", basic)) {
				throw new ParameterException("Please provide basic authorization");
			}
			String credentials = new String(Base64.decodeBase64(st.nextToken()), "UTF-8");
			int separatorIndex = credentials.indexOf(":");
			if (separatorIndex == -1) {
				throw new ParameterException("Error in authorization format");
			}
			return new Credentials(credentials.substring(0, separatorIndex).trim(),
					credentials.substring(separatorIndex + 1).trim());
		}
		return null;
	}

	public static void checkAccessAndCredentials(String user, String password, String tableName)
			throws ParameterException, SQLException {
		PreparedStatement st = Application.getInstance().getConnection()
				.prepareStatement("SELECT * FROM users WHERE users_name = ? AND users_password = ? LIMIT 1");
		st.setString(1, user);
		st.setString(2, password);
		ResultSet rs = st.executeQuery();
		if (!rs.next()) {
			throw new ParameterException("Invalid credentials");
		}
		int userLevel = rs.getInt("users_level");
		st = Application.getInstance().getConnection()
				.prepareStatement("SELECT * FROM table_access WHERE table_name = ? LIMIT 1");
		st.setString(1, tableName);
		rs = st.executeQuery();
		boolean emptyResult = !rs.next();
		if (emptyResult || rs.getInt("level_required") > userLevel) {
			throw new ParameterException("This user dont have access to the required table/s by this process");
		}
	}
}
