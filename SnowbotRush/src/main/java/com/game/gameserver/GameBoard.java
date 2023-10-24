package com.game.gameserver;

import java.util.Random;

public class GameBoard {
    GamePiece[][] gamePieces; // array to store game pieces
    int size; // declaring size of game board

    // method to create the game board
    public GameBoard(Constants.LEVEL level) {
        this.size = level.getValue(); // get the size of the game board based on the difficulty level
        this.gamePieces = new GamePiece[this.size][this.size]; // initialize the game board size
        generateGameBoard(); // generate the random pieces and assign to game board
    }

    // generating game board dynamically
    private void generateGameBoard() {
        Random random = new Random(); // randomly generating game board pieces
        for (int row=0; row<gamePieces.length; row++) { // loop through each row
            for (int column=0; column<gamePieces[row].length;column++) { // loop through each column
                int itemNum = random.nextInt(Constants.Items.values().length); // generate a random number for the game piece
                gamePieces[row][column] = new GamePiece(itemNum); // assign the game piece to the position on the board
                gamePieces[row][column].setPosition(row,column); // set the row and column value for that game piece
            }
        }
    }

    // method to get all game pieces for game board
    public GamePiece[][] getGamePieces() {
        return this.gamePieces;
    }

    // method to get a selected game piece on the row and column index
    public GamePiece getGamePiece (int row, int column) {
        return this.gamePieces[row][column];
    }

    // method to get the size of the game board
    public int getSize() {
        return this.size;
    }

    // method called to check if the game is over
    public boolean isGameOver() {
        boolean blnGameOver = true; // set the flag that game has ended
        for (int row=0; row<gamePieces.length && blnGameOver; row++) { // for each row of the game board
            for (int column=0; column<gamePieces[row].length && blnGameOver; column++) { // for each column of the game board
                if (!gamePieces[row][column].isClaimed()) { // check if the game piece was not claimed
                    blnGameOver = false; // reset the flag as game is still not over
                }
            }
        }
        return blnGameOver; // return the flag to indicate if game is over or still game pieces are left to be selected
    }
}
