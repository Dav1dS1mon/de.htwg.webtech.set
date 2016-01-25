package controllers;

import com.google.inject.Inject;

import de.htwg.se.texasholdem.controller.imp.PokerControllerImp;
import model.Lobby;
import de.htwg.se.texasholdem.controller.PokerController;
import play.Logger;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.WebSocket;
import securesocial.core.BasicProfile;
import securesocial.core.RuntimeEnvironment;
import securesocial.core.java.SecureSocial;
import securesocial.core.java.SecuredAction;
import securesocial.core.java.UserAwareAction;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import service.User;

import views.html.*;

public class LobbyController extends Controller {
    public static Logger.ALogger logger = Logger.of("application.controllers.LobbyController");
    private RuntimeEnvironment env;
    private static final String DEFAULT_LOBBY_NAME = "public";
    
    private static Map<String, Lobby> lobbys = new HashMap<String, Lobby>(); 

    /**
     * A constructor needed to get a hold of the environment instance.
     * This could be injected using a DI framework instead too.
     *
     * @param env
     */
    @Inject()
    public LobbyController (RuntimeEnvironment env) {
        this.env = env;
    }
	
	@SecuredAction
	public Result getLobbys() {
		String lobbyString;
		synchronized (lobbys) {
			List<String> lobbyList = new ArrayList<String>(lobbys.keySet());
			lobbyString = new Gson().toJson(lobbyList);
			
			
		}
		return ok(lobbyString);
	}
	
	@SecuredAction
	public Result play(String lobbyName) {
		logger.debug("[LobbyController:play] Play function called");
		if (lobbyName.equals("")) {
			lobbyName = DEFAULT_LOBBY_NAME;
		}
		
		User player = (User) ctx().args.get(SecureSocial.USER_KEY);
		
		synchronized (lobbys) {
			// Check if Player is already in other lobby
			for (Entry<String, Lobby> entry  : lobbys.entrySet()) {
				if (entry.getValue().containsPlayer(player) && !(entry.getKey().equals(lobbyName))) {
					entry.getValue().removePlayer(player);
				}
			}
			
			logger.debug("[LobbyController:play] All lobbys: " + lobbys.toString());
			// Remove empty lobbys
			for (Entry<String, Lobby> entry : lobbys.entrySet()) {
				logger.debug("[LobbyController:play] Lobby: " + entry.getKey() + " count players: " + entry.getValue().getPlayerCount());				
				if (entry.getValue().getPlayerCount() == 0) {
					logger.debug("!!!" + lobbys.toString());
					lobbys.remove(entry.getKey());
					logger.debug("!!!" + lobbys.toString());
				}
			}
			
			
			
			if (!lobbys.containsKey(lobbyName)) {
				// Lobby does not exist
				logger.debug("[LobbyController:play] Lobby '" + lobbyName + "' does not exist. Creating new one.");
				lobbys.put(lobbyName, new Lobby(lobbyName));
				
				logger.debug("[LobbyController:play] Adding player to lobby '" + lobbyName + "'");;
				lobbys.get(lobbyName).addPlayer(player);
			} else {
				// Player is not already in Lobby
				if (!lobbys.get(lobbyName).containsPlayer(player)) {
					logger.debug("[LobbyController:play] Lobby '" + lobbyName + "' exists but player is not in lobby.");;
					
					if (lobbys.get(lobbyName).gameStarted())
					{
						logger.debug("[LobbyController:play] Lobby '" + lobbyName + "' has already started game");;
					} else {
						logger.debug("[LobbyController:play] Adding player to lobby '" + lobbyName + "'");;
						lobbys.get(lobbyName).addPlayer(player);						
					}
				}
			}
		}
		
		return ok(pokerGame.render(player, SecureSocial.env(), lobbyName));
	}

    @SecuredAction
    public WebSocket<String> getSocket() {
        //User player = (User) ctx().args.get(SecureSocial.USER_KEY);
    	User player = (User) SecureSocial.currentUser(env).get(100);
        logger.debug("[LobbyController:getSocket] getSocket called from User: ");

        synchronized (lobbys) {
        	for (String lobbyName : lobbys.keySet()) {
        		if (lobbys.get(lobbyName).containsPlayer(player)) {
        			logger.debug("[LobbyController:getSocket] ...player found! Returning WebSocket for this player");
        			return lobbys.get(lobbyName).getSocketForPlayer(player);
        		}
        	}
        }
        logger.debug("[LobbyController:getSocket] ...player not found. Player didn't joined a lobby. Rejecting WebSocket.");
        return WebSocket.reject(Results.badRequest("Player didn't joined a game."));
    }
}
