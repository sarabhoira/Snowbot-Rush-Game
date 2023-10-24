package com.game.gameserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.Session;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameServerHandler implements Runnable {
    private GameRoom gameRoom; // game room
    private Session session; // user session

    // constructor
    public GameServerHandler(GameRoom gameRoom, Session session) throws IOException {
        this.gameRoom = gameRoom;
        this.session = session;
    }

    // method to run the thread
    public void run() {
        try {
            // send game board size, player score, and number of lives to client
            session.getBasicRemote().sendText("{\"type\": \"game\", \"message\":\""+ gameRoom.getGameBoard().getSize() +"\"}");
            Player player = gameRoom.getPlayer(session.getId());
            session.getBasicRemote().sendText("{\"type\": \"points\", \"message\":\""+ player.getScore() +"\"}");
            session.getBasicRemote().sendText("{\"type\": \"lives\", \"message\":\""+ player.getLives() +"\"}");
            session.getBasicRemote().sendText("{\"type\": \"info\", \"message\":\"Game has started.  All the Best!!!\"}");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
