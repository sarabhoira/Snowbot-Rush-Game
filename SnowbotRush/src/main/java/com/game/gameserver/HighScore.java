package com.game.gameserver;

import java.util.*;
import java.util.stream.Stream;

public class HighScore {
    private Map<String, Long> scores = new HashMap<String, Long>(); // list of all player scores
    private static HighScore highScore = null; // singleton instance
    private HighScore() {} // default constructor

    // static method to create instance of Singleton class
    public static synchronized HighScore getInstance()
    {
        if (highScore == null) {
            highScore = new HighScore();
        }
        return highScore;
    }

    // method to set score of player
    public void setHighScore(String playerName, long score) {
        this.scores.put(playerName.toUpperCase(), Long.valueOf(score));
    }

    // method to get all scores
    public Map<String, Long> getAllScores() {
        return scores;
    }

    // method to get score of a specific player
    public long getScore(String playerName) {
        Long score = Long.valueOf(0);
        if (playerName != null) {
            score = scores.get(playerName.toUpperCase());
        }
        return score.longValue();
    }

    // method to sort the scores of players and return top 5 high scores
    public List<Map.Entry<String, Long>> getSortedByScore() {
        Stream<Map.Entry<String,Long>> sorted = scores.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        return sorted.toList();
    }

    // method to check if there are no scores
    public boolean isEmpty() {
        return scores.isEmpty();
    }
}
