$(function() {
        updateChat('Connecting...');
        Server = new FancyWebSocket('ws://' + location.host + '/socket');
        // watch textarea for release of key press
         $('#send-message-box').keyup(function(e) {
        if (e.keyCode == 13) { //Enter is pressed
			var text = "{command: \"chat\", value: \"" + $(this).val() + "\"}";
			sendChat(text)

            $(this).val('');
            }
        });

    //Log any messages sent from server 
    Server.bind('message', function( message ) {
    	var msg = JSON.parse(message);
    	
    	switch(msg.command) {
    		case "updateChat":
	    		updateChat(msg.value);
	    		break;
	    	case "updateLobby":
	    		var players = msg.value;
	    		for(var i in players) {
     			   $('#player-list').append($('<label><input type="checkbox">' + players[i] + '</label><br>'));
    			}
    	}
        
    });

    Server.connect();
});

var Server;
function updateChat( message ) {
    $('#chat-area').append($("<p>"+ message +"</p>"));
    document.getElementById('chat-area').scrollTop =
    document.getElementById('chat-area').scrollHeight;
}

function sendChat( message ) {
    Server.send("chat", message);
    console.log("Json message: ");
    console.log(message);
}