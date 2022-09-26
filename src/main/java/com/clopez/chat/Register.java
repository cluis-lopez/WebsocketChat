package com.clopez.chat;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.clopez.datamgnt.DatabaseHook;
import com.clopez.datamgnt.DatabaseHookException;
import com.clopez.datamgnt.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

@WebServlet("/Register")
public class Register extends HttpServlet {

	DatabaseHook udb = new DatabaseHook("UserDatabase");
	Gson gson = new Gson();

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Map<String, String> response = new HashMap<>();
		response.put("code", "OK");
		response.put("user", "");
		response.put("id", "");

		String user = req.getParameter("user");
		String password = req.getParameter("password");

		try {
			JsonObject jo = udb.request("findUserByName", user);
		} catch (DatabaseHookException e) {
			if (e.getMessage().equals("User not found") && isValidPassword(password)) {
				User u = new User(user, password);
				try {
					udb.request("createUser", gson.toJson(u));
					response.put("user", u.getName());
					response.put("id", u.getId());
				} catch (DatabaseHookException e1) {
					response.put("code", "Cannot add user to the database " + e1.getMessage());
				}

			} else 
				response.put("code", e.getMessage());
		} catch (JsonSyntaxException e) {
			response.put("code", "Invalid JsonObject received from database");
		} catch (NullPointerException e) {
			response.put("code", "Null Pointer... ¿Invalid parameters?");
		}

		resp.setContentType("application/json");
		PrintWriter pw = resp.getWriter();
		pw.println(gson.toJson(response));
		pw.close();
	}

	public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Map<String, String> response = new HashMap<>();
		response.put("code", "OK");
		response.put("user", "");
		response.put("id", "");

		String user = req.getParameter("user");
		String token = req.getParameter("token");

		try {
			JsonObject jo = udb.request("findUserByName", user);
			User u = gson.fromJson(jo.get("user"), User.class);

			if (u != null && u.isTokenValid() && u.getToken().equals(token)) {
				// El usuario existe y el token es válido
				try {
					response.put("user", u.getName());
					response.put("id", u.getId());
					udb.request("deleteUser", gson.toJson(u));
				} catch (IllegalArgumentException e) {
					response.put("code", e.getMessage());
				}

			} else {
				response.put("code", "Invalid User or Password");
			}
		} catch (DatabaseHookException e) {
			response.put("code", e.getMessage());
		} catch (JsonSyntaxException e) {
			response.put("code", "Invalid JsonObject received from database");
		}

		resp.setContentType("application/json");
		PrintWriter pw = resp.getWriter();
		pw.println(gson.toJson(response));
		pw.close();
	}

	private boolean isValidPassword(String password) {
		if (password != null && password.length() >= 4)
			return true;
		else
			return false;
	}
}
