package com.clopez.chat.datamgnt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.websocket.Session;

import com.google.gson.Gson;

public class Message {
	
	private String type;
	private String mid;
	private String to;
	private String from;
	private String content;
	private boolean delivered;
	
	public Message(String type, String mid, String to, String from, String content) {
		this.type = type;
		this.mid = mid;
		this.to = to;
		this.from = from;
		this.content = content;
		this.delivered = false;
	}
	
	public Message (Map<String, String> jsonPayload) {
        this.type = "TEXT";
        this.mid = jsonPayload.get("id");
        this.to = jsonPayload.get("to");
        this.from = jsonPayload.get("from");
        this.content = jsonPayload.get("content");
        this.delivered = false;
	}
	
	public boolean send(Session sid) {
		boolean ret = false;
		Gson gson = new Gson();
		try {
			sid.getBasicRemote().sendText(gson.toJson(this));
			this.delivered = true;
			ret = true;
		} catch (IOException e) {
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}
	
	public String getTo() {
		return to;
	}
	
	public boolean isDelivered() {
		return this.delivered;
	}
	
	public void setDelivered() {
		this.delivered = true;
	}
	
}
