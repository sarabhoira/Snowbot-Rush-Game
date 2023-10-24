let apiPostURL = "http://localhost:8080/SnowbotRush-1.0-SNAPSHOT/api/game/highscore";

// method to call api to get player top scores and player names
function getHighScore(){
    fetch(apiPostURL, {
        method: 'GET',
        headers: {
            'Accept': 'application/json'
        }
    })
        .then(response => response.json())
        .then(response => displayHighScore(response));
}

// method to display top 5 game high scores
function displayHighScore(data){
    let tableRef = document.getElementById('highScores').tBodies[0];
    tableRef.innerHTML = "";
    for (c in data.scores){
        let newRow = tableRef.insertRow(-1);
        let rankCell = newRow.insertCell(0);
        let nameCell = newRow.insertCell(1);
        let scoreCell = newRow.insertCell(2);
        let rankText = document.createTextNode(data.scores[c].rank);
        let nameText = document.createTextNode(data.scores[c].name);
        let scoreText = document.createTextNode(data.scores[c].score);
        rankCell.appendChild(rankText);
        nameCell.appendChild(nameText);
        scoreCell.appendChild(scoreText);
    }
}

(function () {
    getHighScore();
    // set interval function
    setInterval(getHighScore,60000); // keep checking every minute for high score updates
})();
