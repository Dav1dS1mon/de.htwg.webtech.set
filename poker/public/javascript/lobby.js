var ngLobbyApp = angular.module('lobbyModule',[]);

ngLobbyApp.controller('LobbyController', function ($scope, $http, $location) {
	console.log("Started loading controller");

    $scope.getLobbies = function () {
		$http.get('/lobbys').success(function (data) {
			$scope.lobbies = data;
			$scope.hasLobbies = data.length != 0
			console.log($scope.lobbies);
		});
    };
    
	$scope.newLobbyName = "";
	
	$scope.enterNewLobby = function() {
		console.log("changed site to: " + location.host + '/lobby/' + $scope.newLobbyName);
		window.location.replace('http://' + location.host + '/lobby/' + $scope.newLobbyName);
	}

	$scope.getLobbies();
	setInterval($scope.getLobbies, 2000);
	console.log("Loaded controller");
});