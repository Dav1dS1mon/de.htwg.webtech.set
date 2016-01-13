package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

import de.htwg.se.texasholdem.*;
import de.htwg.se.texasholdem.controller.PokerController;
import de.htwg.se.texasholdem.controller.imp.PokerControllerImp;

public class Application extends Controller {
	
	PokerController controller = new PokerControllerImp();
	String gameName = "Poker Texas Holdem";

    public Result poker() {
        return ok(poker.render(gameName, pokerMain.render()));
    }
    
    public Result pokerGame() {
    	return ok(poker.render(gameName, pokerGame.render()));
    }
    
    public Result pokerHelp() {
    	return ok(poker.render(gameName, pokerHelp.render()));
    }
    
    public Result pokerAbout() {
    	return ok(poker.render(gameName, pokerAbout.render()));
    }
    
    public Result addPlayer(String name) {
    	controller.addPlayer(name);
    	//return ok(index.render("Added player"));
    	return ok();
    }

    public Result getPlayers() {
    	//return ok(index.render(controller.getPlayerList()));
    	return ok();
    }
}
