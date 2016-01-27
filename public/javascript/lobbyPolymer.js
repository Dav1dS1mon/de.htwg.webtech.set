var lobbies = [];

function getLobbyData() {
	$.get("/lobbys", function(data, status){
	  	console.log("Available lobbies: " + data);
	  	lobbies = data;
	  	var lobbyBrowser = document.querySelector('lobby-browser');
	  	lobbyBrowser.updateLobby(data);
	});
}

setInterval(getLobbyData, 4000);