package com.clopez.chat.messages;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class MessageDatabase {
	private static final int MAXFILES = 50;
	private static final int MAXMESSAGESFILE = 500;
	private static final String FILEPREFIX = "messages_";
	private static final String MESSAGESDIR = "webapps/messages/";
	
	private Path currentDataFile;
	private int currentNumMessages;
	private Gson gson;
	private Map<Integer, List<Message>> mapById;

	public MessageDatabase() {
		this.currentNumMessages = 0;
		gson = new Gson();
		mapById = new HashMap<>();
		currentDataFile = getLastFile();
		loadFromFile(currentDataFile);
	}
	
	private void loadFromFile(Path filePath) {
		System.out.println("Trying :" + filePath.toAbsolutePath());
		Message m;
		try (BufferedReader br = Files.newBufferedReader(filePath)){
		    String line;
		    while ((line = br.readLine()) != null) {
		    	try {
		    		m = gson.fromJson(line, new TypeToken<Message>() {}.getType());
		    		mapMessageFromChatId(m);
		    	}catch (JsonIOException | JsonSyntaxException f) {
		   			System.out.println("Malformatted line");
		   		}
		   	}
		    System.out.println("Readed: " + getNumMessages() + " messages in " + getNumChats()+ " chats");
		} catch (IOException e) {
			System.out.println("No existe la BBDD. Creamos el primer fichero");
		}
	}
	
	public boolean addMessage(Message m) {
		if (currentNumMessages > MAXMESSAGESFILE) {
			rotateFiles();
			currentNumMessages = 0;
		}
		currentNumMessages += 1;
		try (BufferedWriter writer = Files.newBufferedWriter(getLastFile(), StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
		    writer.write(m.getJson() + System.lineSeparator());
		    writer.flush();
		} catch (IOException ioe) {
		    System.out.format("Cannot write: IOException: %s%n", ioe);
		    return false;
		}
		
		mapMessageFromChatId(m);
		return true;
	}
	
	public List<Message> getChatById(int id){
		return mapById.get(id);
	}
	
	public List<Message> getPendingMessagesTo(String userTo){
		List<Message> li = new ArrayList<>();
		for (List<Message> temp : mapById.values())
			for (Message m : temp)
				if (m.getTo().equals(userTo) && !m.isDelivered())
					li.add(m);
		return li;
	}
	
	public int getNumMessages() {
		int ret = 0;
		for(int i: mapById.keySet())
			ret += mapById.get(i).size();
		return ret;
	}
	
	public int getNumChats() {
		return mapById.size();
	}
		
	private void mapMessageFromChatId (Message m) {
		if (mapById.containsKey(m.getChatId()))
			mapById.get(m.getChatId()).add(m);
		else {
			List<Message> l = new ArrayList<>();
			l.add(m);
			mapById.put(m.getChatId(), l);
		}
	}
	
	private Path getLastFile() {
		File ls[] = new File(MESSAGESDIR).listFiles();
		return Paths.get(MESSAGESDIR + FILEPREFIX + getLastIndex(ls));
	}
	
	private int getLastIndex(File ls[]) {
		int subindex = FILEPREFIX.length();
		int j; 
		int maxIndex = 0;
		for (int i=0; i<ls.length; i++) {
			try {
				j = Integer.parseInt(ls[i].getName().substring(subindex));
				if (j > maxIndex)
					maxIndex = i;
			} catch (NumberFormatException | IndexOutOfBoundsException e) {
				continue;
			}
					
		}
		return maxIndex;
	}
		
	private void rotateFiles() throws IllegalArgumentException {
		File ls[] = new File(MESSAGESDIR).listFiles();
		int lastIndex = getLastIndex(ls) + 1;
		if (lastIndex > MAXFILES)
			currentDataFile = Paths.get(FILEPREFIX + "0");
		else
			currentDataFile = Paths.get(FILEPREFIX + lastIndex);		
	}
	
}
