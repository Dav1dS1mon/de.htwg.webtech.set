package controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;

import de.htwg.se.texasholdem.controller.PokerController;
import de.htwg.se.texasholdem.controller.imp.PokerControllerImp;
import play.Logger;
import play.libs.F;
import play.libs.F.Callback;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import play.mvc.WebSocket.Out;
import securesocial.core.RuntimeEnvironment;
import service.User;

public class Lobby extends Controller {
	public static Logger.ALogger logger = Logger.of("application.controllers.Lobby");
	
	PokerController controller;
	String lobbyName;
	
	Map<User, WebSocket<JsonNode>> players = new HashMap<User, WebSocket<JsonNode>>();
	Map<User, WebSocket.In<JsonNode>> inputChannels = new HashMap<User, WebSocket.In<JsonNode>>();
	Map<User, WebSocket.Out<JsonNode>> outputChannels = new HashMap<User, WebSocket.Out<JsonNode>>();
	
	public Lobby(String lobbyName) {
		logger.debug("[Lobby:Lobby] Create new Lobby with name '" + lobbyName + "'");
		this.controller = new PokerControllerImp();
		this.lobbyName = lobbyName;
	}

	public void addPlayer(User player) {
		logger.debug("[Lobby:addPlayer] Add player to lobby '" + this.lobbyName + "'");
		players.put(player, getSocketForPlayer(player));
	}
    
    private void initSocket(final User player, WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
    	inputChannels.put(player, in);
    	outputChannels.put(player, out);
    	
    	in.onClose(new F.Callback0() {
			@Override
			public void invoke() throws Throwable {
				logger.debug("[Lobby:initSocket] in.onClose called from player " + player.toString());
				userLeftRoom(player);
				logger.debug("[Lobby:initSocket] remaining players in Lobby: " + players.toString());
			}
		});
    	
    	in.onMessage(new F.Callback<JsonNode>() {

			@Override
			public void invoke(JsonNode request) throws Throwable {
				logger.debug("[Lobby:initSocket] in.onMessage invoked. Message: " + request.toString());
				playerRequest(request);
			}
		});
    }
    
    private void userLeftRoom(final User user) {
    	inputChannels.remove(user);
    	outputChannels.remove(user);
    	players.remove(user);
    }
    
    public void playerRequest(JsonNode request) {
    	String command = request.get("command").textValue();
    	
    	switch(command) {
    	case "chat":
    		updateAll(request);
    		break;
    	}
    }
    
    public void updateAll(JsonNode request) {
    	int count = 0;
		for (Out<JsonNode> channel : outputChannels.values()) {
			count++;
			channel.write(request);
		}
		logger.debug("[Lobby:updateAll] updateAll was sent to " + count + " clients of (out) " + outputChannels.size());
    }

	public boolean containsPlayer(User player) {
		boolean check = players.containsKey(player);
		logger.debug("[Lobby:containsPlayer] All Players: " + players.toString());
		logger.debug("[Lobby:containsPlayer] Searched Player: " + player.toString());
		logger.debug("[Lobby:containsPlayer] containsPlayer called. Returning '" + check + "'");
		return check;
	}

	public WebSocket<JsonNode> getSocketForPlayer(final User player) {
		logger.debug("[Lobby:getSocketForPlayer] getSocketForPlayer called. Returnin new socket for player.");
		return new WebSocket<JsonNode>() {
			@Override
			public void onReady(final In<JsonNode> in, final Out<JsonNode> out) {
				initSocket(player, in, out);
			}
		};
	}
	
	public String getPlayerNameBySocket(WebSocket<JsonNode> ws) {
		for (Entry<User, WebSocket<JsonNode>> entry  : players.entrySet()) {
			if(entry.getValue() == ws) {
				return entry.getKey().main.toString();
			}
		}
		return lobbyName;
	}

	public String getPlayer() {
		StringBuilder sb = new StringBuilder();
		for (Entry<User, WebSocket<JsonNode>> entry  : players.entrySet()) {
			sb.append(entry.getKey().getName() + ";");
		}
		return sb.toString();
	}
    
}
