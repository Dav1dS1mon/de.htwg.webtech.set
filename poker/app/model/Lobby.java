package model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import model.Request;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
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
	
	private final PokerController controller;
	private final String lobbyName;
	private List<User> players = new LinkedList<User>();
	private List<User> offlinePlayers = new LinkedList<User>();
	private Map<User, WebSocket.In<String>> inputChannels = new HashMap<User, WebSocket.In<String>>();
	private Map<User, WebSocket.Out<String>> outputChannels = new HashMap<User, WebSocket.Out<String>>();
	
	public Lobby(String lobbyName) {
		logger.debug("[Lobby:Lobby] Create new Lobby with name '" + lobbyName + "'");
		this.controller = new PokerControllerImp();
		this.lobbyName = lobbyName;
	}
    
    private void initWebSocket(final User player, WebSocket.In<String> in, WebSocket.Out<String> out) {
    	inputChannels.put(player, in);
    	outputChannels.put(player, out);
    	
    	in.onClose(new F.Callback0() {
			@Override
			public void invoke() throws Throwable {
				logger.debug("[Lobby:initSocket] in.onClose called from player " + player.toString());
				playerLeft(player);
				inputChannels.remove(player);
				outputChannels.remove(player);
				logger.debug("[Lobby:initSocket] remaining players in Lobby: " + players.toString());
			}
		});
    	
    	in.onMessage(new F.Callback<String>() {

			@Override
			public void invoke(String request) throws Throwable {
				logger.debug("[Lobby:initSocket] in.onMessage invoked");
				playerRequest(player, request);
			}
		});
    }
    
	private void playerLeft(final User player) {
        offlinePlayers.add(player);
        new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(60000);
					if (!offlinePlayers.contains(player))
						return;
					logger.info(player.toString() + " still offline. Removing from game.");
			        removePlayer(player);
				} catch (InterruptedException consumed) { }
			}
		}).start();
	}
    
	public void addPlayer(User player) {
		logger.debug("[Lobby:addPlayer] Add player '" + player.getName() + "' to lobby '" + this.lobbyName + "'");
		players.add(player);
		updateLobby();
	}
	
    public void removePlayer(User player) {
    	if (containsPlayer(player)) {
    		logger.debug("[Lobby:removePlayer] Remove player '" + player.getName());
    		inputChannels.remove(player);
	    	outputChannels.remove(player);
	    	
	    	offlinePlayers.remove(player);
	    	players.remove(player);
	    	updateLobby();
    	}
    }
    
	public boolean containsPlayer(User player) {
		return players.contains(player);
	}
    
    public void playerRequest(User player, String request) {
		logger.debug("[Lobby:playerResponse] Called with: " + request.getClass().toString() + " text: " + request.toString());
		Request req = new Gson().fromJson(request, Request.class);
    	logger.debug("[Lobby:playerResponse] Called with: " + req.command + " | " + req.value);
    	
    	Response res = new Response();
    	
    	if (req.command.equals("chat")) {
    		res.setCommand("updateChat");
    		res.setChat(player.getName() + ": " + req.value);
    		updateAll(res);
    	} else if (req.command.equals("playField")) {
    		res.setCommand("updatePlayField");
    		res.setGameField(controller);
    		updateAll(res);
    	}
    }
    
    public void updateLobby() {
    	Response res = new Response();
    	res.setCommand("updateLobby");
    	res.setLobbyPlayerList(players);
    	
    	updateAll(res);
    }
    
    public void updateAll(Response response) {
    	int count = 0;
		for (Out<String> channel : outputChannels.values()) {
			count++;
			try {
				channel.write(response.asJson());
			} catch (Exception e) {
				logger.debug("[Lobby:updateAll] ERROR: Could not convert response to Json: " + e.getMessage());
			}
			
		}
		logger.debug("[Lobby:updateAll] updateAll was sent to " + count + " clients of (out) " + outputChannels.size());
    }

	public WebSocket<String> getSocketForPlayer(final User player) {
		logger.debug("[Lobby:getSocketForPlayer] getSocketForPlayer called. Return new socket for player '" + player.getName());
		if (offlinePlayers.contains(player)) {
				logger.debug("[Lobby:getSocketForPlayer] '" + player.getName() + "' rejoined the game");
				offlinePlayers.remove(player);
		}
		return new WebSocket<String>() {
			@Override
			public void onReady(final In<String> in, final Out<String> out) {
				logger.debug("[Lobby:getSocketForPlayer] onReady called with player '" + player.getName());
				initWebSocket(player, in, out);
	    		updateLobby();
			}
		};
	}
	
	public String getPlayer() {
		StringBuilder sb = new StringBuilder();
		for (User p : players) {
			sb.append(p.toString() + ";");
		}
		return sb.toString();
	}    
}
