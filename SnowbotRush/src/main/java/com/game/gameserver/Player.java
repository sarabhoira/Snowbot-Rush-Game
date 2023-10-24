package com.game.gameserver;

public class Player {
    private String id; // player id
    private String name; // player name
    private long score; // current score of player
    private int lives; // number of lives player has

    // constructor
    public Player(String id, String name) {
        this.id = id;
        this.name = name;
        this.score = Constants.START_SCORE;
        this.lives = Constants.START_LIVES;
    }

    // method to get player id
    public String getId() {
        return id;
    }

    // method to get player name
    public String getName() {
        return name;
    }

    // method to set player name
    public void setName(String name) {
        this.name = name;
    }

    // method to get player score
    public long getScore() {
        return score;
    }

    // method to increment player score
    public void setScore(long score) {
        this.score += score;
    }

    // method to get player lives
    public int getLives() {
        return lives;
    }

    // method to add player lives
    // if player has all lives than do not increment
    public void setLives(int lives) {
        this.lives += lives;
        if (this.lives > Constants.START_LIVES) {
            this.lives = Constants.START_LIVES;
        }
    }

    // method to check if the player has lives
    public boolean hasLives() {
        return (this.lives>0);
    }
}