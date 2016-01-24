angular.module('ngPokerApp', [])
.controller('PokerController', ['$scope', function($scope) {

	Server = new FancyWebSocket('ws://' + location.host + '/socket');

    Server.bind('message', function( message ) {
    	var msg = JSON.parse(message);
    	
    	switch(msg.command) {
    		case "updateChat":
	    		console.log("Incoming: " + msg.value);
	    		$scope.chatArea.push(msg.value);
	    		$scope.$apply();
	    		break;
	    	case "updateLobby":
				//Update Lobby Player List
    	}
	});

    Server.connect();


	$scope.message = "";
	$scope.chatArea = [];
	
	$scope.sendMessage = function() {
		var jsonMessage = "{command: chat, value: \"" + $scope.message + "\"}";
	    Server.send("chat", jsonMessage);
	    console.log("Json message: ");
	    console.log(jsonMessage);
		$scope.message = "";
	};
}]);