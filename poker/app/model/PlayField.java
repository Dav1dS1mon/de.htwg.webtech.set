package model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.htwg.se.texasholdem.controller.GameStatus;
import de.htwg.se.texasholdem.controller.PokerController;
import de.htwg.se.texasholdem.model.BettingStatus;
import de.htwg.se.texasholdem.model.Card;
import de.htwg.se.texasholdem.model.Player;
import play.Logger;
import service.User;

public class PlayField {
	private Map<String, Map<String, Object>> players = new HashMap<String, Map<String, Object>>();
	private List<String> activePlayers = new LinkedList<String>();
	private List<Card> communityCards = new LinkedList<Card>();
	private String currentPlayer;
	private String dealer;
	private String winner;
	private int pot;
	private BettingStatus bettingStatus;
	private GameStatus gameStatus;
	private boolean gameOver;
	private int smallBlind;
	private int bigBlind;
	
	public PlayField(PokerController controller, User player) {
		for (Player p : controller.getPlayerList()) {
			
			Map<String, Object> keyValuePair = new HashMap<String, Object>();
			keyValuePair.put("credits", p.getPlayerMoney());
			
			List<Card> holeCards = new LinkedList<Card>();
			for (Card c : p.getHoleCards()) {
				Logger.debug(p.getPlayerName() + " == " + player.getName());
				if (p.getPlayerName().equals(player.getName())) {
					holeCards.add(c);
				}				
			}
			
			keyValuePair.put("cards", holeCards);
			
			players.put(p.getPlayerName(), keyValuePair);
		}
		
		for (Player p : controller.getActivePlayers()) {
			activePlayers.add(p.getPlayerName());
		}
		
		for (Card c : controller.getGameData().getCommunityCards()) {
			communityCards.add(c);
		}
		
		currentPlayer = controller.getCurrentPlayer().getPlayerName();
		dealer = controller.getDealer().getPlayerName();
		if (controller.getWinningPlayer() != null) {
			winner = controller.getWinningPlayer().getPlayerName();
		} else {
			winner = "";
		}
		pot = controller.getGameData().getPot();
		bettingStatus = controller.getBettingStatus();
		gameStatus = controller.getStatus();
		gameOver = controller.getGameOver();
		smallBlind = controller.getSmallBlind();
		bigBlind = controller.getBigBlind();
	}
}
