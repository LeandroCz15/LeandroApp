package com.app.connection;

public class Application {
	private static DatabaseConnection instance = null;

	public static DatabaseConnection getInstance() {
		if (instance == null) {
			instance = createInstance();
		}
		return instance;
	}

	private static DatabaseConnection createInstance() {
		DatabaseConnection conn = null;
		try {
			conn = new DatabaseConnection(ConnectionProperties.HOST, ConnectionProperties.PORT,
					ConnectionProperties.NAME, ConnectionProperties.USER, ConnectionProperties.PASSWORD);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}
}
