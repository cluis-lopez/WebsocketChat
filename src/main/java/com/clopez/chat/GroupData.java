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

@WebServlet(urlPatterns = { "/Groupdata/userGroupMgmt/*", "/Groupdata/groupMgmt/*" })
public class GroupData extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Type type1 = new TypeToken<HashMap<String, Group>>() {
	}.getType();
	private static GroupDatabase groupdb = new GroupDatabase("groupsdb", type1);
	private static Type type2 = new TypeToken<HashMap<String, User>>() {
	}.getType();
	private static UserDatabase userdb = new UserDatabase("usersdb", type2);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String userid = req.getParameter("id");
		String token = req.getParameter("token");

		JsonObject job = new JsonObject();
		User user = isValidUser(userid, token);

		if (user != null) {
			job.addProperty("code", "OK");
			List<Group> ownerOf = groupdb.findByOwner(user);
			JsonArray jarr = new JsonArray();
			for (Group g : ownerOf)
				jarr.add(g.getName());
			job.add("ownerOf", jarr);

			List<Group> memberOf = groupdb.findByUser(user);
			JsonArray jarr2 = new JsonArray();
			for (Group g : memberOf)
				jarr2.add(g.getName());
			job.add("memberOf", jarr2);
		} else {
			job.addProperty("code", "Invaliid or not authoruzed user");
		}

		resp.setContentType("application/json");
		PrintWriter pw = resp.getWriter();
		pw.println(job);
		pw.close();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String userid = req.getParameter("id");
		String token = req.getParameter("token");

		Map<String, String> response = new HashMap<>();
		response.put("code", "OK");

		String command = req.getRequestURI().substring(req.getContextPath().length() + 1);
		command = command.substring(command.indexOf("/"));
		System.out.println("Ha llegado : " + command);

		User owner = isValidUser(userid, token);
		if (owner != null) {
			if (command.equals("/groupMgmt")) { // Crear un nuevo grupo
				String name = req.getParameter("group");
				if (name != null && !name.equals("")) {
					Group g = new Group(name, owner);
					groupdb.createGroup(g);
					response.put("group", name);
				}
			} else if (command.equals("/userGroupMgmt")) { // Añadir un usuario al grupo
				String group = req.getParameter("group");
				String name = req.getParameter("user");
				if (name != null && !name.equals("") && group != null && !group.equals("")) {
					User user = userdb.findUserByName(name);
					Group g = groupdb.findGroupByName(group);
					if (!g.getOwner().equals(user.getName())) // Solo el administrador del grupo puede añadir
																		// nuevos usuarios
						response.put("code", "You're not the owner of this group");
					else if (!groupdb.addmember(g.getId(), user))
						response.put("code", "Invalid user");
				}
			} else {
				response.put("code", "Invaliid API entry point " + command);
			}
		} else {
			response.put("code", "Invaliid or not authoruzed user");
		}

		Gson json = new Gson();

		resp.setContentType("application/json");
		PrintWriter pw = resp.getWriter();
		pw.println(json.toJson(response));
		pw.close();
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String userid = req.getParameter("id");
		String token = req.getParameter("token");

		String command = req.getRequestURI().substring(req.getContextPath().length() + 1);
		command = command.substring(command.indexOf("/"));
		System.out.println("Ha llegado : " + command);

		Map<String, String> response = new HashMap<>();
		response.put("code", "OK");

		User user = isValidUser(userid, token);

		if (user != null) {
			if (command.equals("/groupMgmt")) { // Borrar el grupo
				String group = req.getParameter("group");
				if (group != null && !group.equals("")) {
					Group g = groupdb.findGroupByName(group);
					if (user.getName().equals(g.getOwner())) {
						try {
							groupdb.deleteGroup(g);
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
							response.put("code", "Invalid group name");
						}
					} else {
						response.put("code", "Only the group's owner is autrhorized to delte this group");
					}
				} else {
					response.put("code", "Invalid group name");
				}
			} else if (command.equals("/userGroupMgmt")) { // Eliminar un usuario del grupo
				String group = req.getParameter("group");
				if (group != null && !group.equals("")) {
					Group g = groupdb.findGroupByName(group);
					if (!groupdb.addmember(g.getId(), user))
						response.put("code", "Invalid user");
				} else {
					response.put("code", "Invalid group name");
				}
			}
		}

		Gson json = new Gson();

		resp.setContentType("application/json");
		PrintWriter pw = resp.getWriter();
		pw.println(json.toJson(response));
		pw.close();
	}

	private User isValidUser(String userid, String token) {
		User u = null;
		if (userid != null && !userid.equals("") && token != null && !token.equals("")) {
			u = userdb.findById(userid);
			if (u == null || !u.getToken().equals(token) || !u.isTokenValid())
				u = null;
		}
		return u;
	}
}
