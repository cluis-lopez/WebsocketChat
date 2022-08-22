package com.clopez.chat;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.clopez.chat.datamgnt.User;
import com.clopez.chat.datamgnt.UserDatabase;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;


@WebServlet("/Userdata")
public class UserData extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Type type = new TypeToken<HashMap<String, User>>() {}.getType();
    private static UserDatabase userdb = new UserDatabase("usersdb", type);
       
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userid = req.getParameter("id");
        String token = req.getParameter("token");
        String command = req.getParameter("command");
        String searchChats = req.getParameter("searchChats");
        String searchUsers = req.getParameter("searchUsers");
        JsonObject job = new JsonObject();
        
        User user = userdb.findById(userid);

        if (user != null && token != null && token.equals(user.getToken()) && user.isTokenValid() && command != null) {
        	job.addProperty("code", "OK");
        	int maxNumber = 5;
        	try {
        		maxNumber = Integer.parseInt(req.getParameter("maxNumber"));
        		if (maxNumber > 25)
        			maxNumber = 25;
        	} catch (NumberFormatException e) {
        		System.out.println("Parámetro inválido o no especificado");
        	}
        	
        	if (command.equals("lastChats")) {
        		JsonArray jarr = new JsonArray();
        		String [] temp = user.getRecentChats();
        		int min = (maxNumber<temp.length? maxNumber: temp.length);
        		for (int i=0; i<min; i++)
        			jarr.add(temp[i]);
        		job.add("chatList", jarr);
        	} else if (command.equals("searchChats") && searchChats!= null && ! searchChats.equals("")) {
        		//OJO... rebuild para buscar grupos también
        		List<User> lu = userdb.findUserByWildCar(searchChats);
        		System.out.println("Encontrados "+lu.size()+" usuarios/grupos: " + lu.toString());
        		JsonArray jarr = new JsonArray();
        		int min = (maxNumber<lu.size()? maxNumber: lu.size());
        		for (int i=0; i<min; i++)
        			jarr.add(lu.get(i).getName());
        		job.add("chatList", jarr);
        	} else if (command.equals("searchUsers") && searchUsers!= null && ! searchUsers.equals("")) {
        		List<User> lu = userdb.findUserByWildCar(searchUsers);
        		System.out.println("Encontrados "+lu.size()+" usuarios/grupos: " + lu.toString());
        		JsonArray jarr = new JsonArray();
        		int min = (maxNumber<lu.size()? maxNumber: lu.size());
        		for (int i=0; i<min; i++)
        			jarr.add(lu.get(i).getName());
        		job.add("chatList", jarr);
        		job.addProperty("code", "Invalid command");
        	}
        } else {
        	job.addProperty("code", "Invalid user or token");
        }
        
        
        resp.setContentType("application/json");
        PrintWriter pw = resp.getWriter();
        pw.println(job);
        pw.close();
	}

}
