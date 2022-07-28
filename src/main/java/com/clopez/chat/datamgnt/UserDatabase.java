package com.clopez.chat.datamgnt;

import java.lang.reflect.Type;


public class UserDatabase extends Database<User> {
    
	public UserDatabase(String filename, Type type) { 
    	super (filename, type);
    }

    public void createUser(User u) {
    	createItem(u.getId(), u);
    }

    public void deleteUser(User u) {
    	deleteItem(u.getId());
    }

    public User findUserByName(String name){
        User u;
        for (String id : data.keySet()){
            u = data.get(id);
            if (u.getName().equals(name))
                return u;
        }
        return null;
    }
}
