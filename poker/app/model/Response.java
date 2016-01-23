package model;

import java.util.HashMap;
import java.util.LinkedList;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import service.User;

public class Response {
	private HashMap<String, Object> data; 
	
	public Response() {
		data = new HashMap<String, Object>();
	}

	public void setCommand(String command) {
		data.put("command", command);
	}
	
	public void setLobbyPlayerList(Iterable<User> players) {
		LinkedList<String> valueList = new LinkedList<String>();
		for (User player : players)
			valueList.add(player.getName());
		data.put("value", valueList);
	}
	
	public String asJson() {
		return new Gson().toJson(data);
	}

	public void setChat(String message) {
		data.put("value", message);
	}
}