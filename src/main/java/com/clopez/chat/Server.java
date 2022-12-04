package com.clopez.chat;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.clopez.datamgnt.DatabaseHook;
import com.clopez.datamgnt.DatabaseHookException;
import com.clopez.datamgnt.Group;
import com.clopez.datamgnt.Message;
import com.clopez.datamgnt.User;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

@ServerEndpoint(value = "/Server/{payload}")
public class Server {
	private static Map<String, Session> sessions = new HashMap<String, Session>();
	private Map<String, String> payload;
	private Type typePayload = new TypeToken<HashMap<String, String>>() {
	}.getType();
	private final TypeAdapter<JsonElement> strictAdapter = new Gson().getAdapter(JsonElement.class);
	private Gson gson = new Gson();
	private static DatabaseHook userdb = new DatabaseHook("UserDatabase");
	private static DatabaseHook groupdb = new DatabaseHook("GroupDatabase");
	private static DatabaseHook messdb = new DatabaseHook("MessageDatabase");
	private User user;

	@OnOpen
	public void onOpen(Session session, @PathParam("payload") String pl) {
		System.out.println("Open Connection ..." + session.getId() + " Credentials: " + pl);
		if (!isValidPayload(pl)) {
			System.err.println("Parámetros inválidos " + pl);
			try {
				session.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}

		payload = gson.fromJson(pl, typePayload);
		User u = null;
		try {
			System.out.println("Voy a buscar a " + payload.get("user") + " con id " + payload.get("id"));
			JsonObject jo = userdb.request("findUserById", payload.get("id"));
			System.out.println("Devuelto BBDD: " + jo.get("code"));
			System.out.println("Devuelto2 BBDD " + jo.get("user"));
			if (!jo.get("code").getAsString().equals("OK"))
				throw new DatabaseHookException("Database error: " + jo.get("code").getAsString());
			u = gson.fromJson(jo.get("user"), User.class);
			System.err.println("Econtradio usuario " + u);
			if (u == null || !payload.get("token").equals(u.getToken()))
				throw new DatabaseHookException("Invalid credentials");
		} catch (DatabaseHookException | JsonSyntaxException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			return;
		}

		// Usuario Válido
		sessions.put(u.getName(), session);
		this.user = u;
		u.setConnected(true);
		System.out.println("Usuario " + u.getName() + " Conectado");
		System.out.println("En el sistema hay " + sessions.size() + " usuarios conectados");

		try {
			JsonObject jo = messdb.request("getPendingMessagesTo", u.getName());
			JsonArray ja = jo.get("messages").getAsJsonArray();
			if (ja.size() > 0) {
				System.out.println("El usuario " + u.getName() + " tiene " + ja.size() + " mensajes pendientes");
				for (JsonElement je : ja) {
					Message m = gson.fromJson(je, Message.class);
					if (messageSend(session, m))
						System.err.println("Despachado mensaje " + m.getId());
					else
						System.err.println("Fallo al enviar mensaje pendiente");
				}
			}
		} catch (DatabaseHookException | JsonSyntaxException e) {
			System.err.println("Fallo en el procesado de mensajes pendientes " + e.getMessage());
		}
	}

	@OnClose
	public void onClose() {
		if (sessions.containsKey(user.getName())) {
			sessions.remove(user.getName());
			user.setConnected(false);
		}
		System.out.println("Closing Connection for " + user.getName());
	}

	@OnMessage
	public String onMessage(Session session, String pl) {
		System.out.println("Recibido: " + pl);
		Map<String, String> response;
		response = new HashMap<>();
		response.put("type", "CONTROL");
		response.put("code", "OK");
		response.put("status", "");
		
		Message m;
		
		if ((m = isValidMessage(pl)) == null) {
			response.put("code", "Invalid Message");
			return gson.toJson(response);
		}

		payload = gson.fromJson(pl, typePayload);
		User u;
		Group g;
		
		response.put("id", payload.get("id"));

		if ((u = isValidUser(payload.get("to"))) != null) { // Usuario registrado
			Session sid = isConnectedUser(u.getName());
			if (sid != null) { // Usuario conectado
				if ( ! messageSend(sid, m)) {
					response.put("code", "Cannot deliver Message");
					response.put("status", "Cannot deliver message");
				}
			} else {
				response.put("status", "User not connected");
			}
		} else if ((g = isValidGroup(payload.get("to"))) != null){ // Grupo resgistrado
			for (String s : g.getUsers()) {
				Session sid = (isConnectedUser(s));
				if (sid != null) // Usuario conectado
					if (! messageSend(sid, m)) {
						response.put("code", "Cannot deliver Message");
						response.put("status", "Cannot deliver message");
					}
			}
		} else {
			response.put("code", "Invalid destination");
			return gson.toJson(response);
		}

		user.updateRecent(payload.get("to"), false);
		try {
			messdb.request("addMessage", gson.toJson(m));
			if (user.needsUpdate())
				userdb.request("updateUser", gson.toJson(user));
			response.put("code", "OK");
		} catch (DatabaseHookException | JsonSyntaxException e) {
			response.put("code", "addMessage or updateUser" + e.getMessage());
		}

		System.out.println("Devuelto al remitente : " + gson.toJson(response));
		return gson.toJson(response);
	}

	@OnError
	public void onError(Throwable e) {
		e.printStackTrace();
	}

	private boolean messageSend(Session sid, Message m) {
		boolean ret;
		Gson gson = new Gson();
		try {
			sid.getBasicRemote().sendText(gson.toJson(m));
			m.setDelivered();
			ret = true;
		} catch (IOException e) {
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}

	private User isValidUser(String name) {
		Gson gson = new Gson();
		User u, ret = null;
		try {
			JsonObject jo = userdb.request("findUserByName", name);
			u = gson.fromJson(jo, User.class);
			if (jo.get("code").getAsString().equals("OK") && u != null)
				ret = u;
		} catch (DatabaseHookException | JsonSyntaxException e) {
		}
		return ret;
	}

	private Group isValidGroup(String name) {
		Gson gson = new Gson();
		Group g, ret = null;
		try {
			JsonObject jo = groupdb.request("findGroupByName", name);
			g = gson.fromJson(jo, Group.class);
			if (jo.get("code").getAsString().equals("OK") && g != null)
				ret = g;
		} catch (DatabaseHookException | JsonSyntaxException | NullPointerException e) {
		}
		return ret;
	}

	private boolean isValidJson(String json) {
		try {
			strictAdapter.fromJson(json);
		} catch (JsonSyntaxException | IOException e) {
			System.out.println("Invalid JSON");
			return false;
		}
		return true;
	}

	private boolean isValidPayload(String json) {
		if (json != null && isValidJson(json)) {
			try {
				payload = gson.fromJson(json, typePayload);
				if (payload.get("id") != null && payload.get("id") != "")
					return true;
			} catch (JsonSyntaxException e) {
				System.err.println("Malformed JSON payload " + e.getMessage());
				return false;
			}
		}
		return false;
	}

	private Message isValidMessage(String json) {
		Message m = null;
		if (json != null && isValidJson(json)) {
			payload = gson.fromJson(json, typePayload);
			String from = payload.get("from");
			System.err.println("From : " + from);
			if (from != null && !from.equals("") && from.equals(user.getName()) && payload.get("type") != null
					&& payload.get("createdAt") != null) {
				try {
					Instant.parse(payload.get("createdAt"));
					m = new Message(payload);
				} catch (DateTimeParseException e) {
					m = null;
				}
			}
		}
		return m;
	}

	private Session isConnectedUser(String uname) {
		for (String u : sessions.keySet()) {
			if (u.equals(uname))
				return sessions.get(u);
		}
		return null;
	}
}
