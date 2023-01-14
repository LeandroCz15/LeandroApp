package com.app.utils;

import java.io.BufferedReader;
import java.io.IOException;

import org.json.JSONObject;

public interface ConvertBodyToJson {

	public static JSONObject convertBodyToJson(BufferedReader br) throws IOException {
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
