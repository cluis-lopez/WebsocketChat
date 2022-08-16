package com.clopez.chat;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.clopez.chat.datamgnt.Group;
import com.clopez.chat.datamgnt.GroupDatabase;
import com.clopez.chat.datamgnt.User;
import com.clopez.chat.datamgnt.UserDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@WebServlet("/groupMgmt")
public class groupMgmt extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Type typeUser = new TypeToken<HashMap<String, User>>() {}.getType();
	private static Type typeGroup = new TypeToken<HashMap<String, Group>>() {}.getType();
	private static UserDatabase userdb = new UserDatabase("usersdb", typeUser);
	private static GroupDatabase groupdb = new GroupDatabase("groupsdb", typeGroup);

	/**
	 * Returns the list of groups of the invoking user (both as admin or as normal user)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		JsonObject job = new JsonObject();
		User u;

		if (! isValidParamenters(req, "id", "token")) {
			job.addProperty("code", "Invalid API parameters");
		} else if ((u = isAuthorizedUser(req.getParameter("id"), req.getParameter("token"))) == null) {
			job.addProperty("code", "Invalid or unathorized user");
		} else {
			job.addProperty("code", "OK");
			List<Group> ownerOf = groupdb.findByOwner(u);
			JsonArray jarr = new JsonArray();
			for (Group g : ownerOf)
				jarr.add(g.getName());
			job.add("ownerOf", jarr);

			List<Group> memberOf = groupdb.findByUser(u);
			JsonArray jarr2 = new JsonArray();
			for (Group g : memberOf)
				jarr2.add(g.getName());
			job.add("memberOf", jarr2);
		
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
		User u;
		
		if (! isValidParamenters(req, "id", "token", "group")) {
			job.addProperty("code", "Invalid API parameters");
		} else if ((u = isAuthorizedUser(req.getParameter("id"), req.getParameter("token"))) == null) {
			job.addProperty("code", "Invalid or unathorized user");
		} else {
			Group g = new Group(req.getParameter("group"), u);
			try {
				groupdb.createGroup(g);
				job.addProperty("group", g.getName());
			} catch (IllegalArgumentException e) {
				job.addProperty("code", "Invalid group name " + g.getName());
				e.printStackTrace();
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
		User owner;
		Group g;
		
		if (! isValidParamenters(req, "id", "token", "group")) {
			job.addProperty("code", "Invalid API parameters");
		} else if ((owner = isAuthorizedUser(req.getParameter("id"), req.getParameter("token"))) == null) {
			job.addProperty("code", "Invalid or unathorized user");
		} else if ((g = isValidGroup(req.getParameter("group"))) == null) {
			job.addProperty("code", "Invalid Group name");
		} else if (! g.getOwner().equals(owner.getName())) {
			job.addProperty("code", "Unathorized user: only the admin of a group can remove it");
		} else {
			groupdb.deleteGroup(g);
			job.addProperty("code", "OK");
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
		User u = userdb.findById(userid);
		if (u == null || !u.getToken().equals(token) || !u.isTokenValid())
			u = null;
		return u;
	}
	
	private Group isValidGroup(String groupName) {
		return groupdb.findGroupByName(groupName);
	}	
}
