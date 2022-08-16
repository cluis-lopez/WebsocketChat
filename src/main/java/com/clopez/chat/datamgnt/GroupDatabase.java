package com.clopez.chat.datamgnt;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GroupDatabase extends SimpleJsonDatabase<Group>{
	
	public GroupDatabase (String filename, Type type) {
		super (filename, type);
	}

	public void createGroup(Group g) throws IllegalArgumentException {
		if (findGroupByName(g.getName()) == null)
			createItem(g.getId(), g);
		else
			throw new IllegalArgumentException("Duplicated Group Name");
	}
	
	public void deleteGroup(Group g) {
		deleteItem(g.getId());
	}
	
	public boolean addmember(String id, User u) {
		boolean ret = false;
		Group g = data.get(id);
		try {
			g.addMember(u);
			saveDatabase();
			ret = true;
		} catch (IllegalArgumentException e) {
			System.out.println(e);
			ret = false;
		}
		return ret;
	}
	
	public boolean removeMember(String id, User u) {
		boolean ret = false;
		Group g = data.get(id);
		try {
			g.removeMember(u);
			ret = true;
			saveDatabase();
		} catch (IllegalArgumentException e) {
			System.out.println(e);
			ret = false;
		}
		return ret;
	}

	public Group findGroupByName(String name) {
		Group g;
		for (String id : data.keySet()) {
			g = data.get(id);
			if (g.getName().equals(name))
				return g;
		}
		return null;
	}
	
	public List<Group> findByOwner(User u) {
		List <Group> groups = new ArrayList<>();
		Group g;
		for (String id : data.keySet()) {
			g = data.get(id);
			if (g.getOwner().equals(u.getName()))
				groups.add(g);		
		}
		return groups;
	}
	
	public List<Group> findByUser(User u){
		List <Group> groups = new ArrayList<>();
		Group g;
		for (String id : data.keySet()) {
			g = data.get(id);
			if (g.isMember(u))
				groups.add(g);
		}
		return groups;
	}
}
