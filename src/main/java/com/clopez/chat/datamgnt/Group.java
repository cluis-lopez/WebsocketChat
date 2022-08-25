package com.clopez.chat.datamgnt;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.google.gson.reflect.TypeToken;

public class Group {

    private String id;
    private String name;
    private String owner;
    private Set<String> users;
    private Date creationDate;

    public Group(String name, User owner) {
        this.owner = owner.getName();
        this.name = name;
        this.id = UUID.randomUUID().toString();
        this.users = new HashSet<>();
        this.creationDate = new Date();
    }
    
    public String getName() {
    	return name;
    }
    
    public String getId() {
    	return id;
	}
    
    public String getOwner() {
    	return owner;
    }
    
    public Set<String> getUsers(){
    	return users;
    }
    
    public int getNumMembers() {
    	return users.size();
    }

    protected void addMember(User user) throws IllegalArgumentException {
    	Type type = new TypeToken<HashMap<String, User>>() {}.getType();
    	UserDatabase db = new UserDatabase("usersdb", type);
        if (user != null && db.findUserByName(user.getName()) != null)
            users.add(user.getName());
        else
            throw new IllegalArgumentException("Invalid user");
    }

    protected void removeMember(User user) throws IllegalArgumentException {
    	if (user != null && user.getName().equals(owner))
    		throw new IllegalArgumentException("No se puede eliminar al propietario del grupo");
        if (user != null && isMember(user))
            users.remove(user.getName());
        else
            throw new IllegalArgumentException("Invalid user");
    }

    protected boolean isMember(User user) {
        return users.contains(user.getName());
    }
}
