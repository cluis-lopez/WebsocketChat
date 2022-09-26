package com.clopez.chat;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.clopez.datamgnt.DatabaseHook;
import com.clopez.datamgnt.DatabaseHookException;
import com.clopez.datamgnt.User;
import com.clopez.datamgnt.User.Chat;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

@WebServlet("/Userdata")
public class UserData extends HttpServlet {

	private static DatabaseHook udb = new DatabaseHook("UserDatabase");
	private static DatabaseHook gdb = new DatabaseHook("GroupDatabase");
	Gson gson = new Gson();

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String userid = req.getParameter("id");
		String token = req.getParameter("token");
		String command = req.getParameter("command");
		String searchChats = req.getParameter("searchChats");
		String searchUsers = req.getParameter("searchUsers");
		JsonObject job = new JsonObject();

		try {
			JsonObject jo = udb.request("findUserById", userid);
			User user = gson.fromJson(jo.get("user"), User.class);

			if (user != null && token != null && token.equals(user.getToken()) && user.isTokenValid()
					&& command != null) {
				job.addProperty("code", "OK");
				int maxNumber = 5;
				try {
					maxNumber = Integer.parseInt(req.getParameter("maxNumber"));
					if (maxNumber > 25)
						maxNumber = 25;
				} catch (NumberFormatException e) {
					System.out
							.println("Parámetro inválido o no especificado. Se decuelven " + maxNumber + " resultados");
				}

				if (command.equals("lastChats")) {
					JsonArray jarr = new JsonArray();
					Chat[] temp = user.getRecentChats();
					int min = (maxNumber < temp.length ? maxNumber : temp.length);
					for (int i = 0; i < min; i++) {
						JsonObject j = new JsonObject();
						j.addProperty("name", temp[i].getName());
						j.addProperty("isUser", temp[i].getIsUser());
						jarr.add(j);
					}
					job.add("chatList", jarr);
				} else if (command.equals("searchChats") && searchChats != null && !searchChats.equals("")) {
					JsonArray jar = new JsonArray();

					jo = udb.request("findUserByWildChar", searchChats);
					jo.get("users").getAsJsonArray().forEach((temp) -> {
						JsonObject chat = new JsonObject();
						chat.addProperty("name", temp.getAsJsonObject().get("name").getAsString());
						chat.addProperty("isUser", true);
						chat.addProperty("isConnected", false);
						jar.add(chat);
					});

					jo = gdb.request("findGroupByWildChar", searchChats);
					jo.get("groups").getAsJsonArray().forEach((temp) -> {
						JsonObject chat = new JsonObject();
						chat.addProperty("name", temp.getAsJsonObject().get("name").getAsString());
						chat.addProperty("isUser", false);
						chat.addProperty("isConnected", false);
						jar.add(chat);
					});

					System.out.println("Encontrados " + jar.size() + " usuarios/grupos: " + jar.toString());

					int min = (maxNumber < jar.size() ? maxNumber : jar.size());
					JsonArray jarr = new JsonArray();
					for (int i = 0; i < min; i++)
						jarr.add(jar.get(i));
					job.add("chatList", jarr);
				} else if (command.equals("searchUsers") && searchUsers != null && !searchUsers.equals("")) {
					jo = udb.request("findUserByWildChar", searchUsers);
					JsonArray jarr1 = jo.get("users").getAsJsonArray();
					JsonArray jarr2 = new JsonArray();
					int min = (maxNumber < jarr1.size() ? maxNumber : jarr1.size());
					for (int i = 0; i < min; i++)
						jarr2.add(jarr1.get(i).getAsJsonObject().get("name").getAsString());
					job.add("users", jarr2);
				} else {
					job.addProperty("code", "Invalid command");
				}
			} else {
				job.addProperty("code", "Invalid user or token");
			}
		} catch (DatabaseHookException e) {
			job.addProperty("code", e.getMessage());
		} catch (JsonSyntaxException e) {
			job.addProperty("code", "Invalid JsonObject received from database");
		} catch (NullPointerException e) {
			job.addProperty("code", "Null Pointer... ¿Invalid parameters?");
		}

		resp.setContentType("application/json");
		PrintWriter pw = resp.getWriter();
		pw.println(job);
		pw.close();
	}

}
