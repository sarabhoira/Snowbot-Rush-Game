package com.game.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.gameserver.Constants;
import com.game.gameserver.HighScore;
import com.game.util.FileReaderWriter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Path("/game")
public class GameResource {

    // api method to return high scores that include the name and the score of the top 5 players
    @GET
    @Path("/highscore")
    @Produces("application/json")
    public Response gameHighScore() {
        String val = "";
        //need to create object to build
        try {
            HighScore hs = HighScore.getInstance(); // get singleton instance of class high score
            if(hs.isEmpty()) { // check if high scores is empty
                getHighScoresFromFile(hs); // retrieve the high scores from the file on server
            }
            val = getHighScores(hs); // sort and return the top 5 high scores
        } catch (Exception e) {
            throw new RuntimeException(e); // throw exception if any error
        }
        Response myResp = Response.status(200).header("Content-Type", "application/json")
                .entity(val)
                .build();
        return myResp; // returning the json response
    }

    // method to read all the game scores from file
    public void getHighScoresFromFile(HighScore hs) throws JsonProcessingException {
        // load the resource directory where the file containing the scores is located
        URL url = this.getClass().getClassLoader().getResource(Constants.HIGH_SCORE_DIR);
        String highScores = ""; // variable to hold the high scores as a json string
        File mainDir = null; // variable for the main directory
        try {
            mainDir = new File(url.toURI()); // get main directory from the resource url
        } catch (URISyntaxException e) {
            throw new RuntimeException(e); // throw exception if any error
        }

        try {
            // call the file reader to return the high score json string
            highScores = FileReaderWriter.readHighScoresFile(mainDir,Constants.HIGH_SCORE_FILE);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e); // throw exception if any error
        }

        ObjectMapper objectMapper = new ObjectMapper(); // mapper class to help with converting json string into object
        JsonNode jsonNode = objectMapper.readTree(highScores); // create json node from the json string
        Iterator<JsonNode> iterator = jsonNode.elements(); // get iterator to iterate through the json node objects
        while(iterator.hasNext()) { // continue until each object has been iterated
            JsonNode js = iterator.next(); // get the next json node object
            String name = js.findValue("name").asText(); // read the player name
            String score = js.findValue("score").asText(); // read the player score
            hs.setHighScore(name, Long.parseLong(score)); // set the player name and score in high score singleton object
        }
    }

    // method to sort and create the json string of high scores with player ranking
    // sorting is done in descending order of score value
    private String getHighScores(HighScore highScore) {
        List<Map.Entry<String, Long>> values = highScore.getSortedByScore(); // retrieve all the sorted scores as a list
        String data = "{\"scores\": ["; // initialize the json string
        boolean blnDelimiter = false; // boolean variable to track if comma has to be appended to json data
        int count = 0; // count to track the rank and number of high scores added to json string
        for (Map.Entry<String, Long> record:values) { // for each score record
            count++; // increment the count
            if (count <= Constants.MAX_NUM_HIGH_SCORE) { // if count id less than number of top scores to return
                String name = record.getKey(); // get the player name
                Long score = record.getValue(); // get the player score
                if (blnDelimiter) { // check if this is the first record
                    data += ","; // append the comma to json string
                } else {
                    blnDelimiter = true; // set the boolean to true
                }
                // build the json string of high scores (rank, name, and score)
                data += "{";
                data += "\"rank\": \"" + count + "\",";
                data += "\"name\": \"" + name + "\",";
                data += "\"score\": \"" + score.longValue() + "\"";
                data += "}";
            }
        }
        data += "]}"; // append the closing json tag
        return data; // return high scores
    }
}