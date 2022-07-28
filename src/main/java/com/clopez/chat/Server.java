package com.clopez.chat;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
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
    private static UserDatabase db = new UserDatabase("usersdb", typeUser);
    private User user;

    @OnOpen
    public void onOpen(Session session, @PathParam("payload") String pl) {
        System.out.println("Open Connection ..." + session.getId() + " Payload: " + pl);
        if (isValidPayload(pl)) {
            payload = gson.fromJson(pl, typePayload);
            User u = db.findById(payload.get("id"));
            if (u != null && payload.get("token").equals(u.getToken())) {
                // Usuario Válido
                sessions.put(u.getName(), session);
                this.user = u;
                System.out.println("Usuario " + u.getName() + " Conectado");
                System.out.println("En el sistema hay " + sessions.size() + " usuarios conectados");
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
            if (sid != null){ //El usuario "to" está conectado
                try {
                	Map<String, String> message = new HashMap<>();
                    message.put("type", "TEXT");
                    message.put("id", payload.get("id"));
                    message.put("to", payload.get("to"));
                    message.put("from", payload.get("from"));
                    message.put("content", payload.get("content"));
                    sid.getBasicRemote().sendText(gson.toJson(message));
                    response.put("id", payload.get("id"));
                    System.out.println("Enviado mensaje al usuario: " + message.get("to") + " SesionId: "+ sid.getId() + " desde el usuario " + message.get("from"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
