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

@WebServlet("/Login")
public class Login extends HttpServlet {

    public void doPost (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    	Type type = new TypeToken<HashMap<String, User>>() {}.getType();
        UserDatabase users = new UserDatabase("usersdb", type);
        Gson gs = new Gson();

        String user =req.getParameter("user");
        String password = req.getParameter("password");
        Map<String, String> response = new HashMap<String, String>();
        response.put("code", "OK");
        response.put("user", "");
        response.put("id", "");
        response.put("token", "");
        

        User u = users.findUserByName(user);

        if (u != null)
            if (u.passwordMatch(password)){
                response.put("user", u.getName());
                response.put("id", u.getId());
                response.put("token", u.getToken());
            }
            else {
                response.put("code", "Invalid Password");
            }
        else {
            response.put("code", "Invalid User");
        }
            
        System.out.println ("Respuesta login:  " + gs.toJson(response));
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("cache-control", "no-cache");
        resp.getWriter().write(gs.toJson(response));
        resp.flushBuffer();
    }
}
