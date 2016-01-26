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
		if($scope.newLobbyName == "" || $scope.newLobbyName.length == 0) {
			console.log("changed site to: " + location.host + '/lobby');
			window.location.replace('http://' + location.host + '/lobby');
		} else {
			console.log("changed site to: " + location.host + '/lobby/' + $scope.newLobbyName);
			window.location.replace('http://' + location.host + '/lobby/' + $scope.newLobbyName);
		}
	}

	$scope.getLobbies();
	setInterval($scope.getLobbies, 4000);
	console.log("Loaded controller");
});