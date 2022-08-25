package com.clopez.chat;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.clopez.chat.datamgnt.User;
import com.clopez.chat.datamgnt.UserDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@WebServlet("/Register")
public class Register extends HttpServlet {
	
	Type type = new TypeToken<HashMap<String, User>>() {}.getType();
    UserDatabase db = new UserDatabase("usersdb", type);
    Gson gs = new Gson();

    public void doPost (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> response = new HashMap<String, String>();
        response.put("code", "OK");
        response.put("user", "");
        response.put("id", "");

        String user = req.getParameter("user");
        String password = req.getParameter("password");

        User u = db.findUserByName(user);
        if (u == null && isValidPassword(password)){
            //El usuario NO existe y la password es válida
            u = new User(user, password);
            try {
            	db.createUser(u);
            	response.put("user", u.getName());
                response.put("id", u.getId());
            } catch (IllegalArgumentException e) {
            	response.put("code", e.getMessage());
            }

        } else {
            response.put("code", "Invalid User or Password");
        }

        resp.setContentType("application/json");
        PrintWriter pw = resp.getWriter();
        pw.println(gs.toJson(response));
        pw.close();
    }
    
    public void doDelete (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> response = new HashMap<>();
        response.put("code", "OK");
        response.put("user", "");
        response.put("id", "");

        String user = req.getParameter("user");
        String token = req.getParameter("token");
        System.out.println("Parametros: " + user + "; " + token);

        User u = db.findUserByName(user);
        System.out.println("BBDD: " + u.getToken() + " ; " + u.isTokenValid() + " ; " + u.getToken());
        
        if (u != null && u.isTokenValid() && u.getToken().equals(token)) {
            //El usuario  existe y el token es válido
            try {
            	response.put("user", u.getName());
                response.put("id", u.getId());
                db.deleteUser(u);
            } catch (IllegalArgumentException e) {
            	response.put("code", e.getMessage());
            }

        } else {
            response.put("code", "Invalid User or Password");
        }
        
        resp.setContentType("application/json");
        PrintWriter pw = resp.getWriter();
        pw.println(gs.toJson(response));
        pw.close();
    }

    private boolean isValidPassword(String password){
        if (password != null && password.length() >= 4 )
            return true;
        else
            return false;
    }
}
