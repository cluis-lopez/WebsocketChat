package com.clopez.chat.datamgnt;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class UserDatabase extends SimpleJsonDatabase<User> {
    
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
    
    public List<User> findUserByWildCar(String wc) {
    	List<User> lu = new ArrayList<>();
    	User u;
    	for (String id : data.keySet()) {
    		u = data.get(id);
    		if (u.getName().toLowerCase().contains(wc))
    			lu.add(u);
    	}
    	return lu;
    }
}
