package com.game.gameserver;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@ServerEndpoint(value="/ws/game/")
public class GameServer {

    private static Map<String, String> players = new HashMap<String, String>(); // list of all players and the game room they have joined
    private static List<GameRoom> gameRooms = new ArrayList<>(); // list of all the game rooms

    // method to start a new session with the client
    @OnOpen
    public void open(Session session) throws IOException, EncodeException {
    }

    // method called when ending a connection with the client
    @OnClose
    public void close(Session session) throws IOException, EncodeException {
        String playerId = session.getId(); // get player id
        if (players.containsKey(playerId)) { // find out which game this player belongs to
            String gameID = players.get(playerId); // retrieve the game id
            removeUserFromGame(playerId,gameID,session); // remove the user from the game
        }
    }

    // method to handle all incoming and outgoing requests and responses
    @OnMessage
    public void handleMessage(String comm, Session session) throws IOException, EncodeException {
        String userID = session.getId(); // get player id
        JSONObject jsonMsg = new JSONObject(comm); // build json object from input string sent by client
        String type = (String) jsonMsg.get("type"); // type of message sent by client
        String error = null; // initialize the error string
        if ("enter".equalsIgnoreCase(type)) { // if a user has entered the game room
            enterGameRoom(comm,session); // enter the user to a new or existing room
        } else if ("select".equalsIgnoreCase(type)) { // message indicating user has selected a game piece
            String gameID = players.get(userID); // get the game id
            if (gameID != null) { // game id should not be null
                GameRoom gameRoom = getGameRoom(gameID); // get users game room
                if (gameRoom != null && gameRoom.getPlayer(userID)!=null) { // game room should not be empty or null
                    if (Constants.Status.START.equals(gameRoom.getGameStatus())) { // check if game is in progress
                        String row = (String) jsonMsg.get("row"); // get the row of the game piece from the request
                        String column = (String) jsonMsg.get("column"); // get the column of the game piece from the request
                        int[] selection = new int[2]; // initialize user selection
                        selection[0] = Integer.parseInt(row);
                        selection[1] = Integer.parseInt(column);
                        // pass game room user selection and session
                        PlayerServerHandler psh = new PlayerServerHandler(gameRoom, selection, session);
                        psh.run(); // run the thread
                    } else if (Constants.Status.WAITING.equals(gameRoom.getGameStatus())) { // verify game is still in progress
                        error = "{\"type\": \"error\", \"message\":\"Please wait for player to connect.\"}";
                    } else {
                        error = "{\"type\": \"error\", \"message\":\"Please join a game to play.\"}";
                    }
                } else {
                    error = "{\"type\": \"error\", \"message\":\"Please join a game to play.\"}";
                }
            } else {
                error = "{\"type\": \"error\", \"message\":\"Please join a game to play.\"}";
            }
        } else {
            error = "{\"type\": \"error\", \"message\":\"Command not recognized.\"}";
        }
        if (error != null) {
            session.getBasicRemote().sendText(error); // send the error message to users
        }
    }

    // method to add user to a new or existing game room
    public void enterGameRoom(String comm,Session session) throws IOException, EncodeException {
        String id = session.getId(); // unique id of the user
        JSONObject jsonmsg = new JSONObject(comm); // build json object from input string sent by client
        String name = (String) jsonmsg.get("name"); // read the players name
        if (name != null) { // check if user has provided their name
            session.getBasicRemote().sendText("{\"type\": \"info\", \"message\":\"Welcome " + name.toUpperCase() +"!\"}"); // welcome the user
            String gameID = players.get(id); // to fetch if the user belongs to any game
            if (gameID != null) { // if game found remove the user
                removeUserFromGame(id,gameID,session); // remove the user from the previous game
            }
            String level = (String) jsonmsg.get("level"); // get the difficulty level selected by user
            GameRoom gameRoom = findAGameRoomForPlayer(id,name,level); // based on difficulty level, find a room for the user to join
            players.put(id, gameRoom.getGameID()); // add the user to the player list
            if (gameRoom.getNumOfPlayers() == Constants.MAX_PLAYERS) { // if all players have joined then game can be started
                session.getBasicRemote().sendText("{\"type\": \"info\", \"message\":\"Get Ready " + name.toUpperCase() +", Game Is About To Start!\"}");
                Set<GameServerHandler> allPlayers = new HashSet<>(); // initialize game server handler
                for (Session peer : session.getOpenSessions()) { // for each player initialize the thread to start the game
                    if (gameRoom.inRoom(peer.getId())) {
                        allPlayers.add(new GameServerHandler(gameRoom,peer)); // add player
                    }
                }
                Iterator iterator = allPlayers.iterator(); // iterate for all players in the game
                while (iterator.hasNext()) {
                    ((GameServerHandler)iterator.next()).run(); // run the thread
                }
                gameRoom.setGameStatus(Constants.Status.START); // set the game room status to start
            } else {
                session.getBasicRemote().sendText("{\"type\": \"info\", \"message\":\"Waiting For Other Players To Connect...\"}");
            }
        } else {
            session.getBasicRemote().sendText("{\"type\": \"error\", \"message\":\"Please provide your player name to play the game.\"}");
        }
    }

    // method to find the game room based on game id
    public GameRoom getGameRoom(String gameID) {
        for (GameRoom gr:gameRooms) {
            if (gr.getGameID().equals(gameID)) {
                return gr;
            }
        }
        return null;
    }

    // method to remove user from the game
    public void removeUserFromGame(String userID, String gameID, Session session) throws IOException {
        // remove user from player hash map
        // remove user from the game room
        GameRoom gameRoom = getGameRoom(gameID);
        if (gameRoom != null) {
            players.remove(userID); // remove player from all player list
            Player player = gameRoom.getPlayer(userID); // get player instance
            String playerName = player.getName(); // get player name
            gameRoom.removePlayer(userID); // remove player from game
            if (gameRoom.getNumOfPlayers() == 0) { // check if game room has no players left
                gameRooms.remove(gameRoom); // remove game room
            } else {
                Map<String, Player> players = gameRoom.getPlayers(); // inform other players that current user has left
                Set keys = players.keySet(); // set player
                for (Session peer : session.getOpenSessions()) { // broadcast this person left the server
                    if (keys.contains(peer.getId())) {
                        peer.getBasicRemote().sendText("{\"type\": \"info\", \"message\":\"" + playerName + " has left the game.\"}");
                    }
                }
            }
        }
    }

    // method to find a game for the user depending on game level
    public GameRoom findAGameRoomForPlayer(String userID, String playerName, String gameLevel) throws IOException {
        // loop through array list and see if there is any game available for user to join
        for (GameRoom gr:gameRooms) {
            if (gr.getNumOfPlayers() < Constants.MAX_PLAYERS) {
                if (gr.isLevel(gameLevel)) {
                    gr.addPlayer(userID,playerName);
                    return gr;
                }
            }
        }
        // if there is no game room then generate a new game id by calling the game servlet
        String gameID = getGameRoomID();
        GameRoom gameRoom = new GameRoom(gameID,gameLevel);
        // create a new game room and add the user
        gameRoom.addPlayer(userID,playerName);
        gameRooms.add(gameRoom);
        return gameRoom;
    }

    // method to get unique room id for new room
    public String getGameRoomID() throws IOException {
        // open a http connection to the game servlet
        String uriAPI = "http://localhost:8080/SnowbotRush-1.0-SNAPSHOT/game-servlet";
        URL url = new URL(uriAPI);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "text/plain");
        con.setRequestProperty("Accept", "text/plain");
        // allows us to write content to the outputStream
        con.setDoOutput(false);

        //reading and printing response
        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}