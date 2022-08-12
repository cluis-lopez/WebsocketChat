package com.clopez.chat.datamgnt;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Group {

    private String id;
    private String name;
    private User owner;
    private Set<User> users;
    private Date creationDate;

    public Group(String name, User owner) {
        this.owner = owner;
        this.name = name;
        this.id = UUID.randomUUID().toString();
        this.users = new HashSet<>();
        this.users.add(owner);
        this.creationDate = new Date();
    }
    
    public String getName() {
    	return name;
    }
    
    public String getId() {
    	return id;
	}
    
    public User getOwner() {
    	return owner;
    }
    
    public Set<User> getUsers(){
    	return users;
    }
    
    public int getNumMembers() {
    	return users.size();
    }

    protected void addMember(User user) throws IllegalArgumentException {
    	//UserDatabase db = new UserDatabase("usersdb");
        if (user != null) // && db.findUserByName(user.getName()) != null)
            users.add(user);
        else
            throw new IllegalArgumentException("Invalid user");
    }

    protected void removeMember(User user) throws IllegalArgumentException {
    	if (user != null && user == owner)
    		throw new IllegalArgumentException("No se puede eliminar al propietario del grupo");
        if (user != null && isMember(user))
            users.remove(user);
        else
            throw new IllegalArgumentException("Invalid user");
    }

    protected boolean isMember(User user) {
        return users.contains(user);
    }
}
