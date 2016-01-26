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
import com.fasterxml.jackson.databind.JsonNode;
import service.User;

import views.html.*;


/**
 * A sample controller
 */
public class Application extends Controller {
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
        logger.debug(String.valueOf(env.hashCode()));
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
        User user = (User) ctx().args.get(SecureSocial.USER_KEY);
        return ok(index.render(user, SecureSocial.env()));
    }

    @UserAwareAction
    public Result userAware() {
        User demoUser = (User) ctx().args.get(SecureSocial.USER_KEY);
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
        User current = (User) ctx().args.get(SecureSocial.USER_KEY);
        return ok(linkResult.render(current, current.identities));
    }
    
    
    @SecuredAction
    public Result pokerAngular() {
        User current = (User) ctx().args.get(SecureSocial.USER_KEY);
        return ok(pokerLobbyAngular.render(current, SecureSocial.env()));
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
                    User user = (User) maybeUser;
                    id = user.main.userId();
                } else {
                    id = "not available. Please log in.";
                }
                return ok("your id is " + id);
            }
        });
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
    
    @SecuredAction(authorization = WithProvider.class, params = {"github"})
    public Result onlyGithub() {
        return ok("You are seeing this, because you are logged in using GitHub account.");
    }

    
    
}