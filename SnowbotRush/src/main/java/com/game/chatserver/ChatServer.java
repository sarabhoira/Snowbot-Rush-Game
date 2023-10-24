package com.game.chatserver;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ServerEndpoint(value="/ws/game-chat/")
public class ChatServer {
    private Map<String, String> usernames = new HashMap<String, String>(); // holds the user id and username

    // method to accept new connection with client
    @OnOpen
    public void open(Session session) throws IOException, EncodeException {
        // send the welcome message to user, and request for username from player
        session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server ): Welcome to the chat room. Please state your username to begin.\"}");
    }

    // method invoked when websocket connection is closed between the server and client
    @OnClose
    public void close(Session session) throws IOException, EncodeException {
        String userId = session.getId(); // get the user id
        if (usernames.containsKey(userId)) { // check if name exists
            String username = usernames.get(userId); // read the username
            usernames.remove(userId); // remove the user
            if (!"".equalsIgnoreCase(username)) { // if username is not empty
                for (Session peer : session.getOpenSessions()) { //broadcast this person left the server
                    peer.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server): " + username + " left the chat room.\"}");
                }
            }
        }
    }

    // method to manage the chat messages between users
    @OnMessage
    public void handleMessage(String comm, Session session) throws IOException, EncodeException {
        String userID = session.getId(); // get chat user id
        JSONObject jsonmsg = new JSONObject(comm); // read the message sent by user
        String type = (String) jsonmsg.get("type"); // check for type of message sent by user
        String message = (String) jsonmsg.get("msg"); // check for message sent by user

        if (!"".equalsIgnoreCase(message)) { // if message is not empty
            if (usernames.containsKey(userID)) { // if username contains the user id then this is the chat message from user
                String username = usernames.get(userID); // retrieve the users name
                for (Session peer : session.getOpenSessions()) { // broadcast the message to all the users
                    peer.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(" + username + "): " + message + "\"}");
                }
            } else { //first message is their username
                usernames.put(userID, message); // read the username and welcome the user to the chat room
                session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server ): Welcome, " + message + "!\"}");
                for (Session peer : session.getOpenSessions()) {
                    // only announce to those in the same room as me, excluding myself
                    if (!peer.getId().equals(userID)) {
                        peer.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server): " + message + " joined the chat room.\"}");
                    }
                }
            }
        }
    }
}