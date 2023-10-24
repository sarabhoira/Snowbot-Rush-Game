package com.game.gameserver;

// constants class created to hold all the static variables to be used in the application code
public final class Constants {
    private Constants(){} // default constructor
    public enum Status { WAITING, START, END } // game status
    public enum Items {COIN, JEWEL, DIAMOND, SNOWFLAKE, ENEMY, FREEZE} // game pieces
    public enum Choice {POINTS, LIVES, FREEZE} // game score type
    public static int START_SCORE = 0; // start score
    public static int START_LIVES = 7; // number of max lives
    public static int MAX_PLAYERS = 2; // number of max players that can play in the game
    public static int MAX_NUM_HIGH_SCORE = 5; // maximum number of players high scores to display
    public static String HIGH_SCORE_DIR = "/gameFiles"; // high score directory name
    public static String HIGH_SCORE_FILE = "highscores.json"; // high score file name
    public static String[][] GAME_PIECES = { // array to hold game piece name, type, and value
            {"coin", Choice.POINTS.name(), "10"}, // coin worth 10 points
            {"jewel", Choice.POINTS.name(), "25"}, // jewel worth 25 points
            {"diamond", Choice.POINTS.name(), "50"}, // diamond worth 50 points
            {"snowflake", Choice.LIVES.name(),"1"}, // snowflake gives 1 life
            {"enemy", Choice.LIVES.name(), "-1"}, // enemy takes away 1 life
            {"freeze", Choice.FREEZE.name(), "3"} // freeze makes the player pause for 3 seconds
    };

    // this is the enum class for the difficulty levels
    public enum LEVEL{
        EASY(10), // this is a 10 by 10 game board
        MEDIUM(12), // this is a 12 by 12 game board
        HARD(14); // this is a 14 by 14 game board

        private final int value; // this is for assigning custom values to the levels

        // methods to set and retrieve the custom value of difficulty level
        LEVEL(final int newValue) {
            value = newValue;
        }
        public int getValue() { return value; }
    }
}
