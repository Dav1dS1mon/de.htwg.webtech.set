package model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import de.htwg.se.texasholdem.controller.PokerController;
import service.User;

public class Response {
	private HashMap<String, Object> data; 
	
	public Response() {
		data = new HashMap<String, Object>();
	}

	public void setCommand(String command) {
		data.put("command", command);
	}
	
	public void setLobbyPlayerList(Map<User, Boolean> players) {
		Map<String, Boolean> readyList = new HashMap<String, Boolean>();
		for (Entry<User, Boolean> entry : players.entrySet()) {
			readyList.put(entry.getKey().toString(), entry.getValue());
		}
		data.put("value", readyList);
	}
	
	public void setGameField(PokerController controller, User player) {
		PlayField gameField = new PlayField(controller, player);
		data.put("value", gameField);
	}
	
	public String asJson() {
		return new Gson().toJson(data);
	}

	public void setChat(String message) {
		data.put("value", message);
	}
}