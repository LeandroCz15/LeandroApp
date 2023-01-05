package com.app.utils;

public class Credentials {
	String userName;
	String password;

	public Credentials(String user, String pass) {
		userName = user;
		password = pass;
	}

	public String getUser() {
		return userName;
	}

	public void setUser(String user) {
		userName = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String pass) {
		password = pass;
	}

}
