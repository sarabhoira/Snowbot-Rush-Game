package com.game.gameserver;

import java.util.*;

public class GameRoom {
    private String gameID; // game id
    private GameBoard gameBoard; // game board
    private Map<String, Player> players = new HashMap<String, Player>(); // list of players playing the game
    private String winner; // for keeping track of who won the game
    private Constants.Status gameStatus = Constants.Status.WAITING; // track the game status
    private Constants.LEVEL level; // difficulty level of the game board

    // constructor to create game room
    public GameRoom(String gameID, String level) {
        this.gameID = gameID;
        this.level = Constants.LEVEL.valueOf(level);
        this.gameBoard = new GameBoard(this.level);
        this.winner = null;
    }

    // method to retrieve game id
    public String getGameID() {
        return gameID;
    }

    // method to retrieve game board
    public GameBoard getGameBoard() {
        return gameBoard;
    }

    // method to retrieve all players
    public Map<String, Player> getPlayers() {
        return players;
    }

    // method to retrieve the winner
    public String getWinner() {
        return winner;
    }

    // method to set the winner
    public void setWinner(String winner) {
        this.winner = winner;
    }

    // method to retrieve game status
    public Constants.Status getGameStatus() {
        return gameStatus;
    }

    // method to set game status
    public void setGameStatus(Constants.Status gameStatus) {
        this.gameStatus = gameStatus;
    }

    // method to set player
    public void setPlayer(Player player) {
        String playerID = player.getId();
        removePlayer(playerID);
        players.put(playerID, player);
    }

    // method to retrieve specific player
    public Player getPlayer(String playerID) {
        return players.get(playerID);
    }

    // method to retrieve number of players in the game
    public int getNumOfPlayers() {
        return players.size();
    }

    // method to add a new player to the game
    public void addPlayer(String playerID, String playerName) {
        if(!inRoom(playerID)) {
            Player player = new Player(playerID, playerName);
            this.players.put(playerID, player);
        }
    }

    // method to remove player from the game
    public void removePlayer(String playerID){
        if(inRoom(playerID)){
            players.remove(playerID);
        }
    }

    // method to check if player is present in the game room
    public boolean inRoom(String playerID){
        return (players.containsKey(playerID));
    }

    // method to compare the difficulty level of the game
    public boolean isLevel(String level) {
        boolean blnMatch = false; // assume the level is different
        if (this.level.name().equalsIgnoreCase(level)) { // check if level parameter is equal to the current game difficulty level
            blnMatch = true; // indicate level is the same
        }
        return blnMatch; // return flag
    }

    // method to check if we have a winner of this game
    public boolean hasWinner() {
        boolean blnFound = false; // default assumption there are no winners
        if (winner!=null) { // to proceed further there should be no winner already assigned to teh game board
            blnFound = true; // set the flag we have a winner
        } else {
            List<Player> users = new ArrayList<>(); // get all users
            for (Player player : players.values()) { // for each player check on the number of lives
                if (player.hasLives()) { // if player has life then add it to the user list
                    users.add(player);
                }
            }
            if (users.size() == 1) { // check if there is only one user with lives
                blnFound = true; // set the flag we have a winner
                gameStatus = Constants.Status.END; // game has ended
                Player player = users.get(0); // get the player that has won the game
                winner = player.getId(); // set the winner variable
            } else if (gameBoard.isGameOver()) { // check if all the game pieces have been claimed
                blnFound = true; // set the flag we have a winner
                gameStatus = Constants.Status.END; // game has ended
                // identify the player with the higher score as winner
                long highscore = 0;
                Iterator playerIterator = players.values().iterator(); // iterate through the player list
                while (playerIterator.hasNext()) { // continue until all players are checked
                    Player player = (Player) playerIterator.next(); // get the next player in the list
                    if (highscore < player.getScore()) { // compare if this player has higher score
                        winner = player.getId(); // set the winner
                        highscore = player.getScore(); // set the high score of the player
                    }
                }
            }
        }
        return blnFound; // return the flag if player has been found or not
    }
}