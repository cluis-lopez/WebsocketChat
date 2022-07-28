package com.clopez.chat.datamgnt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public abstract class Database <T> {
	HashMap<String, T> data;
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	String datafile;
	
	public Database(String filename, Type type) {
		data = new HashMap<>();
		JsonReader reader;
		this.datafile = filename;
		System.out.println("Using: " + Database.class.getClassLoader().getResource(".").getPath());
		try {
			reader = new JsonReader(new FileReader("webapps/" + filename));
			//Type type = new TypeToken<HashMap<String, T>>() {}.getType();
			data = gson.fromJson(reader, type);
			reader.close();
			System.out.println("La base de datos contiene " + data.size() + " grupos");
		} catch (FileNotFoundException e) {
			System.out.println("Warning: no existe el fichero de datos " + filename);
		} catch (IOException e) {
			System.out.println("Warning: fichero con problemas");
			e.printStackTrace();
		}
	}
	
	public void saveDatabase() {
		try {
			File file = new File("." + File.separator + "webapps" + File.separator + datafile);
			FileWriter fw = new FileWriter(file);
			gson.toJson(data, fw);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean deleteDatabase() {
		File file = new File("webapps/" + datafile);
		System.out.println("El fichero: " + file.getAbsolutePath() + " se va a borrar");
		return file.delete();
	}
	
	public void createItem (String id, T t) throws IllegalArgumentException {
		if (findById(id) != null)
			throw new IllegalArgumentException ("El elemento ya existe en la BD");
		else {
			data.put(id, t);
			saveDatabase();
		}
		
	}

	public void deleteItem(String id) throws IllegalArgumentException {
		if (!data.containsKey(id)) // Invalid user
			throw new IllegalArgumentException("El elemento no existe");
		else {
			data.remove(id);
			saveDatabase();
		}
	}
	
	public T findById(String id) {
		if (data.containsKey(id))
			return data.get(id);
		else
			return null;
	}
}
