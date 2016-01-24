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

    Server.connect();
    

	$scope.message = "";
	$scope.chatArea = [];
	$scope.players;
	$scope.pot;
	$scope.smallBlind;
	$scope.bigBlind;
	$scope.communityCards;
	
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
	
	$scope.call = function () {
		var jsonMessage = "{command: call, value: test}";
	    Server.send("chat", jsonMessage);
	    console.log("Json message: ");
	    console.log(jsonMessage);
	}
	
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
		
		// active players
		for (var a in json.value.activePlayers) {
			//console.log(json.value.activePlayers[a]);
		}
		
		$scope.$apply();
	};
}]);













