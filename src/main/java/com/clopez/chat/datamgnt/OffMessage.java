package com.clopez.chat.datamgnt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OffMessage {
	private List<Message> offMessages;
	
	public OffMessage () {
		offMessages = new ArrayList<>();
	}
	
	public OffMessage(Message m) {
		offMessages = new ArrayList<>();
		offMessages.add(m);
	}
	
	public void addMessage(Message m) {
		offMessages.add(m);
	}
	
	public List<Message> getMessages(){
		return offMessages;
	}
	
	public List<Message> retrieveUndelivered() {
		List<Message> l = new ArrayList<>();
		for (Message m : offMessages) {
			if (! m.isDelivered())
				l.add(m);
		}
		return l;
	}
	
	public int removeDelivered() {
		List<Message> temp = new ArrayList<>();
		for(Message m: offMessages) {
			if (! m.isDelivered())
				temp.add(m);
		}
		int ret = offMessages.size() - temp.size();
		offMessages = temp;
		return ret;
	}
	
	public int size() {
		return offMessages.size();
	}
}
