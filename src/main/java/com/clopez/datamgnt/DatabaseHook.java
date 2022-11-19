package com.clopez.datamgnt;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DatabaseHook {
	private String databaseHost;
	private String port;
	private String Database;
	private Map<String, String[]> methods;
	private Gson gson = new Gson();

	public DatabaseHook(String database) {
		this.Database = database;
		methods = new HashMap<>();
		
		/*
		 * String propsFile = System.getenv("ConfigFile"); if (propsFile == null ||
		 * propsFile.equals("")) { propsFile = "Serverdata.cnf"; }
		 * 
		 * System.out.println("Using config file at " + propsFile);
		 * 
		 * Properties props = new Properties();
		 * 
		 * try (InputStream input = new FileInputStream(propsFile)) { props.load(input);
		 * databaseHost = props.getProperty("databaseHost"); port =
		 * props.getProperty("port"); } catch (IOException ex) { ex.printStackTrace();
		 * System.err.println("Cannot execute Server without properties file"); return;
		 * }
		 */
		databaseHost = "localhost";
		port = "8081";
		
		try {
			JsonObject jo = serverRequest("getMethods", " ");
			JsonElement je = jo.get("methods");
			JsonArray jmethods = je.getAsJsonArray();
			for (JsonElement m : jmethods) {
				JsonObject method = m.getAsJsonObject();
				String methodName = method.get("name").getAsString();
				JsonArray params = method.get("params").getAsJsonArray();
				String[] pars = new String[params.size()];
				for(int i = 0; i<params.size(); i++) {
					pars[i] = params.get(i).getAsString();
			}
			methods.put(methodName, pars);	
			}
		} catch (DatabaseHookException e) {
			System.out.println("Cannot open Database " + e.getMessage());
		}
	}
	
	public JsonObject request (String command, String arg) throws DatabaseHookException {
		JsonObject jo  = new JsonObject();
		
		if ( ! methods.containsKey(command)) {
			jo.addProperty("code", "Invalid Operation for Database " + Database);
			return jo;
		}
		
		try {
			jo = serverRequest(command, arg);
			if (! jo.isJsonObject()) {
				throw new DatabaseHookException("Invalid response from server");
			}
		
			if (! jo.get("code").getAsString().equals("OK")) {
				throw new DatabaseHookException(jo.get("code").getAsString());
			}
		} catch (DatabaseHookException e) {
			System.out.println(e.getMessage());
			throw new DatabaseHookException (e.getMessage());
		}
		
		return jo;
	}

	private JsonObject serverRequest(String command, String arg) throws DatabaseHookException {
		JsonObject jo = new JsonObject();
		HttpRequest request = HttpRequest.newBuilder(
			       URI.create("http://" + databaseHost + ":" + port + "/" + Database + "/" + command))
			   .header("accept", "application/json")
			   .header("Content-Type",  "application/json")
			   .POST(BodyPublishers.ofString(arg)).build();
		
		HttpResponse<String> response;
		try {
			response = HttpClient.newBuilder().build().send(request, BodyHandlers.ofString());
			if (response.statusCode() == 200) {
				jo = new JsonParser().parse(response.body()).getAsJsonObject();
			} else {
				JsonObject jo2 = new JsonParser().parse(response.body()).getAsJsonObject();
				jo.addProperty("code", "Failed Operation. Database returns :" + jo2.get("code").getAsString());
			}
				
		} catch (IOException | InterruptedException e) {
			System.out.println(Arrays.toString(e.getStackTrace()));
			throw new DatabaseHookException("Failed Operation I/O Error. Is database up & running at ...");
		}
		return jo;
	}

}
