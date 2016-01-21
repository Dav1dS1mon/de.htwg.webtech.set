package controllers;

import com.google.inject.Inject;

import de.htwg.se.texasholdem.controller.imp.PokerControllerImp;
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

import com.fasterxml.jackson.databind.JsonNode;
import service.User;

import views.html.*;

public class LobbyController extends Controller {
    public static Logger.ALogger logger = Logger.of("application.controllers.Application");
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
        
        System.out.println("Application injected()");
    }
	
	@SecuredAction
	public Result getLobbys() {
		List<String> lobbyList = new ArrayList<String>(lobbys.keySet());
		String lobbyString = new Gson().toJson(lobbyList);
		
		return ok(lobbyString);
	}
	
	@SecuredAction
	public Result play(String lobbyName) {
		if (lobbyName.equals("")) {
			lobbyName = DEFAULT_LOBBY_NAME;
		}

		User user = (User) ctx().args.get(SecureSocial.USER_KEY);
		
		synchronized (lobbys) {
			if (!lobbys.containsKey(lobbyName)) {
				// Lobby does not exist
				lobbys.put(lobbyName, new Lobby(lobbyName));
			} else {
				// Player is not already in Lobby
				if (!lobbys.get(lobbyName).containsPlayer(user)) {
					lobbys.get(lobbyName).addPlayer(user);
				}
			}
		}
		
		return ok(pokerLobby.render(user, SecureSocial.env(), lobbyName));
	}

    @SecuredAction
    public WebSocket<String> getSocket() {
        User player = (User) ctx().args.get(SecureSocial.USER_KEY);

        synchronized (lobbys) {
        	for (String lobbyName : lobbys.keySet()) {
        		if (lobbys.get(lobbyName).containsPlayer(player)) {
        			return lobbys.get(lobbyName).getSocketForPlayer(player);
        		}
        	}
        }
        return WebSocket.reject(Results.badRequest("Player didn't joined a game."));
    }
}