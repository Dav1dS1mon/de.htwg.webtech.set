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

import de.htwg.se.texasholdem.controller.GameStatus;
import de.htwg.se.texasholdem.controller.PokerController;
import de.htwg.se.texasholdem.controller.imp.PokerControllerImp;
import play.Logger;
import play.libs.F;
import play.libs.F.Callback;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.WebSocket;
import play.mvc.WebSocket.Out;
import securesocial.core.RuntimeEnvironment;
import service.User;

public class Lobby extends Controller {
	public static Logger.ALogger logger = Logger.of("application.controllers.Lobby");
	
	private final PokerController controller;
	private final String lobbyName;
	//private List<User> players = new LinkedList<User>();
	private Map<User, Boolean> players = new HashMap<User, Boolean>();
	private List<User> offlinePlayers = new LinkedList<User>();
	private Map<User, WebSocket.In<String>> inputChannels = new HashMap<User, WebSocket.In<String>>();
	private Map<User, WebSocket.Out<String>> outputChannels = new HashMap<User, WebSocket.Out<String>>();
	private boolean gameStarted = false;
	
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
		players.put(player, false);
		updateLobby();
	}
	
    public void removePlayer(User player) {
    	if (containsPlayer(player)) {
    		logger.debug("[Lobby:removePlayer] Remove player '" + player.getName());
    		inputChannels.remove(player);
	    	outputChannels.remove(player);
	    	
	    	offlinePlayers.remove(player);
	    	players.remove(player);
	    	
	    	if (controller.getStatus() == GameStatus.RUNNING) {
    			controller.removePlayer(player.toString());
	    	}
	    	
	    	updateLobby();
	    	playFieldChanged();
    	}
    }
    
	public boolean containsPlayer(User player) {
		return players.containsKey(player);
	}
    
    public void playerRequest(User player, String request) {
		logger.debug("[Lobby:playerResponse] Called with: " + request.getClass().toString() + " text: " + request.toString());
		Request req = new Gson().fromJson(request, Request.class);
		if (!req.value.equals(""))
		{
			logger.debug("[Lobby:playerResponse] Called with: " + req.command + " | " + "blubb");
		} else {
			logger.debug("[Lobby:playerResponse] Called with: " + req.command);
		}
    	
    	Response res = new Response();
    	
    	if (req.command.equals("chat")) {
    		res.setCommand("updateChat");
    		res.setChat(player.toString() + ": " + req.value);
    		updateAll(res);
    	} else if (req.command.equals("playField") && gameIsRunning()) {
    		updatePlayField(res, player);
    		
    	} else if (req.command.equals("ready") && gameIsInitializing()) {
    		if (req.value.equals("true")) {
    			players.put(player, true);
    		} else if (req.value.equals("false")) {
    			players.put(player, false);
    		}
    		if (gameIsInitializing()) {
    			updateLobby();
    			checkForGameStart();
    		}
    		
    	} else if (req.command.equals("raise")) {
			int raiseValue = Integer.valueOf(req.value);
			if (raiseValue >= 0 && isCurrentPlayer(player) && gameIsRunning()) {
				controller.raise(raiseValue);
				playFieldChanged();
			} else {
				// Player can not perform action
			}
			
		} else if (req.command.equals("call") || req.command.equals("check")) {
			boolean a = (isCurrentPlayer(player));
			logger.debug("[Lobby:playerResponse] call, player: " + player.getName() + " | " + a);
			if (isCurrentPlayer(player) && gameIsRunning()) {
				logger.debug("[Lobby:playerResponse] call, player: " + player.getName());
				controller.call();
				playFieldChanged();
			} else {
				// Player can not perform action
			}
		} else if (req.command.equals("fold")) {
			if (isCurrentPlayer(player) && gameIsRunning()) {
				controller.fold();
				playFieldChanged();
			} else {
				// Player can not perform action
			}
		}
    }
    
    private boolean gameIsInitializing() {
    	return controller.getStatus() == GameStatus.INITIALIZATION;
    }
    
    private boolean gameIsRunning() {
    	return controller.getStatus() == GameStatus.RUNNING;
    }
    
    private boolean isCurrentPlayer(User player) {
    	return controller.getCurrentPlayer().getPlayerName().equals(player.toString());
    }

	private void checkForGameStart() {
		boolean ready = true;
		for (Entry<User, Boolean> entry : players.entrySet()) {
			if (entry.getValue() == false) {
				ready = false;
			}
		}
		
		if(ready && controller.getStatus() == GameStatus.INITIALIZATION && players.size() >= 2) {
			for (Entry<User, Boolean> entry : players.entrySet()) {
				controller.addPlayer(entry.getKey().toString());
			}
			controller.startGame();
			gameStarted = true;
			playFieldChanged();
		}
	}

	private void playFieldChanged() {
		Response res = new Response();
		res.setCommand("playFieldChanged");
		
		updateAll(res);
	}

	private void updatePlayField(Response res, User player) {
		res.setCommand("updatePlayField");
		res.setGameField(controller, player);
		
		update(res, player);
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
    
    public void update(Response response, User player) {
		try {
			outputChannels.get(player).write(response.asJson());
		} catch (Exception e) {
			logger.debug("[Lobby:updateAll] ERROR: Could not convert response to Json: " + e.getMessage());
		}		
		logger.debug("[Lobby:updateAll] update was sent to " + player.getName());
    }

	public WebSocket<String> getSocketForPlayer(final User player) {
		logger.debug("[Lobby:getSocketForPlayer] getSocketForPlayer called. Return new socket for player '" + player.getName());
		if (offlinePlayers.contains(player)) {
				logger.debug("[Lobby:getSocketForPlayer] '" + player.getName() + "' rejoined the game");
				offlinePlayers.remove(player);
		} else if (players.size() >= 8) {
			return WebSocket.reject(Results.badRequest("Lobby is already full."));
		}
		return new WebSocket<String>() {
			@Override
			public void onReady(final In<String> in, final Out<String> out) {
				logger.debug("[Lobby:getSocketForPlayer] onReady called with player '" + player.getName());
				initWebSocket(player, in, out);
	    		updateLobby();
	    		if (gameStarted) {
	    			Response res = new Response();
	    			res.setCommand("playFieldChanged");
	    			
	    			update(res, player);
	    		}
			}
		};
	}

	public int getPlayerCount() {
		return players.size();
	}

	public boolean gameStarted() {
		return this.gameStarted;
	}  
	
//	public Map<String, String> getPlayerNameIdPair() {
//		Map<String, String> nameIdPair = new HashMap<String, String>();
//		
//		for (Entry<User, Boolean> entry : players.entrySet()) {
//			nameIdPair.put(entry.getKey().getId(), entry.getKey().getName());
//		}
//		
//		return nameIdPair;
//	}
//	
//	public String getPlayerNameForId (String id) {
//		for (Entry<User, Boolean> entry : players.entrySet()) {
//			if (entry.getKey().getId().equals(id)) {
//				return entry.getKey().getName();
//			}
//		}
//		return "";
//	}
}
