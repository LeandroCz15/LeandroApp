package com.app.utils;

public class Credentials {
	String email;
	String password;

	public Credentials(String ema, String pass) {
		email = ema;
		password = pass;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String user) {
		email = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String pass) {
		password = pass;
	}

}
