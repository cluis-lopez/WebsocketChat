package com.clopez.datamgnt;

import com.google.gson.Gson;

public class Test {

	public static void main(String[] args) {
		DatabaseHook userdb = new DatabaseHook("UserDatabase");
		Gson gson = new Gson();

		User u1 = new User("pajarito2", "1234");
		try {
			System.out.println(userdb.request("createUser", gson.toJson(u1)));
		} catch (DatabaseHookException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
