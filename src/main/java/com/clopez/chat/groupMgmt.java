package com.clopez.chat;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.clopez.chat.datamgnt.DatabaseHook;
import com.clopez.chat.datamgnt.DatabaseHookException;
import com.clopez.chat.datamgnt.Group;
import com.clopez.chat.datamgnt.User;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

@WebServlet("/groupMgmt")
public class groupMgmt extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static DatabaseHook userdb = new DatabaseHook("UserDatabase");
	private static DatabaseHook groupdb = new DatabaseHook("GroupDatabase");

	/**
	 * Returns the list of groups of the invoking user (both as admin or as normal
	 * user)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		JsonObject job = new JsonObject();
		Gson gson = new Gson();
		User u;

		if (!isValidParamenters(req, "id", "token")) {
			job.addProperty("code", "Invalid API parameters");
		} else if ((u = isAuthorizedUser(req.getParameter("id"), req.getParameter("token"))) == null) {
			job.addProperty("code", "Invalid or unathorized user");
		} else {
			try {
			job.addProperty("code", "OK");
			JsonObject jo = groupdb.request("findByOwner",gson.toJson(u));
			JsonArray ownerOf = jo.get("groups").getAsJsonArray();
			JsonArray jarr = new JsonArray();
			for (JsonElement je : ownerOf)
				jarr.add(je.getAsJsonObject().get("name").getAsString());
			job.add("ownerOf", jarr);

			jo = groupdb.request("findByUser",gson.toJson(u));
			JsonArray memberOf = jo.get("groups").getAsJsonArray();
			JsonArray jarr2 = new JsonArray();
			for (JsonElement je : memberOf)
				jarr2.add(je.getAsJsonObject().get("name").getAsString());
			job.add("memberOf", jarr2);
			} catch (DatabaseHookException e) {
				job.addProperty("code", "Failed Database Operation " + e.getMessage());
			} catch (JsonSyntaxException e) {
				job.addProperty("code", "Invalid JsonObject received from database");
			}

		}

		resp.setContentType("application/json");
		PrintWriter pw = resp.getWriter();
		pw.println(job);
		pw.close();
	}

	/**
	 * Create a new group. The admin will be user invoking the servlet
	 */

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		JsonObject job = new JsonObject();
		Gson gson = new Gson();
		User u;

		if (!isValidParamenters(req, "id", "token", "group")) {
			job.addProperty("code", "Invalid API parameters");
		} else if ((u = isAuthorizedUser(req.getParameter("id"), req.getParameter("token"))) == null) {
			job.addProperty("code", "Invalid or unathorized user");
		} else {
			Group g = new Group(req.getParameter("group"), u);
			try {
				groupdb.request("createGroup", gson.toJson(g));
				job.addProperty("code", "OK");
				job.addProperty("group", g.getName());
			} catch (DatabaseHookException e) {
				job.addProperty("code", "Failed Database Operation " + e.getMessage());
			} catch (JsonSyntaxException e) {
				job.addProperty("code", "Invalid JsonObject received from database");
			}
		}

		resp.setContentType("application/json");
		PrintWriter pw = resp.getWriter();
		pw.println(job);
		pw.close();
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		JsonObject job = new JsonObject();
		Gson gson = new Gson();
		User owner;
		Group g;

		if (!isValidParamenters(req, "id", "token", "group")) {
			job.addProperty("code", "Invalid API parameters");
		} else if ((owner = isAuthorizedUser(req.getParameter("id"), req.getParameter("token"))) == null) {
			job.addProperty("code", "Invalid or unathorized user");
		} else if ((g = isValidGroup(req.getParameter("group"))) == null) {
			job.addProperty("code", "Invalid Group name");
		} else if (!g.getOwner().equals(owner.getName())) {
			job.addProperty("code", "Unathorized user: only the admin of a group can remove it");
		} else {
			try {
				groupdb.request("deleteGroup", gson.toJson(g));
				job.addProperty("code", "OK");
			} catch (DatabaseHookException e) {
				job.addProperty("code", "Failed Database Operation");
			} catch (JsonSyntaxException e) {
				job.addProperty("code", "Invalid JsonObject received from database");
			}
		}

		resp.setContentType("application/json");
		PrintWriter pw = resp.getWriter();
		pw.println(job);
		pw.close();
	}

	private boolean isValidParamenters(HttpServletRequest req, String... pars) {
		for (String s : pars) {
			if (req.getParameter(s) == null || req.getParameter(s).equals(""))
				return false;
		}
		return true;
	}

	private User isAuthorizedUser(String userid, String token) {
		Gson gson = new Gson();
		User u = null;
		try {
			JsonObject jo = userdb.request("findUserById", userid);
			System.err.println("Json devuelto: " + jo.toString());
			u = gson.fromJson(jo.get("user"), User.class);
			System.err.println("Usuario encontrado : " + u.getName());
			if (u == null || !u.getToken().equals(token) || !u.isTokenValid())
				u = null;
		} catch (DatabaseHookException | JsonSyntaxException e) {
			u = null;
		}

		return u;
	}

	private Group isValidGroup(String groupName) {
		Gson gson = new Gson();
		Group g = null;
		try {
			JsonObject jo = groupdb.request("findGroupByName", groupName);
			g = gson.fromJson(jo, Group.class);
		} catch (DatabaseHookException | JsonSyntaxException e) {
			g = null;
		}

		return g;
	}
}
