package com.clopez.chat.datamgnt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public class GroupDatabase extends SimpleJsonDatabase<Group>{
	
	public GroupDatabase (String filename, Type type) {
		super (filename, type);
	}

	public void createGroup(Group g) {
		createItem(g.getId(), g);
	}
	
	public void deleteGroup(Group g) {
		deleteItem(g.getId());
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
	
	public List<Group> findByOwner(User o) {
		List <Group> groups = new ArrayList<>();
		Group g;
		for (String id : data.keySet()) {
			g = data.get(id);
			if (g.getOwner() == o)
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
