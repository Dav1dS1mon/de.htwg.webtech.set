package controllers;

import play.*;
import play.Logger;
import play.libs.F;
import securesocial.core.RuntimeEnvironment;
import play.mvc.*;

import views.html.*;

import com.google.inject.Inject;

import de.htwg.se.texasholdem.*;
import de.htwg.se.texasholdem.controller.PokerController;
import de.htwg.se.texasholdem.controller.imp.PokerControllerImp;
import securesocial.core.BasicProfile;
import securesocial.core.RuntimeEnvironment;
import securesocial.core.java.SecureSocial;
import securesocial.core.java.SecuredAction;
import securesocial.core.java.UserAwareAction;
import service.DemoUser;

public class Application extends Controller {
	
	PokerController controller = new PokerControllerImp();
	String gameName = "Poker Texas Holdem";
	

	
	public static Logger.ALogger logger = Logger.of("application.controllers.Application");
    private RuntimeEnvironment env;

    /**
     * A constructor needed to get a hold of the environment instance.
     * This could be injected using a DI framework instead too.
     *
     * @param env
     */
    @Inject()
    public Application (RuntimeEnvironment env) {
        this.env = env;
    }
    /**
     * This action only gets called if the user is logged in.
     *
     * @return
     */

    @SecuredAction
    public Result index() {
        if(logger.isDebugEnabled()){
            logger.debug("access granted to index");
        }
        DemoUser user = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
        return ok(index.render(user, SecureSocial.env()));
    }

    @UserAwareAction
    public Result userAware() {
        DemoUser demoUser = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
        String userName ;
        if ( demoUser != null ) {
            BasicProfile user = demoUser.main;
            if ( user.firstName().isDefined() ) {
                userName = user.firstName().get();
            } else if ( user.fullName().isDefined()) {
                userName = user.fullName().get();
            } else {
                userName = "authenticated user";
            }
        } else {
            userName = "guest";
        }
        return ok("Hello " + userName + ", you are seeing a public page");
    }

    @SecuredAction(authorization = WithProvider.class, params = {"twitter"})
    public Result onlyTwitter() {
        return ok("You are seeing this because you logged in using Twitter");
    }

    @SecuredAction
    public Result linkResult() {
        DemoUser current = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
        return ok(linkResult.render(current, current.identities));
    }

    /**
     * Sample use of SecureSocial.currentUser. Access the /current-user to test it
     */
    public F.Promise<Result> currentUser() {
        return SecureSocial.currentUser(env).map( new F.Function<Object, Result>() {
            @Override
            public Result apply(Object maybeUser) throws Throwable {
                String id;

                if ( maybeUser != null ) {
                    DemoUser user = (DemoUser) maybeUser;
                    id = user.main.userId();
                } else {
                    id = "not available. Please log in.";
                }
                return ok("your id is " + id);
            }
        });
    }
	
    
        
	//Startseite
    public Result poker() {
    	if(logger.isDebugEnabled()){
            logger.debug("access granted to index");
        }
        DemoUser user = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
        
        return ok(poker.render(gameName, userDetails.render(user, SecureSocial.env())));
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