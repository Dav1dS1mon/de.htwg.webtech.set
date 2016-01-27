angular.module('ngPokerApp', [])
.controller('PokerController', ['$scope', function($scope) {

	Server = new FancyWebSocket('ws://' + location.host + '/socket');

    Server.bind('message', function( message ) {
    	console.log("Incoming: " + message);
    	var jsonResponse = JSON.parse(message);
    	
    	
    	switch(jsonResponse.command) {
    		case "updateChat":
	    		$scope.chatArea.push(jsonResponse.value);
	    		$scope.$apply();
	    		break;
	    	case "updateLobby":
				//Update Lobby Player List
				$scope.lobbyPlayer = jsonResponse.value;
				$scope.$apply();
				break;
			case "updatePlayField":
				updatePlayField(jsonResponse);
				break;
			case "playFieldChanged":
				console.log("playFieldChanged called");
				$scope.playField();
				break;
    	}
	});
	
	
	var keepAliveInterval = setInterval(keepAlive, 30000);
	
	function keepAlive() {
	    var jsonMessage = "{command: keepAlive, value: none}";
	    Server.send("chat", jsonMessage);
	    console.log("Json message: ");
	}
	
    Server.connect();
	$scope.message = "";
	$scope.chatArea = [];
	$scope.players;
	$scope.pot;
	$scope.smallBlind;
	$scope.bigBlind;
	$scope.readyState;
	$scope.communityCards;
	$scope.lobbyPlayer;
	$scope.raiseValue;
	$scope.currentCallValue;
	
	// To check if buttons (raise, call, ...) have to be visible for you.
	// If game ended, yourTurn will be 'false', so that the controls can be hidden from the user
	// also if ONE ROUND has be ended.
	$scope.yourTurn;
	
	// Will always be a player. Also if game ends.
	// Don't use it to hide something if game has ended.
	$scope.currentPlayer;
	
	$scope.activePlayers = [];
	$scope.smallBlindPlayer;
	$scope.bigBlindPlayer;
	$scope.chipLeaderName;
	$scope.chipLeaderCredits;
	$scope.roundFinished;
	$scope.lastEvent;
	$scope.beforeLastEvent = "";
	$scope.showOnce = false;
	$scope.gameStatus = "";
	
	$scope.playField = function() {
		console.log("$scope.playField - sendUpdatePlayField");
		var jsonMessage = "{command: playField, value: none}";
		Server.send("playField", jsonMessage);
	};
	
	$scope.sendMessage = function() {
		var jsonMessage = "{command: chat, value: \"" + $scope.message + "\"}";
	    Server.send("chat", jsonMessage);
	    console.log("Json message: ");
	    console.log(jsonMessage);
		$scope.message = "";	
	};
	
	$scope.ready = function() {
			var jsonMessage = "{command: ready, value: \"" + $scope.readyState + "\"}";
			Server.send("ready", jsonMessage);
	};
	
	$scope.call = function () {
		var jsonMessage = "{command: call, value: test}";
	    Server.send("chat", jsonMessage);
	    console.log("Json message: ");
	    console.log(jsonMessage);
	};
	
	$scope.fold = function () {
		var jsonMessage = "{command: fold, value: none}";
	    Server.send("chat", jsonMessage);
	    console.log("Json message: ");
	    console.log(jsonMessage);
	};
	
	$scope.raise = function () {
		if ($scope.raiseValue > 0 ) {
			var jsonMessage = "{command: raise, value: \"" + $scope.raiseValue + "\"}";
		    Server.send("chat", jsonMessage);
		    console.log("Json message: ");
		    console.log(jsonMessage);
		    $scope.raiseValue = "";
		} else {
			alert("Raise value must be greater than 0! Please try again.");
		}
	};
		
	updatePlayField = function(json) {
	
		var players = json.value.players;
		$scope.players = json.value.players;
		//console.log($scope.players);
		
		
		for (var p in players) {
		
			//Player name
			//console.log(p);
		
			//Credits
			//console.log(players[p].credits);
			
			//Cards
			cards = players[p].cards;
			for (var c in cards) {
				//console.log(cards[c].rank + " | " + cards[c].suit);
			}
		}
		
		
		
		// smallBlind
		$scope.smallBlind = json.value.smallBlind;
		
		// bigBlind
		$scope.bigBlind = json.value.bigBlind;
		
		// winner
		$scope.winner = json.value.winner;
		
		// pot
		$scope.pot = json.value.pot;
		
		// gameOver
		$scope.gameOver = json.value.gameOver;
		
		// gameStatus
		$scope.gameStatus = json.value.gameStatus;
		
		// bettingStatus
		$scope.bettingStatus = json.value.bettingStatus;
		
		// dealer
		$scope.dealer = json.value.dealer;
		
		// current player
		$scope.currentPlayer = json.value.currentPlayer;
		
		// Community Cards
		$scope.communityCards = json.value.communityCards;
		
		// Current Call Value
		$scope.currentCallValue = json.value.currentCallValue;
		
		// Your Turn
		$scope.yourTurn = json.value.yourTurn;
		
		$scope.activePlayers = [];
		// active players
		for (var a in json.value.activePlayers) {
			//console.log(json.value.activePlayers[a]);
			$scope.activePlayers.push(json.value.activePlayers[a]);
		}
		
		
		$scope.smallBlindPlayer = json.value.smallBlindPlayer;
		$scope.bigBlindPlayer = json.value.bigBlindPlayer;
		$scope.chipLeaderName = json.value.chipLeaderName;
		$scope.chipLeaderCredits = json.value.chipLeaderCredits;
		$scope.roundFinished = json.value.roundFinished;
		$scope.lastEvent = json.value.lastEvent;
		
		if ($scope.beforeLastEvent != $scope.lastEvent && $scope.lastEvent.length >= 12) {
			$scope.beforeLastEvent = $scope.lastEvent;
			
			$scope.chatArea.push($scope.lastEvent);
		} else {
			console.log("prevented same lastEvent");
		}

		$scope.$apply();
		
		if (($scope.winner != "" || $scope.winner.length > 0) && !$scope.showOnce) {
			$scope.showOnce = true;
			alert($scope.winner + " won the game! \nThanks for playing!\n\nYou will be redirected to the Lobby Browser by clicking on 'OK'");
			window.location.replace('http://' + location.host + '/pokerAngular');
		}
	};
}]);













