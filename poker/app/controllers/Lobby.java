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
	
	Map<User, WebSocket<String>> players = new HashMap<User, WebSocket<String>>();
	Map<User, WebSocket.In<String>> inputChannels = new HashMap<User, WebSocket.In<String>>();
	Map<User, WebSocket.Out<String>> outputChannels = new HashMap<User, WebSocket.Out<String>>();
	
	public Lobby(String lobbyName) {
		logger.debug("[Lobby:Lobby] Create new Lobby with name '" + lobbyName + "'");
		this.controller = new PokerControllerImp();
		this.lobbyName = lobbyName;
	}

	public void addPlayer(User player) {
		logger.debug("[Lobby:addPlayer] Add player to lobby '" + this.lobbyName + "'");
		players.put(player, getSocketForPlayer(player));
	}
    
    private void initSocket(final User player, WebSocket.In<String> in, WebSocket.Out<String> out) {
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
    	
    	in.onMessage(new F.Callback<String>() {

			@Override
			public void invoke(String request) throws Throwable {
				logger.debug("[Lobby:initSocket] in.onMessage invoked. Message: " + request);
				//playerRequest(request);
				updateAll(request);
			}
    		
		});
    }
    
    private void userLeftRoom(final User user) {
    	inputChannels.remove(user);
    	outputChannels.remove(user);
    	players.remove(user);
    }
    
    public void playerRequest(String request) {
    	/* TODO: 	Check for Command of Request
    	 *			'check' and other ones
    	 */
    	
    }
    
    public void updateAll(String request) {
    	int count = 0;
		for (Out<String> channel : outputChannels.values()) {
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

	public WebSocket<String> getSocketForPlayer(final User player) {
		logger.debug("[Lobby:getSocketForPlayer] getSocketForPlayer called. Returnin new socket for player.");
		return new WebSocket<String>() {
			@Override
			public void onReady(final In<String> in, final Out<String> out) {
				initSocket(player, in, out);
			}
		};
	}
	
	public String getPlayerNameBySocket(WebSocket ws) {
		for (Entry<User, WebSocket<String>> entry  : players.entrySet()) {
			if(entry.getValue() == ws) {
				return entry.getKey().main.toString();
			}
		}
		return lobbyName;
	}

	public String getPlayer() {
		StringBuilder sb = new StringBuilder();
		for (Entry<User, WebSocket<String>> entry  : players.entrySet()) {
			sb.append(entry.getKey().getName() + " ");
		}
		return sb.toString();
	}
    
}
