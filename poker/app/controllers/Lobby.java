package controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;

import de.htwg.se.texasholdem.controller.PokerController;
import de.htwg.se.texasholdem.controller.imp.PokerControllerImp;
import play.libs.F;
import play.libs.F.Callback;
import play.mvc.Controller;
import play.mvc.WebSocket;
import play.mvc.WebSocket.Out;
import securesocial.core.RuntimeEnvironment;
import service.User;

public class Lobby extends Controller {
	private RuntimeEnvironment env;
	
	PokerController controller;
	String roomName;
	
	Map<User, WebSocket<String>> players = new HashMap<User, WebSocket<String>>();
	Map<User, WebSocket.In<String>> inputChannels = new HashMap<User, WebSocket.In<String>>();
	Map<User, WebSocket.Out<String>> outputChannels = new HashMap<User, WebSocket.Out<String>>();
	
	public Lobby(String roomName) {
		this.controller = new PokerControllerImp();
		this.roomName = roomName;
	}
	
	   /**
     * A constructor needed to get a hold of the environment instance.
     * This could be injected using a DI framework instead too.
     *
     * @param env
     */
    @Inject()
    public Lobby (RuntimeEnvironment env) {
        this.env = env;
    }
	
	public void addPlayer(User user) {
		players.put(user, getSocket(user));
	}
	
	// Websocket interface
    public WebSocket<String> getSocket(final User user){
        return new WebSocket<String>(){
            
            // called when websocket handshake is done
			@Override
			public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {
				System.out.println("onReady called!");
				initSocket(user, in, out);
			}
        };
    }
    
    private void initSocket(final User user, WebSocket.In<String> in, WebSocket.Out<String> out) {
    	inputChannels.put(user, in);
    	outputChannels.put(user, out);
    	
    	in.onClose(new F.Callback0() {
			@Override
			public void invoke() throws Throwable {
				System.out.println("Player " + user.getName());
				userLeftRoom(user);
			}
		});
    	
    	in.onMessage(new F.Callback<String>() {

			@Override
			public void invoke(String request) throws Throwable {
				System.out.println("in.onMessage invoked! Message: " + request);
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
		System.out.println("updateAll was sent to " + count + " clients of (out) " + outputChannels.size());
    }

	public boolean containsPlayer(User player) {
		return players.containsKey(player);
	}

	public WebSocket<String> getSocketForPlayer(final User player) {
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
		return roomName;
	}
    
}
