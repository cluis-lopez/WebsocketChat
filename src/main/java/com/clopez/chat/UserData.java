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
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;


@WebServlet("/UserData")
public class UserData extends HttpServlet {
	private static final long serialVersionUID = 1L;
	Type type = new TypeToken<HashMap<String, User>>() {}.getType();
    UserDatabase userdb = new UserDatabase("usersdb", type);
    Gson gs = new Gson();
       
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userid = req.getParameter("id");
        String token = req.getParameter("token");
        Map<String, String> response = new HashMap<String, String>();
        String recentChats;
        
        User user = userdb.findById(userid);
        if (user != null && token.equals(user.getToken()) && user.isTokenValid()) {
        	response.put("code", "OK");
        	int maxNumber = 5;
        	try {
        		maxNumber = Integer.parseInt(req.getParameter("maxNumber"));
        		if (maxNumber > 25)
        			maxNumber = 25;
        	} catch (NumberFormatException e) {
        		System.out.println("Parámetro inválido");
        	}
        	String [] showUsers = new String[maxNumber];
        	String [] temp = user.getRecentChats();
        	for (int i=0; i<maxNumber; i++) {
        		showUsers[i] = temp[i];
        	}
        	recentChats = gs.toJson(temp);
        	response.put("recentChats", recentChats);
        } else {
        	response.put("code", "Invalid user or token");
        }
        
        
        resp.setContentType("application/json");
        PrintWriter pw = resp.getWriter();
        pw.println(gs.toJson(response));
        pw.close();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
