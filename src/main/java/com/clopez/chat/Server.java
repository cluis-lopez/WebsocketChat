package com.clopez.chat;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.clopez.chat.datamgnt.User;
import com.clopez.chat.datamgnt.UserDatabase;
import com.clopez.chat.messages.Message;
import com.clopez.chat.messages.MessageDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

@ServerEndpoint(value = "/Server/{payload}")
public class Server {
	private static Type typeUser = new TypeToken<HashMap<String, User>>() {}.getType();
    private static Map<String, Session> sessions = new HashMap<String, Session>();
    private Map<String, String> payload;
    private Type typePayload = new TypeToken<HashMap<String, String>>() {}.getType();
    private final TypeAdapter<JsonElement> strictAdapter = new Gson().getAdapter(JsonElement.class);
    private Gson gson = new Gson();
    private static UserDatabase userdb = new UserDatabase("usersdb", typeUser);
    private static MessageDatabase messdb = new MessageDatabase();
    private User user;

    @OnOpen
    public void onOpen(Session session, @PathParam("payload") String pl) {
        System.out.println("Open Connection ..." + session.getId() + " Credentials: " + pl);
        if (isValidPayload(pl)) {
            payload = gson.fromJson(pl, typePayload);
            User u = userdb.findById(payload.get("id"));
            if (u != null && payload.get("token").equals(u.getToken())) {
                // Usuario Válido
                sessions.put(u.getName(), session);
                this.user = u;
                System.out.println("Usuario " + u.getName() + " Conectado");
                System.out.println("En el sistema hay " + sessions.size() + " usuarios conectados");
                List<Message> l = messdb.getPendingMessagesTo(u.getName());
                if (! l.isEmpty()) {
                	System.out.println("El usuario " + u.getName()+" tiene "+l.size()+" mensajes pendientes");
                	for (Message m: l) {
                		if (m.send(session))
                			System.out.println("Despachado mensaje " + m.getId());
                		else
                			System.out.println("Fallo al enviar mensaje pendiente");
                	}
                }
            } else {
                System.out.println("Identificación Incorrecta " + pl);
                try {
                    session.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("Parámetros inválidos " + pl);
            try {
                session.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClose
    public void onClose() {
    	if (sessions.containsKey(user.getName()))
    		sessions.remove(user.getName());
        System.out.println("Closing Connection ...");
    }

    @OnMessage
    public String onMessage(Session session, String pl) {
        System.out.println("Recibido: " + pl);
        Map<String,String> response;
        response = new HashMap<>();
    	response.put("type", "CONTROL");
        response.put("code", "OK");
        response.put("status", "");
        
        Message m;
        if ((m = isValidMessage(pl)) == null) {
            response.put("code", "Invalid Message");
        } else {
            if (! isValidUser(payload.get("to"))){ //Usuario no registrado
            	response.put("code", "Invalid user");
            } else {
            	Session sid = isConnectedUser(payload.get("to"));
            	if (sid != null) //Usuario conectado
            		if (m.send(sid))
            			response.put("id", payload.get("id"));
            		else {
            			response.put("code", "Cannot deliver Message");
            			response.put("status", "Cannot deliver message");
            		} else
            			response.put("status", "User not connected");
            	user.updateRecent(payload.get("to"));
            	if (user.needsUpdate())
            		userdb.saveDatabase();
            	messdb.addMessage(m);
            	}
            }
  
        System.out.println("Devuelto al remitente : " + gson.toJson(response));
        return gson.toJson(response);
    }

    @OnError
    public void onError(Throwable e) {
        e.printStackTrace();
    }
    
    private boolean isValidUser(String name) {
    	if (userdb.findUserByName(name) != null)
    		return true;
    	else
    		return false;
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
            payload = gson.fromJson(json, typePayload);
            if (payload.get("id") != null && payload.get("id") != "")
            	return true;
        }
        return false;
    }

    private Message isValidMessage(String json) {
    	Message m = null;
        if (json != null && isValidJson(json)) {
            payload = gson.fromJson(json, typePayload);
            String from = payload.get("from");
            if (from != null && ! from.equals("")
            		&& from.equals(user.getName() )&& payload.get("type") != null 
            		&& payload.get("createdAt")!= null) {
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

    private Session isConnectedUser(String uname){
        for (String u : sessions.keySet()){
            if (u.equals(uname))
                return sessions.get(u);
        }
        return null;
    }
}
