package com.clopez.chat;

import java.io.IOException;
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

@WebServlet("/Login")
public class Login extends HttpServlet {

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		DatabaseHook udb = new DatabaseHook("UserDatabase");
		Gson gson = new Gson();

		String user = req.getParameter("user");
		String password = req.getParameter("password");
		Map<String, String> response = new HashMap<String, String>();
		response.put("code", "OK");
		response.put("user", "");
		response.put("id", "");
		response.put("token", "");

		try {
			JsonObject jo = udb.request("findUserByName", user);
			User u = gson.fromJson(jo.get("user"), User.class);

			if (u != null)
				if (u.passwordMatch(password)) {
					response.put("user", u.getName());
					response.put("id", u.getId());
					if ( ! u.isTokenValid()) { //Token Expired, regenerate
						System.out.println("Updating user data for : " + u.getName());
						u.generateToken(30);
						udb.request("updateUser", gson.toJson(u));
					}
					response.put("token", u.getToken());
				} else {
					response.put("code", "Invalid Password");
				}
			else {
				response.put("code", "Invalid User");
			}
		} catch (DatabaseHookException e) {
			response.put("code", e.getMessage());
		} catch (JsonSyntaxException e) {
			response.put("code", "Invalid JsonObject received from database");
		} catch (NullPointerException e) {
			response.put("code", "Null Pointer... ¿Invalid parameters?");
		}

		// Renew token life
		
		System.out.println("Respuesta login:  " + gson.toJson(response));
		resp.setContentType("application/json;charset=UTF-8");
		resp.setHeader("cache-control", "no-cache");
		resp.getWriter().write(gson.toJson(response));
		resp.flushBuffer();
	}
}
