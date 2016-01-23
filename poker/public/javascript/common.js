$.get("/lobby", function(data) {
    var players = data.split(" ");
    players.splice(-1,1);
    for(var i in players) {
        $('#player-list').append($('<label><input type="checkbox">' + players[i] + '</label><br>'));
    }
});