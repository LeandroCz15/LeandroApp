package com.app.connection;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {
	private static Connection connection = null;

	public DatabaseConnection(String host, String port, String name, String user, String password) {
		try {
			Class.forName("org.postgresql.Driver"); // load driver
			connection = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/" + name, user,
					password); // try to connect with your attributes
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Connection getConnection() {
		return connection;
	}
}
