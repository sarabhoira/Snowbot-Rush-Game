// all functions for chat

let chatws; // websocket connection object

// method to enter a new or existing room
function enterRoom(){
    closeChatConnection();
    // create the web socket
    chatws = new WebSocket("ws://localhost:8080/SnowbotRush-1.0-SNAPSHOT/ws/game-chat/");

    // parse messages received from the server and update the UI accordingly
    chatws.onmessage = function (event) {
        //console.log(event.data);
        // parsing the server's message as json
        let message = JSON.parse(event.data);

        // handle message
        if (message.type == "chat") { // this is the chat message to be exchanged between users
            const element = document.getElementById("log");
            element.innerHTML += "[" + timestamp() + "] " + message.message + "\n";
            element.scrollIntoView(false);
        }
    };

    chatws.onclose = function() {
    };
}

// method invoked when connection is closed
function closeChatConnection() {
    if (chatws!=null && chatws.readyState == 1) {
        chatws.close();
    }
}

// method to get the current time for display along with the chat message
function timestamp() {
    var d = new Date(), minutes = d.getMinutes();
    if (minutes < 10) minutes = '0' + minutes;
    return d.getHours() + ':' + minutes;
}

// method for creating the JSON request and send the instructions to the server
function sendChatMessage(id) {
    let chatBox = document.getElementById(id);
    let request = {"type":"chat", "msg":chatBox.value};
    chatws.send(JSON.stringify(request));
    chatBox.value = "";
}

// method event listener setup on input element to  listen for enter key event
document.getElementById("chat").addEventListener("keyup", function (event) {
    if (event.keyCode === 13) { // verify if user has pressed enter key
        if (chatws==null || chatws.readyState != 1) {
            enterRoom();
        } else {
            sendChatMessage(event.target.id);
        }
    }
});

(function () {
    enterRoom(); // start the chat session by entering the chat when the page loads
})();