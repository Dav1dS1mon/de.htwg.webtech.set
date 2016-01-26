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
	private int currentCallValue;
	private boolean yourTurn;
	private String smallBlindPlayer;
	private String bigBlindPlayer;
	private String chipLeaderName;
	private int chipLeaderCredits;
	private boolean roundFinished;
	
	public PlayField(PokerController controller, User player) {
		for (Player p : controller.getPlayerList()) {
			
			Map<String, Object> keyValuePair = new HashMap<String, Object>();
			keyValuePair.put("credits", p.getPlayerMoney());
			
			List<Card> holeCards = new LinkedList<Card>();
			for (Card c : p.getHoleCards()) {
				Logger.debug(p.getPlayerName() + " == " + player.getName());
				if (p.getPlayerName().equals(player.toString())) {
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
		if (controller.getStatus() == GameStatus.ENDED) {
			winner = controller.getWinningPlayer().getPlayerName();
			Logger.debug("WINNER: " + winner);
		} else {
			winner = "";
		}
		
		if (player.toString().equals(controller.getCurrentPlayer().getPlayerName()) && controller.getStatus() == GameStatus.RUNNING) {
			yourTurn = true;
			currentCallValue = controller.getCurrentCallValue();
		} else {
			yourTurn = false;
			currentCallValue = -1;
		}
		
		pot = controller.getGameData().getPot();
		bettingStatus = controller.getBettingStatus();
		gameStatus = controller.getStatus();
		gameOver = controller.getGameOver();
		smallBlind = controller.getSmallBlind();
		bigBlind = controller.getBigBlind();
		smallBlindPlayer = controller.getSmallBlindPlayer().getPlayerName();
		bigBlindPlayer = controller.getBigBlindPlayer().getPlayerName();
		chipLeaderName = controller.getChipLeader().getPlayerName();
		chipLeaderCredits = controller.getChipLeader().getPlayerMoney();
		roundFinished = controller.roundFinished();
		Logger.debug("ROUND FINISHED: " + roundFinished);
		
	}
}
