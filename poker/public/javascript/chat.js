$(function() {
        updateChat('Connecting...');
        Server = new FancyWebSocket('ws://' + location.host + '/socket');
        // watch textarea for release of key press
         $('#send-message-box').keyup(function(e) {
        if (e.keyCode == 13) { //Enter is pressed
            var text = {
                chat: $(this).val()
            };
            sendChat( text );
            $(this).val('');
            }
        });

    //Log any messages sent from server 
    Server.bind('message', function( message ) {
        updateChat( message );
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
    Server.send( 'message', message );
}