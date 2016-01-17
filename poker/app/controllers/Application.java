/**
 * Copyright 2012-214 Jorge Aliss (jaliss at gmail dot com) - twitter: @jaliss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package controllers;

import com.google.inject.Inject;
import de.htwg.se.texasholdem.controller.imp.PokerControllerImp;
import play.Logger;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import securesocial.core.BasicProfile;
import securesocial.core.RuntimeEnvironment;
import securesocial.core.java.SecureSocial;
import securesocial.core.java.SecuredAction;
import securesocial.core.java.UserAwareAction;
import service.DemoUser;
import views.html.index;
import views.html.linkResult;
import views.html.pokerGame;
import views.html.pokerHelp;
import views.html.pokerAbout;
import de.htwg.se.texasholdem.controller.PokerController;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * A sample controller
 */
public class Application extends Controller {
    public static Logger.ALogger logger = Logger.of("application.controllers.Application");
    private RuntimeEnvironment env;

    private PokerController controller = new PokerControllerImp();

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

    /*@SecuredAction(authorization = WithProvider.class, params = {"twitter"})
    public Result onlyTwitter() {
        return ok("You are seeing this because you logged in using Twitter");
    }*/

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
    
    @SecuredAction
    public Result pokerGame() {
        if(logger.isDebugEnabled()){
            logger.debug("access granted to pokerGame");
        }
        return ok(pokerGame.render());
    }
    
    @SecuredAction
    public Result pokerHelp() {
        if(logger.isDebugEnabled()){
            logger.debug("access granted to pokerHelp");
        }
        return ok(pokerHelp.render());
    }
    
    @SecuredAction
    public Result pokerAbout() {
        if(logger.isDebugEnabled()){
            logger.debug("access granted to pokerAbout");
        }
        return ok(pokerAbout.render());
    }

    @SecuredAction
    public Result addPlayer(String name) {
        controller.addPlayer(name);

        return ok("Player " + name + " added");
    }

    /*
    @SecuredAction
    public Result jsonField {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JsonOrgModule());
        JSONObject json = new JSONObject();
        JSONArray f = new JSONArray();

        object =
        {
            "players" : [
                {
                    "Player1" : {
                        "credits": credits,
                        Cards: ["card1", "card2"] },
                    "Player2" : {
                        "credits": credits,
                        Cards: ["card1", "card2"] },
                    ...
            ],
            "communityCards" : ["card1", "card2", "card3", "card4", "card5"],
            "smallBind" : 30,
            "dealer" : playerX,
            "pot" : 3000,
            "currentPlayer" : playerX,
            "bettingStatus" : bettingStatus,
            "gameStatus" : gameStatus
        }
    }
    }
    */

    /*

    public JsonNode jsonField() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JsonOrgModule());
		JSONObject json = new JSONObject();
		JSONArray f = new JSONArray();
		int i = 1;
		for (IGameToken[] row : controller.getField()) {
			JSONArray r = new JSONArray();
			int j = 1;
			for (IGameToken token : row) {
				JSONObject t = new JSONObject();
				t.put("id", i + "_" + j);
				t.put("name", token.getName());
				r.put(t);
				j++;
			}
			f.put(r);
			i++;
		}
		json.put("current", controller.getcPlayer().getName());
		json.put("p1wins", controller.getWinPlayer1());
		json.put("p2wins", controller.getWinPlayer2());
		json.put("status", String.valueOf(controller.getStatus()));
		json.put("field", f);
		return mapper.valueToTree(json);
	}

    */
    
    /*@SecuredAction
    public Result testmethode() {
        if(logger.isDebugEnabled()){
            logger.debug("access granted to index");
        }
        DemoUser user = (DemoUser) ctx().args.get(SecureSocial.USER_KEY);
        return ok(test.render());
    }*/

    
    
}
