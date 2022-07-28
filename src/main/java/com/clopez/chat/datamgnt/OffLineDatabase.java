package com.clopez.chat.datamgnt;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class OffLineDatabase extends Database<OffMessage> {

	public OffLineDatabase (String filename, Type type) {
		super (filename, type);
	}

	public void addMessage(Message m) {
		if (data.containsKey(m.getTo()))
			data.get(m.getTo()).addMessage(m);
		else {
			OffMessage om = new OffMessage(m);
			data.put(m.getTo(), om);
		}
		saveDatabase();
	}
	
	public void deleteEntriesForUser(User userTo) {
		deleteItem(userTo.getName());
	}
	
	public int deleteDeliveredForUser(User u) {
		int ret = 0;
		if (data.containsKey(u.getName())) {
			ret = data.get(u.getName()).removeDelivered();
			if (data.get(u.getName()).size() == 0)
				deleteItem(u.getName());
		}
		saveDatabase();
		return ret;
	}
	
	public List<Message> messagesOfUser(User u){
		List<Message> l = new ArrayList<>();
		if (data.containsKey(u.getName())) {
			l = data.get(u.getName()).getMessages();
		}
		return l;
	}
	
	public int numberOfUsers() {
		return data.size();
	}
	
	public int numberOfMessages() {
		int i = 0;
		for (String s : data.keySet()) {
			i += data.get(s).size();
		}
		return i;
	}
}
