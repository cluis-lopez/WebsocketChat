package com.clopez.chat;

import java.io.IOException;
import java.lang.reflect.Type;
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

import com.clopez.chat.datamgnt.Message;
import com.clopez.chat.datamgnt.OffLineDatabase;
import com.clopez.chat.datamgnt.OffMessage;
import com.clopez.chat.datamgnt.User;
import com.clopez.chat.datamgnt.UserDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

@ServerEndpoint(value = "/Server/{payload}")
public class Server {
	private static Type typeUser = new TypeToken<HashMap<String, User>>() {}.getType();
	private static Type typeOffline = new TypeToken<HashMap<String, OffMessage>>() {}.getType();
    private static Map<String, Session> sessions = new HashMap<String, Session>();
    private Map<String, String> payload;
    private Type typePayload = new TypeToken<HashMap<String, String>>() {}.getType();
    private final TypeAdapter<JsonElement> strictAdapter = new Gson().getAdapter(JsonElement.class);
    private Gson gson = new Gson();
    private static UserDatabase userdb = new UserDatabase("usersdb", typeUser);
    private static OffLineDatabase offdb = new OffLineDatabase("offlinedb", typeOffline);
    private User user;

    @OnOpen
    public void onOpen(Session session, @PathParam("payload") String pl) {
        System.out.println("Open Connection ..." + session.getId() + " Payload: " + pl);
        if (isValidPayload(pl)) {
            payload = gson.fromJson(pl, typePayload);
            User u = userdb.findById(payload.get("id"));
            if (u != null && payload.get("token").equals(u.getToken())) {
                // Usuario Válido
                sessions.put(u.getName(), session);
                this.user = u;
                System.out.println("Usuario " + u.getName() + " Conectado");
                System.out.println("En el sistema hay " + sessions.size() + " usuarios conectados");
                List<Message> l = offdb.messagesOfUser(u);
                if (l.size()>0) {
                	System.out.println("El usuario " + u.getName()+" tiene "+l.size()+" mensajes pendientes");
                	for (Message m: l)
                		if (m.send(session))
                			System.out.println("Despachado mensaje " + m.getId());
                	int i = offdb.deleteDeliveredForUser(u);
                	if (i<l.size())
                		System.out.println("Warning... quedan " + (l.size() - i) + " mensajes por despachar");
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

        if (!isValidMessage(pl)) {
            response.put("code", "Invalid Message");
        } else {
            payload = gson.fromJson(pl, typePayload);
            Session sid = isConnectedUser(payload.get("to"));
            user.updateRecent(payload.get("to"));
            Message m = new Message(payload);
            if (sid != null){ //El usuario "to" está conectado
            	if (m.send(sid)) {
            		response.put("id", payload.get("id"));
                    System.out.println("Enviado mensaje al usuario: " + m.getTo() + " SesionId: "+ sid.getId() + " desde el usuario " + m.getFrom());
            	} else {
            		System.out.println("Error al enviar el mensaje");
            	}
            } else if (isValidUser(payload.get("to"))){
            	//User exists but is not connected
                offdb.addMessage(m);
                System.out.println("Mensaje añadido a la cola de " + payload.get("to"));
            } else {
            	response.put("code", "Invalid or non-connected user");
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

    private boolean isValidMessage(String json) {
        if (json != null && isValidJson(json)) {
            payload = gson.fromJson(json, typePayload);
            String from = payload.get("from");
            if (from != null && ! from.equals("")
            		&& from.equals(user.getName() )&& payload.get("type") != null)
                return true;
        }
        return false;
    }

    private Session isConnectedUser(String uname){
        for (String u : sessions.keySet()){
            System.out.println("Conectado : " + u );
            if (u.equals(uname))
                return sessions.get(u);
        }
        return null;
    }
}
