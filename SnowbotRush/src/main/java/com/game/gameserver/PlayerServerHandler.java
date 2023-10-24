package com.game.gameserver;


import jakarta.websocket.Session;
import java.io.IOException;

public class PlayerServerHandler implements Runnable {
    private GameRoom gameRoom; // game room
    private Session session; // session
    private int[] selection; // this identifies which game piece is selected by user

    // constructor
    public PlayerServerHandler(GameRoom gameRoom, int[] selection, Session session) throws IOException {
        this.gameRoom = gameRoom;
        this.selection = selection;
        this.session = session;
    }

    // method to run the thread
    public void run() {
        try {
            GameBoard gameBoard = gameRoom.getGameBoard(); // get game board
            Player player = gameRoom.getPlayer(session.getId()); // get current player
            if (player.hasLives()) { // if player has lives
                GamePiece gamePiece = gameBoard.getGamePiece(selection[0], selection[1]); // find the game piece selected by user
                if (!gamePiece.isClaimed()) { // check if game piece is already claimed by another player
                    gamePiece.setClaimed(); // set the game piece as claimed
                    Constants.Choice choice = gamePiece.getType(); // retrieve the game difficulty level
                    int value = gamePiece.getValue(); // read the value of the piece selected by the user
                    if (Constants.Choice.POINTS.equals(choice)) { // if the game piece is of type points
                        player.setScore(value); // update the player score
                        session.getBasicRemote().sendText(getSendMsg("points", gamePiece, player.getScore())); // send the latest score to user
                        HighScore.getInstance().setHighScore(player.getName(), player.getScore()); // set the new score of user
                    } else if (Constants.Choice.LIVES.equals(choice)) { // if the game piece is of type lives
                        player.setLives(value); // update the player lives
                        session.getBasicRemote().sendText(getSendMsg("lives", gamePiece, player.getLives())); // send the latest live count to user
                        if (!player.hasLives()) { // if player has no more lives left
                            session.getBasicRemote().sendText("{\"type\": \"lost\", \"message\":\"Sorry, no more lives.\"}"); // send player has no more lives
                        }
                    } else if (Constants.Choice.FREEZE.equals(choice)) { // if the game piece is of type freeze
                        session.getBasicRemote().sendText(getSendMsg("freeze", gamePiece, gamePiece.getValue())); // send player has been frozen
                    }
                    checkWinner(); // check for winner
                } else {
                    // send message back to user that game piece is already claimed
                    String data = "{\"type\":\"failed\"";
                    data+=", \"row\":\"" + selection[0] +"\"";
                    data+=", \"column\":\"" + selection[1] +"\"";
                    data+=", \"message\":\"Game Piece is already claimed by other player.\"}";
                    session.getBasicRemote().sendText(data);
                }
            } else {
                // send player has no more lives
                session.getBasicRemote().sendText("{\"type\": \"lost\", \"message\":\"Sorry, no more lives.\"}");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // method to create json string
    public String getSendMsg(String type, GamePiece gp, long message) {
        // create json string for a game piece
        String val = "{\"type\": \"" + type + "\", ";
        val+= "\"piece\": \"" + gp.getItem() + "\", ";
        val+= "\"value\": \"" + gp.getValue() + "\", ";
        val+= "\"row\": \"" + gp.getxPos() + "\", ";
        val+= "\"column\": \"" + gp.getyPos() + "\", ";
        val+= "\"message\":\""+ message +"\"}";
        return val;
    }

    // method to check for winner
    public void checkWinner() throws IOException {
        if (gameRoom.hasWinner()) {
            String winner = gameRoom.getWinner();
            for (Session peer : session.getOpenSessions()) { // broadcast this person left the server
                if (peer.getId().equals(winner)) {
                    peer.getBasicRemote().sendText("{\"type\": \"winner\", \"message\":\"Congratulations!!! you have won the game.\"}");
                } else {
                    peer.getBasicRemote().sendText("{\"type\": \"winner\", \"message\":\"" + gameRoom.getPlayer(winner).getName() + " has won the game.\"}");
                }
            }
        }
    }
}
