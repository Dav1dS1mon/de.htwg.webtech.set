package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

import de.htwg.se.texasholdem.*;
import de.htwg.se.texasholdem.controller.PokerController;
import de.htwg.se.texasholdem.controller.imp.PokerControllerImp;

public class Application extends Controller {
	
	PokerController controller = new PokerControllerImp(); 

    public Result index() {
        //return ok(index.render(controller.getTableString()));
        return ok();
    }
    
    public Result addPlayer(String name) {
    	controller.addPlayer(name);
    	//return ok(index.render("Added player"));
    	return ok();
    }

    public Result getPlayers() {
    	return ok(index.render(controller.getPlayerList()));
    }
}
