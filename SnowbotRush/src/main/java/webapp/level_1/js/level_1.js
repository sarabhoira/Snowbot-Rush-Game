// all functions for game

let gamews; // websocket connection object
let blnGameOver = true; // flag to track if game is over
let blnFreeze = false; // flag to track if user has been frozen

// method to start a new game for the users
function startGame() {
    // check if user has entered their name and selected difficulty level before starting the game
    let name = document.getElementById('name').value;
    let level = document.getElementById('level').value;
    if (name === null || name.trim().length === 0 || level === null || level.length === 0) {
        showMessage("info","Please enter name and select difficulty level to begin.");
    } else {
        enterGame(); // join a game room
        waitForSocketConnection(function(){ // websocket connection is established before sending player data to server
            sendPlayerData(name,level);
            blnGameOver = false;
        });
    }
}

// make the function wait until the connection is made...
function waitForSocketConnection(callback){
    setTimeout(
        function () {
            if (gamews.readyState === 1) {
                console.log("Connection is made")
                if (callback != null){
                    callback();
                }
            } else {
                console.log("wait for connection...")
                waitForSocketConnection(callback);
            }

        }, 500); // wait 500 milliseconds for the connection...
}

// method to enter a new or existing room
function enterGame(){
    closeGameConnection();
    // create the web socket
    gamews = new WebSocket("ws://localhost:8080/SnowbotRush-1.0-SNAPSHOT/ws/game/");

    // parse messages received from the server and update the UI accordingly
    gamews.onmessage = function (event) {
        console.log(event.data);
        // parsing the server's message as json
        let message = JSON.parse(event.data);

        // handle message
        if (message.type == "info") {
            showMessage(message.type,message.message);
        } else if (message.type == "error") {
            showMessage(message.type,message.message);
        } else if (message.type == "game") {
            displayGameBoard(message.message);
        } else if (message.type == "points" || message.type == "lives" || message.type == "freeze") {
            changeImage(message);
        } else if (message.type == "failed") {
            showMessage(message.type,message.message);
            displayFailed(message.row,message.column);
        } else if (message.type == "lost" || message.type == "winner") {
            showMessage(message.type,message.message);
            gameOver(message.type)
        }
    };

    gamews.onclose = function() {
        // websocket is closed.
        blnGameOver = true;
    };
}

// method invoked when connection is closed
function closeGameConnection() {
    if (gamews!=null && gamews.readyState == 1) {
        gamews.close();
    }
}

// method for creating the JSON request and send the instructions to the server
function sendPlayerData(name,level) {
    let request = {"type":"enter", "name":name, "level":level};
    gamews.send(JSON.stringify(request));
}

// method to send user selection to game server
function sendClick(row,column) {
    if (!blnGameOver && !blnFreeze) {
        let request = {"type": "select", "row": row, "column": column};
        gamews.send(JSON.stringify(request));
    }
}

// method to show message to user
function showMessage(type,message) {
    let msgBox = document.getElementById('messages');
    msgBox.innerHTML = "[" + type.toUpperCase() + "] " + message + "\n";
}

// method to initialize the game board
function displayGameBoard(size) {
    let gameBoard = document.getElementById("gameBoard");

    // depending on the difficulty level, generate a table
    const table = document.createElement("table");
    let imgSize = 50 - (5*(size-10));

    // dynamically create the table grid
    for (let i = 0; i < size; i++) {
        let newRow = table.insertRow(-1);
        for (let j = 0; j < size; j++) {
            let pieceCell = newRow.insertCell(j);
            pieceCell.setAttribute("align", "center");
            pieceCell.setAttribute("id","cell_"+i+"_"+j);
            let position = (i*size)+j;
            let pieceImage = document.createElement("img");
            pieceImage.src = "assets/cover.png";
            pieceImage.width = imgSize;
            pieceImage.height = imgSize;
            pieceImage.setAttribute("onclick", "sendClick('"+i+"','"+j+"')");
            pieceImage.setAttribute("id","cover_"+i+"_"+j);
            pieceCell.appendChild(pieceImage);
        }
    }
    gameBoard.innerHTML = "";
    gameBoard.appendChild(table);
}

// method to change image depending on user selection
function changeImage(data) {
    // if game piece has points and lives, then display the information in message box on screen
    if (data.type == "points" || data.type == "lives") {
        let inputBox = document.getElementById(data.type);
        inputBox.value = data.message;
    }
    if (data.row != null && data.column != null) {
        // get the image reference based on the row and column
        let img = document.getElementById("cover_" + data.row + "_" + data.column);
        img.src = "assets/" + data.piece + ".png"; // change the game piece image
        img.width = img.width - 12; // set image width
        img.height = img.height - 12; // set image height
        img.removeAttribute("onclick"); // remove onclick attribute so piece cannot be selected again
        let msgText = data.value;
        // prepare message to display to user in message box
        if (data.type == "points") {
            msgText += " points";
        } else if (data.type == "lives") {
            msgText += " life";
        } else {
            msgText = "frozen";
        }
        // create a div tag to display the score or life earned by player
        let cell = document.getElementById("cell_" + data.row + "_" + data.column);
        let divCell = document.createElement("div");
        divCell.setAttribute("class", "centered");
        divCell.innerHTML = msgText;
        cell.appendChild(divCell);
        // get character image for player
        let snowbot = document.getElementById("snowbot");
        let glow = "green"; // this is for when the player receives a points
        let timeout = 500; // how long the user image should be changed for character
        if (data.value == 1) { // gained 1 life
            glow = "blue";
        } else if (data.value == -1) { // lost one life
            glow = "red";
        } else if (data.type == "freeze") { // when user has selected the icicle
            glow = "freeze";
            msgText = "You are now frozen for " + data.value + " seconds"; // prepare message for player
            timeout = data.value * 1000; // set the freeze timeout
            blnFreeze = true;
        }
        // set the new image and restore after the given timeout value
        snowbot.src = "assets/snowbot_" + glow + ".png";
        setTimeout(
            function () {
                snowbot.src = "assets/snowbot.png";
                blnFreeze = false;
            }, timeout);
        showMessage("info", msgText);
    }
}

// method to show player that game piece is already selected by other player
function displayFailed(row,column) {
    let img = document.getElementById("cover_"+row+"_"+column);
    img.src = "assets/claimed.png";
    img.removeAttribute("onclick");
    // set the new image and restore after the given timeout value
    let timeout = 500; // how long the user image should be changed for character
    let snowbot = document.getElementById("snowbot");
    snowbot.src="assets/snowbot_failed.png";
    setTimeout(
        function () {
            snowbot.src="assets/snowbot.png";
        }, timeout);
}

// method to close the websocket connection when game is over
function gameOver(result) {
    blnGameOver = true;
    closeGameConnection();
}
