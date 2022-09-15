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
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

@WebServlet("/userGroupMgmt")
public class userGroupMgmt extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static DatabaseHook udb = new DatabaseHook("UserDatabase");
	private static DatabaseHook gdb = new DatabaseHook("GroupDatabase");
	Gson gson = new Gson();

	/**
	 * Returns the list of users of certain group
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		JsonObject job = new JsonObject();
		Group g;

		if (!isValidParamenters(req, "id", "token", "group")) {
			job.addProperty("code", "Invalid API parameters");
		} else if ((isAuthorizedUser(req.getParameter("id"), req.getParameter("token"))) == null) {
			job.addProperty("code", "Invalid or unathorized user");
		} else {
			JsonObject jo = new JsonObject();
			try {
				jo = gdb.request("findGroupByName", req.getParameter("group"));
				g = gson.fromJson(jo.get("group"), Group.class);
				JsonArray jarr = new JsonArray();
				for (String s : g.getUsers())
					jarr.add(s);
				job.add("users", jarr);
				job.addProperty("code", "OK");
				job.addProperty("group", g.getName());
				job.addProperty("owner", g.getOwner());
			} catch (DatabaseHookException e) {
				job.addProperty("code", e.getMessage());
			} catch (JsonSyntaxException e) {
				job.addProperty("code", "Invalid JsonObject received from database");
			} catch (NullPointerException e) {
				job.addProperty("code", "Null Pointer... ¿Invalid parameters?");
			}
		}

		resp.setContentType("application/json");
		PrintWriter pw = resp.getWriter();
		pw.println(job);
		pw.close();
	}

	/**
	 * Adds a user to the group. Can only be done by rhe group's administrator
	 * (owner)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		JsonObject job = new JsonObject();
		User owner;
		User userToAdd;
		Group g;

		if (!isValidParamenters(req, "id", "token", "group", "user")) {
			job.addProperty("code", "Invalid API parameters");
		} else if ((owner = isAuthorizedUser(req.getParameter("id"), req.getParameter("token"))) == null) {
			job.addProperty("code", "Invalid or unathorized user");
		} else if ((g = isValidGroup(req.getParameter("group"))) == null) {
			job.addProperty("code", "Invalid Group name");
		} else if ((userToAdd = isValidUser(req.getParameter("user"))) == null) {
			job.addProperty("code", "Invalid user to include as member of group " + g.getName());
		} else if (userToAdd.getName().equals(owner.getName())) {
			job.addProperty("code", "Owner cannot be into the group's member lists");
		} else if (!g.getOwner().equals(owner.getName())) {
			job.addProperty("code", "You're not the owner of this group");
		} else {
			try {
				JsonObject jo = new JsonObject();
				jo.addProperty("id", g.getId());
				jo.addProperty("user", gson.toJson(userToAdd));
				jo = gdb.request("addMember", jo.getAsString());
				if (jo.get("code").getAsString().equals("OK")) {
					job.addProperty("code", "OK");
					job.addProperty("user", userToAdd.getName());
					job.addProperty("group", g.getName());
				} else {
					job.addProperty("code", jo.get("code").getAsString());
				}
			} catch (DatabaseHookException e) {
				job.addProperty("code", "Failed Database Operation " + e.getMessage());
			} catch (JsonSyntaxException e) {
				job.addProperty("code", "Invalid JsonObject received from database");
			} catch (NullPointerException e) {
				job.addProperty("code", "Null Pointer... ¿Invalid parameters?");
			}
		}

		resp.setContentType("application/json");
		PrintWriter pw = resp.getWriter();
		pw.println(job);
		pw.close();
	}

	/**
	 * Delete a user from the group. Can be done by the group's administrator
	 * (owner) amd by the user himself
	 */
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		JsonObject job = new JsonObject();
		User owner;
		User userToDelete;
		Group g;

		if (!isValidParamenters(req, "id", "token", "user", "group")) {
			job.addProperty("code", "Invalid API parameters");
		} else if ((owner = isAuthorizedUser(req.getParameter("id"), req.getParameter("token"))) == null) {
			job.addProperty("code", "Invalid or unathorized user");
		} else if ((g = isValidGroup(req.getParameter("group"))) == null) {
			job.addProperty("code", "Invalid Group name");
		} else if ((userToDelete = isValidUser(req.getParameter("user"))) == null) {
			job.addProperty("code", "Invalid user to delete from the group's members");
		} else if (!g.getUsers().contains(userToDelete.getName())) {
			job.addProperty("code", "This user is not in the group already");
		} else if (owner.getName().equals(g.getOwner()) || userToDelete.getName().equals(owner.getName())) {
			try {
				JsonObject jo = new JsonObject();
				jo.addProperty("id", g.getId());
				jo.addProperty("user", gson.toJson(userToDelete));
				jo = gdb.request("removeMember", jo.getAsString());
				job.addProperty("code", jo.get("code").getAsString());
			} catch (DatabaseHookException e) {
				job.addProperty("code", "Failed Database Operation " + e.getMessage());
			} catch (JsonSyntaxException e) {
				job.addProperty("code", "Invalid JsonObject received from database");
			} catch (NullPointerException e) {
				job.addProperty("code", "Null Pointer... ¿Invalid parameters?");
			}
		} else {
			job.addProperty("code", "Only the admin of a group or the user can delete a user from a group");
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
			JsonObject jo = udb.request("findUserById", userid);
			u = gson.fromJson(jo.get("user"), User.class);
			if (u == null || !u.getToken().equals(token) || !u.isTokenValid())
				u = null;
		} catch (DatabaseHookException | JsonSyntaxException | NullPointerException e) {
			u = null;
		}

		return u;
	}

	private Group isValidGroup(String groupName) {
		Gson gson = new Gson();
		Group g = null;
		try {
			JsonObject jo = gdb.request("findGroupByName", groupName);
			g = gson.fromJson(jo, Group.class);
		} catch (DatabaseHookException | JsonSyntaxException | NullPointerException e) {
			g = null;
		}

		return g;
	}

	private User isValidUser(String userName) {
		Gson gson = new Gson();
		User u = null;
		try {
			JsonObject jo = udb.request("findUserByName", userName);
			u = gson.fromJson(jo, User.class);
		} catch (DatabaseHookException | JsonSyntaxException | NullPointerException e) {
			u = null;
		}

		return u;
	}
}