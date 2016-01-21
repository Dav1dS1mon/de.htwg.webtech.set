package controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;

import de.htwg.se.texasholdem.controller.PokerController;
import play.libs.F;
import play.libs.F.Callback;
import play.mvc.Controller;
import play.mvc.WebSocket;
import securesocial.core.RuntimeEnvironment;
import service.User;

public class RoomController extends Controller {
	private RuntimeEnvironment env;
	
	PokerController controller;
	String roomName;
	
	Map<User, WebSocket<JsonNode>> playerList = new HashMap<User, WebSocket<JsonNode>>();
	Map<User, WebSocket.In<JsonNode>> inputChannels = new HashMap<User, WebSocket.In<JsonNode>>();
	Map<User, WebSocket.Out<JsonNode>> outputChannels = new HashMap<User, WebSocket.Out<JsonNode>>();
	
	public RoomController(PokerController controller, String roomName) {
		this.controller = controller;
		this.roomName = roomName;
	}
	
	   /**
     * A constructor needed to get a hold of the environment instance.
     * This could be injected using a DI framework instead too.
     *
     * @param env
     */
    @Inject()
    public RoomController (RuntimeEnvironment env) {
        this.env = env;
    }
	
	public void addPlayer(User user) {
		playerList.put(user, getSocket(user));
	}
	
	public String getPlayerNameBySocket(WebSocket ws) {
		for (Entry<User, WebSocket<JsonNode>> entry  : playerList.entrySet()) {
			if(entry.getValue() == ws) {
				return entry.getKey().main.toString();
			}
		}
		return roomName;
	}
	
	// Websocket interface
    public WebSocket<JsonNode> getSocket(final User user){
        return new WebSocket<JsonNode>(){
            
            // called when websocket handshake is done
			@Override
			public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
				System.out.println("onReady called!!!!");
				initSocket(user, in, out);
			}
        };
    }
    
    private void initSocket(final User user, WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
    	inputChannels.put(user, in);
    	outputChannels.put(user, out);
    	
    	in.onClose(new F.Callback0() {
			@Override
			public void invoke() throws Throwable {
				System.out.println("Player " + user.getName());
				userLeftRoom(user);
			}
		});
    	
    	in.onMessage(new F.Callback<JsonNode>() {

			@Override
			public void invoke(JsonNode request) throws Throwable {
				System.out.println("in.onMessage invoked!!!!");
				playerRequest(request);
			}
    		
		});
    }
    
    private void userLeftRoom(final User user) {
    	inputChannels.remove(user);
    	outputChannels.remove(user);
    	playerList.remove(user);
    }
    
    public void playerRequest(JsonNode request) {
    	/* TODO: 	Check for Command of Request
    	 *			'check' and other ones
    	 */
    	
    }
    
}
