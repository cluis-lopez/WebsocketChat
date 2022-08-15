package com.clopez.chat;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.clopez.chat.datamgnt.Group;
import com.clopez.chat.datamgnt.GroupDatabase;
import com.clopez.chat.datamgnt.User;
import com.clopez.chat.datamgnt.UserDatabase;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@WebServlet("/userGroupMgmt")
public class userGroupMgmt extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Type typeUser = new TypeToken<HashMap<String, User>>() {}.getType();
	private static Type typeGroup = new TypeToken<HashMap<String, Group>>() {}.getType();
	private static UserDatabase userdb = new UserDatabase("usersdb", typeUser);
	private static GroupDatabase groupdb = new GroupDatabase("groupsdb", typeGroup);
	
	/**
	 * Returns the list of users of certain group
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		JsonObject job = new JsonObject();
		User u;
		Group g;
		
		if (! isValidParamenters(req, "id", "token", "group")) {
			job.addProperty("code", "Invalid API parameters");
		} else if ((u = isAuthorizedUser(req.getParameter("id"), req.getParameter("token"))) == null) {
			job.addProperty("code", "Invalid or unathorized user");
		} else if( (g = groupdb.findGroupByName(req.getParameter("group"))) != null) {;
			JsonArray jarr = new JsonArray();
			for(String s : g.getUsers())
				jarr.add(s);
			job.add("users", jarr);
			job.addProperty("code", "OK");
		} else {
			job.addProperty("code", "Invalid group name");
		}
		
		resp.setContentType("application/json");
		PrintWriter pw = resp.getWriter();
		pw.println(job);
		pw.close();
	}
	
	/**
	 * Adds a user to the group. Can only be done by rhe group's administrator (owner)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		JsonObject job = new JsonObject();
		User owner;
		User userToAdd;
		Group g;
		
		if (! isValidParamenters(req, "id", "token", "user", "group")) {
			job.addProperty("code", "Invalid API parameters");
		} else if ((owner = isAuthorizedUser(req.getParameter("id"), req.getParameter("token"))) == null) {
			job.addProperty("code", "Invalid or unathorized user");
		} else if ((g = isValidGroup(req.getParameter("group"))) == null) {
			job.addProperty("code", "Invalid Group name");
		} else if ((userToAdd = isValidUser(req.getParameter("user"))) != null) {
			job.addProperty("code", "Invalid user to include in the group's members");
		} else if (userToAdd.getName().equals(owner.getName())) {
			job.addProperty("code", "Owner cannot be into the group's member lists");
		} else if (! g.getOwner().equals(owner.getName())) {
			job.addProperty("code", "You're not the owner of this group");
		} else {
			groupdb.addmember(g.getId(), userToAdd);
			job.addProperty("code",  "OK");
			job.addProperty("user", userToAdd.getName());
			job.addProperty("group", g.getName());
		}
		
		resp.setContentType("application/json");
		PrintWriter pw = resp.getWriter();
		pw.println(job);
		pw.close();
	}
	
	/**
	 * Delete a user from the group. Can  be done by the group's administrator (owner) amd by the user himself
	 */
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		JsonObject job = new JsonObject();
		User owner;
		User userToDelete;
		Group g;
		
		if (! isValidParamenters(req, "id", "token", "user", "group")) {
			job.addProperty("code", "Invalid API parameters");
		} else if ((owner = isAuthorizedUser(req.getParameter("id"), req.getParameter("token"))) == null) {
			job.addProperty("code", "Invalid or unathorized user");
		} else if ((g = isValidGroup(req.getParameter("group"))) == null) {
			job.addProperty("code", "Invalid Group name");
		} else if ((userToDelete = isValidUser(req.getParameter("user"))) != null) {
			job.addProperty("code", "Invalid user to delete from the group's members");
		} else if (! g.getUsers().contains(userToDelete)) {
			job.addProperty("code", "This user is not in the group already");
		} else if (owner.getName().equals(g.getOwner()) || userToDelete.getName().equals(owner.getName())) {
			groupdb.removeMember(g.getId(), userToDelete);
			job.addProperty("code", "OK");
		} else {
			job.addProperty("code", "Only the admin of a group or the user can delete a user from a group");
		}
		
		resp.setContentType("application/json");
		PrintWriter pw = resp.getWriter();
		pw.println(job);
		pw.close();
		
	}
		

	private boolean isValidParamenters(HttpServletRequest req, String... pars) {
		boolean ret = false;
		for (String s : pars) {
			if (req.getParameter(s) != null && req.getParameter(s) != "")
				ret = true;
			else
				ret = false;
		}
		return ret;
	}
	
	private User isAuthorizedUser(String userid, String token) {
		User u = userdb.findById(userid);
		if (u == null || !u.getToken().equals(token) || !u.isTokenValid())
			u = null;
		return u;
	}
	
	private Group isValidGroup(String groupName) {
		return groupdb.findGroupByName(groupName);
	}
	
	private User isValidUser(String userName) {
		return userdb.findUserByName(userName);
	}
}