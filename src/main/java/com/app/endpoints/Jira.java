package com.app.endpoints;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.app.utils.ConvertBodyToJson;
import com.app.utils.Credentials;
import com.app.utils.CredentialsUtils;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@WebServlet("/Jira")
public class Jira extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		JSONObject jsonResult = new JSONObject();
		try {
			StringBuilder urlToPost = new StringBuilder("https://");
			Credentials credentials = CredentialsUtils.checkAuthHttpErrors(request);
			JSONObject incomingRequestBody = ConvertBodyToJson.convertBodyToJson(request.getReader());
			urlToPost.append(incomingRequestBody.getString("url")).append("/rest/api/3/issue/")
					.append(incomingRequestBody.getString("issue")).append("/worklog");
			OkHttpClient client = new OkHttpClient();
			JSONObject jsonForJira = createJsonForJira(incomingRequestBody);
			MediaType JSON = MediaType.get("application/json; charset=utf-8");
			RequestBody postRequestBody = RequestBody.create(jsonForJira.toString(), JSON);
			String jiraCredentials = credentials.getEmail() + ":" + credentials.getPassword();
			String encodedCredentials = Base64.getEncoder()
					.encodeToString(jiraCredentials.getBytes(StandardCharsets.UTF_8));
			Request postRequest = new Request.Builder().url(urlToPost.toString()).post(postRequestBody)
					.addHeader("Authorization", "Basic " + encodedCredentials).build();
			Response postResponse = client.newCall(postRequest).execute();
			jsonResult.put("status", postResponse.code() == 201 ? "success" : "error");
			jsonResult.put("message", postResponse.body().toString());
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

	private JSONObject createJsonForJira(JSONObject body) {
		JSONObject json = new JSONObject();
		json.put("timeSpentSeconds", body.getInt("seconds"));
		JSONObject commentJson = new JSONObject();
		commentJson.put("type", "doc");
		commentJson.put("version", 1);
		JSONArray contentJson = new JSONArray();
		JSONObject singleContent = new JSONObject();
		singleContent.put("type", "paragraph");
		JSONArray contentJson2 = new JSONArray();
		JSONObject singleContent2 = new JSONObject();
		singleContent2.put("text", body.getString("text"));
		singleContent2.put("type", "text");
		contentJson2.put(singleContent2);
		singleContent.put("content", contentJson2);
		contentJson.put(singleContent);
		commentJson.put("content", contentJson);
		json.put("comment", commentJson);
		return json;
	}

}