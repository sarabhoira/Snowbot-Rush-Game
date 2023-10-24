package com.game.gameserver;

public class GamePiece {
    private String item; // game piece type (coin/diamond...)
    private Constants.Choice type; // whether is a point or life or freeze
    private int value; // the value of the game piece
    private int xPos; // the row position of a game piece
    private int yPos; // the column position of a game piece
    private boolean claimed; // flag to indicate game piece is already selected by player

    // constructor to initialize game piece
    public GamePiece(int piece) {
        this.item = Constants.GAME_PIECES[piece][0];
        this.type = Constants.Choice.valueOf(Constants.GAME_PIECES[piece][1]);
        this.value = Integer.parseInt(Constants.GAME_PIECES[piece][2]);
        this.xPos = -1; // -1 means not initialized
        this.yPos = -1; // -1 means not initialized
        this.claimed = true;
    }

    // method to set the position for the game piece on the game board
    public void setPosition(int x, int y) {
        this.xPos = x;
        this.yPos = y;
        this.claimed = false;
    }

    // method to get the game piece
    public String getItem() {
        return item;
    }

    // method to get the row position of the game piece
    public int getxPos() {
        return xPos;
    }

    // method to get the column position of the game piece
    public int getyPos() {
        return yPos;
    }

    // method to know if game piece is already selected by any player
    public boolean isClaimed() {
        return claimed;
    }

    // method to set the piece that is already selected by any player
    public void setClaimed() {
        this.claimed = true;
    }

    // method to get the value
    public int getValue() {
        return value;
    }

    // method to get the type
    public Constants.Choice getType() {
        return type;
    }
}
